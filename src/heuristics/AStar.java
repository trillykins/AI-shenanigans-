package heuristics;

import searchclient.Node;
import searchclient.Search.SearchType;

public class AStar extends Heuristic {
	public AStar(Node initialState) {
		super(initialState);
	}

	public int f(Node n) {
		if(n.getSearchType() != null && n.getSearchType().equals(SearchType.MOVE_BOX))
			return n.g() + moveBox(n);
		return n.g() + h(n);
	}

	public String toString() {
		return "A* evaluation";
	}
}