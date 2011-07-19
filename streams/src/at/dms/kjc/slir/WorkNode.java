package at.dms.kjc.slir;

//import at.dms.kjc.sir.*;
import java.util.HashMap;

import at.dms.kjc.backendSupport.Layout;
/** 
 * A {@link InternalFilterNode} that references a {@link WorkNodeContent}.
 **/
public class WorkNode extends InternalFilterNode implements at.dms.kjc.DeepCloneable
{
    private WorkNodeContent content;
   
    private boolean predefined;
    private boolean laidout;
  

    private static HashMap<WorkNodeContent, WorkNode> contentToNode;
    
    static {
        contentToNode = new HashMap<WorkNodeContent, WorkNode>();
    }
    
    /**
     * No argument constructor, FOR AUTOMATIC CLONING ONLY.
     */
    private WorkNode() {
        super();
    }

    public WorkNode(WorkNodeContent filter) {
        predefined = (filter instanceof PredefinedContent);
        this.content = filter;
        laidout = false;
        contentToNode.put(filter, this);
    }
    
    public static WorkNode getContent(WorkNodeContent f) {
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

    public WorkNodeContent getFilter() {
        return content;
    }

    public String toString() {
        return content.toString();   
    }
    
    public String toString(Layout layout) 
    {
        return content.toString() + " " + 
        (layout != null ? layout.getComputeNode(this) : "");   
    }
    
    
    public boolean isFileInput()
    {
        return (content instanceof FileInputContent);
    }
    
    public boolean isFileOutput() 
    {
        return (content instanceof FileOutputContent);
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
        other.content = (at.dms.kjc.slir.WorkNodeContent)at.dms.kjc.AutoCloner.cloneToplevel(this.content);
        other.predefined = this.predefined;
        other.laidout = this.laidout;
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}



