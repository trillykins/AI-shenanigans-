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
	
	public void solveMAgentBoxConflict(Conflict conflict, int index, List<List<Node>> allSolutions) {
		
		Box conflictBox = conflict.getReceiverBox();
		Agent sender = conflict.getSender();
		Agent receiver = conflict.getReceiver();
		Node node = conflict.getNode();
		//Check is it same color of sender agent
		if(sender.getColor().equals(conflictBox.getColor())) {
			moveReceiverAgentAway(receiver,sender, index,conflictBox);
		}else {
			List<Node> newPlan = findNewSolution(sender,conflictBox.getPosition());
			if(newPlan != null && newPlan.size() >0) {
				World.getInstance().getSolutionMap().put(sender.getId(), newPlan);
				sender.initialState.walls.remove(conflictBox.getPosition());
				
				updateOthersSolutions(sender,index);
			}else {
				sender.initialState.walls.remove(conflictBox.getPosition());
				if(receiver != null) {
					Intention intention = receiver.getIntention();
					if(intention != null) {
						Box intBox = intention.getBox();
						if(conflictBox.equals(intBox)) {//The intention is the same box
							moveIntentionBox(conflictBox,sender,receiver,index);
							return;
						}else {
							//Re generate the intention
							if(conflictBox.isOnGoal()) {//If the current box isOnGoal, then remove it and generate the new solution later
								solveBoxOnGoalConflict(node,sender,receiver,conflictBox,index);
								return;
							}else {
							//generateThenewIntention
								//receiver.generateIntention();
								solveBoxOnGoalConflict(node,sender,receiver,conflictBox,index);
								return;
							}
						}
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
			World.getInstance().getSolutionMap().put(agent.getId(), newPlan);
			
			updateOthersSolutions(agent,index);
		}
	}
	
	private void updateOthersSolutions(Agent agent, int index) {
		for(Agent agen: World.getInstance().getAgents().values()) {
			if(agen.getId() != agent.getId()) {
				List<Node> otherSolution = World.getInstance().getSolutionMap().get(agen.getId());
				if(otherSolution != null && otherSolution.size() > index) {
					for (int i=0;i<index-1; i++) {
						otherSolution.remove(0);
					}
					Node parent = createNoOpNode(agent,null);
					otherSolution.get(0).parent = parent;
					otherSolution.add(0,parent);
					World.getInstance().getSolutionMap().put(agen.getId(), otherSolution);
				}
			}
		}
	}
	
	private void moveIntentionBox(Box box, Agent oriAgent,Agent removeBoxAg,int index) {
		List<Node> plan = World.getInstance().getSolutionMap().get(removeBoxAg.getId());
		
		List<Node> oriAgentPlan = World.getInstance().getSolutionMap().get(oriAgent.getId());
		if(plan != null && plan.size()>0) {
			for (int i = 0; i < index - 1; i++) {
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
		
		for(int j=index;j<oriAgentPlan.size();j++) {
			Node nod = oriAgentPlan.get(j);
			if(j==index) {
				nod.parent = newOriAgentPlan.get(newOriAgentPlan.size()-1);
			}
			newOriAgentPlan.add(nod);
		}
		
		World.getInstance().getSolutionMap().put(oriAgent.getId(), newOriAgentPlan);
		World.getInstance().getSolutionMap().put(removeBoxAg.getId(), plan);
	}
	
	private void solveBoxOnGoalConflict(Node node,Agent oriAgent,Agent ag,Box box,int index) {
		World world = World.getInstance();
		Map<Position,FreeSpace> fresSp = world.getFreeSpace();
		Map<Position,FreeSpace> copyOfFreespace = new HashMap<Position,FreeSpace>(fresSp);
		Position posi = findPossiblePosition(copyOfFreespace,ag,index);
		
		List<Node> oriAgentPlan = World.getInstance().getSolutionMap().get(oriAgent.getId());
		List<Node> refreshOriPlan = new LinkedList<Node>();
		
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

		World.getInstance().getSolutionMap().put(oriAgent.getId(), refreshOriPlan);
		
		List<Node> newPlanForMovingBox = generateNewPlanForMovingBox(ag,posi,box);
		
		World.getInstance().getSolutionMap().put(ag.getId(), newPlanForMovingBox);
	}
	
	private List<Node> generateNewPlanForMovingBox(Agent agent,Position moveToPosition,Box moveBox) {
		Strategy strategy = new StrategyBFS();
		Search sear = new Search();
		
		int goalId = agent.initialState.goals.size()+1;
		Goal newGoal = new Goal(goalId,
				moveToPosition,Character.toLowerCase(moveBox.getLetter()),null,0);	
		agent.generateInitialState();
		agent.initialState.agentCol = agent.getPosition().getY();
		agent.initialState.agentRow = agent.getPosition().getX();
		agent.initialState.goals.put(goalId, newGoal);
		agent.initialState.boxes.put(moveBox.getId(),moveBox);
		
	
		List<Node> newPlan = sear.search(strategy, agent.initialState, Search.SearchType.PATH);
//		
//		/**
//		 * After replan, continue the previous plan
//		 */
//		Node lastNode = newPlan.get(newPlan.size()-1);
//		temporaryAgent.initialState.agentCol = lastNode.agentCol;
//		temporaryAgent.initialState.agentRow = lastNode.agentRow;
//		
//		World.getInstance().generatePlan(temporaryAgent);
//		Strategy strategyBFS = new StrategyBFS();
//		Search search = new Search();
//		List<Node> furtherPlan = search.search(strategyBFS, temporaryAgent.initialState, Search.SearchType.PATH);
//		
//		newPlan.addAll(furtherPlan);
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
		List<Node> solutions = World.getInstance().getSolutionMap().get(agent.getId());
		if(solutions != null && solutions.size() >0) {
			for(int i=0;i<solutions.size();i++) {
				Node node = solutions.get(i);
				int col = node.agentCol;
				int row = node.agentRow;
				if(new Position(row,col).equals(posi)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private Node createNoOpNode(Agent agent, Node parent) {
		Node node = new Node(parent,agent.getId());
		node.action = new Command();
		node.agentCol = agent.getPosition().getY();
		node.agentRow = agent.getPosition().getX();
		node.boxes = agent.initialState.boxes;
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

	private List<Node> findNewSolution(Agent agent,Position posi) {
		Strategy strategy = new StrategyBFS();
		Search sear = new Search();
	
		agent.initialState.agentCol = agent.getPosition().getY();
		agent.initialState.agentRow = agent.getPosition().getX();
		//Add a wall to box position
		agent.initialState.walls.add(posi);
		
		List<Node> newPlan = sear.search(strategy, agent.initialState, Search.SearchType.PATH);
		return newPlan;
	}

}
