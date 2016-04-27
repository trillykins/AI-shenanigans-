package conflicts;

import searchclient.Node;
import atoms.Agent;
import atoms.Box;

public class Conflict {
	private ConflictType conflictType;
	private Agent sender;
	private Agent receiver;
	private Node node;
	private Box box;
	
	public enum ConflictType {
		Agent, Box_Box, Agent_Box,
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Conflict [conflictType=").append(conflictType).append(", sender=").append(sender)
		.append(", receiver=").append(receiver).append(", node=").append(node).append("]");
		return builder.toString();
	}
}