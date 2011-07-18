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
        
    //The head of the slice.
    protected InputNode head;
    //the Tail of the slice.
    protected OutputNode tail;
    //The length of the slice.
    protected int len;
    protected WorkNode[] filterNodes;
    
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
        len = -1;
    }

    /**
     * Create slice with a FilterSliceNode.
     * Creates an InputSliceNode automatically and links it with the FilterSliceNode.
     * @param node
     */
    public Filter(InternalFilterNode node) {
        if (!(node instanceof WorkNode))
            Utils.fail("FilterSliceNode expected: " + node);
        head = new InputNode();
        head.setParent(this);
        head.setNext(node);
        node.setPrevious(head);
        len = -1;
    }

    protected Filter() {
    }
    
    /**
     * After a slice has been cloned, set up the fields of the slicenodes included 
     * in it.
     */
    public void finishClone() {
        
        //set the head refs
        head.setParent(this);
        head.setNext(filterNodes[0]);
        
        //set the filternodes' prev, head, and parent
        for (int i = 0; i < filterNodes.length; i++) {
            filterNodes[i].setParent(this);
            if (i == 0)         
                filterNodes[i].setPrevious(head);
            else
                filterNodes[i].setPrevious(filterNodes[i-1]);
            
            if (i == filterNodes.length - 1) 
                filterNodes[i].setNext(tail);
            else
                filterNodes[i].setNext(filterNodes[i+1]);
        }
        
        //set the tail's structures
        tail.setParent(this);
        tail.setPrevious(filterNodes[filterNodes.length -1]);
    }
    
    /**
     * Finishes creating Slice.
     * Expects the slice to have an InputSliceNode, and 1 or more FilterSliceNodes. 
     * Creates an OutputSliceNode if necessary.
     * 
     * @return The number of FilterSliceNodes.
     */
    public int finish() {
        int size = 0;
        InternalFilterNode node = head.getNext();
        InternalFilterNode end = node;
        while (node != null && node instanceof WorkNode) {
            node.setParent(this);
            size++;
            end = node;
            node = node.getNext();
        }
        if (node != null)
            end = node;
        len = size;
        if (end instanceof OutputNode)
            tail = (OutputNode) end;
        else {
            tail = new OutputNode();
            end.setNext(tail);
            tail.setPrevious(end);
        }
        tail.setParent(this);
        //set the filterNodes array
        filterNodes = new WorkNode[size];
        int i = 0;
        node = getHead().getNext();
        //remember that setting a node's next will also set the next node's previous edge
        //so no need to set the previous edges explicitly 
        getHead().setNext(node);
        while (node.isFilterSlice()) {
            filterNodes[i++] = node.getAsFilter();
            node.setNext(node.getNext());
            node = node.getNext();
        }
        assert i == size;
        return size;
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
    
    // finish() must have been called
    public int size() {
        assert len > -1 : "finish() was not called";
        return len;
    }

        
    /**
     * Set the tail of this slice to out.  This method
     * does not fix the intra-slice connections of the slice nodes, but 
     * it does set the parent of the new output slice.
     * 
     * @param out The new output slice node.
     */
    public void setTail(OutputNode out) {
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
    public void setHead(InputNode node) {
        head = node;
        node.setParent(this);
    }

    /**
     * Get the first FilterSliceNode of this slice.
     *   
     * @return The first FilterSliceNode of this Slice.
     */
    public WorkNode getFirstFilter() {
        return head.getNextFilter();
    }
    
    /**
     * get the InputSliceNode of the Slice containing this node.
     * @return
     */
    public InputNode getHead() {
        return head;
    }

    /**
     * get the OutputSliceNode of the Slice containing this node.
     * @return
     */
    // finish() must have been called
    public OutputNode getTail() {
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


    // return the number of filters in the slice
    public int getNumFilters() {
        InternalFilterNode node = getHead().getNext();
        int ret = 0;
        while (node instanceof WorkNode) {
            node = node.getNext();
            ret++;
        }
        assert ret == filterNodes.length;
        return ret;
    }


    /** 
     * @return list the filter slice nodes, in data flow order, unmodifiable.
     */
    public java.util.List<WorkNode> getFilterNodes() {
        return java.util.Collections.unmodifiableList(java.util.Arrays.asList(filterNodes));
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
        other.len = this.len;
        other.filterNodes = (at.dms.kjc.slir.WorkNode[])at.dms.kjc.AutoCloner.cloneToplevel(this.filterNodes);
        //System.out.println(other.filterNodes[0].hashCode() + " " + filterNodes[0].hashCode());
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
