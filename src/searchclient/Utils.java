package searchclient;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import FIPA.MessageType;
import atoms.Agent;
import atoms.Box;
import atoms.Color;
import atoms.Position;
import atoms.World;

public class Utils {

	public static Byte[][] calculateDistanceValues(int x, int y, char id, int MAX_ROW, int MAX_COLUMN) {
		Byte[][] result = new Byte[MAX_ROW][MAX_COLUMN];
		result[x][y] = 0;
		for (int i = 1; i < MAX_ROW - 1; i++) {
			for (int j = 1; j < MAX_COLUMN - 1; j++) {
				result[i][j] = (byte) (Math.abs(i - x) + Math.abs(j - y));
			}
		}
		return result;
	}

	public static int manhattenDistance(Position aPos, Position gPos) {
		int ax = Math.abs(aPos.getX() - gPos.getX());
		int ay = Math.abs(aPos.getY() - gPos.getY());
		return ax + ay;
	}

	public static Color determineColor(String color) {
		if (color == null)
			return Color.BLUE;
		color = color.toLowerCase();
		switch (color) {
		case "green":
			return Color.GREEN;
		case "blue":
			return Color.BLUE;
		case "red":
			return Color.RED;
		case "cyan":
			return Color.CYAN;
		case "magenta":
			return Color.MAGENTA;
		case "orange":
			return Color.ORANGE;
		case "pink":
			return Color.PINK;
		case "yellow":
			return Color.YELLOW;
		default:
			return Color.NONE;
		}
	}

	public static MessageType determineMessage(String type) {
		type = type.toLowerCase();
		switch (type) {
		case "request":
			return MessageType.REQUEST;
		case "agree":
			return MessageType.AGREE;
		case "inform":
			return MessageType.INFORM;
		default:
			return MessageType.NONE;
		}
	}

	public static void performUpdates(Map<Integer, Position> agentPositions, Map<Integer, Box> boxes) {
		if (agentPositions != null) {
			for (Integer agentId : agentPositions.keySet()) {
				World.getInstance().getAgents().get(agentId).setPosition(agentPositions.get(agentId));
			}
		}
		if (boxes != null) {
			for (Integer boxId : boxes.keySet()) {
				World.getInstance().getBoxes().put(boxId, boxes.get(boxId));
			}
		}
	}

	public static boolean canMakeNextMove(int index, List<LinkedList<Node>> allSolutions) {
		if (World.getInstance().getAgents().size() == 1) {
			/* This for loop only contains one agent */
			Agent a1 = World.getInstance().getAgents().get(0);
			/*
			 * as there is no other agents that can be in a1's way, the only
			 * obsticle a1 can bump into is a box
			 */
			for (Box box : World.getInstance().getBoxes().values()) {
				if (box.getPosition().equals(a1.getPosition()))
					return false;
			}
		}
		for (Agent a1 : World.getInstance().getAgents().values()) {
			for (Agent a2 : World.getInstance().getAgents().values()) {
				if (a2.getId() != a1.getId()) {
					if (allSolutions.size() > a2.getId() && allSolutions.get(a2.getId()).size() > index) {
						if (allSolutions.size() > a1.getId() && allSolutions.get(a1.getId()).size() > index) {
							Node currAgentSol = allSolutions.get(a1.getId()).get(index);
							Node agentSol = allSolutions.get(a2.getId()).get(index);
							if (currAgentSol.agentRow == agentSol.agentRow && currAgentSol.agentCol == agentSol.agentCol
									|| a1.getPosition().getX() == agentSol.agentRow
											&& a1.getPosition().getY() == agentSol.agentCol) {
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
}