package searchclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import bdi.Intention;
import conflicts.Conflict;
import conflicts.Conflict.ConflictType;
import conflicts.DetectConflict;
import heuristics.AStar;
import searchclient.Search.SearchType;
import strategies.Strategy;
import strategies.StrategyBFS;
import strategies.StrategyBestFirst;

public class Run {

	public static void main(String[] args) throws Exception {
		System.err.println("SearchClient initializing. I am sending this using the error output stream.");
		SearchClient client = new SearchClient();
		client.init();
		SearchClient.TIME = args.length > 1 ? Integer.parseInt(args[1]) : 300;
		Run run = new Run();
		run.runSolution(client);
	}

	private void runSolution(SearchClient client) {
		World world = World.getInstance();
		do {
			Map<Integer, List<Node>> agentSolutions = new HashMap<Integer, List<Node>>(0);
			List<List<Node>> allSolutions = new ArrayList<List<Node>>(0);
			generatePlanAgents();
			/* 1. Create solutions for each agent */
			for (Agent a : world.getAgents().values()) {
				Strategy strategy = new StrategyBestFirst(new AStar(a.initialState));
				Search s = new Search();
				List<Node> solution = s.search(strategy, a.initialState, SearchType.PATH);
				if (solution != null && solution.size() > 0) {
					agentSolutions.put(a.getId(), solution);
					allSolutions.add(solution);
				}
			}
			world.setSolutionMap(agentSolutions);
			/* 2. Merge simple solutions together */
			int longestPlan = findLongestPlan(allSolutions);
			Map<Integer, Position> updatedAgentPositions = new HashMap<Integer, Position>(0);
			Map<Integer, Box> updatedBoxes = new HashMap<Integer, Box>(0);
			for (int stepInPlan = 0; stepInPlan < longestPlan; stepInPlan++) {
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				int i = 0;
				for (List<Node> solution : allSolutions) {
					if (stepInPlan < solution.size()) {
						sb.append(solution.get(stepInPlan).action.toString());
						Node n = solution.get(stepInPlan);
						System.err.println(n);
						Agent agent = world.getAgents().get(n.agentId);
						updatedAgentPositions.put(agent.getId(), new Position(n.agentRow, n.agentCol));
						for (Integer bId : n.boxes.keySet()) {
							updatedBoxes.put(bId, n.boxes.get(bId));
						}
					} else
						sb.append("NoOp");
					if (i < allSolutions.size() - 1)
						sb.append(", ");
					i++;
				}
				sb.append("]");
				DetectConflict d = new DetectConflict();
				Conflict c = d.checkConflict(stepInPlan);
				if (c != null) {
					if (c.getConflictType().equals(ConflictType.Agent)) {
						List<List<Node>> newPlans = solveAgentOnAgent(c.getNode(), c.getSender(), c.getReceiver(),
								stepInPlan);
						List<Node> newPlanForAgentToMove = newPlans.get(0);
						List<Node> newPlanForAgentToStay = newPlans.get(1);

						System.err.println("SolutionMap.size() = " + world.getSolutionMap().size());
						world.getSolutionMap().put(newPlanForAgentToMove.get(0).agentId, newPlanForAgentToMove);
						world.getSolutionMap().put(newPlanForAgentToStay.get(0).agentId, newPlanForAgentToStay);
						world.getBeliefs().add(c.getSender().getIntention().getDesire().getBelief());
						world.getBeliefs().add(c.getReceiver().getIntention().getDesire().getBelief());
					}
					break;
				} else {
					System.out.println(sb.toString());
					System.err.println(sb.toString());
					Utils.performUpdates(updatedAgentPositions, updatedBoxes);
				}
			}
			for (Agent a : world.getAgents().values()) {
				if (a.getIntention() != null) {
					Goal goal = a.getIntention().getDesire().getBelief().getGoal();
					world.getSolvedGoals().put(goal.getId(), goal);
				}
			}
			System.err.println("Global goal state found = " + world.isGlobalGoalState());
			System.err.println(world.toString());
		} while (!world.isGlobalGoalState());
	}

	public void generatePlanAgents() {
		for (Agent agent : World.getInstance().getAgents().values()) {
			agent.generateInitialState();
			if (!agent.generateDesires()) {
				continue;
			}
			if (!agent.generateIntention()) {
				continue;
			}
			Intention i = agent.getIntention();
			Goal g = i.getDesire().getBelief().getGoal();
			Box b = i.getBox();
			World.getInstance().getBeliefs().remove(i.getDesire().getBelief());
			agent.initialState.goals.put(g.getId(), g);
			agent.initialState.boxes.put(b.getId(), b);

			for (Goal goal : World.getInstance().getSolvedGoals().values()) {
				agent.initialState.walls.add(goal.getPosition());
			}
		}
	}

	public int findLongestPlan(List<List<Node>> allSolutions) {
		int size = 0;
		for (List<Node> solution : allSolutions)
			size = (size < solution.size() ? solution.size() : size);
		return size;
	}

	public List<List<Node>> solveAgentOnAgent(Node node, Agent a1, Agent a2, int index) {
		Agent agentToMove = a1.getPriority() > a2.getPriority() ? a1 : a2;
		Agent agentToStay = a1.getPriority() > a2.getPriority() ? a2 : a1;
		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(new Position(agentToMove.getPosition()));
		agentToMove.initialState.agentRow = agentToStay.getPosition().getX();
		agentToMove.initialState.agentCol = agentToStay.getPosition().getY();
		for (Box box : agentToMove.initialState.boxes.values()) {
			agentToMove.initialState.walls.add(new Position(box.getPosition()));
		}
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		s.setOtherPlan(updatePlan(agentToMove.getId(), index));
		List<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MoveAway);
		List<Node> newPlanAgentToStay = createNewPlanForStayingAgent(newPlanAgentToMove, agentToStay, agentToMove);
		for(int i = 0; i < newPlanAgentToMove.size(); i++) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			sb.append(newPlanAgentToMove.get(i).action.toString());
			sb.append(", ");
			sb.append(newPlanAgentToStay.get(i).action.toString());
			sb.append("]");
			System.out.println(sb.toString());
		}
		List<List<Node>> result = new ArrayList<List<Node>>();
		result.add(newPlanAgentToMove);
		result.add(newPlanAgentToStay);
		return result;
	}

	private List<Node> createNewPlanForStayingAgent(List<Node> planAgentToMove, Agent agentToStay, Agent agentToMove) {
		List<Node> newPlan = new LinkedList<Node>();
		agentToStay.initialState.agentRow = agentToMove.getPosition().getX();
		agentToStay.initialState.agentCol = agentToMove.getPosition().getY();
		for (int i = 0; i < planAgentToMove.size(); i++) {
			Node newN = agentToStay.initialState;
			newN.action = new Command();
			newPlan.add(newN);
		}
		return newPlan;
	}

	private List<Node> updatePlan(int agentId, int index) {
		List<Node> updPlan = new LinkedList<Node>();
		List<Node> oldPlan = World.getInstance().getSolutionMap().get(agentId);
		for (int i = 0; i < oldPlan.size(); i++) {
			if (i >= index - 1) {
				updPlan.add(oldPlan.get(i));
			}
		}
		return updPlan;
	}
}