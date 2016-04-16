package atoms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import FIPA.IMessage;
import FIPA.Message;
import FIPA.MessageType;
import bdi.Belief;
import bdi.Desire;
import bdi.Intention;
import searchclient.Command;
import searchclient.Node;
import searchclient.Utils;

public class Agent implements IMessage {
	private int id;
	private Color col;
	private Position pos;
	private int priority;
	private Set<Desire> desires;
	private Intention intention;
	public Node initialState = null;

	public Agent(int id, String color, Position pos) {
		this(id, Utils.determineColor(color), pos);
	}

	public Agent(int id, Color color, Position pos) {
		this.id = id;
		this.col = color;
		this.pos = pos;
		this.desires = new HashSet<Desire>(0);
	}

	public void generateInitialState() {
		this.initialState = new Node(null, id);
		this.initialState.agentRow = pos.getX();
		this.initialState.agentCol = pos.getY();
		this.initialState.boxes = new HashMap<Integer, Box>(0);
		this.initialState.goals = new HashMap<Integer, Goal>(0);
		this.initialState.walls = World.getInstance().getWalls();
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

	public Set<Desire> getDesires() {
		return desires;
	}

	public void setDesires(Set<Desire> desires) {
		this.desires = desires;
	}

	public Intention getIntention() {
		return intention;
	}

	public void setIntention(Intention intention) {
		this.intention = intention;
	}

	public boolean generateDesires() {
		desires.clear();
		for (Belief belief : World.getInstance().getBeliefs()) {
			Goal g = belief.getGoal();
			for (Integer boxId : World.getInstance().getBoxes().keySet()) {
				Box b = World.getInstance().getBoxes().get(boxId);
				if (Character.toLowerCase(b.getLetter()) == g.getLetter()) {
					if (col.equals(b.getColor())) {
						desires.add(new Desire(belief, this));
					}
				}
			}
		}
		return desires.size() == 0 ? false : true;
	}

	// TODO: THEA
	public boolean generateIntention() {
		for (Desire des : desires) {
			System.err.println(des.toString());
		}
		if (!desires.isEmpty()){
			intention = new Intention(desires.iterator().next());
			return true;
		}
		else 
			return false;
	}

	public Agent clone() {
		Agent newAgent = new Agent(this.getId(), this.getColor(), this.getPosition());
		newAgent.setDesires(this.getDesires());
		newAgent.setIntention(this.getIntention());
		return newAgent;
	}

	@Override
	public Message createMessage(Agent receiver, MessageType type, String content) {
		return new Message(this, receiver, type, content);
	}

	@Override
	public String receiveMessage(Message message) {
		return message.getContent();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((col == null) ? 0 : col.hashCode());
		result = prime * result + id;
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Agent [id=").append(id).append(", col=").append(col).append(", pos=").append(pos)
				.append(", priority=").append(priority).append("]");
		return builder.toString();
	}
}
