package at.dms.kjc.slir;

import at.dms.util.Utils;

/** 
 * Slice class models a slice (joiner, sequence of filters, splitter).
 * Beware: slices are linked with Edges, but the back edge of an InputSliceNode and
 * the forward edge of an OutputSliceNode should be null: they are not related to the
 * InterSliceEdge's of the InputSliceNode and OutputSliceNode.
 *  
 * @author mgordon
 */
public class Filter implements at.dms.kjc.DeepCloneable {
        
	protected InputNode inputNode;
    protected OutputNode outputNode;
    protected WorkNode workNode;
    protected Filter parent;
    

    /**
     * Create slice with an InputSliceNode.
     * "head" is expected to be linked to a FilterSliceNode by the time finish is called.
     * @{link {@link #finish() finish} } will tack on an OutputSliceNode if missing.
     * @param head  the InputSliceNode
     */
    public Filter(InputNode head) {
        this.inputNode = head;
    }

    /**
     * Create slice with a FilterSliceNode.
     * Creates an InputSliceNode automatically and links it with the FilterSliceNode.
     * @param node
     */
    public Filter(WorkNode node) {
        inputNode = new InputNode();
        inputNode.setParent(this);
    }

    protected Filter() {
    }
    
    /**
     * After a slice has been cloned, set up the fields of the slicenodes included 
     * in it.
     */
    public void finishClone() {
        
    }

    /**
     * @return The incoming Slices (Slices) in the partitioned stream graph for this slice (slice). 
     */
    public Filter[] getDependencies(SchedulingPhase phase) {
        Filter[] depends = new Filter[inputNode.getSources(phase).length];
        
        for (int i = 0; i < depends.length; i++)
            depends[i] = inputNode.getSources(phase)[i].getSrc().getParent();
        
        return depends;
    }
  
        
    /**
     * Set the tail of this slice to out.  This method
     * does not fix the intra-slice connections of the slice nodes, but 
     * it does set the parent of the new output slice.
     * 
     * @param out The new output slice node.
     */
    public void setOutputNode(OutputNode out) {
        outputNode = out;
        out.setParent(this);
    }
    
    /**
     * Set the head of this slice to node.  This method
     * does not fix the intra-slice connections of the slice nodes, but 
     * it does set the parent of the new input slice node.
     * 
     * @param node The new input slice node.
     */
    public void setInputNode(InputNode node) {
        inputNode = node;
        node.setParent(this);
    }

    /**
     * Get the work node of this filter
     *   
     * @return The worknode of this filter
     */
    public WorkNode getWorkNode() {
        return workNode;
    }
    
    public void setWorkNode(WorkNode wn) {
    	this.workNode = wn;
    }
    
    /**
     * get the InputSliceNode of the Slice containing this node.
     * @return
     */
    public InputNode getInputNode() {
        return inputNode;
    }

    /**
     * get the OutputSliceNode of the Slice containing this node.
     * @return
     */
    // finish() must have been called
    public OutputNode getOutputNode() {
        return outputNode;
    }

    /**
     * Return a brief string description of this slice.
     * 
     * @return a brief string description of this slice.
     */
    public String getIdent() {
        return inputNode.toString() + outputNode.toString();
    }
    
    public String toString() {
        return "Slice: " + inputNode + "->" + workNode + "->" + outputNode ;
    }


   
    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.slir.Filter other = new at.dms.kjc.slir.Filter();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.Filter other) {
        other.inputNode = (at.dms.kjc.slir.InputNode)at.dms.kjc.AutoCloner.cloneToplevel(this.inputNode);
        other.outputNode = (at.dms.kjc.slir.OutputNode)at.dms.kjc.AutoCloner.cloneToplevel(this.outputNode);
        other.filterNodes = (at.dms.kjc.slir.WorkNode[])at.dms.kjc.AutoCloner.cloneToplevel(this.filterNodes);
        //System.out.println(other.filterNodes[0].hashCode() + " " + filterNodes[0].hashCode());
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
