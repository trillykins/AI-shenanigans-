package analysis;

import atoms.Position;

public class FreeSpace implements Comparable<Object> {

	private Position position;

	private int priority;

	private int narrowCorValue;
	private boolean surroundedByTreeWalls;

	public boolean isSurroundedByTreeWalls() {
		return surroundedByTreeWalls;
	}

	public void setSurroundedByTreeWalls(boolean surroundedByTreeWalls) {
		this.surroundedByTreeWalls = surroundedByTreeWalls;
	}

	public FreeSpace(Position position, int priority, int narrowCorValue) {
		this.position = position;
		this.priority = priority;
		this.narrowCorValue = narrowCorValue;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getNarrowCorValue() {
		return narrowCorValue;
	}

	public void setNarrowCorValue(int narrowCorValue) {
		this.narrowCorValue = narrowCorValue;
	}

	@Override
	public int compareTo(Object o) {
		FreeSpace space = (FreeSpace) o;
		if (this.getPriority() >= space.getPriority()) {
			return 1;
		} else {
			return -1;
		}
	}
}