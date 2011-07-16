package at.dms.kjc.sir.dynamic;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Segmenter implements StreamVisitor {

	public class SegmentedGraph {

		public void addPipe(SIRStream pipeline) {
			staticSubGraphs.add(pipeline);
		}
		
		public SegmentedGraph(List<SIRStream> staticSubGraphs,
				Map<SIRStream, List<SIRStream>> connections) {
			this.staticSubGraphs = staticSubGraphs;
			this.connections = connections;
		}

		public List<SIRStream> getStaticSubGraphs() {
			return staticSubGraphs;
		}

		public void setStaticSubGraphs(List<SIRStream> staticSubGraphs) {
			this.staticSubGraphs = staticSubGraphs;
		}

		public Map<SIRStream, List<SIRStream>> getConnections() {
			return connections;
		}

		public void setConnections(Map<SIRStream, List<SIRStream>> connections) {
			this.connections = connections;
		}

		/* This is the list of all pipelines created */
		private List<SIRStream> staticSubGraphs = null;

		/*
		 * Maps source to sinks to indicate connections between static
		 * subgraphs. Divergent operators will have multiple connections.
		 */
		private Map<SIRStream, List<SIRStream>> connections = null;
	}

	/* The SIR graph partitioned into static sections */
	SegmentedGraph segmentedGraph = null;

	/* These are the child nodes of the pipeline currently being created */
	private LinkedList<Object> children = null;

	/* an identifier to distinguish pipeline names */
	private int pipelineId = 0;

	public Segmenter() {
		super();
		segmentedGraph = new SegmentedGraph(
				new ArrayList<SIRStream>(),
				new HashMap<SIRStream, List<SIRStream>>());
		children = new LinkedList<Object>();

	}

	public SegmentedGraph partition(SIRStream str) {
		IterFactory.createFactory().createIter(str).accept(this);
		return segmentedGraph;
	}

	private void log(String str) {
		boolean debug = false;
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
			children.add(createSink(self, iter));
		}
		if (!isSource(self)) {
			log(this.getClass().getCanonicalName() + " adding a source before "
					+ self.getName());
			children.add(0, createSource((SIRFilter) children.get(0), iter));
		}
		SIRPipeline pipeline = new SIRPipeline(null, uniquePipelineName());
		pipeline.setInit(SIRStream.makeEmptyInit());
		pipeline.setChildren(children);
		segmentedGraph.addPipe(pipeline);
		children = new LinkedList<Object>();
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

	@Override
	public void postVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) {
		// TODO Auto-generated method stub
		log(this.getClass().getCanonicalName() + " postVisitFeedbackLoop()");
	}

	@Override
	public void postVisitPipeline(SIRPipeline self, SIRPipelineIter iter) {
		// TODO Auto-generated method stub
		log(this.getClass().getCanonicalName() + " postVisitPipeline()");
	}

	@Override
	public void postVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		// TODO Auto-generated method stub
		log(this.getClass().getCanonicalName() + " postVisitSplitJoin()");
	}

	@Override
	public void preVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) {
		// TODO Auto-generated method stub
		log(this.getClass().getCanonicalName() + " preVisitFeedbackLoop()");
	}

	@Override
	public void preVisitPipeline(SIRPipeline self, SIRPipelineIter iter) {
		// TODO Auto-generated method stub
		log(this.getClass().getCanonicalName() + " preVisitPipeline()");
		// pipeline = (SIRPipeline) self.deepClone();
	}

	@Override
	public void preVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		// TODO Auto-generated method stub
		log(this.getClass().getCanonicalName() + " preVisitSplitJoin()");
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

		// SIRFilter filter = self;
		SIRFilter filter = (SIRFilter) ObjectDeepCloner.deepCopy(self);

		// If its a dynamic push, then add this filter, and create
		// a new pipeline
		if (isDynamicPush(filter)) {
			// TODO: this should be on a copy
			filter.setPush(new JIntLiteral(1));
			if (isSource(filter)) {
				filter.setPop(new JIntLiteral(1));
				filter.setPeek(new JIntLiteral(1));
			}
			children.add(filter);
			finishPipeline(filter, iter);
			return;
		}

		// If its a dynamic pop, check to see if there is anything
		// preceeding the filter that should be in its own pipeline
		if (isDynamicPop(filter)) {
			if (children.size() > 0) {
				finishPipeline(filter, iter);
				return;
			}
			// TODO: this should be on a copy
			filter.setPop(new JIntLiteral(1));
			filter.setPeek(new JIntLiteral(1));
			children.add(filter);
			if (isSink(filter)) {
				finishPipeline(filter, iter);
				return;
			}
		}
	}

	@Override
	public void visitPhasedFilter(SIRPhasedFilter self, SIRPhasedFilterIter iter) {
		// TODO Auto-generated method stub
		log(this.getClass().getCanonicalName() + " visitPhasedFilter()");
	}

}
