/**
 * 
 */
package at.dms.kjc.backendSupport;

import at.dms.kjc.slir.InternalFilterNode;
import at.dms.kjc.slir.WorkNode;

/**
 * A Layout makes the association between a {@link at.dms.kjc.backendSupport.ComputeNode ComputeNode} and a {@link at.dms.kjc.slir.InternalFilterNode}.
 * @param T  a subtype of {@link at.dms.kjc.backendSupport.ComputeNode ComputeNode}
 * @author mgordon
 *
 */
public interface Layout<T extends at.dms.kjc.backendSupport.ComputeNode> {
    /** Get the ComputeNode for a Slice 
     * @param node : the {@link at.dms.kjc.slir.InternalFilterNode} to look up. 
     * @return the {@link at.dms.kjc.backendSupport.ComputeNode ComputeNode} that should execute the {@link at.dms.kjc.slir.InternalFilterNode}. */
    public T getComputeNode(InternalFilterNode node);
    /** Set the ComputeNode for a Slice 
     * @param node         the {@link at.dms.kjc.slir.InternalFilterNode} to associate with ...
     * @param computeNode  the {@link at.dms.kjc.backendSupport.ComputeNode ComputeNode} where the {@link at.dms.kjc.slir.InternalFilterNode}  should execute.
     */
    public void setComputeNode(InternalFilterNode node, T computeNode);
    /** Do the setup for {@link #getComputeNode(InternalFilterNode) getComputeNode}. */
    public void runLayout();
}