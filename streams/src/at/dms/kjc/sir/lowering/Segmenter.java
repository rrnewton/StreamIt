package at.dms.kjc.sir.lowering;
/**
 * The Segmenter class is used to partition a graph of SIR Streams
 * into static subsections.
 */

import at.dms.kjc.ObjectDeepCloner;
import at.dms.kjc.iterator.IterFactory;
import at.dms.kjc.iterator.SIRFeedbackLoopIter;
import at.dms.kjc.iterator.SIRFilterIter;
import at.dms.kjc.iterator.SIRPhasedFilterIter;
import at.dms.kjc.iterator.SIRPipelineIter;
import at.dms.kjc.iterator.SIRSplitJoinIter;
import at.dms.kjc.sir.SIRDummySource;
import at.dms.kjc.sir.SIRDummySink;
import at.dms.kjc.sir.SIRFeedbackLoop;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.SIRPhasedFilter;
import at.dms.kjc.sir.SIRPipeline;
import at.dms.kjc.sir.SIRSplitJoin;
import at.dms.kjc.sir.StreamVisitor;

import at.dms.kjc.CStdType;
import at.dms.kjc.JIntLiteral;

import java.util.LinkedList;
import java.util.List;

public class Segmenter implements StreamVisitor {

	/* The SIR graph partitioned into static sections */
	SegmentedSIRGraph segmentedGraph = null;

	/* These are the child nodes of the pipeline currently being created */
	private LinkedList<Object> pipelines = null;

	/** An identifier to distinguish pipeline names */
	private int pipelineId = 0;

	/** a flag to turn on debugging */
	boolean debug = true;
	
	public Segmenter() {
		super();
	}

	/** TODO: Remove this after partitioning is fixed */
	public SegmentedSIRGraph noPartition(SIRStream str) {
		segmentedGraph = new SegmentedSIRGraph();
		segmentedGraph.addPipe(str);
		return segmentedGraph;
	}
	
	public SegmentedSIRGraph partition(SIRStream str) {
		log("Segmenter.partition called");
		segmentedGraph = new SegmentedSIRGraph();
		pipelines = new LinkedList<Object>();

		IterFactory.createFactory().createIter(str).accept(this);

		// This section is for debugging
		List<SIRStream> subgraphs = segmentedGraph.getStaticSubGraphs();		
		int j = 0;
		for (SIRStream ssg : subgraphs) {
			log("Segmenter.partition " + "printing ssg " + j
					+ "name=" + ssg.getName());
			at.dms.util.SIRPrinter printer = new at.dms.util.SIRPrinter("ssg_"
					+ j + ".txt");
			IterFactory.createFactory().createIter(ssg).accept(printer);
			j++;
			if (ssg instanceof SIRPipeline) {
				log("Segmenter.partition " + "ssg is a pipeline");
				log("Segmenter.partition "
						+ "((SIRPipeline)ssg).getChildren().size()= "
						+ ((SIRPipeline) ssg).getChildren().size());
			}
		}


		return segmentedGraph;
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
		segmentedGraph.addPipe(pipeline);
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
        throw new java.lang.RuntimeException("Dynamic rates do not support SIRPhasedFilters.");
	}

	@Override
	public void postVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) { /* Do nothing yet */ }

	@Override
	public void postVisitPipeline(SIRPipeline self, SIRPipelineIter iter) { /* Do nothing yet */ }

	@Override
	public void postVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) 
	{ /* Do nothing yet */ }

	@Override
	public void preVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) { /* Do nothing yet */ }

	@Override
	public void preVisitPipeline(SIRPipeline self, SIRPipelineIter iter) 
	{ /* Do nothing yet */ }

	@Override
	public void preVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		{ /* Do nothing yet */ }
		// if (self.getSplitter().getType().isDynamic()) { }		
	}

	
	
}
