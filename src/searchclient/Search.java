package searchclient;

import java.util.LinkedList;
import java.util.List;

import atoms.Box;
import strategies.Strategy;

public class Search {
	public static int TIME = 300;
	private List<Node> otherPlan;
	private List<Box> futureBoxPositions;

	public static enum SearchType {
		PATH, MOVE_TO_POSITION, MOVE_AWAY, MOVE_OWN_BOX, MOVE_BOXES
	}

	public List<Box> getFutureBoxPositions() {
		return futureBoxPositions;
	}

	public void setFutureBoxPositions(List<Box> futureBoxPositions) {
		this.futureBoxPositions = futureBoxPositions;
	}

	public void setPlanForAgentToStay(List<Node> otherPlan) {
		this.otherPlan = otherPlan;
	}

	public List<Node> getOtherPlan() {
		return this.otherPlan;
	}

	public LinkedList<Node> search(Strategy strategy, Node initialState, SearchType searchType) {
		// System.err.format("Search starting with strategy %s\n", strategy);
		strategy.addToFrontier(initialState);
		int iterations = 0;
		while (true) {
			if (iterations % 200 == 0) {
				// System.err.println(strategy.searchStatus());
			}
			if (Memory.shouldEnd()) {
				System.err.format("Memory limit almost reached, terminating search %s\n", Memory.stringRep());
				return null;
			}
			if (strategy.timeSpent() > TIME) { // Minutes timeout
				System.err.format("Time limit reached, terminating search %s\n", Memory.stringRep());
				return null;
			}
			if (strategy.frontierIsEmpty()) {
				return null;
			}
			Node leafNode = strategy.getAndRemoveLeaf();

			switch (searchType) {
			case PATH:
				if (leafNode.isGoalState())
					return leafNode.extractPlan();
				break;
			case MOVE_TO_POSITION:
				if (leafNode.agentAtMovePosition())
					return leafNode.extractPlan();
				break;
			case MOVE_AWAY:
				if (leafNode.movedAway(otherPlan)) {
					return leafNode.extractPlan();
				}
				break;
			case MOVE_OWN_BOX:
				if (leafNode.moveAgentAndBoxAway(otherPlan)) {
					return leafNode.extractPlan();
				}
				break;
			case MOVE_BOXES:
				if (leafNode.moveBoxesAway(futureBoxPositions, otherPlan)) {
					return leafNode.extractPlan();
				}
				break;
			default:
				break;
			}

			strategy.addToExplored(leafNode);
			List<Node> expNodes = leafNode.getExpandedNodes();
			for (int i = 0; i < expNodes.size(); i++) {
				Node n = expNodes.get(i);
				if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
					strategy.addToFrontier(n);
				}
			}
			iterations++;
		}
	}
}
