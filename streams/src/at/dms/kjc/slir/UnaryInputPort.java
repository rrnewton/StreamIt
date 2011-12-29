package at.dms.kjc.slir;

public class UnaryInputPort extends InputPort {

	public UnaryInputPort(StaticSubGraph ssg) {
		super(ssg);
	}
		
	public UnaryInputPort() {
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
    @Override
	public Object deepClone() {
        at.dms.kjc.slir.UnaryInputPort other = new at.dms.kjc.slir.UnaryInputPort();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.UnaryInputPort other) {
        super.deepCloneInto(other);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
