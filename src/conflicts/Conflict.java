package conflicts;

import java.util.LinkedList;
import java.util.List;

import atoms.Agent;
import atoms.Box;
import atoms.Color;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import heuristics.AStar;
import heuristics.Greedy;
import heuristics.WeightedAStar;
import searchclient.Command;
import searchclient.Node;
import searchclient.Search;
import searchclient.Search.SearchType;
import strategies.Strategy;
import strategies.StrategyBFS;
import strategies.StrategyBestFirst;

public class Conflict {
	private ConflictType conflictType;
	private Agent sender;
	private Agent receiver;
	private Node node;
	private Box receiverBox;
	private Box senderBox;

	public enum ConflictType {
		AGENT, BOX_BOX, SINGLE_AGENT_BOX,
	}

	public Box getSenderBox() {
		return senderBox;
	}

	public void setSenderBox(Box senderBox) {
		this.senderBox = senderBox;
	}

	public Box getReceiverBox() {
		return receiverBox;
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

	public void setReceiverBox(Box box) {
		this.receiverBox = box;
	}

	public void solveAgentOnBox() {
		// System.err.println("Theres is a agent on box conflict!");

	}

	public void solveBoxOnBoxMA(Conflict conflict, int index, List<List<Node>> allSolutions) {
		/*
		 * Here we look at the agent who's box we marked as a conflict box (in
		 * conflict type)
		 */
		/*
		 * In general it is a problem selecting the agent based on priority.
		 * Here we need to consider narrow corridors or free fields or
		 * something. See MAsimple9 where the wrong agent is selected to move
		 * (lowest priority)
		 */

		/* testing */

		Agent agentToMove = null, agentToStay = null;
		Box agentToMoveBox = null;
		if (conflict.senderBox != null) {
			agentToMove = conflict.getSender();
			agentToMoveBox = conflict.getSenderBox();
			agentToStay = conflict.getReceiver();
		} else {
			agentToMove = conflict.getReceiver();
			agentToMoveBox = conflict.getReceiverBox();
			agentToStay = conflict.getSender();
		}
		/* First we find the coordinate of where to put a new goal */
		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(new Position(agentToStay.getPosition()));
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();

		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		s.setPlanForAgentToStay(updatePlan(agentToStay.getId(), index));
		List<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_AWAY);
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));

		/*
		 * We create a new goal, for which we want the agent to move the
		 * blocking box to
		 */
		int noGoals = World.getInstance().getGoals().size();
		if (newPlanAgentToMove.size() - 1 >= 0 && newPlanAgentToMove.size() - 1 >= 0) {

			Position newGoalPos = new Position(newPlanAgentToMove.get(newPlanAgentToMove.size() - 1).agentRow,
					newPlanAgentToMove.get(newPlanAgentToMove.size() - 1).agentCol);
			char goalChar = Character.toLowerCase(agentToMoveBox.getLetter());
			Color color = agentToMove.getColor();
			Goal newGoal = new Goal(noGoals + 1, newGoalPos, goalChar, color, noGoals + 1);

			List<Node> newPlanAgentToStay = allSolutions.get(agentToStay.getId());
			for (int i = 0; i < index - 1; i++) {
				if (newPlanAgentToStay.size() == 0)
					break;
				newPlanAgentToStay.remove(0);
			}
			agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));

			/* We set the new goal and create a plan for that goal */
			agentToMove.generateInitialState();
			agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
			agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
			agentToMove.initialState.goals.put(newGoal.getId(), newGoal);

			agentToMove.initialState.boxes.put(agentToMoveBox.getId(), agentToMoveBox);
			strategy = new StrategyBFS();
			s = new Search();
			newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.PATH);

			Node noOp = agentToStay.initialState;
			noOp.action = new Command();
			newPlanAgentToStay.add(0, noOp);
			World.getInstance().getSolutionMap().put(agentToMove.getId(), newPlanAgentToMove);
			World.getInstance().getSolutionMap().put(agentToStay.getId(), newPlanAgentToStay);
			Agent agentToMoveAway = World.getInstance().getAgents().get(newPlanAgentToMove.get(0).agentId);
			World.getInstance().getBeliefs().add(agentToMoveAway.getIntention().getDesire().getBelief());
		}
	}

	public void solveBoxOnBoxSA(Node node, Agent agent, Box senderBox, Box receiverBox, List<List<Node>> allSolutions) {

		Agent agentToMove = agent;
		agentToMove.generateInitialState();
		agentToMove.initialState.setPosition(World.getInstance().getAgents().get(0).getPosition());
		agentToMove.initialState.boxes.put(agent.getIntention().getBox().getId(),
				World.getInstance().getBoxes().get(agent.getIntention().getBox().getId()));
		agentToMove.initialState.goals.put(agent.getIntention().getDesire().getBelief().getGoal().getId(),
				agent.getIntention().getDesire().getBelief().getGoal());

		Agent tmp = new Agent(agentToMove);
		tmp.initialState.boxes.put(receiverBox.getId(), receiverBox);

		// check if generated plan is valid - check that the agent's position
		// doesn't overlap with any box
		boolean planValid = true;
		Search s = new Search();
		List<Node> plan = null;
		do {
			planValid = true;
			plan = s.search(new StrategyBestFirst(new AStar(tmp.initialState)), tmp.initialState, SearchType.PATH);
			for (Node n : plan) {
				for (Box b : n.boxes.values()) {
					if (!b.equals(receiverBox) && !b.equals(senderBox)
							&& new Position(n.agentRow, n.agentCol).equals(b.getPosition())) {
						tmp.initialState.boxes.put(b.getId(), b);
						
						planValid = false;
					}
				}
			}
		} while (!planValid);

		// remove all other boxes from agent except its own
		for (Box box : tmp.initialState.boxes.values()) {
			if (!senderBox.equals(box)) {
				tmp.initialState.boxes.remove(box);
			}
		}
		World.getInstance().getSolutionMap().put(agentToMove.getId(), plan);
		World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
		System.err.println(plan);
	}

	public void solveBoxOnBox(Conflict conflict, int index, List<List<Node>> allSolutions) {
		if (World.getInstance().getAgents().size() > 1) {
			solveBoxOnBoxMA(conflict, index, allSolutions);
		} else {
			solveBoxOnBoxSA(conflict.getNode(), conflict.getSender(), conflict.getSenderBox(),
					conflict.getReceiverBox(), allSolutions);
		}
	}

	public void solveAgentOnAgent(Node node, Agent a1, Agent a2, int index, List<List<Node>> allSolutions) {
		// System.err.println("Theres is a agent on agent conflict!");
		Agent agentToMove = a1.getPriority() > a2.getPriority() ? a2 : a1;
		Agent agentToStay = a1.getPriority() > a2.getPriority() ? a1 : a2;

		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(new Position(agentToStay.getPosition()));
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();

		for (Box box : agentToMove.initialState.boxes.values()) {
			agentToMove.initialState.walls.add(new Position(box.getPosition()));
		}
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		s.setPlanForAgentToStay(updatePlan(agentToStay.getId(), index));
		List<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_AWAY);
		List<Node> newPlanAgentToStay = allSolutions.get(agentToStay.getId());

		for (int i = 0; i < index - 1; i++) {
			if (i >= newPlanAgentToStay.size())
				break;
			newPlanAgentToStay.remove(0);
		}
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));
		Node noOp = agentToStay.initialState;
		noOp.action = new Command();
		newPlanAgentToStay.add(0, noOp);
		World.getInstance().getSolutionMap().put(agentToMove.getId(), newPlanAgentToMove);
		World.getInstance().getSolutionMap().put(agentToStay.getId(), newPlanAgentToStay);
		// Agent agentToMoveAway =
		// World.getInstance().getAgents().get(newPlanAgentToMove.get(0).agentId);
		World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
	}

	public void solveAgentOnBox(Node node, Agent agent, Box box, int index, List<List<Node>> allSolutions) {
		Agent agentToMove = agent;
		agentToMove.generateInitialState();
		agentToMove.initialState.setPosition(World.getInstance().getAgents().get(0).getPosition());
		agentToMove.initialState.boxes.put(agent.getIntention().getBox().getId(),
				World.getInstance().getBoxes().get(agent.getIntention().getBox().getId()));
		agentToMove.initialState.goals.put(agent.getIntention().getDesire().getBelief().getGoal().getId(),
				agent.getIntention().getDesire().getBelief().getGoal());

		Agent tmp = new Agent(agentToMove);
		tmp.initialState.walls.add(box.getPosition());
		Strategy strategy = new StrategyBestFirst(new AStar(tmp.initialState));
		Search s = new Search();

		List<Node> plan = s.search(strategy, tmp.initialState, SearchType.PATH);
		World.getInstance().write("plan = " + (plan == null ? "null" : "WORKZ"));
		if (plan == null || plan.isEmpty()) {
			if (agentToMove.initialState.walls.contains(box.getPosition()))
				agentToMove.initialState.walls.remove(box.getPosition());
			agentToMove.initialState.boxes.put(box.getId(), box);
			strategy = new StrategyBestFirst(new AStar(agentToMove.initialState));
			s = new Search();
			plan = s.search(strategy, agentToMove.initialState, SearchType.PATH);
		} else {
			agentToMove.initialState.walls.remove(box.getPosition());
		}
		World.getInstance().getSolutionMap().put(agentToMove.getId(), plan);
		World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
	}

	private List<Node> updatePlan(int agentId, int index) {
		List<Node> updPlan = new LinkedList<Node>();
		List<Node> oldPlan = World.getInstance().getSolutionMap().get(agentId);
		for (int i = 0; i < oldPlan.size(); i++) {
			if (i >= index - 1) {
				updPlan.add(oldPlan.get(i));
			}
		}
		return updPlan;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Conflict [conflictType=").append(conflictType).append(", sender=").append(sender)
				.append(", receiver=").append(receiver).append(", node=").append("\n").append(node).append("]");
		return builder.toString();
	}
}