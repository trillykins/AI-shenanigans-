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

	public static void moveAgentOnAgentNoBox(Agent agentToMove, Agent agentToStay, Box agentToMoveBox){
		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(new Position(agentToStay.getPosition()));
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
		
		for (Box box : agentToMove.initialState.boxes.values()) {
			agentToMove.initialState.walls.add(new Position(box.getPosition()));
		}
		Strategy strategy = new StrategyBFS();
		Search s = new Search();

		/*we add one no op to the newPlanAgentToStay*/
		List<Node> newPlanAgentToStay = Conflict.updatePlan(agentToStay);
		s.setPlanForAgentToStay(newPlanAgentToStay);
		LinkedList<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_AWAY);
		/*remember to move all walls away*/
		for (Box box : agentToMove.initialState.boxes.values()) {
			agentToMove.initialState.walls.remove(new Position(box.getPosition()));
		}
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));

		/*afterwards we insert noops*/
		if(newPlanAgentToMove != null && !newPlanAgentToMove.isEmpty()){
			newPlanAgentToMove = insertNoOps(newPlanAgentToMove,agentToMove);
			/*in pacman it was nessesary for to add a no op for the agentToStay, else our detection would detect conflicts in a loop*/
			Node noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(0));
			noOp.action = new Command();
			newPlanAgentToStay.add(0,noOp);
			World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());	
			System.err.println("1:");
		}else{
			/*the newplan is empty we just add a no op to existing plan*/
			newPlanAgentToMove = (LinkedList<Node>) Conflict.updatePlan(agentToMove);
			if (agentToMove.getStepInPlan() == 0){
				/*need to create a node where the agent is at current position*/
				Node noOp = createNoOpNode(agentToMove,agentToMove.initialState);
				newPlanAgentToMove.add(0,noOp);
			}
			Node noOp = createNoOpNode(agentToMove,newPlanAgentToMove.get(0));
			noOp.action = new Command();
			newPlanAgentToMove.add(0,noOp);
			newPlanAgentToMove.add(0,noOp);
			newPlanAgentToMove.add(0,noOp);
			System.err.println("2");
		}
		agentToMove.setPlan(newPlanAgentToMove);
		agentToMove.setStepInPlan(0);
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);
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


		/*we add one noOp to the newPlanAgentToStay*/
//		Node noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(newPlanAgentToStay.size()-1));
		Node noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(0));		
//		newPlanAgentToStay.remove(0);
		newPlanAgentToStay.add(0,noOp);
		newPlanAgentToMove = insertNoOps(newPlanAgentToMove,agentToMove);

		agentToMove.setPlan(newPlanAgentToMove);
		agentToMove.setStepInPlan(0);
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);
		World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
	}

	public static LinkedList<Node> insertNoOps(LinkedList<Node> newPlanAgentToMove, Agent agentToMove){
		Node noOp = null;
		int noOpsToAdd = 0;
		if (newPlanAgentToMove != null && !newPlanAgentToMove.isEmpty()) {
			noOp = createNoOpNode(agentToMove,newPlanAgentToMove.peekLast());//agentToMove.initialState;
			noOpsToAdd = newPlanAgentToMove.size();
			if (noOpsToAdd < 2)
				noOpsToAdd = 4;
			for (int i = 0; i < noOpsToAdd; i++) {
				Node n = newPlanAgentToMove.getLast().childNode();
				noOp = n;
				noOp.action = new Command();
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
