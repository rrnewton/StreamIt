//$Id$
/**
 * Extracts the "schedule" part of Mike's SpaceTimeSchedule.
 * 
 */
package at.dms.kjc.backendSupport;

import java.util.HashMap;
import java.util.LinkedList;

import at.dms.kjc.slir.Filter;

/**
 * Collects initialization schedule, prime-pump schedule
 * and steady-state schedule in one place.
 * This is purely a data structure: it is operated on by 
 * other classes to generate these schedules.
 * @author mgordon (refactored dimock)
 */
public class BasicSpaceTimeSchedule {
    //the initialization schedule
    private Filter[] initSchedule;
    //the preloop schedule!
    private Filter[][] primePumpSchedule; 
    //a list of the execution order of slices    
    private Filter[] schedule;
    //the multiplicities of the slices in the primepump
    //Slice->Integer
    private HashMap<Filter, Integer> primePumpMult;
 
    /**
     * Constructor
     */
    public BasicSpaceTimeSchedule() {
        primePumpMult = new HashMap<Filter, Integer>();
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
    final public int getPrimePumpTotalMult(FilterInfo f) {
        return getPrimePumpMult(f.sliceNode.getParent()) * f.steadyMult;
    }

}
