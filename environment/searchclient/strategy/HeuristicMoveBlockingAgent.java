package searchclient.strategy;

import java.util.ArrayList;

import searchclient.Agent;
import searchclient.node.Node;
import searchclient.node.Map;
import searchclient.node.MapAgent;

public class HeuristicMoveBlockingAgent extends Heuristic {
	private ArrayList<Map> route;
	private int row, col;

	public HeuristicMoveBlockingAgent(Agent agent, ArrayList<Map> route, int row, int col) {
		super(agent);
		this.row = row;
		this.col = col;
		this.route = new ArrayList<Map>();
		this.route.addAll(route);
	}

	@Override
	public int h(Node node) {
		MapAgent a = node.agents[agent.id];
		return 10 - node.distance(row, col, a.row, a.col);
	}
}