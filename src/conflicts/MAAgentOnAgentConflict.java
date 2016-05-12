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
		
		/*we add one no op to the newPlanAgentToStay*/
		List<Node> newPlanAgentToStay = Conflict.updatePlan(agentToStay);
		Node noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(0));
		newPlanAgentToStay.remove(0);
		newPlanAgentToStay.add(noOp);
		s.setPlanForAgentToStay(newPlanAgentToStay);
		
		LinkedList<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_AWAY);
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));
		agentToStay.generateInitialState();
		
		/*afterwards we insert noops*/
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
		
		/*we add one no op to the newPlanAgentToStay*/
		List<Node> newPlanAgentToStay = Conflict.updatePlan(agentToStay);
		Node noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(0));
		newPlanAgentToStay.remove(0);
		newPlanAgentToStay.add(noOp);
		
		s.setPlanForAgentToStay(newPlanAgentToStay);
		
		LinkedList<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_OWN_BOX);
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));
				
		newPlanAgentToMove = insertNoOps(newPlanAgentToMove,agentToMove);

		agentToMove.setPlan(newPlanAgentToMove);
		agentToMove.setStepInPlan(0);
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);
		World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
	}
	
	public static LinkedList<Node> insertNoOps(LinkedList<Node> newPlanAgentToMove, Agent agentToMove){
		Node noOp = createNoOpNode(agentToMove,newPlanAgentToMove.peekLast());//agentToMove.initialState;
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
	
	private static Node createNoOpNode(Agent agent, Node parent) {
		Node node = new Node(parent,agent.getId());
		node.action = new Command();
		if(parent != null) {
			node.boxes = parent.boxes;
			node.agentCol = parent.agentCol;
			node.agentRow = parent.agentRow;
		}else {
			node.boxes = agent.initialState.boxes;
			node.agentCol = agent.getPosition().getY();
			node.agentRow = agent.getPosition().getX();
		}
		
		node.goals = agent.initialState.goals;
		node.walls = agent.initialState.walls;
		return node;
	}
}
