package atoms;

import FIPA.IMessage;
import FIPA.Message;
import FIPA.MessageType;
import searchclient.Command;
import searchclient.Node;
import searchclient.SearchClient;
import searchclient.Utils;

public class Agent implements IMessage{
	private int id;
	private Color col;
	private Position pos;
	private int priority;
	public Node initialState = null;
	
	public char[][] boxes = null;
	
	public Agent(int id, String color, Position pos) {
		this.id = id;
		this.col = Utils.determineColor(color);
		this.pos = pos;
	}

	public String act() {
		return Command.every[1].toString();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Color getColor() {
		return col;
	}

	public void setColor(Color col) {
		this.col = col;
	}

	public Position getPosition() {
		return pos;
	}

	public void setPosition(Position pos) {
		this.pos = pos;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((col == null) ? 0 : col.hashCode());
		result = prime * result + id;
//		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		result = prime * result + priority;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Agent))
			return false;
		Agent other = (Agent) obj;
		if (col != other.col)
			return false;
		if (id != other.id)
			return false;
		if (pos == null) {
			if (other.pos != null)
				return false;
		} else if (!pos.equals(other.pos))
			return false;
		if (priority != other.priority)
			return false;
		return true;
	}

	public Node getInitialState() {
		return initialState;
	}

	public void setInitialState(Node initialState) {
		boxes = new char[SearchClient.MAX_ROW][SearchClient.MAX_COLUMN];
		this.initialState = initialState;
	}
	
	public boolean isGoalState() {
		for (int row = 1; row < SearchClient.MAX_ROW - 1; row++) {
			for (int col = 1; col < SearchClient.MAX_COLUMN - 1; col++) {
				char g = initialState.goals[row][col];
				char b = Character.toLowerCase(boxes[row][col]);
				if (g > 0 && b != g) {
					return false;
				}
			}
		}
		System.err.println("found goal state");
		return true;
	}

	@Override
	public Message createMessage(Agent receiver, MessageType type, String content) {
		return new Message(this, receiver, type, content);
	}

	@Override
	public String receiveMessage(Message message) {
		return message.getContent();
	}

}
