package at.dms.kjc.tilera;

import java.util.HashMap;

import at.dms.kjc.backendSupport.Layout;
import at.dms.kjc.backendSupport.SpaceTimeScheduleAndSlicer;
import at.dms.kjc.slir.InternalFilterNode;

/**
 * This class is the super class of all partitioners that act on the SIR graph to
 * data-parallelize the application.  Currently we support space-multiplexed and
 * time-multiplexed data parallel partitioners.  
 * 
 * @author mgordon
 *
 */
public abstract class Scheduler implements Layout<Tile> {
    
    protected SpaceTimeScheduleAndSlicer graphSchedule;
    protected HashMap<InternalFilterNode, Tile> layoutMap;
    
    public Scheduler() {
        graphSchedule = null;
        layoutMap = new HashMap<InternalFilterNode, Tile>();
    }

    public boolean isSMD() {
        return (this instanceof SMD);
    }
    
    public boolean isTMD() {
        return (this instanceof TMD);
    }
    
    public void setGraphSchedule(SpaceTimeScheduleAndSlicer graphSchedule) {
        this.graphSchedule = graphSchedule;
    }
    
    public abstract void run(int tiles);
    
    public SpaceTimeScheduleAndSlicer getGraphSchedule() {
        return graphSchedule;
    }
}
