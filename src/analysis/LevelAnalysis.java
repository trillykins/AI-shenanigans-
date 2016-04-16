package analysis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;

public class LevelAnalysis {
	private World world;
	private Map<Integer, Agent> agents;
	private Map<Integer, Box> boxes;
	private Map<Integer, Goal> goals;
	private Set<Position> walls;
	
	public LevelAnalysis(){
		 world = World.getInstance();
		 agents = world.getAgents();
		 boxes = world.getBoxes();
		 goals = world.getGoals();
		 walls = world.getWalls();
	}
	
	/*Goal priority is based on the number of occupied spaces surrounding a goal*/
	public int calculateGoalPriority(Goal goal){
		List<Position> fields = surroundingFields(goal.getPosition());
		int numberOfOccupiedSpaces = 0;
		for(Position pos : fields){
			if(!isSpaceFree(goal,pos))
				numberOfOccupiedSpaces++;
		}
		return numberOfOccupiedSpaces;
	}
	
	public boolean isSpaceFree(Goal g,Position position){
		/*is there an agent*/
		for(Agent agent : agents.values()){
			if(agent.getPosition().getX() == position.getX() && agent.getPosition().getY() == position.getY())
				return false;
		}
		/*is there a box*/
		for(Box box : boxes.values()){
			if(box.getPosition().getX() == position.getX() && box.getPosition().getY() == position.getY())
				if(!(Character.toLowerCase(box.getLetter()) == g.getLetter()))
					return false;	
		}
		/*is there another goal*/
		for(Goal goal : goals.values()){
			if(goal.getPosition().getX() == position.getX() && goal.getPosition().getY() == position.getY())
				return false;
		}
		/*is there a wall*/
		for(Position wallPos : walls){
			if(wallPos.getX() == position.getX() && wallPos.getY() == position.getY())
				return false;
		}
		return true;
	}
	
	public List<Position> surroundingFields(Position position){
		int xCord = position.getX();
		int yCord = position.getY();	
		List<Position> positionList = new ArrayList<Position>();	
		
		Position pos1 = new Position(xCord-1,yCord);
		positionList.add(pos1);
		Position pos2 = new Position(xCord+1,yCord);
		positionList.add(pos2);
		Position pos3 = new Position(xCord,yCord-1);
		positionList.add(pos3);
		Position pos4 = new Position(xCord,yCord+1);
		positionList.add(pos4);
		
		return positionList;
	}
}
