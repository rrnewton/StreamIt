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

	public List<InterSSGEdge> getLinks() {
		return links;
	}

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

	public void setSSG(StaticSubGraph ssg) {
		this.ssg = ssg;
	}

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() { at.dms.util.Utils.fail("Error in auto-generated cloning methods - deepClone was called on an abstract class."); return null; }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.OutputPort other) {
        other.links = (java.util.List)at.dms.kjc.AutoCloner.cloneToplevel(this.links);
        other.ssg = (at.dms.kjc.slir.StaticSubGraph)at.dms.kjc.AutoCloner.cloneToplevel(this.ssg);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
