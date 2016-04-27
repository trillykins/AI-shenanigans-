package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import atoms.Agent;
import atoms.Box;
import atoms.Goal;
import atoms.Position;
import atoms.World;

public class MapAnalysis {
	
	private World world;
	private Map<Integer, Agent> agents;
	private Map<Integer, Box> boxes;
	private Map<Integer, Goal> goals;
	private Set<Position> walls;
	
	private Map<Position, FreeSpace> freeSpaces = new HashMap<Position,FreeSpace>();
	public MapAnalysis(){
		 world = World.getInstance();
		 agents = world.getAgents();
		 boxes = world.getBoxes();
		 walls = world.getWalls();
		 goals = world.getGoals();
	}
	
	/**
	 * Get all the free spaces map for all agents
	 * @param position
	 * @return
	 */
	public Map<Position,FreeSpace> analysisFreeSpace(List<Position> position) {
		for(int i=0;i<position.size();i++) {
			Position posi = position.get(i);
			if(isSpaceFree(posi)) {
				FreeSpace free = new FreeSpace();
				free.setPosition(posi);
				free.setPriority(calculateCellPriority(posi));
				free.setNarrowCorValue(calulateNarrowCorPriority(posi));
				freeSpaces.put(posi, free);
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
		
		List<Position> allFields = getAllSurroungdings(posi);
		int numberOfOccupiedSpaces = 0;
		for(Position pos : allFields){
			if(isWallPosi(pos)) {
				numberOfOccupiedSpaces++;
			}else if(isGoalPosi(pos)) {
				numberOfOccupiedSpaces += 2;
			}
				
		}
		return numberOfOccupiedSpaces;
	}
	
	/**
	 * Calculate the narrow Corridor value
	 * if the next cell is narrow corridor, then add one value.
	 * @param posi
	 * @return
	 */
	public int calulateNarrowCorPriority(Position posi) {
		List<Position> fields = getAllSurroungdings(posi);
		int numberOfOccupiedSpaces = 0;
		for(Position pos : fields){
			if(isWallPosi(pos)) {
				numberOfOccupiedSpaces++;
			}	
		}
		if(numberOfOccupiedSpaces == 2) {
			if(checkNextNarrowCor(posi)) {
				numberOfOccupiedSpaces ++;
			}	
		}
		return numberOfOccupiedSpaces;
	}
	
	/**
	 * Check the next cell whether a narrow corridor.
	 * Only need to check direct connected cells
	 * @param posi
	 * @return
	 */
	private boolean checkNextNarrowCor(Position posi) {
		boolean isNextNarr = false;
		List<Position> direct = surroundingDirectCells(posi);
		for(Position position:direct) {
			if(!freeSpaces.isEmpty()) {
				FreeSpace free = freeSpaces.get(position);
				if(free != null) {
					int narrowValue = free.getNarrowCorValue();
					if(narrowValue == 4) {
						isNextNarr = true;
						break;
					}
				}
				
			}
		}
		return isNextNarr;
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
	
	private boolean isGoalPosi(Position position) {
		for(Integer goali: goals.keySet()) {
			Goal goal = goals.get(goali);
			if(goal.getPosition().getX() == position.getX() && goal.getPosition().getY() == position.getY()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Check if current space is a free space for specific agent
	 * @param a
	 * @param position
	 * @return
	 */
	public boolean isSpaceFree(Position position){
		/*is there an agent*/
		for(Agent agent : agents.values()){
			if(agent.getPosition().getX() == position.getX() && agent.getPosition().getY() == position.getY())
				return false;
		}
		/*is there a box*/
		for(Box box : boxes.values()){
			if(box.getPosition().getX() == position.getX() && box.getPosition().getY() == position.getY()) {
				return false;
			}
		}
		/*is there a wall*/
		for(Position wallPos : walls){
			if(wallPos.getX() == position.getX() && wallPos.getY() == position.getY())
				return false;
		}
		return true;
	}
	
	private List<Position> getAllSurroungdings(Position posi) {
		List<Position> fields = surroundingDirectCells(posi);
		List<Position> InFields = surroundingInDirectCells(posi);
		 
		List<Position> allFields = new ArrayList<Position>();
		allFields.addAll(fields);
		allFields.addAll(InFields);
		
		return allFields;
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
	 * Get Indirectly surrounding cells of a position
	 * @param position
	 * @return
	 */
	private List<Position> surroundingInDirectCells(Position position){
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
	
	private Map<Integer,PriorityQueue<FreeSpace>> generatePriorityQueue(Map<Integer,List<FreeSpace>> spaces) {
		Map<Integer,PriorityQueue<FreeSpace>> priorityMap = new HashMap<Integer,PriorityQueue<FreeSpace>>();
		for(Integer aid:spaces.keySet()) {
			PriorityQueue<FreeSpace> posiQueue = new PriorityQueue<FreeSpace>();
			List<FreeSpace> positions= spaces.get(aid);
			//int firstPri = positions.get(0).getPriority();
			for(int i=0;i<positions.size();i++) {
				FreeSpace nexPos = positions.get(i);
				posiQueue.add(nexPos);
			}
			priorityMap.put(aid, posiQueue);
		}
		return priorityMap;
	}

}
