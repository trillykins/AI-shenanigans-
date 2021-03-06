package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import conflicts.Conflict;
import conflicts.DetectConflict;
import conflicts.MABoxConflicts;
import heuristics.AStar;
import searchclient.Search.SearchType;
import strategies.Strategy;
import strategies.StrategyBestFirst;
import utils.Utils;

public class Run {
	private World world = World.getInstance();

	private static int conflictId = -1;

	public static void main(String[] args) throws Exception {
		SearchClient client = new SearchClient();
		client.init();
		SearchClient.TIME = args.length > 1 ? Integer.parseInt(args[1]) : 300;
		Run run = new Run();
		run.runSolution(client);
	}

	private void runSolution(SearchClient client) {
		if (world.getAgents().size() == 1) {
			singleAgentPlanner();
		} else {
			multiAgentPlanner();
		}
	}

	private void singleAgentPlanner() {
		boolean replanned = false;
		for (Box box : world.getBoxes().values()) {
			if (!world.isBoxReachable(world.getAgents().get(0), box)) {
				world.getAgents().get(0).addUnreachableBoxId(box.getId());
			}
		}
		mainLoop: while (!world.isGlobalGoalState()) {
			if (!replanned) {
				Agent agent = world.getAgents().get(0);
				if (agent.getPlan().size() == 0 || (agent.getPlan().size() == agent.getStepInPlan())) {
					world.generateSAPlan(agent);
					Strategy strategy = new StrategyBestFirst(new AStar(agent.initialState));
					Search s = new Search();
					List<Node> solution = s.search(strategy, agent.initialState, SearchType.PATH);
					for (Goal g : World.getInstance().getGoals().values())
						if (g.isSolved())
							agent.initialState.walls.remove(g.getPosition());
					agent.setPlan(solution);
					agent.setStepInPlan(0);
					for (Box box : World.getInstance().getBoxes().values())
						if (agent.getIntention() != null && !box.equals(agent.getIntention().getBox()))
							agent.initialState.boxes.remove(box.getId());
					if (solution == null || solution.size() == 0) {
						List<Node> empty = new LinkedList<Node>();
						Node noOp = agent.initialState;
						noOp.action = new Command();
						empty.add(noOp);
						agent.setPlan(empty);
						agent.setStepInPlan(0);
					}
				}
			} else {
				replanned = false;
			}
			Map<Integer, Position> updatedAgentPositions = new HashMap<Integer, Position>(0);
			Map<Integer, Box> updatedBoxes = new HashMap<Integer, Box>(0);
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Agent agent : world.getAgents().values()) {
				Node curNode = agent.getPlan().get(agent.getStepInPlan());
				sb.append(curNode.action.toString());
				updatedAgentPositions.put(agent.getId(), new Position(curNode.agentRow, curNode.agentCol));
				for (Box box : curNode.boxes.values()) {
					updatedBoxes.put(box.getId(), box);
				}
			}
			sb.append("]");
			DetectConflict detectCon = new DetectConflict();
			Conflict con = detectCon.checkConflict();
			if (con != null && !replanned && conflictId != con.getSender().getPosition().hashCode()) {
				conflictId = con.getSender().getPosition().hashCode();
				if (World.getInstance().getAgents().get(0).isExecutingSuperPlan()) {
					con.minorChangeInPlan(World.getInstance().getAgents().get(0), World.getInstance().getBoxes().get(con.getReceiverBox().getId()));
				} else {
					switch (con.getConflictType()) {
					case AGENT:
						con.solveAgentOnAgent(con, con.getNode(), con.getSender(), con.getReceiver());
						break;
					case SINGLE_AGENT_BOX:
						con.solveAgentOnBox(con.getNode(), World.getInstance().getAgents().get(0), World.getInstance().getBoxes().get(con.getReceiverBox().getId()));
						break;
					case BOX_BOX:
						con.SASolveBoxOnBox(con);
						break;
					default:
						break;
					}
					replanned = true;
				}
				continue mainLoop;
			} else {
				replanned = false;
				System.out.println(sb.toString());
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					if (in.ready())
						in.readLine();
				} catch (IOException e) {
					world.write(e.getMessage());
				}
				Utils.performUpdates(updatedAgentPositions, updatedBoxes);
			}
		}
	}

	private void multiAgentPlanner() {
		boolean replanned = false;
		for(Agent agent : world.getAgents().values()){
			for (Box box : world.getBoxes().values()) {
				if (!world.isBoxReachable(agent, box)) {
					world.getAgents().get(agent.getId()).addUnreachableBoxId(box.getId());
				}
			}
		}
		mainLoop: while (!world.isGlobalGoalState()) {
			if (!replanned) {
				for (Agent agent : world.getAgents().values()) {
					if (agent.getPlan().size() == 0 || (agent.getPlan().size() == agent.getStepInPlan())) {
						world.generatePlan(agent);
						Strategy strategy = new StrategyBestFirst(new AStar(agent.initialState));
						Search s = new Search();
						List<Node> solution = s.search(strategy, agent.initialState, SearchType.PATH);
						agent.setPlan(solution);
						agent.setStepInPlan(0);
						for (Box box : World.getInstance().getBoxes().values())
							if (agent.getIntention() != null && !box.equals(agent.getIntention().getBox()))
								agent.initialState.boxes.remove(box.getId());
						if (solution == null || solution.size() == 0) {
							List<Node> empty = new LinkedList<Node>();
							Node noOp = agent.initialState;
							noOp.action = new Command();
							empty.add(noOp);
							agent.setPlan(empty);
							agent.setStepInPlan(0);
						}
					}
				}
			} else {
				replanned = false;
			}
			Map<Integer, Position> updatedAgentPositions = new HashMap<Integer, Position>(0);
			Map<Integer, Box> updatedBoxes = new HashMap<Integer, Box>(0);
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			int i = 0;
			for (Agent agent : world.getAgents().values()) {
				Node curNode = agent.getPlan().get(agent.getStepInPlan());
				sb.append(curNode.action.toString());
				updatedAgentPositions.put(agent.getId(), new Position(curNode.agentRow, curNode.agentCol));
				for (Box box : curNode.boxes.values()) {
					updatedBoxes.put(box.getId(), box);
				}
				if (i < world.getAgents().size() - 1)
					sb.append(", ");
				i++;
			}
			sb.append("]");
			DetectConflict detectCon = new DetectConflict();
			Conflict con = detectCon.checkConflict();
			if (con != null && !replanned) {
				switch (con.getConflictType()) {
				case AGENT:
					con.solveAgentOnAgent(con,con.getNode(), con.getSender(), con.getReceiver());
					break;
				case SINGLE_AGENT_BOX:
					MABoxConflicts maBox = new MABoxConflicts();
					maBox.solveMAgentBoxConflict(con);
					break;
				case BOX_BOX:
					con.MAsolveBoxOnBox(con);
					break;
				default:
					break;
				}
				replanned = true;
				continue mainLoop;
			} else {
				replanned = false;
				System.out.println(sb.toString());
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					if (in.ready())
						in.readLine();
				} catch (IOException e) {
					world.write(e.getMessage());

				}
				Utils.performUpdates(updatedAgentPositions, updatedBoxes);
			}
			world.updateBeliefs();
		}
	}
}