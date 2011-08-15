package at.dms.kjc.slir;

public class UnaryOutputPort extends OutputPort {
	
	public UnaryOutputPort() {
		super();
	}
	
	public void setLink(InterSSGEdge l) {
		links.set(0, l);
	}
	
	public InterSSGEdge getLink() {
		return links.get(0);
	}
}
