package at.dms.kjc.slir;



/**
 * SliceNode's are a doubly-linked list with a parent pointer to a Slice.
 * They can be specialized into {@link InputNode}, {@link WorkNode}, or {@link OutputNode}. 
 */
public class InternalFilterNode implements at.dms.kjc.DeepCloneable 
{

    /** parent filter */
    protected Filter parent;
 
    
    public boolean isInputNode() {
        return this instanceof InputNode;
    }

    public boolean isWorkNode() {
        return this instanceof WorkNode;
    }

    public boolean isOutputNode() {
        return this instanceof OutputNode;
    }

    public InputNode getAsInput() {
        return (InputNode)this;
    }
    
    public OutputNode getAsOutput() {
        return (OutputNode)this;
    }
    
    public WorkNode getAsWork() {
        return (WorkNode) this;
    }
    
   
    public void setParent(Filter par) {
        parent = par;
    }

    public Filter getParent() {
        assert parent != null : "parent not set for slice node";
        return parent;
    }

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.slir.InternalFilterNode other = new at.dms.kjc.slir.InternalFilterNode();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.InternalFilterNode other) {
        other.parent = (at.dms.kjc.slir.Filter)at.dms.kjc.AutoCloner.cloneToplevel(this.parent);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
