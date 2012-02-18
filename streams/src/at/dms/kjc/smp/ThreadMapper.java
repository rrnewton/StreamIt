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
            threadId = KjcOptions.smp;
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

    /** the set of dominated filters */
    private Set<String>               dominated;

    /** a mapping of dominator filter to dominated filter */
    private Map<String, List<String>> dominators;

    /** a mapping of thread to its input type */
    private Map<Integer, String>      threadIdToType;

    /**
     * Private constructor for singleton
     */
    private ThreadMapper() {
        filterToThreadId = new HashMap<Filter, Integer>();
        dominated = new HashSet<String>();
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

    public Set<String> getDominated() {
        return dominated;
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

    public void setDominated(Set<String> dominated) {
        this.dominated = dominated;
    }

    public void setDominators(Map<String, List<String>> dominators) {
        this.dominators = dominators;
    }

    public void setFilterToThreadId(Map<Filter, Integer> filterToThreadId) {
        this.filterToThreadId = filterToThreadId;
    }

    public void setThreadIdToType(Map<Integer, String> threadIdToType) {
        this.threadIdToType = threadIdToType;
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

            for (int i = 0; i < topFilters.length; i++) {
                Filter dynamicReader = topFilters[i];
                Filter[] filterGraph = ssg.getFilterGraph();

                if (isVoidType(dynamicReader)) {
                    for (Filter filter : filterGraph) {
                        dominatorsInitEmptyIfNotEqual(
                                dynamicReader,
                                filter);
                    }
                } else {
                    filterToThreadId.put(
                            dynamicReader,
                            threadId);
                    if (!isVoidType(dynamicReader)
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

        Filter f = ssg.getTopFilters()[0];
        boolean isDynamicInput = ssg.hasDynamicInput();
        boolean isStateful = f.getWorkNodeContent().isStateful();
        boolean isFileOutput = f.getWorkNode().isFileOutput();

       
        if (!isDynamicInput) {
            filterToThreadId.put(
                    f,
                    MAIN_THREAD);
            System.out.println("ThreadMapper.assignThreadsOpt filter=" + getFilterName(f) + " thread=" + MAIN_THREAD);
        }
        /* Check if it has a dynamic pop rate */
        else {
            Filter[] topFilters = ssg.getTopFilters();

            for (int i = 0; i < topFilters.length; i++) {
                Filter dynamicReader = topFilters[i];
                Filter[] filterGraph = ssg.getFilterGraph();

                if (isVoidType(dynamicReader)) {
                    for (Filter filter : filterGraph) {
                        dominatorsInitEmptyIfNotEqual(
                                dynamicReader,
                                filter);
                        filterToThreadId.put(
                                filter,
                                MAIN_THREAD);
                        System.out.println("ThreadMapper.assignThreadsOpt filter=" + getFilterName(filter) + " thread=" + MAIN_THREAD);                    
                    }
                } else {
                    int thread = threadId;  
                    if (isFileOutput) {
                        thread = MAIN_THREAD;
                    } 
                    filterToThreadId.put(dynamicReader,thread);
                    System.out.println("ThreadMapper.assignThreadsOpt filter=" + getFilterName(dynamicReader) + " thread=" + thread);                    
                    if (!isVoidType(dynamicReader)
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

    private boolean isNullType(Filter f) {
        return (getFilterInputType(f) == null);
    }

    private boolean isVoidType(Filter f) {
        return (getFilterInputType(f) == CStdType.Void);
    }
}
