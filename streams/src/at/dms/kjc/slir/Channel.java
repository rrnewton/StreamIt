package at.dms.kjc.slir;

import java.util.HashMap;

import at.dms.kjc.CType;
import at.dms.kjc.backendSupport.FilterInfo;

/**
 *  A channel represents an edge in the partitioned stream graph between or within filters.
 *   
 * @author mgordon
 *
 */
public class Channel implements at.dms.kjc.DeepCloneable {
    public static final String[] DO_NOT_CLONE_THESE_FIELDS = { "src", "dest" };
    /**
     * Source of directed edge in Slice graph
     */
    protected InternalFilterNode src;

    /**
     * Destination of directed edge in Slice graph
     */
    protected InternalFilterNode dest;

    /**
     * Caches type for {@link #getType()} calls
     */
    private CType type;

    /**
     * Full constructor, (type will be inferred from src / dest).
     * @param src   Source assumed to be an OutputNode or a WorkNode.
     * @param dest  Dest assumed to be an InputNode or a WorkNode.
     */
    public Channel(InternalFilterNode src, InternalFilterNode dest) {
        assert src != null;
        assert dest != null;
        this.src = src;
        this.dest = dest;
        type = null;
    }

    /**
     * Partial constructor, for subclasses.
     *
     */
    protected Channel() { }
    
    
    /**
     * @return source InternalFilterNode
     */
    public InternalFilterNode getSrc() {
        return src;
    }

    public Channel(OutputNode src) {
        this.src = src;
    }

    public Channel(InputNode dest) {
        this.dest = dest;
    }

    public CType getType() {
        if (type != null) {
            return type;
        }
        // inter-filter channel
        if (src instanceof OutputNode && dest instanceof InputNode) {
            WorkNodeContent srcContent;
            WorkNodeContent dstContent;
            CType srcType;
            CType dstType;
            srcContent = ((OutputNode)src).getWorkNode().getFilter();
            dstContent = ((InputNode)dest).getWorkNode().getFilter();
            srcType = srcContent.getOutputType();
            dstType = dstContent.getInputType();
            type = dstType;
            assert srcType.equals(dstType) : "Error calculating type: " + 
            srcContent + " -> " + dstContent;
            return type;
        }
        
        // intra-filter channels
        if (src instanceof InputNode && dest instanceof WorkNode) {
            type = ((WorkNode)dest).getFilter().getInputType();
            return type;
        }
        if (src instanceof WorkNode && dest instanceof OutputNode) {
            type = ((WorkNode)src).getFilter().getOutputType();
            return type;
        }
        
        throw new AssertionError ("Unexpected InternalFilterNode connection " + src + " -> " + dest);
    }

    /**
     * @return dest InternalFilterNode
     */
    public InternalFilterNode getDest() {
        return dest;
    }

    /**
     * Set the source InternalFilterNode
     * @param src
     */
    public void setSrc(InternalFilterNode src) {
        this.src = src;
    }

    /**
     * Set the destination InternalFilterNode
     * @param dest
     */
    public void setDest(InternalFilterNode dest) {
        this.dest = dest;
    }

    public String toString() {
        return src + "->" + dest + "(" + hashCode() + ")";
    }


    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.slir.Channel other = new Channel();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.Channel other) {
        other.src = this.src;
        other.dest = this.dest;
        other.type = (at.dms.kjc.CType)at.dms.kjc.AutoCloner.cloneToplevel(this.type);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
