package searchclient.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import searchclient.Agent;
import searchclient.Agent.Status;
import searchclient.Command;
import searchclient.Command.type;
import searchclient.node.Map;
import searchclient.node.MapAgent;
import searchclient.node.MapBox;
import searchclient.node.Node;
import searchclient.node.SearchResult;
import searchclient.node.SearchResult.Result;
import searchclient.node.GoalState;
import searchclient.node.GoalState.RouteClearGoalState;
import searchclient.node.GoalState.RouteClearOfAgentGoalState;
import searchclient.node.GoalState.NearGoalState;

public class ResolveConflict {

	public static ArrayList<LinkedList<Node>> resolve(Node node, ArrayList<LinkedList<Node>> solutions, List<Agent> agents) throws Exception {
		Deque<Agent> agentToHelp = new LinkedList<>();
		HashMap<Agent, ArrayList<MapAgent>> agentsToBeMoved = new HashMap<Agent, ArrayList<MapAgent>>();
		HashMap<Agent, ArrayList<MapBox>> boxesToBeMoved = new HashMap<Agent, ArrayList<MapBox>>();

		ArrayList<Agent> agentsList = new ArrayList<>(agents);
		for (Agent agent : agentsList) {
			if (agent.status == Agent.Status.STUCK || agent.status == Agent.Status.STUCK_HELPING) {
				agentToHelp.addFirst(agent);
				identifyRouteConflicts(agent, node, parseRoute(solutions, agent.id), agentsToBeMoved, boxesToBeMoved);
			}
		}

		while (!agentToHelp.isEmpty()) {
			Agent agentNeedingHelp = agentToHelp.pollFirst();

			ArrayList<MapBox> obstructingBoxes = boxesToBeMoved.get(agentNeedingHelp);
			boxesToBeMoved.remove(agentNeedingHelp);
			if (obstructingBoxes == null) {
				obstructingBoxes = new ArrayList<MapBox>();
			}
			ArrayList<MapAgent> obstructingAgents = agentsToBeMoved.get(agentNeedingHelp);
			agentsToBeMoved.remove(agentNeedingHelp);
			if (obstructingAgents == null) {
				obstructingAgents = new ArrayList<MapAgent>();
			}

			for (MapBox box : obstructingBoxes) {
				resolveBoxConflict(agentNeedingHelp, box, node, agents, solutions, agentToHelp, agentsToBeMoved, boxesToBeMoved);
			}

			for (MapAgent mapAgent : obstructingAgents) {
				if( agents.get(mapAgent.id).status == Status.HELPING ){
					continue;
				}
				if( solutions.get(mapAgent.id).size() < 5 && solutions.get(mapAgent.id).size() > 0) {
					continue;
				}
				resolveAgentConflict(solutions, node, agentNeedingHelp, agents.get(mapAgent.id), parseRoute(solutions.get(agentNeedingHelp.id), agentNeedingHelp.id), agentToHelp, agentsToBeMoved, boxesToBeMoved);
			}
		}
		return solutions;
	}

	private static void identifyRouteConflicts(Agent agent, Node node, ArrayList<Map> route, HashMap<Agent, ArrayList<MapAgent>> agentsToBeMoved, HashMap<Agent, ArrayList<MapBox>> boxesToBeMoved) {
		ArrayList<MapAgent> obstructingAgents = new ArrayList<>();
		ArrayList<MapBox> obstructingBoxes = new ArrayList<>();

		for( Map map : route ){
			Object object = node.objectAt(map);
			if (object instanceof MapAgent && ((MapAgent)object).id != agent.id) {
				obstructingAgents.add((MapAgent)object);
			} else if (object instanceof MapBox) {
				if (agent.colour != ((MapBox)object).colour) {
					obstructingBoxes.add((MapBox)object);
				}
			}
		}

		agentsToBeMoved.put(agent, obstructingAgents);
		boxesToBeMoved.put(agent, obstructingBoxes);
	}

	private static void resolveAgentConflict(ArrayList<LinkedList<Node>> solutions, Node node, Agent agentNeedingHelp, Agent obstructingAgent, ArrayList<Map> route, Deque<Agent> agentToHelp, HashMap<Agent, ArrayList<MapAgent>> agentsToBeMoved, HashMap<Agent, ArrayList<MapBox>> boxesToBeMoved) throws IOException {
		obstructingAgent.status = Agent.Status.HELPING;
		agentToHelp.remove(obstructingAgent);
		boxesToBeMoved.remove(obstructingAgent);
		agentsToBeMoved.remove(obstructingAgent);

		int row, col;
		row = node.agents[obstructingAgent.id].row;
		col = node.agents[obstructingAgent.id].col;

		Heuristic moveBlockingAgentHeuristic = new HeuristicMoveBlockingAgent(obstructingAgent, route, row, col);
		GoalState clearRouteOfAgentsGoalState = new RouteClearOfAgentGoalState(obstructingAgent.id, route);
		Strategy moveOutOfWayStrategy = new Greedy(moveBlockingAgentHeuristic);
		obstructingAgent.setState(node);
		SearchResult moveOutOfWayResult = obstructingAgent.Search(moveOutOfWayStrategy, clearRouteOfAgentsGoalState);

		if (moveOutOfWayResult.result == Result.STUCK) {
			obstructingAgent.status = Agent.Status.STUCK_HELPING;
			Node relaxed = node.subdomain(obstructingAgent.id);
			obstructingAgent.setState(relaxed);
			SearchResult relaxedResult = obstructingAgent.Search(moveOutOfWayStrategy, clearRouteOfAgentsGoalState);

			if (relaxedResult.result == Result.STUCK) {
				obstructingAgent.status = Agent.Status.STUCK;
				agentToHelp.addFirst(agentNeedingHelp);
				agentToHelp.addFirst(obstructingAgent);
				return;
			}

			solutions.get(obstructingAgent.id).clear();
			solutions.get(obstructingAgent.id).addAll(relaxedResult.solution);
			identifyRouteConflicts(obstructingAgent, node, parseRoute(solutions, obstructingAgent.id), agentsToBeMoved, boxesToBeMoved);
		} else {
			solutions.get(obstructingAgent.id).clear();
			solutions.get(obstructingAgent.id).addAll(moveOutOfWayResult.solution);
			addNoOp(node, solutions.get(obstructingAgent.id), moveOutOfWayResult.solution.size() + 1, -1);
			agentNeedingHelp.status = Agent.Status.PLAN;
			obstructingAgent.status = Agent.Status.PLAN;
		}
	}

	private static void resolveBoxConflict(Agent agentNeedingHelp, MapBox box, Node node, List<Agent> agents,ArrayList<LinkedList<Node>> solutions, Deque<Agent> agentToHelp, HashMap<Agent, ArrayList<MapAgent>> agentsToBeMoved, HashMap<Agent, ArrayList<MapBox>> boxesToBeMoved) throws IOException {
		Agent helpingAgent = findHelpingAgent(agentNeedingHelp, box, node, agents);
		helpingAgent.status = Status.HELPING;
		boxesToBeMoved.remove(helpingAgent);

		LinkedList<Node> moveBoxSolution = new LinkedList<>();
		SearchResult moveToBoxResult = moveToBox(agentNeedingHelp, helpingAgent, box, node, solutions, agentToHelp, agentsToBeMoved, boxesToBeMoved);
		if (moveToBoxResult == null) {
			return;
		}

		Node startNode = null;
		if (!moveToBoxResult.solution.isEmpty()) {
			startNode = moveToBoxResult.solution.get(moveToBoxResult.solution.size() - 1);
			moveBoxSolution.addAll(moveToBoxResult.solution);
		} else {
			startNode = node;
		}

		ArrayList<Map> routeToClear = parseRoute(solutions, agentNeedingHelp.id);
		clearRoute(agentNeedingHelp, helpingAgent, box, node, startNode, routeToClear, solutions, moveBoxSolution, agentToHelp, agentsToBeMoved, boxesToBeMoved);	
	}

	private static Agent findHelpingAgent(Agent agentNeedingHelp, MapBox box, Node node, List<Agent> agents) {
		ArrayList<Integer> potentialAgentIDs = new ArrayList<>();
		for (int id : node.getAgentIDsByColour(box.colour)) {
			if (agents.get(id).status != Agent.Status.HELPING && agents.get(id).status != Agent.Status.STUCK_HELPING && node.distance(box, node.agents[id]) != null) {
				potentialAgentIDs.add(id);
			}
		}
		if (potentialAgentIDs.isEmpty()) {
			for (int id : node.getAgentIDsByColour(box.colour)) {
				if (node.distance(box, node.agents[id]) != null) {
					potentialAgentIDs.add(id);
				}
			}
		}
		int distanceAgentToBox = Integer.MAX_VALUE;
		int chosenAgent = -1;
		for (int id : potentialAgentIDs) {
			if (node.distance(box, node.agents[id]) < distanceAgentToBox) {
				chosenAgent = id;
				distanceAgentToBox = node.distance(box, node.agents[id]);
			}
		}
		return agents.get(chosenAgent);
	}

	private static SearchResult moveToBox(Agent agentNeedingHelp, Agent helpingAgent, MapBox box, Node node, ArrayList< LinkedList< Node > > solutions, Deque<Agent> agentToHelp, HashMap<Agent, ArrayList<MapAgent>> agentsToBeMoved, HashMap<Agent, ArrayList<MapBox>> boxesToBeMoved) throws IOException{
		Heuristic moveToBoxHeuristicRelaxed	= new HeuristicMoveNearBox(helpingAgent, box);
		Strategy moveToBoxStrategyRelaxed = new AStar(moveToBoxHeuristicRelaxed);
		GoalState moveToBoxGoalStateRelaxed	= new NearGoalState(helpingAgent.id, box.row, box.col);
		helpingAgent.setState(node.subdomain(helpingAgent.id));
		SearchResult moveToBoxResultRelaxed	= helpingAgent.Search(moveToBoxStrategyRelaxed , moveToBoxGoalStateRelaxed);

		Heuristic moveToBoxHeuristic = new HeuristicMoveNearBox(helpingAgent, box);
		Strategy moveToBoxStrategy = new AStar(moveToBoxHeuristic);
		GoalState moveToBoxGoalState = new NearGoalState(helpingAgent.id, box.row, box.col);
		helpingAgent.setState(node);
		SearchResult moveToBoxResult = helpingAgent.Search(moveToBoxStrategy, moveToBoxGoalState, moveToBoxResultRelaxed);

		if (moveToBoxResult.result == Result.STUCK) {
			if (moveToBoxResultRelaxed.result == Result.STUCK) {
				helpingAgent.status = Agent.Status.IDLE;
				return null;
			}
			solutions.get(helpingAgent.id).clear();
			solutions.get(helpingAgent.id).addAll(moveToBoxResultRelaxed.solution);
			helpingAgent.status = Agent.Status.STUCK_HELPING;

			agentToHelp.addFirst(agentNeedingHelp);
			identifyRouteConflicts(agentNeedingHelp, node, parseRoute(solutions, agentNeedingHelp.id), agentsToBeMoved, boxesToBeMoved);
			agentToHelp.addFirst(helpingAgent);
			identifyRouteConflicts(helpingAgent, node, parseRoute(solutions, helpingAgent.id), agentsToBeMoved, boxesToBeMoved);

			return null;
		}
		return moveToBoxResult;
	}

	private static void clearRoute(Agent agentNeedingHelp, Agent helpingAgent, MapBox box, Node node, Node startNode, ArrayList<Map> routeToClear, ArrayList< LinkedList<Node>> solutions, LinkedList<Node> moveBoxSolution, Deque<Agent> agentToHelp, HashMap<Agent, ArrayList<MapAgent>> agentsToBeMoved, HashMap<Agent, ArrayList<MapBox>> boxesToBeMoved) throws IOException {
		Heuristic clearRouteHeuristicRelaxed = new HeuristicMoveBlockingBox(helpingAgent, box.id, routeToClear);
		Strategy clearRouteStrategyRelaxed = new AStar(clearRouteHeuristicRelaxed);
		helpingAgent.setState(startNode.subdomain(helpingAgent.id));
		SearchResult moveBoxResultRelaxed = helpingAgent.Search(clearRouteStrategyRelaxed, new RouteClearGoalState(helpingAgent.id, box.id, routeToClear));

		Heuristic clearRouteHeuristic = new HeuristicMoveBlockingBox(helpingAgent, box.id, routeToClear);
		Strategy clearRouteStrategy = new AStar(clearRouteHeuristic);
		helpingAgent.setState(startNode);
		SearchResult moveBoxResult = helpingAgent.Search(clearRouteStrategy, new RouteClearGoalState(helpingAgent.id, box.id, routeToClear), moveBoxResultRelaxed);
		
		if (moveBoxResult.result == Result.PLAN) {
			moveBoxSolution.addAll(moveBoxResult.solution);
			solutions.get(helpingAgent.id).clear();
			solutions.get(helpingAgent.id).addAll(moveBoxSolution);
			agentNeedingHelp.status = Agent.Status.PLAN;
			addNoOp(node, solutions.get(helpingAgent.id), Math.abs(routeToClear.size() - moveBoxResult.solution.size()) + 2, -1);
		} else {
			if (moveBoxResultRelaxed.result == Result.STUCK) {
				return;
			} else {
				moveBoxSolution.addAll(moveBoxResultRelaxed.solution);
				solutions.get(helpingAgent.id).clear();
				solutions.get(helpingAgent.id).addAll(moveBoxSolution);
				helpingAgent.status = Agent.Status.STUCK_HELPING;
				agentToHelp.addFirst(agentNeedingHelp);
				identifyRouteConflicts(agentNeedingHelp, node, parseRoute(solutions, agentNeedingHelp.id), agentsToBeMoved, boxesToBeMoved);
				agentToHelp.addFirst(helpingAgent);
				identifyRouteConflicts(helpingAgent, node, parseRoute(solutions, helpingAgent.id), agentsToBeMoved, boxesToBeMoved);
			}
		}
	}

	private static void addNoOp(Node node, LinkedList<Node> target, int count, int at){
		if (at == -1) {
			at = target.size() - 1;
		}
		Node noOpParent = null;
		if (!target.isEmpty()) {
			noOpParent = target.get(at);
		} else {
			noOpParent = node;
		}
		for (int i = 0; i < count; i++) {
			Node noOp = noOpParent.ChildNode();
			noOp.action = new Command();
			target.addLast(noOp);
			noOpParent = noOp;
		}
	}

	private static ArrayList<Map> parseRoute(ArrayList<LinkedList<Node>> solution, int agentId) {
		return parseRoute(solution.get(agentId), agentId);
	}
	private static ArrayList<Map> parseRoute(LinkedList<Node> solution, int agentID) {
		ArrayList<Map> route = new ArrayList<Map>();
		for (Node node : solution) {
			Map agentPosition = new Map(node.getAgents()[agentID].row, node.getAgents()[agentID].col);
			if (!route.contains(agentPosition)){
				route.add(agentPosition);
			}
			if (node.action.actType.equals(type.Push)) {	
				Map boxPosition = null;
				switch (node.action.dir2) {
					case N:
						boxPosition = new Map((node.getAgents()[agentID].row - 1), node.getAgents()[agentID].col);
						break;
					case S:
						boxPosition = new Map((node.getAgents()[agentID].row + 1), node.getAgents()[agentID].col);
						break;
					case E:
						boxPosition = new Map(node.getAgents()[agentID].row, (node.getAgents()[agentID].col + 1));
						break;
					case W:
						boxPosition = new Map(node.getAgents()[agentID].row, (node.getAgents()[agentID].col - 1));
						break;
					default:
						break;
				}
				if (!route.contains(boxPosition)){
					route.add(boxPosition);
				}
			}
		}
		return route;
	}
}