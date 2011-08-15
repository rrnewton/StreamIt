package at.dms.kjc.slir;

import java.util.LinkedList;
import java.util.List;

public abstract class OutputPort {
	protected List<InterSSGEdge> links;
	private StaticSubGraph ssg;
	
	protected OutputPort(StaticSubGraph ssg) {
		this.setSSG(ssg);
		links = new LinkedList<InterSSGEdge>();
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}

	/**
	 * @param link
	 */
	public void addLink(Link link) {
		links.add(link);		
	}

	public StaticSubGraph getSGG() {
		return ssg;
	}

	public void setSSG(StaticSubGraph ssg) {
		this.ssg = ssg;
	}
}
