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

public class ThreadMapper {

    private static ThreadMapper mapper = null;

    /* threads 0-KjcOptions.smp-1 are for the main threads */
    private static int threadId = KjcOptions.smp;

    private static final int MAIN_THREAD = -1;

    /**
     * Provides access to the singleton class
     * @return the single thread mapper
     */
    public static ThreadMapper getMapper() {
        if (mapper == null) {
            mapper = new ThreadMapper();
        }
        return mapper;
    } 

    /** a mapping from filter to thread id */
    private Map<Filter, Integer> filterToThreadId;

    /** the set of dominated filters */
    private Set<String> dominated;

    /** a mapping of dominator filter to dominated filter */
    private Map<String, List<String>> dominators;

    /**  a mapping of thread to its input type */
    private Map<Integer, String> threadIdToType;

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
     * @param ssg The static subgraph that contains the dynamic reader
     */
    public void assignThreads(StaticSubGraph ssg) {
        if (KjcOptions.threadopt) {
            assignThreadsOpt(ssg);
        } else {
            assignThreadsNonOpt(ssg);
        }

    }

    /**
     * Assign thread ids to all filters
     * @param ssg
     */
    private void assignThreadsOpt(StaticSubGraph ssg) {
        System.out.println("ThreadMapper.assignThreadsOpt");
        boolean isDynamicInput = ssg.hasDynamicInput();
        Filter f = ssg.getTopFilters()[0];
        boolean isStateful = f.getWorkNodeContent().isStateful();

        if (!isDynamicInput && !isStateful) {
            filterToThreadId.put(f, MAIN_THREAD);
        } else {
            Filter[] topFilters = ssg.getTopFilters();
            for (int i = 0; i < topFilters.length; i++) {
                Filter dynamicReader = topFilters[i];
                System.out.println("ThreadMapper.assignThreads filter = "
                        + dynamicReader.getWorkNode().toString()
                        + " has threadId=" + threadId + " and type="
                        + dynamicReader.getInputNode().getType().toString());
                if (CStdType.Void == dynamicReader.getInputNode().getType()) {
                    Filter[] filterGraph = ssg.getFilterGraph();
                    for (Filter filter : filterGraph) {
                        if (!dynamicReader.getWorkNodeContent().getName()
                                .equals(filter.getWorkNodeContent().getName())) {   
                            dominators.put(filter.getWorkNodeContent()
                                    .getName(), new ArrayList<String>());
                        }
                    }                    
                    continue;
                }

                CType type = dynamicReader.getWorkNodeContent().getInputType();
                System.out.println("ThreadMapper.assignThreads filter = "
                        + dynamicReader.getWorkNode().toString()
                        + " has threadId=" + threadId + " and type="
                        + type.toString());
                filterToThreadId.put(dynamicReader, threadId);
                if (type != CStdType.Void && type != null) {
                    threadIdToType.put(threadId, type.toString());
                    Filter[] filterGraph = ssg.getFilterGraph();
                    for (Filter filter : filterGraph) {
                        if (!dominators.containsKey(dynamicReader.getWorkNodeContent()
                                .getName())) {
                            dominators.put(dynamicReader.getWorkNodeContent()
                                    .getName(), new ArrayList<String>());
                        }
                        // Don't dominate yourself                      
                        if (!dynamicReader.getWorkNodeContent().getName()
                                .equals(filter.getWorkNodeContent().getName())) {                                   
                            dominators.get(dynamicReader.getWorkNodeContent()
                                    .getName()).add(filter.getWorkNodeContent()
                                            .getName());
                        }
                    }
                }
                threadId++;
            }
        }
    }

    /**
     * Assign thread ids only to dynamic readers
     * @param ssg The static subgraph that contains the dynamic reader
     */   
    private void assignThreadsNonOpt(StaticSubGraph ssg) {
        boolean isDynamicInput = ssg.hasDynamicInput();
        Filter f = ssg.getTopFilters()[0];

        boolean isStateful = f.getWorkNodeContent().isStateful();

        /* Check if it has a dynamic pop rate */
        if (isDynamicInput || isStateful) {
            Filter[] topFilters = ssg.getTopFilters();

            for (int i = 0; i < topFilters.length; i++) {
                Filter dynamicReader = topFilters[i];

                System.out.println("ThreadMapper.assignThreads filter = "
                        + dynamicReader.getWorkNode().toString()
                        + " has threadId=" + threadId + " and type="
                        + dynamicReader.getInputNode().getType().toString());



                if (CStdType.Void == dynamicReader.getInputNode().getType()) {
                    Filter[] filterGraph = ssg.getFilterGraph();
                    for (Filter filter : filterGraph) {
                        if (!dynamicReader.getWorkNodeContent().getName()
                                .equals(filter.getWorkNodeContent().getName())) {   
                            dominators.put(filter.getWorkNodeContent()
                                    .getName(), new ArrayList<String>());
                        }
                    }                    
                    continue;
                }

                CType type = dynamicReader.getWorkNodeContent().getInputType();

                System.out.println("ThreadMapper.assignThreads filter = "
                        + dynamicReader.getWorkNode().toString()
                        + " has threadId=" + threadId + " and type="
                        + type.toString());

                filterToThreadId.put(dynamicReader, threadId);

                if (type != CStdType.Void && type != null) {
                    threadIdToType.put(threadId, type.toString());
                    Filter[] filterGraph = ssg.getFilterGraph();
                    for (Filter filter : filterGraph) {

                        if (!dominators.containsKey(dynamicReader.getWorkNodeContent()
                                .getName())) {
                            dominators.put(dynamicReader.getWorkNodeContent()
                                    .getName(), new ArrayList<String>());
                        }

                        // Don't dominate yourself					    
                        if (!dynamicReader.getWorkNodeContent().getName()
                                .equals(filter.getWorkNodeContent().getName())) {									

                            dominators.get(dynamicReader.getWorkNodeContent()
                                    .getName()).add(filter.getWorkNodeContent()
                                            .getName());
                        }
                    }

                }
                threadId++;
            }

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
}
