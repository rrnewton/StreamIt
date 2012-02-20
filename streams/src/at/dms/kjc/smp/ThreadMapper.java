package at.dms.kjc.smp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private static final int    MAIN_THREAD = -1;
    private static ThreadMapper mapper      = null;
    private static int          threadId;
    static {
        if (KjcOptions.threadopt) {
            threadId = KjcOptions.smp - 1;
        } else {
            threadId = 0;
        }
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

    public static int coreToThread(int core) {
        int index = 0;
        for (int c : SMPBackend.coreOrder) {
            if (c == core) return index;
            index++;
        }
        assert (false) : "Core " + core +" not mapped to a thread!";
        return -1;
    }
    
    
    /** a mapping from filter to thread id */
    private Map<Filter, Integer>      filterToThreadId;

   

    /** a mapping of dominator filter to dominated filter */
    private Map<String, List<String>> dominators;

    /** a mapping of thread to its input type */
    private Map<Integer, String>      threadIdToType;

    /**
     * Private constructor for singleton
     */
    private ThreadMapper() {
        filterToThreadId = new HashMap<Filter, Integer>();     
        dominators = new HashMap<String, List<String>>();
        threadIdToType = new HashMap<Integer, String>();
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
    
    private boolean isFirstAfterFileInput(Filter filter) {
        if (isProgramSource(filter)) {
            return false;
        }
        Filter prev = ProcessFilterWorkNode.getPreviousFilter(filter.getWorkNode());        
        return prev.getWorkNode().isFileInput();        
    }
   
    private boolean isLastBeforeFileOutput(Filter filter) {
        if (isProgramSink(filter)) {
            return false;
        }
        Filter next = ProcessFilterWorkNode.getNextFilter(filter.getWorkNode());
        return next.getWorkNode().isFileOutput();
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
        System.out.println("ThreadMapper.assignThreadsOpt");

        boolean isDynamicInput = ssg.hasDynamicInput();
        Filter firstFilter = ssg.getFilterGraph()[0];
        
        // The first filter of an SSG always gets its own thread, 
        // with the exception of the first filter in the program.
        // Therefore, increment the count, unless we were at the 
        // first one.
        if (!isProgramSource(firstFilter) && !isFirstAfterFileInput(firstFilter)) {
            threadId++;
        }
                
        for (Filter filter : ssg.getFilterGraph()) {
            
            // If the first thread is not dynamic, then 
            // it doesn't dominate anything, and it will 
            // be on the main thread.
            if (!isDynamicInput) {
                filterToThreadId.put(filter, MAIN_THREAD);
                System.out.println("ThreadMapper.assignThreadsOpt  filter=" + getFilterName(filter) + " thread=" + MAIN_THREAD);                
                continue;
            }   
            int thread = threadId;
            if (isProgramSource(filter) || isProgramSink(filter) || isFirstAfterFileInput(firstFilter)) {
                thread = MAIN_THREAD;
            }            
            filterToThreadId.put(filter, thread);
            System.out.println("ThreadMapper.assignThreadsOpt  filter=" + getFilterName(filter) + " thread=" + thread);                
            dominatorsAdd(firstFilter, filter);
            
            // We need another special case here. A FileWriter should be dominated by the
            // last dynamic reader, even though they will be in separate SSGs.
            if (isLastBeforeFileOutput(filter)) {
                dominatorsAdd(firstFilter, ProcessFilterWorkNode.getNextFilter(filter.getWorkNode()));
            }
            
            if (thread != MAIN_THREAD) {
                threadIdToType.put(threadId,getFilterInputType(firstFilter).toString());
            }
                
        }
      
    
        
    }
//        
//        
//        Filter f = ssg.getTopFilters()[0];
//        boolean isDynamicInput = ssg.hasDynamicInput();
//        boolean isFileOutput = f.getWorkNode().isFileOutput();
//
//        if (!isDynamicInput) {
//            filterToThreadId.put(f, MAIN_THREAD);
//            System.out.println("ThreadMapper.assignThreadsOpt 1 filter=" + getFilterName(f) + " thread=" + MAIN_THREAD);
//        } else {
//            /* Check if it has a dynamic pop rate */               
//            for (Filter topFilter : ssg.getTopFilters()) {              
//                
//                Filter[] filterGraph = ssg.getFilterGraph();
//
//                if (isVoidInputType(topFilter)) {
//                    for (Filter filter : filterGraph) {
//                        dominatorsInitEmptyIfNotEqual(
//                                topFilter,
//                                filter);
//                        filterToThreadId.put(
//                                filter,
//                                MAIN_THREAD);
//                        System.out.println("ThreadMapper.assignThreadsOpt 2 filter=" + getFilterName(filter) + " thread=" + MAIN_THREAD);                    
//                    }
//                } else {
//                    int thread = threadId;  
//                    if (isFileOutput) {
//                        thread = MAIN_THREAD;
//                    } 
//                    filterToThreadId.put(topFilter,thread);
//                    System.out.println("ThreadMapper.assignThreadsOpt filter=" + getFilterName(topFilter) + " thread=" + thread);                    
//                    if (!isVoidInputType(topFilter)
//                            && !isNullType(topFilter)) {
//                        threadIdToType.put(
//                                threadId,
//                                getFilterInputType(
//                                        topFilter).toString());
//                        for (Filter filter : filterGraph) {
//                            dominatorsAdd(
//                                    topFilter,
//                                    filter);
//                        }
//                    }
//                    
//                    if (!topFilter.getWorkNode().isFileInput()) {
//                        threadId++;                        
//                    }
//                    
//
//
//                
//                }
//            } // end for loop
//        }
//    }

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
    
    private CType getFilterOutputType(Filter f) {
        return f.getWorkNodeContent().getOutputType();
    }

    private String getFilterName(Filter f) {
        return f.getWorkNodeContent().getName();
    }

    private boolean isNullType(Filter f) {
        return (getFilterInputType(f) == null);
    }

    private boolean isVoidInputType(Filter f) {
        return (getFilterInputType(f) == CStdType.Void);
    }
    
    private boolean isProgramSource(Filter f) {
       return f.getWorkNode().isFileInput() || (getFilterInputType(f) == CStdType.Void);
    }
    
    private boolean isProgramSink(Filter f) {
        return f.getWorkNode().isFileOutput() || (getFilterOutputType(f) == CStdType.Void);
    }
    
}
