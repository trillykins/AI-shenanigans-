package searchclient.node;

import searchclient.Memory;
import searchclient.strategy.Strategy;

public class MapState {

	int explored;
	int frontiers;
	float memory;
	float time;

	public MapState() {
		explored = 0;
		frontiers = 0;
		memory = Memory.used();
		time = 0;
	}

	public MapState(Strategy strategy) {
		explored = strategy.exploredCount();
		frontiers = strategy.frontierCount();
		memory = Memory.used();	
		time = strategy.timeSpent();	
	}

	public void add(MapState mapState) {
		this.explored += mapState.explored;
		this.frontiers += mapState.frontiers;
		this.time += mapState.time;
		this.memory = Math.max(this.memory, mapState.memory);
	}
}