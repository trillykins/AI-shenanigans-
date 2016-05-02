package conflicts;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import bdi.Intention;

public class MABoxConflicts {
	
	public void solveMAgentBoxConflict(Box box,Node node,Agent agent, int index, List<List<Node>> allSolutions) {
		/**
		 * First, set the conflict position to wall, and search solution again,
		 * if there is new solutions, then return new solution, and set box back,
		 * else find a agent to move this box away.
		 */
		List<Node> newPlan = findNewSolution(agent,box.getPosition());
		if(newPlan != null && newPlan.size() >0) {
			World.getInstance().getSolutionMap().put(agent.getId(), newPlan);
			agent.initialState.walls.remove(box.getPosition());
		}else {
			//find a agent to move the box away
			/**
			 * After found the agent
			 * 1. if the conflict box is the intention of that agent, then let it move the box directly, and the original agent stay
			 * 2. if it is not the intention, then try to regenerate the intention(would study this later)
			 */
			agent.initialState.walls.remove(box.getPosition());
			Agent removeBoxAg = findAgentToMoveBox(agent,box,index);
			
			Intention intention = removeBoxAg.getIntention();
			if(intention != null) {
				Box intBox = intention.getBox();
				if(box.equals(intBox)) {//The intention is the same box
					moveIntentionBox(box,agent,removeBoxAg,index);
					return;
				}else {
					//Re generate the intention
					if(box.isOnGoal()) {//If the current box isOnGoal, then remove it and generate the new solution later
						solveBoxOnGoalConflict(node,agent,removeBoxAg,box,index);
						return;
					}else {
						//generateThenewIntention
					}
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
//			Node node = plan.get(i);
//			if(canAgentMove(node,oriAgent,box)) {
//				break;
//			}else {
//				
//			}
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
		Map<Position,FreeSpace> fresSp = World.getInstance().getFreeSpace();
		Position posi = findPossiblePosition(fresSp,ag,index);
		
		List<Node> oriAgentPlan = World.getInstance().getSolutionMap().get(oriAgent.getId());
		List<Node> refreshOriPlan = new LinkedList<Node>();
		
		int oriIndex = searchConflictIndex(node,oriAgent);
		for(int i=oriIndex;i<oriAgentPlan.size();i++) {
			Node stepNo=oriAgentPlan.get(i);
			if(i == oriIndex) {
				Node parent = createNoOpNode(oriAgent,null);
				//Node node2 = createNoOpNode(oriAgent,parent);
				stepNo.parent = parent;
				refreshOriPlan.add(parent);
				//refreshOriPlan.add(node2);
			}
			refreshOriPlan.add(stepNo);
		}
		World.getInstance().getSolutionMap().put(oriAgent.getId(), refreshOriPlan);
		
		List<Node> newPlanForMovingBox = generateNewPlanForMovingBox(oriAgent,ag,posi,box);
		
		World.getInstance().getSolutionMap().put(ag.getId(), newPlanForMovingBox);
	}
	
	private int searchConflictIndex(Node node,Agent agent) {
		List<Node> oriAgentPlan = World.getInstance().getSolutionMap().get(agent.getId());
		if(oriAgentPlan != null && oriAgentPlan.size() > 0) {
			for(int i=0;i<oriAgentPlan.size();i++) {
				Node no = oriAgentPlan.get(i);
				if(no.equals(node)) {
					return i;
				}
			}
		}
		return -1;
	}

	private Agent findAgentToMoveBox(Agent agent,Box box,int index) {
		for(Integer agId: World.getInstance().getAgents().keySet()) {
			Agent ag = World.getInstance().getAgents().get(agId);
			if(ag.getId() != agent.getId() && box.getColor().equals(ag.getColor())) {
				return ag;
			}
		}
		return null;
	}
	
	private List<Node> generateNewPlanForMovingBox(Agent oriAgent,Agent agent,Position moveToPosition,Box moveBox) {
		Strategy strategy = new StrategyBFS();
		Search sear = new Search();
	
		agent.initialState.agentCol = agent.getPosition().getY();
		agent.initialState.agentRow = agent.getPosition().getX();
		//Add a wall to box position
		int goalId = agent.initialState.goals.size()+1;
		Goal newGoal = new Goal(goalId,
				moveToPosition,Character.toLowerCase(moveBox.getLetter()),null,0);
		agent.initialState.goals.put(goalId, newGoal);
		agent.initialState.boxes.put(moveBox.getId(),moveBox);
		
		List<Node> newPlan = sear.search(strategy, agent.initialState, Search.SearchType.PATH);
		return newPlan;
	}
	
	private int compareReplanSize(Agent oriAgent,List<Node> plan) {
		List<Node> oriPlan = World.getInstance().getSolutionMap().get(oriAgent.getId());
		int oriPSize = oriPlan.size();
		int newSize = plan.size();
		if(oriPSize >= newSize) {
			return oriPSize - newSize;
		}
		return -1;
	}
	
	/**
	 * Check the possible position for agent to move the box
	 * @param agent
	 * @param box
	 * @param index
	 */	
	private Position findPossiblePosition(Map<Position, FreeSpace> fress,Agent agent,int index) {
		FreeSpace free = getHighestPriorityOfFreeSpace(fress);
		if(free != null) {
			Position posi = free.getPosition();
			if(checkFutureRouteConflict(posi,agent,index)) {
				fress.remove(posi);
				return findPossiblePosition(fress,agent,index);
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
	
	public void generatePlanAgents() {
		for (Agent agent : World.getInstance().getAgents().values()) {
			agent.generateInitialState();
			if (!agent.generateDesires()) {
				continue;
			}
			if (!agent.generateIntention()) {
				continue;
			}
			Intention intention = agent.getIntention();
			Goal goal = intention.getDesire().getBelief().getGoal();
			Box intentionBox = intention.getBox();
			World.getInstance().getBeliefs().remove(intention.getDesire().getBelief());
			agent.initialState.goals.put(goal.getId(), goal);
			agent.initialState.boxes.put(intentionBox.getId(), intentionBox);

			// Add boxes of same color to the initialstate.
//			for (Box box : World.getInstance().getBoxes().values()) {
//				if (box.getColor().equals(agent.getColor())) {
//					agent.initialState.boxes.put(box.getId(), box);
//				}
//			}
		}
	}

}
