package searchclient.strategy;

import searchclient.Agent;
import searchclient.node.Node;
import searchclient.node.MapBox;

public class HeuristicMoveNearBox extends Heuristic {

	private MapBox box;

	public HeuristicMoveNearBox(Agent agent, MapBox box) {
		super(agent);
		this.box = box;
	}

	@Override
	public int h(Node node) {
		return node.distance(node.agents[agent.id], box) - 1;
	}
}