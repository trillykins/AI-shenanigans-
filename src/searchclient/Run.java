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
import heuristics.AStar;
import strategies.Strategy;
import strategies.StrategyBestFirst;

public class Run {

	public static void main(String[] args) throws Exception {
		System.err.println("SearchClient initializing. I am sending this using the error output stream.");
		try {
			SearchClient client = new SearchClient();
			SearchClient.TIME = args.length > 1 ? Integer.parseInt(args[1]) : 300;

			World world = World.getInstance();
			do {
				for (Agent agent : world.getAgents().values()) {
					agent.generateInitialState();
					if (!agent.generateDesires()) {
						continue;
					}
					if (!agent.generateIntention()) {
						continue;
					}
					Intention i = agent.getIntention();
					Goal g = i.getDesire().getBelief().getGoal();
					world.getBeliefs().remove(i.getDesire().getBelief());
					agent.initialState.goals.put(g.getId(), g);
					for (Integer boxId : world.getBoxes().keySet()) {
						Box b = world.getBoxes().get(boxId);
						if (Character.toLowerCase(b.getLetter()) == g.getLetter()) {
							agent.initialState.boxes.put(b.getId(), b);
						}
					}
					for (Goal goal : world.getSolvedGoals().values()) {
						agent.initialState.walls.add(goal.getPosition());
					}
				}

				/* 1. Create solutions for each agent */
				List<LinkedList<Node>> allSolutions = new ArrayList<LinkedList<Node>>();
				for (Agent a : world.getAgents().values()) {
					Strategy strategy = new StrategyBestFirst(new AStar(a.initialState));
					LinkedList<Node> solution = client.search(strategy, a.initialState);
					if (solution != null) {
						// if(solution.size() == 0) {
						// System.err.println("Solution of length 0....");
						// System.err.println(a.initialState);
						// }
						// System.err.println("\nSummary for " + strategy);
						// System.err.println("Found solution of length " +
						// solution.size());
						// System.err.println(strategy.searchStatus());
						allSolutions.add(solution);
					} else {
						// System.err.println("!!!!!!");
					}
				}

				/* 2. Merge simple solutions together */
				int size = 0;
				for (LinkedList<Node> solution : allSolutions) {
					if (size < solution.size()) {
						size = solution.size();
					}
				}
				Map<Integer, Position> updatedAgentPositions = new HashMap<Integer, Position>(0);
				Map<Integer, Box> updatedBoxes = new HashMap<Integer, Box>(0);
				for (int m = 0; m < size; m++) {
					StringBuilder sb = new StringBuilder();
					sb.append("[");
					int i = 0;
					for (LinkedList<Node> solution : allSolutions) {
						// set agent position
						if (m < solution.size()) {
							sb.append(solution.get(m).action.toString());
							Node n = solution.get(m);
							// System.err.println(n.toString());
							Agent agent = world.getAgents().get(n.agentId);
							// Agent newAgent = new Agent(agent.getId(),
							// agent.getColor(), new Position(n.agentRow,
							// n.agentCol));
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
					if (SearchClient.canMakeNextMove(m, allSolutions)) {
						System.err.println(world.toString());
						Utils.performUpdates(updatedAgentPositions, updatedBoxes);
						System.out.println(sb.toString());
						System.err.println(sb.toString());
					} else {
						System.err.println("bræk!");
						break;
					}
				}
				for (Agent a : world.getAgents().values()) {
					if (a.getIntention() != null) {
						Goal goal = a.getIntention().getDesire().getBelief().getGoal();
						world.getSolvedGoals().put(goal.getId(), goal);
					}
				}
				System.err.println("Global goal state found = " + world.isGlobalGoalState());
				// System.err.println(world.toString());
			} while (!world.isGlobalGoalState());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}