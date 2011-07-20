package at.dms.kjc.slir;

import at.dms.kjc.backendSupport.FilterInfo;

/**
 * Go through all the slices and create and install the schedule of distribution for the
 * init schedule for input and output slice nodes by setting to empty if nothing done or
 * setting to init if something done. 
 *  
 * @author mgordon
 *
 */
public class InstallInitDistributions {

    /**
     * Go through all the slices and create and install the schedule of distribution for the
     * init schedule for input and output slice nodes by setting to empty if nothing done or
     * setting to init if something done. 
     */  
    public static void doit(Filter[] slices) {
        for (Filter slice : slices) {
            FilterInfo fi = FilterInfo.getFilterInfo(slice.getWorkNode());
            //do input slice node
            if (fi.totalItemsReceived(SchedulingPhase.INIT) > 0) {
                slice.getInputNode().setInitWeights(slice.getInputNode().getWeights(SchedulingPhase.STEADY).clone());
                slice.getInputNode().setInitSources(slice.getInputNode().getSources(SchedulingPhase.STEADY).clone());
            } else {
                //nothing done by input
                slice.getInputNode().setInitWeights(new int[0]);
                slice.getInputNode().setInitSources(new InterFilterChannel[0]);
            }
            
            //do output slice
            if (fi.totalItemsSent(SchedulingPhase.INIT) > 0) {
                slice.getOutputNode().setInitDests(slice.getOutputNode().getDests(SchedulingPhase.STEADY).clone());
                slice.getOutputNode().setInitWeights(slice.getOutputNode().getWeights(SchedulingPhase.STEADY).clone());
            } else {
                //nothing done by output
                slice.getOutputNode().setInitDests(new InterFilterChannel[0][0]);
                slice.getOutputNode().setInitWeights(new int[0]);
            }
        }
    }
}
