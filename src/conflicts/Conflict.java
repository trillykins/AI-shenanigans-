package conflicts;

import java.util.LinkedList;
import java.util.List;

import atoms.Agent;
import atoms.Box;
import atoms.Color;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import heuristics.AStar;
import searchclient.Command;
import searchclient.Node;
import searchclient.Search;
import searchclient.Search.SearchType;
import strategies.Strategy;
import strategies.StrategyBFS;
import strategies.StrategyBestFirst;

public class Conflict {
	private ConflictType conflictType;
	private Agent sender;
	private Agent receiver;
	private Node node;
	private Box receiverBox;
	private Box senderBox;
	private static World world = World.getInstance();

	public enum ConflictType {
		AGENT, BOX_BOX, SINGLE_AGENT_BOX,
	}

	public Box getSenderBox() {
		return senderBox;
	}

	public void setSenderBox(Box senderBox) {
		this.senderBox = senderBox;
	}

	public Box getReceiverBox() {
		return receiverBox;
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

	public void setReceiverBox(Box box) {
		this.receiverBox = box;
	}

	
	public void MAsolveBoxOnBox(Conflict conflict/*, int index, List<List<Node>> allSolutions*/) {	
		Agent agentToMove = null, agentToStay = null;
		Box agentToMoveBox = null, agentToStayBox = null;
		
		agentToMove = conflict.getReceiver();
		agentToMoveBox = World.getInstance().getBoxes().get(conflict.getReceiverBox().getId());
		agentToStay = conflict.getSender();
		agentToStayBox = conflict.getSenderBox();
		
		System.err.println("agentToStay position : " +conflict.getSender() + ", pos :" +conflict.getSender().getPosition());
		if(agentToMove == null){
			MABoxOnBoxConflict.AgentBoxBoxConflict(agentToStay,agentToStayBox,agentToMoveBox);
		}else{
			MABoxOnBoxConflict.AgentWithBoxOnAgentWithBoxConflict(agentToMove,agentToStay,agentToMoveBox);					
		}
	}
	
	public void SASolveBoxOnBox(Conflict con){
		SABoxOnBoxConflict.solveBoxOnBoxSA(con.getNode(), con.getSender(), con.getSenderBox(), con.getReceiverBox());
	}

	public void solveAgentOnAgent(Conflict conflict,Node node, Agent a1, Agent a2) {
		Agent agentToMove = conflict.receiver;
		Agent agentToStay = conflict.sender;
		Box boxToMove = conflict.senderBox;
		
		if(agentToMove.getPlan().get(agentToMove.getStepInPlan()).action.actType.equals(Command.type.Pull) ||
				agentToMove.getPlan().get(agentToMove.getStepInPlan()).action.actType.equals(Command.type.Push)){
			MAAgentOnAgentConflict.moveAgentOnAgentWithBox(agentToMove, agentToStay, boxToMove);
		}else{
			MAAgentOnAgentConflict.moveAgentOnAgentNoBox(agentToMove, agentToStay, boxToMove);
			
		}
	}

	public void solveAgentOnBox(Node node, Agent agent, Box box) {
		Agent agentToMove = agent;
		agentToMove.generateInitialState();
		agentToMove.initialState.setPosition(World.getInstance().getAgents().get(0).getPosition());
		agentToMove.initialState.boxes.put(agent.getIntention().getBox().getId(),
				World.getInstance().getBoxes().get(agent.getIntention().getBox().getId()));
		agentToMove.initialState.goals.put(agent.getIntention().getDesire().getBelief().getGoal().getId(),
				agent.getIntention().getDesire().getBelief().getGoal());

		Agent tmp = new Agent(agentToMove);
		tmp.initialState.walls.add(box.getPosition());
		Strategy strategy = new StrategyBestFirst(new AStar(tmp.initialState));
		Search s = new Search();

		List<Node> plan = s.search(strategy, tmp.initialState, SearchType.PATH);
		if (plan == null || plan.isEmpty()) {
			if (agentToMove.initialState.walls.contains(box.getPosition()))
				agentToMove.initialState.walls.remove(box.getPosition());
			agentToMove.initialState.boxes.put(box.getId(), box);
			strategy = new StrategyBestFirst(new AStar(agentToMove.initialState));
			s = new Search();
			plan = s.search(strategy, agentToMove.initialState, SearchType.PATH);
		} else {
			agentToMove.initialState.walls.remove(box.getPosition());
		}
		agentToMove.setPlan(plan);
		agentToMove.setStepInPlan(0);
		World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
	}

	public static List<Node> updatePlan(Agent agent) {
		List<Node> updPlan = new LinkedList<Node>();
		List<Node> oldPlan = agent.getPlan();
		for (int i = 0; i < oldPlan.size(); i++) {
			if (i >= agent.getStepInPlan() - 1) {
				updPlan.add(oldPlan.get(i));
			}
		}
		return updPlan;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Conflict [conflictType=").append(conflictType).append(", sender=").append(sender)
				.append(", receiver=").append(receiver).append(", node=").append("\n").append(node).append("]");
		return builder.toString();
	}
}