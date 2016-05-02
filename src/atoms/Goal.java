package atoms;

public class Goal {
	private int id;
	private Position position;
	private char letter;
	private int priority;
	
	public Goal(int id, Position position, char letter, Color color, int priority) {
		this.id = id;
		this.position = position;
		this.letter = letter;
		this.priority = priority;
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
			if(position.equals(box.getPosition()) && Character.toLowerCase(box.getLetter()) == letter) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "Goal [id=" + id + ", position=" + position + ", letter=" + letter + ", priority=" + priority + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + letter;
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + priority;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Goal))
			return false;
		Goal other = (Goal) obj;
		if (id != other.id)
			return false;
		if (letter != other.letter)
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (priority != other.priority)
			return false;
		return true;
	}

}