package bdi;

import java.util.LinkedList;

import searchclient.Node;

public class Intention {
	private Desire desire;
	private LinkedList<Node> plan;

	public Intention(Desire desire, LinkedList<Node> plan) {
		this.desire = desire;
		this.plan = plan;
	}
	
	public Desire getDesire() {
		return desire;
	}
	public void setDesire(Desire desire) {
		this.desire = desire;
	}
	public LinkedList<Node> getPlan() {
		return plan;
	}
	public void setPlan(LinkedList<Node> plan) {
		this.plan = plan;
	}
}