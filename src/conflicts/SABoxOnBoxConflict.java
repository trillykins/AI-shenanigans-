package conflicts;

import java.util.List;

import atoms.Agent;
import atoms.Box;
import atoms.World;
import heuristics.AStar;
import searchclient.Node;
import searchclient.Search;
import searchclient.Search.SearchType;
import strategies.Strategy;
import strategies.StrategyBestFirst;

public class SABoxOnBoxConflict {

	// TODO fix: does not validate plan
	public static void solveBoxOnBoxSA(Node node, Agent agent, Box intentionBox, Box conflictingBox) {
		Agent agentToMove = agent;
		agentToMove.generateInitialState();
		agentToMove.initialState.setPosition(World.getInstance().getAgents().get(0).getPosition());
		agentToMove.initialState.boxes.put(agent.getIntention().getBox().getId(),
				World.getInstance().getBoxes().get(agent.getIntention().getBox().getId()));
		agentToMove.initialState.goals.put(agent.getIntention().getDesire().getBelief().getGoal().getId(),
				agent.getIntention().getDesire().getBelief().getGoal());

		Agent tmp = new Agent(agentToMove);
		tmp.initialState.walls.add(conflictingBox.getPosition());
		Strategy strategy = new StrategyBestFirst(new AStar(tmp.initialState));
		Search s = new Search();

		List<Node> plan = s.search(strategy, tmp.initialState, SearchType.PATH);
		if (plan == null || plan.isEmpty()) {
			if (tmp.initialState.walls.contains(conflictingBox.getPosition()))
				tmp.initialState.walls.remove(conflictingBox.getPosition());
			tmp.initialState.boxes.put(conflictingBox.getId(), conflictingBox);
			strategy = new StrategyBestFirst(new AStar(tmp.initialState));
			s = new Search();
			plan = s.search(strategy, tmp.initialState, SearchType.PATH);
		} else {
			tmp.initialState.walls.remove(conflictingBox.getPosition());
		}

		// check if agent's position overlaps with any other boxes
		boolean planValid = true;
		do {
			planValid = true;

			outer: for (Node n : plan) {
				for (Box nb : n.boxes.values()) {
					for (Box wb : World.getInstance().getBoxes().values()) {
						if (nb.getId() != wb.getId() && nb.getPosition().equals(wb.getPosition())
								&& !n.boxes.containsKey(wb.getId())
								&& !tmp.initialState.walls.contains(wb.getPosition())) {
							tmp.initialState.boxes.put(wb.getId(), wb);
							planValid = false;
							System.err.println(plan);
							System.err.println(tmp.initialState);
							break outer;
						}
					}
				}
			}
			if (!planValid) {
				plan = s.search(new StrategyBestFirst(new AStar(tmp.initialState)), tmp.initialState, SearchType.PATH);
				System.err.println("NOT VALID!");
			}
		} while (!planValid);

		// remove all other boxes from agent except its own
		for (Box box : tmp.initialState.boxes.values()) {
			if (!intentionBox.equals(box)) {
				tmp.initialState.boxes.remove(box);
			}
		}

		agentToMove.setPlan(plan);
		agentToMove.setStepInPlan(0);
		World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());

	}

}
