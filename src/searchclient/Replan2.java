package searchclient;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import FIPA.Message;
import FIPA.MessageType;
import atoms.Agent;
import searchclient.Command.dir;
import searchclient.Command.type;

public class Replan2 {

	public int conflictingAgentId;
	
	public int getConflictingAgentId() {
		return conflictingAgentId;
	}

	public void setConflictingAgentId(int conflictingAgentId) {
		this.conflictingAgentId = conflictingAgentId;
	}

	public boolean canMakeNextMove(int index,List<LinkedList<Node>> allSolutions,Agent currAgent,List<Agent> agents){
		int nextIndex = index + 1;
		for(Agent agent : agents){
			if(agent.getId() != currAgent.getId()){
				if (allSolutions.get(agent.getId()).size() > nextIndex){
					Node currAgentSol = allSolutions.get(currAgent.getId()).get(index);
					Node agentSol = allSolutions.get(agent.getId()).get(nextIndex);
					if (currAgentSol.agentRow == agentSol.agentRow && currAgentSol.agentCol == agentSol.agentCol){
						
						/*HERE the FIFO should work : send a message that currAgent and agent will be conflicting in the future
						 * Currently this doesnt seem to work, did i do something wrong?*/
						Message msg = currAgent.createMessage(agent, MessageType.REQUEST, "Please replan, you are in my way");
						agent.receiveMessage(msg);
						/*not a nice solution, a trigger should be activated when a message is sent from one agent to another*/
						setConflictingAgentId(agent.getId());
						return false;
					}
				}
			}
		}
		return true;
	}

	/*Compare paths, returns the index where the two paths no longer conflicts*/
	public int comparePaths(LinkedList<Node> agentPath, LinkedList<Node> conflictAgentPath, int index){
		int agentPathIndex = index;
		for(int i=index+1; i > -1; i--){
			if(agentPathIndex >= agentPath.size()) break;
			if (!(agentPath.get(agentPathIndex).agentCol == conflictAgentPath.get(i).agentCol &&
					agentPath.get(agentPathIndex).agentRow == conflictAgentPath.get(i).agentRow)){
				/*conflictAgent is out of agents way, we should replan to this index*/
				return i;
			}	
			agentPathIndex++;
		}
		return 0;
	}
	
	/*This method calculates the conflicting agents new plan, 
	 * This plan is simply a concatination of another agents plan, a node and the rest of the conflict agents path
	 * */
	public LinkedList<Node> dummyReplan(LinkedList<Node> agentPlan,int conflictIndex,LinkedList<Node> conflictAgentPlan, int replanIndex){
		LinkedList<Node> list = new LinkedList<Node>();
		/*adding agent nodes*/
		for(int i = conflictIndex+2;i<conflictIndex+replanIndex-1;i++){
			list.add(agentPlan.get(i));
		}
		/*adding conflict agent node. We check that the direction is correct*/
		Node newNode = new Node(null,1);
		changeDir(newNode,type.Move);
		list.add(newNode);
		
		/*adding NoOp for the steps that the conflicting agent should wait for other agent to execute plan*/
		for(int h = 0;h < conflictIndex-replanIndex;h++){
			Node newNode2 = new Node(null,1);
			changeDir(newNode2,type.NoOp);
			list.add(newNode2);
   	}
		
		/*adding the rest of the conflicting agents path*/
		for(int j = replanIndex+1;j<conflictAgentPlan.size();j++){
			list.add(conflictAgentPlan.get(j));
		}
		return list;
	}
	
	/*We create the replanned solutions (copied from the index where an error occured to the rest of the paths)*/
	public List<LinkedList<Node>> createNewSolutions(List<LinkedList<Node>> allSolutions,int copyIndex,int agentId,int conflictAgentId,LinkedList<Node> newPlan, int replanIndex){
		List<LinkedList<Node>> replannedSolutions = new ArrayList<LinkedList<Node>>();
		for(int j = 0; j<allSolutions.size();j++){
			LinkedList<Node> temp = new LinkedList<Node>();
			LinkedList<Node> currAgentSol = allSolutions.get(j);
			if (j != conflictAgentId){
				for(int i = copyIndex; i < allSolutions.get(j).size();i++){
					temp.add(currAgentSol.get(i));
				}
				replannedSolutions.add(temp);
			}else{
				replannedSolutions.add(newPlan);
			}
		}
		return replannedSolutions;
	}
	
	/*Just for testing*/
	public String printCoord(int row, int col){
		return "(" + row + "," + col+ ")";
	}
	
	/*Copied from Node - used to change dir of an already existin node*/
	private int dirToRowChange(dir d) {
		return (d == dir.S ? 1 : (d == dir.N ? -1 : 0)); // South is down one
															// row (1), north is
															// up one row (-1)
	}

	private int dirToColChange(dir d) {
		return (d == dir.E ? 1 : (d == dir.W ? -1 : 0)); // East is left one
															// column (1), west
															// is right one
															// column (-1)
	}
	/*Copied from Node and modified. Used to change direction or set a node action to NoOp*/
	private void changeDir(Node node,type newType){
		for (Command c : Command.every) {
			int newAgentRow = node.agentRow + dirToRowChange(c.dir1);
			int newAgentCol = node.agentCol + dirToColChange(c.dir1);
			if ((c.actType  == type.Move) && (c.actType == newType)) {
				node.action = c;
				node.agentRow = newAgentRow;
				node.agentCol = newAgentCol;	
			}
			else if (type.NoOp == newType){
				Command cmd = new Command();
				node.action = cmd;
			}
		}
	}
	
	
}

