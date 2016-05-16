package conflicts;

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
		agent.initialState.walls.add(conflictingBox.getPosition());
		agent.initialState.boxes.put(intentionBox.getId(), intentionBox);
		agent.initialState.goals.put(agent.getIntention().getDesire().getBelief().getGoal().getId(), agent.getIntention().getDesire().getBelief().getGoal());
		Search s = new Search();
		List<Node> plan = s.search(new StrategyBFS(), agent.initialState, SearchType.PATH);
		agent.initialState.walls.remove(conflictingBox.getPosition());	
		if (plan != null && !plan.isEmpty()) {
			agent.setPlan(plan);
			agent.setStepInPlan(0);
		} else {
			agent.generateInitialState();
			agent.initialState.setPosition(agent.getPosition());
			agent.initialState.boxes.put(intentionBox.getId(), intentionBox);
			agent.initialState.boxes.put(conflictingBox.getId(), conflictingBox);
			List<Node> otherPlan = Conflict.updatePlan(agent);
			s = new Search();
			s.setPlanForAgentToStay(otherPlan);
			plan = s.search(new StrategyBFS(), agent.initialState, SearchType.MOVE_OWN_BOX);
			agent.setPlan(plan);
			agent.setStepInPlan(0);
			World.getInstance().getBeliefs().add(agent.getIntention().getDesire().getBelief());
		}
	}
}