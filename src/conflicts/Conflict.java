package conflicts;

import searchclient.Node;
import atoms.Agent;

public class Conflict {
	
	private ConflictType conflictType;
	
	private Agent sender;
	
	private Agent receiver;
	
	private Node node;
	
	public static enum ConflictType {
		Agent, Box,
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
	
}
