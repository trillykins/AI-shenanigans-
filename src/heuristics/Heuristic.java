package heuristics;

import java.util.Comparator;

import atoms.Box;
import atoms.Goal;
import searchclient.Node;
import searchclient.SearchClient;

public abstract class Heuristic implements Comparator<Node> {

	public Node initialState;

	public Heuristic(Node initialState) {
		this.initialState = initialState;
	}

	public int compare(Node n1, Node n2) {
		return f(n1) - f(n2);
	}

	public abstract int f(Node n);

	public int h(Node n) {
		int sum = 0;
		for (Integer bId : n.boxes.keySet()) {
			Box b = n.boxes.get(bId);
			for (Goal goal : SearchClient.precomputedGoalH.keySet()) {
				if (Character.toLowerCase(b.getLetter()) == goal.getLetter()) {
					Byte[][] dist = SearchClient.precomputedGoalH.get(goal);
					int bDist = dist[b.getPosition().getX()][b.getPosition().getY()];
					int ax = Math.abs(n.agentRow - b.getPosition().getX());
					int ay = Math.abs(n.agentCol - b.getPosition().getY());
					sum += ax + ay + bDist;
				}
			}
		}
		return sum;
	}
}