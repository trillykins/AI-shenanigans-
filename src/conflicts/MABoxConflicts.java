package conflicts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import searchclient.Command;
import searchclient.Node;
import searchclient.Search;
import strategies.Strategy;
import strategies.StrategyBFS;
import analysis.FreeSpace;
import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import bdi.Belief;
import bdi.Desire;
import bdi.Intention;

public class MABoxConflicts {
	
	public void solveMAgentBoxConflict(Conflict conflict) {
		
		Box conflictBox = conflict.getReceiverBox();
		Agent sender = conflict.getSender();
		Agent receiver = conflict.getReceiver();
		Node node = conflict.getNode();
		//Check is it same color of sender agent
		if(sender.getColor().equals(conflictBox.getColor()) && sender.getIntention().getBox().getLetter() == conflictBox.getLetter()) {
//			List<Node> newPlan = findNewSolution(node,sender,receiver.getPosition());
//			if(newPlan != null && newPlan.size() >0) {
//				//World.getInstance().getSolutionMap().put(sender.getId(), newPlan);
//				sender.setPlan(newPlan);
//				sender.initialState.walls.remove(conflictBox.getPosition());
//				
//				updateOthersSolutions(sender,sender.getStepInPlan());
//			}else {
//				moveReceiverAgentAway(receiver,sender, sender.getStepInPlan(),conflictBox);
//			}
			moveReceiverAgentAway(receiver,sender, sender.getStepInPlan(),conflictBox);
		}else {
			List<Node> newPlan = findNewSolution(node,sender,conflictBox.getPosition());
			if(newPlan != null && newPlan.size() >0) {
				sender.setPlan(newPlan);
				sender.setStepInPlan(0);
				//World.getInstance().getSolutionMap().put(sender.getId(), newPlan);
				sender.initialState.walls.remove(conflictBox.getPosition());
				
				updateOthersSolutions(sender);
			}else {
				sender.initialState.walls.remove(conflictBox.getPosition());
				if(receiver != null) {
					Intention intention = receiver.getIntention();
					if(intention != null) {
						Box intBox = intention.getBox();
						if(conflictBox.equals(intBox)) {//The intention is the same box
							moveIntentionBox(conflictBox,sender,receiver);
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
				//Agent removeBoxAg = findAgentToMoveBox(sender,conflictBox,index);
			}
		}
	}
	
	private void moveReceiverAgentAway(Agent agent,Agent agentToStay,int index,Box moveBox) {
		Map<Position,FreeSpace> fresSp = World.getInstance().getFreeSpace();
		Position moveToPosition = findPossiblePosition(fresSp,agent,index);
		if(moveToPosition != null) {
			Strategy strategy = new StrategyBFS();
			Search sear = new Search();
			
			agent.generateInitialState();
			agent.initialState.agentCol = agent.getPosition().getY();
			agent.initialState.agentRow = agent.getPosition().getX();
			
			agent.initialState.moveToPositionCol = moveToPosition.getY();
			agent.initialState.moveToPositionRow = moveToPosition.getX();
		
			List<Node> newPlan = sear.search(strategy, agent.initialState, Search.SearchType.MOVE_TO_POSITION);
			//World.getInstance().getSolutionMap().put(agent.getId(), newPlan);
			agent.setPlan(newPlan);
			agent.setStepInPlan(0);
			updateOthersSolutions(agent);
		}
	}
	
	private void updateOthersSolutions(Agent agent) {
		int solutionSize = agent.getPlan().size();
		for(Agent otherAgent: World.getInstance().getAgents().values()) {
			if(otherAgent.getId() != agent.getId()) {
				List<Node> otherSolution = otherAgent.getPlan();
				if(otherSolution != null && otherSolution.size() > otherAgent.getStepInPlan()) {
					for (int i=0;i<otherAgent.getStepInPlan()-1; i++) {
						otherSolution.remove(0);
					}
					if(!(otherSolution.size() == 1 
							&& otherSolution.get(0).action.actType.equals(Command.type.NoOp))) {
						Node parent = createNoOpNode(otherAgent,null);
						otherSolution.get(0).parent = parent;
						otherSolution.add(0,parent);
					}
					otherAgent.setPlan(otherSolution);
					otherAgent.setStepInPlan(0);
				}
			}
		}
	}
	
	private void moveIntentionBox(Box box, Agent oriAgent,Agent removeBoxAg) {
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
		World world = World.getInstance();
		Map<Position,FreeSpace> fresSp = world.getFreeSpace();
		Map<Position,FreeSpace> copyOfFreespace = new HashMap<Position,FreeSpace>(fresSp);
		Position posi = findPossiblePosition(copyOfFreespace,ag,ag.getStepInPlan());
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

		List<Node> newPlanForMovingBox = generateNewPlanForMovingBox(ag,posi,box,oriAgentPlan);
		/*Add noOps (can be optimized)*/
		int newPlanForMovingBoxIndex = newPlanForMovingBox.size();
		if (newPlanForMovingBoxIndex < 2)
			newPlanForMovingBoxIndex = 4;
		for(int i = 0; i < newPlanForMovingBoxIndex; i++){
			Node lastNode = newPlanForMovingBox.get(newPlanForMovingBoxIndex-1);
			newPlanForMovingBox.add(createNoOpNode(ag,lastNode));
		}
		ag.setPlan(newPlanForMovingBox);
		ag.setStepInPlan(0);
		
		oriAgent.setPlan(refreshOriPlan);
		oriAgent.setStepInPlan(0);
		
	}
	
	private List<Node> generateNewPlanForMovingBox(Agent agent,Position moveToPosition,Box moveBox,List<Node> agentToStayPlan) {
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		
		int goalId = agent.initialState.goals.size()+1;
		Goal newGoal = new Goal(goalId,
				moveToPosition,Character.toLowerCase(moveBox.getLetter()),null,0);	
		agent.generateInitialState();
		agent.initialState.agentCol = agent.getPosition().getY();
		agent.initialState.agentRow = agent.getPosition().getX();
		agent.initialState.goals.put(goalId, newGoal);
		agent.initialState.boxes.put(moveBox.getId(),moveBox);
		
		/*wee need to set the other agents plan in order to compare in search*/
		s.setPlanForAgentToStay(agentToStayPlan);
	
		/*we call move-own-box : it compares with the other agents path and moves both agent and box :) */
		List<Node> newPlan = s.search(strategy, agent.initialState, Search.SearchType.MOVE_OWN_BOX);
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

	private List<Node> findNewSolution(Node currentNode,Agent agent,Position posi) {
		Strategy strategy = new StrategyBFS();
		Search sear = new Search();
	
		//Add a wall to box position
		agent.initialState.walls.add(posi);
		List<Node> newPlan = new LinkedList<Node>();
		if(!currentNode.action.actType.equals(Command.type.Move)) {
			/**
			 * If the agent is carrying a box, then should update the box position then replan
			 */
			agent.initialState.agentCol = currentNode.parent.agentCol;
			agent.initialState.agentRow = currentNode.parent.agentRow;
			agent.initialState.boxes = currentNode.parent.boxes;
		}else {
			agent.initialState.agentCol = agent.getPosition().getY();
			agent.initialState.agentRow = agent.getPosition().getX();
		}
		newPlan = sear.search(strategy, agent.initialState, Search.SearchType.PATH);
		return newPlan;
	}

}
