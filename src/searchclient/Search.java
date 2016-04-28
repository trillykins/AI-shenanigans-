package searchclient;

import java.util.LinkedList;
import java.util.List;

import atoms.Agent;
import atoms.Box;
import atoms.World;
import strategies.Strategy;

public class Search {
	public static int TIME = 300;
	private List<Node> otherPlan;

	public static enum SearchType {
		PATH, MoveToPosition, MoveAway
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
//				 System.err.println(strategy.searchStatus());
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
			case MoveToPosition:
				if (leafNode.agentAtMovePosition())
					return leafNode.extractPlan();
				break;
			case MoveAway:
				if (leafNode.movedAway(otherPlan)) {
					return leafNode.extractPlan();
				}
				break;
			default:
				break;
			}

			strategy.addToExplored(leafNode);
			for (Node n : leafNode.getExpandedNodes()) {
				if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
					strategy.addToFrontier(n);
				}
			}
			iterations++;
		}
	}

	public static boolean canMakeNextMove(int index, List<LinkedList<Node>> allSolutions) {
		if (World.getInstance().getAgents().size() == 1) {
			/* This for loop only contains one agent */
			Agent a1 = World.getInstance().getAgents().get(0);
			/*
			 * as there is no other agents that can be in a1's way, the only
			 * obsticle a1 can bump into is a box
			 */
			for (Box box : World.getInstance().getBoxes().values()) {
				if (box.getPosition().equals(a1.getPosition()))
					return false;
			}
		}
		for (Agent a1 : World.getInstance().getAgents().values()) {
			for (Agent a2 : World.getInstance().getAgents().values()) {
				if (a2.getId() != a1.getId()) {
					if (allSolutions.size() > a2.getId() && allSolutions.get(a2.getId()).size() > index) {
						if (allSolutions.size() > a1.getId() && allSolutions.get(a1.getId()).size() > index) {
							Node currAgentSol = allSolutions.get(a1.getId()).get(index);
							Node agentSol = allSolutions.get(a2.getId()).get(index);
							if (currAgentSol.agentRow == agentSol.agentRow && currAgentSol.agentCol == agentSol.agentCol
									|| a1.getPosition().getX() == agentSol.agentRow
											&& a1.getPosition().getY() == agentSol.agentCol) {
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
}
