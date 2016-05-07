package conflicts;

import java.util.List;
import atoms.Agent;
import atoms.Box;
import atoms.Color;
import atoms.Goal;
import atoms.Position;
import atoms.World;
import searchclient.Command;
import searchclient.Node;
import searchclient.Search;
import searchclient.Search.SearchType;
import strategies.Strategy;
import strategies.StrategyBFS;

public class BoxOnBoxConflict {



	public static void AgentBoxBoxConflict(int index,List<List<Node>> allSolutions,Agent agentToMove, Agent agentToStay, 
			Box agentToMoveBox,Box agentToStayBox){
		World.getInstance().write("We had a agent-with-box on box conflict : system exit");
		System.err.println("We had a agent-with-box on box conflict : system exit");
		System.exit(0);

	}

	public static void AgentWithBoxOnAgentWithBoxConflict(int index, List<List<Node>> allSolutions,
			Agent agentToMove, Agent agentToStay, Box agentToMoveBox){

		/* First we find the coordinate of where to put a new goal */
		agentToMove.generateInitialState();
		agentToMove.initialState.walls.add(new Position(agentToStay.getPosition()));
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
		Strategy strategy = new StrategyBFS();
		Search s = new Search();
		s.setPlanForAgentToStay(Conflict.updatePlan(agentToStay));
		List<Node> newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_AWAY);
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));
		/*
		 * We create a new goal, for which we want the agent to move the
		 * blocking box to
		 */
		int noGoals = World.getInstance().getGoals().size();

		Position newGoalPos;
		/*If the new plan is not zero, then we want to set a manual goal, which is the end pos of the agent in the new plan*/
		if (newPlanAgentToMove.size() - 1 >= 0 && newPlanAgentToMove.size() - 1 >= 0) {
			newGoalPos = new Position(newPlanAgentToMove.get(newPlanAgentToMove.size() - 1).agentRow,
					newPlanAgentToMove.get(newPlanAgentToMove.size() - 1).agentCol);
		}else{
			/*if the new plan was zero, it means that we are actually staning in a place where the other agent does not cross*/
			newGoalPos = agentToMove.getPosition();
		}
		char goalChar = Character.toLowerCase(agentToMoveBox.getLetter());
		Color color = agentToMove.getColor();
		Goal newGoal = new Goal(noGoals + 1, newGoalPos, goalChar, color, noGoals + 1);
		List<Node> newPlanAgentToStay = allSolutions.get(agentToStay.getId());
		for (int i = 0; i < index - 1; i++) {
			if (newPlanAgentToStay.size() == 0)
				break;
			newPlanAgentToStay.remove(0);
		}
		agentToMove.initialState.walls.remove(new Position(agentToStay.getPosition()));

		/* We set the new goal and create a plan for that goal */
		agentToMove.generateInitialState();
		agentToMove.initialState.agentRow = agentToMove.getPosition().getX();
		agentToMove.initialState.agentCol = agentToMove.getPosition().getY();
		agentToMove.initialState.goals.put(newGoal.getId(), newGoal);

		agentToMove.initialState.boxes.put(agentToMoveBox.getId(), agentToMoveBox);
		strategy = new StrategyBFS();
		s = new Search();
		s.setPlanForAgentToStay(World.getInstance().getSolutionMap().get(agentToStay.getId()));
		newPlanAgentToMove = s.search(strategy, agentToMove.initialState, SearchType.MOVE_OWN_BOX);
		Node noOp = agentToStay.initialState;
		noOp.action = new Command();
		newPlanAgentToStay.add(0, noOp);
		newPlanAgentToStay.add(0, noOp);
		World.getInstance().getSolutionMap().put(agentToMove.getId(), newPlanAgentToMove);
		World.getInstance().getSolutionMap().put(agentToStay.getId(), newPlanAgentToStay);
		Agent agentToMoveAway = World.getInstance().getAgents().get(newPlanAgentToMove.get(0).agentId);
		World.getInstance().getBeliefs().add(agentToMoveAway.getIntention().getDesire().getBelief());
	}
}
