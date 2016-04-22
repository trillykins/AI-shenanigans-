package searchclient.strategy;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.HashSet;

import searchclient.node.Node;

public abstract class Strategy implements Comparator<Node> {

	private PriorityQueue<Node> priorityQueue;
	private HashSet<Node> frontier;
	public HashSet<Node> explored;
	public long startTime = System.currentTimeMillis();

	public Strategy() {
		explored = new HashSet<Node>();
		priorityQueue = new PriorityQueue<>(this);
		frontier = new HashSet<Node>();
	}

	public Node getAndRemoveLeaf() {
		Node node = priorityQueue.poll();
		frontier.remove(node);
		return node;
	}

	public void addExplored(Node node) {
		explored.add(node);
	}

	public boolean isExplored(Node node) {
		return explored.contains(node);
	}

	public int exploredCount() {
		return explored.size();
	}

	public void addFrontier(Node node) {
		priorityQueue.add(node);
		frontier.add(node);
	}

	public boolean frontierIsEmpty() {
		return priorityQueue.isEmpty();
	}

	public boolean inFrontier(Node node) {
		return frontier.contains(node);
	}
	
	public int frontierCount() {
		return priorityQueue.size();
	}

	public float timeSpent() {
		return (System.currentTimeMillis() - startTime ) / 1000f;
	}


	public abstract int f(Node n);

	public int compare(Node n1, Node n2) {
		return f(n1) - f(n2);
	}
}