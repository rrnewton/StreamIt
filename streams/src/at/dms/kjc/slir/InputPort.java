package at.dms.kjc.slir;

import java.util.LinkedList;
import java.util.List;

public abstract class InputPort {

	protected List<InterSSGEdge> links;
	
	private StaticSubGraph ssg;
	
	/**
	 * Create a new InputPort
	 * @param ssg 
	 */
	protected InputPort(StaticSubGraph ssg) {
		this.setSSH(ssg);
		links = new LinkedList<InterSSGEdge>();
	}

	/**
	 * 
	 * @return
	 */
	public List<InterSSGEdge> getLinks() {
		return links;
	}

	/**
	 * 
	 * @param links
	 */
	public void setLinks(List<InterSSGEdge> links) {
		this.links = links;
	}

	/**
	 * @param link
	 */
	public void addLink(InterSSGEdge link) {
		links.add(link);
	}

	public StaticSubGraph getSSH() {
		return ssg;
	}

	public void setSSH(StaticSubGraph ssg) {
		this.ssg = ssg;
	}
	
	public OutputPort getAdjacentPort(int i) {
		if (links.size() <= i) {
			return null;
		}
		return links.get(i).getOutputPort();		
	}
}
