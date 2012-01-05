package at.dms.kjc.slir;

//import at.dms.kjc.sir.*;
import java.util.HashMap;

import at.dms.kjc.backendSupport.ComputeNode;
import at.dms.kjc.backendSupport.Layout;
/** 
 * A {@link InternalFilterNode} that references a {@link WorkNodeContent}.
 **/
public class WorkNode extends InternalFilterNode implements at.dms.kjc.DeepCloneable
{
    private WorkNodeContent workNodeContent;
   
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

    public WorkNode(WorkNodeContent content) {
        predefined = (content instanceof PredefinedContent);
        this.workNodeContent = content;
        laidout = false;
        contentToNode.put(content, this);
    }
    
    public static WorkNode getFilterNode(WorkNodeContent f) {
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

    public WorkNodeContent getWorkNodeContent() {
        return workNodeContent;
    }

    @Override
	public String toString() {
        return workNodeContent.toString();   
    }
    
    public <T extends ComputeNode<?>> String toString(Layout<T> layout) 
    {
        return workNodeContent.toString() + " " + 
        (layout != null ? layout.getComputeNode(this) : "");   
    }
    
    
    public boolean isFileInput()
    {
        return (workNodeContent instanceof FileInputContent);
    }
    
    public boolean isFileOutput() 
    {
        return (workNodeContent instanceof OutputContent);
    }
    
    public boolean hasIO() {
    	
    	return workNodeContent.hasIO();    	
	}

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    @Override
	public Object deepClone() {
        at.dms.kjc.slir.WorkNode other = new at.dms.kjc.slir.WorkNode();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.WorkNode other) {
        super.deepCloneInto(other);
        other.workNodeContent = (at.dms.kjc.slir.WorkNodeContent)at.dms.kjc.AutoCloner.cloneToplevel(this.workNodeContent);
        other.predefined = this.predefined;
        other.laidout = this.laidout;
    }
	

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}



