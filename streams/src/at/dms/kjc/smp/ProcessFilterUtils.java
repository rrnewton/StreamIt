package at.dms.kjc.smp;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InputPort;
import at.dms.kjc.slir.InterFilterEdge;
import at.dms.kjc.slir.InterSSGEdge;
import at.dms.kjc.slir.OutputPort;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.WorkNode;

/**
 * This class contains a number of utility functions for getting information
 * abotu the threads and cores of filters.
 * 
 * @author soule
 * 
 */

public class ProcessFilterUtils {

    /**
     * Get the core that a filter is on
     * @param workNode1 the current worknode
     * @param filter teh parent of the worknode
     * @return
     */
    public static Core getCore(WorkNode workNode1, Filter filter) {
        if (filter == null) {           
            return SMPBackend.getComputeNode(workNode1);
        }
        return SMPBackend.getComputeNode(filter.getWorkNode());
    }
    
    /**
     * Get the thread that a filter is on
     * 
     * @param w
     *            worknode
     * @param filter
     *            filter that contains the worknode
     * @return thread id number
     */
    static int getFilterThread(Filter filter) {


//
//        
//        if (filter == null) {
//            return getFilterThread(
//                    w,
//                    w.getParent());
//        }
//

        
        assert filter != null : "Null filter passed to getFilterThread!";
        
       

        return ThreadMapper.getMapper().getFilterToThreadId().get(filter);
        
//        if (KjcOptions.threadopt) {
//
////            // A FileInput is always on the core of its next filter
////            if (workNode.isFileInput()) {
////                Filter next = getNextFilter(workNode);
////                return ThreadMapper.getMapper().getFilterToThreadId().get(next);                               
////            }
////
////            // A FileOutput is always on the core of its next filter
////            if (workNode.isFileOutput()) {
////                Filter prev = getPreviousFilter(workNode);
////                return ThreadMapper.getMapper().getFilterToThreadId().get(prev);                                             
////            }
//
//        
//        } else {
//
//            // A FileInput is always on the core of its next filter
//            if (workNode.isFileInput()) {
//                Filter next = getNextFilter(workNode);
//                Core nextCore = SMPBackend.getComputeNode(next.getWorkNode());
//                return ThreadMapper.coreToThread(nextCore.coreID);
//            }
//
//            // A FileOutput is always on the core of its next filter
//            if (workNode.isFileOutput()) {
//                Filter prev = getPreviousFilter(workNode);
//                Core prevCore = SMPBackend.getComputeNode(prev.getWorkNode());
//                return ThreadMapper.coreToThread(prevCore.coreID);
//            }
//        }

       
      
    }

    /**
     * Get the next filter in the data flow graph
     * 
     * @param filter
     *            the current filters
     * @return the next filter
     */
    public static Filter getNextFilter(WorkNode filter) {
        StaticSubGraph ssg = filter.getParent().getStaticSubGraph();
        if (filter == ssg.getFilterGraph()[ssg.getFilterGraph().length - 1]
                .getWorkNode()) {
            OutputPort outputPort = ssg.getOutputPort();
            if (outputPort == null) {
                return null;
            }
            for (InterSSGEdge edge : outputPort.getLinks()) {
                Filter dstTop = edge.getDest().getSSG().getTopFilters()[0];
                return dstTop;
            }

        } else {
            InterFilterEdge[] dstEdges = filter.getParent().getOutputNode()
                    .getDestList(
                            SchedulingPhase.STEADY);
            Set<InterFilterEdge> edgeSet = new HashSet<InterFilterEdge>();
            for (InterFilterEdge e : dstEdges) {
                edgeSet.add(e);
            }
            for (InterFilterEdge e : edgeSet) {
                return e.getDest().getParent();
            }
            return null;
        }
        return null;
    }
    
          

    /**
     * Get the next filter in the dataflow graph that is on the same core
     * 
     * @param workNode
     *            the current workode
     * @return the next filter on the same core
     */
    static Filter getNextFilterOnCore(WorkNode workNode) {
        
        if (workNode.isFileOutput()) {
            return null;
        }
        Core core = SMPBackend.getComputeNode(workNode);
        Filter next = workNode.getParent();
        Core nextCore;
        while (true) {
            next = getNextFilter(next.getWorkNode());

            if (next == null) {
                return null;
            } else {                
                nextCore = SMPBackend.getComputeNode(next.getWorkNode());                                

                if (nextCore.coreID == core.coreID) {
                    return next;
                }
                if (next.getWorkNode().isFileOutput()) {                                               
                    if (nextCore.coreID != core.coreID) {
                        return null;
                    }
                }

            }
        }
    }
    

    static Filter getNextFiltersOnCore(WorkNode workNode) {

    	if (workNode.isFileOutput()) {
    		return null;
    	}

    	Core core = SMPBackend.getComputeNode(workNode);
    	return checkList(workNode, core);

    }

 private static Filter checkList(WorkNode workNode, Core core) {
	 List<Filter> nextFilters = getNextFilters(workNode);
	 if (nextFilters.isEmpty()) {
		 return null;
	 }                    	  
	 Core nextCore;
	 for (Filter f : nextFilters) {
		 nextCore = SMPBackend.getComputeNode(f.getWorkNode());  
		 if (nextCore.coreID == core.coreID) {
			 return f;              	
		 }
	 }
	 for (Filter f : nextFilters) {
		 Filter next = checkList(f.getWorkNode(), core);
		 if (next != null) {
			 return next;
		 }
	 }
	 return null;  
 }
    

    /**
     * Get the next filter in the dataflow graph that is on a different core
     * 
     * @param workNode
     *            the current workode
     * @return the next filter on on a different core
     */
    static Filter getNextFilterOnCoreDifferentThread(WorkNode workNode) {

        int filterThread = getFilterThread(workNode.getParent());

        Filter next = getNextFilterOnCore(workNode);
        while (next != null) {
            int nextThread = getFilterThread(next);
            if (filterThread != nextThread) {
                return next;
            }
            next = getNextFilterOnCore(next.getWorkNode());
        }

        return null;
    }

    /**
     * If there is more than one next filter, such as occurs after a divergent
     * pattern of fission, this will return all the next filters
     * 
     * @param filter
     *            the current filter
     * @return a list of all the next fitlers
     */
    static List<Filter> getNextFilters(WorkNode filter) {
        List<Filter> nextFilters = new LinkedList<Filter>();
        StaticSubGraph ssg = filter.getParent().getStaticSubGraph();
        if (filter == ssg.getFilterGraph()[ssg.getFilterGraph().length - 1]
                .getWorkNode()) {
            OutputPort outputPort = ssg.getOutputPort();
            if (outputPort == null) {
                return nextFilters;
            }
            for (InterSSGEdge edge : outputPort.getLinks()) {
                Filter dstTop = edge.getDest().getSSG().getTopFilters()[0];
                nextFilters.add(dstTop);
            }

        } else {
            InterFilterEdge[] dstEdges = filter.getParent().getOutputNode()
                    .getDestList(
                            SchedulingPhase.STEADY);
            Set<InterFilterEdge> edgeSet = new HashSet<InterFilterEdge>();
            for (InterFilterEdge e : dstEdges) {
                edgeSet.add(e);
            }
            for (InterFilterEdge e : edgeSet) {
                nextFilters.add(e.getDest().getParent());
            }
        }
        return nextFilters;
    }

    /**
     * Return the previous filter in the dataflow graph
     * 
     * @param filter
     *            the current filter
     * @return the previous filter
     */
    public static Filter getPreviousFilter(WorkNode filter) {
        StaticSubGraph ssg = filter.getParent().getStaticSubGraph();
        if (filter == ssg.getFilterGraph()[0].getWorkNode()) {
            InputPort inputPort = ssg.getInputPort();
            if (inputPort == null) {
                return null;
            }
            for (InterSSGEdge edge : inputPort.getLinks()) {
                Filter src = edge.getSrc().getSSG().getLastFilter();
                return src;
            }
        } else {
            InterFilterEdge[] srcEdges = filter.getParent().getInputNode()
                    .getSources(
                            SchedulingPhase.STEADY);
//            Set<InterFilterEdge> edgeSet = new HashSet<InterFilterEdge>();
//            for (InterFilterEdge e : srcEdges) {
//                edgeSet.add(e);
//            }
            //for (InterFilterEdge e : edgeSet) {
            	
            for (InterFilterEdge e : srcEdges) {
            	
            	
                return e.getSrc().getParent();
            }
            return null;
        }
        return null;
    }

    /**
     * Return the previous filter in the dataflow graph on the same core
     * 
     * @param workNode
     *            the current filter
     * @return the previous filter on the same core
     */
    static Filter getPreviousFilterOnCore(WorkNode workNode) {
        if (workNode.isFileInput()) {
            return null;
        }
        Core core = SMPBackend.getComputeNode(workNode);
        Filter prev;
        Core prevCore;
        while (true) {
            prev = getPreviousFilter(workNode);
            if (prev == null) {
                return null;
            } else {
                prevCore = SMPBackend.getComputeNode(prev.getWorkNode());
                if (prevCore.coreID == core.coreID
                        || prev.getWorkNode().isFileInput()) {
                    return prev;
                }
            }
            workNode = prev.getWorkNode();
        }
    }

}
