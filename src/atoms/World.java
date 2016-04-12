package atoms;

import java.util.Map;
import java.util.Set;

public class World {
	private Map<Integer, Agent> agents;
	private Map<Integer, Box> boxes;
	private Map<Integer, Goal> goals;
	private Set<Position> walls;
	private Set<Color> colors;
	private static World instance = null;

	public static World getInstance() {
		if (instance == null) {
			instance = new World();
		}
		return instance;
	}

	protected World() {
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
}