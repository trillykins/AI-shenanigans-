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

	
	public void solveBoxOnBox(Conflict conflict, int index, List<List<Node>> allSolutions) {
		/*
		 * Here we look at the agent who's box we marked as a conflict box (in
		 * conflict type)
		 */
		/*
		 * In general it is a problem selecting the agent based on priority.
		 * Here we need to consider narrow corridors or free fields or
		 * something. See MAsimple9 where the wrong agent is selected to move
		 * (lowest priority)
		 */
		/* testing */
		Agent agentToMove = null, agentToStay = null;
		Box agentToMoveBox = null, agentToStayBox = null;
		agentToMove = conflict.getReceiver();
		agentToMoveBox = conflict.getReceiverBox();
		agentToStay = conflict.getSender();
		 
		/*HERE we need some code to differantiate beetween agen-box-other-agent-box conflict and agent-box-box conflict*/
//		for(Agent agent : World.getInstance().getAgents().values()){
//			if(agent.getIntention().getBox().equals(agentToMoveBox)){
//				//if the agent did a pull or push the last time, we is touchking the box
//				World.getInstance().write(""+index);
//				if(allSolutions.get(agentToMove.getId()).get(index).action.actType.equals(Command.type.Pull)){
//					World.getInstance().write(agent.getId() + " and box : " +agent.getIntention().getBox().getLetter() + " moveBox " + agentToMoveBox.getLetter());
					BoxOnBoxConflict.AgentWithBoxOnAgentWithBoxConflict(index,allSolutions,agentToMove,agentToStay,agentToMoveBox);					
//				}else{
//					World.getInstance().write("HER SKAL DER SKE NOGET 1");
//				}
//			}else{				
//				World.getInstance().write("HER SKAL DER SKE NOGET 2 " + agent.toString() + " agentToMoveBox : " +agentToMoveBox.getLetter());
//			}
//		}
		/*in this case we first want to make the sender move his box another way*/
//		agentToMove = conflict.getSender();
//		agentToMoveBox = conflict.getSenderBox();
//		agentToStay = conflict.getReceiver();
//		agentToStayBox = conflict.getReceiverBox();
//		
//		BoxOnBoxConflict.AgentBoxBoxConflict(index,allSolutions,agentToMove,agentToStay,agentToMoveBox,agentToStayBox);
		
	}

	public void solveAgentOnAgent(Node node, Agent a1, Agent a2, int index, List<List<Node>> allSolutions) {
		// System.err.println("Theres is a agent on agent conflict!");
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
			if (i >= newPlanAgentToStay.size())
				break;
			newPlanAgentToStay.remove(0);
		}
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));
		Node noOp = agentToStay.initialState;
		noOp.action = new Command();
		newPlanAgentToStay.add(0, noOp);
		World.getInstance().getSolutionMap().put(agentToMove.getId(), newPlanAgentToMove);
		World.getInstance().getSolutionMap().put(agentToStay.getId(), newPlanAgentToStay);
		// Agent agentToMoveAway =
		// World.getInstance().getAgents().get(newPlanAgentToMove.get(0).agentId);
		World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
	}

	public void solveAgentOnBox(Node node, Agent agent, Box box, int index, List<List<Node>> allSolutions) {
		Agent agentToMove = agent;
		agentToMove.generateInitialState();
		agentToMove.initialState.setPosition(World.getInstance().getAgents().get(0).getPosition());
		agentToMove.initialState.boxes.put(agent.getIntention().getBox().getId(), agent.getIntention().getBox());
		agentToMove.initialState.goals.put(agent.getIntention().getDesire().getBelief().getGoal().getId(),
				agent.getIntention().getDesire().getBelief().getGoal());
		agentToMove.initialState.boxes.put(box.getId(), box);

		Strategy strategy = new StrategyBestFirst(new AStar(agentToMove.initialState));
		// Strategy strategy = new StrategyBFS();
		Search s = new Search();

		List<Node> plan = s.search(strategy, agentToMove.initialState, SearchType.PATH);
		World.getInstance().write("agent conflict resolution plan: \n" + plan);
		World.getInstance().getSolutionMap().put(agentToMove.getId(), plan);
//		Agent agentToMoveAway = World.getInstance().getAgents().get(agentToMove.getId());
//		World.getInstance().getBeliefs().add(agentToMoveAway.getIntention().getDesire().getBelief());
	}

	public static List<Node> updatePlan(int agentId, int index) {
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
				.append(", receiver=").append(receiver).append(", node=").append("\n").append(node).append("]");
		return builder.toString();
	}
}