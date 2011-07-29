package at.dms.kjc.slir;

import at.dms.util.Utils;
import java.util.ArrayList;

/** 
 * Slice class models a slice (joiner, sequence of filters, splitter).
 * Beware: slices are linked with Edges, but the back edge of an InputSliceNode and
 * the forward edge of an OutputSliceNode should be null: they are not related to the
 * InterSliceEdge's of the InputSliceNode and OutputSliceNode.
 *  
 * @author mgordon
 */
public class Filter implements at.dms.kjc.DeepCloneable {
        
	protected InputNode head;
    protected OutputNode tail;
    protected WorkNode workNode;
    
    /*
     * public Slice (Slice[] edges, Slice[] depends, InputSliceNode head) { if
     * (edges == null) this.edges = new Slice[0]; else this.edges = edges;
     * 
     * this.head = head; head.setParent(this);
     * 
     * if (depends == null) this.depends = new Slice[0]; else this.depends =
     * depends; len=-1; }
     */

    /**
     * Create slice with an InputSliceNode.
     * "head" is expected to be linked to a FilterSliceNode by the time finish is called.
     * @{link {@link #finish() finish} } will tack on an OutputSliceNode if missing.
     * @param head  the InputSliceNode
     */
    public Filter(InputNode head) {
        this.head = head;
        head.setParent(this);
    }

    /**
     * Create slice with a FilterSliceNode.
     * Creates an InputSliceNode automatically and links it with the FilterSliceNode.
     * @param node
     */
    public Filter(SliceNode node) {
        if (!(node instanceof WorkNode))
            Utils.fail("FilterSliceNode expected: " + node);
        head = new InputNode();
        head.setParent(this);
        head.setNext(node);
        node.setPrevious(head);
       
    }

    protected Filter() {
    }
    
    /**
     * After a slice has been cloned, set up the fields of the slicenodes included 
     * in it.
     */
    public void finishClone() {
      finish();
    }
    
    /**
     * Finishes creating Slice.
     * Expects the slice to have an InputSliceNode, and 1 or more FilterSliceNodes. 
     * Creates an OutputSliceNode if necessary.
     * 
     * @return The number of FilterSliceNodes.
     */
    public void finish() {
        //set the head refs
        head.setParent(this);
        head.setNext(workNode);
        
        workNode.setParent(this);
        
        //set the tail's structures
        tail.setParent(this);
        tail.setPrevious(workNode);
    }

    /**
     * @return The incoming Slices (Slices) in the partitioned stream graph for this slice (slice). 
     */
    public Filter[] getDependencies(SchedulingPhase phase) {
        Filter[] depends = new Filter[head.getSources(phase).length];
        
        for (int i = 0; i < depends.length; i++)
            depends[i] = head.getSources(phase)[i].getSrc().getParent();
        
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
        tail = out;
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
        head = node;
        node.setParent(this);
    }

    public WorkNodeContent getWorkNodeContent() {
    	return workNode.getFilter();
    }
    
    /**
     * Get the first FilterSliceNode of this slice.
     *   
     * @return The first FilterSliceNode of this Slice.
     */
    public WorkNode getWorkNode() {
        return workNode;
    }
    
    /**
     * get the InputSliceNode of the Slice containing this node.
     * @return
     */
    public InputNode getInputNode() {
        return head;
    }

    /**
     * get the OutputSliceNode of the Slice containing this node.
     * @return
     */
    // finish() must have been called
    public OutputNode getOutputNode() {
        return tail;
    }

    /**
     * Return a brief string description of this slice.
     * 
     * @return a brief string description of this slice.
     */
    public String getIdent() {
        return head.toString() + tail.toString();
    }
    
    public String toString() {
        return "Slice: " + head + "->" + head.getNext() + "->...";
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
        other.head = (at.dms.kjc.slir.InputNode)at.dms.kjc.AutoCloner.cloneToplevel(this.head);
        other.tail = (at.dms.kjc.slir.OutputNode)at.dms.kjc.AutoCloner.cloneToplevel(this.tail);
        other.workNode = (at.dms.kjc.slir.WorkNode)at.dms.kjc.AutoCloner.cloneToplevel(this.workNode);
        //System.out.println(other.filterNodes[0].hashCode() + " " + filterNodes[0].hashCode());
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
