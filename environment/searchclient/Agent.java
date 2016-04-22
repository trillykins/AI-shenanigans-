package searchclient;

import java.util.ArrayList;
import java.util.LinkedList;
import java.io.IOException;

import searchclient.node.Node;
import searchclient.node.Colour;
import searchclient.node.MapGoal;
import searchclient.node.MapState;
import searchclient.node.MapAgent;
import searchclient.node.GoalState;
import searchclient.node.GoalState.FinalGoalState;
import searchclient.node.SearchResult;
import searchclient.strategy.Heuristic;
import searchclient.strategy.Strategy;

public class Agent {
	public int id;
	public Colour colour;
	public Node state;

	public ArrayList<MapGoal> subgoals = new ArrayList<>();
	public enum Status {
		IDLE, 
		PLAN, 
		STUCK, 
		HELPING, 
		STUCK_HELPING,
		DONE
	}
	public Status status = Status.IDLE;

	int startGoal;

	public Agent(int name, Colour colour) {
		this.id = name;
		if (colour == null) {
			this.colour = Colour.BLUE;
		} else {
			this.colour = colour;
		}
	}

	public Agent(MapAgent agent) {
		this.id = agent.id;
		this.colour = agent.colour;
	}

	public void setState(Node state) {
		Node node = state.DuplicateNode();
		node.parent = null;
		startGoal = node.g();
		this.state = node;
	}

	public void addSubgoal() {
		Heuristic heuristic = new Heuristic(this);
		MapGoal subgoal = heuristic.selectGoal(state);
		if (subgoal != null) {
			this.subgoals.add(subgoal);
		}
	}

	public SearchResult Search(Strategy strategy, ArrayList<MapGoal> goals) throws IOException {
		return Search(strategy, new FinalGoalState(goals), null);
	}
	public SearchResult Search(Strategy strategy, GoalState goal) throws IOException {
		return Search(strategy, goal, null);
	}
	public SearchResult Search(Strategy strategy, ArrayList<MapGoal> goals, SearchResult previousResult) throws IOException {
		return Search(strategy, new FinalGoalState(goals), previousResult);
	}

	public SearchResult Search(Strategy strategy, GoalState goal, SearchResult previousResult) throws IOException {
		strategy.addFrontier(this.state);
		while (true) {
			if (Memory.shouldEnd()) {
				MapState mapState = new MapState(strategy);
				return new SearchResult(SearchResult.Result.MEMLIMIT, new LinkedList<>(), mapState);
			}
			if (strategy.timeSpent() > Memory.timeLimit) {
				MapState mapState = new MapState(strategy);
				return new SearchResult(SearchResult.Result.TIMELIMIT, new LinkedList<>(), mapState);
			}
			if(strategy.exploredCount() > 150000) {
				MapState mapState = new MapState(strategy);
				return new SearchResult(SearchResult.Result.STUCK, new LinkedList<>(), mapState);
			}

			if (strategy.frontierIsEmpty()) {
				MapState mapState = new MapState(strategy);
				if (goal.eval(state)) {
					return new SearchResult(SearchResult.Result.DONE, new LinkedList<>(), mapState);
				} else if (previousResult == null) {
					return new SearchResult(SearchResult.Result.IMPOSIBLE, new LinkedList<>(), mapState);
				} else {
					return new SearchResult(SearchResult.Result.STUCK, new LinkedList<>(), mapState);
				}
			}
			Node leafNode = strategy.getAndRemoveLeaf();
			if (leafNode.g() > (20 + startGoal) && previousResult != null && leafNode.g() > (startGoal + previousResult.solution.size() * 2)) {
				MapState mapState = new MapState(strategy);
				if (previousResult.result == SearchResult.Result.DONE) {
					return new SearchResult(SearchResult.Result.DONE, new LinkedList<>(), mapState);
				}
				return new SearchResult(SearchResult.Result.STUCK, new LinkedList<>());
			}
			if (goal.eval(leafNode)) {
				MapState mapState = new MapState(strategy);
				if (leafNode.isInitialState()) {
					return new SearchResult(SearchResult.Result.DONE, new LinkedList<>(), mapState);
				}
				return new SearchResult(SearchResult.Result.PLAN, leafNode.extractPlan(), mapState);
			}
			strategy.addExplored(leafNode);
			for (Node node : leafNode.getExpandedNodes(id)) {
				if (!strategy.isExplored(node) && !strategy.inFrontier(node)) {
					strategy.addFrontier(node);
				}
			}
		}
	}

	@Override
	public boolean equals(Object object) {
		if (getClass() != object.getClass()) {
			return false;
		}
		Agent agent = (Agent)object;
		return (this.id == agent.id && this.colour == agent.colour);
	}

	@Override
	public int hashCode() {
		final int prime = 37;
		int result = 5;
		result = prime * result + this.id;
		return result;
	}

	@Override
	public String toString() {
		return id + " " + colour;
	}
}