package searchclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import atoms.Agent;
import atoms.Box;
import atoms.Position;
import atoms.World;
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
			Strategy strategy = null;
			/* 1. Create solutions for each agent */
			List<LinkedList<Node>> allSolutions = new ArrayList<LinkedList<Node>>();
			World world = World.getInstance();
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
						Agent newAgent = new Agent(agent.getId(), agent.getColor(), new Position(n.agentRow, n.agentCol));
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
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}