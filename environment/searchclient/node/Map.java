package searchclient.node;

import java.awt.Point;

public class Map {

	public int row, col;

	public Map(int row, int col) {
		this.row = row;
		this.col = col;
	}

	public boolean isAt(int row, int col) {
		return (this.row == row && this.col == col);
	}

	public Point getPoint() {
		return new Point(this.row, this.col);
	}

	@Override
	public boolean equals(Object object) {
		if (getClass() != object.getClass()) {
			return false;
		}
		Map map = (Map)object;
		return (this.row == map.row && this.col == map.col);
	}

	@Override
	public int hashCode() {
		final int prime = 41;
		int result = 7;
		result = prime * result + this.row;
		result = prime * result + this.col;
		return result;
	}

	@Override
	public String toString(){
		return "(" + row + "," + col + ")";
	}
}