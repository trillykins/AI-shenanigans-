package searchclient;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import searchclient.node.Node;
import searchclient.node.Colour;
import searchclient.node.SearchResult;
import searchclient.node.MapState;
import searchclient.strategy.Distance;
import searchclient.strategy.Greedy;
import searchclient.strategy.Heuristic;
import searchclient.strategy.Strategy;
import searchclient.strategy.Dist;
import searchclient.strategy.DistanceFloydWarshall;
import searchclient.strategy.ResolveConflict;
import searchclient.Agent.Status;

public class SearchClient {

	public static Node state = null;

	public static List<Agent> agents = new ArrayList<>();
	static BufferedReader serverMessages = new BufferedReader( new InputStreamReader(System.in) );

	static MapState mapState = null;

	public static void main(String[] args) {
		try {
			mapState = new MapState();
			state = examineLevel(serverMessages);
			for (int i = 0; i < state.agents.length; i++) {
				if (state.agents[i] != null) {
					agents.add(new Agent(state.agents[i]));
				}
			}
			ArrayList<LinkedList<Node>> solutions = new ArrayList<LinkedList<Node>>();
			for (Agent agent : agents) {
				solutions.add(new LinkedList<Node>());
			}
			while (!state.isGoalState()) {
				if (agents.size() == 1) {
					singleAgentPlanning(solutions);
				} else {
					multiAgentPlanning(solutions);
				}
				executePlans(solutions);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void executePlans(ArrayList<LinkedList<Node>> solutions) throws Exception {
		StringBuilder builder = new StringBuilder();
		boolean done = false;
		while (!done) {
			builder.append('[');
			for (int i = 0; i < solutions.size(); i++) {
				if (solutions.get(i).isEmpty()) {
					if(agents.get(i).status != Agent.Status.STUCK_HELPING && agents.get(i).status != Agent.Status.STUCK) {
						agents.get(i).status = Agent.Status.IDLE;
					}
					builder.append("NoOp");
				} else {
					builder.append(solutions.get(i).peek().action);
				}
				if (i != solutions.size() - 1) {
					builder.append(',');
				}
			}
			builder.append(']');

			// communicate with server
			System.out.println(builder.toString());
			builder.setLength(0);

			String response;
			do {
				response = serverMessages.readLine();
			} while (response.equals(""));

			String[] strings = response.split(",");
			ArrayList<Command> commands = new ArrayList<>();
			for (int i = 0; i < strings.length; i++) {
				if (strings[i].contains("false")) {
					commands.add(new Command());
					solutions.get(i).clear();
				} else {
					if (!solutions.get(i).isEmpty()) {
						commands.add(solutions.get(i).pop().action);
					} else {
						commands.add(new Command());
					}
				}
			}
			state = state.excecuteCommands(commands);

			for (int i = 0; i < solutions.size(); i++) {
				if (solutions.get(i).isEmpty()) {
					if (!state.isGoalState(state.agents[i].colour)) {
						agents.get(i).status = Status.IDLE;
						done = true;
					}
				}
			}
		}
	}


	private static void singleAgentPlanning(ArrayList<LinkedList<Node>> solution) throws Exception {
		Agent agent = agents.get(0);
		agent.setState(state);
		Heuristic heuristic = new Heuristic(agent);
		Greedy greedy = new Greedy(heuristic);
		Strategy strategy = greedy;
		if (state.isGoalState(agent.subgoals)) {
			agent.addSubgoal();
		}
		SearchResult result = agent.Search(strategy, agent.subgoals);
		if (!result.equals(null) || !result.mapState.equals(null)) {
			mapState.add(result.mapState);
		}
		solution.get(agent.id).addAll(result.solution);
	}

	private static void multiAgentPlanning(ArrayList<LinkedList<Node>> solutions) throws Exception {
		boolean stuck = false;
		Strategy strategy = null;
		Strategy relaxedStrategy = null;
		Greedy greedy = null;
		SearchResult relaxedResult = null;

		for (Agent agent : agents) {
			if (solutions.get(agent.id).isEmpty()) {
				if (agent.status == Status.HELPING || agent.status == Status.STUCK_HELPING) {
					agent.status = Status.IDLE;
				}

				if (state.isGoalState(agent.subgoals)) {
					agent.setState(state);
					agent.addSubgoal();
				}

				Node relaxedState = state.subdomain(agent.id);
				Heuristic relaxedHeuristic = new Heuristic(agent);
				greedy = new Greedy(relaxedHeuristic);
				relaxedStrategy = greedy;
				agent.setState(relaxedState);
				relaxedResult = agent.Search(relaxedStrategy, agent.subgoals);
				if (!relaxedResult.equals(null) || !relaxedResult.mapState.equals(null)) {
					mapState.add(relaxedResult.mapState);
				}
				
				agent.setState(state);
				Heuristic heuristic = new Heuristic(agent);
				greedy = new Greedy(heuristic);
				strategy = greedy;
				SearchResult result = agent.Search(strategy, agent.subgoals, relaxedResult);
				if (!result.equals(null) || !result.mapState.equals(null)) {
					mapState.add(result.mapState);
				}

				switch (result.result) {
					case STUCK:
						agent.status = Status.STUCK;		
						stuck = true;
						solutions.get(agent.id).addAll(relaxedResult.solution);
						break;
					case DONE:
						agent.status = Status.DONE;
						break;
					case IMPOSIBLE:
						throw new Exception("IMPOSSIBLE");
					default:
						agent.status=Status.PLAN;
						solutions.get(agent.id).addAll(result.solution);
						break;
				}
			}
		}
		if (stuck) {
			solutions = ResolveConflict.resolve(state, solutions, agents);
		}
	}


	private static Node examineLevel(BufferedReader in) throws IOException {
		Map<Character, Colour> colours = new HashMap<Character, Colour>();
		String line, colour;
		ArrayList<String> tmpMapContainer = new ArrayList<String>();
		Dist distance = new Dist(); 
		distance.dist = new DistanceFloydWarshall();

		// Read lines specifying colors
		while ((line = in.readLine()).matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
			line = line.replaceAll("\\s", "");
			colour = (line.split(":")[0]).toUpperCase();
			for ( String id : line.split(":")[1].split(",")) {	
				colours.put( Character.toLowerCase(id.charAt(0)), Colour.valueOf(colour));
			}
		}
		// Read lines specifying level layout
		int maxCol = 0, maxRow = 0;
		while (line != null && !line.equals("")) {
			if (line.length() > maxCol) {
				maxCol = line.length();
			}
			tmpMapContainer.add(line);
			line=in.readLine();
		}
		maxRow = tmpMapContainer.size();
		Level level = new Level(maxRow, maxCol, distance.dist);
		Node node = new Node(level);

		for (int row = 0; row < maxRow; row++) {
			line = tmpMapContainer.get(row);
			for (int col = 0; col < line.length(); col++) {
				if (line.charAt(col) == ' ') {
					level.addSpace(row, col);
				} else if (line.charAt(col) == '+') {
					level.addWall(row, col);
				} else if (line.charAt(col) >= 'a' && line.charAt(col) <= 'z') {
					level.addGoal(row, col, line.charAt(col), colours.get(Character.toLowerCase(line.charAt(col))));
				} else if (line.charAt(col) >= 'A' && line.charAt(col) <= 'Z') {
					node.addBox(line.charAt(col), colours.get(Character.toLowerCase(line.charAt(col))), row, col);
					level.addSpace(row, col);
				} else if (line.charAt(col) >= '0' && line.charAt(col) <= '9') {
					node.addAgent(line.charAt(col), colours.get(line.charAt(col)), row, col);
					level.addSpace(row, col);
				}
			}
		}
		distance.dist.initialize(level);
		level.setGoalsList(node.agents);
		level.analyse();
		return node;
	}
}