package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;

public class MapAnalysis {

	private List<Position> narrowCells = new ArrayList<>();

	/**
	 * Get all the free spaces map for all agents
	 * 
	 * @param Position
	 * @return Map<Position, FreeSpace>
	 */
	public Map<Position, FreeSpace> analysisFreeSpace(List<Position> positions) {
		Map<Position, FreeSpace> freeSpaces = new HashMap<Position, FreeSpace>();
		for (Position position : positions) {
			if (isFree(position)) {
				FreeSpace free = new FreeSpace();
				free.setPosition(position);
				free.setPriority(calculateCellPriority(position));
				free.setNarrowCorValue(calulateNarrowCorPriority(position));
				freeSpaces.put(position, free);
			}
		}
		return freeSpaces;
	}

	/**
	 * Calculate cell priority if it is wall surrounding, then priority +1 if it
	 * has goal surrounding, then priority +2
	 * 
	 * @param Position
	 * @return int
	 */
	public int calculateCellPriority(Position posi) {

		List<Position> allFields = getAllSurroungdings(posi);
		int numberOfOccupiedSpaces = 0;
		for (Position pos : allFields) {
			if (isWall(pos)) {
				numberOfOccupiedSpaces++;
			} else if (isGoal(pos)) {
				numberOfOccupiedSpaces += 2;
			}
		}
		return numberOfOccupiedSpaces;
	}

	/**
	 * Calculate the narrow Corridor value if the next cell is narrow corridor,
	 * then add one value.
	 * 
	 * Updated on 27th April: Feel it is not reliable to decide
	 * the narrow corridor just based on the surrounding walls,can easily find
	 * an example that has not been covered yet.
	 * 
	 * @param Position
	 * @return int
	 */
	public int calulateNarrowCorPriority(Position posi) {
		List<Position> fields = getAllSurroungdings(posi);
		int numberOfOccupiedSpaces = 0;
		for (Position pos : fields) {
			if (!isWall(pos)) {
				numberOfOccupiedSpaces++;
			}
		}
		if (numberOfOccupiedSpaces <= 4) {
			narrowCells.add(posi);
		}

		if (numberOfOccupiedSpaces > 3) {
			for (Position position : narrowCells) {
				if (isNeighbourCell(posi, position)) {
					numberOfOccupiedSpaces++;
					break;
				}
			}
		}
		return numberOfOccupiedSpaces;
	}

	private boolean isNeighbourCell(Position posi1, Position posi2) {
		int row1 = posi1.getX();
		int row2 = posi2.getX();

		int col1 = posi1.getY();
		int col2 = posi2.getY();

		if (row1 == row2) {
			if (col1 == col2 - 1 || col1 == col2 + 1) {
				return true;
			}
		}

		if (col1 == col2) {
			if (row1 == row2 - 1 || row1 == row2 + 1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the position contains a wall
	 * 
	 * @param Position
	 * @return boolean
	 */
	private boolean isWall(Position position) {
		for (Position wallPos : World.getInstance().getWalls()) {
			if (wallPos.getX() == position.getX() && wallPos.getY() == position.getY())
				return true;
		}
		return false;
	}

	private boolean isGoal(Position position) {
		for (Goal goal : World.getInstance().getGoals().values()) {
			if (goal.getPosition().getX() == position.getX() && goal.getPosition().getY() == position.getY()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if current space is a free space for specific agent
	 * 
	 * @param Position
	 * @return boolean
	 */
	public boolean isFree(Position position) {
		/* is there an agent */
		for (Agent agent : World.getInstance().getAgents().values()) {
			if (agent.getPosition().getX() == position.getX() && agent.getPosition().getY() == position.getY())
				return false;
		}
		/* is there a box */
		for (Box box : World.getInstance().getBoxes().values()) {
			if (box.getPosition().getX() == position.getX() && box.getPosition().getY() == position.getY()) {
				return false;
			}
		}
		/* is there a wall */
		for (Position wallPos : World.getInstance().getWalls()) {
			if (wallPos.getX() == position.getX() && wallPos.getY() == position.getY())
				return false;
		}
		return true;
	}

	private List<Position> getAllSurroungdings(Position posi) {
		List<Position> fields = surroundingDirectCells(posi);
		List<Position> InFields = surroundingIndirectCells(posi);

		List<Position> allFields = new ArrayList<Position>();
		allFields.addAll(fields);
		allFields.addAll(InFields);

		return allFields;
	}

	/**
	 * Get directly surrounding cells of a position(which a box or agent can
	 * directly move to like N,S,W,E)
	 * 
	 * @param Position
	 * @return List<Position>
	 */
	private List<Position> surroundingDirectCells(Position position) {
		int xCord = position.getX();
		int yCord = position.getY();
		List<Position> positionList = new ArrayList<Position>();

		positionList.add(new Position(xCord - 1, yCord));
		positionList.add(new Position(xCord + 1, yCord));
		positionList.add(new Position(xCord, yCord - 1));
		positionList.add(new Position(xCord, yCord + 1));

		return positionList;
	}

	/**
	 * Get Indirectly surrounding cells of a position
	 * 
	 * @param Position
	 * @return List<Position>
	 */
	private List<Position> surroundingIndirectCells(Position position) {
		int xCord = position.getX();
		int yCord = position.getY();
		List<Position> positionList = new ArrayList<Position>();

		positionList.add(new Position(xCord - 1, yCord - 1));
		positionList.add(new Position(xCord + 1, yCord - 1));
		positionList.add(new Position(xCord - 1, yCord + 1));
		positionList.add(new Position(xCord + 1, yCord + 1));

		return positionList;
	}

	private Map<Integer, PriorityQueue<FreeSpace>> generatePriorityQueue(Map<Integer, List<FreeSpace>> spaces) {
		Map<Integer, PriorityQueue<FreeSpace>> priorityMap = new HashMap<Integer, PriorityQueue<FreeSpace>>();
		for (Integer aid : spaces.keySet()) {
			PriorityQueue<FreeSpace> posiQueue = new PriorityQueue<FreeSpace>();
			List<FreeSpace> positions = spaces.get(aid);
			// int firstPri = positions.get(0).getPriority();
			for (int i = 0; i < positions.size(); i++) {
				FreeSpace nexPos = positions.get(i);
				posiQueue.add(nexPos);
			}
			priorityMap.put(aid, posiQueue);
		}
		return priorityMap;
	}

}