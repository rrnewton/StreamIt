//$Id: SpaceTimeScheduleAndSlicer.java,v 1.2 2008-09-04 18:53:25 mgordon Exp $
/**
 * Extracts the "schedule" part of Mike's SpaceTimeSchedule.
 * 
 */
package at.dms.kjc.backendSupport;

import java.util.Vector;

import at.dms.kjc.slicegraph.Slicer;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.WorkNode;

/**
 * Extend BasicSpaceTimeSchedule by storing a slicer.
 * 
 * BasicSpaceTimeSchedule collects initialization schedule, prime-pump schedule
 * and steady-state schedule in one place.
 * This is purely a data structure: it is operated on by 
 * other classes to generate these schedules.
 * 
 * It is convenient for classes performing layout to keep the slicer with
 * the schedule.  The slicer supplies the initial slice graph and includes
 * a map from filters to the amount of work that they perform, which is needed
 * to partition.
 * 
 * @author mgordon (refactored dimock)
 */
public class SpaceTimeScheduleAndSlicer extends BasicSpaceTimeSchedule {
    /** Partitioner stored with schedule. */
    private Slicer slicer;
    
    public SpaceTimeScheduleAndSlicer(Slicer slicer) {
        super();
        this.slicer = slicer;
    }
    
    
    /**
     * Return the number of outputs that are written to file writers during the 
     * steady-state.
     *  
     * @return the number of outputs that are written to file writers during the 
     * steady-state.
     */
    public int outputsPerSteady() {
        int outputs = 0;
        
        //get all the file writers
        Vector<Filter> fileWriters = new Vector<Filter>();
        for (int i = 0; i < getSlicer().io.length; i++) 
            if (getSlicer().io[i].getInputNode().isFileOutput())
                fileWriters.add(getSlicer().io[i]);
        
        for (int i = 0; i < fileWriters.size(); i++) {
            WorkNode node = (WorkNode)fileWriters.get(i).getInputNode().getNext();
            FilterInfo fi = FilterInfo.getFilterInfo(node);
            assert node.getFilter().getInputType().isNumeric() :
                "non-numeric type for input to filewriter";
        
            outputs += fi.totalItemsReceived(SchedulingPhase.STEADY);
        }
        return outputs;
    }
    

    
    /** 
     * @param slicer
     */
    public void setSlicer(Slicer slicer) {
        this.slicer = slicer;
    }

    /**
     * @return the partitioner associated with this schedule.
     */
    public Slicer getSlicer() {
        return slicer;
    }
}
