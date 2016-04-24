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
import bdi.Belief;
import bdi.Intention;
import conflicts.Conflict;
import conflicts.DetectConflict;
import heuristics.AStar;
import searchclient.SearchClient.SearchType;
import strategies.Strategy;
import strategies.StrategyBestFirst;

public class Run {

	public static void main(String[] args) throws Exception {
		System.err.println("SearchClient initializing. I am sending this using the error output stream.");
		try {
			SearchClient client = new SearchClient();
			SearchClient.TIME = args.length > 1 ? Integer.parseInt(args[1]) : 300;

			World world = World.getInstance();
			HashMap<Integer, LinkedList<Node>> agentSolutions = new HashMap<Integer, LinkedList<Node>>();
			boolean[] pikhoved = new boolean[world.getAgents().size()];
			do {
				for (Agent agent : world.getAgents().values()) {
					pikhoved[agent.getId()] = true;
					agent.generateInitialState();
					if (!agent.generateDesires()) {
						pikhoved[agent.getId()] = false;
						continue;
					}
					if (!agent.generateIntention()) {
						pikhoved[agent.getId()] = false;
						continue;
					}
					Intention i = agent.getIntention();
					Goal g = i.getDesire().getBelief().getGoal();
					world.getBeliefs().remove(i.getDesire().getBelief());
					Belief b = i.getDesire().getBelief();
					b.setReserved(true);
					world.getBeliefs().add(b);
					agent.initialState.goals.put(g.getId(), g);
					agent.initialState.boxes.put(i.getBox().getId(), i.getBox());

					 for (Goal goal : world.getSolvedGoals().values()) {
					 agent.initialState.walls.add(goal.getPosition());
					 }
					System.err.println(agent.getIntention());
					System.err.println(agent.initialState);
				}
				System.err.println("number of agents: " + world.getAgents().size());

				if(!kusse(pikhoved)){
					System.err.println("LOOOOOOOOOOOOOOORT!!!");
					System.exit(0);
				}
				
				/* 1. Create solutions for each agent */
				List<LinkedList<Node>> allSolutions = new ArrayList<LinkedList<Node>>();
				for (Agent a : world.getAgents().values()) {
					Strategy strategy = new StrategyBestFirst(new AStar(a.initialState));
					System.err.println(a.initialState);
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
				System.err.println(size);
				Map<Integer, Position> updatedAgentPositions = new HashMap<Integer, Position>(0);
				Map<Integer, Box> updatedBoxes = new HashMap<Integer, Box>(0);
				boolean replan = false;
				for (int stepInPlan = 0; stepInPlan < size; stepInPlan++) {
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

						if (c.getConflictType().equals(Conflict.ConflictType.Agent)) {
							Map<Integer, Position> pik02 = new HashMap<Integer, Position>(0);
							int agentId = c.getNode().agentId;
							Node n = allSolutions.get(agentId).get(stepInPlan - 1);
							n.parent = null;
							System.err.println("BRÆK!" + "\n" + n);

							n.moveToPositionRow = 1;
							n.moveToPositionCol = 8;

							executePlan(client, n, world, agentId);
							pik02.put(agentId, new Position(n.moveToPositionRow, n.moveToPositionCol));
							
							Utils.performUpdates(pik02, null);

//							System.exit(0);
							replan = true;
							break;
						} else if (c.getConflictType().equals(Conflict.ConflictType.Box)) {

						}
					} else {
						System.out.println(sb.toString());
						Utils.performUpdates(updatedAgentPositions, updatedBoxes);
					}
					if (replan)
						break;
				}
				for (Agent a : world.getAgents().values()) {
					if (a.getIntention() != null && !replan) {
						Goal goal = a.getIntention().getDesire().getBelief().getGoal();
						world.getSolvedGoals().put(goal.getId(), goal);
					} else {
						System.err.println("lort");
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
	
	private static boolean kusse(boolean...fuckface){
		for(boolean fuck : fuckface){
			if(!fuck)
				return false;
		}
		return true;
	}

	private static void executePlan(SearchClient client, Node n, World world, int agentId) {
		Map<Integer, Position> updatedAgentPositions = new HashMap<Integer, Position>(0);
		System.err.println("EXECUTING PLAN!");
		Strategy strategy = new StrategyBestFirst(new AStar(n));
		LinkedList<Node> solution = client.search(strategy, n, SearchType.MoveToPosition);
		
		if (solution != null) {
			System.err.println(solution.size());
			for (Node s : solution) {
				updatedAgentPositions.put(agentId, new Position(s.agentRow, s.agentCol));
				StringBuilder pik = new StringBuilder();
				pik.append("[");

				for (Agent a : world.getAgents().values()) {
					if (a.getId() == world.getAgents().size() - 1 && world.getAgents().size() > 1)
						pik.append(", ");

					if (a.getId() != agentId) {
						pik.append("NoOp");
					} else {
						pik.append(s.action.toString());
					}
				}
				pik.append("]");
				System.out.println(pik.toString());
				Utils.performUpdates(updatedAgentPositions, null);
				System.err.println(s);
			}
		}
		System.err.println("PLAN EXECUTED!");
	}
}