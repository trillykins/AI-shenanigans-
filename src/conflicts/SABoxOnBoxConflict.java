package conflicts;

import java.util.LinkedList;

import atoms.Agent;
import atoms.Box;
import searchclient.Node;
import searchclient.Search;
import searchclient.Search.SearchType;
import strategies.StrategyBFS;

public class SABoxOnBoxConflict {

	public static void solveBoxOnBoxSA(Node node, Agent agent, Box intentionBox, Box conflictingBox) {

		agent.generateInitialState();
		agent.initialState.agentRow = agent.getPosition().getX();
		agent.initialState.agentCol = agent.getPosition().getY();
		agent.initialState.boxes.put(intentionBox.getId(), intentionBox);
		agent.initialState.boxes.put(conflictingBox.getId(), conflictingBox);
		
		Search s = new Search();
		s.setPlanForAgentToStay(Conflict.updatePlan(agent));
		LinkedList<Node> plan = s.search(new StrategyBFS(), agent.initialState, SearchType.MOVE_OWN_BOX);
		agent.setPlan(plan);
		agent.setStepInPlan(0);
//		if (!World.getInstance().getBeliefs().contains(agent.getIntention().getDesire().getBelief())) {
//			World.getInstance().getBeliefs().add(agent.getIntention().getDesire().getBelief());
//		}
		// Agent agentToMove = agent;
		// agentToMove.generateInitialState();
		// agentToMove.initialState.setPosition(World.getInstance().getAgents().get(0).getPosition());
		// agentToMove.initialState.boxes.put(agent.getIntention().getBox().getId(),
		// World.getInstance().getBoxes().get(agent.getIntention().getBox().getId()));
		// agentToMove.initialState.goals.put(agent.getIntention().getDesire().getBelief().getGoal().getId(),
		// agent.getIntention().getDesire().getBelief().getGoal());
		//
		// Agent tmp = new Agent(agentToMove);
		// tmp.initialState.walls.add(conflictingBox.getPosition());
		// Strategy strategy = new StrategyBestFirst(new
		// AStar(tmp.initialState));
		// Search s = new Search();
		//
		// List<Node> plan = s.search(strategy, tmp.initialState,
		// SearchType.PATH);
		// if (plan == null || plan.isEmpty()) {
		// if (tmp.initialState.walls.contains(conflictingBox.getPosition()))
		// tmp.initialState.walls.remove(conflictingBox.getPosition());
		// tmp.initialState.boxes.put(conflictingBox.getId(), conflictingBox);
		// strategy = new StrategyBestFirst(new AStar(tmp.initialState));
		// s = new Search();
		// plan = s.search(strategy, tmp.initialState, SearchType.PATH);
		// } else {
		// tmp.initialState.walls.remove(conflictingBox.getPosition());
		// }
		//
		// // check if agent's position overlaps with any other boxes
		// boolean planValid = true;
		// do {
		// planValid = true;
		//
		// outer: for (Node n : plan) {
		// for (Box nb : n.boxes.values()) {
		// for (Box wb : World.getInstance().getBoxes().values()) {
		// if (nb.getId() != wb.getId() &&
		// nb.getPosition().equals(wb.getPosition())
		// && !n.boxes.containsKey(wb.getId())
		// && !tmp.initialState.walls.contains(wb.getPosition())) {
		// tmp.initialState.boxes.put(wb.getId(), wb);
		// planValid = false;
		// System.err.println(plan);
		// System.err.println(tmp.initialState);
		// break outer;
		// }
		// }
		// }
		// }
		// if (!planValid) {
		// plan = s.search(new StrategyBestFirst(new AStar(tmp.initialState)),
		// tmp.initialState, SearchType.PATH);
		// System.err.println("NOT VALID!");
		// }
		// } while (!planValid);
		//
		// // remove all other boxes from agent except its own
		// for (Box box : tmp.initialState.boxes.values()) {
		// if (!intentionBox.equals(box)) {
		// tmp.initialState.boxes.remove(box);
		// }
		// }
		//
		// agentToMove.setPlan(plan);
		// agentToMove.setStepInPlan(0);
		// World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());

	}

}
