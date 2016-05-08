package analysis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;

public class LevelAnalysis {
	
	public LevelAnalysis(){
	}
	
	/*Goal priority is based on the number of occupied spaces surrounding a goal
	 *it is enhanced by the a "narrow corridor" value*/
	public int calculateGoalPriority(Goal goal){
		List<Position> fields = surroundingFields(goal.getPosition());
		int numberOfOccupiedSpaces = 0;
		for(Position pos : fields){
			if(isSpaceFree(goal,pos))
				numberOfOccupiedSpaces++;
		}
		numberOfOccupiedSpaces += 10 * World.getInstance().getFreeSpace().get(goal.getPosition()).getNarrowCorValue();
//		System.err.println(goal.toString() + ", occ : " + numberOfOccupiedSpaces + " narrow : "+freespace.get(goal.getPosition()).getNarrowCorValue());
		return 2 * numberOfOccupiedSpaces;
	}
	
	public boolean isSpaceFree(Goal g,Position position){
		/*is there an agent*/
		for(Agent agent : World.getInstance().getAgents().values()){
			if(agent.getPosition().getX() == position.getX() && agent.getPosition().getY() == position.getY())
				return false;
		}
		/*is there a box*/
		for(Box box : World.getInstance().getBoxes().values()){
			if(box.getPosition().getX() == position.getX() && box.getPosition().getY() == position.getY())
				if(!(Character.toLowerCase(box.getLetter()) == g.getLetter()))
					return false;	
		}
		/*is there another goal*/
		for(Goal goal : World.getInstance().getGoals().values()){
			if(goal.getPosition().getX() == position.getX() && goal.getPosition().getY() == position.getY())
				return false;
		}
		/*is there a wall*/
		for(Position wallPosition : World.getInstance().getWalls()){
			if(wallPosition.getX() == position.getX() && wallPosition.getY() == position.getY())
				return false;
		}
		return true;
	}
	
	public List<Position> surroundingFields(Position position){
		int xCord = position.getX();
		int yCord = position.getY();	
		List<Position> positionList = new ArrayList<Position>();	
		
		positionList.add(new Position(xCord-1,yCord));
		positionList.add(new Position(xCord+1,yCord));
		positionList.add(new Position(xCord,yCord-1));
		positionList.add(new Position(xCord,yCord+1));
		positionList.add(new Position(xCord+1,yCord+1));
		positionList.add(new Position(xCord+1,yCord-1));
		positionList.add(new Position(xCord-1,yCord+1));
		positionList.add(new Position(xCord-1,yCord-1));
		return positionList;
	}
}