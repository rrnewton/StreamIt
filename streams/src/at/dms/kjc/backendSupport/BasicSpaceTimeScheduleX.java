//$Id$
/**
 * Extracts the "schedule" part of Mike's SpaceTimeSchedule.
 * 
 */
package at.dms.kjc.backendSupport;

import java.util.*;

import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.slir.WorkNodeInfo;
import at.dms.kjc.common.CommonUtils;

/**
 * Collects initialization schedule, prime-pump schedule
 * and steady-state schedule in one place.
 * This is purely a data structure: it is operated on by 
 * other classes to generate these schedules.
 * @author mgordon (refactored dimock)
 */
public class BasicSpaceTimeScheduleX {
    //the initialization schedule
    private Filter[] initSchedule;
    //the preloop schedule!
    private Filter[][] primePumpSchedule; 
    //a list of the execution order of slices    
    private Filter[] schedule;
    //the multiplicities of the slices in the primepump
    //Filter->Integer
    private HashMap<Filter, Integer> primePumpMult;
    /** Partitioner stored with schedule. */
    private StaticSubGraph ssg;
    
    /**
     * Constructor
     */
    public BasicSpaceTimeScheduleX(StaticSubGraph ssg) {
        primePumpMult = new HashMap<Filter, Integer>();
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
    
    /**
     * @param is The initSchedule to set.
     */
    final public void setInitSchedule(LinkedList<Filter> is) {
        this.initSchedule = (Filter[])is.toArray(new Filter[0]);
    }

    /**
     * @return Returns the initSchedule.
     */
    final public Filter[] getInitSchedule() {
        return initSchedule;
    }

    /**
     * Return a linked list representation of the
     * steady state schedule;
     * 
     * @return a linked list representation of the
     * steady state schedule;
     */
    final public LinkedList<Filter> getScheduleList() {
        LinkedList<Filter> list = new LinkedList<Filter>();
        for (int i = 0; i < schedule.length; i++)
            list.add(schedule[i]);
        
        return list;
    }
    
    /**
     * @return Returns the steady state schedule.
     */
    final public Filter[] getSchedule() {
        return schedule;
    }

    /**
     * @param schedule The steady-state schedule to set.
     */
    final public void setSchedule(LinkedList<Filter> schedule) {
        this.schedule = schedule.toArray(new Filter[0]);
    }
    
    
    /**
     * @return Returns the primePumpSchedule.
     */
    final public Filter[][] getPrimePumpSchedule() {
        return primePumpSchedule;
    }

    /** 
     * @return A flat (one-dimensional) array of the primepump schedule.
     */
    final public Filter[] getPrimePumpScheduleFlat() {
        LinkedList<Filter> pp = new LinkedList<Filter>();
        
        for (int i = 0; i < primePumpSchedule.length; i++) 
            for (int j = 0; j < primePumpSchedule[i].length; j++) 
                pp.add(primePumpSchedule[i][j]);
        
        
        return pp.toArray(new Filter[0]);
    }
    
    /**
     * @param preLoopSchedule The primePumpSchedule to set.
     */
    final public void setPrimePumpSchedule(LinkedList<LinkedList<Filter>> preLoopSchedule) {
        //      convert into an array for easier access...
        //CommonUtils.println_debugging("Setting primepump schedule:");   
        primePumpSchedule = new Filter[preLoopSchedule.size()][];
        for (int i = 0; i < preLoopSchedule.size(); i++ ) {
            LinkedList schStep = preLoopSchedule.get(i);
            primePumpSchedule[i] = new Filter[schStep.size()];
            for (int j = 0; j < schStep.size(); j++) {
                Filter current = (Filter)schStep.get(j);
                //CommonUtils.println_debugging(current.toString());
                primePumpSchedule[i][j] = current;
                //generate the prime pump multiplicity map
                if (!primePumpMult.containsKey(current))
                    primePumpMult.put(current, new Integer(1));
                else 
                    primePumpMult.put(current, 
                            new Integer(primePumpMult.get(current).intValue() + 1));
            }
        }
    }
 
    /** 
     * @param slice
     * @return Return the number of times this slice fires in the prime pump schedule.
     */
    final public int getPrimePumpMult(Filter slice) {
        if (!primePumpMult.containsKey(slice))
            return 0;
        return primePumpMult.get(slice).intValue();
    }

    /**
     * @param f
     * @return The total number of times this filter fires in the prime pump stage
     * so this accounts for the number number of times that a slice if called in the
     * prime pump stage to fill the rotating buffers.
     */
    final public int getPrimePumpTotalMult(WorkNodeInfo f) {
        return getPrimePumpMult(f.sliceNode.getParent()) * f.steadyMult;
    }

}
