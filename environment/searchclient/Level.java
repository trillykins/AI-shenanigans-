package searchclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import searchclient.strategy.Distance;
import searchclient.node.Map;
import searchclient.node.MapGoal;
import searchclient.node.MapAgent;
import searchclient.node.Cell;
import searchclient.node.Colour;

public class Level {
	private ArrayList<MapGoal> goals;
	private HashMap<Integer, ArrayList<MapGoal>> goalsList;
	private HashMap<Colour, ArrayList<MapGoal>> goalsByColour;
	private HashMap<Character, ArrayList<MapGoal> > goalsByLetter;
	private static Cell[][] map;
	private static Distance dist;
	private int maxRow;
	private int maxCol;

	public Level(int maxRow, int maxCol, Distance distance){
		this.maxCol = maxCol;
		this.maxRow = maxRow;
		map = new Cell[maxRow][maxCol];
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				map[i][j] = new Cell(Cell.Type.SPACE);
			}
		}
		this.goalsByLetter = new HashMap<Character, ArrayList<MapGoal>>();
		this.goalsByColour = new HashMap<Colour, ArrayList<MapGoal>>();
		Level.dist = distance;
		this.goalsList = new HashMap<Integer, ArrayList<MapGoal>>();
		this.goals = new ArrayList<>();
	}

	public Integer distance(Map p1, Map p2) {
		return distance(p1.row, p1.col, p2.row, p2.col);
	}

	public Integer distance(int rowP1, int colP1, int rowP2, int colP2) {
		if (dist == null) {
			return 0;
		}
		return Level.dist.distance(rowP1, colP1, rowP2, colP2);
	}

	public void addSpace(int row, int col) {
		Level.map[row][col] = new Cell(Cell.Type.SPACE);
	}

	public void addWall(int row, int col) {
		Level.map[row][col] = new Cell(Cell.Type.WALL);
	}

	public void addGoal(int row, int col, char letter, Colour colour) {
		letter = Character.toLowerCase(letter);
		Level.map[row][col] = new Cell(Cell.Type.GOAL, letter);

		if (!goalsByLetter.containsKey(new Character(letter))) {
			goalsByLetter.put(letter, new ArrayList<MapGoal>());
		}
		if (colour == null) {
			colour = Colour.BLUE;
		}
		MapGoal goal = new MapGoal(letter, row, col);
		addColour(goal, colour);
		goals.add(goal);
	}

	public void addColour(MapGoal goal, Colour colour) {
		ArrayList<MapGoal> goals = goalsByColour.get(colour);
		if (goals == null) {
			goals = new ArrayList<>();
			goalsByColour.put(colour, goals);
		}
		goals.add(goal);
	}

	public ArrayList<MapGoal> getGoals() {
		return goals;
	}

	public ArrayList<MapGoal> getGoals(Colour colour) {
		return this.goalsByColour.get(colour);
	}

	public ArrayList<MapGoal> getAgentGoals(int agentID) {
		return goalsList.get(agentID);
	}

	public ArrayList<Colour> getColours() {
		return new ArrayList<Colour>(this.goalsByColour.keySet());
	}

	public char isGoal(int row, int col) {
		if (Level.map[row][col].type == Cell.Type.GOAL) {
			return Level.map[row][col].letter;
		}
		return '\0';
	}

	public boolean isWall(int row, int col) {
		try {
			return (Level.map[row][col].type == Cell.Type.WALL);
		} catch(Exception e) {
			return false;
		}
	}

	public void setGoalsList(MapAgent[] agents) {
		for (int i = 0; i < agents.length; i++) {
			if (agents[i] != null) {
				ArrayList<MapGoal> goals = new ArrayList<>();
				for (MapGoal goal : this.goalsByColour.get(agents[i].colour)) {
					if (distance(agents[i], goal) != null) {												
						goals.add(goal);
					}
				}
				this.goalsList.put(agents[i].id, goals);
			}
		}
	}

	public int[][] analyse() {
		int importance[][] = new int[maxRow][maxCol];
		HashSet<Map> explored = new HashSet<>();
		ArrayList<Map> deadends = new ArrayList<>();
		ArrayList<Map> cornors = new ArrayList<>();
		ArrayList<Map> nowalls = new ArrayList<>();
		ArrayList<Map> onewall = new ArrayList<>();
		ArrayList<Map> twowalls = new ArrayList<>();
		for (int i = 1; i < map.length-1; i++) {
			for (int j = 1; j < map[0].length-1; j++) {
				if (map[i][j].type == Cell.Type.WALL) {
					continue;
				}
				int wallCount = 0;
				if (map[i][j-1].type == Cell.Type.WALL) {
					wallCount++;
				}
				if (map[i][j+1].type == Cell.Type.WALL) {
					wallCount++;
				}
				if (map[i-1][j].type == Cell.Type.WALL) {
					wallCount++;
				}
				if (map[i+1][j].type == Cell.Type.WALL) {
					wallCount++;
				}
				switch (wallCount) {
					case 0:
						nowalls.add(new Map(i,j));
						break;
					case 1:
						onewall.add(new Map(i,j));
						break;
					case 2:
						if ((map[i][j-1].type == Cell.Type.WALL && map[i][j+1].type == Cell.Type.WALL) || (map[i-1][j].type == Cell.Type.WALL && map[i+1][j].type == Cell.Type.WALL)) {
							twowalls.add(new Map(i,j));
						} else {
							if ((map[i-1][j-1].type == Cell.Type.WALL && map[i][j+1].type == Cell.Type.WALL && map[i+1][j].type == Cell.Type.WALL) 
									|| (map[i+1][j-1].type == Cell.Type.WALL && map[i][j+1].type == Cell.Type.WALL && map[i-1][j].type == Cell.Type.WALL) 
									|| (map[i-1][j+1].type == Cell.Type.WALL && map[i][j-1].type == Cell.Type.WALL && map[i+1][j].type == Cell.Type.WALL) 
									|| (map[i+1][j+1].type == Cell.Type.WALL && map[i][j-1].type == Cell.Type.WALL && map[i-1][j].type == Cell.Type.WALL)) {
								twowalls.add(new Map(i,j));
							} else {
								cornors.add(new Map(i,j));
							}
						}
						break;
					case 3:
						deadends.add(new Map(i,j));
						break;
					default:
				}

			}
		}
		int max = 0;
		for (int i = 0; i < deadends.size(); i++) {
			Map deadendsMap = deadends.get(i);
			int wallCount;
			int imp = 1;
			do {
				wallCount = 0;
				importance[deadendsMap.row][deadendsMap.col] = imp;
				explored.add(deadendsMap);
				imp++;
				max = Math.max(imp, max);
				Map tmpMap = null;
				if (map[deadendsMap.row][deadendsMap.col-1].type == Cell.Type.WALL) {
					wallCount++;
				} else {
					Map m = new Map(deadendsMap.row, deadendsMap.col-1);
					if (!explored.contains(m)) {
						tmpMap = m;
					}
				}
				if (map[deadendsMap.row][deadendsMap.col+1].type == Cell.Type.WALL) {
					wallCount++;
				} else {
					Map m = new Map(deadendsMap.row, deadendsMap.col+1);
					if (!explored.contains(m)) {
						tmpMap = m;
					}
				}
				if (map[deadendsMap.row-1][deadendsMap.col].type == Cell.Type.WALL) {
					wallCount++;
				} else {
					Map m = new Map(deadendsMap.row-1, deadendsMap.col);
					if (!explored.contains(m)) {
						tmpMap = m;
					}
				}
				if (map[deadendsMap.row+1][deadendsMap.col].type == Cell.Type.WALL) {
					wallCount++;
				} else {
					Map m = new Map(deadendsMap.row+1, deadendsMap.col);
					if(!explored.contains(m)){
						tmpMap = m;
					}
				}				
				deadendsMap = tmpMap;
			} while (deadendsMap != null && wallCount >= 2 && !explored.contains(deadendsMap));
		}
		for (Map cornorsMap : cornors) {
			if(!explored.contains(cornorsMap)) {
				importance[cornorsMap.row][cornorsMap.col] = max;
			}
		}
		max++;
		for (Map onewallMap : onewall) {
			if(!explored.contains(onewallMap)){
				importance[onewallMap.row][onewallMap.col] = max;
			}
		}
		max++;
		for (Map nowallsMap : nowalls) {
			if (!explored.contains(nowallsMap)) {
				importance[nowallsMap.row][nowallsMap.col] = max;
			}
		}
		max++;
		for (Map twowallsMap :twowalls) {
			if (!explored.contains(twowallsMap)) {
				importance[twowallsMap.row][twowallsMap.col] = max;
			}
		}
		for (MapGoal goal : goals) {
			goal.importance = importance[goal.row][goal.col];
		}

		return importance;
	}


	public Character[][] toArray(){
		Character[][] result = new Character[maxRow][maxCol];
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				switch (map[i][j].type) {
					case WALL:
						result[i][j] = '+';
						break;
					case SPACE:
						result[i][j] = ' ';
						break;
					case GOAL:
						result[i][j] = map[i][j].letter;
						break;
					default:
						result[i][j] = 'x';
						break;
				}
			}
		}
		return result;
	}
}
