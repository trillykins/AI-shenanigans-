package atoms;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import analysis.FreeSpace;
import bdi.Belief;
import bdi.Intention;
import heuristics.AStar;
import searchclient.Node;
import searchclient.Search;
import searchclient.Search.SearchType;
import searchclient.SearchClient;
import strategies.Strategy;
import strategies.StrategyBestFirst;
import utils.FileUtils;

public class World {
	private Map<Integer, Agent> agents;
	private Map<Integer, Box> boxes;
	private Map<Integer, Goal> goals;
	private List<Position> walls;
	private List<Color> colors;
	private List<Belief> beliefs;
	private Map<Integer, List<Node>> solutionMap;
	private Map<Position, FreeSpace> freeSpace;
	private FileUtils files = new FileUtils();

	private static World instance = null;

	public static World getInstance() {
		if (instance == null) {
			instance = new World();
		}
		return instance;
	}

	protected World() {
	}

	public void write(String str) {
		 files.write(str);
	}

	public Map<Integer, Agent> getAgents() {
		return agents;
	}

	public void setAgents(Map<Integer, Agent> agents) {
		this.agents = agents;
	}

	public Map<Integer, Box> getBoxes() {
		return boxes;
	}

	public void setBoxes(Map<Integer, Box> boxes) {
		this.boxes = boxes;
	}

	public Map<Integer, Goal> getGoals() {
		return goals;
	}

	public void setGoals(Map<Integer, Goal> goals) {
		this.goals = goals;
	}

	public Map<Position, FreeSpace> getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(Map<Position, FreeSpace> freeSpace) {
		this.freeSpace = freeSpace;
	}

	public List<Position> getWalls() {
		return walls;
	}

	public void setWalls(List<Position> walls) {
		this.walls = walls;
	}

	public List<Color> getColors() {
		return colors;
	}

	public void setColors(List<Color> colors) {
		this.colors = colors;
	}

	public List<Belief> getBeliefs() {
		return beliefs;
	}

	public void setBeliefs(List<Belief> beliefs) {
		this.beliefs = beliefs;
	}

	public FileUtils getFiles() {
		return files;
	}

	public void setFiles(FileUtils files) {
		this.files = files;
	}

	public boolean isGlobalGoalState() {
		for (Goal goal : goals.values()) {
			if (!goal.isSolved())
				return false;
		}
		return true;
	}

	public int findLongestPlan() {
		int size = 0;
		for (List<Node> solution : solutionMap.values())
			size = (size < solution.size() ? solution.size() : size);
		return size;
	}

	public void updateBeliefs() {
		// System.err.println(World.getInstance().getBeliefs().size());
		for (Goal goal : World.getInstance().getGoals().values()) {
			if (!goal.isSolved()) {
				boolean contained = false;
				for (Belief b : World.getInstance().getBeliefs()) {
					if (goal.equals(b.getGoal()))
						contained = true;
				}
				/*
				 * if the goal is not fulfilled or another agent doesn't have
				 * it, we add the belief again
				 */
				if (!contained && !agentHasGoalInBelief(goal)) {
//					System.err.println("adding belief again: " + goal);
					World.getInstance().getBeliefs().add(new Belief(goal));
				}
			}
		}
		// System.err.println(World.getInstance().getBeliefs().size());
	}

	public boolean agentHasGoalInBelief(Goal goal) {
		for (Agent agent : World.getInstance().getAgents().values()) {
			if (agent.getIntention().getDesire().getBelief().getGoal().equals(goal))
				return true;
		}
		return false;
	}

	public Agent generateSAPlan(Agent agent) {
		agent.generateInitialState();
		if (!agent.generateDesires()) {
			return agent;
		}
		if (!agent.generateIntention()) {
			return agent;
		}
		Intention intention = agent.getIntention();
		// System.err.println(intention.getDesire() == null);
		Goal goal = intention.getDesire().getBelief().getGoal();
		Box intentionBox = intention.getBox();
		World.getInstance().getBeliefs().remove(intention.getDesire().getBelief());
		agent.initialState.goals.put(goal.getId(), goal);
		agent.initialState.boxes.put(intentionBox.getId(), intentionBox);

		for (Box box : boxes.values()) {
			if (box.isOnGoal())
				agent.initialState.boxes.put(box.getId(), box);
		}
		return agent;
	}

	public Agent generatePlan(Agent agent) {
		agent.generateInitialState();
		if (!agent.generateDesires()) {
			return agent;
		}
		if (!agent.generateIntention()) {
			return agent;
		}
		Intention intention = agent.getIntention();
		World.getInstance().beliefs.remove(intention.getDesire().getBelief());
		Goal goal = intention.getDesire().getBelief().getGoal();
		Box intentionBox = intention.getBox();

		agent.initialState.goals.put(goal.getId(), goal);
		agent.initialState.boxes.put(intentionBox.getId(), intentionBox);
		return agent;
	}

	public boolean isBoxReachable(Agent agent, Box box) {
		if (box == null)
			return false;
		agent.generateInitialState();
		agent.initialState.boxes = new HashMap<Integer, Box>();
		agent.initialState.goals = new HashMap<Integer, Goal>();
		agent.initialState.boxes.put(box.getId(), box);
		for (Goal goal : World.getInstance().getGoals().values()) {
			if (goal.getLetter() == Character.toLowerCase(box.getLetter())) {
				agent.initialState.goals.put(goal.getId(), goal);
				Strategy strategy = new StrategyBestFirst(new AStar(agent.initialState));
				Search s = new Search();
				LinkedList<Node> plan = s.search(strategy, agent.initialState, SearchType.PATH);
				if (plan != null) {
					return true;
				}
				agent.initialState.goals.remove(goal.getId());
			}
		}
		return false;
	}
	
	public String toString2() {
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < SearchClient.MAX_ROW; row++) {
			for (int col = 0; col < SearchClient.MAX_COLUMN; col++) {
				sb.append("(" + row + ", " + col + ")");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int row = 0; row < SearchClient.MAX_ROW; row++) {
			inner: for (int col = 0; col < SearchClient.MAX_COLUMN; col++) {
				Position pos = new Position(row, col);
				for (Agent a : agents.values()) {
					if (row == a.getPosition().getX() && col == a.getPosition().getY()) {
						s.append(a.getId());
						continue inner;
					}
				}
				for (Box b : boxes.values()) {
					if (b.getPosition().equals(pos)) {
						s.append(b.getLetter());
						continue inner;
					}
				}
				for (Goal g : goals.values()) {
					if (g.getPosition().equals(pos)) {
						s.append(g.getLetter());
						continue inner;
					}
				}
				s.append((walls.contains(pos) ? "+" : " "));
			}
			s.append("\n");
		}
		return s.toString();
	}
}