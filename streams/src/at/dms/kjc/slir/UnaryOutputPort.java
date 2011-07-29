package at.dms.kjc.slir;

public class UnaryOutputPort extends OutputPort {
	
	public UnaryOutputPort() {
		super();
	}
	
	public void setLink(Link l) {
		links.set(0, l);
	}
	
	public Link getLink() {
		return links.get(0);
	}
}
