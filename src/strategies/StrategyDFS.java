package strategies;

import java.util.Stack;

import searchclient.Node;

public class StrategyDFS extends Strategy {
	private Stack<Node> frontier;

	public StrategyDFS() {
		super();
		frontier = new Stack<Node>();
	}

	public Node getAndRemoveLeaf() {
		return frontier.pop();
	}

	public void addToFrontier(Node n) {
		frontier.push(n);
	}

	public int countFrontier() {
		return frontier.size();
	}

	public boolean frontierIsEmpty() {
		return frontier.isEmpty();
	}

	public boolean inFrontier(Node n) {
		return frontier.contains(n);
	}

	public String toString() {
		return "Depth-first Search";
	}
}