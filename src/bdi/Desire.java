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
}