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
		desires = new HashSet<Desire>(0);
		for (Belief belief : World.getInstance().getBeliefs()) {
			Goal g = belief.getGoal();
			for (Box b : World.getInstance().getBoxes().values()) {
				if (Character.toLowerCase(b.getLetter()) == g.getLetter()) {
					if (col.equals(b.getColor())) {
						desires.add(new Desire(belief, this));
					}
				}
			}
		}
		return desires.size() == 0 ? false : true;
	}

	/*
	 * Generate intention finds intentions based on cost and goal priority goal
	 * priority reflects how many occupied surrounding spaces a goal have maybe
	 * This method also consider the closest box that can fulfill the goal
	 */
	public boolean generateIntention() {
		if (desires.isEmpty())
			return false;
		Desire bestDesire = null;
		Box bestBox = null;
		int bestTotal = Integer.MAX_VALUE;
		int bestGoalPriority = 0;
		Box box = null;
		for (Desire des : desires) {
			Goal goal = des.getBelief().getGoal();
			int goalPriority = goal.getPriority();
			int cost = Utils.manhattenDistance(des.getAgent().getPosition(), goal.getPosition());

			// compute distance from agent to the closest box.

			int costOfClosestBox = findCostOfClosestBox(goal);
			box = findClosestBox(goal);
			int currTotal = goalPriority + cost + costOfClosestBox;
			/*
			 * we are looking for the smallest value possible, the optimal would
			 * be a very close goal, which have 0 occupied neighbors.
			 */
			if (bestTotal > currTotal) {
				bestGoalPriority = goalPriority;
				bestTotal = currTotal;
				bestDesire = des;
				bestBox = box;
			} else if (bestTotal == currTotal) {
				/*
				 * if two goal totals are equal, we look at how many occupied
				 * neighbors they have
				 */
				if (bestGoalPriority > goalPriority) {
					bestGoalPriority = goalPriority;
					bestTotal = currTotal;
					bestDesire = des;
					bestBox = box;
				}
			}
		}
		System.err.println("Best intention = " + bestDesire + ", " + bestBox);
		intention = new Intention(bestDesire, bestBox);
		return true;
	}

	public int findCostOfClosestBox(Goal goal) {
		World world = World.getInstance();
		int smallestDistance = Integer.MAX_VALUE;
		for (Box box : world.getBoxes().values()) {
			if (Character.toLowerCase(box.getLetter()) == goal.getLetter()
					&& (world.getBoxesInGoals().get(box.getId()) == null)) {
				int currDistance = Utils.manhattenDistance(box.getPosition(), pos);
				if (smallestDistance > currDistance) {
					smallestDistance = currDistance;
				}
			}
		}
		return smallestDistance;
	}

	public Box findClosestBox(Goal goal) {
		Box b = null;
		World world = World.getInstance();
		int smallestDistance = Integer.MAX_VALUE;
		for (Box box : world.getBoxes().values()) {
			if (Character.toLowerCase(box.getLetter()) == goal.getLetter()
					&& (world.getBoxesInGoals().get(box.getId()) == null)) {
				int currDistance = Utils.manhattenDistance(box.getPosition(), pos);
				if (smallestDistance > currDistance) {
					smallestDistance = currDistance;
					b = box;
				}
			}
		}
		return b;
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
		builder.append("Agent [id=").append(id).append(", colour=").append(col).append(", ").append(pos).append(", priority=")
				.append(priority).append("]");
		return builder.toString();
	}
}
