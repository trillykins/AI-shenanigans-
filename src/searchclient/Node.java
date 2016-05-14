package searchclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import atoms.Box;
import atoms.Color;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import searchclient.Command.dir;
import searchclient.Command.type;
import searchclient.Search.SearchType;

public class Node {
	private static Random rnd = new Random(1);

	public Color agentColor;
	public int agentRow;
	public int agentCol;
	public int moveToPositionRow;
	public int moveToPositionCol;
	public int boxToPosRow, boxToPosCol;
	public Position boxToPosition;
	public Map<Integer, Box> boxes;
	public Map<Integer, Goal> goals;
	public List<Position> walls;
	public SearchType sType;

	public Node parent;
	public Command action;

	private int g;
	public int agentId;

	public Node(Node parent, int agentId) {
		this.parent = parent;
		this.agentId = agentId;
		if (parent == null) {
			g = 0;
		} else {
			g = parent.g() + 1;
		}
	}

	public int g() {
		return g;
	}

	public boolean isInitialState() {
		return this.parent == null;
	}

	public SearchType getSearchType() {
		return this.sType;
	}
	
	public void setSearchType(SearchType type) {
		this.sType = type;
	}
	
	public Position getBoxToPosition() {
		return boxToPosition;
	}

	public void setBoxToPosition(Position boxToPosition) {
		this.boxToPosition = boxToPosition;
	}

	public boolean isGoalState() {
		boolean result = false;
		for (Integer goalId : goals.keySet()) {
			for (Integer boxId : boxes.keySet()) {
				Goal goal = goals.get(goalId);
				Box box = boxes.get(boxId);
				if (goal.getLetter() == Character.toLowerCase(box.getLetter())) {
					if (goal.getPosition().equals(box.getPosition())) {
						result = true;
						break;
					} else
						result = false;
				}
			}
			if (!result) {
				return false;
			}
		}
		return result;
	}

	public boolean agentAtMovePosition() {
		if (agentRow == moveToPositionRow && agentCol == moveToPositionCol)
			return true;
		return false;
	}

	public boolean movedBoxToPosition() {
		for (Box b : boxes.values()) {
			if (b.getPosition().equals(boxToPosition))
				return true;
		}
		return false;
	}

	public boolean movedAway(List<Node> otherPlan) {
		Position aPos = new Position(agentRow, agentCol);
		for (Node otherNode : otherPlan) {
			if (otherNode.agentRow == agentRow && otherNode.agentCol == agentCol)
				return false;
			for (Box b : otherNode.boxes.values())
				if (b.getPosition().equals(aPos))
					return false;
		}
		return true;
	}

	// public boolean movedAwayWithBox(List<Node> otherPlan) {
	// Position aPos = new Position(agentRow, agentCol);
	// for (Node otherNode : otherPlan) {
	// if (otherNode.agentRow == agentRow && otherNode.agentCol == agentCol)
	// return false;
	// for (Box b : otherNode.boxes.values())
	// if (b.getPosition().equals(aPos))
	// return false;
	// }
	// return true;
	// }
	public boolean moveAgentAndBoxAway(List<Node> otherPlan) {
		for (int i = 0; i < otherPlan.size(); i++) {
			Node otherNode = otherPlan.get(i);
			if (getAgentPosition().equals(otherNode.getAgentPosition()))
				return false;
			for (Box box : otherNode.boxes.values())
				if (getAgentPosition().equals(box.getPosition()))
					return false;
			for (Box box : boxes.values()) {
				if (box.getPosition().equals(otherNode.getAgentPosition()))
					return false;
			}
		}
		return true;
	}

	public boolean moveBoxesAway(List<Box> futureBoxPlans, List<Node> agentPlan) {
		for (int i = 0; i < futureBoxPlans.size(); i++) {
			for (Box b : boxes.values()) {
				if (futureBoxPlans.get(i).getPosition().equals(b.getPosition())) {
					return false;
				}
			}
		}
		for (int i = 0; i < agentPlan.size(); i++) {
			Node otherNode = agentPlan.get(i);
			for (Box box : otherNode.boxes.values()) {
				if (getAgentPosition().equals(box.getPosition()))
					return false;
			}
			for (Box box : boxes.values()) {
				if (box.getPosition().equals(otherNode.getAgentPosition()))
					return false;
			}
		}

		return true;
	}

	public ArrayList<Node> getExpandedNodes() {
		ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.every.length);
		for (Command c : Command.every) {
			// Determine applicability of action
			int newAgentRow = this.agentRow + dirToRowChange(c.dir1);
			int newAgentCol = this.agentCol + dirToColChange(c.dir1);
			if (c.actType == type.Move) {
				// Check if there's a wall or box on the cell to which the agent
				// is moving
				if (cellIsFree(newAgentRow, newAgentCol)) {
					Node n = this.childNode();
					n.action = c;
					n.agentRow = newAgentRow;
					n.agentCol = newAgentCol;
					expandedNodes.add(n);
				}
			} else if (c.actType == type.Push) {
				// Make sure that there's actually a box to move
				if (boxAt(newAgentRow, newAgentCol)) {
					int newBoxRow = newAgentRow + dirToRowChange(c.dir2);
					int newBoxCol = newAgentCol + dirToColChange(c.dir2);
					// .. and that new cell of box is free
					if (cellIsFree(newBoxRow, newBoxCol)) {
						Node n = this.childNode();
						n.action = c;
						n.agentRow = newAgentRow;
						n.agentCol = newAgentCol;
						Box foundBox = null;
						for (Box b : boxes.values()) {
							if (b.getPosition().equals(new Position(newAgentRow, newAgentCol))) {
								foundBox = b;
								break;
							}
						}
						if (foundBox.getColor().equals(agentColor)) {
							n.boxes.put(foundBox.getId(), new Box(foundBox.getId(), new Position(newBoxRow, newBoxCol),
									foundBox.getLetter(), foundBox.getColor()));
							expandedNodes.add(n);
						}
					}
				}
			} else if (c.actType == type.Pull) {
				// Cell is free where agent is going
				if (cellIsFree(newAgentRow, newAgentCol)) {
					int boxRow = agentRow + dirToRowChange(c.dir2);
					int boxCol = agentCol + dirToColChange(c.dir2);
					// .. and there's a box in "dir2" of the agent
					if (boxAt(boxRow, boxCol)) {
						Node n = this.childNode();
						n.action = c;
						n.agentRow = newAgentRow;
						n.agentCol = newAgentCol;
						Box foundBox = null;
						for (Integer bId : boxes.keySet()) {
							Box b = boxes.get(bId);
							if (b.getPosition().equals(new Position(boxRow, boxCol))) {
								b.setPosition(new Position(boxRow, boxCol));
								foundBox = b;
								break;
							}
						}
						n.boxes.put(foundBox.getId(), new Box(foundBox.getId(), new Position(agentRow, agentCol),
								foundBox.getLetter(), foundBox.getColor()));
						expandedNodes.add(n);
					}
				}
			}
		}
		Collections.shuffle(expandedNodes, rnd);
		return expandedNodes;
	}

	private boolean cellIsFree(int row, int col) {
		Position pos = new Position(row, col);
		for (Box b : boxes.values()) {
			// for(Box b : World.getInstance().getBoxes().values()) {
			if (b.getPosition().equals(pos))
				return false;
		}
		return (!World.getInstance().getWalls().contains(pos));
	}

	private boolean boxAt(int row, int col) {
		for (Box b : boxes.values()) {
			if (b.getPosition().equals(new Position(row, col)))
				return true;
		}
		return false;
	}

	private int dirToRowChange(dir d) {
		return (d == dir.S ? 1 : (d == dir.N ? -1 : 0)); // South is down one
															// row (1), north is
															// up one row (-1)
	}

	private int dirToColChange(dir d) {
		return (d == dir.E ? 1 : (d == dir.W ? -1 : 0)); // East is left one
															// column (1), west
															// is right one
															// column (-1)
	}

	public Node childNode() {
		Node copy = new Node(this, this.agentId);
		copy.agentColor = this.agentColor;
		copy.boxes = new HashMap<Integer, Box>(this.boxes);
		copy.walls = new ArrayList<>(this.walls);
		copy.goals = this.goals;
		copy.moveToPositionRow = this.moveToPositionRow;
		copy.moveToPositionCol = this.moveToPositionCol;
		copy.agentRow = this.agentRow;
		copy.agentCol = this.agentCol;
		copy.boxToPosition = this.boxToPosition;
		return copy;
	}

	public LinkedList<Node> extractPlan() {
		LinkedList<Node> plan = new LinkedList<Node>();
		Node n = this;
		while (!n.isInitialState()) {
			plan.addFirst(n);
			n = n.parent;
		}
		return plan;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + agentCol;
		result = prime * result + agentRow;
		result = prime * result + boxes.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (agentCol != other.agentCol)
			return false;
		if (agentRow != other.agentRow)
			return false;
		if (!boxes.equals(other.boxes))
			return false;
		return true;
	}

	public Position getAgentPosition() {
		return new Position(agentRow, agentCol);
	}

	public void setPosition(Position pos) {
		this.agentRow = pos.getX();
		this.agentCol = pos.getY();
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int row = 0; row < SearchClient.MAX_ROW; row++) {
			for (int col = 0; col < SearchClient.MAX_COLUMN; col++) {
				boolean skip = false;
				Position pos = new Position(row, col);
				for (Integer bId : boxes.keySet()) {
					Box b = boxes.get(bId);
					if (b.getPosition().equals(pos)) {
						s.append(b.getLetter());
						skip = true;
						break;
					}
				}
				if (skip)
					continue;
				for (Integer gId : goals.keySet()) {
					Goal g = goals.get(gId);
					if (g.getPosition().equals(pos)) {
						s.append(g.getLetter());
						skip = true;
						break;
					}
				}
				if (skip)
					continue;
				if (walls.contains(pos)) {
					s.append("+");
				} else if (row == this.agentRow && col == this.agentCol) {
					s.append(agentId);
				} else {
					s.append(" ");
				}
			}
			s.append("\n");
		}
		return s.toString();
	}
}