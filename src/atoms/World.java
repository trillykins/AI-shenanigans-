package atoms;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import analysis.FreeSpace;
import bdi.Belief;
import bdi.Intention;
import searchclient.Node;
import searchclient.SearchClient;
import utils.FileUtils;

public class World {
	private Map<Integer, Agent> agents;
	private Map<Integer, Box> boxes;
	private Map<Integer, Goal> goals;
	private Map<Integer, Box> boxesInGoals;
	private Map<Integer, Goal> solvedGoals;
	private List<Position> walls;
	private List<Color> colors;
	private List<Belief> beliefs;
	private Map<Integer, List<Node>> solutionMap;
	private Map<Position, FreeSpace> freeSpace;
	private FileUtils files = new FileUtils();
	// private Map<Integer, List<FreeSpace>> freeSpace;

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
	
	public Map<Integer, Box> getBoxesInGoals() {
		return boxesInGoals;
	}

	public void setBoxesInGoals(Map<Integer, Box> boxesInGoals) {
		this.boxesInGoals = boxesInGoals;
	}

	public Map<Integer, Goal> getSolvedGoals() {
		return solvedGoals;
	}

	public void setSolvedGoals(Map<Integer, Goal> solvedGoals) {
		this.solvedGoals = solvedGoals;
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

	public Map<Integer, List<Node>> getSolutionMap() {
		return solutionMap;
	}

	public void setSolutionMap(Map<Integer, List<Node>> solutionMap) {
		this.solutionMap = solutionMap;
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
		for (List<Node> solution : World.getInstance().getSolutionMap().values())
			size = (size < solution.size() ? solution.size() : size);
		return size;
	}

	public void updateBeliefs() {
		for (Goal goal : World.getInstance().getGoals().values()) {
			if (!goal.isSolved()) {
				boolean contained = true;
				for (Belief b : World.getInstance().getBeliefs()) {
					if (goal.equals(b.getGoal()))
						contained = true;
				}
				if (!contained)
					World.getInstance().getBeliefs().add(new Belief(goal));
			}
		}
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
//		System.err.println(intention.getDesire() == null);
		Goal goal = intention.getDesire().getBelief().getGoal();
		Box intentionBox = intention.getBox();
		World.getInstance().getBeliefs().remove(intention.getDesire().getBelief());
		agent.initialState.goals.put(goal.getId(), goal);
		agent.initialState.boxes.put(intentionBox.getId(), intentionBox);
		
		for(Box box : boxes.values()) {
			if(box.isOnGoal())
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
		write("beliefs before removing: " + Arrays.toString(beliefs.toArray()));
		write("contains: " + beliefs.contains(intention.getDesire().getBelief()));
		write("removing belief: " + intention.getDesire().getBelief());
		World.getInstance().beliefs.remove(intention.getDesire().getBelief());
		write("beliefs after removing: " + Arrays.toString(beliefs.toArray()));
		write("here we have selected intion for agent : " + agent.getId() + " intention is : " + intention.toString());
		Goal goal = intention.getDesire().getBelief().getGoal();
		Box intentionBox = intention.getBox();
		
//		Set<Belief> beliefs = new HashSet<Belief>();
//		Belief belief = intention.getDesire().getBelief();
//		for(Belief bel : World.getInstance().getBeliefs()){
//			if (!belief.equals(bel)) {
//				beliefs.add(bel);
//			}
//		}
//		World.getInstance().setBeliefs(beliefs);
//		World.getInstance().getBeliefs().remove(intention.getDesire().getBelief());

		agent.initialState.goals.put(goal.getId(), goal);
		agent.initialState.boxes.put(intentionBox.getId(), intentionBox);
		return agent;
	}

	public void generatePlans() {
		for (Agent agent : World.getInstance().getAgents().values()) {
			generatePlan(agent);
		}
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int row = 0; row < SearchClient.MAX_ROW; row++) {
			for (int col = 0; col < SearchClient.MAX_COLUMN; col++) {
				boolean skip = false;
				Position pos = new Position(row, col);
				for (Box b : boxes.values()) {
					if (b.getPosition().equals(pos)) {
						s.append(b.getLetter());
						skip = true;
						break;
					}
				}
				if (skip)
					continue;
				for (Goal g : goals.values()) {
					if (g.getPosition().equals(pos)) {
						s.append(g.getLetter());
						skip = true;
						break;
					}
				}
				if(skip)
					continue;
				for (Agent a : agents.values()) {
					if (row == a.getPosition().getX() && col == a.getPosition().getY()) {
						s.append(a.getId());
						skip = true;
						break;
					}
				}
				if (skip)
					continue;
				if (World.getInstance().getWalls().contains(pos)) {
					s.append("+");
				} else {
					s.append(" ");
				}
			}
			s.append("\n");
		}
		return s.toString();
	}
}