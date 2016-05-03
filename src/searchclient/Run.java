package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import atoms.Agent;
import atoms.Box;
import atoms.Position;
import atoms.World;
import conflicts.Conflict;
import conflicts.DetectConflict;
import heuristics.AStar;
import searchclient.Search.SearchType;
import strategies.Strategy;
import strategies.StrategyBestFirst;

public class Run {
	private World world = World.getInstance();

	public static void main(String[] args) throws Exception {
		System.err.println("SearchClient initializing. I am sending this using the error output stream.");
		SearchClient client = new SearchClient();
		client.init();
		SearchClient.TIME = args.length > 1 ? Integer.parseInt(args[1]) : 300;
		Run run = new Run();
		run.runSolution(client);
	}

	private void runSolution(SearchClient client) {
		if (world.getAgents().size() == 1) {
			SAPlanner();
		} else {
			MAPlanner();
		}
	}

	private void SAPlanner() {
		boolean replanned = false;
		while (!world.isGlobalGoalState()) {
			List<List<Node>> allSolutions = new ArrayList<>(0);
			if (!replanned) {
				Map<Integer, List<Node>> agentSolutions = new HashMap<>(0);
				/* 1. Create solutions for each agent */
				Agent a = world.getAgents().get(0);
				world.generatePlan(a);
				Strategy strategy = new StrategyBestFirst(new AStar(a.initialState));
				Search s = new Search();
				List<Node> solution = s.search(strategy, a.initialState, SearchType.PATH);
				for (Box box : World.getInstance().getBoxes().values()) {
					if (a.getIntention() != null && !box.equals(a.getIntention().getBox())) {
						a.initialState.boxes.remove(box.getId());
					}
				}
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
				world.setSolutionMap(agentSolutions);
			} else {
				for (List<Node> solution : world.getSolutionMap().values()) {
					allSolutions.add(solution);
				}
			}

			List<Node> plan = world.getSolutionMap().get(0);
			Map<Integer, Position> updatedAgentPositions = new HashMap<Integer, Position>(0);
			Map<Integer, Box> updatedBoxes = new HashMap<Integer, Box>(0);
			for (int stepInPlan = 0; stepInPlan < plan.size(); stepInPlan++) {
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				Node n = plan.get(stepInPlan);
				sb.append(n.action.toString());
				Agent agent = world.getAgents().get(n.agentId);
				updatedAgentPositions.put(agent.getId(), new Position(n.agentRow, n.agentCol));
				for (Box box : n.boxes.values()) {
					updatedBoxes.put(box.getId(), box);
				}
				sb.append("]");
				DetectConflict detectCon = new DetectConflict();
				Conflict con = detectCon.checkConflict(stepInPlan);
				if (con != null && !replanned) {
					switch (con.getConflictType()) {
					case AGENT:
						con.solveAgentOnAgent(con.getNode(), con.getSender(), con.getReceiver(), stepInPlan,
								allSolutions);
						break;
					case SINGLE_AGENT_BOX:
						System.err.println("BOX CONFLICT");
						con.solveAgentOnBox(con.getNode(), World.getInstance().getAgents().get(0), con.getReceiverBox(),
								stepInPlan, allSolutions);
						break;
					case BOX_BOX:
						System.err.println("BOX_BOX CONFLICT");
						break;
					default:
						System.err.println("UNDEFINED CONFLICT");
						break;
					}
					replanned = true;
					break;
				} else {
					replanned = false;
					System.out.println(sb.toString());
					System.err.println(sb.toString());
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
						if (in.ready())
							in.readLine();
					} catch (IOException e) {
						System.err.println(e.getMessage());
					}
					Utils.performUpdates(updatedAgentPositions, updatedBoxes);
				}
				world.updateBeliefs();
				System.err.println("World:\n" + world.toString());
				System.err.println("Global goal state found = " + world.isGlobalGoalState());
			}
		}
	}

	private void MAPlanner() {
		boolean replanned = false;
		while (!world.isGlobalGoalState()) {
			List<List<Node>> allSolutions = new ArrayList<List<Node>>(0);
			if (!replanned) {
				Map<Integer, List<Node>> agentSolutions = new HashMap<Integer, List<Node>>(0);
				world.generatePlans();
				/* 1. Create solutions for each agent */
				for (Agent a : world.getAgents().values()) {
					Strategy strategy = new StrategyBestFirst(new AStar(a.initialState));
					Search s = new Search();
					List<Node> solution = s.search(strategy, a.initialState, SearchType.PATH);
					for (Box box : World.getInstance().getBoxes().values()) {
						if (a.getIntention() != null && !box.equals(a.getIntention().getBox())) {
							a.initialState.boxes.remove(box.getId());
						}
					}
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
				replanned = false;
			}

			int size = world.findLongestPlan();
			Map<Integer, Position> updatedAgentPositions = new HashMap<Integer, Position>(0);
			Map<Integer, Box> updatedBoxes = new HashMap<Integer, Box>(0);
			for (int stepInPlan = 0; stepInPlan < size; stepInPlan++) {
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				int i = 0;
				for (List<Node> solution : world.getSolutionMap().values()) {
					if (stepInPlan < solution.size()) {
						sb.append(solution.get(stepInPlan).action.toString());
						Node n = solution.get(stepInPlan);
						Agent agent = world.getAgents().get(n.agentId);
						updatedAgentPositions.put(agent.getId(), new Position(n.agentRow, n.agentCol));
						for (Box box : n.boxes.values()) {
							updatedBoxes.put(box.getId(), box);
						}
					} else
						sb.append("NoOp");
					if (i < allSolutions.size() - 1)
						sb.append(", ");
					i++;
				}
				sb.append("]");
				DetectConflict detectCon = new DetectConflict();
				Conflict con = detectCon.checkConflict(stepInPlan);
				if (con != null && !replanned) {
					switch (con.getConflictType()) {
					case AGENT:
						con.solveAgentOnAgent(con.getNode(), con.getSender(), con.getReceiver(), stepInPlan,
								allSolutions);
						break;
					case SINGLE_AGENT_BOX:
						System.err.println("BOX CONFLICT");
						System.err.println(con.getReceiverBox());
						con.solveAgentOnBox(con.getNode(), World.getInstance().getAgents().get(0), con.getReceiverBox(),
								stepInPlan, allSolutions);
						break;
					case BOX_BOX:
						con.solveBoxOnBox(con, stepInPlan, allSolutions);
						break;
					default:
						break;
					}
					replanned = true;
					break;
				} else {
					replanned = false;
					System.out.println(sb.toString());
					System.err.println(sb.toString());
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
						if (in.ready())
							in.readLine();
					} catch (IOException e) {
						System.err.println(e.getMessage());
					}
					Utils.performUpdates(updatedAgentPositions, updatedBoxes);
				}
				if (world.isGlobalGoalState()) {
					System.err.println("DONE");
					return;
				}

				world.updateBeliefs();
				System.err.println("World:\n" + world.toString());
				System.err.println("Global goal state found = " + world.isGlobalGoalState());
			}
		}

	}
}