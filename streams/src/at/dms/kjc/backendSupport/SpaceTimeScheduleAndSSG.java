//$Id: SpaceTimeScheduleAndSlicer.java,v 1.2 2008-09-04 18:53:25 mgordon Exp $
/**
 * Extracts the "schedule" part of Mike's SpaceTimeSchedule.
 * 
 */
package at.dms.kjc.backendSupport;

import java.util.Vector;

import at.dms.kjc.slir.*;

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
public class SpaceTimeScheduleAndSSG extends BasicSpaceTimeSchedule {
    /** Partitioner stored with schedule. */
    private StaticSubGraph ssg;
    
    public SpaceTimeScheduleAndSSG(StaticSubGraph ssg) {
        super();
        this.ssg = ssg;
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
        for (int i = 0; i < getSSG().io.length; i++) 
            if (getSSG().io[i].getInputNode().isFileOutput())
                fileWriters.add(getSSG().io[i]);
        
        for (int i = 0; i < fileWriters.size(); i++) {
            WorkNode node = (WorkNode)fileWriters.get(i).getInputNode().getNext();
            WorkNodeInfo fi = WorkNodeInfo.getFilterInfo(node);
            assert node.getFilter().getInputType().isNumeric() :
                "non-numeric type for input to filewriter";
        
            outputs += fi.totalItemsReceived(SchedulingPhase.STEADY);
        }
        return outputs;
    }
    

    
    /** 
     * @param ssg
     */
    public void setSSG(StaticSubGraph ssg) {
        this.ssg = ssg;
    }

    /**
     * @return the partitioner associated with this schedule.
     */
    public StaticSubGraph getSSG() {
        return ssg;
    }
}
