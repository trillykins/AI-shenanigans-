package atoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

	public Agent(int id, String color, Position pos, int priority) {
		this(id, Utils.determineColor(color), pos, priority);
	}

	public Agent(int id, Color color, Position pos, int priority) {
		this.id = id;
		this.col = color;
		this.pos = pos;
		this.priority = priority;
	}

	public void generateInitialState() {
		this.initialState = new Node(null, id);
		this.initialState.agentColor = col;
		this.initialState.agentRow = pos.getX();
		this.initialState.agentCol = pos.getY();
		this.initialState.boxes = new HashMap<Integer, Box>(0);
		this.initialState.goals = new HashMap<Integer, Goal>(0);
		this.initialState.walls = World.getInstance().getWalls();
		this.desires = new HashSet<>(0);
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
	 * This method also considder the closest box that can fullfill the goal
	 */
	public boolean generateIntention() {
		if (desires.isEmpty())
			return false;
		Desire bestDesire = null;
		Box bestBox = null;
		int bestTotal = Integer.MAX_VALUE;
		int bestGoalPriority = 0;
		Box closestBox = null;
		for (Desire des : desires) {
			Goal goal = des.getBelief().getGoal();
			int goalPriority = goal.getPriority();
			// compute distance from agent to the closest box.
			List<Object> result = findClosestBox(goal);
			int costOfClosestBoxToGoal = (int) result.get(0);
			closestBox = (Box) result.get(1);
			int costOfAgentToClosestBox = Integer.MAX_VALUE;
			if(closestBox != null) {
				 costOfAgentToClosestBox = Utils.manhattenDistance(pos, closestBox.getPosition());
				 costOfClosestBoxToGoal = Integer.MAX_VALUE;
			}
			int currTotal = goalPriority + costOfClosestBoxToGoal + costOfAgentToClosestBox;
			/*
			 * we are looking for the smallest value possible, the optimal would
			 * be a very close goal, which have 0 occupied neighbours.
			 */
			if (bestTotal > currTotal) {
				bestGoalPriority = goalPriority;
				bestTotal = currTotal;
				bestDesire = des;
				bestBox = closestBox;
			} else if (bestTotal == currTotal) {
				/*
				 * if two goal totals are equal, we look at how many occupied
				 * neighbors they have
				 */
				if (bestGoalPriority > goalPriority) {
					bestGoalPriority = goalPriority;
					bestTotal = currTotal;
					bestDesire = des;
					bestBox = closestBox;
				}
			}
		}
		intention = (bestDesire != null && bestBox != null ? new Intention(bestDesire, bestBox) : null);
		return intention != null;
	}

	public List<Object> findClosestBox(Goal goal) {
		List<Object> result = new ArrayList<Object>(0);
		Box b = null;
		World world = World.getInstance();
		Integer smallestDistance = Integer.MAX_VALUE;
		for (Box box : world.getBoxes().values()) {
			if (!box.isOnGoal() && Character.toLowerCase(box.getLetter()) == goal.getLetter()
//					&& (world.getBoxesInGoals().get(box.getId()) == null)
					) {
				int currDistance = Utils.manhattenDistance(box.getPosition(), goal.getPosition());
				if (smallestDistance > currDistance) {
					smallestDistance = currDistance;
					b = box;
				}
			}
		}
		result.add(smallestDistance);
		result.add(b);
		return result;
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
		builder.append("Agent [id=").append(id).append(", color=").append(col).append(", pos=").append(pos)
				.append(", priority=").append(priority).append("]");
		return builder.toString();
	}
}
