package searchclient;

import java.io.IOException;
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
import conflicts.DetectConflict;
import heuristics.AStar;
import searchclient.SearchClient.SearchType;
import strategies.Strategy;
import strategies.StrategyBestFirst;

public class Run {

	public static void main(String[] args) throws Exception {
		System.err
				.println("SearchClient initializing. I am sending this using the error output stream.");
		SearchClient client = new SearchClient();
		SearchClient.TIME = args.length > 1 ? Integer.parseInt(args[1]) : 300;
		Run run = new Run();
		run.runSolution(client);
	}

	public void runSolution(SearchClient client) throws IOException {
		World world = World.getInstance();
		// boolean[] plans = new boolean[world.getAgents().size()];
		do {
			HashMap<Integer, LinkedList<Node>> agentSolutions = new HashMap<Integer, LinkedList<Node>>();
			generateAgentPlans();

			/* 1. Create solutions for each agent */
			List<LinkedList<Node>> allSolutions = new ArrayList<LinkedList<Node>>();
			for (Agent a : world.getAgents().values()) {
				Strategy strategy = new StrategyBestFirst(new AStar(
						a.initialState));
				// System.err.println(a.initialState);
				LinkedList<Node> solution = client.search(strategy,
						a.initialState, SearchType.PATH);
				if (solution != null && solution.size() > 0) {
					agentSolutions.put(a.getId(), solution);
					allSolutions.add(solution);
				}
			}

			World.getInstance().setSolutionMap(agentSolutions);

			/* 2. Merge simple solutions together */
			int longestPlan = findLongestSolution(allSolutions);

			Map<Integer, Position> updatedAgentPositions = new HashMap<Integer, Position>(
					0);
			Map<Integer, Box> updatedBoxes = new HashMap<Integer, Box>(0);

			boolean replan = false;

			for (int stepInPlan = 0; stepInPlan < longestPlan; stepInPlan++) {
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				int i = 0;
				for (LinkedList<Node> solution : allSolutions) {
					// set agent position
					if (stepInPlan < solution.size()) {
						sb.append(solution.get(stepInPlan).action.toString());
						Node n = solution.get(stepInPlan);
						// System.err.println(n);
						Agent agent = world.getAgents().get(n.agentId);
						updatedAgentPositions.put(agent.getId(), new Position(
								n.agentRow, n.agentCol));
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
					if (c.getConflictType().equals(Conflict.ConflictType.Agent)) {
						Map<Integer, Position> plan = new HashMap<Integer, Position>(
								0);

//						System.err.println(c.getSender().getId());
//						System.err.println(c.getReceiver().getId());
						// System.exit(0);
						int agentId = c.getNode().agentId;

						if (agentId >= allSolutions.size()) {
							continue;
						}
						Node n = allSolutions.get(agentId).get(stepInPlan - 1);
						n.parent = null;

						n.moveToPositionRow = 1;
						n.moveToPositionCol = 2;

						agentSolutions.put(c.getSender().getId(), executePlan(client, c.getNode(), c.getSender(), c.getReceiver(), agentSolutions, stepInPlan));
						
						World.getInstance().setSolutionMap(agentSolutions);
						
						plan.put(c.getSender().getId(), new Position(
								n.moveToPositionRow, n.moveToPositionCol));

						Utils.performUpdates(plan, null);

						// System.exit(0);
						replan = true;
						break;
					} else if (c.getConflictType().equals(
							Conflict.ConflictType.Box)) {

					}
				} else {
					System.out.println(sb.toString());
//					System.err.println(sb.toString());
					Utils.performUpdates(updatedAgentPositions, updatedBoxes);
				}
				if (replan)
					break;
			}
			for (Agent a : world.getAgents().values()) {
				if (a.getIntention() != null && !replan) {
					Goal goal = a.getIntention().getDesire().getBelief()
							.getGoal();
					world.getSolvedGoals().put(goal.getId(), goal);
				} else {
					System.err.println("something happened");
				}
			}
			System.err.println("Global goal state found = "
					+ world.isGlobalGoalState());
			// System.err.println(world.toString());

		} while (!world.isGlobalGoalState());

	}

	public void generateAgentPlans() {
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
			agent.initialState.goals.put(g.getId(), g);
			agent.initialState.boxes.put(b.getId(), i.getBox());

			for (Goal goal : World.getInstance().getSolvedGoals().values()) {
				agent.initialState.walls.add(goal.getPosition());
			}
		}
	}

	private LinkedList<Node> executePlan(SearchClient client, Node n,
			Agent sender, Agent receiver, HashMap<Integer, LinkedList<Node>> agentSolutions, int index) {
		
		Map<Integer, Position> updatedAgentPositions = new HashMap<Integer, Position>(
				0);
		n.moveToPositionRow = 1;
		n.moveToPositionCol = 2;
		
//		System.err.println("Receiver plan length: " + receiver.size());
//		System.err.println(index);
		
		Strategy strategy = new StrategyBestFirst(new AStar(n));
		LinkedList<Node> solution = client.search(strategy, n, SearchType.MoveToPosition);
		boolean hasWaited = false;
		if (solution != null) {
			for (Node s : solution) {
				
				updatedAgentPositions.put(sender.getId(), new Position(
						s.agentRow, s.agentCol));
				StringBuilder action = new StringBuilder();
				action.append("[");

				for (Agent a : World.getInstance().getAgents().values()) {
					if (a.getId() == World.getInstance().getAgents().size() - 1
							&& World.getInstance().getAgents().size() > 1)
						action.append(", ");
					
					// Continue receiver-agent's plan, but 
					if (a.getId() != sender.getId()) {
						if (!hasWaited) {
							hasWaited = true;
							action.append("NoOp");
							System.err.println(a.getId() + ": NoOp");
						} else if (receiver.getId() < agentSolutions.size()) {
							action.append(agentSolutions.get(receiver.getId()).get(index).action.toString());
							System.err.print(a.getId() + ": " + agentSolutions.get(receiver.getId()).get(index).action
									.toString());
							index++;
						} else {
							action.append("NoOp");
							System.err.println(a.getId() + ": NoOp");
						}
					} else {
						action.append(s.action.toString());
						System.err.println(a.getId() + ": " + s.action.toString());
					}
				}
				action.append("]");
				System.out.println(action.toString());
				Utils.performUpdates(updatedAgentPositions, null);
				// System.err.println(s);
			}
		}
		
		
//		World.getInstance().setSolutionMap(agentSolutions);
//		
//		plan.put(c.getSender().getId(), new Position(
//				n.moveToPositionRow, n.moveToPositionCol));
//
//		Utils.performUpdates(plan, null);
		
		System.err.println("replanning done");
		return solution;
	}

	private int findLongestSolution(List<LinkedList<Node>> allSolutions) {
		int result = 0;
		for (LinkedList<Node> solution : allSolutions) {
			if (result < solution.size()) {
				result = solution.size();
			}
		}
		return result;
	}
}