package conflicts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.World;
import searchclient.Command;
import searchclient.Node;
import searchclient.Search;
import searchclient.Search.SearchType;
import strategies.StrategyBFS;

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

	public void MAsolveBoxOnBox(
			Conflict conflict/* , int index, List<List<Node>> allSolutions */) {

		Agent agentToMove = null, agentToStay = null;
		Box agentToMoveBox = null; /* agentToStayBox = null; */
		agentToMove = conflict.getReceiver();
		/*
		 * we need to fetch the box where it is at in the moment, therefore we
		 * retrieve the box from world
		 */
		// agentToMoveBox = conflict.getReceiverBox();
		agentToMoveBox = World.getInstance().getBoxes().get(conflict.getReceiverBox().getId());
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
		SABoxOnBoxConflict.solveBoxOnBoxSA(con.getNode(), con.getSender(), World.getInstance().getBoxes().get(con.getSenderBox().getId()),
				World.getInstance().getBoxes().get(con.getReceiverBox().getId()));
	}

	public void solveAgentOnAgent(Conflict conflict, Node node, Agent a1, Agent a2) {
		Agent agentToMove = conflict.receiver;
		Agent agentToStay = conflict.sender;
		Box boxToMove = conflict.senderBox;

		if (agentToMove.getPlan().get(agentToMove.getStepInPlan()).action.actType.equals(Command.type.Pull)
				|| agentToMove.getPlan().get(agentToMove.getStepInPlan()).action.actType.equals(Command.type.Push)) {
			MAAgentOnAgentConflict.moveAgentOnAgentWithBox(agentToMove, agentToStay, boxToMove);
		} else {
			MAAgentOnAgentConflict.moveAgentOnAgentNoBox(agentToMove, agentToStay, boxToMove);

		}
	}

	/*
	 * Scribbles If the plan returns null, remove all box- and goal-walls and
	 * recalculate plan. Create a plan as done originally, but somehow include
	 * the boxes that have originally been put aside to make room. If this isn't
	 * possible, then shit pants and give up. Cake!
	 * 
	 * keep track of previously moved boxes (that are not used to solve goals)
	 * 
	 * if plan is null for moving boxes out of the way: - plan new route to
	 * place boxes if previously moved box in the way, - make plan to move box
	 * to a spot where it is possible for agent other boxes (new search, to
	 * check that the box spot isn't on the previously moved box position)
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	public void superPlanner(Agent agent, Box box) {
		List<Node> originalAgentPlan = agent.getPlan();
		Agent agentToMove = agent;
		agentToMove.initialState.setPosition(World.getInstance().getAgents().get(0).getPosition());
		Search s = new Search();
		List<Box> boxesForReplanning = new LinkedList<>();
		for (int i = 0; i < originalAgentPlan.size(); i++) {
			for (Box b : World.getInstance().getBoxes().values()) {
				if (originalAgentPlan.get(i).getAgentPosition().equals(b.getPosition()) && !boxesForReplanning.contains(b)) {
					boxesForReplanning.add(b);
					agentToMove.addPreviouslyMovedBoxLocations(b.getId());
				}
			}
		}
		List<Box> futurePositions = new ArrayList<>(boxesForReplanning.size());
		List<List<Node>> plans = new ArrayList<>(boxesForReplanning.size());
		// reverse order
		for (int i = boxesForReplanning.size() - 1; i >= 0; i--) {
			Agent boxieToMove = agent;
			boxieToMove.generateInitialState();

			boxieToMove.initialState.setPosition(World.getInstance().getAgents().get(0).getPosition());
			boxieToMove.initialState.boxes.put(boxesForReplanning.get(i).getId(), boxesForReplanning.get(i));
			for (Box wb : World.getInstance().getBoxes().values()) {
				if (!boxesForReplanning.contains(wb)) {
					agentToMove.initialState.walls.add(wb.getPosition());
				}
			}
			for (Goal wg : World.getInstance().getGoals().values()) {
				if (!boxesForReplanning.contains(wg)) {
					agentToMove.initialState.walls.add(wg.getPosition());
				}
			}
			s.setFutureBoxPositions(futurePositions);
			s.setPlanForAgentToStay(updatePlan(agentToMove));
			boxieToMove.initialState.moveToPositionRow = s.getOtherPlan().get(0).agentRow;
			boxieToMove.initialState.moveToPositionCol = s.getOtherPlan().get(0).agentCol;
			LinkedList<Node> tmpPlan = s.search(new StrategyBFS(), boxieToMove.initialState, SearchType.MOVE_BOXES);
			if (tmpPlan == null || tmpPlan.isEmpty()) {
				for (int j = 0; j < futurePositions.size(); j++) {
					Box box1 = futurePositions.get(j);
					boxieToMove.initialState.boxes.put(box1.getId(), box1);
				}
			}
			for (Box wb : World.getInstance().getBoxes().values()) {
				if (!boxesForReplanning.contains(wb)) {
					agentToMove.initialState.walls.remove(wb.getPosition());
				}
			}
			for (Goal wg : World.getInstance().getGoals().values()) {
				if (!boxesForReplanning.contains(wg)) {
					agentToMove.initialState.walls.remove(wg.getPosition());
				}
			}

			if (tmpPlan != null && !tmpPlan.isEmpty()) {
				for (Box b : tmpPlan.getLast().boxes.values()) {
					futurePositions.add(b); // TODO potential problem
				}
				plans.add(tmpPlan);
			}
		}
		List<Node> finalPlan = new LinkedList<>();
		for (int i = plans.size() - 1; i >= 0; i--) {
			finalPlan.addAll(plans.get(i));
		}
		finalPlan.addAll(updatePlan(agent));

		agentToMove.setPlan(finalPlan);
		agentToMove.setStepInPlan(0);
	}

	public void solveAgentOnBox(Node node, Agent agent, Box box) {
		superPlanner(agent, box);
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
		builder.append("Conflict [conflictType=").append(conflictType).append(", sender=").append(sender).append(", receiver=").append(receiver).append(", node=")
				.append("\n").append(node).append("]");
		return builder.toString();
	}
}