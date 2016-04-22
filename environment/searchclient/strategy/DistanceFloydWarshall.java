package searchclient.strategy;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.ArrayDeque;

import searchclient.node.Map;
import searchclient.Level;

public class DistanceFloydWarshall extends Distance {
	int[][] distance;
	private HashMap<Map, Integer> index;
	private int id;

	public DistanceFloydWarshall() {
		index = new HashMap<Map, Integer>();
		id = 0;
	}

	public void initialize(Level level) {
		HashMap<Map, ArrayList<Map>> map = explore(level);
		int size = map.size();
		distance = new int[size][size];
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance.length; j++) {
				distance[i][j]=Integer.MAX_VALUE;
			}
		}
		for (int i = 0; i < size; i++) {
			distance[i][i] = 0;
		}
		for (Map p : map.keySet()) {
			for (Map n : map.get(p)) {
				distance[getIndex(p)][getIndex(n)] = 1;
			}
		}
		for (int k = 0; k < size; k++) {
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					if( distance[i][j] > ((distance[i][k] == Integer.MAX_VALUE || distance[k][j]== Integer.MAX_VALUE)?Integer.MAX_VALUE:distance[i][k] + distance[k][j])) {
						distance[i][j] = distance[i][k] + distance[k][j];
					}
				}
			}
		}
	}

	private HashMap<Map, ArrayList<Map>> explore(Level level) {
		HashMap<Map, ArrayList<Map>> visited = new HashMap<Map, ArrayList<Map>>();
		for (Map map : level.getGoals()) {
			HashMap<Map, ArrayList<Map>> visited2 = explore(level, new Map(map.row, map.col));
			if (visited2 != null) {
				visited.putAll(visited2);
			}
		}	
		return visited;
	}

	private HashMap<Map, ArrayList<Map>> explore(Level level, Map start) {
		if (index.containsKey(start)) {
			return null;
		}
		ArrayDeque<Map> frontier = new ArrayDeque<Map>();
		frontier.push(start);
		HashMap<Map, ArrayList<Map>> visited = new HashMap<Map, ArrayList<Map>>();
		while (!frontier.isEmpty()) {
			Map p = frontier.pop();
			if (!index.containsKey(p)) {
				index.put(p, id);
				id++;
			}
			ArrayList<Map> neighbours = new ArrayList<Map>();
			Map[] move 	= new Map[4];
			move[0] = new Map(p.row-1, p.col);
			move[1] = new Map(p.row+1, p.col);
			move[2]	= new Map(p.row, p.col-1);
			move[3]	= new Map(p.row, p.col+1);
			for (int i = 0; i < move.length; i++) {
				if (!level.isWall(move[i].row, move[i].col))
					neighbours.add(move[i]);
				if (!level.isWall(move[i].row, move[i].col) && !visited.containsKey(move[i])) {
					frontier.push(move[i]);
				}
			}
			visited.put(p, neighbours);
		}
		return visited;
	}


	public Integer distance(Map p1, Map p2) {
		Integer result;
		try {			
			result= distance[getIndex(p1)][getIndex(p2)];
		} catch (Exception e) {
			return null;
		}
		if(result == Integer.MAX_VALUE) {
			return null;
		}
		return result;
	}

	public Integer distance(int rowP1, int colP1, int rowP2, int colP2) {
		return distance(new Map(rowP1, colP1), new Map(rowP2, colP2));
	}

	private Integer getIndex(Map p) {
		return index.get(p);
	}
}