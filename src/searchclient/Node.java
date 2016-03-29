package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import atoms.Agent;
import atoms.Position;
import searchclient.Command.dir;
import searchclient.Command.type;

public class Node {

	private static Random rnd = new Random(1);

	// public Agent agent;
	public int agentX;
	public int agentY;
	public char[][] boxes = new char[SearchClient.MAX_ROW][SearchClient.MAX_COLUMN];
	public char[][] goals = new char[SearchClient.MAX_ROW][SearchClient.MAX_COLUMN];

	public Node parent;
	public Command action;

	private int g;

	public Node(Node parent) {
		this.parent = parent;
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

	public boolean isGoalState() {
		for (int row = 1; row < SearchClient.MAX_ROW - 1; row++) {
			for (int col = 1; col < SearchClient.MAX_COLUMN - 1; col++) {
				char g = goals[row][col];
				char b = Character.toLowerCase(boxes[row][col]);
				if (g > 0 && b != g) {
					return false;
				}
			}
		}
		return true;
	}

	public ArrayList<Node> getExpandedNodes() {
		ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.every.length);
		for (Command c : Command.every) {
			// Determine applicability of action
			// Position agentPosition = agent.getPosition();
			// int newAgentRow = agentPosition.getX() + dirToRowChange(c.dir1);
			// int newAgentCol = agentPosition.getY() + dirToColChange(c.dir1);
			int newAgentRow = agentX + dirToRowChange(c.dir1);
			int newAgentCol = agentY + dirToColChange(c.dir1);

			if (c.actType == type.Move) {
				// Check if there's a wall or box on the cell to which the agent
				// is moving
				if (cellIsFree(newAgentRow, newAgentCol)) {
					Node n = this.childNode();
					n.action = c;
					// agent.setPosition(new Position(newAgentRow,
					// newAgentCol));
					n.agentX = newAgentRow;
					n.agentY = newAgentCol;
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
						// agent.setPosition(new Position(newAgentRow,
						// newAgentCol));
						n.agentX = newAgentRow;
						n.agentY = newAgentCol;
						n.boxes[newBoxRow][newBoxCol] = this.boxes[newAgentRow][newAgentCol];
						n.boxes[newAgentRow][newAgentCol] = 0;
						expandedNodes.add(n);
					}
				}
			} else if (c.actType == type.Pull) {
				// Cell is free where agent is going
				if (cellIsFree(newAgentRow, newAgentCol)) {
					// int boxRow = agentPosition.getX() +
					// dirToRowChange(c.dir2);
					// int boxCol = agentPosition.getY() +
					// dirToColChange(c.dir2);
					int boxRow = agentX + dirToRowChange(c.dir2);
					int boxCol = agentY + dirToColChange(c.dir2);
					// .. and there's a box in "dir2" of the agent
					if (boxAt(boxRow, boxCol)) {
						Node n = this.childNode();
						n.action = c;
						// Position oldAgentPos = agent.getPosition();
						// agent.setPosition(new Position(newAgentRow,
						// newAgentCol));
						n.agentX = newAgentRow;
						n.agentY = newAgentCol;
						n.boxes[this.agentX][this.agentY] = this.boxes[boxRow][boxCol];
						n.boxes[boxRow][boxCol] = 0;
						expandedNodes.add(n);
					}
				}
			}
		}
		Collections.shuffle(expandedNodes, rnd);
		return expandedNodes;
	}

	private boolean cellIsFree(int row, int col) {
		return (!SearchClient.walls[row][col] && this.boxes[row][col] == 0);
	}

	private boolean boxAt(int row, int col) {
		return this.boxes[row][col] > 0;
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

	private Node childNode() {
		Node copy = new Node(this);
		for (int row = 0; row < SearchClient.MAX_ROW; row++) {
			// System.arraycopy( this.walls[row], 0, copy.walls[row], 0,
			// SearchClient.MAX_COLUMN );
			System.arraycopy(this.boxes[row], 0, copy.boxes[row], 0, SearchClient.MAX_COLUMN);
			System.arraycopy(this.goals[row], 0, copy.goals[row], 0, SearchClient.MAX_COLUMN);
		}
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
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + agentX;
		result = prime * result + agentY;
		result = prime * result + g;
		result = prime * result + Arrays.deepHashCode(boxes);
		result = prime * result + Arrays.deepHashCode(goals);
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Node))
			return false;
		Node other = (Node) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (agentX != other.agentX)
			return false;
		if (agentY != other.agentY)
			return false;
		if (!Arrays.deepEquals(boxes, other.boxes))
			return false;
		if (g != other.g)
			return false;
		if (!Arrays.deepEquals(goals, other.goals))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int row = 0; row < SearchClient.MAX_ROW; row++) {
			if (!SearchClient.walls[row][0]) {
				break;
			}
			for (int col = 0; col < SearchClient.MAX_COLUMN; col++) {
				if (this.boxes[row][col] > 0) {
					s.append(this.boxes[row][col]);
				} else if (goals[row][col] > 0) {
					s.append(goals[row][col]);
				} else if (SearchClient.walls[row][col]) {
					s.append("+");
				} else if (row == this.agentX && col == this.agentY) {
					s.append("0");
				} else {
					s.append(" ");
				}
			}
			s.append("\n");
		}
		return s.toString();
	}
}