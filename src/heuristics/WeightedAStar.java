package heuristics;

import searchclient.Node;

public class WeightedAStar extends Heuristic {
	private int W;

	public WeightedAStar(Node initialState) {
		super(initialState);
		W = 5; 
	}

	@Override
	public int f(Node n) {
		return n.g() + W * h(n);
	}

	@Override
	public String toString() {
		return String.format("WA*(%d) evaluation", W);
	}
}