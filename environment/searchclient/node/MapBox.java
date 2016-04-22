package searchclient.node;

import searchclient.node.Colour;

public class MapBox extends Map {
	static int boxID = 0;
	private char letter;
	public Colour colour;
	public int id;

	public MapBox(MapBox box) {
		super(box.row, box.col);
		this.id = box.id;
		this.colour = box.colour;
		this.setLetter(box.getLetter());
	}

	public MapBox(char letter, Colour colour, int row, int col) { 
		super(row, col);
		this.setLetter(letter);
		this.id = boxID++;
		if (colour == null) {
			this.colour = Colour.BLUE;
		} else {
			this.colour = colour;
		}
	}

	public char getLetter() {
		return letter;
	}

	public void setLetter(char letter) {
		this.letter = Character.toLowerCase(letter);
	}

	@Override
	public boolean equals(Object object) {
		if (getClass() != object.getClass()) {
			return false;
		}
		if (!super.equals(object)) {
			return false;
		}
		MapBox box = (MapBox)object;
		return (this.id == box.id && this.getLetter() == box.getLetter() && this.colour == box.colour);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 17;
		result = prime * result + super.hashCode();
		result = prime * result + this.letter;
		return result;
	}

	@Override
	public String toString(){
		return letter + " " + super.toString();
	}
}