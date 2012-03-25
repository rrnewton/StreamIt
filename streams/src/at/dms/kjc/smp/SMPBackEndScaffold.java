/**
 * 
 */
package at.dms.kjc.smp;

import at.dms.kjc.backendSupport.BackEndFactory;
import at.dms.kjc.backendSupport.BackEndScaffold;
import at.dms.kjc.backendSupport.BasicSpaceTimeSchedule;
import at.dms.kjc.backendSupport.ComputeNodesI;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InputNode;
import at.dms.kjc.slir.SchedulingPhase;

/**
 * @author mgordon
 *
 */
public class SMPBackEndScaffold extends BackEndScaffold {
	
	public SMPBackEndScaffold() {

	}
	
    @Override
	protected void afterScheduling(BasicSpaceTimeSchedule schedule,
            BackEndFactory resources) {
    	    
    }
    
    @Override
	protected void beforeScheduling(BasicSpaceTimeSchedule schedule,
            BackEndFactory resources) {
        // nothing to do in default case.
   
    }
    
   
    @Override
	protected void betweenScheduling(BasicSpaceTimeSchedule schedule,
            BackEndFactory resources) {
        // nothing to do in default case.
    }
    
    /**
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
    @Override
	protected boolean doNotCreateJoiner(InputNode input) {
        return false;
    }
    
    /**
     * Iterate over the schedule of slices and over each node of each slice and 
     * generate the code necessary to fire the schedule.  Generate splitters and 
     * joiners intermixed with the trace execution...
     * 
     * @param filters The schedule to execute.
     * @param whichPhase True if the init stage.
     * @param computeNodes The collection of compute nodes.
     */
    @Override
    protected void iterateInorder(Filter filters[], SchedulingPhase whichPhase,
                                       ComputeNodesI computeNodes) {
        
        System.out.println("SMPBackEndScaffold iterateInorder...");
        for (Filter filter : filters) {
            System.out.println("    - filter=" + filter.getWorkNode());
        }        
                
        for (Filter filter : filters) {
            //create code for joining input to the trace
            backEndFactory.processFilterInputNode(filter.getInputNode(),
                    whichPhase, computeNodes);
            //create the compute code and the communication code for the
            //filters of the trace
            backEndFactory.processFilterWorkNode(filter.getWorkNode(), whichPhase, computeNodes);
            //create communication code for splitting the output
            backEndFactory.processFilterOutputNode(filter.getOutputNode(),
                    whichPhase, computeNodes);
        }
    }
 
    /**
     * Pass in a {@link BasicSpaceTimeScheduleX schedule}, and get a set of {@link at.dms.kjc.backendSupport.ComputeNode ComputeNode}s
     * and a set of (underspecified) {@link at.dms.kjc.backendSupport.IntraSSGChannel Buffer}s filled in.
     * @param schedule
     * @param computeNodes
     * @param resources The instance of BackEndFactory to be used for callbacks, data.
     */
    @Override
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
}
