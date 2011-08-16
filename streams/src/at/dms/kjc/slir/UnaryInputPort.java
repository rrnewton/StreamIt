package at.dms.kjc.slir;

public class UnaryInputPort extends InputPort {

	public UnaryInputPort(StaticSubGraph ssg) {
		super(ssg);
	}
		
	public void setLink(InterSSGEdge l) {
		links.set(0, l);
	}
	
	public InterSSGEdge getLink() {
		return links.get(0);
	}
}
