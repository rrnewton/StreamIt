package at.dms.kjc.slir;


/**
 * SliceNode's are a doubly-linked list with a parent pointer to a Slice.
 * They can be specialized into {@link InputNode}, {@link WorkNode}, or {@link OutputNode}. 
 */
public class InternalFilterNode implements at.dms.kjc.DeepCloneable      {
    public static final String[] DO_NOT_CLONE_THESE_FIELDS = { "toNext", "toPrev" };

    private IntraFilterEdge toNext = null;  // internal to slice: remains null for OutputSliceNode
    private IntraFilterEdge toPrev = null;  // internal to slice: remains null for InputSliceNode

    private Filter parent;

    public InternalFilterNode getNext() {
        return (toNext == null)? null : toNext.getDest();
    }

    public InternalFilterNode getPrevious() {
        return (toPrev == null)? null : toPrev.getSrc();
    }

    public IntraFilterEdge getEdgeToNext() {
        return toNext;
    }
    
    public IntraFilterEdge getEdgeToPrev() {
        return toPrev;
    }

    private void setNextEdge(IntraFilterEdge edge) {
        toNext = edge;
    }
    

    private void setPrevEdge(IntraFilterEdge edge) {
        toPrev = edge;
    }
    
    /**
     * Set the IntraSliceEdge pointing to previous to prev, by creating a new edge.  
     * Also, set prev's next edge to the newly created edge. 
     * 
     * @param prev The new previous node
     */
    public void setPrevious(InternalFilterNode prev) {
        assert ! (this instanceof InputNode); 
        toPrev = new IntraFilterEdge(prev,this);
        prev.setNextEdge(toPrev);
    }

    /**
     * Set the intraslicenedge pointing to the next node to next by creating 
     * a new edge.  Also, set next's edge to the newly created edge.
     * 
     * @param next The new next node
     */
    public void setNext(InternalFilterNode next) {
        assert ! (this instanceof OutputNode);
        toNext = new IntraFilterEdge(this, next);
        next.setPrevEdge(toNext);
    }

//    public Edge getEdgeToPrev() {
//        return toPrev;
//    }
    
    public boolean isInputSlice() {
        return this instanceof InputNode;
    }

    public boolean isWorkNode() {
        return this instanceof WorkNode;
    }

    public boolean isOutputSlice() {
        return this instanceof OutputNode;
    }

    public InputNode getAsInput() {
        return (InputNode)this;
    }
    
    public OutputNode getAsOutput() {
        return (OutputNode)this;
    }
    
    public WorkNode getAsFilter() {
        return (WorkNode) this;
    }
    
    /**
     * Had been some assertion checking: removed.
     * Now does nothing.
     *
     */
    protected InternalFilterNode() {
    }
    
    public void setParent(Filter par) {
        parent = par;
    }

    public Filter getParent() {
        //System.out.println(this);
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
        other.toNext = this.toNext;
        other.toPrev = this.toPrev;
        other.parent = (at.dms.kjc.slir.Filter)at.dms.kjc.AutoCloner.cloneToplevel(this.parent);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
