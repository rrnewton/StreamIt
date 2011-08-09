/**
 * A SegmentedGraph is a structure that contains each of the static subgraphs
 * from a SIRStream with dynamic regions. Each segment has a source and sink.
 * The SegmentedGraph also maintains connectivity and rate information.
 */
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

public class SegmentedSIRGraph implements StreamVisitor {

	/** This is the list of all pipelines created */
	private List<SIRStream> staticSubGraphs = null;

	/**
	 * Maps source to sinks to indicate connections between static subgraphs.
	 * Divergent operators will have multiple connections.
	 */
	private Map<SIRStream, List<SIRStream>> connections = null;

	/** These are the child nodes of the pipeline currently being created */
	private LinkedList<Object> pipelines = null;

	/** An identifier to distinguish pipeline names */
	private int pipelineId = 0;

	/** a flag to turn on debugging */
	boolean debug = true;

	/**
	 * Create a new empty SegmentedGraph.
	 */
	public SegmentedSIRGraph() {
		staticSubGraphs = new ArrayList<SIRStream>();
		connections = new HashMap<SIRStream, List<SIRStream>>();
	}

	public SegmentedSIRGraph init(SIRStream str, boolean isDynamic) {		
		if (!isDynamic) {
			this.addPipe(str);
			return this;
		} 

		pipelines = new LinkedList<Object>();

		IterFactory.createFactory().createIter(str).accept(this);

		// This section is for debugging
		log("*********************************");
		int j = 0;
		for (SIRStream ssg : staticSubGraphs) {
			log(this.getClass().getCanonicalName() + " init " + "printing ssg " + j + " name="
					+ ssg.getName());
			at.dms.util.SIRPrinter printer = new at.dms.util.SIRPrinter("ssg_"
					+ j + ".txt");
			IterFactory.createFactory().createIter(ssg).accept(printer);
			j++;
			if (ssg instanceof SIRPipeline) {
				log(this.getClass().getCanonicalName() + " init " + "ssg is a pipeline");
				log(this.getClass().getCanonicalName() + " init "
						+ "((SIRPipeline)ssg).getChildren().size()= "
						+ ((SIRPipeline) ssg).getChildren().size());
			}
		}
		log("*********************************");

		return this;
	}

	/**
	 * Add a new static subsection to the graph. Each subsection must be a
	 * pipeline with a source, some number of other filters, and a sink.
	 * 
	 * @param pipeline
	 *            subsection to add
	 * @return void
	 */
	public void addPipe(SIRStream pipeline) {
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

	public SIRStream getStaticSubGraph0() {
		assert staticSubGraphs.size() == 1 : "SegmentedGraph.getStaticSubGraph0 staticSubGraph should have only one subsection in static case.";
		return staticSubGraphs.get(0);
	}

	private void log(String str) {
		if (debug)
			System.out.println(str);
	}

	private SIRFilter createSink(SIRFilter pred, SIRFilterIter iter) {
		SIRFilter sink = new SIRDummySink(pred.getOutputType());
		log(this.getClass().getCanonicalName() + " createSink()");
		return sink;
	}

	private SIRFilter createSource(SIRFilter succ, SIRFilterIter iter) {
		SIRFilter source = new SIRDummySource(succ.getInputType());
		log(this.getClass().getCanonicalName() + " createSource()");
		return source;
	}

	private void finishPipeline(SIRFilter self, SIRFilterIter iter) {
		/* add the source and sink */
		if (!isSink(self)) {
			log(this.getClass().getCanonicalName() + " adding a sink after "
					+ self.getName());
			pipelines.add(createSink(self, iter));
		}
		if (!isSource(self)) {
			log(this.getClass().getCanonicalName() + " adding a source before "
					+ self.getName());
			pipelines.add(0, createSource((SIRFilter) pipelines.get(0), iter));
		}
		SIRPipeline pipeline = new SIRPipeline(null, uniquePipelineName());
		pipeline.setInit(SIRStream.makeEmptyInit());
		pipeline.setChildren(pipelines);
		addPipe(pipeline);
		pipelines = new LinkedList<Object>();
		log(this.getClass().getCanonicalName() + " pipeline.size()="
				+ pipeline.size());
	}

	private boolean isDynamicPop(SIRFilter self) {
		return self.getPop().isDynamic();
	}

	private boolean isDynamicPush(SIRFilter self) {
		return self.getPush().isDynamic();
	}

	private boolean isSink(SIRFilter self) {
		return (self.getOutputType() == CStdType.Void);
	}

	private boolean isSource(SIRFilter self) {
		return (self.getInputType() == CStdType.Void);
	}

	private String uniquePipelineName() {
		pipelineId++;
		return "SSGPipeline" + pipelineId;
	}

	@Override
	public void visitFilter(SIRFilter self, SIRFilterIter iter) {
		log(this.getClass().getCanonicalName() + " visitFilter()");
		log(this.getClass().getCanonicalName() + " self.getPopString="
				+ self.getPopString());
		log(this.getClass().getCanonicalName() + " self.getPushString="
				+ self.getPushString());
		log(this.getClass().getCanonicalName() + " self.getPeekString="
				+ self.getPeekString());
		log(this.getClass().getCanonicalName() + " isDynamicPush="
				+ isDynamicPush(self));
		log(this.getClass().getCanonicalName() + " isDynamicPop="
				+ isDynamicPop(self));
		log(this.getClass().getCanonicalName() + " iter.getPos="
				+ iter.getPos());

		SIRFilter filter = (SIRFilter) ObjectDeepCloner.deepCopy(self);

		// If its a dynamic push, then add this filter, and create
		// a new pipeline
		if (isDynamicPush(filter)) {
			filter.setPush(new JIntLiteral(1));
			if (isSource(filter)) {
				filter.setPop(new JIntLiteral(1));
				filter.setPeek(new JIntLiteral(1));
			}
			pipelines.add(filter);
			finishPipeline(filter, iter);
			return;
		}

		// If its a dynamic pop, check to see if there is anything
		// preceeding the filter that should be in its own pipeline
		if (isDynamicPop(filter)) {
			if (pipelines.size() > 0) {
				finishPipeline(filter, iter);
				return;
			}
			filter.setPop(new JIntLiteral(1));
			filter.setPeek(new JIntLiteral(1));
			pipelines.add(filter);
			if (isSink(filter)) {
				finishPipeline(filter, iter);
				return;
			}
		}
	}

	/******************************* Code below is not used for now ******************************/

	@Override
	public void visitPhasedFilter(SIRPhasedFilter self, SIRPhasedFilterIter iter) {
		throw new java.lang.RuntimeException(
				"Dynamic rates do not support SIRPhasedFilters.");
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
		{ /* Do nothing yet */
		}
		// if (self.getSplitter().getType().isDynamic()) { }
	}

}
