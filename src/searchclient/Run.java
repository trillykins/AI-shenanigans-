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
			client.init();
			if (args.length > 1)
				SearchClient.TIME = Integer.parseInt(args[1]);
			World world = World.getInstance();
			do {
				for (Integer id : world.getAgents().keySet()) {
					Agent agent = world.getAgents().get(id);
					agent.initialState = new Node(null, agent.getId());
					agent.initialState.agentRow = agent.getPosition().getX();
					agent.initialState.agentCol = agent.getPosition().getY();
					agent.initialState.boxes = new HashMap<Integer, Box>(0);
					agent.initialState.goals = new HashMap<Integer, Goal>(0);
					agent.generateDesires();
					agent.generateIntention();
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
				}
				Strategy strategy = null;
				/* 1. Create solutions for each agent */
				List<LinkedList<Node>> allSolutions = new ArrayList<LinkedList<Node>>();
				for (Integer id : world.getAgents().keySet()) {
					Agent a = world.getAgents().get(id);
					strategy = new StrategyBestFirst(new AStar(a.initialState));
					LinkedList<Node> solution = client.search(strategy, a.initialState);
					if (solution != null) {
						System.err.println("\nSummary for " + strategy);
						System.err.println("Found solution of length " + solution.size());
						System.err.println(strategy.searchStatus());
						allSolutions.add(solution);
					} else {
						System.err.println("!!!!!!");
					}
				}
				/* 2. Merge simple solutions together */
				int size = 0;
				for (LinkedList<Node> solution : allSolutions) {
					if (size < solution.size()) {
						size = solution.size();
					}
				}
				Map<Integer, Agent> updatedAgents = new HashMap<Integer, Agent>(0);
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
							Agent agent = world.getAgents().get(n.agentId);
							Agent newAgent = new Agent(agent.getId(), agent.getColor(),
									new Position(n.agentRow, n.agentCol));
							updatedAgents.put(newAgent.getId(), newAgent);
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
						Utils.performUpdates(updatedAgents, updatedBoxes);
						System.out.println(sb.toString());
						System.err.println(sb.toString());
					} else {
						break;
					}
				}
				System.err.println("Global goal state found = " + world.isGlobalGoalState());
				System.err.println(world.toString());
			} while (!world.isGlobalGoalState());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}