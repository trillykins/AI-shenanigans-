package searchclient.strategy;

import java.util.ArrayList;

import searchclient.Agent;
import searchclient.node.Node;
import searchclient.node.Map;
import searchclient.node.MapAgent;
import searchclient.node.MapBox;

public class HeuristicMoveBlockingBox extends Heuristic {

	private ArrayList<Map> route;
	private int boxID;

	public HeuristicMoveBlockingBox(Agent agent, int boxID, ArrayList<Map> route) {
		super(agent);
		this.boxID = boxID;
		this.route = new ArrayList<Map>();
		this.route.addAll(route);
	}

	@Override
	public int h(Node node) {
		int f = 0;
		for (Map map : route) {
			Object object = node.objectAt(map);
			if (object instanceof MapAgent && ((MapAgent)object).id == agent.id) {
				f++;
			} else if (object instanceof MapBox && ((MapBox)object).id == this.boxID) {
				f++;
			}
		}
		return f;
	}
}