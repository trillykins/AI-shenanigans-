package conflicts;

import java.util.LinkedList;
import java.util.List;

import analysis.FreeSpace;
import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import searchclient.Command;
import searchclient.Node;
import searchclient.Search;
import searchclient.Search.SearchType;
import strategies.Strategy;
import strategies.StrategyBFS;
import utils.Utils;

public class MAAgentOnAgentConflict {

	public static void moveAgentOnAgentNoBox(Agent agentToMove, Agent agentToStay, Box agentToMoveBox){
		if(agentToStay.getStepInPlan() == 0)
			addNoOpToAgentToStay(agentToMove,agentToStay);
		else{
			agentToMove.generateInitialState();
			agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
			agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
			agentToMove.initialState.walls.add(new Position(agentToStay.getPosition()));
			if (agentToMoveBox  == null && agentToMove.getIntention() != null )
				agentToMoveBox = World.getInstance().getBoxes().get(agentToMove.getIntention().getBox().getId());

			for(Box box : World.getInstance().getBoxes().values()){
				agentToMove.initialState.walls.add(new Position(box.getPosition()));
			}
			Strategy strategy = new StrategyBFS();
			Search s = new Search();

			/*we add one no op to the newPlanAgentToStay*/
			List<Node> newPlanAgentToStay = Conflict.updatePlan(agentToStay);
			s.setPlanForAgentToStay(newPlanAgentToStay);
			LinkedList<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_AWAY);
			/*remember to move all walls away*/
			agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));
			//		for (Box box : agentToMove.initialState.boxes.values()) {
			for(Box box : World.getInstance().getBoxes().values()){
				agentToMove.initialState.walls.remove(new Position(box.getPosition()));
			}
			boolean putAgentInACorner = false;
			/*the agentToMove is locked in a narrow corridor by boxes and the agentToStay*/
			/*we try to do a new plan for the agentToStay instead*/
			if(newPlanAgentToMove == null){
				newPlanAgentToMove = (LinkedList<Node>) Conflict.updatePlan(agentToMove);
				agentToStay.generateInitialState();
				agentToStay.initialState.agentRow = agentToStay.getPosition().getX();
				agentToStay.initialState.agentCol = agentToStay.getPosition().getY();
				agentToStay.initialState.walls.add(new Position(agentToMove.getPosition()));
				for(Box box : World.getInstance().getBoxes().values()){
					agentToStay.initialState.walls.add(new Position(box.getPosition()));
				}
				s.setPlanForAgentToStay(newPlanAgentToMove);
				newPlanAgentToStay = s.search(strategy, agentToStay.initialState, SearchType.MOVE_AWAY);
				agentToStay.initialState.walls.remove(agentToMove.getPosition());
				for(Box box : World.getInstance().getBoxes().values()){
					agentToStay.initialState.walls.remove(box.getPosition());
				}

				if(newPlanAgentToStay == null || newPlanAgentToStay.isEmpty()){
					/*we put the agent to stay in a corner*/
					putAgentInACorner = putAgentInACorner(agentToStay,agentToMove,newPlanAgentToMove,agentToMoveBox);
				}else{
					Node noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(newPlanAgentToStay.size()-1));
					noOp.action = new Command();
					for(int i = 0;i<newPlanAgentToMove.size();i++){
						newPlanAgentToStay.add(noOp);
					}
					World.getInstance().getBeliefs().add(agentToStay.getIntention().getDesire().getBelief());
				}

			}
			/*afterwards we insert noops*/
			if(!putAgentInACorner){
				if(newPlanAgentToMove != null && !newPlanAgentToMove.isEmpty()){
					newPlanAgentToMove = insertNoOps(newPlanAgentToMove,agentToMove);
					/*in pacman it was nessesary for to add a no op for the agentToStay, else our detection would detect conflicts in a loop*/
					Node noOp = null;
					if(newPlanAgentToStay.isEmpty()){
						noOp = createNoOpNode(agentToStay,agentToStay.initialState);
						noOp.action = new Command();
						newPlanAgentToStay.add(noOp);
					}else{
						noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(0));
						newPlanAgentToStay.remove(0);
						noOp.action = new Command();
						newPlanAgentToStay.add(0,noOp);
					}
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
					/*we need to copy the first node, but remember to remove it since it still has the not-noOp command*/
					Node noOp = createNoOpNode(agentToMove,newPlanAgentToMove.get(0));
					newPlanAgentToMove.remove(0);	
					noOp.action = new Command();
					newPlanAgentToMove.add(0,noOp);
					newPlanAgentToMove.add(0,noOp);
					newPlanAgentToMove.add(0,noOp);
				}
			}
			if(!putAgentInACorner){
				agentToMove.setPlan(newPlanAgentToMove);
				agentToMove.setStepInPlan(0);
				agentToStay.setPlan(newPlanAgentToStay);
				agentToStay.setStepInPlan(0);
			}
		}
	}

	public static void addNoOpToAgentToStay(Agent agentToMove, Agent agentToStay){
		List<Node> newPlanAgentToStay = Conflict.updatePlan(agentToStay);
		newPlanAgentToStay.add(0,agentToStay.initialState);
		Node noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(0));	
		newPlanAgentToStay.remove(0);
		for(int i = 0;i < agentToMove.getPlan().size()-agentToMove.getStepInPlan();i++)
			newPlanAgentToStay.add(0,noOp);
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);
	}
	public static void moveAgentOnAgentWithBox(Agent agentToMove, Agent agentToStay, Box boxToMove){
		if(agentToStay.getStepInPlan() == 0)
			addNoOpToAgentToStay(agentToMove,agentToStay);
		else{
			agentToMove.generateInitialState();
			agentToMove.initialState.walls.add(new Position(agentToStay.getPosition()));
			agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
			agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
			if (boxToMove  == null )
				boxToMove = World.getInstance().getBoxes().get(agentToMove.getIntention().getBox().getId());

			agentToMove.initialState.boxes.put(boxToMove.getId(), boxToMove);
			/*testing*/
			for(Box boxOfSameColor : World.getInstance().getBoxes().values()){
				if(boxOfSameColor.getColor() == boxToMove.getColor())
					agentToMove.initialState.boxes.put(boxOfSameColor.getId(), boxOfSameColor);
			}
			Strategy strategy = new StrategyBFS();
			Search s = new Search();

			List<Node> newPlanAgentToStay = Conflict.updatePlan(agentToStay);
			s.setPlanForAgentToStay(newPlanAgentToStay);
			LinkedList<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_OWN_BOX);
			agentToMove.initialState.walls.remove(agentToStay.getPosition());
			/*testing*/
			for(Box boxOfSameColor : World.getInstance().getBoxes().values()){
				if(boxOfSameColor.getColor() == boxToMove.getColor())
					agentToMove.initialState.boxes.remove(boxOfSameColor.getId());
			}
			/*if the new plan was null it means that it wasnt possible to find a route away from agentToStay.*/
			/*we replan agentToStay*/
			boolean putAgentInACorner = false;
			if(newPlanAgentToMove == null){
				strategy = new StrategyBFS();
				s = new Search();
				agentToStay.generateInitialState();
				agentToStay.initialState.walls.add(new Position(agentToMove.getPosition()));
				agentToStay.initialState.agentRow = agentToStay.getPosition().getX();
				agentToStay.initialState.agentCol = agentToStay.getPosition().getY();
				newPlanAgentToMove = (LinkedList<Node>) Conflict.updatePlan(agentToMove);
				s.setPlanForAgentToStay(newPlanAgentToMove);
				newPlanAgentToStay = s.search(strategy, agentToStay.initialState, SearchType.MOVE_AWAY);
				agentToStay.initialState.walls.remove(agentToMove.getPosition());

				if(newPlanAgentToStay == null){
					putAgentInACorner = putAgentInACorner(agentToStay, agentToMove,newPlanAgentToMove,boxToMove);
				}else{
					agentToStay.setPlan(newPlanAgentToStay);
					agentToStay.setStepInPlan(0);
					World.getInstance().getBeliefs().add(agentToStay.getIntention().getDesire().getBelief());

				}
			}else{
				/*we add one noOp to the newPlanAgentToStay*/
				//		Node noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(newPlanAgentToStay.size()-1));
				Node noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(0));		
				if(agentToStay.getId() < agentToMove.getId())
					newPlanAgentToStay.remove(0);
				newPlanAgentToStay.add(0,noOp);
				newPlanAgentToStay.add(0,noOp);
				
				newPlanAgentToMove = insertNoOps(newPlanAgentToMove,agentToMove);
				agentToStay.setPlan(newPlanAgentToStay);
				agentToStay.setStepInPlan(0);
			}
			World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
			if(!putAgentInACorner){
				agentToMove.setPlan(newPlanAgentToMove);
				agentToMove.setStepInPlan(0);
			}
		}
	}

	public static boolean putAgentInACorner(Agent agentToStay, Agent agentToMove, LinkedList<Node> newPlanAgentToMove, Box boxToMove){
		/*we want to put him in a corner (make use of free fields calculation) until agent*/
		/*at this point we know it was not possible to replan either of the agents, therefore
		 * we need to put agentToStay in a corner, and then replan agent to move*/
		int closetsCorner = Integer.MAX_VALUE;
		Position position = null;
		for(FreeSpace freespace : World.getInstance().getFreeSpace().values()){
			if(freespace.isSurroundedByTreeWalls()){
				int currDistance = Utils.manhattenDistance(agentToStay.getPosition(), freespace.getPosition());
				if(currDistance < closetsCorner){
					for(Box box: World.getInstance().getBoxes().values()){
						if(!freespace.getPosition().equals(box.getPosition()))
							position = freespace.getPosition();
					}
				}
			}
		}
		agentToStay.initialState.moveToPositionCol = position.getY();
		agentToStay.initialState.moveToPositionRow = position.getX();
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		LinkedList<Node> newPlanAgentToStay = s.search(strategy, agentToStay.initialState, SearchType.MOVE_TO_POSITION);

		Node noOp = null;
		if(newPlanAgentToStay.isEmpty() && agentToStay.getStepInPlan() == 0){
			noOp = createNoOpNode(agentToStay,agentToStay.initialState); 
		}else
			noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(newPlanAgentToStay.size()-1));		
		for(int i = 0;i < newPlanAgentToMove.size();i++){
			newPlanAgentToStay.add(noOp);
		}
		World.getInstance().getBeliefs().add(agentToStay.getIntention().getDesire().getBelief());

		/*we replan for agentToMove*/
		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(position);
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
		agentToMove.initialState.boxes.put(boxToMove.getId(), boxToMove);
		Goal intentionGoal = agentToMove.getIntention().getDesire().getBelief().getGoal();
		agentToMove.initialState.goals.put(intentionGoal.getId(), intentionGoal);
		strategy = new StrategyBFS();
		s = new Search();
		newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.PATH);
		agentToMove.initialState.walls.remove(position);

		if(newPlanAgentToMove.isEmpty()){
			noOp = createNoOpNode(agentToMove,agentToMove.initialState); 
			newPlanAgentToMove.add(noOp);
		}

		agentToMove.setPlan(newPlanAgentToMove);
		agentToMove.setStepInPlan(0);
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);
		return true;
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
