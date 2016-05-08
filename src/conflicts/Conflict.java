package conflicts;

import java.util.LinkedList;
import java.util.List;

import atoms.Agent;
import atoms.Box;
import atoms.Color;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import conflicts.Conflict.ConflictType;
import heuristics.AStar;
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
	private static World world = World.getInstance();

	/**
	 * @param sender
	 * @param receiver
	 * @param receiverBox
	 * @param senderBox
	 * @param node
	 * @param type
	 **/
	public Conflict(Agent sender, Agent receiver, Box receiverBox, Box senderBox, Node node, ConflictType type) {
		this.sender = sender;
		this.receiver = receiver;
		this.receiverBox = receiverBox;
		this.senderBox = senderBox;
		this.node = node;
		this.conflictType = type;
	}

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

	public void solveBoxOnBoxMA(Conflict conflict) {
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
		s.setPlanForAgentToStay(updatePlan(agentToStay));
		List<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_AWAY);
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));

		/*
		 * We create a new goal, for which we want the agent to move the
		 * blocking box to
		 */
		int noGoals = world.getGoals().size();
		if (newPlanAgentToMove.size() - 1 >= 0 && newPlanAgentToMove.size() - 1 >= 0) {

			Position newGoalPos = new Position(newPlanAgentToMove.get(newPlanAgentToMove.size() - 1).agentRow,
					newPlanAgentToMove.get(newPlanAgentToMove.size() - 1).agentCol);
			char goalChar = Character.toLowerCase(agentToMoveBox.getLetter());
			Color color = agentToMove.getColor();
			Goal newGoal = new Goal(noGoals + 1, newGoalPos, goalChar, color, noGoals + 1);

			List<Node> newPlanAgentToStay = agentToStay.getPlan();
			agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));

			/* We set the new goal and create a plan for that goal */
			agentToMove.generateInitialState();
			agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
			agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
			agentToMove.initialState.goals.put(newGoal.getId(), newGoal);

			agentToMove.initialState.boxes.put(agentToMoveBox.getId(), agentToMoveBox);
			strategy = new StrategyBFS();
			s = new Search();
			s.setPlanForAgentToStay(updatePlan(agentToStay));
			newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.PATH);
			agentToMove.setStepInPlan(0);
			Node noOp = agentToStay.initialState;
			noOp.action = new Command();
			newPlanAgentToStay.add(0, noOp);
			world.getSolutionMap().put(agentToMove.getId(), newPlanAgentToMove);
			world.getSolutionMap().put(agentToStay.getId(), newPlanAgentToStay);
			Agent agentToMoveAway = World.getInstance().getAgents().get(newPlanAgentToMove.get(0).agentId);
			world.getBeliefs().add(agentToMoveAway.getIntention().getDesire().getBelief());
		}
	}

	// TODO fix: does not validate plan
	public void solveBoxOnBoxSA(Node node, Agent agent, Box intentionBox, Box conflictingBox) {
		Agent agentToMove = agent;
		agentToMove.generateInitialState();
		agentToMove.initialState.setPosition(World.getInstance().getAgents().get(0).getPosition());
		agentToMove.initialState.boxes.put(agent.getIntention().getBox().getId(),
				World.getInstance().getBoxes().get(agent.getIntention().getBox().getId()));
		agentToMove.initialState.goals.put(agent.getIntention().getDesire().getBelief().getGoal().getId(),
				agent.getIntention().getDesire().getBelief().getGoal());

		Agent tmp = new Agent(agentToMove);
		tmp.initialState.walls.add(conflictingBox.getPosition());
		Strategy strategy = new StrategyBestFirst(new AStar(tmp.initialState));
		Search s = new Search();

		List<Node> plan = s.search(strategy, tmp.initialState, SearchType.PATH);
		if (plan == null || plan.isEmpty()) {
			if (tmp.initialState.walls.contains(conflictingBox.getPosition()))
				tmp.initialState.walls.remove(conflictingBox.getPosition());
			tmp.initialState.boxes.put(conflictingBox.getId(), conflictingBox);
			strategy = new StrategyBestFirst(new AStar(tmp.initialState));
			s = new Search();
			plan = s.search(strategy, tmp.initialState, SearchType.PATH);
		} else {
			tmp.initialState.walls.remove(conflictingBox.getPosition());
		}

		// check if agent's position overlaps with any other boxes
		boolean planValid = true;
		do {
			planValid = true;

			for (Node n : plan) {
				for (Box nb : n.boxes.values()) {
					for (Box wb : World.getInstance().getBoxes().values()) {
						if(nb.getId() != wb.getId() && nb.getPosition().equals(wb.getPosition()) && !n.boxes.containsKey(wb.getId()) && !tmp.initialState.walls.contains(wb.getPosition())){
							System.err.println("BOX " + wb.getLetter() + " IN THE WAY!");
							tmp.initialState.boxes.put(wb.getId(), wb);
							planValid = false;
						}
					}
				}
			}
			if (!planValid) {
				plan = s.search(new StrategyBestFirst(new AStar(tmp.initialState)), tmp.initialState, SearchType.PATH);
				System.err.println("NOT VALID!");
			}
		} while (!planValid);
		
		tmp.initialState.walls.remove(conflictingBox.getPosition());

//		System.err.println(plan);
//		System.exit(90);
		// remove all other boxes from agent except its own
		for (Box box : tmp.initialState.boxes.values()) {
			if (!intentionBox.equals(box)) {
				tmp.initialState.boxes.remove(box);
			}
		}

		agentToMove.setPlan(plan);
		agentToMove.setStepInPlan(0);
		world.getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());

	}

	public void solveBoxOnBox(Conflict conflict) {
		if (World.getInstance().getAgents().size() == 1) {
			solveBoxOnBoxSA(conflict.getNode(), conflict.getSender(), conflict.getSenderBox(),
					conflict.getReceiverBox());
		} else {
			// solveBoxOnBoxMA(conflict, index, allSolutions);
		}
	}

	public void solveAgentOnAgent(Node node, Agent a1, Agent a2) {
		Agent agentToMove = a1.getPriority() > a2.getPriority() ? a2 : a1;
		Agent agentToStay = a1.getPriority() > a2.getPriority() ? a1 : a2;
		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(new Position(agentToStay.getPosition()));
		for (Box box : agentToMove.initialState.boxes.values()) {
			agentToMove.initialState.walls.add(new Position(box.getPosition()));
		}
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		List<Node> newPlanAgentToStay = updatePlan(agentToStay);
		s.setPlanForAgentToStay(newPlanAgentToStay);
		LinkedList<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_AWAY);
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));
		agentToStay.generateInitialState();
		Node noOp = agentToStay.initialState;
		noOp.action = new Command();
		newPlanAgentToStay.add(0, noOp);
		if (newPlanAgentToMove != null && !newPlanAgentToMove.isEmpty()) {
			if (newPlanAgentToMove.size() < newPlanAgentToStay.size()) {
				int noOpsToAdd = Math.abs(newPlanAgentToMove.size() - newPlanAgentToStay.size());
				for (int i = 0; i < noOpsToAdd; i++) {
					Node n = newPlanAgentToMove.getLast().childNode();
					noOp = n;
					noOp.action = new Command();
					newPlanAgentToMove.add(noOp);
				}
			}
		} else {
			agentToMove.generateInitialState();
			Node n = agentToMove.initialState;
			noOp = n;
			noOp.action = new Command();
			newPlanAgentToMove.add(noOp);
		}
		agentToMove.setPlan(newPlanAgentToMove);
		agentToMove.setStepInPlan(0);
		agentToStay.setPlan(newPlanAgentToStay);
		agentToStay.setStepInPlan(0);
		world.getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
	}

	public void solveAgentOnBox(Node node, Agent agent, Box conflictingBox) {
		Agent agentToMove = agent;
		agentToMove.generateInitialState();
		agentToMove.initialState.setPosition(World.getInstance().getAgents().get(0).getPosition());
		agentToMove.initialState.boxes.put(agent.getIntention().getBox().getId(),
				World.getInstance().getBoxes().get(agent.getIntention().getBox().getId()));
		agentToMove.initialState.goals.put(agent.getIntention().getDesire().getBelief().getGoal().getId(),
				agent.getIntention().getDesire().getBelief().getGoal());

		Agent tmp = new Agent(agentToMove);
		tmp.initialState.walls.add(conflictingBox.getPosition());
		Strategy strategy = new StrategyBestFirst(new AStar(tmp.initialState));
		Search s = new Search();

		List<Node> plan = s.search(strategy, tmp.initialState, SearchType.PATH);
		if (plan == null || plan.isEmpty()) {
			if (agentToMove.initialState.walls.contains(conflictingBox.getPosition()))
				agentToMove.initialState.walls.remove(conflictingBox.getPosition());
			agentToMove.initialState.boxes.put(conflictingBox.getId(), conflictingBox);
			strategy = new StrategyBestFirst(new AStar(agentToMove.initialState));
			s = new Search();
			plan = s.search(strategy, agentToMove.initialState, SearchType.PATH);
		} else {
			agentToMove.initialState.walls.remove(conflictingBox.getPosition());
		}
		agentToMove.setPlan(plan);
		agentToMove.setStepInPlan(0);
		World.getInstance().getBeliefs().add(agentToMove.getIntention().getDesire().getBelief());
	}

	private List<Node> updatePlan(Agent agent) {
		List<Node> updPlan = new LinkedList<Node>();
		List<Node> oldPlan = agent.getPlan();
		for (int i = 0; i < oldPlan.size(); i++) {
			if (i >= agent.getStepInPlan() - 1) {
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