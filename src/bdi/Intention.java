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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((box == null) ? 0 : box.hashCode());
		result = prime * result + ((desire == null) ? 0 : desire.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Intention))
			return false;
		Intention other = (Intention) obj;
		if (box == null) {
			if (other.box != null)
				return false;
		} else if (!box.equals(other.box))
			return false;
		if (desire == null) {
			if (other.desire != null)
				return false;
		} else if (!desire.equals(other.desire))
			return false;
		return true;
	}
}