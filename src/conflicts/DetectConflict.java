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

	public Conflict checkConflict(int index) {
		Conflict conflict = null;

		if (World.getInstance().getAgents().size() == 1) {
			// System.exit(0); // TODO MA debug purpose
			Agent a1 = World.getInstance().getAgents().get(0);
			/*
			 * as there is no other agents that can be in a1's way, the only
			 * obstacle a1 can bump into is a box
			 */
			Intention intention = a1.getIntention();
			if (intention != null) {

				Node next = World.getInstance().getSolutionMap().get(a1.getId()).get(index);
				Box intentionBox = intention.getBox();
				for (Box box : World.getInstance().getBoxes().values()) {
					if (!box.equals(intentionBox)) {
						if (box.getPosition().equals(next.getPosition())) {
							conflict = new Conflict();
							if (next.action.actType.equals(Command.type.Move)  || next.action.actType.equals(Command.type.Pull)) {
								conflict.setConflictType(ConflictType.SINGLE_AGENT_BOX);
								conflict.setReceiverBox(box);
								conflict.setSender(World.getInstance().getAgents().get(0));
								conflict.setNode(next);
								return conflict;
							}
						}
						/*Want to check if the next node (to the box we are "carring") is a box)*/
						else {
							for (Box nextNodeBox : next.boxes.values()) {
								if(box.getPosition().equals(nextNodeBox.getPosition()) && next.action.actType.equals(Command.type.Push)){
									conflict.setConflictType(ConflictType.BOX_BOX);
									conflict.setReceiverBox(box);
									conflict.setSender(World.getInstance().getAgents().get(0));
									conflict.setNode(next);
									return conflict;
								}
							}	

						}

					}
				}
			}
		} else { /*Dealing with MA situation*/
			for (Agent curAgent : World.getInstance().getAgents().values()) {
				/*We don't want to look further if the currAgents plan is null or the size is smaller than curr index*/
				if (curAgent.getId() > World.getInstance().getSolutionMap().size()
						|| World.getInstance().getSolutionMap().get(curAgent.getId()) == null
						|| index >= World.getInstance().getSolutionMap().get(curAgent.getId()).size()
						|| isFinishSolution(curAgent)) {
					continue;
				}
				Node curAgentNode = World.getInstance().getSolutionMap().get(curAgent.getId()).get(index);
				for (Agent otherAgent : World.getInstance().getAgents().values()) {
					if (otherAgent.getId() != curAgent.getId()) {

						List<Node> planForOtherAgent = World.getInstance().getSolutionMap().get(otherAgent.getId());
						if (planForOtherAgent != null && planForOtherAgent.size() > 0) {

							/*Agent on agent detection*/
							if (planForOtherAgent.size() > index) {
								Node otherAgentNode = planForOtherAgent.get(index);
								if (otherAgentNode.getPosition().equals(curAgentNode.getPosition())
										|| curAgent.getPosition().equals(otherAgent.getPosition())
										|| (curAgentNode.getPosition().equals(otherAgent.getPosition()))) {
									conflict = new Conflict();
									conflict.setConflictType(ConflictType.AGENT);
									conflict.setSender(curAgent);
									conflict.setReceiver(otherAgent);
									/*TODO : Soooo.. for single agent we put in next node, but for multi agent we put in previous node? da fuck
									 * PLEASE EXPLAIN!!!*/
									Node previousNode = curAgentNode.parent;
									conflict.setNode(previousNode);

									if(curAgentNode.action.actType.equals(Command.type.Pull) || curAgentNode.action.actType.equals(Command.type.Push)){
										for (Box box : curAgentNode.boxes.values()) {
											conflict.setSenderBox(box);
										}
									}
									if(otherAgentNode.action.actType.equals(Command.type.Pull) || otherAgentNode.action.actType.equals(Command.type.Push)){
										for (Box box : curAgentNode.boxes.values()) {
											conflict.setReceiverBox(box);	
										}
									}
									return conflict;
								}
							}else {
								if(curAgentNode.action.actType.equals(Command.type.Push)) {
									Node parent = curAgentNode.parent;
									for(Box box: parent.boxes.values()) {
										if(box.getPosition().equals(otherAgent.getPosition())) {
											conflict = new Conflict();
											conflict.setSender(curAgent);
											conflict.setReceiver(otherAgent);
											/*TODO : Soooo.. for single agent we put in next node, but for multi agent we put in previous node? da fuck
											 * PLEASE EXPLAIN!!!*/
											conflict.setNode(parent);
											conflict.setConflictType(ConflictType.SINGLE_AGENT_BOX);
											conflict.setSenderBox(box);
											conflict.setReceiverBox(box);
											return conflict;
										}
									}
								}
								if (curAgent.getPosition().equals(otherAgent.getPosition())
										|| (curAgentNode.getPosition().equals(otherAgent.getPosition()))) {
									conflict = new Conflict();
									conflict.setSender(curAgent);
									conflict.setReceiver(otherAgent);
									/*TODO : Soooo.. for single agent we put in next node, but for multi agent we put in previous node? da fuck
									 * PLEASE EXPLAIN!!!*/
									conflict.setNode(curAgentNode);

									if(curAgentNode.action.actType.equals(Command.type.Pull) || curAgentNode.action.actType.equals(Command.type.Push)){
										conflict.setConflictType(ConflictType.SINGLE_AGENT_BOX);

									}else {
										conflict.setConflictType(ConflictType.AGENT);
										
									}
									for (Box box : curAgentNode.boxes.values()) {
										conflict.setSenderBox(box);
										conflict.setReceiverBox(box);
									}
									return conflict;
								}
							}
							
							/*Detects if the currAgent bumps into a box he cannot see*/
							boolean isOtherBoxes = isOtherBoxesOrNonIntentionBox(curAgent,otherAgent,curAgentNode.getPosition());
							if(isOtherBoxes) {
								conflict = new Conflict();
								conflict.setConflictType(ConflictType.SINGLE_AGENT_BOX);
								conflict.setSender(curAgent);
								conflict.setReceiver(otherAgent);
								conflict.setNode(curAgentNode);
								conflict.setReceiverBox(receiverBox);	
								return conflict;
							}
							
							/*We consider an agent that moved into a box or box on box conflict*/
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
									conflict.setConflictType(ConflictType.BOX_BOX);
								}
								conflict.setReceiverBox(receiverBox);
								conflict.setSenderBox(senderBox);
								conflict.setSender(curAgent);
								conflict.setReceiver(otherAgent);
								conflict.setNode(curAgentNode);
								return conflict;
							}

							/*Agent on box detection (where curAgent pulls or pushes a box)*/
							if (planForOtherAgent.size() > index) {
								/*if the box we are pulling or pushing is in the field of the agent*/
								if (curAgentNode.action.actType.equals(Command.type.Pull)
										|| curAgentNode.action.actType.equals(Command.type.Push)) {
									for (Integer bId : curAgent.initialState.boxes.keySet()) {
										Box b = curAgentNode.boxes.get(bId);
										if(b.getPosition().equals(planForOtherAgent.get(index).getPosition())){
											conflict = new Conflict();
											conflict.setConflictType(ConflictType.SINGLE_AGENT_BOX);
											conflict.setSender(curAgent);
											conflict.setReceiver(otherAgent);
											conflict.setNode(curAgentNode);
											conflict.setReceiverBox(b);
											return conflict;
										}
									}
								}else {
									/*
									 * Add a situation that one agent has two goals, and already achieved one.
									 * But the achieved goals is on the way of second solution
									 */
									Intention intention = curAgent.getIntention();
									if(intention != null) {
										Box intenbox = curAgent.getIntention().getBox();
										Position nextMovePosition = new Position(curAgentNode.agentRow,curAgentNode.agentCol);
										for (Box boxes : World.getInstance().getBoxes().values()) {
											if(boxes.getPosition().equals(nextMovePosition) && 
													!boxes.equals(intenbox)){
												conflict = new Conflict();
												conflict.setConflictType(ConflictType.SINGLE_AGENT_BOX);
												conflict.setSender(curAgent);
												conflict.setReceiver(otherAgent);
												conflict.setNode(curAgentNode);
												conflict.setReceiverBox(boxes);
												return conflict;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	private boolean isFinishSolution(Agent agent) {
		List<Node> solution = World.getInstance().getSolutionMap().get(agent.getId());
		if(solution != null && solution.size() == 1) {
			if(solution.get(0).action.actType.equals(Command.type.NoOp)) {
				return true;
			}
		}
		return false;
	}

	private boolean isOtherBoxesOrNonIntentionBox(Agent agent,Agent otherAgent,Position currAgentPos) {
		for(Box box: World.getInstance().getBoxes().values()) {
			//check if is it current agent non intention box
			if(box.getColor().equals(otherAgent.getColor()) && box.getPosition().equals(currAgentPos)) {
				receiverBox = box;
				return true;
			}
		}
		return false;
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
					if (checkBoxes(row, col, agen, null)) {
						return false;
					}

				}
			}

		}
		boolean isOwnBox = true;
		if (isBoxPosi) {
			isOwnBox = checkBoxes(row, col, agent, null);
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
	 * @param otherAgent
	 * @return
	 */
	public boolean checkBoxes(int row, int col, Agent otherAgent, Node nextNodeCurrAgent) {
		/* agent on box */
		//World.getInstance().write("her");
		for (Integer bId : otherAgent.initialState.boxes.keySet()) {
			Box b = World.getInstance().getBoxes().get(bId);
			if (b.getPosition().equals(new Position(row, col))) {
				receiverBox = b;
				return true;
			} else if (nextNodeCurrAgent != null) {
				/* box on box conflict detected */
				if (nextNodeCurrAgent.agentRow == b.getPosition().getX()
						&& nextNodeCurrAgent.agentCol == b.getPosition().getY()) {
					receiverBox = b;
					for (Box nextNodeCurrAgentBox : nextNodeCurrAgent.boxes.values()) {
					senderBox = nextNodeCurrAgentBox;
					}
					return true;
				} else if (nextNodeCurrAgent.boxes.values().size() > 0) {
					for (Box box : nextNodeCurrAgent.boxes.values()) {
						if (box.getPosition().equals(b.getPosition())) {
							receiverBox = b;
							senderBox = box;
							return true;
						}
					}
				}
			} else {
				return false;
			}
		}
		/*run though boxes that does not belong to any agents (this case all boxes)*/
		for(Integer bid : World.getInstance().getBoxes().keySet()){
			Box b = World.getInstance().getBoxes().get(bid);
			if (nextNodeCurrAgent == null) return false;
			/*TODO if this can be optimized maybe it should be :S running through all boxes*/
			for (Box nextNodeCurrAgentBox : nextNodeCurrAgent.boxes.values()) {
				if(b.getPosition().equals(nextNodeCurrAgentBox.getPosition()) && !b.equals(nextNodeCurrAgentBox)){
					receiverBox = b;
					senderBox = nextNodeCurrAgentBox;
					return true;
				}
			}
		}
		return false;
	}
}