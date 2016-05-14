package atoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import FIPA.IMessage;
import FIPA.Message;
import FIPA.MessageType;
import analysis.LevelAnalysis;
import bdi.Belief;
import bdi.Desire;
import bdi.Intention;
import searchclient.Node;
import utils.Utils;

public class Agent implements IMessage {
	private int id;
	private Color color;
	private Position position;
	private int priority;
	private List<Desire> desires;
	private Intention intention;
	public Node initialState = null;
	private int stepInPlan;
	private List<Node> plan;
	private List<Integer> unreachableBoxIds;
	private List<Integer> previouslyMovedBoxLocations;

	public Agent(int id, String color, Position pos, int priority) {
		this(id, Utils.determineColor(color), pos, priority);
	}

	public Agent(int id, Color color, Position pos, int priority) {
		this.id = id;
		this.color = color;
		this.position = pos;
		this.priority = priority;
		this.stepInPlan = 0;
		this.plan = new ArrayList<>(0);
		this.unreachableBoxIds = new ArrayList<>(0);
		this.previouslyMovedBoxLocations = new ArrayList<>(0);
	}

	public Agent(Agent agent) {
		this.id = agent.getId();
		this.color = agent.getColor();
		this.position = agent.getPosition();
		this.priority = agent.getPriority();
		this.desires = agent.getDesires();
		this.intention = agent.getIntention();
		this.initialState = agent.initialState;
		this.stepInPlan = agent.getStepInPlan();
		this.unreachableBoxIds = agent.unreachableBoxIds;
		this.previouslyMovedBoxLocations = agent.previouslyMovedBoxLocations;
	}

	public void generateInitialState() {
		this.initialState = new Node(null, id);
		this.initialState.agentColor = color;
		this.initialState.agentRow = position.getX();
		this.initialState.agentCol = position.getY();
		this.initialState.boxes = new HashMap<Integer, Box>(0);
		this.initialState.goals = new HashMap<Integer, Goal>(0);
		this.initialState.walls = World.getInstance().getWalls();
		this.desires = new ArrayList<>(0);
		// this.unreachableBoxIds = new ArrayList<Integer>(0);
	}

	public List<Integer> getPreviouslyMovedBoxLocations() {
		return previouslyMovedBoxLocations;
	}

	public void addPreviouslyMovedBoxLocations(Integer id) {
		if (!this.previouslyMovedBoxLocations.contains(id))
			this.previouslyMovedBoxLocations.add(id);
	}

	public void addUnreachableBoxId(int boxId) {
		this.unreachableBoxIds.add(boxId);
	}

	public List<Integer> getUnreachableBoxIds() {
		return unreachableBoxIds;
	}

	public List<Node> getPlan() {
		return plan;
	}

	public void setPlan(List<Node> plan) {
		this.plan = plan;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color col) {
		this.color = col;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position pos) {
		this.position = pos;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Intention getIntention() {
		return intention;
	}

	public void setIntention(Intention intention) {
		this.intention = intention;
	}

	public int getStepInPlan() {
		return stepInPlan;
	}

	public void setStepInPlan(int stepInPlan) {
		this.stepInPlan = stepInPlan;
	}

	public List<Desire> getDesires() {
		return desires;
	}

	public void setDesires(List<Desire> desires) {
		this.desires = desires;
	}

	public boolean generateDesires() {
		desires = new ArrayList<>(0);
		for (int i = 0; i < World.getInstance().getBeliefs().size(); i++) {
			Belief belief = World.getInstance().getBeliefs().get(i);
			for (Box b : World.getInstance().getBoxes().values()) {
				if (Character.toLowerCase(b.getLetter()) == belief.getGoal().getLetter()
						&& !unreachableBoxIds.contains(b.getId())) {
					if (color.equals(b.getColor()) && !desires.contains(new Desire(belief, this))) {
						desires.add(new Desire(belief, this));
					}
				}
			}
		}
		return desires.size() == 0 ? false : true;
	}

	/**
	 * Generate intention finds intentions based on cost and goal priority goal
	 * priority reflects how many occupied surrounding spaces a goal have maybe
	 * This method also consider the closest box that can fulfill the goal
	 **/
	public boolean generateIntention() {
		if (World.getInstance().getAgents().size() == 1) {
			return generationIntentionSA();
		} else {
			return generationIntentionMA();
		}
	}

	public boolean generationIntentionSA() {
		// Recalculate goal priority after goal has been solved
		if (desires.isEmpty())
			return false;
		Desire bestDesire = null;
		Box bestBox = null;
		for (int i = 0; i < desires.size(); i++) {
			Desire des = desires.get(i);
			Goal goal = des.getBelief().getGoal();

			/* calculate goalPriority : based on world elements */
			LevelAnalysis la = new LevelAnalysis();
			List<Position> fields = la.surroundingFields(goal.getPosition());
			int numberOfOccupiedSpaces = 0;
			for (int j = 0; j < fields.size(); j++) {
				if (la.isSpaceWallOrSolvedGoal(fields.get(j))) {
					numberOfOccupiedSpaces++;
				} else if (la.isSpaceGoal(fields.get(j))) {
					numberOfOccupiedSpaces += 3;
				} else {
					numberOfOccupiedSpaces += 10;
				}

			}
			goal.setPriority(numberOfOccupiedSpaces);
		}

		int boxDistance = Integer.MAX_VALUE;
		int desirePriority = Integer.MAX_VALUE;
		bestDesire = null;
		for (Desire d : desires) {
			if (d.getBelief().getGoal().getPriority() < desirePriority) {
				desirePriority = d.getBelief().getGoal().getPriority();
				bestDesire = d;
			}
		}
		for (Box b : World.getInstance().getBoxes().values()) {
			if (bestDesire.getBelief().getGoal().getLetter() == Character.toLowerCase(b.getLetter())
					&& !unreachableBoxIds.contains(b.getId())) {
				if (boxDistance > Utils.manhattenDistance(bestDesire.getBelief().getGoal().getPosition(),
						b.getPosition())) {
					boxDistance = Utils.manhattenDistance(bestDesire.getBelief().getGoal().getPosition(),
							b.getPosition());
					bestBox = b;
				}
			}
		}
		intention = (bestDesire != null && bestBox != null ? new Intention(bestDesire, bestBox) : null);
		return intention != null;
	}

	public boolean generationIntentionMA() {

		// Recalculate goal priority after goal has been solved
		if (desires.isEmpty())
			return false;
		Desire bestDesire = null;
		Box bestBox = null;
		int bestTotal = Integer.MAX_VALUE;
		int bestGoalPriority = 0;
		Box closestBox = null;
		for (int i = 0; i < desires.size(); i++) {
			Desire des = desires.get(i);
			Goal goal = des.getBelief().getGoal();

			/* calculate goalPriority : based on world elements */
			goal.setPriority(new LevelAnalysis().calculateGoalPriority(goal));
			/*
			 * if a goal has been solved we do not want to consider it in our
			 * calculations
			 */
			if (goal.isSolved())
				continue;
			int goalPriority = goal.getPriority();
			/* compute distance from agent to the closest box. */
			List<Object> result = findClosestBox(goal);
			int costOfClosestBoxToGoal = (int) result.get(0);
			closestBox = (Box) result.get(1);
			int costOfAgentToClosestBox = 0;
			costOfAgentToClosestBox = Utils.manhattenDistance(position, closestBox.getPosition());

			/* calculate current number of free spaces surrounding the goal */
			LevelAnalysis levelAnalysis = new LevelAnalysis();
			int numberOfFreeSpacesForGoal = levelAnalysis.calculateGoalPriority(goal);

			int currTotal = goalPriority + numberOfFreeSpacesForGoal + costOfClosestBoxToGoal + costOfAgentToClosestBox
					+ numberOfFreeSpacesForGoal;

			System.err.println("Goal " + goal.getLetter() + " currTotal " + currTotal + "\tgoalP: " + goalPriority
					+ " costOfClosestBoxToG: " + costOfClosestBoxToGoal + "costOfAgentToClosestB: "
					+ costOfAgentToClosestBox + "numberOfFreeSpacesForGoal: " + numberOfFreeSpacesForGoal);

			/*
			 * we are looking for the smallest value possible, the optimal would
			 * be a very close goal, which have 0 occupied neighbors.
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
			if (!unreachableBoxIds.contains(box.getId())) {
				if (!box.isOnGoal() && Character.toLowerCase(box.getLetter()) == goal.getLetter()) {
					int currDistance = Utils.manhattenDistance(box.getPosition(), goal.getPosition());
					if (smallestDistance > currDistance) {
						smallestDistance = currDistance;
						b = box;
					}
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
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Agent [id=").append(id).append(", col=").append(color).append(", pos=").append(position)
				.append(", priority=").append(priority).append(", desires=").append(desires).append(", intention=")
				.append(intention).append(", initialState=").append(initialState).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((desires == null) ? 0 : desires.hashCode());
		result = prime * result + id;
		result = prime * result + ((initialState == null) ? 0 : initialState.hashCode());
		result = prime * result + ((intention == null) ? 0 : intention.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
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
		if (color != other.color)
			return false;
		if (desires == null) {
			if (other.desires != null)
				return false;
		} else if (!desires.equals(other.desires))
			return false;
		if (id != other.id)
			return false;
		if (initialState == null) {
			if (other.initialState != null)
				return false;
		} else if (!initialState.equals(other.initialState))
			return false;
		if (intention == null) {
			if (other.intention != null)
				return false;
		} else if (!intention.equals(other.intention))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (priority != other.priority)
			return false;
		return true;
	}
}