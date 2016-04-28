package atoms;

public class Goal {
	private int id;
	private Position position;
	private char letter;
	private int priority;
	private boolean solved;
	
	public Goal(int id, Position position, char letter, Color color, int priority) {
		this.id = id;
		this.position = position;
		this.letter = letter;
		this.priority = priority;
		this.solved = false;
	}
	
	public Goal(int id, int x, int y, char letter, Color color, int priority) {
		this(id, new Position(x, y), letter, color, priority);
	}
	
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
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
	
	public boolean isSolved() {
		for(Box box : World.getInstance().getBoxes().values()) {
			if(box.getPosition().equals(position) && Character.toLowerCase(box.getLetter()) == letter)
				return true;
		}
		return false;
	}
	
//	public void setSolved(boolean solved) {
//		this.solved = solved;
//	}

	@Override
	public String toString() {
		return "Goal [id=" + id + ", position=" + position + ", letter=" + letter + ", priority=" + priority + "]";
	}

}