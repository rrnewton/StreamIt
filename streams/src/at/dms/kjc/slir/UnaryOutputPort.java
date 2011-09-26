package at.dms.kjc.slir;

public class UnaryOutputPort extends OutputPort {
		
	public UnaryOutputPort(StaticSubGraph ssg) {
		super(ssg);
	}
	
	public UnaryOutputPort() {
		// TODO Auto-generated constructor stub
		super(null);
	}

	public void setLink(InterSSGEdge l) {
		links.set(0, l);
	}
	
	public InterSSGEdge getLink() {
		return links.get(0);
	}

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.slir.UnaryOutputPort other = new at.dms.kjc.slir.UnaryOutputPort();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.UnaryOutputPort other) {
        super.deepCloneInto(other);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
