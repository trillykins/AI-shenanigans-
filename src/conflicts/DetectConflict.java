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
	
	public Conflict checkConflict() {
		Conflict conflict = null;

		if (World.getInstance().getAgents().size() == 1) {
			conflict =  detectSignalAgentConflict();
		} else { /*Dealing with MA situation*/
			conflict =  detectMultiAgengConflict();
		}
		return conflict;
	}
	
	private Conflict detectSignalAgentConflict() {
		// System.exit(0); // TODO MA debug purpose
		Agent a1 = World.getInstance().getAgents().get(0);
		/*
		* as there is no other agents that can be in a1's way, the only
		* obstacle a1 can bump into is a box
		*/
		Intention intention = a1.getIntention();
		if (intention != null) {
			Node next = a1.getPlan().get(a1.getStepInPlan());
			Box intentionBox = intention.getBox();
			for (Box box : World.getInstance().getBoxes().values()) {
				if (!box.equals(intentionBox)) {
					if (box.getPosition().equals(next.getPosition())) {
						if (next.action.actType.equals(Command.type.Move)  || next.action.actType.equals(Command.type.Pull)) {
							return createConflict(a1,null,box,null,next,ConflictType.SINGLE_AGENT_BOX);
						}
					}
					/*Want to check if the next node (to the box we are "carring") is a box)*/
					else {
						for (Box nextNodeBox : next.boxes.values()) {
							if(box.getPosition().equals(nextNodeBox.getPosition()) && next.action.actType.equals(Command.type.Push)){
								return createConflict(a1,null,box,null,next,ConflictType.BOX_BOX);
							}
						}	

					}

				}
			}
		}
		return null;
	}
	
	private Conflict detectMultiAgengConflict() {
		for (Agent curAgent : World.getInstance().getAgents().values()) {
			/*We don't want to look further if the currAgents plan is null or the size is smaller than curr index*/
			if (curAgent.getPlan() == null || curAgent.getPlan().size() == 0
					|| isFinishSolution(curAgent)) {
				continue;
			}
			Conflict conflict = null;
			Node curAgentNode = curAgent.getPlan().get(curAgent.getStepInPlan());
			for (Agent otherAgent : World.getInstance().getAgents().values()) {
				if (otherAgent.getId() != curAgent.getId()) {
					List<Node> planForOtherAgent = otherAgent.getPlan();
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
						conflict = currentPullingOrMoveConflictCheck(curAgentNode,curAgent,otherAgent);
						if(conflict != null) {
							return conflict;
						}
					}
				}
			}
		}
		return null;
	}
	
	
	/**
	 * If current agent already finish the solution, then do not need to check the solution of this agent
	 * becasue it could only be a conflict agent for other agent,other agent would not conflict it.
	 * @param agent
	 * @return
	 */
	private boolean isFinishSolution(Agent agent) {
		List<Node> solution = agent.getPlan();
		if(solution != null && solution.size() == 1) {
			if(solution.get(0).action.actType.equals(Command.type.NoOp)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create a conflict
	 * @param sender
	 * @param receiver
	 * @param receBox
	 * @param senderBox
	 * @param currNode
	 * @param type
	 * @return
	 */
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
	 * @param curAgentNode
	 * @param otherAgentNode
	 * @param curAgent
	 * @param otherAgent
	 * @param isPush
	 * @return
	 */
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
					if(curBox.getPosition().equals(otherBoxPosition) || curBox.getPosition().equals(otherAgent.getPosition())) {
						senderBox = curBox;
						return createConflict(curAgent,otherAgent,receiverBox,senderBox,curAgentNode.parent,ConflictType.BOX_BOX);
					}
				}
				if(curAgent.getPosition().equals(otherAgentNode.getPosition()) ||
						(curAgentNode.getPosition().equals(otherAgent.getPosition()))) {
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
			
			if(box.getPosition().equals(otherAgent.getPosition()) || box.getPosition().equals(otherAgentNode.getPosition())) {
				return createConflict(curAgent,otherAgent,box,box,parent,ConflictType.SINGLE_AGENT_BOX);
			}
		}
		
		for(Box box: curAgentNode.boxes.values()) {//if current step would like to move to a box position or other agent's next step
			for(Box otherBox:World.getInstance().getBoxes().values()) {
				if(box.getPosition().equals(otherBox.getPosition()) && (box.getId() != otherBox.getId())) {
					return createConflict(curAgent,otherAgent,box,box,curAgentNode,ConflictType.BOX_BOX);
				}
			}
			if(box.getPosition().equals(otherAgent.getPosition()) || box.getPosition().equals(otherAgentNode.getPosition())) {
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
	 * @param curAgentNode
	 * @param curAgent
	 * @param otherAgent
	 * @return
	 */
	private Conflict currentPullingOrMoveConflictCheck(Node curAgentNode,Agent curAgent,Agent otherAgent) {
		if (curAgentNode.getPosition().equals(otherAgent.getPosition()) || 
				curAgentNode.getPosition().equals(otherAgent.getPosition())) {
			Box receiverBox = null;
			for (Box box : curAgentNode.boxes.values()) {
				receiverBox = box;
			}
			return createConflict(curAgent,otherAgent,receiverBox,null,curAgentNode,ConflictType.AGENT);
		}
		
		for(Box box:World.getInstance().getBoxes().values()) {
			Intention inten = curAgent.getIntention();
			if(inten != null) {
				Box intenBox = inten.getBox();
				if(box.getPosition().equals(curAgentNode.getPosition()) && !box.equals(intenBox)) {
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
		
		return null;
	}

}