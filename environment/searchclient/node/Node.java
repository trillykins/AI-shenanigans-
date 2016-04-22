package searchclient.node;

import java.awt.Point;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Random;
import java.util.Collections;

import searchclient.Command;
import searchclient.Command.dir;
import searchclient.Command.type;
import searchclient.Level;
import searchclient.node.MapAgent;
import searchclient.node.MapBox;
import searchclient.node.MapGoal;

public class Node {
	public Node parent;
	public ArrayList<Command> actions= new ArrayList<>();
	public Command action;
	public MapAgent[] agents;
	private int g;
	private static Random rnd = new Random(System.nanoTime());
	private static Level level;
	private static HashMap<Colour, ArrayList<Integer>> agentColourIDs = new HashMap<>();
	private HashMap<Integer, MapBox> boxAtID;
	private HashMap<Character, ArrayList<MapBox>> boxAtLetter;
	private HashMap<Point, MapBox> boxAtPoint;

	public Node() {
		boxAtLetter = new HashMap<Character, ArrayList<MapBox>>();
		boxAtPoint = new HashMap<Point, MapBox>();
		boxAtID = new HashMap<Integer, MapBox>();
		agents = new MapAgent[10];
		g = 0;
	}

	public Node(Level level){
		Node.level = level;
		boxAtLetter = new HashMap<Character, ArrayList<MapBox>>();
		boxAtPoint = new HashMap<Point, MapBox>();
		boxAtID = new HashMap<Integer, MapBox>();
		agents = new MapAgent[10];
		g = 0;
	}

	public Integer distance(Map p1, Map p2) {
		return Node.level.distance(p1, p2);
	}
	public Integer distance(int rowP1, int colP1, int rowP2, int colP2) {
		return Node.level.distance(rowP1, colP1, rowP2, colP2);
	}

	public char isGoal(int row, int col){
		return level.isGoal(row, col);
	}

	public boolean isGoalState() {
		return isGoalState(Node.level.getGoals());
	}
	public boolean isGoalState(MapGoal goal) {
		return (this.boxAtPoint.containsKey(goal.getPoint()) && this.boxAtPoint.get(goal.getPoint()).getLetter() == goal.getLetter());
	}
	public boolean isGoalState(Colour colour) {
		return isGoalState(this.getGoals(colour));
	}
	public boolean isGoalState(ArrayList<MapGoal> goals) {
		if (goals == null) {
			return true;
		}
		for (int i = 0; i < goals.size(); i++) {
			Point point = goals.get(i).getPoint();
			MapBox box = this.boxAtPoint.get(point);
			if (box == null) {
				return false;
			}
			if (box.getLetter() != goals.get(i).getLetter()) {
				return false;
			}
		}
		return true;
	}

	public boolean isInitialState() {
		return this.parent == null;
	}

	public Node DuplicateNode() {
		Node node = new Node();
		for (int i = 0; i < agents.length; i++) {
			if (this.agents[i] != null) {
				node.agents[i] = new MapAgent(this.agents[i]);
			}
		}
		for (MapBox box : this.boxAtPoint.values()) {
			node.addBox(new MapBox(box));
		}
		node.g = this.g;
		node.parent = this.parent;
		return node;
	}


	private void addBox(MapBox box) {
		this.boxAtID.put(box.id, box);
		this.boxAtPoint.put(new Point(box.row, box.col), box);

		ArrayList<MapBox> boxes = boxAtLetter.get(box.getLetter());
		if (boxes == null) {
			boxAtLetter.put(box.getLetter(), new ArrayList<>());
			boxes = boxAtLetter.get(box.getLetter());
		}
		boxes.add(box);
	}
	public void addBox(char letter, Colour colour, int row, int col) {
		addBox(new MapBox(letter, colour, row, col));
	}

	public ArrayList<MapBox> getBoxes(char letter) {
		return boxAtLetter.get(letter);
	}
	public ArrayList<MapBox> getBoxes(Colour colour) {
		ArrayList<MapBox> results = new ArrayList<>();
		for (MapBox box:boxAtPoint.values()) {
			if (box.colour==colour) {
				results.add(box);
			}
		}
		return results;
	}

	public void addAgent(char agentID, Colour colour, int row, int col) throws IOException {
		int id = (int)agentID - 48;
		if (agents[id] != null) {
			return;
		}
		agents[id] = new MapAgent(id, colour, row, col);
		if (colour == null) {
			colour = Colour.BLUE;
		}
		if (!agentColourIDs.containsKey(colour)) {
			agentColourIDs.put(colour, new ArrayList<Integer>());
		}
		agentColourIDs.get(colour).add(id);
	}

	public ArrayList<MapGoal> getAgentGoals(int agentID) {
		ArrayList<MapGoal> agentGoals = Node.level.getAgentGoals(agentID);
		ArrayList<MapGoal> goals = new ArrayList<MapGoal>();
		if (agentGoals == null) {
			return new ArrayList<MapGoal>();
		}
		for (MapGoal goal : agentGoals) {
			MapBox box = this.boxAtPoint.get(goal.getPoint());
			if (box == null || box.getLetter() != goal.getLetter()) {
				goals.add(goal);
			}
		}
		return goals;
	}

	public MapAgent[] getAgents(){
		return this.agents;
	}

	public ArrayList<Integer> getAgentIDsByColour(Colour colour){
		return agentColourIDs.get(colour);
	}

	public ArrayList<MapGoal> getGoals(){
		return Node.level.getGoals();
	}

	public ArrayList<MapGoal> getGoals(Colour colour){
		return Node.level.getGoals(colour);
	}

	public Node ChildNode() {
		Node child = DuplicateNode();
		child.parent = this;
		child.g += 1;	
		return child;
	}

	public ArrayList<Node> getExpandedNodes(int agentID) {
		ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.every.length);
		for ( Command c : Command.every ) {
			// Determine applicability of action
			int newAgentRow = agents[agentID].row + dirToRowChange(c.dir1);
			int newAgentCol = agents[agentID].col + dirToColChange(c.dir1);
			MapBox box;
			if (c.actType == type.Move) {
				// Check if there's a wall or box on the cell to which the agent is moving
				if (cellIsFree(newAgentRow, newAgentCol)) {
					Node child = ChildNode();
					child.action = c;
					child.agents[agentID].row = newAgentRow;
					child.agents[agentID].col = newAgentCol;
					expandedNodes.add(child);
				}
			} else if (c.actType == type.Push) {
				// Make sure that there's actually a box to move
				box = boxAt(newAgentRow, newAgentCol);
				if (box != null && agents[agentID].colour.equals(box.colour)) {
					int newBoxRow = newAgentRow + dirToRowChange( c.dir2 );
					int newBoxCol = newAgentCol + dirToColChange( c.dir2 );
					// .. and that new cell of box is free
					if (cellIsFree( newBoxRow, newBoxCol)) {
						Node n = this.ChildNode();
						n.action = c;
						n.agents[agentID].row = newAgentRow;
						n.agents[agentID].col = newAgentCol;
						n.moveBox(n.boxAt(newAgentRow, newAgentCol), newBoxRow, newBoxCol);
						expandedNodes.add(n);
					}
				}
			} else if ( c.actType == type.Pull ) {
				// Cell is free where agent is going
				if ( cellIsFree( newAgentRow, newAgentCol ) ) {
					int boxRow = agents[agentID].row + dirToRowChange( c.dir2 );
					int boxCol = agents[agentID].col + dirToColChange( c.dir2 );
					// .. and there's a box in "dir2" of the agent			
					box = boxAt(boxRow, boxCol);
					if ( box!= null  && agents[agentID].colour == box.colour) {
						Node n = this.ChildNode();
						n.action = c;
						n.agents[agentID].row = newAgentRow;
						n.agents[agentID].col = newAgentCol;
						n.moveBox(n.boxAt(boxRow, boxCol), agents[agentID].row, agents[agentID].col);
						expandedNodes.add(n);
					}
				}
			}
		}
		Collections.shuffle(expandedNodes, rnd);
		return expandedNodes;
	}

	public boolean cellIsFree(int row, int col) {
		if (Node.level.isWall(row, col)) {
			return false;
		}
		if (this.boxAtPoint.containsKey(new Point(row, col))) {
			return false;
		}
		if (this.agentAt(row, col) != null) {
			return false;
		}
		return true;
	}

	public Object objectAt(Map map) {
		return objectAt(map.row, map.col);
	}
	public Object objectAt(int row, int col) {
		Point p = new Point(row, col);
		if (boxAtPoint.containsKey(p)) {
			return boxAtPoint.get(p);
		}
		for (int i = 0; i < 10; i++) {
			if (agents[i] != null && agents[i].isAt(row, col)) {
				return agents[i];
			}
		}
		return null;
	}

	public MapBox boxAt(int row, int col) {
		return this.boxAtPoint.get(new Point(row, col));
	}

	public MapAgent agentAt(int row, int col) {
		for (int i = 0; i < 10; i++) {
			if (this.agents[i] != null && this.agents[i].isAt(row, col)) {
				return this.agents[i];
			}
		}
		return null;
	}

	private void moveBox(MapBox box, int row, int col) {
		boxAtPoint.remove(new Point(box.row, box.col));
		boxAtID.get(box.id);
		box.row = row;
		box.col = col;
		boxAtPoint.put(new Point(box.row, box.col), box);
		boxAtID.put(box.id, box);
	}

	public boolean isWall(int row, int col) {
		return Node.level.isWall(row, col);
	}

	private static int dirToRowChange( dir d ) { 
		return ( d == dir.S ? 1 : ( d == dir.N ? -1 : 0 ) ); // South is down one row (1), north is up one row (-1)
	}

	private static int dirToColChange( dir d ) {
		return ( d == dir.E ? 1 : ( d == dir.W ? -1 : 0 ) ); // East is left one column (1), west is right one column (-1)
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

	public Node excecuteCommands(ArrayList<Command> commands) {
		Node child = ChildNode();
		for (int i = 0; i < commands.size(); i++) {
			if (commands.get(i) != null) {
				child.excecuteCommand(commands.get(i), i);
			}
		}
		return child;
	}
	public void excecuteCommand(Command command, int agentID) {
		actions.add(command);
		int newAgentRow = agents[agentID].row + dirToRowChange(command.dir1);
		int newAgentCol = agents[agentID].col + dirToColChange(command.dir1);
		switch (command.actType) {
			case Move:
				agents[agentID].row = newAgentRow;
				agents[agentID].col = newAgentCol;
				break;
			case Push:
				int newBoxRow = newAgentRow + dirToRowChange(command.dir2);
				int newBoxCol = newAgentCol + dirToColChange(command.dir2);
				agents[agentID].row = newAgentRow;
				agents[agentID].col = newAgentCol;
				moveBox(boxAt(newAgentRow, newAgentCol), newBoxRow, newBoxCol);
				break;
			case Pull:
				int boxRow = agents[agentID].row + dirToRowChange(command.dir2);
				int boxCol = agents[agentID].col + dirToColChange(command.dir2);
				int tmpAgentRow = agents[agentID].row;
				int tmpAgentCol = agents[agentID].col;
				agents[agentID].row = newAgentRow;
				agents[agentID].col = newAgentCol;
				moveBox(boxAt(boxRow, boxCol), tmpAgentRow, tmpAgentCol);
				break;
			case NoOp:
				break;
			default:
				throw new UnsupportedOperationException(command.toString());
		}
	}

	public Node subdomain(int agentID){
		Node subdomainNode = new Node();

		subdomainNode.agents[agentID] = new MapAgent(this.agents[agentID]);
		for(MapBox box: this.getBoxes(agents[agentID].colour)){
			subdomainNode.addBox(box);
		}
		
		return subdomainNode;
	}

	public int g() {
		return g;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.boxAtPoint.hashCode();
		result = prime * result + Arrays.deepHashCode(agents);
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		Node other = (Node) object;	
		if (!Arrays.equals(this.agents, other.agents)) {
			return false;
		}
		if (!this.boxAtPoint.equals(other.boxAtPoint)) {
			return false;
		}
		return true;
	}


	public MapBox[] getBoxes() {
		return boxAtPoint.values().toArray(new MapBox[0]);
	}
	public HashMap<Integer, MapBox> getBoxesByID() {
		return new HashMap<Integer, MapBox>(this.boxAtID);
	}

	@Override
	public String toString() {
		Character[][] map=level.toArray();
		for (int i = 0; i < agents.length; i++) {
			if(agents[i]!=null) {
				map[agents[i].row][agents[i].col] = (char)((int)'0' + agents[i].id);
			}
		}
		for (MapBox box : getBoxes()) {
			map[box.row][box.col]=Character.toUpperCase(box.getLetter());
		}
		StringBuilder string = new StringBuilder();
		string.append("\n");
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				string.append(map[i][j]);
			}
			string.append("\n");
		}
		return string.toString();
	}
}