package atoms;

import java.util.Map;
import java.util.Set;

public class World {
	private Map<Integer, Agent> agents;
	private Map<Integer, Box> boxes;
	private Map<Integer, Goal> goals;
	private Set<Position> walls;
	private static World instance = null;
	
	public static World getInstance() {
	      if(instance == null) {
	         instance = new World();
	      }
	      return instance;
	   }
	
	protected World() {}

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
} 