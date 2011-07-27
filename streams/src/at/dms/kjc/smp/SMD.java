package at.dms.kjc.smp;

import at.dms.kjc.sir.*;
import at.dms.kjc.slir.SliceNode;

public class SMD extends Scheduler {

    public void run(int tiles) {
        
    }
    
    /** Get the tile for a Slice 
     * @param node the {@link at.dms.kjc.slir.SliceNode} to look up. 
     * @return the tile that should execute the {@link at.dms.kjc.slir.SliceNode}. 
     */
    public Core getComputeNode(SliceNode node) {
        return null;
    }
    
    
    /** Set the Tile for a Slice 
     * @param node         the {@link at.dms.kjc.slir.SliceNode} to associate with ...
     * @param tile   The tile to assign the node
     */
    public void setComputeNode(SliceNode node, Core tile) {
        
    }
    

    public void runLayout() {
        
    }
}
