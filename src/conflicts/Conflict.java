package conflicts;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

public class Conflict {
	private ConflictType conflictType;
	private Agent sender;
	private Agent receiver;
	private Node node;
	private Box box;
	
	public enum ConflictType {
		AGENT, BOX_BOX, AGENT_BOX,
	}

	public ConflictType getConflictType() {
		return conflictType;
	}

	public void setConflictType(ConflictType conflictType) {
		this.conflictType = conflictType;
	}

	public Agent getSender() {
		return sender;
	}

	public void setSender(Agent sender) {
		this.sender = sender;
	}

	public Agent getReceiver() {
		return receiver;
	}

	public void setReceiver(Agent receiver) {
		this.receiver = receiver;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public Box getBox() {
		return box;
	}

	public void setBox(Box box) {
		this.box = box;
	}

	public void solveAgentOnAgent(Node node, Agent a1, Agent a2, int index, List<List<Node>> allSolutions) {
		Agent agentToMove = a1.getPriority() > a2.getPriority() ? a2 : a1;
		Agent agentToStay = a1.getPriority() > a2.getPriority() ? a1 : a2;

		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(new Position(agentToStay.getPosition()));
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();

		for (Box box : agentToMove.initialState.boxes.values()) {
			agentToMove.initialState.walls.add(new Position(box.getPosition()));
		}
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		s.setPlanForAgentToStay(updatePlan(agentToStay.getId(), index));
		List<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_AWAY);
		List<Node> newPlanAgentToStay = allSolutions.get(agentToStay.getId());
		for (int i = 0; i < index - 1; i++) {
			newPlanAgentToStay.remove(0);
		}
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));
		Node noOp = agentToStay.initialState;
		noOp.action = new Command();
		newPlanAgentToStay.add(0, noOp);
		World.getInstance().getSolutionMap().put(agentToMove.getId(), newPlanAgentToMove);
		World.getInstance().getSolutionMap().put(agentToStay.getId(), newPlanAgentToStay);
		Agent agentToMoveAway = World.getInstance().getAgents().get(newPlanAgentToMove.get(0).agentId);
		World.getInstance().getBeliefs().add(agentToMoveAway.getIntention().getDesire().getBelief());
	}

	public void solveAgentOnBox(Node node, Agent agent, Box box, int index, List<List<Node>> allSolutions) {
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		List<Node> old = World.getInstance().getSolutionMap().get(agent.getId());
		List<Node> tmpPlan = old.subList(index, old.size());

		s.setBoxRemovalPlan(tmpPlan, box);

		agent.initialState.boxes.put(box.getId(), box);
		List<Node> plan = s.search(strategy, agent.initialState, SearchType.MOVE_OWN_BOX);
		agent.initialState.boxes.remove(box.getId(), box);
		
		World.getInstance().getSolutionMap().put(agent.getId(), plan);
		Agent agentToMoveAway = World.getInstance().getAgents().get(agent.getId());
		World.getInstance().getBeliefs().add(agentToMoveAway.getIntention().getDesire().getBelief());
		
	}

	private List<Node> updatePlan(int agentId, int index) {
		List<Node> updPlan = new LinkedList<Node>();
		List<Node> oldPlan = World.getInstance().getSolutionMap().get(agentId);
		for (int i = 0; i < oldPlan.size(); i++) {
			if (i >= index - 1) {
				updPlan.add(oldPlan.get(i));
			}
		}
		return updPlan;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Conflict [conflictType=").append(conflictType).append(", sender=").append(sender)
		.append(", receiver=").append(receiver).append(", node=").append(node).append("]");
		return builder.toString();
	}
}