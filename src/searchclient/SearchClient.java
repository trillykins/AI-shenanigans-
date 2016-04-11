package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import atoms.Agent;
import atoms.Box;
import atoms.Color;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import strategies.Strategy;

public class SearchClient {
	public static int MAX_ROW = 0;
	public static int MAX_COLUMN = 0;
	public static int TIME = 300;
	public static BufferedReader in;
	public static Map<Goal, Byte[][]> precomputedGoalH;
	private Map<Character, String> colors;

	public SearchClient() throws IOException {
		precomputedGoalH = new HashMap<Goal, Byte[][]>(0);
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
		Set<Position> walls = new HashSet<Position>(0);
		Map<Integer, Goal> goals = new HashMap<Integer, Goal>(0);
		Map<Integer, Box> boxes = new HashMap<Integer, Box>(0);
		Map<Integer, Agent> agents = new HashMap<Integer, Agent>(0);
		List<String> messages = new ArrayList<String>();
		Set<Color> colorSet = new HashSet<Color>();
		
		int row = 0, column = 0;
		String line, color;

		// Read lines specifying colors
		while ((line = in.readLine()).matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
			line = line.replaceAll("\\s", "");
			color = line.split(":")[0];
			for (String id : line.split(":")[1].split(",")) {
				colors.put(id.charAt(0), color);
				colorSet.add(Utils.determineColor(color));
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
					boxes.put(boxes.size() + 1, new Box(boxes.size() + 1, new Position(row, i), id, Utils.determineColor(colors.get(id))));
				} else if ('a' <= id && id <= 'z') { // Goals
					goals.put(goals.size() + 1, new Goal(goals.size() + 1, new Position(row, i), id, Utils.determineColor(colors.get(id))));
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
		world.setColors(colorSet);
		
		MAX_ROW = row;
		MAX_COLUMN = column;

		for (Integer id : agents.keySet()) {
			Agent agent = agents.get(id);
			agent.initialState = new Node(null, agent.getId());
			agent.initialState.agentRow = agent.getPosition().getX();
			agent.initialState.agentCol = agent.getPosition().getY();
			agent.initialState.boxes = new HashMap<Integer, Box>(0);
			agent.initialState.goals = new HashMap<Integer, Goal>(0);
			for (Integer boxId : boxes.keySet()) {
				for (Integer goalId : goals.keySet()) {
					Box b = boxes.get(boxId);
					Goal g = goals.get(goalId);
					if (Character.toLowerCase(b.getLetter()) == g.getLetter()) {
						if (agent.getColor().equals(b.getColor())) {
							agent.initialState.goals.put(g.getId(), g);
							agent.initialState.boxes.put(b.getId(), b);
						}
					}
				}
			}
		}
		
		/*This method removes duplicate goals for agents that have the same color*/
		removeDuplicateGoals(world,colors);

		/* Needs to be modified such that it calculates for each agent */
		for (Integer id : agents.keySet()) {
			Agent agent = agents.get(id);
			for(Integer goalId : agent.initialState.goals.keySet()) {
				Goal goal = agent.initialState.goals.get(goalId);
				Position gPos = goal.getPosition();
				Byte[][] result = Utils.calculateDistanceValues(gPos.getX(), gPos.getY(), goal.getLetter(), MAX_ROW, MAX_COLUMN);
				precomputedGoalH.put(goal, result);
			}
		}
	}
	
	public void removeDuplicateGoals(World world, Map<Character, String> colors){
		Map<Goal,Agent> regen = new HashMap<Goal,Agent>();
		
		for(Color color : world.getColors()){
			/*We find the agents of the same color*/
			Set<Agent> sameColorAgents = new HashSet<Agent>(); 
			Map<Integer, Goal> sameColorGoals = new HashMap<Integer,Goal>();
			for(Agent agent : world.getAgents().values()){
				if (color == agent.getColor()){
					sameColorAgents.add(agent);
					/*Two agents of same color will have the same goals, the goals are only added ones*/
					if (sameColorGoals.isEmpty())
						sameColorGoals = agent.initialState.goals;
				}
			}
			if(sameColorAgents.size() > 1){ /*No reason to do this if there is only one agent of one color*/
				for(Goal g : sameColorGoals.values()){	
					int distance = sameColorAgents.size();
					/*for each agent calc manhattan distance*/
					TreeMap<Integer,Agent> priorityMap = new TreeMap<Integer,Agent>();
					for(Agent agent : sameColorAgents){
						distance = Utils.manhattenDistance(agent.getPosition(),g.getPosition());
						priorityMap.put(distance, agent);	
					}
					/*We add the goal and agent to regen, for which the distance is shortest*/
					Agent agent = priorityMap.firstEntry().getValue();
					System.err.println("Agent " + agent.getId() + ", mål " + g.getLetter());
					regen.put(g, agent);
				}
			}
		}
		for(Goal g : regen.keySet()){
			for(int aId : world.getAgents().keySet()){
				Agent agent = world.getAgents().get(aId);
				if (agent.getId() != regen.get(g).getId()){
					world.getAgents().get(agent.getId()).initialState.goals.remove(g.getId());
				}
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
			if (leafNode.isGoalState()) 
				return leafNode.extractPlan();
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