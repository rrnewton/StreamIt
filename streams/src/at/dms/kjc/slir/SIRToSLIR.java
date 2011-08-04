/**
 * 
 */
package at.dms.kjc.slir;

import java.util.ArrayList;
import java.util.Arrays;
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
import at.dms.kjc.sir.SIRFileWriter;
import at.dms.kjc.sir.SIRFileReader;
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

	private StaticSubGraph currentSSG = null;
		
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
	public StreamGraph translate(SegmentedGraph segmentedGraph, int numCores) {
		log(this.getClass().getCanonicalName() + " translate()");		
		// A StreamGraph is a list of StaticSubGraphs. 
		// A StaticSubGraph has an input, output, and a List<Filter>;		
		//StreamGraph streamGraph = new StreamGraph();
		StreamGraph streamGraph = null;
		List<SIRStream> subgraphs = segmentedGraph.getStaticSubGraphs();

		LinearAnalyzer lfa = Flattener.lfa;
		
		for (SIRStream str : subgraphs) {

			InputPort inputPort = new UnaryInputPort();
			OutputPort outputPort = new UnaryOutputPort();
				//StaticSubGraph ssg = new StaticSubGraph(inputPort, outputPort, new ArrayList<Filter>(Arrays.asList(topNodes)));
			currentSSG = new StaticSubGraph();			
			IterFactory.createFactory().createIter(str).accept(this);
			currentSSG.setInputPort(inputPort);
			currentSSG.setOutputPort(outputPort);
			
			
			
			// TODO: REMOVE SOON!
			@SuppressWarnings("rawtypes")
			HashMap[] executionCounts = SIRScheduler.getExecutionCounts(str);

			
			streamGraph = new StreamGraph(executionCounts,
					lfa, WorkEstimate.getWorkEstimate(str), numCores);
			streamGraph.flatten(str, executionCounts);
			// END SECTION OF TO REMOVE
			
			
			streamGraph.addRoot(currentSSG);
			


			
			
//			
//			// Don't use flattenGraph at all
//			
//			// Each SIROperator -> Filter, even joiners and splitters
//			// joiners and splitters will be identity (leave it for sync removal
//			// to get rid of the identities)
//			
//			// flatten the graph by running (super?) synch removal
//			UnflatFilter[] topNodes = null;
//			if (!KjcOptions.nopartition) {
//				FlattenGraph.flattenGraph(str, lfa, executionCounts);
//				topNodes = FlattenGraph.getTopLevelNodes();
//				CommonUtils.println_debugging("Top Nodes:");
//				for (int i = 0; i < topNodes.length; i++) {
//					CommonUtils.println_debugging(topNodes[i].toString());
//				}
//			}
//
//			Filter[] filterGraph = null;
//
//			// Conversion pass, SIRFilter to Filter, looks like OneFilterSlicer
//			// but removing some cruft.
//			// There should not be a "slicer"
//						
//			if (KjcOptions.tilera > 1 || KjcOptions.smp > 1) {
//				if (!KjcOptions.nopartition) {
//					System.out.println("Using OneFilterSlicer slicer");
//					slicer = new OneFilterSlicer(topNodes, executionCounts);
//				} else {
//					System.out.println("Using FlattenAndPartition slicer");
//					slicer = new FlattenAndPartition(topNodes, executionCounts,
//							lfa, WorkEstimate.getWorkEstimate(str), numCores);
//					((FlattenAndPartition) slicer).flatten(str, executionCounts);
//				}
//
//			} else {
//				if (KjcOptions.nopartition) {
//					slicer = new FlattenAndPartition(topNodes, executionCounts,
//							lfa, WorkEstimate.getWorkEstimate(str), numCores);
//					((FlattenAndPartition) slicer).flatten(str, executionCounts);
//				} else {
//					slicer = new SimpleSlicer(topNodes, executionCounts, lfa,
//							WorkEstimate.getWorkEstimate(str), numCores);
//				}
//			}
//			assert (slicer != null);
//			
//			
//			
//			filterGraph = slicer.partition();
//			System.out.println("Traces: " + filterGraph.length);
//
//
//			// TODO: Set the inputPort, outputPort, etc.
//			// topSlices is pointers to the roots of the forest
//			// filterGraph(i.e. sliceGraph) is all the filters in the graph
//			// Want to get rid of unflat filters
//			// "TopNodes" should be filters...
//			// TODO: Get rid of things to do with Slicing....
//			// Remove FlattenAndPartition, SimpleSlicer, etc. once SIRToSLIR is working
//			// Might need to keep Unflat... see how it works. Prefer to not use it.
//			
//			// Filter -> WorkNodeContent 
//			// WorkNodeContent installed in WorkNode (Decide if we need both)
//			// Filter requires InputNode, OutputNode, WorkNode
//			// Examples : Splitter, SIRFilter, Joiner
//			
//			// SIRFitler -> (Input, Output (WorkNode (WorkNodeContent (SIRFilter))))
//			// Splitter -> (Input, (Output i.e. dup, rr) (WorkNode (WorkNodeContent (Identity))))
//			
//			// TODO: The FlattenAndPartition does pretty much what I want to do
//			// SIR to Slice node, is already in there.
//			
//			
//			InputPort inputPort = null;
//			OutputPort outputPort = null;
//			//StaticSubGraph ssg = new StaticSubGraph(inputPort, outputPort, new ArrayList<Filter>(Arrays.asList(topNodes)));
//			StaticSubGraph ssg = new StaticSubGraph();
//			streamGraph.addRoot(ssg);
			
			
		} // for (SIRStream str : subgraphs) 
				
		return streamGraph;
	}
	
	@Override
	public void visitFilter(SIRFilter sirFilter, SIRFilterIter iter) {
		log(this.getClass().getCanonicalName() + " visitFilter()");
		log(this.getClass().getCanonicalName() + " self:" + sirFilter.getName());
		if (sirFilter.getName().contains("DummySouce") || sirFilter.getName().contains("DummySink")) {
			// TODO: should we set an isDummy flag?
			log(this.getClass().getCanonicalName() + " skipping dummy");
			return;
		}
		// SIRFitler -> (Input, Output (WorkNode (WorkNodeContent (SIRFilter))))
		OutputNode output = new OutputNode();
		InputNode input = new InputNode();
		WorkNodeContent content;
		if (sirFilter instanceof SIRFileWriter) {
			content = new FileOutputContent((SIRFileWriter)sirFilter);
		} else if (sirFilter instanceof SIRFileReader) {
			content = new FileInputContent((SIRFileReader)sirFilter);
		} else {
			content = new WorkNodeContent(sirFilter);
		}
		Filter slirFilter = new Filter(new WorkNode(content));
		currentSSG.addRoot(slirFilter);
	}	
	
	@Override
	public void preVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		log(this.getClass().getCanonicalName() + " preVisitSplitJoin()");
		// Splitter -> (Input, (Output i.e. dup, rr) (WorkNode (WorkNodeContent (Identity))))
		OutputNode output = new OutputNode();
		InputNode input = new InputNode();
		WorkNodeContent content;

//		CType type = CommonUtils.getOutputType(self);
//		SIRIdentity id = new SIRIdentity(type);
//		RenameAll.renameAllFilters(id);
//		content = new IDFilterContent(id);
//		if (!node.isDuplicateSplitter()) {
//			mult = node.getTotalOutgoingWeights();
//		}
	}

	@Override
	public void postVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		log(this.getClass().getCanonicalName() + " postVisitSplitJoin()");
		// Joiner -> ((Output i.e. dup, rr), Input, (WorkNode (WorkNodeContent (Identity))))		
		OutputNode output = new OutputNode();
		InputNode input = new InputNode();
		WorkNodeContent content;

//		CType type = CommonUtils.getOutputType(node);
//        SIRIdentity id = new SIRIdentity(type);
//        RenameAll.renameAllFilters(id);
//        //content = new FilterContent(id);
//        content = new IDFilterContent(id);
//        mult = node.getTotalIncomingWeights();

	}

	@Override
	public void preVisitPipeline(SIRPipeline self, SIRPipelineIter iter) { /* Do Nothing */ }

	@Override
	public void postVisitPipeline(SIRPipeline self, SIRPipelineIter iter) { /* Do Nothing */ }

	@Override
	public void visitPhasedFilter(SIRPhasedFilter self, SIRPhasedFilterIter iter) {
		 throw new java.lang.RuntimeException("SIRPhasedFilters are no longer supported.");
	}
	
	@Override
	public void preVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) {
		 throw new java.lang.RuntimeException("SIRFeedbackLoop are not supported in graphs with dynamic rates.");
	}

	@Override
	public void postVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) {
		 throw new java.lang.RuntimeException("SIRFeedbackLoop are not supported in graphs with dynamic rates.");
	}


	

}
