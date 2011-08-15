package at.dms.kjc.slir;

public class UnaryOutputPort extends OutputPort {
	
	public UnaryOutputPort(StaticSubGraph ssg) {
		super(ssg);
	}
	
	public void setLink(InterSSGEdge l) {
		links.set(0, l);
	}
	
	public InterSSGEdge getLink() {
		return links.get(0);
	}
}
