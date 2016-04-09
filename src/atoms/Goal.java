package atoms;

public class Goal {
	private Position position;
	private char letter;
	
	public Goal(Position position, char letter, Color color) {
		this.position = position;
		this.letter = letter;
	}
	
	public Goal(int x, int y, char letter, Color color) {
		this(new Position(x, y), letter, color);
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
}
