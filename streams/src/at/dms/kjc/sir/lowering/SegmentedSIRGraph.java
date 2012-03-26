package at.dms.kjc.sir.lowering;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.StreamItDot;
import at.dms.kjc.iterator.IterFactory;
import at.dms.kjc.iterator.SIRFeedbackLoopIter;
import at.dms.kjc.iterator.SIRFilterIter;
import at.dms.kjc.iterator.SIRPhasedFilterIter;
import at.dms.kjc.iterator.SIRPipelineIter;
import at.dms.kjc.iterator.SIRSplitJoinIter;
import at.dms.kjc.sir.SIRContainer;
import at.dms.kjc.sir.SIRDummySink;
import at.dms.kjc.sir.SIRDummySource;
import at.dms.kjc.sir.SIRFeedbackLoop;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIROperator;
import at.dms.kjc.sir.SIRPhasedFilter;
import at.dms.kjc.sir.SIRPipeline;
import at.dms.kjc.sir.SIRSplitJoin;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.StreamVisitor;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.StaticSubGraph;

/**
 * A SegmentedGraph is a structure that contains each of the static subgraphs
 * from a SIRStream with dynamic regions. Each segment has a source and sink.
 * The SegmentedGraph also maintains connectivity and rate information.
 * 
 * @author soule
 * 
 */
public class SegmentedSIRGraph implements StreamVisitor {

    /**
     * Maps source to sinks to indicate connections between static subgraphs.
     * Divergent operators will have multiple connections.
     */
    private Map<Integer, List<Integer>> connections     = null;

    /** An identifier to distinguish pipeline names */
    private int                         pipelineId      = 0;

    /** This is the list of all pipelines created */
    private List<SIRStream>             staticSubGraphs = null;

    /** a flag to turn on debugging */
    boolean                             debug           = true;

    /**
     * Create a new empty SegmentedGraph.
     */
    public SegmentedSIRGraph() {
        staticSubGraphs = new ArrayList<SIRStream>();
        connections = new HashMap<Integer, List<Integer>>();
    }

    /**
     * Add a new static subsection to the graph. Each subsection must be a
     * pipeline with a source, some number of other filters, and a sink.
     * 
     * @param pipeline
     *            subsection to add
     * @return void
     */
    public void addToSegmentedGraph(SIRStream pipeline) {
        staticSubGraphs.add(pipeline);
    }

    public void printGraph(String filename) {
        FileOutputStream out = null;
        try {
            File file = new File("./dotfiles", filename);
            file.getParentFile().mkdirs();     
            out = new FileOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        StreamItDot dot = new StreamItDot(new PrintStream(out));
        dot.print("digraph streamit {\n");
        for (SIRStream str : staticSubGraphs) {
            str.accept(dot);
        }
        dot.print("}\n");   
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
        

    /**
     * Returns the connections between static subsections.
     * 
     * @return the connections between static subsections.
     */
    public Map<Integer, List<Integer>> getConnections() {
        return connections;
    }

    /**
     * Returns the list of static subsections
     * 
     * @return the list of static subsections
     */
    public List<SIRStream> getStaticSubGraphs() {
        return staticSubGraphs;
    }

    public SegmentedSIRGraph init(SIRStream str, boolean isDynamic) {
        if (!isDynamic) {
            System.out
                    .println("SegmentedSIRGraph.init !Dynamic, no partitioning performed.");
            this.addToSegmentedGraph(str);
            return this;
        }
        IterFactory.createFactory().createIter(
                str).accept(
                this);
        System.out
                .println("SegmentedSIRGraph.init() isDynamic=true, number of partitions="
                        + staticSubGraphs.size());
        return this;
    }

    @Override
    public void postVisitFeedbackLoop(SIRFeedbackLoop self,
            SIRFeedbackLoopIter iter) { /* Do nothing yet */
    }

    @Override
    public void postVisitPipeline(SIRPipeline pipeline, SIRPipelineIter iter) {
        // We only want to do this at the top level
        if (pipeline.getParent() != null) {
            return;
        }
        List<SIROperator> allChildren = getAllChildren(pipeline);
        String name = uniquePipelineName();
        SIRPipeline currentPipeline = new SIRPipeline(null, name);

        for (int i = 0; i < allChildren.size() - 1; i++) {
             
            // First we want to check for illegal graphs. For now,
            // we will say that if a component is followed or preceded
            // by a dynamic filter, then the graph is illegal
            if ((allChildren.get(i) instanceof SIRContainer)
                    && (allChildren.get(i + 1) instanceof SIRFilter)) {
                assert !isDynamicPop((SIRFilter) allChildren.get(i + 1)) : "Illegal graph! A container cannot be followed by a dynamic pop rate.";
            }

            if ((allChildren.get(i) instanceof SIRFilter)
                    && (allChildren.get(i + 1) instanceof SIRContainer)) {
                assert !isDynamicPush((SIRFilter) allChildren.get(i)) : "Illegal graph! A container cannot be preceded by a dynamic push rate.";
            }

            currentPipeline.add((SIRStream) allChildren.get(i));

            if (allChildren.get(i) instanceof SIRContainer) {
                continue;
            }

            if ((allChildren.get(i) instanceof SIRFilter)
                    && (allChildren.get(i + 1) instanceof SIRFilter)) {
                if (isCut(
                        (SIRFilter) allChildren.get(i),
                        (SIRFilter) allChildren.get(i + 1))) {
                    SIRFilter filter = (SIRFilter) allChildren.get(i);
                  
                    filter.setPush(new JIntLiteral(1));
                    SIRFilter sink = createSink(filter);
                    currentPipeline.add(sink);
                    addToSegmentedGraph(currentPipeline);
                    int ssgSrcNum = staticSubGraphs.size() - 1;
                    int ssgDstNum = staticSubGraphs.size();
                    currentPipeline = new SIRPipeline(null,
                            uniquePipelineName());
                    SIRFilter next = (SIRFilter) allChildren.get(i + 1);
                    
                    filter.setDynamicPush(true);
                    next.setDynamicPop(true);
                    
                    SIRFilter source = createSource(next);
                    addConnection(
                            connections,
                            ssgSrcNum,
                            ssgDstNum);
                    currentPipeline.add(source);
                    next.setPop(new JIntLiteral(1));
                    next.setPeek(new JIntLiteral(1));
                }
            }
        }
        // add the last filter to the pipeline
        currentPipeline
                .add((SIRStream) allChildren.get(allChildren.size() - 1));
        addToSegmentedGraph(currentPipeline);
    }

    @Override
    public void postVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {

    }

    @Override
    public void preVisitFeedbackLoop(SIRFeedbackLoop self,
            SIRFeedbackLoopIter iter) {
        /* Do nothing */
    }

    @Override
    public void preVisitPipeline(SIRPipeline self, SIRPipelineIter iter) {
        /* Do nothing */
    }

    @Override
    public void preVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
        /* Do nothing */
    }

    /**
     * Sets the connections between static subsections.
     * 
     */
    public void setConnections(Map<Integer, List<Integer>> connections) {
        this.connections = connections;
    }

    @Override
    public void visitFilter(SIRFilter filter, SIRFilterIter iter) {      
        
        System.out.println("  * SegmentedSIRGraph.visitFilter filter=" + filter.getName() + " isDynamicPop=" + isDynamicPop(filter) +
                " isDynamicPush=" +isDynamicPush(filter));
        
        setDynamic(
                filter,
                isDynamicPop(filter),
                isDynamicPush(filter) );
              
    }

    @Override
    public void visitPhasedFilter(SIRPhasedFilter self, SIRPhasedFilterIter iter) {
        throw new java.lang.RuntimeException(
                "Dynamic rates do not support SIRPhasedFilters.");
    }

    public void visitPipeline(SIRFilter self, SIRFilterIter iter) {
      /* Do nothing */
    }

    /**
     * Add a connection between one ssg to another
     * 
     * @param connections
     *            the map of all connections
     * @param src
     *            the source of one connect
     * @param dst
     *            the destination of one connection
     */
    private void addConnection(Map<Integer, List<Integer>> connections,
            int src, int dst) {
        System.out.println("SegmentedSIRGraph.addConnection ssg src=" + src
                + "--> ssg dst=" + dst);
        if (!connections.containsKey(src)) {
            connections.put(
                    src,
                    new ArrayList<Integer>());
        }
        connections.get(
                src).add(
                dst);
    }

    private SIRFilter createSink(SIRFilter pred) {
        SIRFilter sink = new SIRDummySink(pred.getOutputType());
        return sink;
    }

    private SIRFilter createSource(SIRFilter succ) {
        SIRFilter source = new SIRDummySource(succ.getInputType());
        return source;
    }

    /**
     * Remove the hierarchy from the pipeline, and just return all children in
     * an in-order traversal.
     * 
     * @param pipeline
     *            the top level container
     * @return all of the descendants.
     */
    private List<SIROperator> getAllChildren(SIRPipeline pipeline) {
        List<SIROperator> allChildren = new ArrayList<SIROperator>();
        List<SIROperator> children = pipeline.getChildren();
        for (SIROperator child : children) {
            if (child instanceof SIRPipeline) {
                allChildren.addAll(getAllChildren((SIRPipeline) child));
            } else {
                // This will add splitjoins and feedback loops as
                // whole components.
                allChildren.add(child);
            }
        }
        return allChildren;
    }

    /**
     * Returns true if there should be a cut between two SIRFilters
     * @param sirStream the first filter
     * @param sirStream2 the second filter
     * @return true if there should be a cut, false otherwise
     */
    private boolean isCut(SIRFilter sirStream, SIRFilter sirStream2) {                
        /* Cut if there is a dynamic rate between the filters */
        if (isDynamicPush(sirStream) || isDynamicPop(sirStream2)) {
            return true;
        }
        /* Cut if the second filter is stateful */
        if (sirStream2.isStateful()) {
            return true;
        }
        return false;
    }

    private boolean isDynamicPop(SIRFilter second) {
        return second.getPop().isDynamic();
    }

    private boolean isDynamicPush(SIRFilter self) {
        return self.getPush().isDynamic();
    }

    private void setDynamic(SIRStream filter, boolean dynamicPop, boolean dynamicPush) {
        filter.setDynamicPop(dynamicPop);
        filter.setDynamicPush(dynamicPush);
        SIRStream parent = filter.getParent();
        while (null != parent) {           
            parent.setDynamicPop(dynamicPop);
            parent.setDynamicPush(dynamicPush);
            parent = parent.getParent();
        }
    }

    private String uniquePipelineName() {
        pipelineId++;
        return "SSGPipeline" + pipelineId;
    }

}
