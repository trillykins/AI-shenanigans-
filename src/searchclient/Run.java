package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import FIPA.Message;
import FIPA.MessageNotify;
import atoms.Agent;
import atoms.Position;
import heuristics.AStar;
import strategies.Strategy;
import strategies.StrategyBestFirst;

public class Run {

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
					allSolutions.add(solution);
				}
			}
			/* 2. Merge simple solutions together */
			int size = 0;
			for (LinkedList<Node> list : allSolutions) {
				if (size < list.size())
					size = list.size();
			}
			String[] test = new String[size];
			for (int m = 0; m < size; m++) {
				for (LinkedList<Node> list : allSolutions) {
					if (m < list.size()) {
						test[m] += list.get(m).action.toString() + ",";
					} else {
						test[m] += "NoOp,";
					}
				}
			}
			for (String tester : test) {
				String newString = tester.replace("null", "[");
				String s = newString.substring(0, newString.length() - 1) + "]";
				System.out.println(s);
				System.err.println(s);
				// Find out if the joint action was succesful or not.
				// String response = serverMessages.readLine();
			}
			runSolutions(SearchClient.in, allSolutions, 0);
			/* 3. Use Update function to send solution */
			// Use stderr to print to console
			// while ( client.update() )
			// ;
			//
		} catch (IOException e) {
		}
	}
	
	public static void runSolutions(BufferedReader serverMessages, List<LinkedList<Node>> allSolutions,int index) throws IOException {
		/* 2. Merge simple solutions together */
		int size = 0;
		for (LinkedList<Node> list : allSolutions) {
			if (size < list.size())
				size = list.size();
		}
		String[] test = new String[size];
		for (int m = 0; m < size; m++) {
			for (LinkedList<Node> list : allSolutions) {
				if (m < list.size()) {
					test[m] += list.get(m).action.toString() + ",";
				} else {
					test[m] += "NoOp,";
				}
			}
		}

		for (int i=index;i<test.length;i++) {
			String newString = test[i].replace("null", "[");
			String s = newString.substring(0, newString.length() - 1) + "]";
			System.out.println(s);
			System.err.println(s);

			boolean isReplan = monitorServer(serverMessages,serverMessages.readLine(),allSolutions,s,i);
			if(isReplan) {
				break;
			}
		}
	}

	
	public static boolean monitorServer(BufferedReader in,String serverMessage,List<LinkedList<Node>> allSolutions,String action, int index) throws IOException {
		boolean isReplan = false;
		if(serverMessage.length() < 2){
			return true;
		}
		String test = serverMessage.substring(1, serverMessage.length()-1);
		List<String> list = Arrays.asList(test.split("\\s*,\\s*"));
		
		String act = action.substring(1, action.length()-1);
		List<String> actionList = Arrays.asList(act.split("\\s*,\\s*"));
		
		for(int i=0;i<list.size();i++) {
			//TODO
			/**
			 * Need to be changed to adapt the priority of Agents(how to decide the agent priority)
			 * higher priority agent should be the sender
			 */
			if(list.get(i).equals("false")) {
				
				//agent next move position
				Node node = allSolutions.get(i).get(index);
				
				/*create message*/
				String agentAction = actionList.get(i);
				Agent sender = SearchClient.agents.get(i);
				
				/**
				 * Will reconstruct later
				 */
				//Node node = sender.
				int row = node.agentRow;
				int col = node.agentCol;
				
				sender.setPosition(new Position(row, col));
				
				/**
				 * Figure out the next position is box or agent
				 */
				/*
				 * if it is box, then
				 */
				for (Agent agent : SearchClient.agents) {
					if('A' <= agent.initialState.boxes[row][col] && agent.initialState.boxes[row][col] <= 'Z') {
						System.err.println("Confilcts from box color " + Character.toLowerCase(agent.initialState.boxes[row][col]));
						sender.createMessage(agent, Utils.determineMessage("inform"), "Move(E)");
						isReplan = true;
						break;
					}
				}
				/*
				 * if it its agent, then
				 */
				for(int j=0; j<allSolutions.size();j++) {
					if(j != i) {
						Node conNodes = allSolutions.get(j).get(index-1);
						
						int nodeRow = conNodes.agentRow;
						int nodeCol = conNodes.agentCol;
						
						if(nodeRow == row && nodeCol == col) {
							Agent conAgent = SearchClient.agents.get(j);
							System.err.println("Confilcts from agent color " + sender.getPosition().getY() + "and agent color " + conAgent.getPosition().getY());
							conAgent.setPosition(new Position(nodeRow, nodeCol));
							
							Message mesg = sender.createMessage(conAgent, Utils.determineMessage("request"), "Move(E)");
							
							MessageNotify notify = new MessageNotify(mesg);
							notify.run();
							
							System.err.println("had confilcts, replaned solutions.....");
							Node newAct = notify.getNewAction();
							//TODO
							/**
							 * How to update the new solutions
							 * Problem now: cannot continue the previous solution??
							 */
							if(newAct != null) {
								LinkedList<Node> solAgent2 = allSolutions.get(j);
								solAgent2.set(index-1, newAct);
								allSolutions.remove(j);
								allSolutions.add(solAgent2);
								
								LinkedList<Node> solAgent1 = allSolutions.get(i);
								
								Node oldAction = solAgent1.get(index);
								Node noOp = new Node(null, 0);
								noOp.action = new Command();
								solAgent2.add(index-1, noOp);
								solAgent2.set(index, oldAction);
								allSolutions.remove(i);
								allSolutions.add(solAgent1);
								
								runSolutions(in, allSolutions,index-1);
							}
							isReplan = true;
							break;
						}
					}
				}
			}
			if(isReplan) {
				break;
			}
		}
		return isReplan;
	}

}