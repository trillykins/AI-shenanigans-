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

public class MABoxOnBoxConflict {

	public static void AgentBoxBoxConflict(int index, List<List<Node>> allSolutions, Agent agentToMove,
			Agent agentToStay, Box agentToMoveBox, Box agentToStayBox) {
		World.getInstance().write("We had a agent-with-box on box conflict : system exit");
		System.err.println("We had a agent-with-box on box conflict : system exit");
		System.exit(0);

	}

	public static void AgentWithBoxOnAgentWithBoxConflict(Agent agentToMove, Agent agentToStay, Box boxToMove) {
		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(new Position(agentToStay.getPosition()));
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
		agentToMove.initialState.boxes.put(boxToMove.getId(), boxToMove);
		
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		s.setPlanForAgentToStay(Conflict.updatePlan(agentToStay));
		LinkedList<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_OWN_BOX);
		
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));

		List<Node> newPlanAgentToStay = agentToStay.getPlan();
		int agentToStayCurrIndex = agentToStay.getStepInPlan();
		for (int i = 0; i < agentToStayCurrIndex - 1; i++) {
			if (newPlanAgentToStay.size() == 0)
				break;
			newPlanAgentToStay.remove(0);
		}
		
		/* For the agent to stay we add a noop according to the agentToMovePlan */
		Node noOp = createNoOpNode(agentToStay,newPlanAgentToStay.get(0));
		noOp.action = new Command();
		newPlanAgentToStay.add(0, noOp);
		for(int j = 0; j<newPlanAgentToMove.size();j++){
			newPlanAgentToStay.add(0, noOp);
		}
		
		/* We add noOps acc. to how many steps the new plan is */
		/*
		 * If the agent to move is only moving the box 1 step, then we assume
		 * the other agent want to pass (which takes at least 2 steps). If we
		 * dont do this check we might end up with dead lock
		 */
		int newPlanAgentToMoveSize = newPlanAgentToMove.size();
		if (newPlanAgentToMoveSize < 2)
			newPlanAgentToMoveSize = 4;
		noOp = createNoOpNode(agentToMove,newPlanAgentToMove.peekLast());
		for (int i = 0; i < newPlanAgentToMoveSize; i++) {
			newPlanAgentToMove.add(newPlanAgentToMove.size(), noOp);
		}
		
		agentToMove.setPlan(newPlanAgentToMove);
		agentToMove.setStepInPlan(0);
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);
		if (agentToMove.getIntention() != null){
			World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
		}else {
			/* we check if the box is in a goal that matches his */
			for (Goal goal : World.getInstance().getGoals().values()) {
				if (goal.getPosition().equals(boxToMove.getPosition())
						&& goal.getLetter() == Character.toLowerCase(boxToMove.getLetter())) {
					/* we create a new intention and give it to the agent */
					Belief belief = new Belief(goal);
					World.getInstance().getBeliefs().add(belief);
				}
			}
			/* if not then we just move the box and don't do anything else */
		}
//		System.err.println("newAgentPlan \n" + newPlanAgentToMove);
//		System.err.println("agentToStayPlan \n" + newPlanAgentToStay);
//		System.exit(0);
//		System.err.println("new plan 'with no ops' \n"+newPlanAgentToMove);
		
//		System.exit(0);
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
		return node;
	}
}
