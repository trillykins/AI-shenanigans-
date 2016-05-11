package conflicts;

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
<<<<<<< HEAD

=======
	
>>>>>>> refs/remotes/origin/master
	public Conflict checkConflict() {
		Conflict conflict = null;

		if (World.getInstance().getAgents().size() == 1) {
<<<<<<< HEAD
			conflict = detectConflictSA();
		} else { /* Dealing with MA situation */
			conflict = detectConflictMA();
		}
		return conflict;
	}

	private Conflict detectConflictSA() {
		Agent agent = World.getInstance().getAgents().get(0);
		/*
		 * as there is no other agents that can be in a1's way, the only
		 * obstacle a1 can bump into is a box
		 */

		Intention intention = agent.getIntention();
		// a1.getPlan().get(a1.getStepInPlan()).action.t
=======
			conflict =  detectSignalAgentConflict();
		} else { /*Dealing with MA situation*/
			conflict =  detectMultiAgengConflict();
		}
		return conflict;
	}
	
	private Conflict detectSignalAgentConflict() {
		// System.exit(0); // TODO MA debug purpose
		Agent agent = World.getInstance().getAgents().get(0);
		/*
		* as there is no other agents that can be in a1's way, the only
		* obstacle a1 can bump into is a box
		*/
		Intention intention = agent.getIntention();
>>>>>>> refs/remotes/origin/master
		if (intention != null) {
			Node next = agent.getPlan().get(agent.getStepInPlan());
			Box intentionBox = intention.getBox();
			Box intentionBoxPositionInNext = next.boxes.get(intention.getBox().getId());
			switch (next.action.actType) {
			case Move:
				for (Box box : World.getInstance().getBoxes().values()) {
					if (box.getPosition().equals(next.getAgentPosition()) && !box.equals(intentionBox)) {
						System.err.println("MOVE");
						return createConflict(agent, null, box, null, next, ConflictType.SINGLE_AGENT_BOX);
					}
				}
				break;
			case Push:
				for (Box conflictingBox : World.getInstance().getBoxes().values()) {
					if (conflictingBox.getPosition().equals(intentionBoxPositionInNext.getPosition()) && conflictingBox.getId() != intentionBox.getId() /*&& !conflictingBox.equals(intentionBox)*/) {
						System.err.println("PUSH");
						return createConflict(agent, null, conflictingBox, intentionBox, next, ConflictType.BOX_BOX);
					}
				}
				break;
			case Pull:
				for (Box conflictingBox : World.getInstance().getBoxes().values()) {
					if (conflictingBox.getPosition().equals(next.getAgentPosition()) && !conflictingBox.equals(intentionBox)) {
						System.err.println("PULL");
						return createConflict(agent, null, conflictingBox, intentionBox, next, ConflictType.SINGLE_AGENT_BOX);
					}
				}
				break;
			case NoOp:
				System.err.println("!!!Agent is No-Op'ing in a single-agent level!!!".toUpperCase());
				System.exit(-1);
				break;
			}
		}
		return null;
	}
<<<<<<< HEAD

	private Conflict detectConflictMA() {
		for (Agent curAgent : World.getInstance().getAgents().values()) {
			/*
			 * We don't want to look further if the currAgents plan is null or
			 * the size is smaller than curr index
			 */
			if (curAgent.getPlan() == null || curAgent.getPlan().size() == 0 || isFinishSolution(curAgent)) {
=======
	
	private Conflict detectMultiAgengConflict() {
		for (Agent curAgent : World.getInstance().getAgents().values()) {
			/*We don't want to look further if the currAgents plan is null or the size is smaller than curr index*/
			if (curAgent.getPlan() == null || curAgent.getPlan().size() == 0
					|| isFinishSolution(curAgent)) {
>>>>>>> refs/remotes/origin/master
				continue;
			}
			Conflict conflict = null;
			Node curAgentNode = curAgent.getPlan().get(curAgent.getStepInPlan());
			for (Agent otherAgent : World.getInstance().getAgents().values()) {
				if (otherAgent.getId() != curAgent.getId()) {
					List<Node> planForOtherAgent = otherAgent.getPlan();
<<<<<<< HEAD
					if (planForOtherAgent != null && planForOtherAgent.size() > 0) {
						Node otherAgentNode = planForOtherAgent.get(otherAgent.getStepInPlan());
						/*
						 * If current agent node action is push, then should
						 * check the parent node
						 */
						boolean isCurrentPush = false;
						if (curAgentNode.action.actType.equals(Command.type.Push)) {
							isCurrentPush = true;
						}
						conflict = compareNextNodesOfTwoAgent(curAgentNode, otherAgentNode, curAgent, otherAgent,
								isCurrentPush);
						if (conflict != null) {
							return conflict;
						}
					}
					conflict = compareNextNodeWithBoxOrAgent(curAgentNode, curAgent, otherAgent);
					if (conflict != null) {
						return conflict;
=======
					Node otherAgentNode = null;
					if (planForOtherAgent != null && planForOtherAgent.size() > 0) {
						otherAgentNode = planForOtherAgent.get(otherAgent.getStepInPlan());
					}
					
					/*
					 * If current agent node action is push, then should check the parent node
					 */
					if(curAgentNode.action.actType.equals(Command.type.Push)) {
						conflict = currentPushingConflictCheck(curAgentNode,otherAgentNode,curAgent,otherAgent);
						if(conflict != null) {
							return conflict;
						}
					}else {
						conflict = currentPullingOrMoveConflictCheck(curAgentNode,otherAgentNode,curAgent,otherAgent);
						if(conflict != null) {
							return conflict;
						}
>>>>>>> refs/remotes/origin/master
					}
				}
			}
		}
		return null;
	}
	
	
	/**
<<<<<<< HEAD
	 * If current agent already finish the solution, then do not need to check
	 * the solution of this agent becasue it could only be a conflict agent for
	 * other agent,other agent would not conflict it.
	 * 
=======
	 * If current agent already finish the solution, then do not need to check the solution of this agent
	 * becasue it could only be a conflict agent for other agent,other agent would not conflict it.
>>>>>>> refs/remotes/origin/master
	 * @param agent
	 * @return
	 */
	private boolean isFinishSolution(Agent agent) {
		List<Node> solution = agent.getPlan();
<<<<<<< HEAD
		if (solution != null && solution.size() == 1) {
			if (solution.get(0).action.actType.equals(Command.type.NoOp)) {
=======
		if(solution != null && solution.size() == 1) {
			if(solution.get(0).action.actType.equals(Command.type.NoOp)) {
>>>>>>> refs/remotes/origin/master
				return true;
			}
		}
		return false;
	}

	/**
	 * Create a conflict
<<<<<<< HEAD
	 * 
=======
>>>>>>> refs/remotes/origin/master
	 * @param sender
	 * @param receiver
	 * @param receBox
	 * @param senderBox
	 * @param currNode
	 * @param type
	 * @return
	 */
<<<<<<< HEAD
	private Conflict createConflict(Agent sender, Agent receiver, Box receBox, Box senderBox, Node currNode,
			ConflictType type) {
		return new Conflict(sender, receiver, receBox, senderBox, currNode, type);
		// Conflict conflict = new Conflict();
		// conflict.setConflictType(type);
		// conflict.setSender(sender);
		// conflict.setReceiver(receiver);
		// conflict.setNode(currNode);
		// conflict.setReceiverBox(receBox);
		// conflict.setSenderBox(senderBox);
		// return conflict;
	}

	/**
	 * first compare the next move of two agents if current agent is pushing
	 * next agent is pushing====> box-box next agent is pulling or moving ====>
	 * agent-box else if current agent is pulling or moving next agent is
	 * pushing ===> agent-box next agent is pulling or moving ====> agent-agent
	 * 
=======
	private Conflict createConflict(Agent sender,Agent receiver, Box receBox, Box senderBox, Node currNode,ConflictType type) {
		Conflict conflict = new Conflict();
		conflict.setConflictType(type);
		conflict.setSender(sender);
		conflict.setReceiver(receiver);
		conflict.setNode(currNode);
		conflict.setReceiverBox(receBox);
		conflict.setSenderBox(senderBox);
		return conflict;
	}
	
	/**
	 * Compare the current pushing node with other's conflict
	 * if current agent is pushing
	 * 	   if other agent is pushing 
	 * 				===> check current agent previous node boxes position = otherNode boxes position =====> Box-Box
	 * 	            ===> check current node boxes position = otherNode boxes postion =======> Box-Box
	 *     else if other agent is pulling or moving 
	 *     			===> check current agent previous node boxes position = other agent postion/other agent's node position ======>Agent-Box
	 *                                                                    = other box position   ==========>Box-Box
	 *              ===> check current node boxes position = other agent postion/other agent's node position ======>Agent-Box
	 *              								       = other box position   ==========>Box-Box
>>>>>>> refs/remotes/origin/master
	 * @param curAgentNode
	 * @param otherAgentNode
	 * @param curAgent
	 * @param otherAgent
	 * @param isPush
	 * @return
	 */
<<<<<<< HEAD
	private Conflict compareNextNodesOfTwoAgent(Node curAgentNode, Node otherAgentNode, Agent curAgent,
			Agent otherAgent, boolean isPush) {
		/*
		 * If other agent also pushing, then get other agent parent node, and
		 * check In replan part, you need to go back the parent code to replan.
		 */
		if (isPush) {
			Node parent = curAgentNode.parent;
			if (otherAgentNode.action.actType.equals(Command.type.Push)) {
				Position otherBoxPosition = null;
				for (Box otherBox : otherAgentNode.boxes.values()) {
					otherBoxPosition = otherBox.getPosition();
					receiverBox = otherBox;
				}
				for (Box curBox : parent.boxes.values()) {
					if (curBox.getPosition().equals(otherBoxPosition)) {
						senderBox = curBox;
						return createConflict(curAgent, otherAgent, receiverBox, senderBox, curAgentNode.parent,
								ConflictType.BOX_BOX);
					}
				}
			}

			for (Box box : curAgentNode.boxes.values()) {
				if (box.getPosition().equals(otherAgentNode.getAgentPosition())
						|| box.getPosition().equals(otherAgent.getPosition())) {
					// If these agents are pushing,then it should be box-box
					// conflict
					receiverBox = box;
					/*
					 * Maybe you need the other node, I am not sure.if you use
					 * it, need add one property in Conflict. We will discuss
					 * that when you meet it.
					 */
					return createConflict(curAgent, otherAgent, receiverBox, senderBox, curAgentNode,
							ConflictType.SINGLE_AGENT_BOX);
				}
			}
		} else {
			/*
			 * If the other agent is pulling or moving ,then it is agent-box
			 * conflict
			 */
			if (!curAgentNode.action.actType.equals(Command.type.NoOp)) {
				if (curAgentNode.getAgentPosition().equals(otherAgentNode.getAgentPosition())
						|| curAgentNode.getAgentPosition().equals(otherAgent.getPosition())) {
					// If these agents are pushing,then it should be box-box
					// conflict
					for (Box box : curAgentNode.boxes.values()) {
						senderBox = box;
					}
					return createConflict(curAgent, otherAgent, receiverBox, senderBox, curAgentNode,
							ConflictType.AGENT);
				}
				for (Box box : World.getInstance().getBoxes().values()) {
					Intention inten = curAgent.getIntention();
					if (inten != null) {
						Box intenBox = inten.getBox();
						if (box.getPosition().equals(curAgentNode.getAgentPosition()) && !box.equals(intenBox)) {
							receiverBox = box;
							if (box.getColor().equals(curAgent.getColor())) {
								otherAgent = curAgent;
							}
							return createConflict(curAgent, otherAgent, receiverBox, senderBox, curAgentNode,
									ConflictType.SINGLE_AGENT_BOX);
						}
=======
	private Conflict currentPushingConflictCheck(Node curAgentNode,Node otherAgentNode,Agent curAgent,Agent otherAgent) {
		/*
		 * If other agent also pushing, then get other agent parent node, and check
		 * In replan part, you need to go back the parent code to replan.
		 */
		Node parent = curAgentNode.parent;
		/*
		 * if the other agent also pushing, then if have conflicts, then it is box-box conflict.
		 */
		if(otherAgentNode != null) {
			if(otherAgentNode.action.actType.equals(Command.type.Push)) {
				Position otherBoxPosition = null;
				for(Box otherBox: otherAgentNode.boxes.values()) {
					otherBoxPosition = otherBox.getPosition();
					receiverBox = otherBox;
				}
				for(Box curBox: parent.boxes.values()) {//check could achieve previous node or not
					if(curBox.getPosition().equals(otherBoxPosition) || curBox.getPosition().equals(otherAgent.getPosition())) {
						senderBox = curBox;
						return createConflict(curAgent,otherAgent,receiverBox,senderBox,parent.parent,ConflictType.BOX_BOX);
					}
				}
				for(Box curBox: curAgentNode.boxes.values()) {//check could achieve current node or not
					// this does not really make sense ? 
					//if(curBox.getPosition().equals(otherBoxPosition) || curBox.getPosition().equals(otherAgent.getPosition())){
					if(curBox.getPosition().equals(otherBoxPosition) /*|| curBox.getPosition().equals(otherAgent.getPosition())*/) {
						senderBox = curBox;
						return createConflict(curAgent,otherAgent,receiverBox,senderBox,curAgentNode.parent,ConflictType.BOX_BOX);
>>>>>>> refs/remotes/origin/master
					}

				}
<<<<<<< HEAD
			} else {
				if (otherAgentNode.action.actType.equals(Command.type.Push)) {
					Node otherParent = otherAgentNode.parent;

				}
				if (otherAgentNode.getAgentPosition().equals(curAgent.getPosition())) {

					return createConflict(curAgent, otherAgent, null, null, curAgentNode, ConflictType.AGENT);
				}
			}
		}
		return null;
	}

	/**
	 * Compare the next position is box or agent if current agent is pushing,
	 * next is box, then it is box-box conflict,next is agent ,then it is
	 * agent-box conflict if current agent is pulling or moving, next is box,
	 * then agent-box conflict, next is agent. the it is agent-agent conflict
	 * 
=======
				if(curAgent.getPosition().equals(otherAgentNode.getAgentPosition()) ||
						(curAgentNode.getAgentPosition().equals(otherAgent.getPosition()))) {
					return createConflict(curAgent,otherAgent,receiverBox,senderBox,curAgentNode.parent,ConflictType.AGENT);
				}
			}
		}
		for(Box box: parent.boxes.values()) {//if the previous step would like to move to a box position or other agent's next step
			for(Box otherBox:World.getInstance().getBoxes().values()) {
				if(box.getPosition().equals(otherBox.getPosition()) && (box.getId() != otherBox.getId())) {
					return createConflict(curAgent,otherAgent,box,box,curAgentNode,ConflictType.BOX_BOX);
				}
			}
			
			if(box.getPosition().equals(otherAgent.getPosition()) || box.getPosition().equals(otherAgentNode.getAgentPosition())) {
				return createConflict(curAgent,otherAgent,box,box,parent,ConflictType.SINGLE_AGENT_BOX);
			}
		}
		
		for(Box box: curAgentNode.boxes.values()) {//if current step would like to move to a box position or other agent's next step
			for(Box otherBox : World.getInstance().getBoxes().values()) {
				if(box.getPosition().equals(otherBox.getPosition()) && (box.getId() != otherBox.getId())) {
					return createConflict(curAgent,otherAgent,otherBox,box,curAgentNode,ConflictType.BOX_BOX);
				}
			}
			if(box.getPosition().equals(otherAgent.getPosition()) || box.getPosition().equals(otherAgentNode.getAgentPosition())) {
				return createConflict(curAgent,otherAgent,box,box,curAgentNode,ConflictType.SINGLE_AGENT_BOX);
			}
		}
		return null;
	}
	
	
	/**
	 * Check current pulling/Moving node conflict
	 * 		if current node position = other agent position ========>Agent-Agent
	 *                               = other agent node position ====>Agent-Agent
	 *                               = other agent box/ own not-intention box =====> Agent-box
	 *      if current agent position = other agent node position ====>Agent-Agent
>>>>>>> refs/remotes/origin/master
	 * @param curAgentNode
	 * @param curAgent
	 * @param otherAgent
	 * @return
	 */
<<<<<<< HEAD
	private Conflict compareNextNodeWithBoxOrAgent(Node curAgentNode, Agent curAgent, Agent otherAgent) {
		if (curAgentNode.action.actType.equals(Command.type.Push)) {
			Node parent = curAgentNode.parent;
			for (Box box : parent.boxes.values()) {
				if (box.getPosition().equals(otherAgent.getPosition())) {
					return createConflict(curAgent, otherAgent, box, box, parent, ConflictType.SINGLE_AGENT_BOX);
				}
			}

			for (Box box : World.getInstance().getBoxes().values()) {
				// check if is it current agent non intention box
				Position movingToPosi = new Position(parent.agentRow, parent.agentCol);
				if (box.getColor().equals(otherAgent.getColor()) && box.getPosition().equals(movingToPosi)) {
					receiverBox = box;
					return createConflict(curAgent, otherAgent, receiverBox, box, parent, ConflictType.BOX_BOX);
				}
			}
		} else {// it is moving or pulling
			if ((curAgentNode.getAgentPosition().equals(otherAgent.getPosition()))) {
=======
	private Conflict currentPullingOrMoveConflictCheck(Node curAgentNode,Node otherAgentNode,Agent curAgent,Agent otherAgent) {
		if (curAgentNode.getAgentPosition().equals(otherAgent.getPosition()) || 
				curAgent.getPosition().equals(otherAgent.getPosition())) {
			Box receiverBox = null;
			for (Box box : curAgentNode.boxes.values()) {
				receiverBox = box;
			}
			return createConflict(curAgent,otherAgent,receiverBox,null,curAgentNode,ConflictType.AGENT);
		}
		
		if(otherAgentNode != null) {
			if (curAgentNode.getAgentPosition().equals(otherAgentNode.getAgentPosition()) || 
					curAgent.getPosition().equals(otherAgentNode.getAgentPosition())) {
>>>>>>> refs/remotes/origin/master
				Box receiverBox = null;
				for (Box box : curAgentNode.boxes.values()) {
					receiverBox = box;
				}
<<<<<<< HEAD
				return createConflict(curAgent, otherAgent, receiverBox, null, curAgentNode, ConflictType.AGENT);
			}

			for (Box box : World.getInstance().getBoxes().values()) {
				// check if is it current agent non intention box
				if (box.getColor().equals(otherAgent.getColor())
						&& box.getPosition().equals(curAgentNode.getAgentPosition())) {
					receiverBox = box;
					return createConflict(curAgent, otherAgent, receiverBox, box, curAgentNode,
							ConflictType.SINGLE_AGENT_BOX);
				}
			}

			Intention intention = curAgent.getIntention();
			if (intention != null) {
				Box intenbox = curAgent.getIntention().getBox();
				Position nextMovePosition = new Position(curAgentNode.agentRow, curAgentNode.agentCol);
				for (Box boxes : World.getInstance().getBoxes().values()) {
					if (boxes.getPosition().equals(nextMovePosition) && !boxes.equals(intenbox)) {
						return createConflict(curAgent, otherAgent, boxes, null, curAgentNode,
								ConflictType.SINGLE_AGENT_BOX);
					}
				}
			}
		}

=======
				return createConflict(curAgent,otherAgent,receiverBox,null,curAgentNode,ConflictType.AGENT);
			}
		}
		
		for(Box box:World.getInstance().getBoxes().values()) {
			Intention inten = curAgent.getIntention();
			if(inten != null) {
				Box intenBox = inten.getBox();
				if(box.getPosition().equals(curAgentNode.getAgentPosition()) && !box.equals(intenBox)) {
					receiverBox = box;
					/*
					 * if the conflict box is the same color of current agent, then sender and receiver should be the same.
					 */
					if(box.getColor().equals(curAgent.getColor())) {
						otherAgent = curAgent;
					}
					return createConflict(curAgent,otherAgent,receiverBox,senderBox,curAgentNode,ConflictType.SINGLE_AGENT_BOX);
				}
			}	
		}
		
>>>>>>> refs/remotes/origin/master
		return null;
	}

}