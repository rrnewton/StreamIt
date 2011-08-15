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
import at.dms.kjc.slir.InputPort;
import at.dms.kjc.slir.Link;
import at.dms.kjc.slir.OutputNode;
import at.dms.kjc.slir.OutputPort;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.StaticSubGraph;

/**
 * @author mgordon
 *
 */
public class SMPBackEndScaffold extends BackEndScaffold {
    
    protected void beforeScheduling(BasicSpaceTimeSchedule schedule,
            BackEndFactory resources) {
        // nothing to do in default case.
    	System.out.println(this.getClass().getCanonicalName() + ".beforeScheduling()");    	
    	System.out.println(this.getClass().getCanonicalName() + " TODO: Add the dynamic links!");    	

    	// TODO:
    	// get the Filter, then get it's WorkNode
    	// WorkNode filter
    	// Channel inputChannel
    	// Set the following:
    	// peekName = inputChannel.peekMethodName();
        // popName = inputChannel.popMethodName();
        // popManyName = inputChannel.popManyMethodName();
    	
    	StaticSubGraph ssg = schedule.getSSG();
    	InputPort inputPort = ssg.getInputPort();
    	for (Link link : inputPort.getLinks()) {
    		OutputPort op = link.getOutputPort();
    		System.out.println(this.getClass().getCanonicalName() + " Creating a dynamic link between InputPort=" + inputPort + "->" + op);
    		System.out.println(this.getClass().getCanonicalName() + " Creating a pop!");
    		/* This will tell us if we should pop */
    	}
    	
    }
    
    protected void betweenScheduling(BasicSpaceTimeSchedule schedule,
            BackEndFactory resources) {
        // nothing to do in default case.
    }
    
   
    protected void afterScheduling(BasicSpaceTimeSchedule schedule,
            BackEndFactory resources) {
    	System.out.println(this.getClass().getCanonicalName() + ".afterScheduling()");
        // nothing to do.
    	// TODO:
    	// get the Filter, then get it's WorkNode
    	// WorkNode filter
    	// Channel inputChannel
    	// Set the following:    	
        // pushName = outputChannel.pushMethodName();
    	
    	StaticSubGraph ssg = schedule.getSSG();
    	OutputPort outputPort = ssg.getOutputPort();
    	for (Link link : outputPort.getLinks()) {
    		InputPort ip = link.getInputPort();
    		System.out.println(this.getClass().getCanonicalName() + "Creating a dynamic link between InputPort=" + outputPort + "->" + ip);
    		System.out.println(this.getClass().getCanonicalName() + "Creating a push!");

    		//ssg.getTopFilters()[0].get
    		
    	}

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
    protected boolean doNotCreateJoiner(InputNode input) {
        return false;
    }
    
    /**
     * Pass in a {@link BasicSpaceTimeScheduleX schedule}, and get a set of {@link at.dms.kjc.backendSupport.ComputeNode ComputeNode}s
<<<<<<< .mine
     * and a set of (under-specified) {@link at.dms.kjc.backendSupport.Channel Buffer}s filled in.
=======
     * and a set of (underspecified) {@link at.dms.kjc.backendSupport.IntraSSGChannel Buffer}s filled in.
>>>>>>> .r11313
     * @param schedule
     * @param computeNodes
     * @param resources The instance of BackEndFactory to be used for callbacks, data.
     */
    @Override
    public void run(BasicSpaceTimeSchedule schedule, BackEndFactory resources) {
   
        ComputeNodesI computeNodes = resources.getComputeNodes();
        this.resources = resources;
        
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
    @Override
    protected void iterateInorder(Filter slices[], SchedulingPhase whichPhase,
                                       ComputeNodesI computeNodes) {
        Filter slice;

        for (int i = 0; i < slices.length; i++) {
            slice = (Filter) slices[i];
            //create code for joining input to the trace
            resources.processInputSliceNode((InputNode)slice.getInputNode(),
                    whichPhase, computeNodes);
            //create the compute code and the communication code for the
            //filters of the trace
            resources.processFilterSliceNode(slice.getWorkNode(), whichPhase, computeNodes);
            //create communication code for splitting the output
            resources.processOutputSliceNode((OutputNode)slice.getOutputNode(),
                    whichPhase, computeNodes);
        }
    }  
}
