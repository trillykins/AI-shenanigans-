package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import strategies.Strategy;

public class SearchClient {
	public static int MAX_ROW = 0;
	public static int MAX_COLUMN = 0;
	public static int TIME = 300;
	public static BufferedReader in;
	public static Set<Goal> goals;
	public static Set<Box> boxes;
	public static Set<Position> walls;
	public static Map<Integer, Agent> agents;
	public static Map<Character, Byte[][]> precomputedGoalH;
	private Map<Character, String> colors;

	public SearchClient() throws IOException {
		precomputedGoalH = new HashMap<Character, Byte[][]>(0);
		goals = new HashSet<Goal>(0);
		boxes = new HashSet<Box>(0);
		walls = new HashSet<Position>(0);
		agents = new HashMap<Integer, Agent>(0);
//		agents = new HashSet<Agent>(0);
//		agents = new ArrayList<Agent>(0);
		colors = new HashMap<Character, String>(0);
		in = new BufferedReader(new InputStreamReader(System.in));
	}

	public boolean update() throws IOException {
		String jointAction = "[";
//
//		for (int i = 0; i < agents.size() - 1; i++)
//			jointAction += agents.get(i).act() + ",";
//
//		jointAction += agents.get(agents.size() - 1).act() + "]";

		// Place message in buffer
		System.out.println(jointAction);
		System.err.println(jointAction);
		// Flush buffer
		System.out.flush();

		// Disregard these for now, but read or the server stalls when its
		// output buffer gets filled!
		String percepts = in.readLine();
		if (percepts == null)
			return false;

		return true;
	}

	public void init() throws IOException {
		int row = 0, column = 0;
		String line, color;
		ArrayList<String> messages = new ArrayList<String>();

		// Read lines specifying colors
		while ((line = in.readLine()).matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
			line = line.replaceAll("\\s", "");
			color = line.split(":")[0];
			for (String id : line.split(":")[1].split(",")) {
				colors.put(id.charAt(0), color);
			}
		}

		// Read lines specifying level layout
		while (!line.equals("")) {
			messages.add(line);
			for (int i = 0; i < line.length(); i++) {
				char id = line.charAt(i);
				if ('0' <= id && id <= '9') {
					agents.put(Integer.parseInt("" + id), new Agent(Integer.parseInt("" + id), colors.get(id), new Position(row, i)));
				} else if ('A' <= id && id <= 'Z') { // Boxes
					boxes.add(new Box(new Position(row, i), id, Utils.determineColor(colors.get(id))));
				} else if ('a' <= id && id <= 'z') { // Goals
					goals.add(new Goal(new Position(row, i), id, Utils.determineColor(colors.get(id))));
				} else if(id == '+') {
					walls.add(new Position(row, i));
				}
			}
			if (line.length() > column) {
				column = line.length();
			}
			row++;
			line = in.readLine();
		}
		World world = World.getInstance();
		world.setAgents(agents);
		world.setBoxes(boxes);
		world.setGoals(goals);
		world.setWalls(walls);
		
		MAX_ROW = row;
		MAX_COLUMN = column;

		for (Integer id : agents.keySet()) {
			Agent agent = agents.get(id);
			agent.setInitialState(new Node(null, agent.getId()));
			agent.initialState.agentRow = agent.getPosition().getX();
			agent.initialState.agentCol = agent.getPosition().getY();
			agent.initialState.boxes = new ArrayList<Box>(0);
			agent.initialState.goals = new ArrayList<Goal>(0);
			for (Box b : boxes) {
				for (Goal g : goals) {
					if (Character.toLowerCase(b.getLetter()) == g.getLetter()) {
						if (agent.getColor().equals(b.getColor())) {
							agent.initialState.goals.add(g);
							agent.initialState.boxes.add(b);
						}
					}
				}
			}
		}
		/* Needs to be modified such that it calculates for each agent */
		for (Integer id : agents.keySet()) {
			Agent agent = agents.get(id);
			for(Goal goal : agent.initialState.goals) {
				Position gPos = goal.getPosition();
				Byte[][] result = Utils.calculateDistanceValues(gPos.getX(), gPos.getY(), goal.getLetter(), MAX_ROW, MAX_COLUMN);
				precomputedGoalH.put(goal.getLetter(), result);
			}
		}
	}

	public LinkedList<Node> search(Strategy strategy, Node initialState) {
		System.err.format("Search starting with strategy %s\n", strategy);
		strategy.addToFrontier(initialState);
		int iterations = 0;
		while (true) {
			if (iterations % 200 == 0) {
				System.err.println(strategy.searchStatus());
			}
			if (Memory.shouldEnd()) {
				System.err.format("Memory limit almost reached, terminating search %s\n", Memory.stringRep());
				return null;
			}
			if (strategy.timeSpent() > TIME) { // Minutes timeout
				System.err.format("Time limit reached, terminating search %s\n", Memory.stringRep());
				return null;
			}
			if (strategy.frontierIsEmpty()) {
				return null;
			}
			Node leafNode = strategy.getAndRemoveLeaf();
			System.err.println(leafNode.toString());
			if (leafNode.isGoalState()) {
				System.err.println(leafNode.toString());
				return leafNode.extractPlan();
			}
			strategy.addToExplored(leafNode);
			for (Node n : leafNode.getExpandedNodes()) {
				if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
					strategy.addToFrontier(n);
				}
			}
			iterations++;
		}
	}
	
}