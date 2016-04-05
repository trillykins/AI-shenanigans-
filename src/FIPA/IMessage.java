package FIPA;

import atoms.Agent;

public interface IMessage {

	public Message createMessage(Agent receiver, MessageType type, String content);

	public String receiveMessage(Message message);

}
