package at.dms.kjc.slir;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.backendSupport.GreedyBinPacking;
import at.dms.kjc.backendSupport.Layout;
import at.dms.kjc.smp.Core;
import at.dms.kjc.smp.FissionGroupStore;
import at.dms.kjc.smp.ProcessFilterUtils;
import at.dms.kjc.smp.SMPBackend;

/**
 * 
 * @author mgordon
 * 
 */
public class StreamGraph implements Layout<Core> {

    protected HashMap<InternalFilterNode, Core> layoutMap = new HashMap<InternalFilterNode, Core>();
    private GreedyBinPacking<Filter>            dominatorPacking;
    List<Filter>                                allFilters = new LinkedList<Filter>();
        
    protected int                               steadyMult;
    
    List<StaticSubGraph>                        ssgs;

    public StreamGraph() {
        steadyMult = KjcOptions.steadymult;
        ssgs = new ArrayList<StaticSubGraph>();
        dominatorPacking = new GreedyBinPacking<Filter>(SMPBackend.chip.size());
    }

    public void addSSG(StaticSubGraph ssg) {
        ssgs.add(ssg);
    }

    /**
     * Print the entire stream graph as a dot file
     * 
     * @param filename
     *            the output file name
     */
    public void dumpGraph(String filename) {
        StringBuffer buf = new StringBuffer();
        buf.append("digraph Flattend {\n");
        buf.append("size = \"8, 10.5\";\n");
        for (int i = 0; i < ssgs.size(); i++) {
            buf = dumpSingle(
                    buf,
                    ssgs.get(i));
            if (i < ssgs.size() - 1) {
                StaticSubGraph srcSSG = ssgs.get(i);
                StaticSubGraph dstSSG = ssgs.get(i + 1);
                Filter src = srcSSG.getFilterGraph()[srcSSG.getFilterGraph().length - 1];
                Filter dst = dstSSG.getFilterGraph()[0];
                assert src != null;
                assert dst != null;
                buf.append(src.hashCode() + " [ " + srcSSG.filterName(src)
                        + "\" ];\n");
                buf.append(src.hashCode() + " -> " + dst.hashCode() + ";\n");
            }

        }
        buf.append("}\n");
        try {
            FileWriter fw = new FileWriter(filename);
            fw.write(buf.toString());
            fw.close();
        } catch (Exception e) {
            System.err.println("Could not print extracted slices");
        }
    }

    @Override
    public Core getComputeNode(InternalFilterNode node) {
        
        //System.out.println("StreamGraph.getComputeNode node=" + node.getAsFilter());
        
        if (null == layoutMap.get(node)) {
            assert false : " StreamGraph.getComputeNode Node" + node.toString() + " is not in the layoutMap";    
        }                      
        
        if (node.getAsFilter().isFileInput()) {

            
            System.out.println("StreamGraph.getComputeNode node.getAsFilter().isFileInput()=" + node.getAsFilter().isFileInput());
            return layoutMap.get(node);    
        }
        else if (node.getAsFilter().isFileOutput()) {
            System.out.println("StreamGraph.getComputeNode node.getAsFilter().isFileOutput()=" + node.getAsFilter().isFileOutput() );                        
            Filter prev = ProcessFilterUtils.getPreviousFilter(node.getAsFilter());                                  
            System.out.println("StreamGraph.getComputeNode node.getAsFilter().isFileOutput()=" + node.getAsFilter().isFileOutput() + " prev=" + prev.getWorkNode());                        
            System.out.println("StreamGraph.getComputeNode node.getAsFilter().isFileOutput()=" + node.getAsFilter().isFileOutput() + " core is " + layoutMap.get(prev.getWorkNode()).getCoreID());                        
            return layoutMap.get(prev.getWorkNode());    
            //return layoutMap.get(node);
        }        
        else {
            return layoutMap.get(node);       
        }
    }

    public int getNumSSGs() {
        return ssgs.size();
    }

    public StaticSubGraph getSSG(int i) {
        return ssgs.get(i);
    }

    public StaticSubGraph getSSG0() {
        assert ssgs.size() == 1 : "Calling getSSG() on a graph with dynamic rates, and thus multiple SSGs!";
        return ssgs.get(0);
    }

    public List<StaticSubGraph> getSSGs() {
        return ssgs;
    }

    public int getSteadyMult() {
        return steadyMult;
    }

    /**
     * Assign the filternodes of the slice graph to tiles on the chip based on
     * the levels of the graph.
     */
    @Override
    public void runLayout() {
        System.out.println("TMDBinPackFissAll.runLayout()");

        List<Filter> slices = DataFlowOrder.getTraversal(getAllFilter());
        List<Filter> fizzedSlices = new LinkedList<Filter>();
        List<Filter> unfizzedSlices = new LinkedList<Filter>();
        List<Filter> dominators = new LinkedList<Filter>();
        
        
        // Get work estimates for all slices
        HashMap<WorkNode, Long> workEsts = new HashMap<WorkNode, Long>();
        for (Filter slice : slices) {
            long workEst = FilterWorkEstimate.getWork(slice);
            workEsts.put(
                    slice.getWorkNode(),
                    workEst);
        }

        // Categorize slices into predefined, dominators, fizzed and unfizzed
        // slices
        // Predefined filters are automatically added to off-chip memory
        for (Filter slice : slices) {
            if (slice.getWorkNode().isPredefined())
                setComputeNode(
                        slice.getWorkNode(),
                        SMPBackend.chip.getOffChipMemory());
            else if (slice.getStaticSubGraph().isTopFilter(
                    slice))
                dominators.add(slice);
            else if (FissionGroupStore.isFizzed(slice))
                fizzedSlices.add(slice);
            else
                unfizzedSlices.add(slice);
        }

        System.out.println("Number of fizzed slices: " + fizzedSlices.size());
        System.out.println("Number of unfizzed slices: "
                + unfizzedSlices.size());

        // use a global greedy bin packing across all ssgs and cores for the
        // dominators of the ssgs
        for (Filter filter : dominators) {
            layoutDominator(filter);
        }

        // Sort unfizzed slices by estimated work, most work first
        LinkedList<Filter> sortedUnfizzedSlices = new LinkedList<Filter>();
        for (Filter slice : unfizzedSlices) {
            boolean inserted = false;
            for (int x = 0; x < sortedUnfizzedSlices.size(); x++) {
                if (workEsts.get(slice.getWorkNode()) > workEsts
                        .get(sortedUnfizzedSlices.get(
                                x).getWorkNode())) {
                    sortedUnfizzedSlices.add(
                            x,
                            slice);
                    inserted = true;
                    break;
                }
            }

            if (!inserted)
                sortedUnfizzedSlices.add(slice);
        }

        // Attempt to load balance unfizzed slices across cores by using
        // LPT algorithm, which greedily assigns the slice with the most work
        // to the core with the least amount of estimate work
        long[] workAmounts = new long[SMPBackend.chip.size()];
        for (int x = 0; x < workAmounts.length; x++)
            workAmounts[x] = 0;

        for (Filter slice : sortedUnfizzedSlices) {
            // Find core with minimum amount of work
            long minWork = Long.MAX_VALUE;
            int minCore = -1;

            for (int core = 0; core < workAmounts.length; core++) {
                if (workAmounts[core] < minWork) {
                    minWork = workAmounts[core];
                    minCore = core;
                }
            }

            // Add slice to core
            setComputeNode(
                    slice.getWorkNode(),
                    SMPBackend.chip.getNthComputeNode(minCore));
            workAmounts[minCore] += workEsts.get(slice.getWorkNode());
        }

        for (int x = 0; x < workAmounts.length; x++)
            System.out.println("Core " + x + " has work: " + workAmounts[x]);

        // Schedule fizzed slices by assigning fizzed copies sequentially
        // across cores
        List<Filter> alreadyAssigned = new LinkedList<Filter>();
                
        for (Filter slice : fizzedSlices) {
            // If slice already assigned, skip it
            if (alreadyAssigned.contains(slice))
                continue;

            // Get fizzed copies of slice
            Filter[] fizzedCopies = FissionGroupStore.getFizzedSlices(slice);

            // Assign fizzed set sequentially across cores
            for (int x = 0; x < fizzedCopies.length; x++) {
                setComputeNode(
                        fizzedCopies[x].getWorkNode(),
                        SMPBackend.chip.getNthComputeNode(x));
                System.out.println("Assigning " + fizzedCopies[x].getWorkNode()
                        + " to core " + SMPBackend.chip.getNthComputeNode(
                                x).getCoreID());
            }

            // Mark fizzed set as assigned
            for (Filter fizzedSlice : fizzedCopies)
                alreadyAssigned.add(fizzedSlice);

            // If using shared buffers, then fission does not replace the
            // original
            // unfizzed slice with fizzed slices. The current 'slice' is the
            // original
            // unfizzed slice. Set the compute node for 'slice' to the
            // offChipMemory.
            // This is so that when we dump a dot-graph, we have a core to
            // return when
            // we display the 'slice' in the graph. Returning offChipMemory as
            // the core
            // is sub-optimal, though there's not much else we can do right now
            if (KjcOptions.sharedbufs) {
                assert FissionGroupStore.isUnfizzedSlice(slice);
                setComputeNode(
                        slice.getWorkNode(),
                        SMPBackend.chip.getOffChipMemory());
                alreadyAssigned.add(slice);
            }
        }
    }

    @Override
    public void setComputeNode(InternalFilterNode node, Core core) {
        System.out.println("==> StreamGraph.setComputeNode " + node + " to core " + core.getCoreID());
        assert node != null && core != null;
        // remember what filters each tile has mapped to it

        layoutMap.put(
                node,
                core);
        if (core.isComputeNode())
            core.getComputeCode().addFilter(
                    node.getAsFilter());
    }

    public void setSteadyMult(int steadyMult) {
        this.steadyMult = steadyMult;
    }

    /**
     * This method performs some standard cleanup on the slice graph. On return,
     * file readers and file writers are expanded to contain Kopi code to read
     * and write files. The slice graph will have any rate skew corrected and
     * will be converted to SimpleSlice's. The FilterInfo class will be usable.
     * 
     * Spacetime does not use this code since it allows general slices and
     * generates its own code for file readers and file writers.
     */
    public void simplifyFilters(int numCores) {
        for (StaticSubGraph ssg : ssgs) {
            simplifyStaticSubGraph(
                    ssg,
                    numCores);
        }
    }

    /**
     * This method performs some standard cleanup on the slice graph. On return,
     * file readers and file writers are expanded to contain Kopi code to read
     * and write files. The slice graph will have any rate skew corrected and
     * will be converted to SimpleSlice's. The FilterInfo class will be usable.
     * 
     * Spacetime does not use this code since it allows general slices and
     * generates its own code for file readers and file writers.
     */
    public StaticSubGraph simplifyStaticSubGraph(StaticSubGraph ssg,
            int numCores) {
        // Create code for predefined content: file readers, file writers.
        ssg.createPredefinedContent();
        // guarantee that we are not going to hack properties of filters in the
        // future
        WorkNodeInfo.canUse();
        // now we require that all input and output slice nodes have separate
        // init distribution pattern
        // for splitting and joining in the init stage (could be null or could
        // be equal to steady or could be
        // different)
        /*
         * if (KjcOptions.nopartition) { for (FilterSliceNode id :
         * ((FlattenAndPartition)getSlicer()).generatedIds) {
         * IDSliceRemoval.doit(id.getParent()); } }
         */

        // remove synchronization from the graph (remove ids added by the
        // conversion
        // to flatgraph
        SynchRemoval.doit(ssg);

        InstallInitDistributions.doit(ssg.getFilterGraph());
        // fix any rate skew introduced in conversion to Slice graph.
        CheckBuffering.doit(
                ssg,
                false,
                numCores);
        // decompose any pipelines of filters in the Slice graph.
        // slicer.ensureSimpleSlices();
        return ssg;
    }

    private StringBuffer dumpSingle(StringBuffer buf, StaticSubGraph ssg) {
        Filter[] filterGraph = ssg.getFilterGraph();
        for (int i = 0; i < filterGraph.length; i++) {
            Filter filter = filterGraph[i];
            assert filter != null;
            buf.append(filter.hashCode() + " [ " + ssg.filterName(filter)
                    + "\" ];\n");
            Filter[] next = ssg.getNext(filter/* ,parent */);
            for (int j = 0; j < next.length; j++) {
                assert next[j] != null;
                buf.append(filter.hashCode() + " -> " + next[j].hashCode()
                        + ";\n");
            }
        }
        return buf;
        // write the file

    }

    private Filter[] getAllFilter() {
        System.out.println("StreamGraph.getAllFilter called" );
        for (StaticSubGraph ssg : ssgs) {
            Filter[] tops = ssg.getTopFilters();
            for (Filter top : tops) {
                allFilters.add(top);
            }
        }
        return allFilters.toArray(new Filter[0]);
    }

    protected void layoutDominator(Filter filter) {
        assert filter.isTopFilter();

        if (KjcOptions.nofuse) {
        	//if we have turned off fusion, then we assume that we don't 
        	//want to take advantage of any pipeline parallelism between the
        	//dominators, do just map them to the 0th core.
        	 setComputeNode(
                     filter.getWorkNode(),
                     SMPBackend.chip.getNthComputeNode(0));
        }
        
        HashMap<Filter, Long> workMap = new HashMap<Filter, Long>();
        workMap.put(
                filter,
                FilterWorkEstimate.getWork(filter));

        dominatorPacking.pack(workMap);

        setComputeNode(
                filter.getWorkNode(),
                SMPBackend.chip.getNthComputeNode(dominatorPacking
                        .getBin(filter)));
    }
 

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.slir.StreamGraph other = new at.dms.kjc.slir.StreamGraph();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.StreamGraph other) {
        other.steadyMult = this.steadyMult;
        other.ssgs = (java.util.List)at.dms.kjc.AutoCloner.cloneToplevel(this.ssgs);
    }

  

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
    
}
