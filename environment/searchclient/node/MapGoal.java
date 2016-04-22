package searchclient.node;

public class MapGoal extends Map {
	public int importance = 0;
	private char letter;

	public MapGoal(char letter, int row, int col) {
		super(row, col);
		this.letter = letter;
	}

	public char getLetter() {
		return letter;
	}

	@Override
	public boolean equals(Object object) {
		if (getClass() != object.getClass()) {
			return false;
		}
		if (!super.equals(object)) {
			return false;
		}
		MapGoal goal = (MapGoal)object;
		return (this.letter == goal.letter);
	}

	@Override
	public String toString() {
		return letter + " " + super.toString();
	}
}