package searchclient.node;

import java.util.ArrayList;

public abstract class GoalState {

	abstract public boolean eval(Node node);

	public static class FinalGoalState extends GoalState {
		private ArrayList<MapGoal> goals;
		
		public FinalGoalState(ArrayList<MapGoal> goals) {
			this.goals = goals;
		}

		@Override
		public boolean eval(Node node) {
			return node.isGoalState(goals);
		}
	}

	public static class RouteClearGoalState extends GoalState {
		private int agentID, boxID;
		private ArrayList<Map> route;

		public RouteClearGoalState(int agentID, int boxID, ArrayList<Map> route) {
			this.agentID = agentID;
			this.boxID = boxID;
			this.route = new ArrayList<>();
			this.route.addAll(route);
		}

		public boolean eval(Node node) {
			MapAgent agent = node.agents[this.agentID];
			MapBox box = null;
			if (this.boxID != -1) {
				box = node.getBoxesByID().get(this.boxID);
			}
			for (Map map : route) {
				if (agent.row == map.row && agent.col == map.col) {
					return false;
				}
				if (box != null && box.row == map.row && box.col == map.col) {
					return false;
				}
			}
			return true;
		}
	}

	public static class RouteClearOfAgentGoalState extends GoalState {
		private int agentID;
		private ArrayList<Map> route;

		public RouteClearOfAgentGoalState(int agentID, ArrayList<Map> route) {
			this.agentID = agentID;
			this.route = route;
		}

		public boolean eval(Node node) {
			MapAgent agent = node.agents[agentID];
			for (Map box : route) {
				if (agent.row == box.row && agent.col == box.col) {
					return false;
				}
			}
			return true;
		}
	}

	public static class NearGoalState extends GoalState {
		private int agentID;
		private int targetRow, targetCol;

		public NearGoalState(int agentID, int targetRow, int targetCol) {
			this.agentID = agentID;
			this.targetCol = targetCol;
			this.targetRow = targetRow;
		}

		public boolean eval(Node node){
			MapAgent agent = node.agents[agentID];
			return (( targetRow == agent.row+1 && targetCol == agent.col ) ||
					( targetRow == agent.row-1 && targetCol == agent.col ) ||
					( targetRow == agent.row && targetCol == agent.col+1 ) ||
					( targetRow == agent.row && targetCol == agent.col-1 ));
		}
	}
}