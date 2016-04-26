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
import atoms.Goal;
import atoms.Position;
import atoms.World;
import bdi.Belief;
import bdi.Intention;
import conflicts.Conflict;
import conflicts.Conflict.ConflictType;
import conflicts.DetectConflict;
import heuristics.AStar;
import searchclient.Search.SearchType;
import strategies.Strategy;
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
				System.err.println("generatePlanAgents");
				/* 1. Create solutions for each agent */
				for (Agent a : world.getAgents().values()) {
					Strategy strategy = new StrategyBestFirst(new AStar(a.initialState));
					Search s = new Search();
					System.err.println(a.initialState);
					List<Node> solution = s.search(strategy, a.initialState, SearchType.PATH);
					for (Box box : World.getInstance().getBoxes().values()) {
						if (!box.equals(a.getIntention().getBox())) {
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
			}
			/* 2. Merge simple solutions together */
			int longestPlan = world.findLongestPlan();
			System.err.println("longestPlan: " + longestPlan);
			Map<Integer, Position> updatedAgentPositions = new HashMap<Integer, Position>(0);
			Map<Integer, Box> updatedBoxes = new HashMap<Integer, Box>(0);
			for (int stepInPlan = 0; stepInPlan < longestPlan; stepInPlan++) {
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
				// DetectConflict d = new DetectConflict();
				// Conflict c = d.checkConflict(stepInPlan);
				// if (c != null) {
				// System.err.println("CONFLICT: " + c.getConflictType());
				// if (c.getConflictType().equals(ConflictType.Agent)) {
				// c.solveAgentOnAgent(c.getNode(), c.getSender(),
				// c.getReceiver(), stepInPlan, allSolutions);
				// } else if (c.getConflictType().equals(ConflictType.Box)) {
				// System.err.println(c.getNode());
				// System.exit(1);
				// }
				// replanned = true;
				// break plan;
				// } else {
				replanned = false;
				System.out.println(sb.toString());
				System.err.println(sb.toString());
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					in.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(0);
				}
				Utils.performUpdates(updatedAgentPositions, updatedBoxes);
			}
			// }
			// for (Agent a : world.getAgents().values()) {
			// if (a.getIntention() != null) {
			// for (Goal goal : world.getGoals().values()) {
			// boolean solved = false;
			// for (Box box : world.getBoxes().values()) {
			// if (goal.getPosition().equals(box.getPosition()) &&
			// goal.getLetter() == Character.toLowerCase(box.getLetter())) {
			// world.getSolvedGoals().put(goal.getId(), goal);
			//// box.setOnGoal(true);
			// solved = true;
			// System.err.println("solved goal " + goal.getLetter());
			// }
			// }
			// if(!solved) {
			// world.getSolvedGoals().remove(goal);
			// }
			// }
			// System.err.println("Solved goals: " + world.getSolvedGoals());
			// }
			// }
			System.err.println("Global goal state found = " + world.isGlobalGoalState());
			System.err.println("World:\n" + world.toString());
			updateBeliefs();
		} while (!world.isGlobalGoalState());
	}

	public void updateBeliefs() {
		for(Goal goal : World.getInstance().getGoals().values()) {
			if(!goal.isSolved()) {
				if(!World.getInstance().getBeliefs().contains(goal)) {
					World.getInstance().getBeliefs().add(new Belief(goal));
				}
			}
		}
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
			Intention intention = agent.getIntention();
			Goal goal = intention.getDesire().getBelief().getGoal();
			Box intentionBox = intention.getBox();
			World.getInstance().getBeliefs().remove(intention.getDesire().getBelief());
			agent.initialState.goals.put(goal.getId(), goal);
			agent.initialState.boxes.put(intentionBox.getId(), intentionBox);

			// Add boxes of same color to the initialstate.
			for (Box box : World.getInstance().getBoxes().values()) {
				if (box.getColor().equals(agent.getColor())) {
					agent.initialState.boxes.put(box.getId(), box);
				}
			}
		}
	}
}