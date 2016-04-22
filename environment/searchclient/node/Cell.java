package searchclient.node;

public class Cell {
	public enum Type { 
		SPACE,
		WALL,
		GOAL,
		BOX,
		AGENT
	}
	public Type type;
	public char letter;

	public Cell(Type type) {
		this.type = type;
	}

	public Cell(Type type, char letter) {
		this.letter = letter;
		this.type = type;
	}
}