package conflicts;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import searchclient.Command;
import searchclient.Node;
import searchclient.Search;
import searchclient.Search.SearchType;
import strategies.StrategyBFS;

public class Conflict {
	private ConflictType conflictType;
	private Agent sender;
	private Agent receiver;
	private Node node;
	private Box receiverBox;
	private Box senderBox;
	private static World world = World.getInstance();

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

	public void MAsolveBoxOnBox(Conflict conflict) {
		Agent agentToMove = null, agentToStay = null;
		Box agentToMoveBox = null, agentToStayBox = null;

		if (conflict.getReceiver() != null)
			agentToMove = world.getAgents().get(conflict.getReceiver().getId());
		agentToMoveBox = world.getBoxes().get(conflict.getReceiverBox().getId());
		agentToStay = world.getAgents().get(conflict.getSender().getId());
		agentToStayBox = world.getBoxes().get(conflict.getSenderBox().getId());

		if (agentToMove == null) {
			MABoxOnBoxConflict.AgentBoxBoxConflict(agentToStay, agentToStayBox, agentToMove, agentToMoveBox);
		} else {
			if (agentToMove.getPlan().get(agentToMove.getStepInPlan()).action.actType.equals(Command.type.Move)) {
				MABoxOnBoxConflict.AgentBoxBoxConflict(agentToStay, agentToStayBox, agentToMove, agentToMoveBox);
			} else {
				MABoxOnBoxConflict.AgentWithBoxOnAgentWithBoxConflict(agentToMove, agentToMoveBox, agentToStay, agentToStayBox);
			}
		}
	}

	public void SASolveBoxOnBox(Conflict con) {
		// superPlanner(con.getSender(),
		// World.getInstance().getBoxes().get(con.getSenderBox().getId()));
		SABoxOnBoxConflict.solveBoxOnBoxSA(con.getNode(), con.getSender(), World.getInstance().getBoxes().get(con.getSenderBox().getId()),
				World.getInstance().getBoxes().get(con.getReceiverBox().getId()));
	}

	public void solveAgentOnAgent(Conflict conflict, Node node, Agent a1, Agent a2) {
		Agent agentToMove = null, agentToStay = null;
		Box agentToMoveBox = null;

		if (conflict.getReceiver() != null)
			agentToMove = world.getAgents().get(conflict.getReceiver().getId());
		if (conflict.getSenderBox() != null)
			agentToMoveBox = world.getBoxes().get(conflict.getSenderBox().getId());
		agentToStay = world.getAgents().get(conflict.getSender().getId());

		if (agentToMove.getPlan().get(agentToMove.getStepInPlan()).action.actType.equals(Command.type.Pull)
				|| agentToMove.getPlan().get(agentToMove.getStepInPlan()).action.actType.equals(Command.type.Push)) {
			System.err.println("AA 1");
			MAAgentOnAgentConflict.moveAgentOnAgentWithBox(agentToMove, agentToStay, agentToMoveBox);
		} else {
			System.err.println("AA 2");
			MAAgentOnAgentConflict.moveAgentOnAgentNoBox(agentToMove, agentToStay, agentToMoveBox);

		}
	}

	/*
	 * Scribbles If the plan returns null, remove all box- and goal-walls and
	 * recalculate plan. Create a plan as done originally, but somehow include
	 * the boxes that have originally been put aside to make room. If this isn't
	 * possible, then shit pants and give up. Cake!
	 * 
	 * keep track of previously moved boxes (that are not used to solve goals)
	 * 
	 * if plan is null for moving boxes out of the way: - plan new route to
	 * place boxes if previously moved box in the way, - make plan to move box
	 * to a spot where it is possible for agent other boxes (new search, to
	 * check that the box spot isn't on the previously moved box position)
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	public void minorChangeInPlan(Agent agent, Box conflictingBox) {
		Agent mover = new Agent(agent);
		mover.generateInitialState();
		mover.initialState.agentRow = agent.getPlan().get(agent.getStepInPlan() - 1).agentRow;
		mover.initialState.agentCol = agent.getPlan().get(agent.getStepInPlan() - 1).agentCol;
		for (Box b : agent.getPlan().get(agent.getStepInPlan() - 1).boxes.values()) {
			mover.initialState.walls.add(b.getPosition());
			// mover.initialState.boxes.remove(b.getId());
		}
		mover.initialState.boxes.put(conflictingBox.getId(), conflictingBox);
		Search s = new Search();
		s.setPlanForAgentToStay(updatePlan(agent));
		s.setFutureBoxPositions(new ArrayList<Box>(0));
		mover.initialState.moveToPositionRow = mover.initialState.agentRow;
		mover.initialState.moveToPositionCol = mover.initialState.agentCol;
		LinkedList<Node> tmpPlan = s.search(new StrategyBFS(), mover.initialState, SearchType.MOVE_BOXES);
		for (Node n : tmpPlan) {
			for (Box b : agent.getPlan().get(agent.getStepInPlan() - 1).boxes.values()) {
				n.walls.remove(b.getPosition());
			}
		}

		List<Node> newPlan = agent.getPlan();
		newPlan.addAll(agent.getStepInPlan(), tmpPlan);
		agent.setPlan(newPlan);
		agent.setStepInPlan(agent.getStepInPlan());
		mover.initialState.boxes.remove(conflictingBox.getId());
		for (Box b : agent.getPlan().get(agent.getStepInPlan() - 1).boxes.values()) {
			mover.initialState.walls.remove(b.getPosition());
		}

	}

	public void superPlanner(Agent agent, Box box) {
		List<Node> originalAgentPlan = agent.getPlan();
		Agent agentToMove = agent;
		// System.err.println(World.getInstance().getAgents().get(0).getPosition());
		// System.err.println(World.getInstance().getAgents().get(0).getPosition());
		agentToMove.initialState.setPosition(World.getInstance().getAgents().get(0).getPosition());
		Search s = new Search();
		List<Box> boxesForReplanning = new LinkedList<>();
		for (int i = 0; i < originalAgentPlan.size(); i++) {
			for (Box b : World.getInstance().getBoxes().values()) {
				if (originalAgentPlan.get(i).getAgentPosition().equals(b.getPosition()) && !boxesForReplanning.contains(b)
						&& b.getId() != agentToMove.getIntentionBox().getId()) {
					boxesForReplanning.add(b);
					agentToMove.addPreviouslyMovedBoxLocations(b.getId());
				}
			}
		}
		world.write("boxesForReplanning.size() = " + boxesForReplanning.size());
		List<Box> futurePositions = new ArrayList<>(boxesForReplanning.size());
		List<LinkedList<Node>> plans = new ArrayList<>(boxesForReplanning.size());
		// reverse order
		LinkedList<Node> tmpPlan = new LinkedList<>();
		for (int i = boxesForReplanning.size() - 1; i >= 0; i--) {
			Agent boxieToMove = agent;
			boxieToMove.generateInitialState();

			boxieToMove.initialState.setPosition(World.getInstance().getAgents().get(0).getPosition());
			boxieToMove.initialState.boxes.put(boxesForReplanning.get(i).getId(), boxesForReplanning.get(i));
			for (Box wb : World.getInstance().getBoxes().values()) {
				if (!boxesForReplanning.contains(wb)) {
					agentToMove.initialState.walls.add(wb.getPosition());
				}
			}
			for (Goal wg : World.getInstance().getGoals().values()) {
				if (!boxesForReplanning.contains(wg)) {
					agentToMove.initialState.walls.add(wg.getPosition());
				}
			}
			s.setFutureBoxPositions(futurePositions);
			s.setPlanForAgentToStay(updatePlan(agentToMove));
			// System.err.println("future: " + s.getFutureBoxPositions());
			// System.err.println("agent plan: " + s.getOtherPlan());
			boxieToMove.initialState.moveToPositionRow = s.getOtherPlan().get(0).agentRow;
			boxieToMove.initialState.moveToPositionCol = s.getOtherPlan().get(0).agentCol;
			tmpPlan = s.search(new StrategyBFS(), boxieToMove.initialState, SearchType.MOVE_BOXES);
			// if (tmpPlan == null || tmpPlan.isEmpty()) {
			// for (int j = 0; j < futurePositions.size(); j++) {
			// Box box1 = futurePositions.get(j);
			// boxieToMove.initialState.boxes.put(box1.getId(), box1);
			// }
			// }
			for (Box wb : World.getInstance().getBoxes().values()) {
				if (!boxesForReplanning.contains(wb)) {
					agentToMove.initialState.walls.remove(wb.getPosition());
				}
			}
			for (Goal wg : World.getInstance().getGoals().values()) {
				if (!boxesForReplanning.contains(wg)) {
					agentToMove.initialState.walls.remove(wg.getPosition());
				}
			}

			if (tmpPlan != null && !tmpPlan.isEmpty()) {
				for (Box b : tmpPlan.getLast().boxes.values()) {
					futurePositions.add(b); // TODO potential problem
				}
				plans.add(tmpPlan);
			}
		}
		// List<Box> boxPositions = new ArrayList<>(0);
		// for (int i = 0; i < plans.size(); i++) {
		// LinkedList<Node> plan = plans.get(i);
		// Node last = plan.getLast();
		// for (Box b : last.boxes.values()) {
		// boxPositions.add(b);
		// }
		// if (i == 0)
		// continue;
		// for (int j = 0; j < plan.size(); j++) {
		// Node n = plan.get(j);
		// for (Box boxP : boxPositions) {
		// if (boxP.getPosition().equals(n.getAgentPosition())) {
		// System.err.println("FUCK!");
		// System.err.println(n.getAgentPosition());
		// // System.exit(0);
		// Node first = plan.getFirst();
		// first.walls.add(n.getAgentPosition());
		// s = new Search();
		// s.setFutureBoxPositions(boxPositions);
		// s.setPlanForAgentToStay(originalAgentPlan);
		// LinkedList<Node> newPlan = s.search(new StrategyBFS(), first,
		// SearchType.MOVE_BOXES);
		// first.walls.remove(n.getAgentPosition());
		// LinkedList<Node> h = plans.set(i, newPlan);
		// System.err.println(h.equals(newPlan));
		// }
		// }
		// }
		// System.err.println("boxPositions: \n" + boxPositions);
		// }

		List<Node> finalPlan = new LinkedList<>();
		// while (plans.size() >= World.getInstance().getBoxes().size()) {
		// plans.remove(0);
		// }
		for (int i = plans.size() - 1; i >= 0; i--) {
			finalPlan.addAll(plans.get(i));
		}
		// System.err.println("plan_extra2_" +
		// agent.getIntentionBox().getLetter() + "_" + plans.size());
		List<Node> originalPlanFromRightIndex = updatePlan(agent);

		finalPlan.addAll(originalPlanFromRightIndex);
		finalPlan = verifyPlan(finalPlan);

		agentToMove.setPlan(finalPlan);
		agentToMove.setStepInPlan(0);
		agentToMove.setExecutingSuperPlan(true);
	}

	public void solveAgentOnBox(Node node, Agent agent, Box box) {
		int tries = 0;
		Agent agentToMove = agent;
		agentToMove.generateInitialState();
		Search s = new Search();
		if (agent.getStepInPlan() > 0) {
			agentToMove.initialState.agentRow = agent.getPlan().get(agent.getStepInPlan() - 1).agentRow;
			agentToMove.initialState.agentCol = agent.getPlan().get(agent.getStepInPlan() - 1).agentCol;
		} else {
			agentToMove.initialState.setPosition(agent.getPosition());
		}
		agentToMove.initialState.walls.add(box.getPosition());
		agentToMove.initialState.boxes.put(agentToMove.getIntentionBox().getId(), World.getInstance().getBoxes().get(agentToMove.getIntentionBox().getId()));
		agentToMove.initialState.goals.put(agentToMove.getIntentionGoal().getId(), World.getInstance().getGoals().get(agentToMove.getIntentionGoal().getId()));

		LinkedList<Node> initialPlan = s.search(new StrategyBFS(), agentToMove.initialState, SearchType.PATH);
		List<Box> boxWalls = new ArrayList<>();
		// System.err.println("Checking if there's a route around the
		// conflict");
		while (initialPlan != null) {
			if (tries > 20)
				break;
			for (int i = 0; i < initialPlan.size(); i++) {
				for (Box b : World.getInstance().getBoxes().values()) {
					if (b.getId() != agentToMove.getIntentionBox().getId() && initialPlan.get(i).getAgentPosition().equals(b.getPosition()) && !boxWalls.contains(b)) {
						boxWalls.add(b);
						agentToMove.initialState.walls.add(b.getPosition());
					}
				}
			}
			initialPlan = s.search(new StrategyBFS(), agentToMove.initialState, SearchType.PATH);
			tries++;
		}

		agentToMove.initialState.walls.remove(box.getPosition());
		for (Box b : boxWalls) {
			agentToMove.initialState.walls.remove(b.getPosition());
		}
		agentToMove.initialState.boxes.remove(agentToMove.getIntentionBox().getId());

		if (initialPlan == null) {
			superPlanner(agent, box);
		} else {
			agentToMove.setPlan(initialPlan);
			agentToMove.setStepInPlan(0);
		}
	}

	public static List<Node> updatePlan(Agent agent) {
		List<Node> updPlan = new LinkedList<Node>();
		List<Node> oldPlan = agent.getPlan();
		for (int i = 0; i < oldPlan.size(); i++) {
			if (i >= agent.getStepInPlan() - 1) {
				updPlan.add(oldPlan.get(i));
			}
		}
		return updPlan;
	}

	public void printPlan(List<Node> plan, String filename) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(filename + ".txt", "UTF-8");
			for (Node n : plan) {
				writer.println(n.action);
				writer.println(n);
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public List<Node> verifyPlan(List<Node> plan) {
		Position agentPosition;
		for (int index = 0; index < plan.size(); index++) {
			if (index - 1 >= 0) {
				if (plan.get(index).action.toString().contains("Move")) {
					switch (plan.get(index).action.dir1) {
					case N:
						agentPosition = new Position(plan.get(index - 1).getAgentPosition().getX() - 1, plan.get(index - 1).getAgentPosition().getY());
						if (!plan.get(index).getAgentPosition().equals(agentPosition)) {
							plan.remove(index);
						}
						break;
					case E:
						agentPosition = new Position(plan.get(index - 1).getAgentPosition().getX(), plan.get(index - 1).getAgentPosition().getY() + 1);
						if (!plan.get(index).getAgentPosition().equals(agentPosition)) {
							plan.remove(index);
						}
						break;
					case S:
						agentPosition = new Position(plan.get(index - 1).getAgentPosition().getX() + 1, plan.get(index).getAgentPosition().getY());
						if (!plan.get(index).getAgentPosition().equals(agentPosition)) {
							plan.remove(index);
						}
						break;
					case W:
						agentPosition = new Position(plan.get(index - 1).getAgentPosition().getX(), plan.get(index - 1).getAgentPosition().getY() - 1);
						if (!plan.get(index).getAgentPosition().equals(agentPosition)) {
							plan.remove(index);
						}
						break;
					}
				}
			}
		}
		return plan;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Conflict [conflictType=").append(conflictType).append(", sender=").append(sender).append(", receiver=").append(receiver).append(", node=")
				.append("\n").append(node).append("]");
		return builder.toString();
	}
	
	public static Node createNoOpNode(Agent agent, Node parent) {
		Node node = new Node(parent,agent.getId());
		node.action = new Command();
		if(parent != null) {
			node.boxes = parent.boxes;
			node.agentCol = parent.agentCol;
			node.agentRow = parent.agentRow;
		}else {
			node.boxes = agent.initialState.boxes;
			node.agentCol = agent.getPosition().getY();
			node.agentRow = agent.getPosition().getX();
		}

		node.goals = agent.initialState.goals;
		node.walls = agent.initialState.walls;
		return node;
	}
}