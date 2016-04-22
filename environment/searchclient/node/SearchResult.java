package searchclient.node;

import java.util.LinkedList;

public class SearchResult {
	public enum Result {
		PLAN, 
		STUCK, 
		DONE, 
		MEMLIMIT, 
		TIMELIMIT, 
		IMPOSIBLE
	}
	public Result result;
	public LinkedList<Node> solution;
	public MapState mapState;
	
	public SearchResult() {
		solution = new LinkedList<>();
		result = Result.STUCK;
	}
	
	public SearchResult(Result result, LinkedList<Node> solution) {
		this.solution = solution;
		this.result = result;
		this.mapState = new MapState();
	}

	public SearchResult(Result result, LinkedList<Node> solution, MapState mapState) {
		this.solution = solution;
		this.result = result;
		this.mapState = mapState;
	}
}