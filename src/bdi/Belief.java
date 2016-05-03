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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((goal == null) ? 0 : goal.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Belief))
			return false;
		Belief other = (Belief) obj;
		if (goal == null) {
			if (other.goal != null)
				return false;
		} else if (!goal.equals(other.goal))
			return false;
		return true;
	}
}
