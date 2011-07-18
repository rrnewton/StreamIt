/**
 * 
 */
package at.dms.kjc.slicegraph;

import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InputNode;
import at.dms.kjc.slir.InternalFilterNode;
import at.dms.kjc.slir.OutputNode;
import at.dms.kjc.slir.WorkNode;

/**
 * A SimpleSlice is a Slice with exactly one {@link WorkNode}.
 * 
 * @author dimock
 *
 */
public class SimpleSlice extends Filter implements at.dms.kjc.DeepCloneable {

    protected WorkNode body;
    
    /** Constructor: creates a slice with one filter and sets
     * previous parent and next links the supplied
     * InputSliceNode, FilterSliceNode, and OutputSliceNode.
     * <br/>
     * One of head, body, tail must be non-null.
     * If body is null, then a FilterSliceNode for the body
     * must be reachable from the head or the tail.
     * If head is null and no InputSliceNode is connected to body,
     * then a default InputSliceNode is generated.
     * If tail is null and no OutputSliceNode is connected to body,
     * then a default OutputSliceNode is created.
     * @param head InputSliceNode at head of slice
     * @param body FilterSliceNode in simple slice.
     * @param tail OutputSliceNode at tail of slice.
     */
    public SimpleSlice(InputNode head, 
       WorkNode body, OutputNode tail) {
            if (body == null && head != null) {
                body = (WorkNode)head.getNext();
            }
            if (tail == null && body != null) {
                tail = (OutputNode)body.getNext();
            }
            if (body == null && tail != null) {
                body = (WorkNode)tail.getPrevious();
            }
            if (head == null && body != null) {
                head = (InputNode)body.getPrevious();
            }
            if (head == null) {
                head = new InputNode();
            }
            if (tail == null) {
                tail = new OutputNode();
            }
            assert body != null : "SimpleSlice must be created with a non-null body.";
            
            this.head = head;
            this.body = body; 
            this.tail = tail;
            head.setParent(this);
            body.setParent(this);
            tail.setParent(this);
            len = 1;
            head.setNext(body);
            body.setPrevious(head);
            body.setNext(tail);
            tail.setPrevious(body);
    }
    
    protected SimpleSlice(){};
    
    /**
     * @param head
     */
    public SimpleSlice(InputNode head) {
        this(head,null,null);
    }

    /**
     * @param node
     */
    public SimpleSlice(InternalFilterNode node) {
        this(null,(WorkNode)node,null);
    }

    /**
     * Not needed for SimpleSlice, kept as a sanity check.
     */
    public int finish() {
        super.finish();
        
        assert head.getNext() == body : head.getNext() + " " + body.getPrevious();
        assert tail.getPrevious() == body;
        assert head.getParent() == this; 
        assert body.getParent() == this;
        assert tail.getParent() == this;
        return 1;
    }
    
    /**
     * Should not need to be called for SimpleSlice: always 1.
     */
    public int getNumFilters() {
        return 1;
    }
    
    
    /**
     * For SimpleSlice: call {@link #getBody()} instead.
     * For compatability with Slice, returns a one-element list of
     * FilterSliceNode.
      * @return (singleton) list the filter slice nodes, in data flow order, unmodifiable.
    */
    public java.util.List<WorkNode> getFilterNodes() {
        return java.util.Collections.singletonList(body);
    }

    /**
     * Preferred way to access body of a SimpleSlice.
     * @return the FilterSliceNode
     */
    public WorkNode getBody() {
        return body;
    }
    
    /**
     * Set the body.
     * Updates parent pointer in the body, but not the previous
     * or next pointers.
     * @param body a FilterSliceNode.
     */
    public void setBody(WorkNode body) {
        this.body = body; 
        body.setParent(this);
    }
    
    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */    
    
    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.slicegraph.SimpleSlice other = new at.dms.kjc.slicegraph.SimpleSlice();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slicegraph.SimpleSlice other) {
        super.deepCloneInto(other);
        other.body = (at.dms.kjc.slir.WorkNode)at.dms.kjc.AutoCloner.cloneToplevel(this.body);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
