package atoms;

public class Box {
	private int id;
	private Position position;
	private char letter;
	private Color color;
	private boolean isOnGoal;
	
	public Box(int id, Position p, char l, Color c) {
		this.id = id;
		this.position = p;
		this.letter = l;
		this.color = c;
		this.isOnGoal = false;
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

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public boolean isOnGoal() {
		for(Goal goal : World.getInstance().getGoals().values()) {
			if(goal.getPosition().equals(position) && goal.getLetter() == Character.toLowerCase(letter)) {
				return true;
			}
		}
		return false;
	}
	
//	public void setOnGoal(boolean isOnGoal) {
//		this.isOnGoal = isOnGoal;
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + letter;
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Box))
			return false;
		Box other = (Box) obj;
		if (color != other.color)
			return false;
		if (letter != other.letter)
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Box [position=").append(position).append(", letter=").append(letter).append(", color=")
				.append(color).append("]");
		return builder.toString();
	}

}