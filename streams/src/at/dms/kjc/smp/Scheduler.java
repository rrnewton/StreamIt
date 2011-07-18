package at.dms.kjc.smp;

import java.util.HashMap;

import at.dms.kjc.backendSupport.Layout;
import at.dms.kjc.backendSupport.SpaceTimeScheduleAndSlicer;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.slir.InternalFilterNode;

/**
 * This class is the super class of all partitioners that act on the SIR graph to
 * data-parallelize the application.  Currently we support space-multiplexed and
 * time-multiplexed data parallel partitioners.  
 * 
 * @author mgordon
 *
 */
public abstract class Scheduler implements Layout<Core> {
    
    protected SpaceTimeScheduleAndSlicer graphSchedule;
    protected HashMap<InternalFilterNode, Core> layoutMap;
    
    public Scheduler() {
        graphSchedule = null;
        layoutMap = new HashMap<InternalFilterNode, Core>();
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

    public SIRStream SIRFusion(SIRStream str, int tiles) {return str;};
    
    public SpaceTimeScheduleAndSlicer getGraphSchedule() {
        return graphSchedule;
    }
}
