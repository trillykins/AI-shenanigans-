package bdi;

import atoms.Goal;

public class Belief {
	private Goal goal;
	private boolean reserved;
	
	public Belief(Goal goal) {
		this.goal = goal;
	}

	public Goal getGoal() {
		return goal;
	}

	public void setGoal(Goal goal) {
		this.goal = goal;
	}
	
	public boolean isReserved(){
		return reserved;
	}
	
	public void setReserved(boolean shit){
		this.reserved = shit;
	}
	
	@Override
	public String toString(){
		return "(" + goal.getId() + ", " + goal.getLetter() + ", " + goal.getPriority() + ")";
	}
}
