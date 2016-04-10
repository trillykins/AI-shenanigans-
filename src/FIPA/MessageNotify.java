package FIPA;

import java.util.PriorityQueue;

import atoms.Agent;
import searchclient.Command;
import searchclient.Node;

public class MessageNotify implements Runnable {//

	private Node newAction;

	public PriorityQueue<Message> messageQueue = new PriorityQueue<Message>();

	public MessageNotify(Message message) {
		messageQueue.add(message);
	}

	public Node getNewAction() {
		return newAction;
	}

	public void setNewAction(Node newAction) {
		this.newAction = newAction;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Message message = messageQueue.poll();
		if (message != null) {
			Agent receiver = message.getReceiver();
			System.err.println("Agent color " + receiver.getPosition().getY() + "has received the message");
			System.err.println("Request agent color " + receiver.getPosition().getY() + " to " + message.getContent());

			// Test code
			// Should decide the logical of the replanning (How to update the
			// current solution)
			newAction = new Node(null, receiver.getId());
			int col = message.getReceiver().getPosition().getY();
			int row = message.getReceiver().getPosition().getY();
			Command newCommand = new Command(Command.dir.E);
			newAction.action = newCommand;
			if (newCommand.dir1.equals(Command.dir.E)) {
				col -= 1;
			} else if (newCommand.dir1.equals(Command.dir.W)) {
				col += 1;
			} else if (newCommand.dir1.equals(Command.dir.S)) {
				row -= 1;
			} else if (newCommand.dir1.equals(Command.dir.N)) {
				row += 1;
			}
			newAction.agentCol = col;
			newAction.agentRow = row;
			newAction.boxes = receiver.initialState.boxes;
			newAction.goals = receiver.initialState.goals;
			receiver.receiveMessage(message);
		}
	}
}