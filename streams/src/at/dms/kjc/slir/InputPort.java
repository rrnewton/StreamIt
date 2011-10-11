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

	public StaticSubGraph getSSG() {
		return ssg;
	}

	public void setSSH(StaticSubGraph ssg) {
		this.ssg = ssg;
	}
	
	public OutputPort getAdjacentPort(int i) {
		if (links.size() <= i) {
			return null;
		}
		return links.get(i).getSrc();		
	}

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() { at.dms.util.Utils.fail("Error in auto-generated cloning methods - deepClone was called on an abstract class."); return null; }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.InputPort other) {
        other.links = (java.util.List)at.dms.kjc.AutoCloner.cloneToplevel(this.links);
        other.ssg = (at.dms.kjc.slir.StaticSubGraph)at.dms.kjc.AutoCloner.cloneToplevel(this.ssg);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
