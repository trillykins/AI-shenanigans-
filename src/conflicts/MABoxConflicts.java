package conflicts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import analysis.FreeSpace;
import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import bdi.Intention;
import searchclient.Command;
import searchclient.Node;
import searchclient.Search;
import strategies.Strategy;
import strategies.StrategyBFS;

public class MABoxConflicts {
	
	public void solveMAgentBoxConflict(Conflict conflict) {
		
		Box conflictBox = conflict.getReceiverBox();
		Agent sender = conflict.getSender();
		Agent receiver = conflict.getReceiver();
		Node node = conflict.getNode();
		//Check is it same color of sender agent
		if(sender.getColor().equals(conflictBox.getColor()) && sender.getIntention().getBox().getId() == conflictBox.getId()) {
			//If the conflict box is the one sender is pushing
			if(sender.equals(receiver)) {
				moveSenderIntentionBox(conflictBox,sender);
			}else {
				if(!checkCouldSolveWithoutReplan(node,sender, receiver)) {
					moveReceiverAgentAway(receiver,sender, sender.getStepInPlan(),conflictBox);
				}
			}
		}else {
			//If the sender's box or other agent's box
			//Try to find a new plan for sender
			if(!sender.equals(receiver)) {
				if(checkCouldSolveWithoutReplan(node,sender, receiver)) {
					updateOthersSolutions(sender);
				}else {
					List<Node> newPlan = findNewSolutionForSender(node,sender,conflictBox.getPosition(),receiver);
					if(newPlan != null && newPlan.size() >0) {
						sender.setPlan(newPlan);
						sender.setStepInPlan(0);
						sender.initialState.walls.remove(conflictBox.getPosition());
						
						updateOthersSolutions(sender);
					}else {
						//If there is no new solution for sender, then should move the conflict box away.
						sender.initialState.walls.remove(conflictBox.getPosition());
						if(receiver.equals(sender)) {
							solveOwnBoxConflict(node,sender,conflictBox);
						}else {// ask other agent to move the conflict box;
							RequestOtherAgentToMoveBoxAway(sender,receiver,conflictBox,node);
						}
					}
				}
			}
		}
	}
	
	private void RequestOtherAgentToMoveBoxAway(Agent sender,Agent receiver,Box conflictBox,Node node) {
		if(receiver != null) {
			Intention intention = receiver.getIntention();
			if(intention != null) {
				Box intBox = intention.getBox();
				if(conflictBox.equals(intBox)) {//The intention is the same box
					moveReceiverIntentionBox(conflictBox,sender,receiver);
					return;
				}
			}
			//Re generate the intention
			if(conflictBox.isOnGoal()) {//If the current box isOnGoal, then remove it and generate the new solution later
				solveBoxOnGoalConflict(node,sender,receiver,conflictBox);
				return;
			}else {
			//generateThenewIntention
				//receiver.generateIntention();
				solveBoxOnGoalConflict(node,sender,receiver,conflictBox);
				return;
			}
		}
	}
	
	private void solveOwnBoxConflict(Node node,Agent oriAgent,Box moveBox) {
		Strategy strategy = new StrategyBFS();
		Search s = new Search();

		oriAgent.generateInitialState();
		oriAgent.initialState.agentCol = oriAgent.getPosition().getY();
		oriAgent.initialState.agentRow = oriAgent.getPosition().getX();
		oriAgent.initialState.boxes.put(moveBox.getId(),moveBox);
		oriAgent.initialState.walls.add(oriAgent.getIntention().getBox().getPosition());

		for(Agent agent: World.getInstance().getAgents().values()) {
			Intention inten = agent.getIntention();
			if(inten != null) {
				Goal goal = inten.getDesire().getBelief().getGoal();
				oriAgent.initialState.walls.add(goal.getPosition());
			}
		}
		
		/*wee need to set the other agents plan in order to compare in search*/
		s.setPlanForAgentToStay(oriAgent.getPlan());
	
		/*we call move-own-box : it compares with the other agents path and moves both agent and box :) */
		List<Node> newPlan = s.search(strategy, oriAgent.initialState, Search.SearchType.MOVE_OWN_BOX);
		oriAgent.initialState.walls.remove(oriAgent.getIntention().getBox().getPosition());
		for(Agent agent: World.getInstance().getAgents().values()) {
			Intention inten = agent.getIntention();
			if(inten != null) {
				Goal goal = inten.getDesire().getBelief().getGoal();
				oriAgent.initialState.walls.remove(goal.getPosition());
			}
		}
		oriAgent.setPlan(newPlan);
		oriAgent.setStepInPlan(0);
	}
	
	private void moveSenderIntentionBox(Box box, Agent oriAgent) {
		Strategy strategy = new StrategyBFS();
		Search s = new Search();

		oriAgent.generateInitialState();
		oriAgent.initialState.agentCol = oriAgent.getPosition().getY();
		oriAgent.initialState.agentRow = oriAgent.getPosition().getX();
		oriAgent.initialState.boxes.put(box.getId(),box);

		Goal oriGoal = oriAgent.getIntention().getDesire().getBelief().getGoal();
		List<Node> newPlan = new LinkedList<Node>();
		if(box.getPosition().equals(oriGoal.getPosition())) {//if sender box on goal, then set other agent's position as wall, and replan 
			for(Agent otherAgent:World.getInstance().getAgents().values()) {
				if(otherAgent.getId() != oriAgent.getId()) {
					oriAgent.initialState.walls.add(otherAgent.getPosition());
					s.setPlanForAgentToStay(otherAgent.getPlan());
				}
			}
			newPlan = s.search(strategy, oriAgent.initialState, Search.SearchType.MOVE_OWN_BOX);
			World.getInstance().getBeliefs().add(oriAgent.getIntention().getDesire().getBelief());
		}else {
			oriAgent.initialState.goals.put(oriGoal.getId(), oriGoal);
			newPlan = s.search(strategy, oriAgent.initialState, Search.SearchType.PATH);
		}
		
		oriAgent.setPlan(newPlan);
		oriAgent.setStepInPlan(0);
	}
	
	private boolean checkCouldSolveWithoutReplan(Node node,Agent sender,Agent receiver) {
		List<Node> plan = receiver.getPlan();
		int conflictIndex = -1;
		if(plan != null && plan.size() >1) {
			if(node.action.actType.equals(Command.type.Move)) {//if current sender agent is moving
				int startIndex = 0;
				if(receiver.getStepInPlan() != 0) {
					startIndex = receiver.getStepInPlan()-1;
				}
				for(int i=startIndex;i<plan.size();i++) {
					Node otherNode = plan.get(i);
					for(Box box:otherNode.boxes.values()) {//if current sender agent is pushing
						if(node.getAgentPosition().equals(box.getPosition()) && 
								!(otherNode.action.actType.equals(Command.type.NoOp) || (otherNode.action.actType.equals(Command.type.Move)))) {
									conflictIndex = i;
						}
					}
					
				}
			}else {
				for(Box box:node.boxes.values()) {//if current sender agent is pushing
					for(int i=receiver.getStepInPlan()-1;i<plan.size();i++) {
						Node otherNode = plan.get(i);
						if(box.getPosition().equals(otherNode.getAgentPosition())) {
							conflictIndex = i;
							break;
						}else if(box.getPosition().equals(receiver.getPosition())) {
							conflictIndex = 0;
							break;
						}
					}
				}
			}
			int stepInSenderPlan = sender.getStepInPlan();
			if(conflictIndex != -1) {
				List<Node> senderPlan = getCurrentLeftPlan(sender);
				List<Node> newSenderPlan = new LinkedList<Node>();
				for(int j=0;j<(conflictIndex-stepInSenderPlan)+1;j++) {
					Node curNode = null;
					if(senderPlan != null && senderPlan.size()>0) {
						curNode = senderPlan.get(0);
					}
					Node noOp = createNoOpNode(sender,curNode.parent);
					newSenderPlan.add(noOp);
				}
				newSenderPlan.addAll(senderPlan);
				sender.setPlan(newSenderPlan);
				sender.setStepInPlan(0);
				return true;
			}
		}
		return false;
	}
	
	private List<Node> getCurrentLeftPlan(Agent agent) {
		int stepInPlan = agent.getStepInPlan();
		List<Node> plan = agent.getPlan();
		if(plan != null && plan.size()>stepInPlan) {
			for(int i=0; i<stepInPlan;i++) {
				plan.remove(0);
			}
		}else {
			plan = new LinkedList<Node>();
		}
		return plan;
	}
	
	private void moveReceiverAgentAway(Agent agent,Agent agentToStay,int index,Box moveBox) {
		Strategy strategy = new StrategyBFS();
		Search sear = new Search();
		
		agent.generateInitialState();
		agent.initialState.agentCol = agent.getPosition().getY();
		agent.initialState.agentRow = agent.getPosition().getX();
		
//		Position goalPosition = null;
//		Intention inten = agent.getIntention();
//		if(inten != null) {
//			Goal goal = inten.getDesire().getBelief().getGoal();
//			goalPosition = goal.getPosition();
//			agent.initialState.walls.add(goalPosition);
//		}
//		
//		Position agentStaygoalPosition = null;
//		Intention agentStayInten = agent.getIntention();
//		if(agentStayInten != null) {
//			Goal agentStayGoal = agentStayInten.getDesire().getBelief().getGoal();
//			agentStaygoalPosition = agentStayGoal.getPosition();
//			agent.initialState.walls.add(agentStaygoalPosition);
//			
//		}
		
		agent.initialState.walls.add(agentToStay.getPosition());
		
	    sear.setPlanForAgentToStay(agentToStay.getPlan());
		List<Node> newPlan = sear.search(strategy, agent.initialState, Search.SearchType.MOVE_AWAY);
		if(newPlan != null) {//assume that it is next its own box
			
		}
		agent.setPlan(newPlan);
		agent.setStepInPlan(0);
//		if(agentStaygoalPosition != null) {
//			agent.initialState.walls.remove(agentStaygoalPosition);
//		}
//		if(goalPosition != null) {
//			agent.initialState.walls.remove(goalPosition);
//		}
		agent.initialState.walls.remove(agentToStay.getPosition());
		updateOthersSolutions(agent);
	}
	
	private void updateOthersSolutions(Agent agent) {
		for(Agent otherAgent: World.getInstance().getAgents().values()) {
			if(otherAgent.getId() != agent.getId()) {
				List<Node> otherSolution = getCurrentLeftPlan(otherAgent);
				if(!(otherSolution.size() == 1 
						&& otherSolution.get(0).action.actType.equals(Command.type.NoOp))) {
					Node curNode = otherSolution.get(0);
					Node parent = createNoOpNode(otherAgent,curNode);
					otherSolution.get(0).parent = parent;
					otherSolution.add(0,parent);
				}
				otherAgent.setPlan(otherSolution);
				otherAgent.setStepInPlan(0);
			}
		}
	}
	
	private void moveReceiverIntentionBox(Box box, Agent oriAgent,Agent removeBoxAg) {
		List<Node> plan = removeBoxAg.getPlan();
		
		List<Node> oriAgentPlan = oriAgent.getPlan();
		if(plan != null && plan.size()>0) {
			for (int i = 0; i < oriAgent.getStepInPlan() - 1; i++) {
				plan.remove(0);
			}
		}
		
		List<Node> newOriAgentPlan = new LinkedList<Node>();
		for(int i = 0; i<plan.size();i++) {
			Node parent = null;
			if(i != 0) {
				int size = newOriAgentPlan.size();
				parent = newOriAgentPlan.get(size-1);
			}
			newOriAgentPlan.add(createNoOpNode(oriAgent,parent));
		}
		
		for(int j=oriAgent.getStepInPlan();j<oriAgentPlan.size();j++) {
			Node nod = oriAgentPlan.get(j);
			if(j==oriAgent.getStepInPlan()) {
				nod.parent = newOriAgentPlan.get(newOriAgentPlan.size()-1);
			}
			newOriAgentPlan.add(nod);
		}
		oriAgent.setPlan(newOriAgentPlan);
		oriAgent.setStepInPlan(0);
		removeBoxAg.setPlan(plan);
		removeBoxAg.setStepInPlan(0);
	}
	
	private void solveBoxOnGoalConflict(Node node,Agent oriAgent,Agent ag,Box box) {
		List<Node> oriAgentPlan = oriAgent.getPlan();
		List<Node> refreshOriPlan = new LinkedList<Node>();
		
		int index = oriAgent.getStepInPlan();
		if(oriAgentPlan.size()>index) {
			for(int i=index;i<oriAgentPlan.size();i++) {
				Node stepNo=oriAgentPlan.get(i);
				if(i == index) {
					Node parent = createNoOpNode(oriAgent,null);
					//Node node2 = createNoOpNode(oriAgent,parent);
					stepNo.parent = parent;
					refreshOriPlan.add(parent);
					//refreshOriPlan.add(node2);
				}
				refreshOriPlan.add(stepNo);
			}
		}
		oriAgent.setPlan(refreshOriPlan);
		oriAgent.setStepInPlan(0);

		List<Node> newPlanForMovingBox = generateNewPlanForReceiverToMoveBox(ag,box,oriAgent,oriAgentPlan);
		if(newPlanForMovingBox != null) {
			/*Add noOps (can be optimized)*/
			int newPlanForMovingBoxIndex = newPlanForMovingBox.size();
			int newIndex = 0;
			if (newPlanForMovingBoxIndex < 2) {
				newIndex = 4;
			}else {
				newIndex = newPlanForMovingBoxIndex;
			}
				
			for(int i = 0; i < newIndex; i++){
				Node lastNode = newPlanForMovingBox.get(newPlanForMovingBoxIndex-1);
				newPlanForMovingBox.add(createNoOpNode(ag,lastNode));
			}
			
			/*
			 * If the box is already on the goal, then add belief again
			 */
			World.getInstance().getBeliefs().add(ag.getIntention().getDesire().getBelief());
			
			ag.setPlan(newPlanForMovingBox);
			ag.setStepInPlan(0);
		}
	}
	
	private List<Node> generateNewPlanForReceiverToMoveBox(Agent agent,Box moveBox,Agent oriAgent,List<Node> agentToStayPlan) {
		Strategy strategy = new StrategyBFS();
		Search s = new Search();

		agent.generateInitialState();
		agent.initialState.agentCol = agent.getPosition().getY();
		agent.initialState.agentRow = agent.getPosition().getX();
		agent.initialState.boxes.put(moveBox.getId(),moveBox);
		agent.initialState.walls.add(oriAgent.getPosition());
		
		
		/*wee need to set the other agents plan in order to compare in search*/
		s.setPlanForAgentToStay(agentToStayPlan);
	
		/*we call move-own-box : it compares with the other agents path and moves both agent and box :) */
		List<Node> newPlan = s.search(strategy, agent.initialState, Search.SearchType.MOVE_OWN_BOX);
		
		agent.initialState.walls.remove(oriAgent.getPosition());
		return newPlan;
	}
	
	/**
	 * Check the possible position for agent to move the box
	 * @param agent
	 * @param box
	 * @param index
	 */	
	private Position findPossiblePosition(Map<Position, FreeSpace> fress,Agent agent,int index) {
		FreeSpace free = getHighestPriorityOfFreeSpace(fress);
		Map<Position,FreeSpace> newMap = new HashMap<Position,FreeSpace>();
		if(free != null) {
			Position posi = free.getPosition();
			if(posi.equals(agent.getPosition())) {
				for(Position newPosi: fress.keySet()) {
					if(!newPosi.equals(posi)) {
						newMap.put(newPosi, fress.get(newPosi));
					}
				}
				return findPossiblePosition(newMap,agent,index);
			}
			
			/*is there a goal*/
			for(Goal goal : World.getInstance().getGoals().values()){
				if(goal.getPosition().getX() == posi.getX() && goal.getPosition().getY() == posi.getY()) {
					for(Position newPosi: fress.keySet()) {
						if(!newPosi.equals(posi)) {
							newMap.put(newPosi, fress.get(newPosi));
						}
					}
					return findPossiblePosition(newMap,agent,index);
				}
			}
			if(checkFutureRouteConflict(posi,agent,index)) {
				for(Position newPosi: fress.keySet()) {
					if(!newPosi.equals(posi)) {
						newMap.put(newPosi, fress.get(newPosi));
					}
				}
				return findPossiblePosition(newMap,agent,index);
			}else {
				return posi;
			}
		}
		return null;
	}
	
	private boolean checkFutureRouteConflict(Position posi,Agent agent,int index) {
		for(Agent agen: World.getInstance().getAgents().values()) {
			if(agen.getId() != agent.getId()) {
				List<Node> solutions = agen.getPlan();
				if(solutions != null && solutions.size() >0) {
					if(solutions.size() == 1 && solutions.get(0).action.actType.equals(Command.type.NoOp)) {
						return false;
					}else {
						for(int i=0;i<solutions.size();i++) {
							Node node = solutions.get(i);
							int col = node.agentCol;
							int row = node.agentRow;
							if(new Position(row,col).equals(posi)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	private Node createNoOpNode(Agent agent, Node parent) {
		Node node = new Node(parent,agent.getId());
		node.action = new Command();
		node.agentCol = agent.getPosition().getY();
		node.agentRow = agent.getPosition().getX();
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
		return node;
	}
	
	private FreeSpace getHighestPriorityOfFreeSpace(Map<Position,FreeSpace> freeSpace) {
		int highpriority = Integer.MAX_VALUE;
		Position highPosi = null;
		for(Position posi: freeSpace.keySet()) {
			FreeSpace free = freeSpace.get(posi);
			int priority = free.getPriority();
			if(priority < highpriority) {
				highpriority = priority;
				highPosi = posi;
			}
		}
		return freeSpace.get(highPosi);
	}

	private List<Node> findNewSolutionForSender(Node currentNode,Agent agent,Position posi,Agent receiver) {
		Strategy strategy = new StrategyBFS();
		Search sear = new Search();
	
		//Add a wall to box position
		agent.generateInitialState();
		agent.initialState.walls.add(posi);
		agent.initialState.walls.add(receiver.getPosition());
		List<Node> newPlan = new LinkedList<Node>();
		if(!currentNode.action.actType.equals(Command.type.Move)) {
			/**
			 * If the agent is carrying a box, then should update the box position then replan
			 */
			agent.initialState.agentCol = currentNode.parent.agentCol;
			agent.initialState.agentRow = currentNode.parent.agentRow;
			agent.initialState.boxes = currentNode.parent.boxes;
			Goal goal = agent.getIntention().getDesire().getBelief().getGoal();
			agent.initialState.goals.put(goal.getId(), goal);
		}else {
			//if the box is moving, and get the previous box and goal, and then replan
			Intention inten = agent.getIntention();
			if(inten != null) {
				agent.initialState.boxes.put(inten.getBox().getId(), inten.getBox());
				Goal goal = inten.getDesire().getBelief().getGoal();
				agent.initialState.goals.put(goal.getId(), goal);
			}
			agent.initialState.agentCol = agent.getPosition().getY();
			agent.initialState.agentRow = agent.getPosition().getX();
		}
		
		newPlan = sear.search(strategy, agent.initialState, Search.SearchType.PATH);
		return newPlan;
	}

}
