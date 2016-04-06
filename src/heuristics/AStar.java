package heuristics;

import searchclient.Node;

public class AStar extends Heuristic {
	public AStar(Node initialState) {
		super(initialState);
	}

	@Override
	public int f(Node n) {
		return n.g() + h(n);
	}

	@Override
	public String toString() {
		return "A* evaluation";
	}
}