package bdi;

import atoms.Agent;

public class Desire {
	private Belief belief;
	private Agent agent;

	public Desire(Belief belief, Agent agent) {
		this.belief = belief;
		this.agent = agent;
	}

	public Belief getBelief() {
		return belief;
	}

	public void setBelief(Belief belief) {
		this.belief = belief;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}
	
	@Override
	public String toString(){
		return agent.getId() + ": " + belief.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agent == null) ? 0 : agent.hashCode());
		result = prime * result + ((belief == null) ? 0 : belief.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Desire))
			return false;
		Desire other = (Desire) obj;
		if (agent == null) {
			if (other.agent != null)
				return false;
		} else if (!agent.equals(other.agent))
			return false;
		if (belief == null) {
			if (other.belief != null)
				return false;
		} else if (!belief.equals(other.belief))
			return false;
		return true;
	}
}