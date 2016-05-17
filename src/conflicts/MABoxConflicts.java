package conflicts;

import java.util.LinkedList;
import java.util.List;

import searchclient.Command;
import searchclient.Node;
import searchclient.Search;
import strategies.Strategy;
import strategies.StrategyBFS;
import utils.Utils;
import analysis.FreeSpace;
import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import bdi.Intention;

public class MABoxConflicts {
	
	public void solveMAgentBoxConflict(Conflict conflict) {
		
		Box conflictBox = conflict.getReceiverBox();
		Agent sender = conflict.getSender();
		Agent receiver = conflict.getReceiver();
		Node node = conflict.getNode();
		//Check is it same color of sender agent
		if(sender.getColor().equals(conflictBox.getColor()) && sender.getIntention().getBox().getId() == conflictBox.getId()) {
			//If the conflict box is the one sender is pushing
			if(sender.getId() == receiver.getId()) {
				moveSenderIntentionBox(conflictBox,sender);
			}else {
				if(!checkCouldSolveWithoutReplan(node,sender, receiver)) {
					moveReceiverAgentAway(receiver,sender,conflictBox);
				}else {
					
				}
			}
		}else {
			//If the sender's box or other agent's box
			//Try to find a new plan for sender
			if(receiver != null) {
				if(sender.getId() != receiver.getId()) {
					if(receiver != null && checkCouldSolveWithoutReplan(node,sender, receiver)) {
						updateOthersSolutions(sender);
						return;
					}
				}
			}
			if(checkIsIntentionMoving(sender)) {
				List<Node> newPlan = findNewSolutionForSender(node,sender,conflictBox.getPosition(),receiver,conflictBox);
				if(newPlan != null && newPlan.size() >0) {
					sender.setPlan(newPlan);
					sender.setStepInPlan(0);
					sender.initialState.walls.remove(conflictBox.getPosition());
					
					updateOthersSolutions(sender);
				}else {
					//If there is no new solution for sender, then should move the conflict box away.
					sender.initialState.walls.remove(conflictBox.getPosition());
					if(receiver == null || receiver.getId() == sender.getId()) {
						solveOwnBoxConflict(node,sender,conflictBox);
					}else {// ask other agent to move the conflict box;
						RequestOtherAgentToMoveBoxAway(sender,receiver,conflictBox,node);
					}
				}
			}else {
				RequestOtherAgentToMoveBoxAway(sender,receiver,conflictBox,node);
			}
		}
	}
	
	private boolean checkIsIntentionMoving(Agent sender) {
		List<Node> senderPlan = sender.getPlan();
		Node lastStep = senderPlan.get(senderPlan.size()-1);
		
		Intention inten = sender.getIntention();
		if(inten != null) {
			Goal goal = inten.getDesire().getBelief().getGoal();
			Position goalPosi = goal.getPosition();
			
			for(Box box:lastStep.boxes.values()) {
				Position boxPosi = box.getPosition();
				if(boxPosi.equals(goalPosi)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void RequestOtherAgentToMoveBoxAway(Agent sender,Agent receiver,Box conflictBox,Node node) {
		if(receiver != null) {
			Intention intention = receiver.getIntention();
			if(intention != null) {
				Box intBox = intention.getBox();
				if(conflictBox.getId() == intBox.getId() && !conflictBox.isOnGoal()) {//The intention is the same box
					moveReceiverIntentionBox(node,conflictBox,sender,receiver);
					return;
				}
			}
			//Re generate the intention
			if(conflictBox.isOnGoal()) {//If the current box isOnGoal, then remove it and generate the new solution later
				solveBoxOnGoalConflict(node,sender,receiver,conflictBox);
				return;
			}else {
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

//		for(Agent agent: World.getInstance().getAgents().values()) {
//			Intention inten = agent.getIntention();
//			if(inten != null) {
//				Goal goal = inten.getDesire().getBelief().getGoal();
//				oriAgent.initialState.walls.add(goal.getPosition());
//			}
//		}
		
		/*wee need to set the other agents plan in order to compare in search*/
		s.setPlanForAgentToStay(oriAgent.getPlan());
	
		/*we call move-own-box : it compares with the other agents path and moves both agent and box :) */
		List<Node> newPlan = s.search(strategy, oriAgent.initialState, Search.SearchType.MOVE_OWN_BOX);
		oriAgent.initialState.walls.remove(oriAgent.getIntention().getBox().getPosition());
//		for(Agent agent: World.getInstance().getAgents().values()) {
//			Intention inten = agent.getIntention();
//			if(inten != null) {
//				Goal goal = inten.getDesire().getBelief().getGoal();
//				oriAgent.initialState.walls.remove(goal.getPosition());
//			}
//		}
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
		if(box.getPosition().equals(oriGoal.getPosition())) {
			//if sender box on goal, then set other agent's position as wall, and replan 
			Agent oAgent = null;
			for(Agent otherAgent:World.getInstance().getAgents().values()) {
				if(otherAgent.getId() != oriAgent.getId()) {
					oAgent = otherAgent;
					oriAgent.initialState.walls.add(oAgent.getPosition());
					s.setPlanForAgentToStay(otherAgent.getPlan());
				}
			}
			newPlan = s.search(strategy, oriAgent.initialState, Search.SearchType.MOVE_OWN_BOX);
			if(oAgent != null)
				oriAgent.initialState.walls.remove(oAgent.getPosition());
			World.getInstance().getBeliefs().add(oriAgent.getIntention().getDesire().getBelief());
		}else {
			oriAgent.initialState.goals.put(oriGoal.getId(), oriGoal);
			newPlan = s.search(strategy, oriAgent.initialState, Search.SearchType.PATH);
		}
		
		oriAgent.setPlan(newPlan);
		oriAgent.setStepInPlan(0);
	}
	
	private boolean checkCouldSolveWithoutReplan(Node node,Agent sender,Agent receiver) {
		if(receiver != null) {
			List<Node> plan = receiver.getPlan();
			int conflictIndex = -1;
			if(plan != null && plan.size() >1) {
				if(node.action != null && node.action.actType.equals(Command.type.Move)) {//if current sender agent is moving
					int startIndex = 0;
					for(int i=startIndex;i<plan.size();i++) {
						Node otherNode = plan.get(i);
						for(Box box:otherNode.boxes.values()) {//if current sender agent is pushing
							if(node.getAgentPosition().equals(box.getPosition()) && 
									!(otherNode.action.actType.equals(Command.type.NoOp) 
											|| (otherNode.action.actType.equals(Command.type.Move)))) {
										conflictIndex = i;
							}
						}
						
					}
				}else {
					for(Box box:node.boxes.values()) {//if current sender agent is pushing
						int startIndex = 0;
						if(receiver.getStepInPlan() != 0) {
							startIndex = receiver.getStepInPlan()-1;
						}
						if(checkReceiverCouldReplan(sender,receiver)) {
							for(int i=startIndex;i<plan.size();i++) {
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
				}
				int stepInSenderPlan = sender.getStepInPlan();
				if(conflictIndex != -1 && conflictIndex < sender.getPlan().size()) {
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
		}
		return false;
	}
	
	private boolean checkReceiverCouldReplan(Agent sender, Agent receiver) {
		Strategy strategy = new StrategyBFS();
		Search sear = new Search();
		
		sender.generateInitialState();
		sender.initialState.agentCol = sender.getPosition().getY();
		sender.initialState.agentRow = sender.getPosition().getX();

		sender.initialState.walls.add(receiver.getPosition());
		Intention senderInten = sender.getIntention();
		if(senderInten != null) {
			Box box = senderInten.getBox();
			Goal goal = senderInten.getDesire().getBelief().getGoal();
			
			sender.initialState.boxes.put(box.getId(), box);
			sender.initialState.goals.put(goal.getId(), goal);
		}
	    sear.setPlanForAgentToStay(receiver.getPlan());
		List<Node> newPlan = sear.search(strategy, sender.initialState, Search.SearchType.PATH);
		sender.initialState.walls.remove(receiver.getPosition());
		if(newPlan != null) {
			return true;
		}
		return false;
	}
	
	private List<Node> getCurrentLeftPlan(Agent agent) {
		int stepInPlan = agent.getStepInPlan();
		List<Node> plan = agent.getPlan();
		if(stepInPlan != 0) {
			if(plan != null && plan.size()>stepInPlan) {
				for(int i=0; i<stepInPlan;i++) {
					plan.remove(0);
				}
			}else {
				plan = new LinkedList<Node>();
			}
		}
		return plan;
	}
	
	private void moveReceiverAgentAway(Agent agent,Agent agentToStay,Box conflictBox) {
		//Shoulde check whether agentToMove is carrying a box
		List<Node> agentToMovePlan = agent.getPlan();
		
		List<Node> newPlanForMovingBox = null;
		if(agentToMovePlan != null && agentToMovePlan.size()>= agent.getStepInPlan()) {
			Node nextNode = agentToMovePlan.get(agent.getStepInPlan());
			if(nextNode.action.actType.equals(Command.type.Push) ||
					nextNode.action.actType.equals(Command.type.Pull)) {
				Box moveBox = null;
				for(Box box: nextNode.parent.boxes.values()) {
					moveBox = box;
				}
				newPlanForMovingBox = generateNewPlanForReceiverToMoveBox(agent,moveBox,agentToStay,agentToStay.getPlan());
			}else {
				Strategy strategy = new StrategyBFS();
				Search sear = new Search();
				
				agent.generateInitialState();
				agent.initialState.agentCol = agent.getPosition().getY();
				agent.initialState.agentRow = agent.getPosition().getX();
				
				for(Box box:World.getInstance().getBoxes().values()) {
					if(box.getId() != conflictBox.getId()) {
						agent.initialState.walls.add(box.getPosition());
					}
				}

				agent.initialState.walls.add(agentToStay.getPosition());
				
			    sear.setPlanForAgentToStay(agentToStay.getPlan());
			    newPlanForMovingBox = sear.search(strategy, agent.initialState, Search.SearchType.MOVE_AWAY);
				for(Box box:World.getInstance().getBoxes().values()) {
					if(box.getId() != conflictBox.getId()) {
						agent.initialState.walls.remove(box.getPosition());
					}
				}
			}
		}
		agent.initialState.walls.remove(agentToStay.getPosition());
		if(newPlanForMovingBox != null && newPlanForMovingBox.size()>0) {
			Node lastNode = newPlanForMovingBox.get(newPlanForMovingBox.size()-1);
			for(int i=0;i<3;i++) {
				Node noOp = createNoOpNode(agentToStay,lastNode);
				noOp.parent = lastNode;
				newPlanForMovingBox.add(noOp);
			}
			agent.setPlan(newPlanForMovingBox);
			agent.setStepInPlan(0);
			updateOthersSolutions(agent);
		}else {//If current agent can not move anywhere, then sender should move
			Strategy strategy = new StrategyBFS();
			Search sear = new Search();
			//Node currentNode = agentToStay.getPlan().get(agentToStay.getStepInPlan());
			agent.generateInitialState();
			
			int closetsCorner = 10000;
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
			
			agent.initialState.agentCol = agent.getPosition().getY();
			agent.initialState.agentRow = agent.getPosition().getX();

			agent.initialState.moveToPositionCol = position.getY();
			agent.initialState.moveToPositionRow = position.getX();
			
		    sear.setPlanForAgentToStay(agent.getPlan());
		    newPlanForMovingBox = sear.search(strategy, agent.initialState, Search.SearchType.MOVE_TO_POSITION);
		    agent.setPlan(newPlanForMovingBox);
		    agent.setStepInPlan(0);
		    
		    updateOthersSolutions(agent);
		}
		
	}
	
	private void updateOthersSolutions(Agent agent) {
		for(Agent otherAgent: World.getInstance().getAgents().values()) {
			if(otherAgent.getId() != agent.getId()) {
				List<Node> otherSolution = getCurrentLeftPlan(otherAgent);
				if(!(otherSolution.size() == 1 
						&& otherSolution.get(0).action.actType.equals(Command.type.NoOp))) {
					Node parentNode = null;
					if(otherAgent.getStepInPlan() != 0) {
						parentNode = otherSolution.get(0);
					}
					parentNode = createNoOpNode(otherAgent,parentNode);
					
					otherSolution.get(0).parent = parentNode;
					otherSolution.add(0,parentNode);
				}
				otherAgent.setPlan(otherSolution);
				otherAgent.setStepInPlan(0);
			}
		}
	}
	
	private void moveReceiverIntentionBox(Node node,Box box, Agent oriAgent,Agent removeBoxAg) {
		if(checkCouldSolveWithoutReplan(node, oriAgent,removeBoxAg)) {
			List<Node> plan = removeBoxAg.getPlan();
			
			List<Node> oriAgentPlan = oriAgent.getPlan();
			if(plan != null && plan.size()>1) {
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
		}else {//the sender is on the receiver's route, need receiver replan
			Strategy strategy = new StrategyBFS();
			Search sear = new Search();
			
			removeBoxAg.generateInitialState();
			removeBoxAg.initialState.agentCol = removeBoxAg.getPosition().getY();
			removeBoxAg.initialState.agentRow = removeBoxAg.getPosition().getX();

			removeBoxAg.initialState.walls.add(oriAgent.getPosition());
			Intention removerInten = removeBoxAg.getIntention();
			if(removerInten != null) {
				Goal goal = removerInten.getDesire().getBelief().getGoal();
				
				removeBoxAg.initialState.boxes.put(box.getId(), box);
				removeBoxAg.initialState.goals.put(goal.getId(), goal);
			}
		    sear.setPlanForAgentToStay(oriAgent.getPlan());
			List<Node> newPlan = sear.search(strategy, removeBoxAg.initialState, Search.SearchType.MOVE_OWN_BOX);
			removeBoxAg.initialState.walls.remove(oriAgent.getPosition());
			if(newPlan == null) {
				//receiver could not replan, then let sender wait for receiver to move here
				//receiver continue the previous plan
				List<Node> prePlan = removeBoxAg.getPlan();
				if(prePlan != null && prePlan.size() >0) {
					int noOpsize = prePlan.size() - removeBoxAg.getStepInPlan();
					addNoOpToSenderPlan(noOpsize, oriAgent);
				}
			}else {
				removeBoxAg.setPlan(newPlan);
				removeBoxAg.setStepInPlan(0);
				
				Node noOp = createNoOpNode(oriAgent,null);
				oriAgent.getPlan().add(0, noOp);
				oriAgent.setStepInPlan(0);
			}
		}
		
	}
	
	private void addNoOpToSenderPlan(int size, Agent agent) {
		List<Node> agentPlan = getCurrentLeftPlan(agent);
		List<Node> newPlan = new LinkedList<Node>();
		if(agentPlan != null && agentPlan.size() > 0) {
			Node firstNode = agentPlan.get(0);
			for(int i=0;i<size+1;i++) {
				//Remember the current node is the conflict node, it should not be execute
				//so Noop should be created based on the parent node
				Node noOp = createNoOpNode(agent,firstNode.parent);
				newPlan.add(noOp);
			}
			newPlan.addAll(agentPlan);
			agent.setPlan(newPlan);
			agent.setStepInPlan(0);
		}
	}
	
	private void solveBoxOnGoalConflict(Node node,Agent oriAgent,Agent ag,Box box) {
		List<Node> oriAgentPlan = oriAgent.getPlan();
		List<Node> refreshOriPlan = new LinkedList<Node>();
		
		int index = oriAgent.getStepInPlan();
		if(oriAgentPlan.size()>index) {
			boolean isAddNo = false;
			for(int i=index;i<oriAgentPlan.size();i++) {
				Node stepNo=oriAgentPlan.get(i);
				if(index == 0 && !isAddNo) {
					Node parent = createNoOpNode(oriAgent,null);
					refreshOriPlan.add(parent);
					isAddNo = true;
				}else {
					if(i==index) {//Add one NoOp before new Plan
						//Remember current node is not supposed to execute, so add noOp
						//based on parent node
						Node parent = createNoOpNode(oriAgent,stepNo.parent);
						refreshOriPlan.add(parent);
					}
				}
				refreshOriPlan.add(stepNo);
			}
			for(int j=0;j<1;j++) {//Add one noOp after new Plan
				Node lastNode = refreshOriPlan.get(refreshOriPlan.size()-1);
				Node parent = createNoOpNode(oriAgent,lastNode);
				refreshOriPlan.add(parent);
			}
		}
		oriAgent.setPlan(refreshOriPlan);
		oriAgent.setStepInPlan(0);

		List<Node> newPlanForMovingBox = generateNewPlanForReceiverToMoveBox(ag,box,oriAgent,oriAgentPlan);
		if(newPlanForMovingBox != null) {
			/*Add noOps (can be optimized)*/
			int oriAgentPlanSize = refreshOriPlan.size();
			int newPlanForMovingBoxIndex = newPlanForMovingBox.size();
			int newIndex = 0;
//			if (newPlanForMovingBoxIndex < 2) {
//				newIndex = 4;
//			}else {
//				newIndex = newPlanForMovingBoxIndex;
//			}
			int indexSize = oriAgentPlanSize - newPlanForMovingBoxIndex;
			if(indexSize < 2) {
				indexSize = 5;
			}	
//			for(int i = 0; i < indexSize+1; i++){
//				Node lastNode = newPlanForMovingBox.get(newPlanForMovingBoxIndex-1);
//				newPlanForMovingBox.add(createNoOpNode(ag,lastNode));
//				
//			}
			addNoOpToSenderPlan(indexSize+1, oriAgent);
			
			/*
			 * If the box is already on the goal, then add belief again
			 */
			if(ag.getIntention() != null)
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
		
		World.getInstance().getBeliefs().add(agent.getIntention().getDesire().getBelief());
		return newPlan;
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

	private List<Node> findNewSolutionForSender(Node currentNode,Agent agent,Position posi,Agent receiver,Box conflictBox) {
		Strategy strategy = new StrategyBFS();
		Search sear = new Search();
	
		//Add a wall to box position
		agent.generateInitialState();
		agent.initialState.walls.add(posi);
		if(receiver != null && agent.getId() != receiver.getId()) {
			agent.initialState.walls.add(receiver.getPosition());
		}
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
		agent.initialState.walls.remove(posi);
		if(receiver != null && agent.getId() != receiver.getId()) {
			agent.initialState.walls.remove(receiver.getPosition());
		}
		if(agent.getIntention() != null) {
			World.getInstance().getBeliefs().add(agent.getIntention().getDesire().getBelief());
		}
		return newPlan;
	}

}
