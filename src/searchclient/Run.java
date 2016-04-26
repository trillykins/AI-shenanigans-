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
		boolean replanned = false;
		do {
			List<List<Node>> allSolutions = new ArrayList<List<Node>>(0);
			if (!replanned) {
				Map<Integer, List<Node>> agentSolutions = new HashMap<Integer, List<Node>>(0);
				generatePlanAgents();
				/* 1. Create solutions for each agent */
				for (Agent a : world.getAgents().values()) {
					Strategy strategy = new StrategyBestFirst(new AStar(a.initialState));
					Search s = new Search();
					List<Node> solution = s.search(strategy, a.initialState, SearchType.PATH);
					if (solution != null && solution.size() > 0) {
						agentSolutions.put(a.getId(), solution);
						allSolutions.add(solution);
					} else {
						List<Node> empty = new LinkedList<Node>();
						Node noOp = a.initialState;
						noOp.action = new Command();
						empty.add(noOp);
						agentSolutions.put(a.getId(), empty);
						allSolutions.add(empty);
					}
				}
				world.setSolutionMap(agentSolutions);
			} else {
				for (List<Node> solution : world.getSolutionMap().values()) {
					allSolutions.add(solution);
				}
			}
			
			System.err.println(world.getSolutionMap().size());
			/* 2. Merge simple solutions together */
			int longestPlan = findLongestPlan();
			System.err.println("longestPlan: " + longestPlan);
			Map<Integer, Position> updatedAgentPositions = new HashMap<Integer, Position>(0);
			Map<Integer, Box> updatedBoxes = new HashMap<Integer, Box>(0);
			plan: for (int stepInPlan = 0; stepInPlan < longestPlan; stepInPlan++) {
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				int i = 0;
				for (List<Node> solution : world.getSolutionMap().values()) {
					if(world.getSolutionMap().size() != world.getAgents().size())
						System.err.println("AGENT AND PLAN MISMATCH!");
					if (stepInPlan < solution.size()) {
						sb.append(solution.get(stepInPlan).action.toString());
						Node n = solution.get(stepInPlan);

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
								stepInPlan, allSolutions);
						List<Node> newPlanForAgentToMoveAway = newPlans.get(0);
						List<Node> newPlanForAgentToMoveToGoal = newPlans.get(1);
						world.getSolutionMap().put(newPlanForAgentToMoveAway.get(0).agentId, newPlanForAgentToMoveAway);
						world.getSolutionMap().put(newPlanForAgentToMoveToGoal.get(1).agentId,
								newPlanForAgentToMoveToGoal);
						Agent agentToMoveAway = World.getInstance().getAgents()
								.get(newPlanForAgentToMoveAway.get(0).agentId);
						world.getBeliefs().add(agentToMoveAway.getIntention().getDesire().getBelief());
						// world.getBeliefs().add(c.getReceiver().getIntention().getDesire().getBelief());
					}
					replanned = true;
					System.err.println("replan done");
					break plan;
				} else {
					replanned = false;
					System.out.println(sb.toString());
					System.err.println(sb.toString());
					Utils.performUpdates(updatedAgentPositions, updatedBoxes);
				}
			}
			for (Agent a : world.getAgents().values()) {
				if (a.getIntention() != null) {
					Goal goal = a.getIntention().getDesire().getBelief().getGoal();
					for (Box box : World.getInstance().getBoxes().values()) {
						if (goal.getPosition().equals(box.getPosition())) {
							world.getSolvedGoals().put(goal.getId(), goal);
							System.err.println("solved goal");
						}
					}
				}
			}
			System.err.println("Global goal state found = " + world.isGlobalGoalState());
			System.err.println("World:\n" + world.toString());
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

	public int findLongestPlan() {
		int size = 0;
		for (List<Node> solution : World.getInstance().getSolutionMap().values())
			size = (size < solution.size() ? solution.size() : size);
		return size;
	}

	public List<List<Node>> solveAgentOnAgent(Node node, Agent a1, Agent a2, int index, List<List<Node>> allSolutions) {
		Agent agentToMove = a1.getPriority() > a2.getPriority() ? a2 : a1;
		Agent agentToStay = a1.getPriority() > a2.getPriority() ? a1 : a2;

		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(new Position(agentToStay.getPosition()));
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();

		for (Box box : agentToMove.initialState.boxes.values()) {
			agentToMove.initialState.walls.add(new Position(box.getPosition()));
		}
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		s.setPlanForAgentToStay(updatePlan(agentToStay.getId(), index));
		List<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MoveAway);
		List<Node> newPlanAgentToStay = allSolutions.get(agentToStay.getId());
		for (int i = 0; i < index - 1; i++) {
			newPlanAgentToStay.remove(0);
		}
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));
		Node noOp = agentToStay.initialState;
		noOp.action = new Command();
		newPlanAgentToStay.add(0, noOp);
		
		
		
		// World.getInstance().getSolutionMap().put(agentToMove.getId(),
		// newPlanAgentToMove);
		// World.getInstance().getSolutionMap().put(agentToStay.getId(),
		// newPlanAgentToStay);
		// int size = Math.max(newPlanAgentToMove.size(),
		// newPlanAgentToStay.size());
		// for (int i = 0; i < size; i++) {
		// StringBuilder sb = new StringBuilder();
		// sb.append("[");
		// if(agentToMove.getId() < agentToStay.getId()) {
		// if(i >= newPlanAgentToMove.size()) {
		// sb.append("NoOp");
		// } else {
		// sb.append(newPlanAgentToMove.get(i).action.toString());
		// }
		// sb.append(", ");
		// if(i >= newPlanAgentToStay.size()) {
		// sb.append("NoOp");
		// } else {
		// sb.append(newPlanAgentToStay.get(i).action.toString());
		// }
		// } else {
		// if(i >= newPlanAgentToStay.size()) {
		// sb.append("NoOp");
		// } else {
		// sb.append(newPlanAgentToStay.get(i).action.toString());
		// }
		// sb.append(", ");
		// if(i >= newPlanAgentToMove.size()) {
		// sb.append("NoOp");
		// } else {
		// sb.append(newPlanAgentToMove.get(i).action.toString());
		// }
		// }
		//
		// sb.append("]");
		// System.out.println(sb.toString());
		// System.err.println(sb.toString());
		// }
		List<List<Node>> result = new ArrayList<List<Node>>();
		result.add(newPlanAgentToMove);
		result.add(newPlanAgentToStay);
		return result;
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