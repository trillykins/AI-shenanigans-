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

public class Run {
	
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
			int size = 25;
			String[] test = new String[size];
			runSolutions(allSolutions,0,test);
		}catch (IOException e) {
		}
	}
	
	/**
	 * Run the solution 
	 * and if has replanned then call it again.
	 * @param allSolutions
	 * @param index
	 * @param test
	 */
	private static void runSolutions(List<LinkedList<Node>> allSolutions, int index,String[] test) {
		Replan replan = new Replan ();
		/* 2. Merge simple solutions together */
		List<LinkedList<Node>> solutionList = null;
		for (int m = index; m < test.length; m++) {
			boolean isReplan = false;
			for (int i=0; i<allSolutions.size();i++) {
				Agent agent = SearchClient.agents.get(i);
				
				boolean isValidAction = false;
				
				LinkedList<Node> list = allSolutions.get(i);
				if(list.size() == 0) {
					if(agent.isGoalState()) {
						//is any of the agent has achieved the goal,then all the left steps are NoOp
						test[m] += "NoOp,";
						continue;
					}
				}
				
				//agent.setPosition(new Position(list.peek().agentCol,list.peek().agentRow));
				boolean isCanMove = replan.canMove(list.peek(), agent);
				if(isCanMove) {
					isReplan = false;
					//System.err.println("The next Step can move, next check the valid of action");
					isValidAction  = replan.checkAction(list.peek(), solutionMap, SearchClient.agents,agent);
					if(isValidAction) {
						Node node = list.removeFirst();
						agent.setPosition(new Position(node.agentRow,node.agentCol));
						//remove the first node from the linked list and update the map
						solutionMap.put(agent, list);
						
						//Guess should update the boxes position
						if(node.action.actType.equals(Command.type.Push)) {
							Position posi = setBoxPosition(node,agent);
							char ch = agent.initialState.boxes[node.agentRow][node.agentCol];
							agent.boxes[posi.getRow()][posi.getCol()] = ch;
						}
						test[m] += node.action.toString() + ",";
					}else {
						isReplan = true;
						Agent confliAgent = replan.getAgent();
						System.err.println("Confilcts from agent " + confliAgent.getPosition().getCol() + "and " + confliAgent.getPosition().getRow());
						solutionList = replan.communicate(agent,confliAgent,solutionMap);
						test[m] += "NoOp,";
					}
				}else {
					//Will improve later
					//Change to other method, otherwise the agent would have stop on GUI
					test[m] += "NoOp,";
				}	
			}
			run(test[m]);
			if(isReplan) {
				runSolutions(solutionList,m,test);
			}
		}
	}
	
	/**
	 * Guess we also should record the position for boxes??
	 * Maybe useful for the online replanning(not sure)
	 * @param node
	 * @param agent
	 * @return
	 */
	private static Position setBoxPosition(Node node,Agent agent) {
		int col = node.agentCol;
		int row = node.agentRow;
		
		Position posi = null;
		if(node.action.actType.equals(Command.type.Push)) {
			if(node.action.dir1.equals(Command.dir.N)) {
				posi = new Position(row-1,col);
			}
		}
		return posi;
	}
	
	public static void run(String output) {
		String newString = output.replace("null", "[");
		String s = newString.substring(0, newString.length() - 1) + "]";
		System.out.println(s);
		System.err.println(s);
	}

}