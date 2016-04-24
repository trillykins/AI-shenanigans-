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

import analysis.LevelAnalysis;
import atoms.Agent;
import atoms.Box;
import atoms.Color;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import bdi.Belief;
import strategies.Strategy;

public class SearchClient {
	public static int MAX_ROW = 0;
	public static int MAX_COLUMN = 0;
	public static int TIME = 300;
	public static BufferedReader in;
	public static Map<Goal, Byte[][]> precomputedGoalH;
	private Map<Character, String> colors;
	Set<Color> colorSet;
	World world;
	LevelAnalysis levelAnalysis;

	public SearchClient() throws IOException {
		precomputedGoalH = new HashMap<Goal, Byte[][]>(0);
		colors = new HashMap<Character, String>(0);
		colorSet = new HashSet<Color>(0);
		in = new BufferedReader(new InputStreamReader(System.in));
		world = World.getInstance();
	}

	public boolean update() throws IOException {
		String jointAction = "[";
		//
		// for (int i = 0; i < agents.size() - 1; i++)
		// jointAction += agents.get(i).act() + ",";
		//
		// jointAction += agents.get(agents.size() - 1).act() + "]";

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

	public String readLines() {
		String line = null, color;
		try {
			while ((line = in.readLine()).matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
				line = line.replaceAll("\\s", "");
				color = line.split(":")[0];
				for (String id : line.split(":")[1].split(",")) {
					System.err.println(id + ", " + color);
					colors.put(id.charAt(0), color);
					colorSet.add(Utils.determineColor(color));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}

	public void initWorld(String line) {
		Set<Position> walls = new HashSet<Position>(0);
		Map<Integer, Goal> goals = new HashMap<Integer, Goal>(0);
		Map<Integer, Box> boxes = new HashMap<Integer, Box>(0);
		Map<Integer, Agent> agents = new HashMap<Integer, Agent>(0);
		List<Belief> beliefs = new ArrayList<Belief>(0);
		int row = 0, column = 0;
		while (!line.equals("")) {
			for (int i = 0; i < line.length(); i++) {
				char id = line.charAt(i);
				if ('0' <= id && id <= '9') {
					System.err.println("Agent " + id + " color: " + colors.get(id));
					agents.put(Integer.parseInt("" + id),
							new Agent(Integer.parseInt("" + id), colors.get(id), new Position(row, i), Integer.parseInt("" + id)));
				} else if ('A' <= id && id <= 'Z') { // Boxes
					boxes.put(boxes.size() + 1,
							new Box(boxes.size() + 1, new Position(row, i), id, Utils.determineColor(colors.get(id))));
				} else if ('a' <= id && id <= 'z') { // Goals
					goals.put(goals.size() + 1, new Goal(goals.size() + 1, new Position(row, i), id,
							Utils.determineColor(colors.get(id)), 0));
				} else if (id == '+') {
					walls.add(new Position(row, i));
				}
			}
			column = line.length() > column ? line.length() : column;
			row++;
			try {
				line = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		MAX_ROW = row;
		MAX_COLUMN = column;
		world.setAgents(agents);
		world.setBoxes(boxes);
		world.setGoals(goals);
		world.setWalls(walls);
		world.setColors(colorSet);
		for (Goal goal : goals.values()) {
			beliefs.add(new Belief(goal));
		}
		world.setBeliefs(beliefs);
		world.setBoxesInGoals(new HashMap<Integer, Box>(0));
		world.setSolvedGoals(new HashMap<Integer, Goal>(0));
	}

	public void init() throws IOException {
		String line = null;
		line = readLines();
		initWorld(line);
		levelAnalysis = new LevelAnalysis();
		/* calculates distances for each goal */
		for (Integer id : world.getGoals().keySet()) {
			Goal goal = world.getGoals().get(id);
			Position gPos = goal.getPosition();
			Byte[][] result = Utils.calculateDistanceValues(gPos.getX(), gPos.getY(), goal.getLetter(), MAX_ROW,
					MAX_COLUMN);
			precomputedGoalH.put(goal, result);

			/* calculate goalPriority : based on world elements */
			goal.setPriority(levelAnalysis.calculateGoalPriority(goal));
		}
	}
}