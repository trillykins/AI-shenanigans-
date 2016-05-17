package analysis;

import java.util.ArrayList;
import java.util.List;

import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;

public class LevelAnalysis {

	public LevelAnalysis() {
	}

	/*
	 * Goal priority is based on the number of occupied spaces surrounding a
	 * goal it is enhanced by the a "narrow corridor" value
	 */
	public int calculateGoalPriority(Goal goal) {
		List<Position> fields = surroundingFields(goal.getPosition());
		int numberOfOccupiedSpaces = 0;
		for (Position pos : fields) {
			if (!isSpaceFree(goal, pos)) {
				numberOfOccupiedSpaces++;
			}
		}
		numberOfOccupiedSpaces += 10 * World.getInstance().getFreeSpace().get(goal.getPosition()).getNarrowCorValue();
		return 2 * numberOfOccupiedSpaces;
	}

	public boolean isSpaceFree(Goal g, Position position) {
		/* is there an agent */
		for (Agent agent : World.getInstance().getAgents().values()) {
			if (agent.getPosition().equals(position))
				return false;
		}
		/* is there a box */
		for (Box box : World.getInstance().getBoxes().values()) {
			if (box.getPosition().equals(position) && !(Character.toLowerCase(box.getLetter()) == g.getLetter()))
				return false;
		}
		/* is there another goal */
		for (Goal goal : World.getInstance().getGoals().values()) {
			if (goal.getPosition().equals(position))
				return false;
		}
		/* is there a wall */
		for (Position wallPosition : World.getInstance().getWalls()) {
			if (wallPosition.equals(position))
				return false;
		}
		return true;
	}

	public boolean isSpaceWall(Position position) {
		for (Position wallPosition : World.getInstance().getWalls()) {
			if (wallPosition.equals(position))
				return true;
		}
		return false;
	}
	
	public boolean isSpaceSolvedGoal(Position position) {
		/* check for solved goal */
		for (Goal goal : World.getInstance().getGoals().values()) {
			if (goal.getPosition().equals(position) && goal.isSolved())
				return true;
		}
		return false;
	}

	public boolean isSpaceGoal(Position position) {
		/* is there a goal */
		for (Goal goal : World.getInstance().getGoals().values()) {
			if (goal.getPosition().equals(position))
				return true;
		}
		return false;
	}

	public boolean isSpaceBox(Position position) {
		for (Box box : World.getInstance().getBoxes().values()) {
			if (box.getPosition().equals(position))
				return true;
		}
		return false;
	}

	public List<Position> surroundingFields(Position position) {
		int xCord = position.getX();
		int yCord = position.getY();
		List<Position> positionList = new ArrayList<Position>(8);

		positionList.add(new Position(xCord - 1, yCord));
		positionList.add(new Position(xCord + 1, yCord));
		positionList.add(new Position(xCord, yCord - 1));
		positionList.add(new Position(xCord, yCord + 1));
		positionList.add(new Position(xCord + 1, yCord + 1));
		positionList.add(new Position(xCord + 1, yCord - 1));
		positionList.add(new Position(xCord - 1, yCord + 1));
		positionList.add(new Position(xCord - 1, yCord - 1));
		return positionList;
	}
}