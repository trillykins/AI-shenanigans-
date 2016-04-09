package atoms;

import java.util.Map;
import java.util.Set;

public class World {
	private Map<Integer, Agent> agents;
	private Set<Box> boxes;
	private Set<Goal> goals;
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

	public Set<Box> getBoxes() {
		return boxes;
	}

	public void setBoxes(Set<Box> boxes) {
		this.boxes = boxes;
	}

	public Set<Goal> getGoals() {
		return goals;
	}

	public void setGoals(Set<Goal> goals) {
		this.goals = goals;
	}

	public Set<Position> getWalls() {
		return walls;
	}

	public void setWalls(Set<Position> walls) {
		this.walls = walls;
	}
	
	public void update(Map<Integer, Agent> agents, Set<Box> boxes) {
		setAgents(agents);
		setBoxes(boxes);
	}
} 