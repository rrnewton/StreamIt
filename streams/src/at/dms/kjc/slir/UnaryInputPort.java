package at.dms.kjc.slir;

public class UnaryInputPort extends InputPort {

	public UnaryInputPort() {
		super();
	}
	
	public void setLink(InterSSGEdge l) {
		links.set(0, l);
	}
	
	public InterSSGEdge getLink() {
		return links.get(0);
	}
}
