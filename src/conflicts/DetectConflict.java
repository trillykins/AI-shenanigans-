package conflicts;

import java.util.LinkedList;
import java.util.List;

import atoms.Agent;
import atoms.Box;
import atoms.Position;
import atoms.World;
import bdi.Intention;
import conflicts.Conflict.ConflictType;
import searchclient.Command;
import searchclient.Node;

public class DetectConflict {

	private Box receiverBox = null;
	private Box senderBox = null;

	/**
	 * Check the next step of current moving agent whether has conflict with
	 * other agent or not
	 * 
	 * Update with new conflict type: if current node of agent equals Move, then
	 * it would be Agent-box conflicts otherwise if it is push or pull, then it
	 * would be box-box conflict
	 * 
	 * @param node
	 * @param agent
	 * @return
	 */
	public Conflict checkConflict(int index) {
		Conflict conflict = null;

		if (World.getInstance().getAgents().size() == 1) {
			// System.exit(0); // TODO MA debug purpose
			Agent a1 = World.getInstance().getAgents().get(0);
			/*
			 * as there is no other agents that can be in a1's way, the only
			 * obsticle a1 can bump into is a box
			 */
			Intention intention = a1.getIntention();
			if (intention != null) {

				Node node = World.getInstance().getSolutionMap().get(a1.getId()).get(index);
				Box intentionBox = intention.getBox();
				for (Box box : World.getInstance().getBoxes().values()) {
					if (!box.equals(intentionBox)) {
						if (box.getPosition().equals(a1.getPosition())
								|| intentionBox.getPosition().equals(box.getPosition())) {
							conflict = new Conflict();
							if (node.action.actType.equals(Command.type.Move)) {
								conflict.setConflictType(ConflictType.SINGLE_AGENT_BOX);
							} else {
								conflict.setConflictType(ConflictType.BOX_BOX);
							}
							conflict.setReceiverBox(box);
							conflict.setSender(World.getInstance().getAgents().get(0));
							conflict.setNode(node);
							return conflict;
						}
					}
				}
			}
		} else {
			for (Agent curAgent : World.getInstance().getAgents().values()) {
				if (curAgent.getId() > World.getInstance().getSolutionMap().size()
						|| World.getInstance().getSolutionMap().get(curAgent.getId()) == null
						|| index >= World.getInstance().getSolutionMap().get(curAgent.getId()).size()) {
					continue;
				}
<<<<<<< HEAD
				Node node = World.getInstance().getSolutionMap().get(agent.getId()).get(index);
				int nodeCol = node.agentCol;
				int nodeRow = node.agentRow;
				for (Agent a : World.getInstance().getAgents().values()) {
					Agent sender = null;
					Agent receiver = null;
					if (a.getId() != agent.getId()) {
						LinkedList<Node> solutionForAgentX = (LinkedList<Node>) World.getInstance().getSolutionMap()
								.get(a.getId());
						// solution list is not empty
						if (solutionForAgentX != null && solutionForAgentX.size() > 0) {
							//Node next = solutionForAgentX.peekLast(); -- this peekLast does not make sense to me (THEA)
							if(solutionForAgentX.size() > index){
							Node next = solutionForAgentX.get(index);
							if (next.agentCol == nodeCol && next.agentRow == nodeRow
									|| agent.getPosition() == a.getPosition()) {
								conflict = new Conflict();
								conflict.setConflictType(ConflictType.Agent);
								System.err.println("her?");
								
								if (a.getPriority() > agent.getPriority()) {
									sender = a;
									receiver = agent;
								} else {
									sender = agent;
									receiver = a;
=======
				Node curAgentNode = World.getInstance().getSolutionMap().get(curAgent.getId()).get(index);
				for (Agent otherAgent : World.getInstance().getAgents().values()) {
					if (otherAgent.getId() != curAgent.getId()) {
						List<Node> planForOtherAgent = World.getInstance().getSolutionMap().get(otherAgent.getId());
						if (planForOtherAgent != null && planForOtherAgent.size() > 0) {
							if (planForOtherAgent.size() > index) {
								Node otherAgentNode = planForOtherAgent.get(index);
								if (otherAgentNode.getPosition().equals(curAgentNode.getPosition())
										|| curAgent.getPosition().equals(otherAgent.getPosition())
										|| (curAgentNode.getPosition().equals(otherAgent.getPosition()))) {
									conflict = new Conflict();
									conflict.setConflictType(ConflictType.AGENT);
									conflict.setSender(curAgent);
									conflict.setReceiver(otherAgent);
									Node previousNode = curAgentNode.parent;
									conflict.setNode(previousNode);
									return conflict;
>>>>>>> master
								}
							}
<<<<<<< HEAD
							}
						}

						if (nodeCol == a.getPosition().getY() && nodeRow == a.getPosition().getX()) {
							conflict = new Conflict();
							conflict.setConflictType(ConflictType.Agent);
							conflict.setSender(agent);
							conflict.setReceiver(a);
							Node previousNode = node.parent;
							conflict.setNode(previousNode); // Need to get the
															// previous node, as
															// the current one
															// already has a
															// conflict.
							return conflict;
						}
						
						Node nextNodeCurrAgent = null;
						if(World.getInstance().getSolutionMap().get(agent.getId()).size() > index + 1)
							nextNodeCurrAgent = World.getInstance().getSolutionMap().get(agent.getId()).get(index+1);
						
						boolean isOtherAgentBox = checkBoxes(nodeRow, nodeCol,a,nextNodeCurrAgent);
						if (isOtherAgentBox) {
							System.err.println("her 3");
							conflict = new Conflict();
							if (node.action.actType.equals(Command.type.Move)) {
								conflict.setConflictType(ConflictType.Agent_Box);
							} else if (node.action.actType.equals(Command.type.Pull) || node.action.actType.equals(Command.type.Push)){
								conflict.setConflictType(ConflictType.Box_Box);
=======
							Node nextNodeCurrAgent = null;
							if (World.getInstance().getSolutionMap().get(curAgent.getId()).size() > index + 1)
								nextNodeCurrAgent = World.getInstance().getSolutionMap().get(curAgent.getId())
										.get(index + 1);

							boolean isOtherAgentBox = checkBoxes(curAgentNode.agentRow, curAgentNode.agentCol,
									otherAgent, nextNodeCurrAgent);
							if (isOtherAgentBox) {
								conflict = new Conflict();
								if (curAgentNode.action.actType.equals(Command.type.Move)) {
									conflict.setConflictType(ConflictType.SINGLE_AGENT_BOX);
								} else if (curAgentNode.action.actType.equals(Command.type.Pull)
										|| curAgentNode.action.actType.equals(Command.type.Push)) {
									{
										conflict.setConflictType(ConflictType.BOX_BOX);
									}
									conflict.setReceiverBox(receiverBox);
									conflict.setSenderBox(senderBox);
									conflict.setSender(curAgent);
									conflict.setReceiver(otherAgent);
									conflict.setNode(curAgentNode);
									return conflict;
								}
>>>>>>> master
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Check whether the agent can move to the position(row,col) based on the
	 * logic that : check is it a wall on this position or a movable box of this
	 * agent OR is there a box for any other agent, otherwise this cell if free
	 * for the agent to move to
	 * 
	 * @param row
	 * @param col
	 * @param agent
	 * @return
	 */
	public boolean isCellFree(int row, int col, Agent agent) {
		boolean isBoxPosi = isBoxPosi(row, col);
		for (Agent agen : World.getInstance().getAgents().values()) {
			if (agen.getId() != agent.getId()) {
				// check current postion is the next postion of other agent
				LinkedList<Node> plan = (LinkedList<Node>) World.getInstance().getSolutionMap().get(agen.getId());
				if (plan.size() > 0) {
					Node next = plan.peek();
					int nextcol = next.agentCol;
					int nextrow = next.agentRow;
					if (nextcol == col && nextrow == row) {
						return false;
					}
				}
				if (agen.getPosition().getY() == col && agen.getPosition().getX() == row) {
					// if current position is the position of other agent
					return false;
				} else if (isBoxPosi) {
					// If current position is the box of other agent, then could
					// not move
<<<<<<< HEAD
					if (checkBoxes(row, col, agen,null)) {
=======
					if (checkBoxes(row, col, agen, null)) {
>>>>>>> master
						return false;
					}

				}
			}

		}
		boolean isOwnBox = true;
		if (isBoxPosi) {
<<<<<<< HEAD
			isOwnBox = checkBoxes(row, col, agent,null);
=======
			isOwnBox = checkBoxes(row, col, agent, null);
>>>>>>> master
		}
		return isOwnBox && !World.getInstance().getWalls().contains(new Position(row, col));
	}

	/**
	 * Check the current position is box position
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	private boolean isBoxPosi(int row, int col) {
		for (Integer intB : World.getInstance().getBoxes().keySet()) {
			Box b = World.getInstance().getBoxes().get(intB);
			if (b.getPosition().equals(new Position(row, col))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check current position is the box position of the input agent
	 * 
	 * @param row
	 * @param col
	 * @param agen
	 * @return
	 */
<<<<<<< HEAD
	public boolean checkBoxes(int row, int col, Agent agen,Node nextNodeCurrAgent) {
		/*agent on box*/
=======
	public boolean checkBoxes(int row, int col, Agent agen, Node nextNodeCurrAgent) {
		/* agent on box */
>>>>>>> master
		for (Integer bId : agen.initialState.boxes.keySet()) {
			Box b = World.getInstance().getBoxes().get(bId);
			if (b.getPosition().equals(new Position(row, col))) {
				receiverBox = b;
				return true;
<<<<<<< HEAD
			} else if(nextNodeCurrAgent != null){
				/*box on box conflict detected*/
				if(nextNodeCurrAgent.agentRow == b.getPosition().getX() && nextNodeCurrAgent.agentCol == b.getPosition().getY()){
					conflictBox = b;
					return true;
				}else if (nextNodeCurrAgent.boxes.values().size() > 0){
					for(Box box : nextNodeCurrAgent.boxes.values()){
						if(box.getPosition().equals(b.getPosition())){
							conflictBox = b;
=======
			} else if (nextNodeCurrAgent != null) {
				/* box on box conflict detected */
				if (nextNodeCurrAgent.agentRow == b.getPosition().getX()
						&& nextNodeCurrAgent.agentCol == b.getPosition().getY()) {
					receiverBox = b;
					return true;
				} else if (nextNodeCurrAgent.boxes.values().size() > 0) {
					for (Box box : nextNodeCurrAgent.boxes.values()) {
						if (box.getPosition().equals(b.getPosition())) {
							receiverBox = b;
							senderBox = box;
>>>>>>> master
							return true;
						}
					}
				}
<<<<<<< HEAD
			}else {
=======
			} else {
>>>>>>> master
				return false;
			}
		}
		return false;
	}
}