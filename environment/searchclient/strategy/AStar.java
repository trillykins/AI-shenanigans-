package searchclient.strategy;

import searchclient.node.Node;

public class AStar extends Strategy {
	public Heuristic heuristic;
	public AStar(Heuristic heuristic) {
		super();
		this.heuristic = heuristic;
	}

	public int f(Node node) {
		return node.g() + heuristic.h(node);
	}
}