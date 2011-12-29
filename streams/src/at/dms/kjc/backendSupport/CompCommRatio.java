package at.dms.kjc.backendSupport;

import java.util.Iterator;
import java.util.Map;

import at.dms.kjc.sir.SIRFeedbackLoop;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIROperator;
import at.dms.kjc.sir.SIRPipeline;
import at.dms.kjc.sir.SIRSplitJoin;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.lowering.partition.WorkEstimate;
import at.dms.kjc.slir.DataFlowOrder;
import at.dms.kjc.slir.InputNode;
import at.dms.kjc.slir.InternalFilterNode;
import at.dms.kjc.slir.OutputNode;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.Util;
import at.dms.kjc.slir.WorkNode;

/**
 * Calculate the computation to communication ratio.  Poorly named class,
 * the name should be CompCommRatio. 
 * 
 * @author mgordon
 *
 */
public class CompCommRatio {
    
   
    private static double comp = 0;
    private static double comm = 0;
    private static WorkEstimate work;
    private static Map<SIROperator, int[]> mults;
    
    public static double ratio(SIRStream str, WorkEstimate work,
            Map<SIROperator, int[]> executionCounts) {
        
        comp = 0;
        comm = 0;
        CompCommRatio.work = work;
        mults = executionCounts;
        walkSTR(str);
        
        return comp/comm; 
    }

    // The following structure appears all over the place.  It needs to be abstracted somehow.
    // Walk SIR structure to get down to 
    private static void walkSTR(SIRStream str) {
        if (str instanceof SIRFeedbackLoop) {
            SIRFeedbackLoop fl = (SIRFeedbackLoop) str;
            walkSTR(fl.getBody());
            walkSTR(fl.getLoop());
        }
        if (str instanceof SIRPipeline) {
            SIRPipeline pl = (SIRPipeline) str;
            Iterator iter = pl.getChildren().iterator();
            while (iter.hasNext()) {
                SIRStream child = (SIRStream) iter.next();
                walkSTR(child);
            }
        }
        if (str instanceof SIRSplitJoin) {
            SIRSplitJoin sj = (SIRSplitJoin) str;
            Iterator<SIRStream> iter = sj.getParallelStreams().iterator();
            if (mults.containsKey(sj)) {
                comm += sj.getSplitter().getSumOfWeights() * mults.get(sj)[0];
                comm += sj.getJoiner().getSumOfWeights() * mults.get(sj)[0];
            }
            while (iter.hasNext()) {
                SIRStream child = iter.next();
                walkSTR(child);
            }
        }
        // update the comm and comp numbers...
        if (str instanceof SIRFilter) {
           comp += work.getWork((SIRFilter)str);
           comm += ((SIRFilter)str).getPushInt();
        }
    }

    /**
     * Calculate the computation to communication ratio of the 
     * application.  Where the computation is total work of all the filters
     * in the steady-state and the communication is the 
     * number of items sent between slices.
     * 
     * @param SSG The partitioner we used to slice the graph.
     * 
     * @return The computation to communication ratio.
     */
    public static double ratio(StaticSubGraph SSG) {
        int comp = 0, comm = 0;
        // get the slice node travesal
        Iterator<InternalFilterNode> sliceNodeIt = Util.sliceNodeTraversal(DataFlowOrder
                                                       .getTraversal(SSG.getTopFilters()));

        while (sliceNodeIt.hasNext()) {
            InternalFilterNode sliceNode = sliceNodeIt.next();

            if (sliceNode.isWorkNode()) {
                WorkNode filter = (WorkNode) sliceNode;
                // comm += (filter.getFilter().getSteadyMult() *
                // filter.getFilter().getPushInt());
                comp += (filter.getWorkNodeContent().getSteadyMult() * SSG
                         .getFilterWork(filter));
            } else if (sliceNode.isOutputSlice()) {
                OutputNode output = (OutputNode) sliceNode;
                WorkNode filter = (WorkNode) output.getPrevious();
                // FilterInfo filterInfo = FilterInfo.getFilterInfo(filter);
                // calculate the number of items sent

                int itemsReceived = filter.getWorkNodeContent().getPushInt()
                    * filter.getWorkNodeContent().getSteadyMult();
                int iterations = (output.totalWeights(SchedulingPhase.STEADY) != 0 ? itemsReceived
                                  / output.totalWeights(SchedulingPhase.STEADY) : 0);

                int itemsSent = 0;

                for (int j = 0; j < output.getWeights(SchedulingPhase.STEADY).length; j++) {
                    for (int k = 0; k < output.getWeights(SchedulingPhase.STEADY)[j]; k++) {
                        // generate the array of compute node dests
                        itemsSent += output.getDests(SchedulingPhase.STEADY)[j].length;
                    }
                }

                comm += (iterations * itemsSent);
            } else {
                InputNode input = (InputNode) sliceNode;
                WorkNode filter = (WorkNode) input.getNext();

                // calculate the number of items received
                int itemsSent = filter.getWorkNodeContent().getSteadyMult()
                    * filter.getWorkNodeContent().getPopInt();

                int iterations = (input.totalWeights(SchedulingPhase.STEADY) != 0 ? itemsSent
                                  / input.totalWeights(SchedulingPhase.STEADY) : 0);
                int itemsReceived = 0;

                for (int j = 0; j < input.getWeights(SchedulingPhase.STEADY).length; j++) {
                    // get the source buffer, pass thru redundant buffer(s)
                    itemsReceived += input.getWeights(SchedulingPhase.STEADY)[j];
                }

                comm += (iterations * itemsReceived);
            }

        }

        if (comm == 0)
            return 0.0;

        System.out.println("Computation / Communication Ratio: "
                           + ((double) comp) / ((double) comm));
        return ((double) comp) / ((double) comm);
    }
}
