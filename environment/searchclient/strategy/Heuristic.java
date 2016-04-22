package searchclient.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import searchclient.Agent;
import searchclient.node.MapAgent;
import searchclient.node.MapBox;
import searchclient.node.MapGoal;
import searchclient.node.Node;

public class Heuristic {
	public Agent agent;
	HashMap<Node, Integer> heuristics =new HashMap<>();
	private static HashMap<MapGoal, Integer> agentGoals = new HashMap<>();

	public Heuristic(Agent agent) {
		this.agent = agent;
	}

	public int h(Node n) {
		Integer tmpH = heuristics.get(n);
		HashSet<MapBox> usedBoxes = new HashSet<>();

		if (tmpH == null) {
			int goalCount = n.getGoals().size();
			for (MapGoal goal : n.getGoals()) {
				if (n.isGoalState(goal)) {
					goalCount--;
				}
			}			
			int h = goalCount * 5;
			for (MapGoal subgoal : agent.subgoals) {
				int tmp = Integer.MAX_VALUE;
				MapBox tmpBox = null;
				for (MapBox box : n.getBoxes(subgoal.getLetter())) {
					if (!usedBoxes.contains(box) && !n.isGoalState(subgoal) && n.isGoal(box.row, box.col) != box.getLetter() && n.distance(n.agents[agent.id], box) != null) {
						tmp = Math.min(tmp, n.distance(n.agents[agent.id], box) - 1 + n.distance(box, subgoal) * 2);
						tmpBox = box;
					}
				}
				if(tmp == Integer.MAX_VALUE) {					
					tmp = 0;
				}
				h += tmp;
				usedBoxes.add(tmpBox);
			}
			heuristics.put(n, h);
			return h;
		} else {
			return tmpH;
		}	
	}

	public MapGoal selectGoal(Node node) {
		MapGoal goal = selectGoalByDistance(node);
		if (goal != null) {
			Heuristic.agentGoals.put(goal, this.agent.id);
		}
		return goal;
	}

	private MapGoal selectGoalByDistance(Node node) {
		MapAgent agent = node.agents[this.agent.id];
		ArrayList<MapGoal> goals = node.getAgentGoals(agent.id);
		MapGoal selectedGoal = null;
		int distance = Integer.MAX_VALUE;
		for (MapGoal goal : goals) {
			if (node.isGoalState(goal)) {
				continue;
			} else if (node.distance(goal, agent) == null) {
				continue;
			} else if (Heuristic.agentGoals.get(goal) != null) {
				continue;
			}

			for (MapBox box : node.getBoxes(goal.getLetter())) {
				if (node.distance(agent, box) != null && (node.distance(agent, box)-1 + node.distance(box, goal)*goal.importance) < distance) {
					distance = node.distance(agent, box)-1 + node.distance(box, goal)*goal.importance;
					selectedGoal = goal;
				}
			}
		}
		return selectedGoal;
	}
}