package bdi;

import atoms.Goal;

public class Belief {
	private Goal goal;

	public Belief(Goal goal) {
		this.goal = goal;
	}

	public Goal getGoal() {
		return goal;
	}

	public void setGoal(Goal goal) {
		this.goal = goal;
	}
	
	@Override
	public String toString(){
		return "(" + goal.getId() + ", " + goal.getLetter() + ", " + goal.getPriority() + ")";
	}
}
