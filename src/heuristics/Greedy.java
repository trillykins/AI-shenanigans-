package heuristics;

import searchclient.Node;

public class Greedy extends Heuristic {

	public Greedy(Node initialState) {
		super(initialState);
	}

	@Override
	public int f(Node n) {
		return h(n);
	}

	@Override
	public String toString() {
		return "Greedy evaluation";
	}
}
