package searchclient.strategy;

import searchclient.node.Map;
import searchclient.Level;

public abstract class Distance {
	public abstract Integer distance(Map p1, Map p2);
	public abstract Integer distance(int rowP1, int colP1, int rowP2, int colP2);
	public abstract void initialize(Level level);
}