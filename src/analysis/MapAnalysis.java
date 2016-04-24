package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import atoms.Agent;
import atoms.Box;
import atoms.Position;
import atoms.World;

public class MapAnalysis {
	
	private World world;
	private Map<Integer, Agent> agents;
	private Map<Integer, Box> boxes;
	private Set<Position> walls;
	
	public MapAnalysis(){
		 world = World.getInstance();
		 agents = world.getAgents();
		 boxes = world.getBoxes();
		 walls = world.getWalls();
	}
	
	/**
	 * Get all the free spaces map for all agents
	 * @param position
	 * @return
	 */
	public Map<Integer,List<FreeSpace>> analysisFreeSpace(List<Position> position) {
		Map<Integer,List<FreeSpace>> freeSpaceMap = new HashMap<Integer,List<FreeSpace>>();
		
		for(Integer aig:agents.keySet()) {
			Agent a = agents.get(aig);
			
			List<FreeSpace> spaces = analysisPosition(a,position);
			
			freeSpaceMap.put(aig, spaces);
		}
		return freeSpaceMap;
	}
	
	/**
	 * Analysis the free spaces for each agent
	 * @param a
	 * @param position
	 * @return
	 */
	private List<FreeSpace> analysisPosition(Agent a, List<Position> position) {
		List<FreeSpace> freeSpaces = new ArrayList<FreeSpace>();
		for(int i=0;i<position.size();i++) {
			Position posi = position.get(i);
			if(isSpaceFree(a,posi)) {
				FreeSpace free = new FreeSpace();
				free.setPosition(posi);
				free.setPriority(calculateCellPriority(posi));
				
				freeSpaces.add(free);
			}
		}
		return freeSpaces;
	}
	
	/**
	 * Calculate cell priority 
	 * if directly surrounding, then priority +1
	 * if indirectly surrounding, then priority +2
	 * 
	 * The top priority is 0 and the last priority is 11
	 * @param posi
	 * @return
	 */
	public int calculateCellPriority(Position posi){
		List<Position> fields = surroundingDirectCells(posi);
		int numberOfOccupiedSpaces = 0;
		for(Position pos : fields){
			if(isWallPosi(pos))
				numberOfOccupiedSpaces++;
		}
		
		List<Position> inDirFields = surroundingInDirectCells(posi);
		for(Position posit: inDirFields) {
			if(isWallPosi(posit))
				numberOfOccupiedSpaces+=2;
		}
		return numberOfOccupiedSpaces;
	}
	
	/**
	 * Check the current position whether a wall or not
	 * @param position
	 * @return
	 */
	private boolean isWallPosi(Position position) {
		/*is there a wall*/
		for(Position wallPos : walls){
			if(wallPos.getX() == position.getX() && wallPos.getY() == position.getY())
				return true;
		}
		return false;
	}
	
	/**
	 * Check if current space is a free space for specific agent
	 * @param a
	 * @param position
	 * @return
	 */
	public boolean isSpaceFree(Agent a,Position position){
		/*is there an agent*/
		for(Agent agent : agents.values()){
			if(agent.getId() != a.getId()) {
				if(agent.getPosition().getX() == position.getX() && agent.getPosition().getY() == position.getY())
					return false;
			}
		}
		/*is there a box*/
		for(Box box : boxes.values()){
			if(box.getPosition().getX() == position.getX() && box.getPosition().getY() == position.getY()) {
				if(!box.getColor().equals(a.getColor())) {
					return false;
				}
			}
		}
		/*is there a wall*/
		for(Position wallPos : walls){
			if(wallPos.getX() == position.getX() && wallPos.getY() == position.getY())
				return false;
		}
		return true;
	}
	
	/**
	 * Get directly surrounding cells of a position(which a box or agent can directly move to like N,S,W,E)
	 * @param position
	 * @return
	 */
	private List<Position> surroundingDirectCells(Position position){
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
	
	/**
	 * Get indirectly surrounding cells of a position,
	 * where a box or agent cannot move to by one step, but could move there
	 * @param position
	 * @return
	 */
	private List<Position> surroundingInDirectCells(Position position) {
		
		int xCord = position.getX();
		int yCord = position.getY();
	
		List<Position> positionList = new ArrayList<Position>();	
		
		Position pos5 = new Position(xCord-1,yCord-1);
		positionList.add(pos5);
		Position pos6 = new Position(xCord+1,yCord-1);
		positionList.add(pos6);
		Position pos7 = new Position(xCord-1,yCord+1);
		positionList.add(pos7);
		Position pos8 = new Position(xCord+1,yCord+1);
		positionList.add(pos8);
		
		return positionList;
	}

}
