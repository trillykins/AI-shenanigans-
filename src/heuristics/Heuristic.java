package heuristics;

import java.util.Comparator;

import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;
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
		for (Box b : n.boxes.values()) {
			for (Integer goalId : SearchClient.precomputedGoalH.keySet()) {
				Goal goal = World.getInstance().getGoals().get(goalId);
				if (Character.toLowerCase(b.getLetter()) == goal.getLetter()) {
					Byte[][] dist = SearchClient.precomputedGoalH.get(goalId);
					int bDist = dist[b.getPosition().getX()][b.getPosition().getY()];
					int ax = Math.abs(n.agentRow - b.getPosition().getX());
					int ay = Math.abs(n.agentCol - b.getPosition().getY());
					sum += ax + ay + bDist;
				}
			}
		}
		return sum;
	}
	
	public int moveBox(Node n) {
		Position p1 = n.getBoxToPosition();
		int ax = 0, ay = 0;
		for(Box b : n.boxes.values()) {
			ax = Math.abs(p1.getX() - b.getPosition().getX());
			ay = Math.abs(p1.getY() - b.getPosition().getY());
		}
		return ax + ay;
	}
}