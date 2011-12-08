package at.dms.kjc.sir.lowering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.dms.kjc.CStdType;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.ObjectDeepCloner;
import at.dms.kjc.iterator.IterFactory;
import at.dms.kjc.iterator.SIRFeedbackLoopIter;
import at.dms.kjc.iterator.SIRFilterIter;
import at.dms.kjc.iterator.SIRPhasedFilterIter;
import at.dms.kjc.iterator.SIRPipelineIter;
import at.dms.kjc.iterator.SIRSplitJoinIter;
import at.dms.kjc.sir.SIRDummySink;
import at.dms.kjc.sir.SIRDummySource;
import at.dms.kjc.sir.SIRFeedbackLoop;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRPhasedFilter;
import at.dms.kjc.sir.SIRPipeline;
import at.dms.kjc.sir.SIRSplitJoin;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.StreamVisitor;

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
	private Map<SIRStream, List<SIRStream>> connections = null;

	/** An identifier to distinguish pipeline names */
	private int pipelineId = 0;

	/** These are the child nodes of the pipeline currently being created */
	private List<SIRStream> pipelineChildren = null;

	/** This is the list of all pipelines created */
	private List<SIRStream> staticSubGraphs = null;

	/** a flag to turn on debugging */
	boolean debug = true;

	/**
	 * Create a new empty SegmentedGraph.
	 */
	public SegmentedSIRGraph() {
		staticSubGraphs = new ArrayList<SIRStream>();
		connections = new HashMap<SIRStream, List<SIRStream>>();
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

	/**
	 * Returns the connections between static subsections.
	 * 
	 * @return the connections between static subsections.
	 */
	public Map<SIRStream, List<SIRStream>> getConnections() {
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
			System.out.println("SegmentedSIRGraph.init !Dynamic, no partitioning performed.");
			this.addToSegmentedGraph(str);
			return this;
		}
		startPipeline();
		IterFactory.createFactory().createIter(str).accept(this);
		endPipeline();

		return this;
	}

	@Override
	public void postVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) { /* Do nothing yet */
	}

	@Override
	public void postVisitPipeline(SIRPipeline self, SIRPipelineIter iter) { /*
																			 * Do
																			 * nothing
																			 * yet
																			 */
	}

	@Override
	public void postVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) { /*
																				 * Do
																				 * nothing
																				 * yet
																				 */
	}

	@Override
	public void preVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) { /* Do nothing yet */
	}

	@Override
	public void preVisitPipeline(SIRPipeline self, SIRPipelineIter iter) { /*
																			 * Do
																			 * nothing
																			 * yet
																			 */
	}

	@Override
	public void preVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		/* Do nothing yet */
	}

	@Override
	public void visitFilter(SIRFilter self, SIRFilterIter iter) {
		System.out.println("SegmentedSIRGraph.visitFilter"
				+ this.getClass().getCanonicalName() + " visitFilter() "
				+ self.getName() + " isDynamicPush=" + isDynamicPush(self)
				+ " isDynamicPop=" + isDynamicPop(self) 
				+ " isStateful=" + self.isStateful()				
				+ " iter.getPos=" + iter.getPos());

		SIRFilter filter = (SIRFilter) ObjectDeepCloner.deepCopy(self);


		boolean isSource = CStdType.Void == filter.getInputType();			
		boolean isSink = CStdType.Void == filter.getOutputType();			

				
		// If this is a completely static filter, then we
		// just add it to the current pipeline
		//if (!isDynamicPush(filter) && !(isDynamicPop(filter))) {
		if (!isDynamicPush(filter) && !(isDynamicPop(filter)) 
					&& !filter.isStateful()){					
			pipelineChildren.add(filter);
		}
							
		else if (filter.isStateful()) {			
			if (isSource) {
				filter.setPush(new JIntLiteral(1));
				pipelineChildren.add(filter);
				pipelineChildren.add(createSink(filter, iter));
				endPipeline();
				startPipeline();
			} else {
				endPipeline();
				startPipeline();
				pipelineChildren.add(createSource(filter));
				filter.setPop(new JIntLiteral(1));
				filter.setPeek(new JIntLiteral(1));
				filter.setPush(new JIntLiteral(1));
				pipelineChildren.add(filter);
				pipelineChildren.add(createSink(self, iter));
				endPipeline();
				startPipeline();
			}
		}
		
		else if (isDynamicPush(filter) && !(isDynamicPop(filter))) {
			System.out
					.println("SegmentedSIRGraph.visitFilter adding the dynamic push only case ");
			filter.setPush(new JIntLiteral(1));
			pipelineChildren.add(filter);
			pipelineChildren.add(createSink(filter, iter));
			endPipeline();
			startPipeline();
		}

		else if (!isDynamicPush(filter) && (isDynamicPop(filter))) {
			endPipeline();
			startPipeline();
			pipelineChildren.add(createSource(filter));
			filter.setPop(new JIntLiteral(1));
			filter.setPeek(new JIntLiteral(1));
			pipelineChildren.add(filter);
		}

		else { // if (isDynamicPush(filter) && (isDynamicPop(filter))) {
			endPipeline();
			startPipeline();
			pipelineChildren.add(createSource(filter));
			filter.setPop(new JIntLiteral(1));
			filter.setPeek(new JIntLiteral(1));
			filter.setPush(new JIntLiteral(1));
			pipelineChildren.add(filter);
			pipelineChildren.add(createSink(filter, iter));
			endPipeline();
			startPipeline();
		}
	}

	private void endPipeline() {
		if (pipelineChildren.size() == 0) {
			return;
		}
		// Create a new empty list of pipeline children
		String name = uniquePipelineName();

		System.out.println("SegmentedSIRGraph.visitFilter creating pipeline ");

		SIRPipeline pipeline = new SIRPipeline(null, name);

		pipeline.setInit(SIRStream.makeEmptyInit());
		this.setChildren(pipeline, pipelineChildren);

		addToSegmentedGraph(pipeline);
	}

	private void startPipeline() {
		// Create a new empty list of pipeline children
		pipelineChildren = new LinkedList<SIRStream>();

	}

	@Override
	public void visitPhasedFilter(SIRPhasedFilter self, SIRPhasedFilterIter iter) {
		throw new java.lang.RuntimeException(
				"Dynamic rates do not support SIRPhasedFilters.");
	}

	private SIRFilter createSink(SIRFilter pred, SIRFilterIter iter) {
		SIRFilter sink = new SIRDummySink(pred.getOutputType());
		return sink;
	}

	private SIRFilter createSource(SIRFilter succ) {
		SIRFilter source = new SIRDummySource(succ.getInputType());
		return source;
	}

	private void setChildren(SIRPipeline pipeline, List<SIRStream> children) {
		pipeline.setChildren(pipelineChildren);
	}

	private boolean isDynamicPop(SIRFilter self) {
		return self.getPop().isDynamic();
	}

	private boolean isDynamicPush(SIRFilter self) {
		return self.getPush().isDynamic();
	}

	private String uniquePipelineName() {
		pipelineId++;
		return "SSGPipeline" + pipelineId;
	}

}
