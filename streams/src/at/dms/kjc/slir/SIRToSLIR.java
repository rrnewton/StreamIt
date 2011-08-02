/**
 * 
 */
package at.dms.kjc.slir;

import java.util.HashMap;
import java.util.List;

import at.dms.kjc.KjcOptions;
import at.dms.kjc.common.CommonUtils;
import at.dms.kjc.iterator.IterFactory;
import at.dms.kjc.iterator.SIRFeedbackLoopIter;
import at.dms.kjc.iterator.SIRFilterIter;
import at.dms.kjc.iterator.SIRPhasedFilterIter;
import at.dms.kjc.iterator.SIRPipelineIter;
import at.dms.kjc.iterator.SIRSplitJoinIter;
import at.dms.kjc.sir.SIRFeedbackLoop;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRPhasedFilter;
import at.dms.kjc.sir.SIRPipeline;
import at.dms.kjc.sir.SIRSplitJoin;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.StreamVisitor;
import at.dms.kjc.sir.linear.LinearAnalyzer;
import at.dms.kjc.sir.lowering.Flattener;
import at.dms.kjc.sir.lowering.SIRScheduler;
import at.dms.kjc.sir.lowering.SegmentedGraph;
import at.dms.kjc.sir.lowering.partition.WorkEstimate;


/**
 * @author soule
 *
 */
public class SIRToSLIR implements StreamVisitor {

	
	public class Result {
		private StreamGraph streamGraph;
		private Slicer slicer;
		public StreamGraph getStreamGraph() {
			return streamGraph;
		}
		public void setStreamGraph(StreamGraph streamGraph) {
			this.streamGraph = streamGraph;
		}
		public Slicer getSlicer() {
			return slicer;
		}
		public void setSlicer(Slicer slicer) {
			this.slicer = slicer;
		}
	}
	
	public SIRToSLIR() {
		log(this.getClass().getCanonicalName() + " SIRToSLIR()");		
	}

	private void log(String str) {
		boolean debug = true;
		if (debug)
			System.out.println(str);
	}
	
	/**
	 * 
	 * General Algorithm:
	 * for sir : sirs {
	 *   if (sir.isSource()) return new EntryFilter()
	 *   if (sir.isInk()) return new ExitFilter()
	 *   if (sir.isSplitter()) return new IdentityFilter() with splitting pattern
	 *        encoded in the output node
	 *   if (sir.isJoiner()) return new IdentityFilter() with joining pattern
	 *        encoded in the output node
	 *   if (sir.isFilter()) return new slir.Filter()
	 * }
	 * 
	 * Note: Take a look at the FlatGraph code to see how the hierarchy is handled
	 * @return
	 */
	public Result translate(SegmentedGraph segmentedGraph, int numCores) {
		log(this.getClass().getCanonicalName() + " translate()");		
		// A StreamGraph is a list of StaticSubGraphs. 
		// A StaticSubGraph has an input, output, and a List<Filter>;		
		StreamGraph streamGraph = new StreamGraph();
		List<SIRStream> subgraphs = segmentedGraph.getStaticSubGraphs();

		LinearAnalyzer lfa = Flattener.lfa;
		Slicer slicer = null;
		
		for (SIRStream str : subgraphs) {
			streamGraph.addRoot(new StaticSubGraph());
			//IterFactory.createFactory().createIter(str).accept(this);

			@SuppressWarnings("rawtypes")
			HashMap[] executionCounts = SIRScheduler.getExecutionCounts(str);

			// flatten the graph by running (super?) synch removal
			UnflatFilter[] topNodes = null;
			if (!KjcOptions.nopartition) {
				FlattenGraph.flattenGraph(str, lfa, executionCounts);
				topNodes = FlattenGraph.getTopLevelNodes();
				CommonUtils.println_debugging("Top Nodes:");
				for (int i = 0; i < topNodes.length; i++) {
					CommonUtils.println_debugging(topNodes[i].toString());
				}
			}

			Filter[] filterGraph = null;

			if (KjcOptions.tilera > 1 || KjcOptions.smp > 1) {
				if (!KjcOptions.nopartition) {
					System.out.println("Using OneFilterSlicer slicer");
					slicer = new OneFilterSlicer(topNodes, executionCounts);
				} else {
					System.out.println("Using FlattenAndPartition slicer");
					slicer = new FlattenAndPartition(topNodes, executionCounts,
							lfa, WorkEstimate.getWorkEstimate(str), numCores);
					((FlattenAndPartition) slicer).flatten(str, executionCounts);
				}

			} else {
				if (KjcOptions.nopartition) {
					slicer = new FlattenAndPartition(topNodes, executionCounts,
							lfa, WorkEstimate.getWorkEstimate(str), numCores);
					((FlattenAndPartition) slicer).flatten(str, executionCounts);
				} else {
					slicer = new SimpleSlicer(topNodes, executionCounts, lfa,
							WorkEstimate.getWorkEstimate(str), numCores);
				}
			}
			assert (slicer != null);
			filterGraph = slicer.partition();
			System.out.println("Traces: " + filterGraph.length);

		} // for (SIRStream str : subgraphs) 
		
		Result result = new Result();
		result.setStreamGraph(streamGraph);
		result.setSlicer(slicer);
		return result;
	}
	
	@Override
	public void visitFilter(SIRFilter self, SIRFilterIter iter) {
		log(this.getClass().getCanonicalName() + " visitFilter()");
		log("********************************************");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitPhasedFilter(SIRPhasedFilter self, SIRPhasedFilterIter iter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preVisitPipeline(SIRPipeline self, SIRPipelineIter iter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postVisitPipeline(SIRPipeline self, SIRPipelineIter iter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) {
		// TODO Auto-generated method stub
		
	}

}
