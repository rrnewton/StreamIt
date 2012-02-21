package at.dms.kjc.smp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.dms.kjc.KjcOptions;
import at.dms.kjc.backendSupport.BasicSpaceTimeSchedule;
import at.dms.kjc.backendSupport.Layout;
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
    
    protected BasicSpaceTimeSchedule graphSchedule;
    protected static HashMap<InternalFilterNode, Core> layoutMap = new HashMap<InternalFilterNode, Core>();
    
    public Scheduler() {
        graphSchedule = null;
        if (!KjcOptions.threadopt) {
            layoutMap = new HashMap<InternalFilterNode, Core>();
        }
    }

    public boolean isSMD() {
        return (this instanceof SMD);
    }
    
    public boolean isTMD() {
        return (this instanceof TMD);
    }
    
    public void setGraphSchedule(BasicSpaceTimeSchedule graphSchedule) {
        this.graphSchedule = graphSchedule;
    }
    
    public abstract void run(int tiles);

    public SIRStream SIRFusion(SIRStream str, int tiles) {return str;};
    
    public BasicSpaceTimeSchedule getGraphSchedule() {
        return graphSchedule;
    }
}
