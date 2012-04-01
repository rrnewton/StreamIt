package at.dms.kjc.smp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import at.dms.kjc.CStdType;
import at.dms.kjc.CType;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.StreamGraph;
import at.dms.kjc.slir.WorkNode;

/**
 * A singleton class for mapping filters to threads.
 * 
 * @author soule
 * 
 */
public class ThreadMapper {

    private static ThreadMapper mapper = null;
    private static int          threadId;
    static {
        if (KjcOptions.threadopt) {
            threadId = KjcOptions.smp - 1;
            // threadId = 0;
        } else {
            threadId = 0;
        }
    }

    public static int coreToThread(int core) {
        if (core == -1) {
            return -1;
        }
        int index = 0;
        for (int c : SMPBackend.coreOrder) {
            if (c == core)
                return index;
            index++;
        }
        assert (false) : "Core " + core + " not mapped to a thread!";
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

    public static int getNumThreads() {
        return threadId + 1;
    }

    public static boolean isMain(int thread) {
        return (thread < KjcOptions.smp);
    }

    /** a mapping from filter to thread id */
    private Map<Filter, Integer>        filterToThreadId;

    /** a mapping of dominator filter to dominated filter */
    private Map<String, LinkedHashSet<String>>   dominators;

    /** a mapping of thread to its input type */
    private Map<Integer, String>        threadIdToType;

    /** a mapping to filter to a token that it writes to */
    private Map<WorkNode, List<String>> tokenWrites;

    /** a mapping to filter to a token that it reads from */
    private Map<WorkNode, List<String>> tokenReads;

    /** a mapping to dominators to a token that it writes to */
    private Map<WorkNode, List<String>> dominatorToTokens;

    /**
     * Private constructor for singleton
     */
    private ThreadMapper() {
        filterToThreadId = new HashMap<Filter, Integer>();
        dominators = new HashMap<String, LinkedHashSet<String>>();
        threadIdToType = new HashMap<Integer, String>();
        tokenWrites = new HashMap<WorkNode, List<String>>();
        tokenReads = new HashMap<WorkNode, List<String>>();
        dominatorToTokens = new HashMap<WorkNode, List<String>>();
    }

    /**
     * Assign a unique id to each thread for dynamic readers
     * 
     * @param streamGraph
     *            The static subgraph that contains the dynamic reader
     */
    public void assignThreads(StreamGraph streamGraph) {
        System.out.println("ThreadMapper.assignThreads...");
        for (StaticSubGraph ssg : streamGraph.getSSGs()) {              
            if (KjcOptions.threadopt) {
                assignThreadsOpt(ssg);
            } else {
                assignThreadsNonOpt(ssg);
            }
        }
    }

    public Map<String, LinkedHashSet<String>> getDominators() {
        return dominators;
    }

    public Map<Filter, Integer> getFilterToThreadId() {
        return filterToThreadId;
    }

    public Map<Integer, String> getThreadIdToType() {
        return threadIdToType;
    }

    /**
     * @return the tokenReads
     */
    public Map<WorkNode, List<String>> getTokenReads() {
        return tokenReads;
    }

    /**
     * @return the dominatorToTokens
     */
    public Map<WorkNode, List<String>> getDominatorToTokens() {
        return dominatorToTokens;
    }

    /**
     * @return the tokenWrites
     */
    public Map<WorkNode, List<String>> getTokenWrites() {
        return tokenWrites;
    }

    public void setDominators(Map<String, LinkedHashSet<String>> dominators) {
        this.dominators = dominators;
    }

    private void addToListInMap(Map<WorkNode, List<String>> tokenWrites2,
            WorkNode key, String value) {
        if (!tokenWrites2.containsKey(key)) {
            tokenWrites2.put(
                    key,
                    new LinkedList<String>());
        }
        tokenWrites2.get(
                key).add(
                value);
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

        for (Filter filter : ssg.getFilterGraph()) {
            checkForTokens(
                    filter,
                    ssg);
        }

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
     * Check to see if we need any synchronization. We need synchronization if
     * three conditions are met: 1. Two filters are in the same SSG 2. The two
     * filters are on separate cores 3. Neither filter is a dominator
     * 
     * @param filter
     * @param ssg
     */
    private void checkForTokens(Filter filter, StaticSubGraph ssg) {
        // System.out.println("ThreadMapper.checkForTokens filter=" +
        // getFilterName(filter) );

        Filter firstFilter = ssg.getFilterGraph()[0];
        int filterCore = SMPBackend.getComputeNode(filter.getWorkNode()).coreID;
        if (!filter.getWorkNode().isFileOutput()) {

            // System.out.println("    ThreadMapper.checkForTokens filter=" +
            // getFilterName(filter) + " is not file output");

            for (Filter nextFilter : ProcessFilterUtils.getNextFilters(filter
                    .getWorkNode())) {
                // System.out.println("    ThreadMapper.checkForTokens filter="
                // + getFilterName(filter) + " and next=" +
                // getFilterName(nextFilter));
                if (ssg.containsFilter(nextFilter)) {
                    int nextCore = SMPBackend.getComputeNode(nextFilter
                            .getWorkNode()).coreID;
                   
                    if (filterCore != nextCore) {

                        if (!filter.getWorkNode().isFileInput()) {

//                            System.out
//                                    .println("    ThreadMapper.checkForTokens TOKEN BETWEEN filter="
//                                            + getFilterName(filter)
//                                            + " and next="
//                                            + getFilterName(nextFilter));
                            String tokenName = getFilterName(filter) + "_to_"
                                    + getFilterName(nextFilter) + "_token";
                            addToListInMap(
                                    dominatorToTokens,
                                    firstFilter.getWorkNode(),
                                    tokenName);
                            addToListInMap(
                                    tokenWrites,
                                    filter.getWorkNode(),
                                    tokenName);
                            addToListInMap(
                                    tokenReads,
                                    nextFilter.getWorkNode(),
                                    tokenName);
                        }

                    }
                } 
            }
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

        // The first filter of an SSG always gets its own thread,
        // with the exception of the first filter in the program.
        // Therefore, increment the count, unless we were at the
        // first one.
        if (!isProgramSource(firstFilter)
                && !isFirstAfterFileInput(firstFilter)
                && !isProgramSink(firstFilter)) {
            threadId++;
        }

        for (Filter filter : ssg.getFilterGraph()) {

            int filterCore = SMPBackend.getComputeNode(filter.getWorkNode()).coreID;

            checkForTokens(
                    filter,
                    ssg);

            // If the first thread is not dynamic, then
            // it doesn't dominate anything, and it will
            // be on the main thread.
            int thread = threadId;
            if (!isDynamicInput || ((ssg.getStreamGraph().getNumSSGs() == 1))) {
                thread = coreToThread(filterCore);
                filterToThreadId.put(
                        filter,
                        thread);
                System.out.println("ThreadMapper.assignThreadsOpt  filter="
                        + getFilterName(filter) + " thread=" + thread
                        + " core=" + filterCore);
                continue;
            }

            // Check to see if we have a fizzed filter. If a filter has
            // been fizzed, then it should have been assigned to a different
            // core.
            if (filterCore != firstCore) {              
                thread = coreToThread(filterCore);
            }

            if (isProgramSink(filter)) {              

                Filter prev = ProcessFilterUtils.getPreviousFilter(filter
                        .getWorkNode());               
                // Need to special case when the program is just a source and
                // sink.
                if (isProgramSource(prev)) {                   
                    thread = coreToThread(getFirstCore());
                } else {
                    // int core =
                    // SMPBackend.getComputeNode(prev.getWorkNode()).coreID;
                    // thread = coreToThread(core);
                    thread = filterToThreadId.get(prev);
                }
            }

            else if (isProgramSource(filter)) {
                Filter next = ProcessFilterUtils.getNextFilter(filter
                        .getWorkNode());
                // Need to special case when the program is just a source and
                // sink.
             
                if (isProgramSink(next)) {
                    thread = coreToThread(getFirstCore());
                } else if (filter.getWorkNode().isFileInput()) {
                    thread = coreToThread(SMPBackend.getComputeNode(next
                            .getWorkNode()).coreID);
                } else {
                    thread = coreToThread(filterCore);
                }
            }

            else if (isFirstAfterFileInput(filter)) {              
                thread = coreToThread(filterCore);
            }

            filterToThreadId.put(
                    filter,
                    thread);
            System.out.println("    + filter="
                    + getFilterName(filter) + " thread=" + thread);
            dominatorsAdd(
                    firstFilter,
                    filter);

            // We need another special case here. A FileWriter should be
            // dominated by the
            // last dynamic reader, even though they will be in separate SSGs.
            if (isLastBeforeFileOutput(filter)) {
                dominatorsAdd(
                        firstFilter,
                        ProcessFilterUtils.getNextFilter(filter.getWorkNode()));
            }

            if (thread < KjcOptions.smp) {
                threadIdToType.put(
                        threadId,
                        getFilterInputType(
                                firstFilter).toString());
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
                new LinkedHashSet<String>());
    }

    private void dominatorsInitEmptyIfNotEqual(Filter f1, Filter f2) {
        if (!equalNames(
                f1,
                f2)) {
            dominators.put(
                    getFilterName(f1),
                    new LinkedHashSet<String>());
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

}
