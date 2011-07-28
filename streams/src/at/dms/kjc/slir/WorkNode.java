package at.dms.kjc.slir;

//import at.dms.kjc.sir.*;
import at.dms.kjc.backendSupport.Layout;

import java.util.*;
/** 
 * A {@link SliceNode} that references a {@link FilterContent}.
 **/
public class WorkNode extends SliceNode implements at.dms.kjc.DeepCloneable
{
    private FilterContent filter;
   
    private boolean predefined;
    private boolean laidout;

    private static HashMap<FilterContent, WorkNode> contentToNode;
    
    static {
        contentToNode = new HashMap<FilterContent, WorkNode>();
    }
    
    /**
     * No argument constructor, FOR AUTOMATIC CLONING ONLY.
     */
    private WorkNode() {
        super();
    }

    public WorkNode(FilterContent filter) {
        predefined = (filter instanceof PredefinedContent);
        this.filter = filter;
        laidout = false;
        contentToNode.put(filter, this);
    }
    
    public static WorkNode getFilterNode(FilterContent f) {
        return contentToNode.get(f);
    }
    
    public boolean isPredefined() 
    {
        return predefined;
    }

    public boolean isAssignedTile() 
    {
        return laidout;
    }

    public FilterContent getFilter() {
        return filter;
    }

    public String toString() {
        return filter.toString();   
    }
    
    public String toString(Layout layout) 
    {
        return filter.toString() + " " + 
        (layout != null ? layout.getComputeNode(this) : "");   
    }
    
    
    public boolean isFileInput()
    {
        return (filter instanceof FileInputContent);
    }
    
    public boolean isFileOutput() 
    {
        return (filter instanceof FileOutputContent);
    }

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.slir.WorkNode other = new at.dms.kjc.slir.WorkNode();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.WorkNode other) {
        super.deepCloneInto(other);
        other.filter = (at.dms.kjc.slir.FilterContent)at.dms.kjc.AutoCloner.cloneToplevel(this.filter);
        other.predefined = this.predefined;
        other.laidout = this.laidout;
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}



