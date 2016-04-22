package searchclient.node;

import searchclient.node.Colour;

public class MapAgent extends Map {
	public int id;
	public Colour colour;

	public MapAgent(MapAgent agent) {
		super(agent.row, agent.col);
		this.id = agent.id;
		this.colour = agent.colour;
	}	

	public MapAgent(int id, Colour colour, int row, int col) {
		super(row, col);
		this.id = id;
		if (colour == null) {
			this.colour = Colour.BLUE;
		} else {
			this.colour = colour;
		}
	}

	@Override
	public boolean equals(Object object) {
		if (getClass() != object.getClass()) {
			return false;
		}
		if (!super.equals(object)) {
			return false;
		}
		MapAgent agent = (MapAgent)object;
		return (this.id == agent.id && this.colour == agent.colour);
	}

	@Override
	public int hashCode() {
		final int prime = 37;
		int result = 5;
		result = prime * result + super.hashCode();
		result = prime * result + this.id;
		return result;
	}

	@Override
	public String toString() {
		return id + " " + colour + " " + super.toString();
	}
}