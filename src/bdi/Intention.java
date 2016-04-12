package bdi;

public class Intention {
	private Desire desire;
//	private LinkedList<Node> plan;
//	private Box box;

	public Intention(Desire desire/*, Box box*, LinkedList<Node> plan*/) {
		this.desire = desire;
//		this.box = box;
//		this.plan = plan;
	}
	
//	public Box getBox() {
//		return box;
//	}
//
//	public void setBox(Box box) {
//		this.box = box;
//	}

	public Desire getDesire() {
		return desire;
	}
	public void setDesire(Desire desire) {
		this.desire = desire;
	}
//	public LinkedList<Node> getPlan() {
//		return plan;
//	}
//	public void setPlan(LinkedList<Node> plan) {
//		this.plan = plan;
//	}
}