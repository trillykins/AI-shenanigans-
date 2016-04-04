package searchclient;


import FIPA.IMessage;
import FIPA.Message;
import FIPA.MessageType;

public class Agent implements IMessage{
	private int id;
	private Color col;
	public Node initialState = null;
	
	private int currentCol;
	private int currentRow;
	
	public Agent(int id, String color) {
		this.id = id;
		this.col = Utils.determineColor(color);
	}

	public String act() {
		return Command.every[1].toString();
	}

	public void SetInitialState(Node initial) {
		initialState = initial;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Color getCol() {
		return col;
	}

	public void setCol(Color col) {
		this.col = col;
	}

	public Node getInitialState() {
		return initialState;
	}

	public void setInitialState(Node initialState) {
		this.initialState = initialState;
	}
	
	public int getCurrentCol() {
		return currentCol;
	}

	public void setCurrentCol(int currentCol) {
		this.currentCol = currentCol;
	}

	public int getCurrentRow() {
		return currentRow;
	}

	public void setCurrentRow(int currentRow) {
		this.currentRow = currentRow;
	}

	@Override
	public Message createMessage(Agent receiver,
			MessageType type, String content) {
		Message mess = new Message();
		mess.setSender(this);
		mess.setReceiver(receiver);
		mess.setType(type);
		mess.setContent(content);
		
		return mess;
	}

	@Override
	public String receiveMessage(Message message) {
		String act = message.getContent();
		return act;
	}

}
