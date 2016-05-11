package conflicts;

import java.util.LinkedList;
import java.util.List;

import atoms.Agent;
import atoms.Box;
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

	public void MAsolveBoxOnBox(
			Conflict conflict/* , int index, List<List<Node>> allSolutions */) {

		Agent agentToMove = null, agentToStay = null;
		Box agentToMoveBox = null;
		agentToMove = conflict.getReceiver();
		/*
		 * we need to fetch the box where it is at in the moment, therefore we
		 * retrieve the box from world
		 */
		// agentToMoveBox = conflict.getReceiverBox();
		agentToMoveBox = world.getBoxes().get(conflict.getReceiverBox().getId());
		agentToStay = conflict.getSender();
		/*
		 * HERE we need some code to differantiate beetween
		 * agen-box-other-agent-box conflict and agent-box-box conflict
		 */
		// for(Agent agent : World.getInstance().getAgents().values()){
		// if(agent.getIntention().getBox().equals(agentToMoveBox)){
		// //if the agent did a pull or push the last time, we is touchking the
		// box
		// World.getInstance().write(""+index);
		// if(allSolutions.get(agentToMove.getId()).get(index).action.actType.equals(Command.type.Pull)){
		// World.getInstance().write(agent.getId() + " and box : "
		// +agent.getIntention().getBox().getLetter() + " moveBox " +
		// agentToMoveBox.getLetter());
		MABoxOnBoxConflict.AgentWithBoxOnAgentWithBoxConflict(agentToMove, agentToStay, agentToMoveBox);
		// }else{
		// World.getInstance().write("HER SKAL DER SKE NOGET 1");
		// }
		// }else{
		// World.getInstance().write("HER SKAL DER SKE NOGET 2 " +
		// agent.toString() + " agentToMoveBox : " +agentToMoveBox.getLetter());
		// }
		// }
		/*
		 * in this case we first want to make the sender move his box another
		 * way
		 */
		// agentToMove = conflict.getSender();
		// agentToMoveBox = conflict.getSenderBox();
		// agentToStay = conflict.getReceiver();
		// agentToStayBox = conflict.getReceiverBox();
		//
		// BoxOnBoxConflict.AgentBoxBoxConflict(index,allSolutions,agentToMove,agentToStay,agentToMoveBox,agentToStayBox);

	}

	public void SASolveBoxOnBox(Conflict con) {
		SABoxOnBoxConflict.solveBoxOnBoxSA(con.getNode(), con.getSender(), con.getSenderBox(), con.getReceiverBox());
	}

	public void solveAgentOnAgent(Conflict conflict, Node node, Agent a1, Agent a2) {
		// Agent agentToMove = a1.getPriority() > a2.getPriority() ? a2 : a1;
		// Agent agentToStay = a1.getPriority() > a2.getPriority() ? a1 : a2;
		Agent agentToMove = conflict.receiver;
		Agent agentToStay = conflict.sender;

		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(new Position(agentToStay.getPosition()));
		for (Box box : agentToMove.initialState.boxes.values()) {
			agentToMove.initialState.walls.add(new Position(box.getPosition()));
		}
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		List<Node> newPlanAgentToStay = updatePlan(agentToStay);
		s.setPlanForAgentToStay(newPlanAgentToStay);
		LinkedList<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_AWAY);
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));
		agentToStay.generateInitialState();
		Node noOp = agentToStay.initialState;
		noOp.action = new Command();
		/* Unessesary noOp */
		// newPlanAgentToStay.add(0, noOp);
		int noOpsToAdd = 0;
		if (newPlanAgentToMove != null && !newPlanAgentToMove.isEmpty()) {
			noOpsToAdd = newPlanAgentToMove.size();
			if (noOpsToAdd < 2)
				noOpsToAdd = 4;
			for (int i = 0; i < noOpsToAdd; i++) {
				Node n = newPlanAgentToMove.getLast().childNode();
				noOp = n;
				noOp.action = new Command();
				newPlanAgentToMove.add(noOp);
			}
		} else {
			newPlanAgentToMove = new LinkedList<Node>();
			agentToMove.generateInitialState();
			Node n = agentToMove.initialState;
			noOp = n;
			noOp.action = new Command();
			noOpsToAdd = 2; /*
							 * if we only add 1 noOp we will most likely have an
							 * error. We add 2 noOps
							 */
			for (int i = 0; i < noOpsToAdd; i++) {
				newPlanAgentToMove.add(noOp);
			}
		}
		agentToMove.setPlan(newPlanAgentToMove);
		agentToMove.setStepInPlan(0);
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);
		if (!world.getBeliefs().contains(agentToMove.getIntention().getDesire().getBelief()))
			world.getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
	}

	public void solveAgentOnBox(Node node, Agent agent, Box conflictingBox) {
		Agent agentToMove = agent;
		agentToMove.generateInitialState();
		agentToMove.initialState.setPosition(World.getInstance().getAgents().get(0).getPosition());
		agentToMove.initialState.boxes.put(agent.getIntention().getBox().getId(),
				World.getInstance().getBoxes().get(agent.getIntention().getBox().getId()));
		agentToMove.initialState.goals.put(agent.getIntention().getDesire().getBelief().getGoal().getId(),
				agent.getIntention().getDesire().getBelief().getGoal());

		Agent tmp = new Agent(agentToMove);
		tmp.initialState.walls.add(conflictingBox.getPosition());
		Strategy strategy = new StrategyBestFirst(new AStar(tmp.initialState));
		Search s = new Search();

		List<Node> plan = s.search(strategy, tmp.initialState, SearchType.PATH);
		if (plan == null || plan.isEmpty()) {
			if (agentToMove.initialState.walls.contains(conflictingBox.getPosition()))
				agentToMove.initialState.walls.remove(conflictingBox.getPosition());
			agentToMove.initialState.boxes.put(conflictingBox.getId(), conflictingBox);
			strategy = new StrategyBestFirst(new AStar(agentToMove.initialState));
			s = new Search();
			plan = s.search(strategy, agentToMove.initialState, SearchType.PATH);
		} else {
			agentToMove.initialState.walls.remove(conflictingBox.getPosition());
		}
		agentToMove.setPlan(plan);
		agentToMove.setStepInPlan(0);
		World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
	}

	public void solveAgentOnBox2(Node node, Agent agent, Box box) {
		Agent agentToMove = agent;
		agentToMove.generateInitialState();
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
		agentToMove.initialState.boxes.put(box.getId(), box);
		agentToMove.initialState.boxes.put(agent.getIntention().getBox().getId(),
				world.getBoxes().get(agent.getIntention().getBox().getId()));
		Search s = new Search();
		s.setPlanForAgentToStay(updatePlan(agent));
		LinkedList<Node> newPlan = s.search(new StrategyBFS(), agentToMove.initialState, SearchType.MOVE_OWN_BOX);
		agent.setPlan(newPlan);
		agent.setStepInPlan(0);
		if (!world.getBeliefs().contains(agent.getIntention().getDesire().getBelief())) {
			world.getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
		}
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