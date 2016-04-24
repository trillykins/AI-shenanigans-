package atoms;

import java.util.List;
import java.util.Map;
import java.util.Set;

import bdi.Belief;
import searchclient.Node;
import searchclient.SearchClient;

public class World {
	private Map<Integer, Agent> agents;
	private Map<Integer, Box> boxes;
	private Map<Integer, Goal> goals;
	private Map<Integer, Box> boxesInGoals;
	private Map<Integer, Goal> solvedGoals;
	private Set<Position> walls;
	private Set<Color> colors;
	private List<Belief> beliefs;
	private Map<Integer, List<Node>> solutionMap;

	private static World instance = null;

	public static World getInstance() {
		if (instance == null) {
			instance = new World();
		}
		return instance;
	}

	protected World() {
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

	public List<Belief> getBeliefs() {
		return beliefs;
	}

	public void setBeliefs(List<Belief> beliefs) {
		this.beliefs = beliefs;
	}

	public Set<Color> getColors() {
		return colors;
	}

	public void setColors(Set<Color> colors) {
		this.colors = colors;
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

	public Set<Position> getWalls() {
		return walls;
	}

	public void setWalls(Set<Position> walls) {
		this.walls = walls;
	}

	public Map<Integer, List<Node>> getSolutionMap() {
		return solutionMap;
	}

	public void setSolutionMap(Map<Integer, List<Node>> solutionMap) {
		this.solutionMap = solutionMap;
	}

	public boolean isGlobalGoalState() {
		boolean result = false;
		for (Integer goalId : this.goals.keySet()) {
			for (Integer boxId : this.boxes.keySet()) {
				Goal goal = this.goals.get(goalId);
				Box box = this.boxes.get(boxId);
				if (goal.getLetter() == Character.toLowerCase(box.getLetter())) {
					if (goal.getPosition().equals(box.getPosition())) {
						result = true;
						break;
					} else
						result = false;
				}
			}
			if (!result) {
				return false;
			}
		}
		return result;
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