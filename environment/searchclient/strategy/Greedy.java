package searchclient.strategy;

import searchclient.node.Node;

public class Greedy extends Strategy {

	public Heuristic heuristic;
	
	public Greedy(Heuristic heuristic) {
		super();
		this.heuristic = heuristic;
	}

	public int f(Node node) {
		return heuristic.h(node);
	}
}