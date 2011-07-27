package at.dms.kjc.spacetime;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import at.dms.kjc.slir.Edge;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.Filter;
import at.dms.util.Utils;

/** This class generates the init schedule, the execution order as 
    given by the flow of the graph 
**/

public class InitSchedule 
{
    public static LinkedList<Filter> getInitSchedule(Filter[] topTraces) 
    {
        LinkedList<Filter> schedule = new LinkedList<Filter>();
        HashSet<Filter> visited = new HashSet<Filter>();
        LinkedList<Filter> queue = new LinkedList<Filter>();
        for (int i = 0; i < topTraces.length; i++) {
            queue.add(topTraces[i]);
            while (!queue.isEmpty()) {      
                Filter slice = queue.removeFirst();
                if (!visited.contains(slice)) {
                    visited.add(slice);
                    Iterator dests = slice.getTail().getDestSet(SchedulingPhase.INIT).iterator();
                    while (dests.hasNext()) {
                        Filter current = ((Edge)dests.next()).getDest().getParent();
                        if (!visited.contains(current)) {
                            //only add if all sources has been visited
                            Iterator sources = current.getHead().getSourceSet(SchedulingPhase.INIT).iterator();
                            boolean addMe = true;
                            while (sources.hasNext()) {
                                if (!visited.contains(((Edge)sources.next()).getSrc().getParent())) {
                                    addMe = false;
                                    break;
                                }   
                            }
                            if (addMe)
                                queue.add(current);
                        }    
                    }
                    if (!slice.getHead().getNextFilter().isPredefined()) {
                        System.out.println("Adding " + slice + " to init schedule.");           
                        schedule.add(slice);
                    }
                }
            }
        }
    
        return schedule;
    }
}