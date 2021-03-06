package conflicts;

import java.util.LinkedList;
import java.util.List;

import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import bdi.Belief;
import searchclient.Command;
import searchclient.Node;
import searchclient.Search;
import searchclient.Search.SearchType;
import strategies.Strategy;
import strategies.StrategyBFS;
import utils.Utils;

public class MABoxOnBoxConflict {

	public static void AgentBoxBoxConflict(Agent agentToStay, Box agentToStayBox, Agent agentToMove,Box agentToMoveBox) {

		/*First we try to replan for agentToStay*/
		agentToStay.generateInitialState();

		/*we add the conflict box*/
		agentToStay.initialState.walls.add(agentToMoveBox.getPosition());

		/*trying to add the boxes that belongs to the agent*/
		for(Box box : World.getInstance().getBoxes().values()){
			if(box.getColor().equals(agentToStayBox.getColor()) && !box.equals(agentToStayBox)){
				agentToStay.initialState.walls.add(new Position(box.getPosition()));
				//				agentToStay.initialState.boxes.put(box.getId(),box);
			}
		}
		agentToStay.initialState.agentRow = agentToStay.getPosition().getX();
		agentToStay.initialState.agentCol = agentToStay.getPosition().getY();
		agentToStay.initialState.boxes.put(agentToStayBox.getId(), agentToStayBox);

		Goal intentionGoal = agentToStay.getIntention().getDesire().getBelief().getGoal();
		agentToStay.initialState.goals.put(intentionGoal.getId(), intentionGoal);
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		LinkedList<Node> newPlanAgentToStay = s.search(strategy, agentToStay.initialState, SearchType.PATH);

		/*we remember to remove all walls we have inputed*/
		agentToStay.initialState.walls.remove(new Position(agentToMoveBox.getPosition()));
		for(Box box : World.getInstance().getBoxes().values()){
			if(box.getColor().equals(agentToStayBox.getColor()) && !box.equals(agentToStayBox))
				agentToStay.initialState.walls.remove(new Position(box.getPosition()));
		}
		/*if the newplan was null, we try to add the boxes of the agent instead*/
		if(newPlanAgentToStay == null && agentToMoveBox.getColor().equals(agentToStay.getColor())){
			for(Box box : World.getInstance().getBoxes().values()){
				if(box.getColor().equals(agentToStayBox.getColor()) || box.equals(agentToStayBox)){
					agentToStay.initialState.boxes.put(box.getId(),box);
				}else{
					agentToStay.initialState.walls.add(box.getPosition());
				}
			}
			strategy = new StrategyBFS();
			s = new Search();
			newPlanAgentToStay = s.search(strategy, agentToStay.initialState, SearchType.PATH);

			for(Box box : World.getInstance().getBoxes().values()){
				if(box.getColor().equals(agentToStayBox.getColor()) || box.equals(agentToStayBox)){
					agentToStay.initialState.boxes.remove(box.getId());
				}else{
					agentToStay.initialState.walls.remove(box.getPosition());
				}
			}
		}

		if(newPlanAgentToStay == null){
			if (agentToMoveBox.getColor().equals(agentToStayBox.getColor())){
				moveAgentBoxAndConflictBox(agentToStay,agentToStayBox,agentToMove,agentToMoveBox);
			}else{
				getAnotherAgentToMoveConflictBox(agentToStay,agentToStayBox,agentToMove,agentToMoveBox);
			}
		}else{
			/*we just want the agent to run the new plan*/
			/*update beliefs with his intention*/
			agentToStay.setPlan(newPlanAgentToStay);
			agentToStay.setStepInPlan(0);
		}
	}

	public static void moveAgentBoxAndConflictBox(Agent agentToStay, Box agentToStayBox,Agent agentToMove, Box agentToMoveBox){
		/*first we try to replan, if that is not possible, we will make the agent move the box himself*/
		/*TODO this check below is not nessesary - but code errors*/
		if(replanAgentToStay(agentToStay,agentToStayBox,agentToMoveBox)){
		}else{
			/*if it is not possible to replan, we have to move conflict box away first*/
			replanAgentToStayWithConflictBox(agentToStay,agentToStayBox,agentToMoveBox);
		}
	}

	public static void getAnotherAgentToMoveConflictBox(Agent agentToStay, Box agentToStayBox, Agent agentToMove,Box agentToMoveBox){
		/*find closets agent that can move the box*/
		int bestDistance = Integer.MAX_VALUE;
		if (agentToMove == null){
			for(Agent agent : World.getInstance().getAgents().values()){
				if(agent.getColor().equals(agentToMoveBox.getColor())){
					int currDistance = Utils.manhattenDistance(agent.getPosition(), agentToMoveBox.getPosition());
					if(currDistance < bestDistance){
						bestDistance = currDistance;
						agentToMove = agent;
					}
				}
			}
		}
		/*create the plan for the agentToMove the box*/
		agentToMove.generateInitialState();
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
		agentToMove.initialState.boxes.put(agentToMoveBox.getId(), agentToMoveBox);
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		s.setPlanForAgentToStay(Conflict.updatePlan(agentToStay));
		LinkedList<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_OWN_BOX);

		/*create a new plan for the agent to stay where it removes its own box away*/
		agentToStay.generateInitialState();
		agentToStay.initialState.agentRow = agentToStay.getPosition().getX();
		agentToStay.initialState.agentCol = agentToStay.getPosition().getY();
		agentToStay.initialState.boxes.put(agentToStayBox.getId(), agentToStayBox);
		strategy = new StrategyBFS();
		s = new Search();
		s.setPlanForAgentToStay(newPlanAgentToMove);
		LinkedList<Node> newPlanAgentToStay = s.search(strategy, agentToStay.initialState, SearchType.MOVE_OWN_BOX);
		
		if(newPlanAgentToMove != null){
			Node noOp = null;
			if(newPlanAgentToStay.isEmpty()){
				newPlanAgentToStay = (LinkedList<Node>) Conflict.updatePlan(agentToStay);
				if(agentToStay.getStepInPlan() == 0){
					newPlanAgentToStay.add(0,agentToStay.initialState);
					noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(0));
					newPlanAgentToStay.remove(0);
				}else
					noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(0));
				for(int i = 0; i<newPlanAgentToMove.size();i++){
					newPlanAgentToStay.add(0,noOp);
				}
			}else{
				noOp = createNoOpNode(agentToStay,newPlanAgentToStay.getLast());
				for(int i = 0;i<newPlanAgentToMove.size();i++){
					newPlanAgentToStay.add(newPlanAgentToStay.size(),noOp);
				}
			}
			agentToMove.setPlan(newPlanAgentToMove);
			agentToMove.setStepInPlan(0);
			agentToStay.setPlan(newPlanAgentToStay);
			agentToStay.setStepInPlan(0);
			
			if (agentToStay.getIntention() != null){
				World.getInstance().getBeliefs().add(agentToStay.getIntention().getDesire().getBelief());
			}
			if (agentToMove.getIntention() != null){
				World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
			}
		}

	}


	public static void updatePlansSenderCanReplan(Agent agentToStay, Agent agentToMove,LinkedList<Node> newPlanAgentToStay){
		/*update agent to move plan such that it waits for agentToStay to move away*/
		List<Node> newPlanAgentToMove = agentToMove.getPlan();
		int agentToMoveCurrIndex = agentToMove.getStepInPlan();
		for (int i = 0; i < agentToMoveCurrIndex - 1; i++) {
			if (newPlanAgentToMove.size() == 0)
				break;
			newPlanAgentToMove.remove(0);
		}

		Node noOp = createNoOpNode(agentToMove,newPlanAgentToMove.get(0));
		noOp.action = new Command();
		agentToMoveCurrIndex = newPlanAgentToMove.size();
		for(int j = 0; j<newPlanAgentToStay.size();j++){
			newPlanAgentToMove.add(0, noOp);
		}
		/*update agent to stay to wait until agent to move */
		noOp = createNoOpNode(agentToStay,newPlanAgentToStay.getLast());
		noOp.action = new Command();
		int numberOfStepsFromAgentToMoveCurrIndex = agentToMove.getPlan().size()-agentToMove.getStepInPlan();
		for(int j = 0; j<numberOfStepsFromAgentToMoveCurrIndex;j++){
			newPlanAgentToStay.add(newPlanAgentToStay.size(), noOp);
		}

		/*update beliefs with his intention*/
		agentToMove.setPlan(newPlanAgentToMove);
		agentToMove.setStepInPlan(0);
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);

		if (agentToStay.getIntention() != null){
			World.getInstance().getBeliefs().add(agentToStay.getIntention().getDesire().getBelief());
		}
	}

	public static boolean replanAgentToStayWithConflictBox(Agent agentToStay,Box agentToStayBox,Box agentToMoveBox){
		/*check if agentToStay is in the way of agent to move */
		agentToStay.generateInitialState();
		agentToStay.initialState.agentRow = agentToStay.getPosition().getX();
		agentToStay.initialState.agentCol = agentToStay.getPosition().getY();
		agentToStay.initialState.boxes.put(agentToStayBox.getId(), agentToStayBox);
		agentToStay.initialState.boxes.put(agentToMoveBox.getId(), agentToMoveBox);
		Goal agentToStayGoal = agentToStay.getIntention().getDesire().getBelief().getGoal();
		agentToStay.initialState.goals.put(agentToStayGoal.getId(), agentToStayGoal);
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		LinkedList<Node> newPlanAgentToStay = s.search(strategy, agentToStay.initialState, SearchType.PATH);

		/*check if the agentToMoveBox was in a goal, if yes we need to add it to beliefs again*/
		for(Goal goal : World.getInstance().getGoals().values()){
			if(goal.getPosition().equals(agentToMoveBox.getPosition()) && 
					goal.getLetter() == Character.toLowerCase(agentToMoveBox.getLetter())){
				/*we have removed a box from its goal, we add the belief to the world again*/
				Belief belief = new Belief(goal);
				World.getInstance().getBeliefs().add(belief);
			}
		}
		if(newPlanAgentToStay == null)
			return false;
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);
		return true;

	}
	public static void replanAgentToMove(Agent agentToStay,Box agentToStayBox,Agent agentToMove,Box agentToMoveBox){
		/* we replan for agentToMove (with his box)*/
		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(agentToStay.getPosition());
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
		agentToMove.initialState.boxes.put(agentToMoveBox.getId(), agentToMoveBox);

		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		s.setPlanForAgentToStay(Conflict.updatePlan(agentToStay));

		LinkedList<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_OWN_BOX);

		agentToMove.initialState.walls.remove(agentToStay.getPosition());

		List<Node> newPlanAgentToStay = Conflict.updatePlan(agentToStay);
		/*
		 * If the agent to move is only moving the box 1 step, then we assume
		 * the other agent want to pass (which takes at least 2 steps). If we
		 * dont do this check we might end up with dead lock
		 */
		Node noOp = null;
		boolean planHasChanged = false;
		if(newPlanAgentToMove != null && !newPlanAgentToMove.isEmpty()){
			/* For the agent to stay we add a noop according to the agentToMovePlan */
			/* We add noOps acc. to how many steps the new plan is */
			if(agentToStay.getId() < agentToMove.getId())
				noOp = createNoOpNode(agentToStay,agentToStay.initialState);
			else{
				noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(0));
				/*if the agentToStay id is smaller than the agent to move, we need to remove the node first
				 * (else plan will be out of order)*/
				if(agentToStay.getId() < agentToMove.getId())
					newPlanAgentToStay.remove(0);
			}
			noOp.action = new Command();
			for(int j = 0; j<newPlanAgentToMove.size();j++){
				newPlanAgentToStay.add(0, noOp);
			}
			/*then a add the same number of no ops in the end of agentToMove path*/
			int newPlanAgentToMoveSize = newPlanAgentToMove.size();
			if (newPlanAgentToMoveSize < 2)
				newPlanAgentToMoveSize = 4;
			noOp = createNoOpNode(agentToMove,newPlanAgentToMove.peekLast());
			for (int i = 0; i < newPlanAgentToMoveSize; i++) {
				newPlanAgentToMove.add(newPlanAgentToMove.size(), noOp);
			}
			planHasChanged = true;
		}else{
			/*we just want to use his current plan, but insert a noOp in the begginning of it*/
			newPlanAgentToMove = (LinkedList<Node>) Conflict.updatePlan(agentToMove);
			noOp = createNoOpNode(agentToMove,newPlanAgentToMove.get(0));
			if(agentToStay.getId() < agentToMove.getId()){
				newPlanAgentToMove.remove(0);
				newPlanAgentToStay.remove(0);
			}
			/*we add three noOps as this is the maximum it takes for an agent to move out of the way with box*/
			newPlanAgentToMove.add(0,noOp);
			newPlanAgentToMove.add(0,noOp);
			newPlanAgentToMove.add(0,noOp);
		}


		agentToMove.setPlan(newPlanAgentToMove);
		agentToMove.setStepInPlan(0);
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);
		if (agentToMove.getIntention() != null && planHasChanged){
			World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
		}else {
			/* we check if the box is in a goal that matches his */
			for (Goal goal : World.getInstance().getGoals().values()) {
				if (goal.getPosition().equals(agentToMoveBox.getPosition())
						&& goal.getLetter() == Character.toLowerCase(agentToMoveBox.getLetter())) {
					/* we create a new intention and give it to the agent */
					Belief belief = new Belief(goal);
					World.getInstance().getBeliefs().add(belief);
				}
			}
			/* if not then we just move the box and don't do anything else */
		}		

	}

	public static void moveBoxAwayFromGoal(Agent agentToMove, Box agentToMoveBox, Agent agentToStay, Box agentToStayBox){
		/*first we generate a plan for the agentToMove - to move his box away*/
		/*here we compare with the agentToStay current plan*/

		agentToMove.generateInitialState();
		agentToMove.initialState.boxes.put(agentToMoveBox.getId(), agentToMoveBox);
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		s.setPlanForAgentToStay(agentToStay.getPlan());
		LinkedList<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_OWN_BOX);

		/*then we move the agentToStay out of the way for the agentToMove*/
		agentToStay.generateInitialState();
		agentToStay.initialState.boxes.put(agentToStayBox.getId(),agentToStayBox);
		agentToStay.initialState.agentRow = agentToStay.getPosition().getX();
		agentToStay.initialState.agentCol = agentToStay.getPosition().getY();
		strategy = new StrategyBFS();
		s = new Search();
		s.setPlanForAgentToStay(newPlanAgentToMove);
		LinkedList<Node> newPlanAgentToStay = s.search(strategy, agentToStay.initialState, SearchType.MOVE_OWN_BOX);

		/*add noOps such that agentToStay can move out of agentToMoves way*/
		Node noOp = createNoOpNode(agentToMove,agentToMove.initialState);
		for(int i = 0; i < newPlanAgentToStay.size();i++){
			newPlanAgentToMove.add(0,noOp);
		}
		noOp = createNoOpNode(agentToStay,newPlanAgentToStay.getLast());
		for(int j = 0; j < newPlanAgentToMove.size();j++){
			newPlanAgentToStay.add(newPlanAgentToStay.size(),noOp);
		}

		agentToMove.setPlan(newPlanAgentToMove);
		agentToMove.setStepInPlan(0);
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);

		Goal goal = null;
		for(Goal g : World.getInstance().getGoals().values()){
			if(g.getPosition().equals(agentToMoveBox.getPosition()))
				goal = g;
		}
		Belief belief = new Belief(goal);
		World.getInstance().getBeliefs().add(belief);
		World.getInstance().getBeliefs().add(agentToStay.getIntention().getDesire().getBelief());
	}
	public static boolean replanAgentToStay(Agent agentToStay,Box agentToStayBox,Box agentToMoveBox){
		/*check if agentToStay is in the way of agent to move */
		agentToStay.generateInitialState();
		agentToStay.initialState.walls.add(new Position(agentToMoveBox.getPosition()));
		agentToStay.initialState.agentRow = agentToStay.getPosition().getX();
		agentToStay.initialState.agentCol = agentToStay.getPosition().getY();
		agentToStay.initialState.boxes.put(agentToStayBox.getId(), agentToStayBox);
		Goal agentToStayGoal = agentToStay.getIntention().getDesire().getBelief().getGoal();
		agentToStay.initialState.goals.put(agentToStayGoal.getId(), agentToStayGoal);
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		LinkedList<Node> newPlanAgentToStay = s.search(strategy, agentToStay.initialState, SearchType.PATH);

		agentToStay.initialState.walls.remove(agentToMoveBox.getPosition());

		if(newPlanAgentToStay == null)
			return false;
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);
		return true;
	}

	public static void updatePlansSenderCannotReplan(){}
	public static void AgentWithBoxOnAgentWithBoxConflict(Agent agentToMove, Box agentToMoveBox, Agent agentToStay, Box agentToStayBox) {
		/*if the box to move is not on a goal try to replan */
		if(agentToMoveBox.getPosition().equals(agentToMove.getIntention().getDesire().getBelief().getGoal().getPosition())){
			/*check if agentToStay is in the way of agent to move */
			if(!replanAgentToStay(agentToStay,agentToStayBox,agentToMoveBox)){
				/*it was not possible to replan - we move other agent */
				replanAgentToMove(agentToStay,agentToStayBox,agentToMove,agentToMoveBox);
			}
		}else{
			replanAgentToMove(agentToStay,agentToStayBox,agentToMove,agentToMoveBox);
		}
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
