package at.dms.kjc.slir;

public class UnaryInputPort extends InputPort {

	public UnaryInputPort() {
		super();
	}
	
	public void setLink(Link l) {
		links.set(0, l);
	}
	
	public Link getLink() {
		return links.get(0);
	}
}
