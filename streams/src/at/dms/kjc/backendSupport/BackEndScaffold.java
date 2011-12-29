// $Id: BackEndScaffold.java,v 1.11 2008-09-06 18:45:32 mgordon Exp $
package at.dms.kjc.backendSupport;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InputNode;
import at.dms.kjc.slir.InterFilterEdge;
import at.dms.kjc.slir.OutputNode;
import at.dms.kjc.slir.SchedulingPhase;


/**
 * Create code for a partitioning of {@link at.dms.kjc.slir.Filter Slice}s 
 * on a collection of {@link at.dms.kjc.backendSupport.ComputeNode ComputeNode}s.
 * Connections between the ComputeNode s are returned as 
 * {@link at.dms.kjc.backendSupport.IntraSSGChannel Buffer}s.
 * @author dimock
  */
public class BackEndScaffold  {
    
    /** used to pass back-end factory around */
    protected  BackEndFactory backEndFactory;

    /**
     * Use in subclasses to perform work before code is created.
     * Only needed if subclassing and need to share data generated in beforeScheduling code.
     * for any schedule.
     * @param schedule
     * @param resources the BackEndFactory used to redirect to correct code generation routines.
     */
    protected void beforeScheduling(BasicSpaceTimeSchedule schedule,
            BackEndFactory resources) {
    	System.out.println("===========> " + this.getClass().getCanonicalName() + ".beforeScheduling()");
        // nothing to do in default case.
    }
    
    protected void betweenScheduling(BasicSpaceTimeSchedule schedule,
            BackEndFactory resources) {
        // nothing to do in default case.
    }
    
    /**
     * Use in subclasses to perform work after code is created for all schedules.
       Only needed if subclassing and need to share data generated beforeScheduling or schedule code.
    * @param schedule
     * @param resources
     */
    protected void afterScheduling(BasicSpaceTimeSchedule schedule,
            BackEndFactory resources) {
        // nothing to do.
    }
    
    /**
     * Use in subclass to indicate that no code needs to be created for
     * this joiner.
     * Called in case of no software pipelining to determine if no
     * code should be created for this joiner, but it is allowable
     * to create code for the following filter(s) in the slice.
     * Not called if software pipelining.
     * <br/>
     * Historic leftover from RAW specetime schedule, which ignored
     * file inputs (which came from off-chip).
     * @param input InputSliceNode to consider for to a joiner.
     * @return
     */
    protected boolean doNotCreateJoiner(InputNode input) {
        return false;
    }
    
    /**
     * Pass in a {@link BasicSpaceTimeScheduleX schedule}, and get a set of {@link at.dms.kjc.backendSupport.ComputeNode ComputeNode}s
     * and a set of (underspecified) {@link at.dms.kjc.backendSupport.IntraSSGChannel Buffer}s filled in.
     * @param schedule
     * @param computeNodes
     * @param resources The instance of BackEndFactory to be used for callbacks, data.
     */
    public void run(BasicSpaceTimeSchedule schedule, BackEndFactory resources) {
   
        ComputeNodesI computeNodes = resources.getComputeNodes();
        this.backEndFactory = resources;
        
        Filter slices[];

        beforeScheduling(schedule,resources);
        
        // schedule the initialization phase.
        slices = schedule.getInitSchedule();
        iterateInorder(slices, SchedulingPhase.INIT, computeNodes);
        // schedule the prime pump phase.
        // (schedule should be empty if not spacetime)
        slices = schedule.getPrimePumpScheduleFlat();
        iterateInorder(slices, SchedulingPhase.PRIMEPUMP, computeNodes);
        // schedule the steady-state phase.
        slices = schedule.getSchedule();

        betweenScheduling(schedule, resources);
        
       
        iterateInorder(slices, SchedulingPhase.STEADY, computeNodes);
        
        afterScheduling(schedule, resources);
    }
 
    /**
     * Iterate over the schedule of slices and over each node of each slice and 
     * generate the code necessary to fire the schedule.  Generate splitters and 
     * joiners intermixed with the trace execution...
     * 
     * @param slices The schedule to execute.
     * @param whichPhase True if the init stage.
     * @param computeNodes The collection of compute nodes.
     */
    protected void iterateInorder(Filter slices[], SchedulingPhase whichPhase,
                                       ComputeNodesI computeNodes) {
        Filter slice;

        for (int i = 0; i < slices.length; i++) {
            slice = (Filter) slices[i];
            //create code for joining input to the trace
            backEndFactory.processFilterInputNode((InputNode)slice.getInputNode(),
                    whichPhase, computeNodes);
            //create the compute code and the communication code for the
            //filters of the trace
            backEndFactory.processFilterWorkNode(slice.getWorkNode(), whichPhase, computeNodes);
            //create communication code for splitting the output
            backEndFactory.processFilterOutputNode((OutputNode)slice.getOutputNode(),
                    whichPhase, computeNodes);
            
        }
    }
    
    /**
     * Iterate over the schedule of slices and over each node of each slice and 
     * generate the code necessary to fire the schedule.  Generate splitters and 
     * joiners first so that the data will be redistributed before the filters
     * execute.
     * 
     * @param slices The schedule to execute.
     * @param whichPhase True if the init stage.
     * @param rawChip The raw chip
     */
    private void iterateJoinFiltersSplit(Filter slices[], SchedulingPhase whichPhase,
                                                ComputeNodesI computeNodes) {
        Filter slice;

        for (int i = 0; i < slices.length; i++) {
            slice = (Filter) slices[i];
            //create code for joining input to the trace
            backEndFactory.processFilterInputNode((InputNode)slice.getInputNode(),
                    whichPhase, computeNodes);
        }
        for (int i = 0; i < slices.length; i++) {
            slice = (Filter) slices[i];
            //create the compute code and the communication code for the
            //filters of the trace
            if (slice instanceof Filter) {
                backEndFactory.processFilterWorkNode(((Filter)slice).getWorkNode(), whichPhase, computeNodes);                
            } else {
                backEndFactory.processFilterSlices(slice, whichPhase, computeNodes);
            }
        }
        for (int i = 0; i < slices.length; i++) {
            slice = (Filter) slices[i];
            //create communication code for splitting the output
            backEndFactory.processFilterOutputNode((OutputNode)slice.getOutputNode(),
                    whichPhase, computeNodes);
        }
    }
    
    /** Special scheduling for --spacetime --noswpipe */
    private void iterateNoSWPipe(List<Filter> scheduleList, SchedulingPhase whichPhase,
            ComputeNodesI computeNodes) {
        HashSet<OutputNode> hasBeenSplit = new HashSet<OutputNode>();
        HashSet<InputNode> hasBeenJoined = new HashSet<InputNode>();
        LinkedList<Filter> scheduled = new LinkedList<Filter>();
        LinkedList<Filter> needToSchedule = new LinkedList<Filter>();
        needToSchedule.addAll(scheduleList);
        
        
        while (needToSchedule.size() != 0) {
            //join everyone that can be joined
            for (int n = 0; n < needToSchedule.size(); n++) {
                Filter notSched = needToSchedule.get(n);

                // a joiner with 0 inputs does not create code.
                // presumably followed by a filter with 0 inputs that
                // may create code.
                if (notSched.getInputNode().noInputs()) {
                    hasBeenJoined.add(notSched.getInputNode());
                    continue;
                }

                // If a subclass of this says that there is no joiner code
                // then do not create this joiner.
                if (doNotCreateJoiner(notSched.getInputNode())) {
                    hasBeenJoined.add(notSched.getInputNode());
                    continue;
                }


                // joiner can not be created until upstream slpitters
                // feeding the joiner have all been created.
                // XXX WTF: Precludes feedback loops.
                boolean canJoin = true;
                for (InterFilterEdge inEdge : notSched.getInputNode().getSourceSet(whichPhase)) {
                    if (!hasBeenSplit.contains(inEdge.getSrc())) {
                        canJoin = false;
                        break;
                    }
                }
                if (! canJoin) { continue; }
                
                // create code for joining input to the trace
                hasBeenJoined.add(notSched.getInputNode());
                // System.out.println("Scheduling join of " + notSched.getHead().getNextFilter());
                backEndFactory.processFilterInputNode(notSched.getInputNode(), whichPhase,
                        computeNodes);

            } // end of for loop //join everyone that can be joined

            //create the compute code and the communication code for the
            //filters of the trace after joiner has been processed.
            while (needToSchedule.size() != 0) {
                Filter slice = needToSchedule.get(0);
                if (hasBeenJoined.contains(slice.getInputNode())) {
                    scheduled.add(slice);
                    if (slice instanceof Filter) {
                        backEndFactory.processFilterWorkNode(((Filter)slice).getWorkNode(), whichPhase, computeNodes);
                    }
                    //System.out.println("Scheduling " + trace.getHead().getNextFilter());
                    needToSchedule.removeFirst();
                }
                else {
                    break;
                }
            }

        }
        
        //schedule any splits that have not occured
        // but whose preceeding filters in a slice have been scheduled.
        if (hasBeenSplit.size() != scheduled.size()) {
            for (int t = 0; t < scheduled.size(); t++) {
                if (!hasBeenSplit.contains(scheduled.get(t).getOutputNode())) {
                    OutputNode output = 
                        scheduled.get(t).getOutputNode();
                    //System.out.println("Scheduling split of " + output.getPrevFilter()); 
                    backEndFactory.processFilterOutputNode(output,
                            whichPhase, computeNodes);
                    hasBeenSplit.add(output);
                }
            }
        }
        
        // schedule any joins that have not occured 
        // but whose following filters in the slice have been scheduled
        if (hasBeenJoined.size() != scheduled.size()) {
            for (int t = 0; t < scheduled.size(); t++) {
                if (!hasBeenJoined.contains(scheduled.get(t).getInputNode())) {
                    InputNode input  = 
                        scheduled.get(t).getInputNode();
                    //System.out.println("Scheduling join of " + input.getNextFilter()); 
                    backEndFactory.processFilterInputNode(input,
                            whichPhase, computeNodes);
                    hasBeenJoined.add(input);
                }
            }
        }

    }
    
}
