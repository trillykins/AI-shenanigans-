package searchclient;

import heuristics.AStar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import strategies.Strategy;
import strategies.StrategyBestFirst;
import atoms.Agent;
import atoms.Position;

public class Run2 {
	
	private static HashMap<Agent, LinkedList<Node>> solutionMap = new HashMap<Agent,LinkedList<Node>>();

	public static void main(String[] args) throws Exception {
		System.err.println("SearchClient initializing. I am sending this using the error output stream.");
		try {
			SearchClient client = new SearchClient();
			client.init();
			if (args.length > 1)
				SearchClient.TIME = Integer.parseInt(args[1]);
			Strategy strategy = null;
			/* 1. Create solutions for each agent */
			List<LinkedList<Node>> allSolutions = new ArrayList<LinkedList<Node>>();

			for (Agent a : SearchClient.agents) {
				strategy = new StrategyBestFirst(new AStar(a.initialState));
				LinkedList<Node> solution = client.search(strategy, a.initialState);
				if (solution != null) {
					System.err.println("\nSummary for " + strategy);
					System.err.println("Found solution of length " + solution.size());
					System.err.println(strategy.searchStatus());
					solutionMap.put(a, solution);
					allSolutions.add(solution);
				}
			}
			int size = 0;
			for(LinkedList<Node> list : allSolutions){
				if(size < list.size()) size = list.size();
			}
			runSolutions(allSolutions,size);
		}catch (IOException e) {
		}
	}
	
	/**
	 * Run the solution 
	 * and if has replanned then call it again.
	 * @param allSolutions
	 * @param index
	 */

	private static void runSolutions(List<LinkedList<Node>> allSolutions, int size) {
		Replan2 replan = new Replan2 ();
		List<LinkedList<Node>> replannedSolutions = new ArrayList<LinkedList<Node>>();
		boolean needToReplan = false;
		
		for (int m = 0;m<size;m++) {
			String actionList = "[";
			for(int n = 0; n < allSolutions.size();n++){
				Agent agent = SearchClient.agents.get(n);
				if (allSolutions.get(n).size() > m){
					/*Check if it is possible to make next move*/
					if(!replan.canMakeNextMove(m,allSolutions,agent,SearchClient.agents)){
						System.err.println("Agent : "+ replan.getConflictingAgentId()+" should replan");
						/*find out where conflicting agent is no longer in agents path */
						int replanIndex = replan.comparePaths(allSolutions.get(agent.getId()),allSolutions.get(replan.getConflictingAgentId()),m);
						/* Create a new plan which is a concatination of parts*/
						LinkedList<Node> newPlan = replan.dummyReplan(allSolutions.get(n), m, allSolutions.get(replan.getConflictingAgentId()), 
								replanIndex);
						/*Creating a new replanned solution, where the solutions are copied from current index m to the end*/
						replannedSolutions = replan.createNewSolutions(allSolutions,m,agent.getId(),replan.getConflictingAgentId(),newPlan,replanIndex);
						
						needToReplan = true;
						break;
					}
					actionList += allSolutions.get(n).get(m).action.toString()+",";
				}else{
					actionList += "NoOp,"; 
				}
			}
			if (!needToReplan)
				run(actionList);
			else{
				/*We call runSolutions with the replanned plan*/
				runSolutions(replannedSolutions,getSize(replannedSolutions));
				break;
			}
		}
	}
	
	private static int getSize(List<LinkedList<Node>> sol){
		int size = 0;
		for(LinkedList<Node> list : sol){
			if(size < list.size()) size = list.size();
		}
		return size;
	}
	public LinkedList<Node> reversePath(){
		LinkedList<Node> solution = new LinkedList<Node>();
		return solution;
	}
	/**
	 * Guess we also should record the position for boxes??
	 * Maybe useful for the online replanning(not sure)
	 * @param node
	 * @param agent
	 * @return
	 */
	public static void run(String output) {
		//String newString = output.replace("null", "[");
		String s = output.substring(0, output.length() - 1) + "]";
		System.out.println(s);
		System.err.println(s);
	}

}