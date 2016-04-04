package FIPA;

import searchclient.Agent;

public class Message {
	
	private MessageType type;
	private Agent sender;
	private Agent receiver;
	private String content;
	
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public MessageType getType() {
		return type;
	}
	public void setType(MessageType type) {
		this.type = type;
	}

}
