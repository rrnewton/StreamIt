package at.dms.kjc.smp;

import at.dms.kjc.slir.InternalFilterNode;

public class SMD extends Scheduler {

    @Override
	public void run(int tiles) {
        
    }
    
    /** Get the tile for a Slice 
     * @param node the {@link at.dms.kjc.slir.InternalFilterNode} to look up. 
     * @return the tile that should execute the {@link at.dms.kjc.slir.InternalFilterNode}. 
     */
    @Override
	public Core getComputeNode(InternalFilterNode node) {
        return null;
    }
    
    
    /** Set the Tile for a Slice 
     * @param node         the {@link at.dms.kjc.slir.InternalFilterNode} to associate with ...
     * @param tile   The tile to assign the node
     */
    @Override
	public void setComputeNode(InternalFilterNode node, Core tile) {
        
    }
    

    @Override
	public void runLayout() {
        
    }
}
