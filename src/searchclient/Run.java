package searchclient;

import heuristics.AStar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import searchclient.SearchClient.SearchType;
import strategies.Strategy;
import strategies.StrategyBestFirst;
import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import bdi.Intention;
import conflicts.Conflict;
import conflicts.DetectConflict;

public class Run {

	public static void main(String[] args) throws Exception {
		System.err.println("SearchClient initializing. I am sending this using the error output stream.");
		try {
			SearchClient client = new SearchClient();
			SearchClient.TIME = args.length > 1 ? Integer.parseInt(args[1]) : 300;

			World world = World.getInstance();
			HashMap<Integer, LinkedList<Node>> agentSolutions = new HashMap<Integer, LinkedList<Node>>();
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
					agent.initialState.boxes.put(i.getBox().getId(), i.getBox());
//					for (Box b : world.getBoxes().values()) {
//						if (Character.toLowerCase(b.getLetter()) == g.getLetter()) {
//							agent.initialState.boxes.put(b.getId(), b);
//						}
//					}
					for (Goal goal : world.getSolvedGoals().values()) {
						agent.initialState.walls.add(goal.getPosition());
					}
					System.err.println(agent.getIntention());
					System.err.println(agent.initialState);
				}
				System.err.println("number of agents: " + world.getAgents().size());
				/* 1. Create solutions for each agent */
				List<LinkedList<Node>> allSolutions = new ArrayList<LinkedList<Node>>();
				for (Agent a : world.getAgents().values()) {
					Strategy strategy = new StrategyBestFirst(new AStar(a.initialState));
					LinkedList<Node> solution = client.search(strategy, a.initialState, SearchType.PATH);
					if (solution != null && solution.size() > 0) {
						System.err.println("Agent " + a.getId() + " initial state = \n" + a.initialState);
						agentSolutions.put(a.getId(), solution);
						allSolutions.add(solution);
					}
				}
				World.getInstance().setSolutionMap(agentSolutions);

				/* 2. Merge simple solutions together */
				int size = 0;
				for (LinkedList<Node> solution : allSolutions) {
					if (size < solution.size()) {
						size = solution.size();
					}
				}
				Map<Integer, Position> updatedAgentPositions = new HashMap<Integer, Position>(0);
				Map<Integer, Box> updatedBoxes = new HashMap<Integer, Box>(0);
				for (int stepInPlan = 0; stepInPlan < size; stepInPlan++) {
					StringBuilder sb = new StringBuilder();
					sb.append("[");
					int i = 0;
					for (LinkedList<Node> solution : allSolutions) {
						// set agent position
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
//						if(c.getConflictType().equals(Conflict.ConflictType.Box_Box)) {
//							SolveBoxWithBoxConflict solve = new SolveBoxWithBoxConflict();
//							solve.solveConflicts();
//						}
						System.err.println("break: " + c);
						break;
					} else {
						System.out.println(sb.toString());
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
		} catch (

		IOException e)

		{
			System.err.println(e.getMessage());
		}

	}

	private static void runSolution() {

	}
}