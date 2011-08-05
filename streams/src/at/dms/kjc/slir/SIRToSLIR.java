/**
 * 
 */
package at.dms.kjc.slir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.dms.kjc.flatgraph.DataFlowTraversal;
import at.dms.kjc.flatgraph.FlatNode;
import at.dms.kjc.flatgraph.GraphFlattener;
import at.dms.kjc.iterator.IterFactory;
import at.dms.kjc.iterator.SIRFeedbackLoopIter;
import at.dms.kjc.iterator.SIRFilterIter;
import at.dms.kjc.iterator.SIRPhasedFilterIter;
import at.dms.kjc.iterator.SIRPipelineIter;
import at.dms.kjc.iterator.SIRSplitJoinIter;
import at.dms.kjc.sir.SIRFeedbackLoop;
import at.dms.kjc.sir.SIRFileReader;
import at.dms.kjc.sir.SIRFileWriter;
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
	 * General Algorithm: for sir : sirs { if (sir.isSource()) return new
	 * EntryFilter() if (sir.isInk()) return new ExitFilter() if
	 * (sir.isSplitter()) return new IdentityFilter() with splitting pattern
	 * encoded in the output node if (sir.isJoiner()) return new
	 * IdentityFilter() with joining pattern encoded in the output node if
	 * (sir.isFilter()) return new slir.Filter() }
	 * 
	 * Note: Take a look at the FlatGraph code to see how the hierarchy is
	 * handled
	 * 
	 * @return
	 */
	public StreamGraph translate(SegmentedGraph segmentedGraph, int numCores) {
		log(this.getClass().getCanonicalName() + " translate()");
		// A StreamGraph is a list of StaticSubGraphs.
		// A StaticSubGraph has an input, output, and a List<Filter>;
		// StreamGraph streamGraph = new StreamGraph();
		StreamGraph streamGraph = new StreamGraph();
		List<SIRStream> subgraphs = segmentedGraph.getStaticSubGraphs();

		LinearAnalyzer lfa = Flattener.lfa;

		for (SIRStream str : subgraphs) {

			@SuppressWarnings("rawtypes")
			HashMap[] executionCounts = SIRScheduler.getExecutionCounts(str);

			// use FlatGraph to eliminate intermediate levels of pipelines
			// when looking at stream.
			GraphFlattener fg = new GraphFlattener(str);
			SIRToSliceNodes sliceNodes = new SIRToSliceNodes();
			sliceNodes.createNodes(fg.top, executionCounts);
			List<Filter> sliceList = new LinkedList<Filter>();
			List<Filter> ioList = new LinkedList<Filter>();
			WorkEstimate work = WorkEstimate.getWorkEstimate(str);
			Map<OutputNode, HashMap<InputNode, InterFilterEdge>> edges = 
						new HashMap<OutputNode, HashMap<InputNode, InterFilterEdge>>();

			HashMap<Filter, WorkNode> bottleNeckFilter = new HashMap<Filter, WorkNode>();
			


			 HashMap<Filter, Long> sliceBNWork = new HashMap<Filter, Long>();	
			 Map<WorkNodeContent, Long> workEstimation = new HashMap<WorkNodeContent, Long>();
			 LinkedList<Filter> topSlices = new LinkedList<Filter>();
			 Filter topSlice = null;
			
			flattenInternal(sliceNodes, fg.top, edges, bottleNeckFilter,
					sliceBNWork, workEstimation, work, topSlice, sliceList,
					ioList, topSlices);
				
			// System.out.println("Slices: " + sliceList.size());
			// sliceGraph = sliceList.toArray(new Slice[sliceList.size()]);

			Filter[] io = ioList.toArray(new Filter[ioList.size()]);
			// for (FilterSliceNode id : sliceNodes.generatedIds) {
			// IDSliceRemoval.doit(id.getParent());
			// }

			HashSet<WorkNode> generatedIds = sliceNodes.generatedIds;

			InputPort inputPort = new UnaryInputPort();
			OutputPort outputPort = new UnaryOutputPort();
			// StaticSubGraph ssg = new StaticSubGraph(inputPort, outputPort,
			// new ArrayList<Filter>(Arrays.asList(topNodes)));
			currentSSG = new StaticSubGraph(streamGraph);
			//IterFactory.createFactory().createIter(str).accept(this);
			currentSSG.setInputPort(inputPort);
			currentSSG.setOutputPort(outputPort);

			for (Filter f : topSlices) {
				currentSSG.addTopSlice(f);
			}
			
			// TODO: REMOVE SOON!
			//Filter[] slices = topSlices.toArray(new Filter[0]);
			//currentSSG.setSliceGraphNewIds(slices);
			streamGraph.addSSG(currentSSG);
		

			// END SECTION OF TO REMOVE

			// streamGraph.addRoot(currentSSG);

			//
			// // Don't use flattenGraph at all
			//
			// // Each SIROperator -> Filter, even joiners and splitters
			// // joiners and splitters will be identity (leave it for sync
			// removal
			// // to get rid of the identities)
			//
			// // flatten the graph by running (super?) synch removal
			// UnflatFilter[] topNodes = null;
			// if (!KjcOptions.nopartition) {
			// FlattenGraph.flattenGraph(str, lfa, executionCounts);
			// topNodes = FlattenGraph.getTopLevelNodes();
			// CommonUtils.println_debugging("Top Nodes:");
			// for (int i = 0; i < topNodes.length; i++) {
			// CommonUtils.println_debugging(topNodes[i].toString());
			// }
			// }
			//
			// Filter[] filterGraph = null;
			//
			// // Conversion pass, SIRFilter to Filter, looks like
			// OneFilterSlicer
			// // but removing some cruft.
			// // There should not be a "slicer"
			//
			// if (KjcOptions.tilera > 1 || KjcOptions.smp > 1) {
			// if (!KjcOptions.nopartition) {
			// System.out.println("Using OneFilterSlicer slicer");
			// slicer = new OneFilterSlicer(topNodes, executionCounts);
			// } else {
			// System.out.println("Using FlattenAndPartition slicer");
			// slicer = new FlattenAndPartition(topNodes, executionCounts,
			// lfa, WorkEstimate.getWorkEstimate(str), numCores);
			// ((FlattenAndPartition) slicer).flatten(str, executionCounts);
			// }
			//
			// } else {
			// if (KjcOptions.nopartition) {
			// slicer = new FlattenAndPartition(topNodes, executionCounts,
			// lfa, WorkEstimate.getWorkEstimate(str), numCores);
			// ((FlattenAndPartition) slicer).flatten(str, executionCounts);
			// } else {
			// slicer = new SimpleSlicer(topNodes, executionCounts, lfa,
			// WorkEstimate.getWorkEstimate(str), numCores);
			// }
			// }
			// assert (slicer != null);
			//
			//
			//
			// filterGraph = slicer.partition();
			// System.out.println("Traces: " + filterGraph.length);
			//
			//
			// // TODO: Set the inputPort, outputPort, etc.
			// // topSlices is pointers to the roots of the forest
			// // filterGraph(i.e. sliceGraph) is all the filters in the graph
			// // Want to get rid of unflat filters
			// // "TopNodes" should be filters...
			// // TODO: Get rid of things to do with Slicing....
			// // Remove FlattenAndPartition, SimpleSlicer, etc. once SIRToSLIR
			// is working
			// // Might need to keep Unflat... see how it works. Prefer to not
			// use it.
			//
			// // Filter -> WorkNodeContent
			// // WorkNodeContent installed in WorkNode (Decide if we need both)
			// // Filter requires InputNode, OutputNode, WorkNode
			// // Examples : Splitter, SIRFilter, Joiner
			//
			// // SIRFitler -> (Input, Output (WorkNode (WorkNodeContent
			// (SIRFilter))))
			// // Splitter -> (Input, (Output i.e. dup, rr) (WorkNode
			// (WorkNodeContent (Identity))))
			//
			// // TODO: The FlattenAndPartition does pretty much what I want to
			// do
			// // SIR to Slice node, is already in there.
			//
			//
			// InputPort inputPort = null;
			// OutputPort outputPort = null;
			// //StaticSubGraph ssg = new StaticSubGraph(inputPort, outputPort,
			// new ArrayList<Filter>(Arrays.asList(topNodes)));
			// StaticSubGraph ssg = new StaticSubGraph();
			// streamGraph.addRoot(ssg);

		} // for (SIRStream str : subgraphs)

		return streamGraph;
	}

	@Override
	public void visitFilter(SIRFilter sirFilter, SIRFilterIter iter) {
		log(this.getClass().getCanonicalName() + " visitFilter()");
		log(this.getClass().getCanonicalName() + " self:" + sirFilter.getName());
		if (sirFilter.getName().contains("DummySouce")
				|| sirFilter.getName().contains("DummySink")) {
			// TODO: should we set an isDummy flag?
			log(this.getClass().getCanonicalName() + " skipping dummy");
			return;
		}
		// SIRFitler -> (Input, Output (WorkNode (WorkNodeContent (SIRFilter))))
		OutputNode output = new OutputNode();
		InputNode input = new InputNode();
		WorkNodeContent content;
		if (sirFilter instanceof SIRFileWriter) {
			content = new FileOutputContent((SIRFileWriter) sirFilter);
		} else if (sirFilter instanceof SIRFileReader) {
			content = new FileInputContent((SIRFileReader) sirFilter);
		} else {
			content = new WorkNodeContent(sirFilter);
		}
		Filter slirFilter = new Filter(new WorkNode(content));
		// currentSSG.addRoot(slirFilter);
	}

	@Override
	public void preVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		log(this.getClass().getCanonicalName() + " preVisitSplitJoin()");
		// Splitter -> (Input, (Output i.e. dup, rr) (WorkNode (WorkNodeContent
		// (Identity))))
		OutputNode output = new OutputNode();
		InputNode input = new InputNode();
		WorkNodeContent content;

		// CType type = CommonUtils.getOutputType(self);
		// SIRIdentity id = new SIRIdentity(type);
		// RenameAll.renameAllFilters(id);
		// content = new IDFilterContent(id);
		// if (!node.isDuplicateSplitter()) {
		// mult = node.getTotalOutgoingWeights();
		// }
	}

	@Override
	public void postVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		log(this.getClass().getCanonicalName() + " postVisitSplitJoin()");
		// Joiner -> ((Output i.e. dup, rr), Input, (WorkNode (WorkNodeContent
		// (Identity))))
		OutputNode output = new OutputNode();
		InputNode input = new InputNode();
		WorkNodeContent content;

		// CType type = CommonUtils.getOutputType(node);
		// SIRIdentity id = new SIRIdentity(type);
		// RenameAll.renameAllFilters(id);
		// //content = new FilterContent(id);
		// content = new IDFilterContent(id);
		// mult = node.getTotalIncomingWeights();

	}

	@SuppressWarnings("unused")
	private void flattenInternal(SIRToSliceNodes sliceNodes, FlatNode top,
			Map<OutputNode, HashMap<InputNode, InterFilterEdge>> edges,
			HashMap<Filter, WorkNode> bottleNeckFilter,
			HashMap<Filter, Long> sliceBNWork,
			Map<WorkNodeContent, Long> workEstimation, WorkEstimate work,
			Filter topSlice, List<Filter> sliceList,
			List<Filter> ioList, LinkedList<Filter> topSlices) {
		
		Iterator<FlatNode> dataFlow = DataFlowTraversal.getTraversal(top)
				.iterator();

		while (dataFlow.hasNext()) {
			FlatNode node = dataFlow.next();
			// System.out.println(node);
			InputNode input = sliceNodes.inputNodes.get(node.contents);
			OutputNode output = sliceNodes.outputNodes.get(node.contents);
			WorkNode filterNode = sliceNodes.filterNodes.get(node.contents);

			assert input != null && output != null && filterNode != null;

			// set up the slice
			Filter slice = new Filter(input);
			input.setNext(filterNode);
			filterNode.setPrevious(input);
			filterNode.setNext(output);
			output.setPrevious(filterNode);
			input.setParent(slice);
			output.setParent(slice);
			filterNode.setParent(slice);

			// System.out.println("  outputs: " + node.ways);
			if (node.ways != 0) {
				assert node.ways == node.getEdges().length
						&& node.ways == node.weights.length;

				// set up the i/o arcs
				// set up the splitting...
				LinkedList<InterFilterEdge> outEdges = new LinkedList<InterFilterEdge>();
				LinkedList<Integer> outWeights = new LinkedList<Integer>();
				HashMap<InputNode, InterFilterEdge> newEdges = new HashMap<InputNode, InterFilterEdge>();
				for (int i = 0; i < node.ways; i++) {
					if (node.weights[i] == 0)
						continue;
					InterFilterEdge edge = new InterFilterEdge(
							output,
							sliceNodes.inputNodes.get(node.getEdges()[i].contents));
					newEdges.put(
							sliceNodes.inputNodes
									.get(node.getEdges()[i].contents),
							edge);
					outEdges.add(edge);
					outWeights.add(node.weights[i]);
				}
				edges.put(output, newEdges);

				LinkedList<LinkedList<InterFilterEdge>> translatedEdges = new LinkedList<LinkedList<InterFilterEdge>>();
				if (node.isDuplicateSplitter()) {
					outWeights = new LinkedList<Integer>();
					outWeights.add(new Integer(1));
					translatedEdges.add(outEdges);
				} else {
					for (int i = 0; i < outEdges.size(); i++) {
						LinkedList<InterFilterEdge> link = new LinkedList<InterFilterEdge>();
						link.add(outEdges.get(i));
						translatedEdges.add(link);
					}
				}

				output.set(outWeights, translatedEdges, SchedulingPhase.STEADY);
			} else {
				// no outputs
				output.setWeights(new int[0]);
				output.setDests(new InterFilterEdge[0][0]);
			}

			if (node.isFilter()) {
				if (node.getFilter().getPushInt() == 0) {
					output.setWeights(new int[0]);
					output.setDests(new InterFilterEdge[0][0]);
				}
			}

			// set up the joining, the edges should exist already from upstream
			// System.out.println("  inputs: " + node.inputs);
			if (node.inputs != 0) {
				assert node.inputs == node.incoming.length
						&& node.inputs == node.incomingWeights.length;

				LinkedList<Integer> inWeights = new LinkedList<Integer>();
				LinkedList<InterFilterEdge> inEdges = new LinkedList<InterFilterEdge>();
				for (int i = 0; i < node.inputs; i++) {
					if (node.incomingWeights[i] == 0)
						continue;
					inEdges.add(edges.get(
							sliceNodes.outputNodes
									.get(node.incoming[i].contents)).get(input));
					inWeights.add(node.incomingWeights[i]);
				}
				input.set(inWeights, inEdges, SchedulingPhase.STEADY);
			} else {
				input.setWeights(new int[0]);
				input.setSources(new InterFilterEdge[0]);
			}

			if (node.isFilter() && node.getFilter().getPopInt() == 0) {
				input.setWeights(new int[0]);
				input.setSources(new InterFilterEdge[0]);
			}

			// set up the work hashmaps
			long workEst = 0;
			if (sliceNodes.generatedIds.contains(filterNode)) {
				workEst = 3 * filterNode.getFilter().getSteadyMult();
			} else {
				assert node.isFilter();
				workEst = work.getWork((SIRFilter) node.contents);
			}
			bottleNeckFilter.put(slice, filterNode);
			sliceBNWork.put(slice, workEst);
			workEstimation.put(filterNode.getFilter(), workEst);

			slice.finish();

			if (node.contents instanceof SIRFileReader
					|| node.contents instanceof SIRFileWriter) {
				// System.out.println("Found io " + node.contents);
				ioList.add(slice);
			}

			if (topSlice == null)
				topSlice = slice;
			sliceList.add(slice);
		}
		//topSlices = new LinkedList<Filter>();
		topSlices.add(topSlice);
		System.out.println(topSlices);
	}

	@Override
	public void preVisitPipeline(SIRPipeline self, SIRPipelineIter iter) { /*
																			 * Do
																			 * Nothing
																			 */
	}

	@Override
	public void postVisitPipeline(SIRPipeline self, SIRPipelineIter iter) { /*
																			 * Do
																			 * Nothing
																			 */
	}

	@Override
	public void visitPhasedFilter(SIRPhasedFilter self, SIRPhasedFilterIter iter) {
		throw new java.lang.RuntimeException(
				"SIRPhasedFilters are no longer supported.");
	}

	@Override
	public void preVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) {
		throw new java.lang.RuntimeException(
				"SIRFeedbackLoop are not supported in graphs with dynamic rates.");
	}

	@Override
	public void postVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) {
		throw new java.lang.RuntimeException(
				"SIRFeedbackLoop are not supported in graphs with dynamic rates.");
	}

}
