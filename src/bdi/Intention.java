package bdi;

import atoms.Box;

public class Intention {
	private Desire desire;
	private Box box;

	public Intention(Desire desire, Box box) {
		this.desire = desire;
		this.box = box;
	}
	
	public Box getBox() {
		return box;
	}

	public void setBox(Box box) {
		this.box = box;
	}

	public Desire getDesire() {
		return desire;
	}
	public void setDesire(Desire desire) {
		this.desire = desire;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Intention [desire=").append(desire).append(", box=").append(box).append("]");
		return builder.toString();
	}
}