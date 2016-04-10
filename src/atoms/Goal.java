package atoms;

public class Goal {
	private int id;
	private Position position;
	private char letter;
	
	public Goal(int id, Position position, char letter, Color color) {
		this.id = id;
		this.position = position;
		this.letter = letter;
	}
	
	public Goal(int id, int x, int y, char letter, Color color) {
		this(id, new Position(x, y), letter, color);
	}
	
	public Position getPosition() {
		return position;
	}
	public void setPosition(Position position) {
		this.position = position;
	}
	public char getLetter() {
		return letter;
	}
	public void setLetter(char letter) {
		this.letter = letter;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}