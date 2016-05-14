package conflicts;

import java.util.LinkedList;
import java.util.List;

import atoms.Agent;
import atoms.Box;
import atoms.World;
import searchclient.Node;
import searchclient.Search;
import searchclient.Search.SearchType;
import strategies.StrategyBFS;

public class SABoxOnBoxConflict {

	public static void solveBoxOnBoxSA(Node node, Agent agent, Box intentionBox, Box conflictingBox) {
		agent.generateInitialState();
		agent.initialState.setPosition(agent.getPosition());
		agent.initialState.boxes.put(intentionBox.getId(), intentionBox);
		agent.initialState.boxes.put(conflictingBox.getId(), conflictingBox);
		List<Node> otherPlan = Conflict.updatePlan(agent);
//		agent.initialState.moveToPositionRow = otherPlan.get(0).getAgentPosition().getX();
//		agent.initialState.moveToPositionCol = otherPlan.get(0).getAgentPosition().getY();
		Search s = new Search();
		s.setPlanForAgentToStay(otherPlan);
		LinkedList<Node> plan = s.search(new StrategyBFS(), agent.initialState, SearchType.MOVE_OWN_BOX);
		agent.setPlan(plan);
		agent.setStepInPlan(0);
		World.getInstance().getBeliefs().add(agent.getIntention().getDesire().getBelief());
	}
}