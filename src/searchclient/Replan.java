package searchclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import atoms.Agent;
import atoms.Position;

public class Replan {
	
	private Agent confagent;
	
	/**
	 * Check if the next action of the agent is valid
	 * Only check the agent conflicts
	 * Miss the boxes confilcts(used for solve MAsimple2.lvl for now)
	 * @param node
	 * @param solutionMap
	 * @param agents
	 * @param agent
	 * @return
	 */
	public boolean checkAction(Node node,HashMap<Agent, LinkedList<Node>> solutionMap,List<Agent> agents,Agent agent) {
		
		int nodeCol = node.agentCol;
		int nodeRow = node.agentRow;
		
		for(int i=0;i<agents.size();i++) {
			Agent agen = agents.get(i);
			if(!agen.equals(agent)) {
				LinkedList<Node> solutionForAgentX = solutionMap.get(agen);
				if(solutionForAgentX.size() >0 ) {				
					if(/*nodeCol == nextStep.agentCol && nodeRow == nextStep.agentRow ||*/
							agen.getPosition().getCol() == nodeCol && agen.getPosition().getRow() == nodeRow) {
						confagent = agen;
						return false;
					}
				}else {
					return false;
				}	
			}
		}
		
		return true;
	}
	
	/**
	 * Check the current position of the agent and the next step of the agent
	 * if they are neighbour, then canmove the agent
	 * otherwise need to replan(//TODO to combine with the online replanning)
	 * @param node
	 * @param agent
	 * @return
	 */
	public boolean canMove(Node node,Agent agent) {
		//If the node is not next the agent, then the current agent cannot move
		int col = node.agentCol;
		int row = node.agentRow;
		
		int positionCol = agent.getPosition().getCol();
		int positionRow = agent.getPosition().getRow();
		
		if(row == positionRow) {
			if(col != positionCol -1 && col != positionCol +1) {
				return false;
			}
		}else if(col == positionCol) {
			if(row != positionRow-1 && row != positionRow +1) {
				return false;
			}
		}else {
			return false;
		}
		return true;
	}
	
	/**
	 * Should have other method to decide which action request should be sent
	 * The current idea is:
	 * First, the sender check whether it can move to up or down,if it can, then move(S) or move(E), and after this, 
	 * 	should call the replan function(need to be implemented) to make a new plan to achieve the goal.
	 * 
	 * Then, if the sender cannot move to south or north, then the receiver should make the move.
	 * 	The same logic that check whether can move to south or north to avoid the conflicts
	 *  if cannot move to south or north, then go back(as he level MAsimple2.lvl shows)
	 *  
	 *  #All the actions taken,should replan the plan for the agents.(need to combine with the online replanning part BDI)
	 * @param sender
	 * @param receiver
	 * @param solutionMap
	 * @return
	 */
	public List<LinkedList<Node>> communicate(Agent sender, Agent receiver,HashMap<Agent, LinkedList<Node>> solutionMap) {
		//check if can move to neighbour cell
		int senderCol = sender.getPosition().getCol();
		int senderRow = sender.getPosition().getRow();
		//If the sender agent can move to neighbour cell, then move
		Node senderNo = solutionMap.get(sender).peek();
		Node newAct = null;
		//row,col
		if(isCellFree(senderRow+1,senderCol,sender)) {
			Command c = new Command(Command.dir.S);
			newAct = createNewAction(c,senderRow-1,senderCol,senderNo);
		}else if(isCellFree(senderRow-1,senderCol,sender)) {
			Command c = null;
			if(sender.initialState.boxes[senderRow+1][senderCol] != 0) {
				c = new Command(Command.type.Push,Command.dir.N,Command.dir.N);
			}
			newAct = createNewAction(c,senderRow+1,senderCol+1,senderNo);
		}
		//Add the new actions to the nodelist
		if(newAct != null) {
			solutionMap.get(sender).set(0, newAct);
		}else {
			int receCol = receiver.getPosition().getCol();
			int receRow = receiver.getPosition().getRow();
			Node receNo = solutionMap.get(receiver).peek();
			String content = "";
			
			Node newAction = null;
			if(isCellFree(receRow-1,receCol,receiver)) {
				content = "N";
				Command co = new Command(Command.convertToDir(content));
				newAction = createNewAction(co,receRow-1,receCol,receNo);
			}else if(isCellFree(receRow+1,receCol,receiver)) {
				content = "S";
				Command co = new Command(Command.convertToDir(content));
				newAction = createNewAction(co,receRow+1,receCol,receNo);
			}else if(isCellFree(receRow,receCol-1,receiver)){
				content = "W";
				Command co = new Command(Command.convertToDir(content));
				newAction = createNewAction(co,receRow,receCol-1,receNo);
			}else if(isCellFree(receRow, receCol+1,receiver)) {
				content = "E";
				Command co = new Command(Command.convertToDir(content));
				newAction = createNewAction(co,receRow,receCol+1,receNo);
			}
			
			/**
			 * Woulc call the message at this point, now it is only for test
			 */
			//If the sender cannot move to neighbour cell,then send request to receiver agent to move
//			Message mesg = sender.createMessage(receiver, Utils.determineMessage("request"), content);
			
			//MessageNotify notify = new MessageNotify(mesg);
			//notify.run();
			
			//Node newAction = notify.getNewAction();
			
			solutionMap.get(receiver).add(0, newAction);
		}
		
		System.err.println("had confilcts, replaned solutions.....");
		List<LinkedList<Node>> allSolutions = updateSolution(solutionMap);
		return allSolutions;
	}
	
	private List<LinkedList<Node>> updateSolution(HashMap<Agent,LinkedList<Node>> solutionMap) {
		List<LinkedList<Node>> allSolutions = new ArrayList<LinkedList<Node>>();
		for(Agent agent:solutionMap.keySet()) {
			LinkedList<Node> list = solutionMap.get(agent);
			allSolutions.add(list);
		}
		return allSolutions;
	}
	
	/**
	 * Create a new action to add 
	 * @param c
	 * @param x
	 * @param y
	 * @param oldN
	 * @return
	 */
	private Node createNewAction(Command c, int x, int y, Node oldN) {
		Node node = new Node(null,oldN.agentId);
		node.action = c;
		node.agentRow = x;
		node.agentCol = y;
		node.boxes= oldN.boxes;
		node.goals = oldN.goals;
		return node;
	}
	
	public Agent getAgent() {
		return confagent;
	}
	
	/**
	 * Check whether the agent can move to the positon(row,col)
	 * based on the logic that : check is it a wall on this position or a movable box of this agent
	 * OR
	 * is there a box for any other agent, otherwise this cell if free for the agent to move to
	 * @param row
	 * @param col
	 * @param agent
	 * @return
	 */
	public boolean isCellFree(int row, int col,Agent agent) {
		for(int i=0;i<SearchClient.agents.size();i++) {
			Agent age = SearchClient.agents.get(i);
			if(age.equals(agent)) {
				return !SearchClient.walls.contains(new Position(row, col))
				&& age.initialState.boxes[row][col] == 0;
			}else {
				if(age.getPosition().getCol() == col && age.getPosition().getRow() == row) {
					return false;
				}else if(!SearchClient.walls.contains(new Position(row, col)) && age.initialState.boxes[row][col] != 0){
					return false;
				}
			}
		}
		return true;
	}

}
