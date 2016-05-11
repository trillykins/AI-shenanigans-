package conflicts;

import java.util.LinkedList;
import java.util.List;

import atoms.Agent;
import atoms.Box;
import atoms.Position;
import atoms.World;
import searchclient.Command;
import searchclient.Node;
import searchclient.Search;
import searchclient.Search.SearchType;
import strategies.Strategy;
import strategies.StrategyBFS;

public class MAAgentOnAgentConflict {
	
	public static void moveAgentOnAgentNoBox(Agent agentToMove, Agent agentToStay, Box boxToMove){
		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(new Position(agentToStay.getPosition()));
		for (Box box : agentToMove.initialState.boxes.values()) {
			agentToMove.initialState.walls.add(new Position(box.getPosition()));
		}
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		List<Node> newPlanAgentToStay = Conflict.updatePlan(agentToStay);
		s.setPlanForAgentToStay(newPlanAgentToStay);
		LinkedList<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_AWAY);
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));
		agentToStay.generateInitialState();
		
		newPlanAgentToMove = insertNoOps(newPlanAgentToMove,agentToMove);

		agentToMove.setPlan(newPlanAgentToMove);
		agentToMove.setStepInPlan(0);
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);
		World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());		
	}

	public static void moveAgentOnAgentWithBox(Agent agentToMove, Agent agentToStay, Box boxToMove){
		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(new Position(agentToStay.getPosition()));
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
		if (boxToMove  == null )
			boxToMove = World.getInstance().getBoxes().get(agentToMove.getIntention().getBox().getId());
		agentToMove.initialState.boxes.put(boxToMove.getId(), boxToMove);

		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		List<Node> newPlanAgentToStay = Conflict.updatePlan(agentToStay);
		s.setPlanForAgentToStay(newPlanAgentToStay);
		LinkedList<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_OWN_BOX);
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));
		agentToStay.generateInitialState();
		
		newPlanAgentToMove = insertNoOps(newPlanAgentToMove,agentToMove);

		agentToMove.setPlan(newPlanAgentToMove);
		agentToMove.setStepInPlan(0);
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);
		World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
	}
	
	public static LinkedList<Node> insertNoOps(LinkedList<Node> newPlanAgentToMove, Agent agentToMove){
		Node noOp = agentToMove.initialState;
		noOp.action = new Command();
		int noOpsToAdd = 0;
		if (newPlanAgentToMove != null && !newPlanAgentToMove.isEmpty()) {
				noOpsToAdd = newPlanAgentToMove.size();
				if (noOpsToAdd < 2)
					noOpsToAdd = 4;
				for (int i = 0; i < noOpsToAdd; i++) {
					Node n = newPlanAgentToMove.getLast().childNode();
					noOp = n;
					noOp.action = new Command();
					newPlanAgentToMove.add(noOp);
				}
		} else {
			newPlanAgentToMove = new LinkedList<Node>();
			agentToMove.generateInitialState();
			Node n = agentToMove.initialState;
			noOp = n;
			noOp.action = new Command();
			noOpsToAdd = 2; /*if we only add 1 noOp we will most likely have an error. We add 2 noOps*/
			for(int i = 0; i < noOpsToAdd;i++){
				newPlanAgentToMove.add(noOp);
			}
		}
		return newPlanAgentToMove;
	}
}
