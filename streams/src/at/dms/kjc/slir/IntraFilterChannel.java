package at.dms.kjc.slir;

public class IntraFilterChannel extends Channel implements at.dms.kjc.DeepCloneable {
    /**
     * No argument constructor, FOR AUTOMATIC CLONING ONLY.
     */
    private IntraFilterChannel() {
        super();
    }
    
    public IntraFilterChannel(InternalFilterNode src, InternalFilterNode dst) {
        super(src, dst);
        //assert src.getParent() == dst.getParent(); //can't assume that parent pointer is set up
        //could use a lot more checking here, but at this point, not really crucial
    }

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.slir.IntraFilterChannel other = new at.dms.kjc.slir.IntraFilterChannel();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.IntraFilterChannel other) {
        super.deepCloneInto(other);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
