package at.dms.kjc.smp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import at.dms.kjc.CStdType;
import at.dms.kjc.CType;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.StaticSubGraph;

/**
 * A singleton class for mapping filters to threads.
 * @author soule
 *
 */
public class ThreadMapper {
   
    private static ThreadMapper mapper      = null;
    private static int          threadId;
    static {
        if (KjcOptions.threadopt) {
            threadId = KjcOptions.smp - 1;
            //threadId = 0;
        } else {
            threadId = 0;
        }
    }
    
    public static int getNumThreads() {
        return threadId + 1;
    }

    public static int coreToThread(int core) {
        if (core == -1) {            
            return -1;
        }        
        int index = 0;
        for (int c : SMPBackend.coreOrder) {
            if (c == core) return index;
            index++;
        }
        assert (false) : "Core " + core +" not mapped to a thread!";
        return -1;
    }
    
    public static int getFirstCore() {
        return SMPBackend.coreOrder[0];
    }

    /**
     * Provides access to the singleton class
     * 
     * @return the single thread mapper
     */
    public static ThreadMapper getMapper() {
        if (mapper == null) {
            mapper = new ThreadMapper();
        }
        return mapper;
    }
    
    
    /** a mapping from filter to thread id */
    private Map<Filter, Integer>      filterToThreadId;

    /** a mapping of dominator filter to dominated filter */
    private Map<String, List<String>> dominators;

    /** a mapping of thread to its input type */
    private Map<Integer, String>      threadIdToType;
    
    private Map<Integer, Integer>     coreToThread;

    /**
     * Private constructor for singleton
     */
    private ThreadMapper() {
        filterToThreadId = new HashMap<Filter, Integer>();     
        dominators = new HashMap<String, List<String>>();
        threadIdToType = new HashMap<Integer, String>();
        coreToThread = new HashMap<Integer, Integer>();
    }

    /**
     * Assign a unique id to each thread for dynamic readers
     * 
     * @param ssg
     *            The static subgraph that contains the dynamic reader
     */
    public void assignThreads(StaticSubGraph ssg) {
        if (KjcOptions.threadopt) {
            assignThreadsOpt(ssg);
        } else {
            assignThreadsNonOpt(ssg);
        }

    }

    public Map<String, List<String>> getDominators() {
        return dominators;
    }

    public Map<Filter, Integer> getFilterToThreadId() {
        return filterToThreadId;
    }

    public Map<Integer, String> getThreadIdToType() {
        return threadIdToType;
    }

    public void setDominators(Map<String, List<String>> dominators) {
        this.dominators = dominators;
    }
    
    /**
     * Assign thread ids only to dynamic readers
     * 
     * @param ssg
     *            The static subgraph that contains the dynamic reader
     */
    private void assignThreadsNonOpt(StaticSubGraph ssg) {
        Filter f = ssg.getTopFilters()[0];
        boolean isDynamicInput = ssg.hasDynamicInput();
        boolean isStateful = f.getWorkNodeContent().isStateful();

        /* Check if it has a dynamic pop rate */
        if (isDynamicInput || isStateful) {
            Filter[] topFilters = ssg.getTopFilters();

            for (Filter dynamicReader : topFilters) {              
                Filter[] filterGraph = ssg.getFilterGraph();

                if (isVoidInputType(dynamicReader)) {
                    for (Filter filter : filterGraph) {
                        dominatorsInitEmptyIfNotEqual(
                                dynamicReader,
                                filter);
                    }
                } else {
                    filterToThreadId.put(
                            dynamicReader,
                            threadId);
                    if (!isVoidInputType(dynamicReader)
                            && !isNullType(dynamicReader)) {
                        threadIdToType.put(
                                threadId,
                                getFilterInputType(
                                        dynamicReader).toString());
                        for (Filter filter : filterGraph) {
                            dominatorsAdd(
                                    dynamicReader,
                                    filter);
                        }
                    }
                    threadId++;
                }
            } // end for loop
        }
    }
   
    /**
     * Assign thread ids to all filters
     * 
     * @param ssg
     */
    private void assignThreadsOpt(StaticSubGraph ssg) {

        boolean isDynamicInput = ssg.hasDynamicInput();
        Filter firstFilter = ssg.getFilterGraph()[0];
        
        int firstCore = SMPBackend.getComputeNode(firstFilter.getWorkNode()).coreID;    
        
        System.out.println("ThreadMapper.assignThreadsOpt  firstFilter=" + getFilterName(firstFilter) + " isDynamicInput=" + isDynamicInput);                
        
        // The first filter of an SSG always gets its own thread, 
        // with the exception of the first filter in the program.
        // Therefore, increment the count, unless we were at the 
        // first one.
        if (!isProgramSource(firstFilter) && !isFirstAfterFileInput(firstFilter)
                && !isProgramSink(firstFilter)) {
        	threadId++;
        }
        

                                   
        for (Filter filter : ssg.getFilterGraph()) {
            
            int filterCore = SMPBackend.getComputeNode(filter.getWorkNode()).coreID;
            
            // If the first thread is not dynamic, then 
            // it doesn't dominate anything, and it will 
            // be on the main thread.
            int thread = threadId;
            if (!isDynamicInput || ((ssg.getParent().getNumSSGs() == 1))) {
                thread = coreToThread(filterCore);  
                filterToThreadId.put(filter, thread);
                System.out.println("ThreadMapper.assignThreadsOpt  filter=" + getFilterName(filter) + " thread=" + thread + " core=" + filterCore);                
                continue;
            }   
            
            // Check to see if we have a fizzed filter. If a filter has
            // been fizzed, then it should have been assigned to a different
            // core.
            if (filterCore != firstCore) {
                thread = coreToThread(filterCore);  
            }
            else if ( isProgramSink(filter)) {                
                Filter prev = ProcessFilterWorkNode.getPreviousFilter(filter.getWorkNode());     
                // Need to special case when the program is just a source and sink.
                if (isProgramSource(prev)) {                    
                    thread = coreToThread(getFirstCore());
                } else {                
                    int core = SMPBackend.getComputeNode(prev.getWorkNode()).coreID;                                                
                    thread = coreToThread(core);
                }
            } 
            
            else if (isProgramSource(filter)) {                                                
                Filter next = ProcessFilterWorkNode.getNextFilter(filter.getWorkNode());    
                // Need to special case when the program is just a source and sink.s
                
                System.out.println("==> ThreadMapper.assignThreadsOpt  filter=" + getFilterName(filter) + " isProgramSource(filter)");                
                
                if (isProgramSink(next)) {                    
                    thread = coreToThread(getFirstCore());
                } else if (filter.getWorkNode().isFileInput()) {                
                    thread = coreToThread(SMPBackend.getComputeNode(next.getWorkNode()).coreID);
                } else {
                    thread = coreToThread(filterCore);
                }
            }            

            else if (isFirstAfterFileInput(filter)) {       
                
                System.out.println("==> ThreadMapper.assignThreadsOpt  filter=" + getFilterName(filter) + " isFirstAfterFileInput(filter)");                
                thread = coreToThread(filterCore);                  
            }            

            
            filterToThreadId.put(filter, thread);
            System.out.println("==> ThreadMapper.assignThreadsOpt  filter=" + getFilterName(filter) + " thread=" + thread);                
            dominatorsAdd(firstFilter, filter);
            
            // We need another special case here. A FileWriter should be dominated by the
            // last dynamic reader, even though they will be in separate SSGs.
            if (isLastBeforeFileOutput(filter)) {
                dominatorsAdd(firstFilter, ProcessFilterWorkNode.getNextFilter(filter.getWorkNode()));
            }
            
            if (thread < KjcOptions.smp) {
                threadIdToType.put(threadId,getFilterInputType(firstFilter).toString());
            }
                
        }    
       
    }
    
    private void dominatorsAdd(Filter f1, Filter f2) {
        if (!dominators.containsKey(getFilterName(f1))) {
            dominatorsInitEmpty(f1);
        }
        // Don't dominate yourself
        dominatorsAddIfNotEqual(
                f1,
                f2);
    }

    private void dominatorsAddIfNotEqual(Filter f1, Filter f2) {
        if (!equalNames(
                f1,
                f2)) {
            dominators.get(
                    getFilterName(f1)).add(
                    getFilterName(f2));
        }
    }

    private void dominatorsInitEmpty(Filter f) {
        dominators.put(
                f.getWorkNodeContent().getName(),
                new ArrayList<String>());
    }

    private void dominatorsInitEmptyIfNotEqual(Filter f1, Filter f2) {
        if (!equalNames(
                f1,
                f2)) {
            dominators.put(
                    getFilterName(f1),
                    new ArrayList<String>());
        }
    }

    private boolean equalNames(Filter f1, Filter f2) {
        return getFilterName(
                f1).equals(
                getFilterName(f2));
    }

    private CType getFilterInputType(Filter f) {
        return f.getWorkNodeContent().getInputType();
    }

    private String getFilterName(Filter f) {
        return f.getWorkNodeContent().getName();
    }
  
    private boolean isFirstAfterFileInput(Filter filter) {      
        return ProcessFilterWorkNode.isFirstAfterFileInput(filter);                   
    }

    private boolean isLastBeforeFileOutput(Filter filter) {
        return ProcessFilterWorkNode.isLastBeforeFileOutput(filter);       
    }

    private boolean isNullType(Filter f) {
        return (getFilterInputType(f) == null);
    }

    private boolean isProgramSink(Filter f) {
        return ProcessFilterWorkNode.isProgramSink(f);              
    }
    
    private boolean isProgramSource(Filter f) {
        return ProcessFilterWorkNode.isProgramSource(f);          
    }
    
    private boolean isVoidInputType(Filter f) {
        return (getFilterInputType(f) == CStdType.Void);
    }

	public static boolean isMain(int thread) {
		 return (thread < KjcOptions.smp);
	}
    
}
