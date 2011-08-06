package at.dms.kjc.slir;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.dms.kjc.backendSupport.Layout;
import at.dms.kjc.backendSupport.MultiLevelSplitsJoins;
import at.dms.kjc.flatgraph.DataFlowTraversal;
import at.dms.kjc.flatgraph.FlatNode;
import at.dms.kjc.flatgraph.GraphFlattener;
import at.dms.kjc.sir.SIRFileReader;
import at.dms.kjc.sir.SIRFileWriter;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.lowering.SIRScheduler;
import at.dms.kjc.sir.lowering.partition.WorkEstimate;

/**
 * 
 * 
 * @author mgordon and soule
 * 
 */
public class StaticSubGraph {

	/** Maps a Filter to the WorkNode that has the most work */
	protected HashMap<Filter, WorkNode> bottleNeckFilter;

	/**  */
	private Map<OutputNode, HashMap<InputNode, InterFilterEdge>> edges;

	/**  */
	private HashSet<WorkNode> generatedIds;

	/**  */
	private InputPort inputPort;

	/**  */
	public Filter[] io;

	/**  */
	private List<Filter> ioList = null;

	/**  */
	private OutputPort outputPort;

	/**  */
	private StreamGraph parent;

	/**  */
	protected HashMap<SIRFilter, WorkNodeContent> sirToContent;

	/** Filter->Long for bottleNeck work estimation */
	protected HashMap<Filter, Long> sliceBNWork;

	/**  */
	private List<Filter> sliceList = null;

	/**  */
	private Filter topFilter = null;

	/**  */
	protected LinkedList<Filter> topFilters;

	/**  a work estimate for a Filter */
	private WorkEstimate work = null;

	/** filter content -> work estimation */
	protected HashMap<WorkNodeContent, Long> workEstimation;

	/**
	 * Create a StaticSubGraph.
	 * 
	 * @param parent The StreamGraph that contains this subgraph
	 * @param str The SIR graph used to generate this subgraph
	 * @param inputPort The input port 
	 * @param outputPort The output port
	 */

	public StaticSubGraph(StreamGraph parent, SIRStream str,
			InputPort inputPort, OutputPort outputPort) {
		this.parent = parent;
		this.inputPort = inputPort;
		this.outputPort = outputPort;
		sliceList = new LinkedList<Filter>();
		ioList = new LinkedList<Filter>();
		work = WorkEstimate.getWorkEstimate(str);
		edges = new HashMap<OutputNode, HashMap<InputNode, InterFilterEdge>>();
		bottleNeckFilter = new HashMap<Filter, WorkNode>();
		sliceBNWork = new HashMap<Filter, Long>();
		workEstimation = new HashMap<WorkNodeContent, Long>();
		topFilters = new LinkedList<Filter>();
		sirToContent = new HashMap<SIRFilter, WorkNodeContent>();
		topFilter = null;

		@SuppressWarnings("rawtypes")
		HashMap[] executionCounts = SIRScheduler.getExecutionCounts(str);

		// use FlatGraph to eliminate intermediate levels of pipelines
		// when looking at stream.
		GraphFlattener fg = new GraphFlattener(str);
		SIRToSliceNodes sliceNodes = new SIRToSliceNodes();
		sliceNodes.createNodes(fg.top, executionCounts);

		init(sliceNodes, fg.top, edges, bottleNeckFilter, sliceBNWork,
				workEstimation, work, topFilter, sliceList, ioList, topFilters);

		io = ioList.toArray(new Filter[ioList.size()]);

		for (WorkNode id : sliceNodes.generatedIds) {
			IDFilterRemoval.doit(id.getParent());
		 }

		this.setGeneratedIds(sliceNodes.generatedIds);

	}

	/**
	 * Update all the necessary state to add node to slice.
	 * 
	 * @param node
	 *            The node to add.
	 * @param slice
	 *            The slice to add the node to.
	 */
	public void addFilterToSlice(WorkNode node, Filter slice) {
		long workEst = workEst(node);

		// add the node to the work estimation
		if (!workEstimation.containsKey(node.getFilter()))
			workEstimation.put(node.getFilter(), workEst);

		if (workEst > sliceBNWork.get(slice).intValue()) {
			sliceBNWork.put(slice, workEst);
			bottleNeckFilter.put(slice, node);
		}
	}

	/**
	 * Add the slice to the list of top slices, roots of the forest.
	 */
	public void addTopSlice(Filter slice) {
		topFilters.add(slice);
	}

	/**
	 * Does the the slice graph contain slice (perform a simple linear search).
	 * 
	 * @param slice
	 *            The slice to query.
	 * 
	 * @return True if the slice graph contains slice.
	 */
	public boolean containsSlice(Filter slice) {
		Filter[] sliceGraph = getFilterGraph();
		for (int i = 0; i < sliceGraph.length; i++)
			if (sliceGraph[i] == slice)
				return true;
		return false;
	}

	/**
	 * Force creation of kopi methods and fields for predefined filters.
	 */
	public void createPredefinedContent() {
		for (Filter s : getFilterGraph()) {
			if (s.getWorkNode().getFilter() instanceof PredefinedContent) {
				((PredefinedContent) s.getWorkNode().getFilter())
						.createContent();
			}
		}

	}

	/**
	 * Dump the the completed partition to a dot file
	 */
	public void dumpGraph(String filename) {
		Filter[] sliceGraph = getFilterGraph();
		StringBuffer buf = new StringBuffer();
		buf.append("digraph Flattend {\n");
		buf.append("size = \"8, 10.5\";\n");

		for (int i = 0; i < sliceGraph.length; i++) {
			Filter slice = sliceGraph[i];
			assert slice != null;
			buf.append(slice.hashCode() + " [ " + sliceName(slice) + "\" ];\n");
			Filter[] next = getNext(slice/* ,parent */);
			for (int j = 0; j < next.length; j++) {
				assert next[j] != null;
				buf.append(slice.hashCode() + " -> " + next[j].hashCode()
						+ ";\n");
			}
		}

		buf.append("}\n");
		// write the file
		try {
			FileWriter fw = new FileWriter(filename);
			fw.write(buf.toString());
			fw.close();
		} catch (Exception e) {
			System.err.println("Could not print extracted slices");
		}
	}

	public void dumpGraph(String filename,
			@SuppressWarnings("rawtypes") Layout layout) {
		dumpGraph(filename, layout, true);
	}

	// dump the the completed partition to a dot file
	@SuppressWarnings("rawtypes")
	public void dumpGraph(String filename, Layout layout, boolean fullInfo) {
		Filter[] sliceGraph = getFilterGraph();
		StringBuffer buf = new StringBuffer();
		buf.append("digraph Flattend {\n");
		buf.append("size = \"8, 10.5\";\n");

		for (int i = 0; i < sliceGraph.length; i++) {
			Filter slice = sliceGraph[i];
			assert slice != null;
			buf.append(slice.hashCode() + " [ "
					+ sliceName(slice, layout, fullInfo) + "\" ];\n");
			Filter[] next = getNext(slice/* ,parent */, SchedulingPhase.STEADY);
			for (int j = 0; j < next.length; j++) {
				assert next[j] != null;
				buf.append(slice.hashCode() + " -> " + next[j].hashCode()
						+ ";\n");
			}
			next = getNext(slice, SchedulingPhase.INIT);
			for (int j = 0; j < next.length; j++) {
				assert next[j] != null;
				buf.append(slice.hashCode() + " -> " + next[j].hashCode()
						+ "[style=dashed,color=red];\n");
			}
		}

		buf.append("}\n");
		// write the file
		try {
			FileWriter fw = new FileWriter(filename);
			fw.write(buf.toString());
			fw.close();
		} catch (Exception e) {
			System.err.println("Could not print extracted slices");
		}
	}

	public WorkNodeContent getContent(SIRFilter f) {
		return sirToContent.get(f);
	}

	/**
	 * Get all slices
	 * 
	 * @return All the slices of the slice graph.
	 */
	public Filter[] getFilterGraph() {
		// new slices may have been added so we need to reconstruct the graph
		// each time
		LinkedList<Filter> filterGraph = DataFlowOrder.getTraversal(topFilters
				.toArray(new Filter[topFilters.size()]));

		return filterGraph.toArray(new Filter[filterGraph.size()]);
	}

	/**
	 * @param node
	 *            The Filter
	 * @return The work estimation for the filter slice node for one
	 *         steady-state mult of the filter.
	 */
	public long getFilterWork(WorkNode node) {
		return workEstimation.get(node.getFilter()).longValue();
	}

	/**
	 * @param node
	 * @return The work estimation for the filter for one steady-state
	 *         multiplied by the steady-state multiplier
	 */
	public long getFilterWorkSteadyMult(WorkNode node) {
		return getFilterWork(node) * parent.getSteadyMult();
	}

	public HashSet<WorkNode> getGeneratedIds() {
		return generatedIds;
	}

	/**
	 * @return The InputPort for the StaticSubGraph
	 */
	public InputPort getInputPort() {
		return inputPort;
	}

	// get the downstream slices we cannot use the edge[] of slice
	// because it is for execution order and this is not determined yet.
	protected Filter[] getNext(Filter slice) {
		InternalFilterNode node = slice.getInputNode();
		if (node instanceof InputNode)
			node = node.getNext();
		while (node != null && node instanceof WorkNode) {
			node = node.getNext();
		}
		if (node instanceof OutputNode) {
			Edge[][] dests = ((OutputNode) node)
					.getDests(SchedulingPhase.STEADY);
			ArrayList<Object> output = new ArrayList<Object>();
			for (int i = 0; i < dests.length; i++) {
				Edge[] inner = dests[i];
				for (int j = 0; j < inner.length; j++) {
					// Object next=parent.get(inner[j]);
					Object next = inner[j].getDest().getParent();
					if (!output.contains(next))
						output.add(next);
				}
			}
			Filter[] out = new Filter[output.size()];
			output.toArray(out);
			return out;
		}
		return new Filter[0];
	}

	// get the downstream slices we cannot use the edge[] of slice
	// because it is for execution order and this is not determined yet.
	protected Filter[] getNext(Filter slice, SchedulingPhase phase) {
		InternalFilterNode node = slice.getInputNode();
		if (node instanceof InputNode)
			node = node.getNext();
		while (node != null && node instanceof WorkNode) {
			node = node.getNext();
		}
		if (node instanceof OutputNode) {
			Edge[][] dests = ((OutputNode) node).getDests(phase);
			ArrayList<Object> output = new ArrayList<Object>();
			for (int i = 0; i < dests.length; i++) {
				Edge[] inner = dests[i];
				for (int j = 0; j < inner.length; j++) {
					// Object next=parent.get(inner[j]);
					Object next = inner[j].getDest().getParent();
					if (!output.contains(next))
						output.add(next);
				}
			}
			Filter[] out = new Filter[output.size()];
			output.toArray(out);
			return out;
		}
		return new Filter[0];
	}

	/**
	 * @return The OutputPort for the StaticSubGraph
	 */
	public OutputPort getOutputPort() {
		return outputPort;
	}

	/**
	 * @param slice
	 * @return Return the filter of slice that does the most work.
	 */
	public WorkNode getSliceBNFilter(Filter slice) {
		assert bottleNeckFilter.containsKey(slice);
		return bottleNeckFilter.get(slice);
	}

	/**
	 * @param slice
	 * @return The work estimation for the slice (the estimation for the filter
	 *         that does the most work for one steady-state mult of the filter
	 *         multipled by the steady state multiplier.
	 */
	public long getSliceBNWork(Filter slice) {
		assert sliceBNWork.containsKey(slice);
		return sliceBNWork.get(slice).longValue() * parent.getSteadyMult();
	}

	/**
	 * Get just top level slices in the slice graph.
	 * 
	 * @return top level slices
	 */
	public Filter[] getTopSlices() {
		assert topFilters != null;
		return topFilters.toArray(new Filter[topFilters.size()]);
	}

	protected long getWorkEstimate(WorkNodeContent fc) {
		assert workEstimation.containsKey(fc);
		return workEstimation.get(fc).longValue();
	}

	/**
	 * The cost of 1 firing of the filter, to be run after the steady multiplier
	 * has been accounted for in the steady multiplicity of each filter content.
	 * 
	 * @param node
	 * @return
	 */
	public long getWorkEstOneFiring(WorkNode node) {
		return (getFilterWork(node) / (node.getFilter().getSteadyMult() / parent
				.getSteadyMult()));
	}

	/**
	 * This used to be the flattenInternal in FlattenAndPartition.
	 * 
	 * @param sliceNodes
	 * @param top
	 * @param edges
	 * @param bottleNeckFilter
	 * @param sliceBNWork
	 * @param workEstimation
	 * @param work
	 * @param topSlice
	 * @param sliceList
	 * @param ioList
	 * @param topSlices
	 */
	private void init(SIRToSliceNodes sliceNodes, FlatNode top,
			Map<OutputNode, HashMap<InputNode, InterFilterEdge>> edges,
			HashMap<Filter, WorkNode> bottleNeckFilter,
			HashMap<Filter, Long> sliceBNWork,
			Map<WorkNodeContent, Long> workEstimation, WorkEstimate work,
			Filter topSlice, List<Filter> sliceList, List<Filter> ioList,
			LinkedList<Filter> topSlices) {

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
		// topSlices = new LinkedList<Filter>();
		topSlices.add(topSlice);
		System.out.println(topSlices);
	}

	/**
	 * Check for I/O in slice
	 * 
	 * @param slice
	 * @return Return true if this slice is an IO slice (file reader/writer).
	 */
	public boolean isIO(Filter slice) {
		for (int i = 0; i < io.length; i++) {
			if (slice == io[i])
				return true;
		}
		return false;
	}

	/**
	 * Return true if the slice is a top (source) slice in the forrest
	 */
	public boolean isTopSlice(Filter slice) {
		for (Filter cur : topFilters) {
			if (cur == slice)
				return true;
		}
		return false;
	}

	/**
	 * remove this slice from the list of top slices, roots of the forest.
	 */
	public void removeTopSlice(Filter slice) {
		assert topFilters.contains(slice);
		topFilters.remove(slice);
	}

	public void setGeneratedIds(HashSet<WorkNode> generatedIds) {
		this.generatedIds = generatedIds;
	}

	public void setInputPort(InputPort inputPort) {
		this.inputPort = inputPort;
	}

	public void setOutputPort(OutputPort outputPort) {
		this.outputPort = outputPort;
	}

	/**
	 * Set the slice graph to slices.
	 * 
	 * @param slices
	 *            The slice list to install as the new slice graph.
	 */
	private void setSliceGraph(Filter[] slices) {

		// perform some checks on the slice graph...
		for (int i = 0; i < slices.length; i++) {
			assert sliceBNWork.containsKey(slices[i]) : slices[i];
			// this doesn't get filled till later
			// assert bottleNeckFilter.containsKey(slices[i]) : slices[i];
			assert workEstimation.containsKey(slices[i].getWorkNode()
					.getFilter()) : slices[i].getWorkNode().getFilter();

		}
	}

	/**
	 * Set the slice graph to slices, where the only difference between the
	 * previous slice graph and the new slice graph is the addition of identity
	 * slices (meaning slices with only an identities filter).
	 * 
	 * @param slices
	 *            The new slice graph.
	 */
	public void setSliceGraphNewIds(Filter[] slices) {
		// add the new filters to the necessary structures...
		for (int i = 0; i < slices.length; i++) {
			if (!containsSlice(slices[i])) {
				WorkNode filter = slices[i].getWorkNode();
				assert filter.toString().startsWith("Identity");

				if (!workEstimation.containsKey(filter)) {
					// for a work estimation of an identity filter
					// multiple the estimated cost of on item by the number
					// of items that passes through it (determined by the
					// schedule mult).
					workEstimation
							.put(filter.getFilter(),
									(long) (MultiLevelSplitsJoins.IDENTITY_WORK * filter
											.getFilter().getSteadyMult()));
				}

				// remember that that the only filter, the id, is the
				// bottleneck..
				if (!sliceBNWork.containsKey(slices[i])) {
					sliceBNWork.put(slices[i],
							workEstimation.get(filter.getFilter()));
					;
				}
				if (!bottleNeckFilter.containsKey(slices[i])) {
					bottleNeckFilter.put(slices[i], filter);
				}

			}
		}
		// now set the new slice graph...
		setSliceGraph(slices);
	}

	// return a string with all of the names of the filterslicenodes
	// and blue if linear
	protected String sliceName(Filter slice) {
		InternalFilterNode node = slice.getInputNode();

		StringBuffer out = new StringBuffer();

		// do something fancy for linear slices!!!
		if (((WorkNode) node.getNext()).getFilter().getArray() != null)
			out.append("color=cornflowerblue, style=filled, ");

		out.append("label=\"" + node.getAsInput().debugString(true));// toString());

		node = node.getNext();
		while (node != null) {
			if (node.isFilterSlice()) {
				WorkNodeContent f = node.getAsFilter().getFilter();
				out.append("\\n" + node.toString() + "{" + getWorkEstimate(f)
						+ "}");
				if (f.isTwoStage())
					out.append("\\npre:(peek, pop, push): ("
							+ f.getPreworkPeek() + ", " + f.getPreworkPop()
							+ "," + f.getPreworkPush());
				out.append(")\\n(peek, pop, push: (" + f.getPeekInt() + ", "
						+ f.getPopInt() + ", " + f.getPushInt() + ")");
				out.append("\\nMult: init " + f.getInitMult() + ", steady "
						+ f.getSteadyMult());
				out.append("\\n *** ");
			} else {
				out.append("\\n" + node.getAsOutput().debugString(true));
			}
			/*
			 * else { //out.append("\\n" + node.toString()); }
			 */
			node = node.getNext();
		}
		return out.toString();
	}

	// return a string with all of the names of the filterslicenodes
	// and blue if linear
	@SuppressWarnings("rawtypes")
	protected String sliceName(Filter slice, Layout layout, boolean fullInfo) {
		InternalFilterNode node = slice.getInputNode();

		StringBuffer out = new StringBuffer();

		// do something fancy for linear slices!!!
		if (((WorkNode) node.getNext()).getFilter().getArray() != null)
			out.append("color=cornflowerblue, style=filled, ");

		out.append("label=\"" + slice.hashCode() + "\\n");
		if (fullInfo)
			out.append(node.getAsInput()
					.debugString(true, SchedulingPhase.INIT)
					+ "\\n"
					+ node.getAsInput().debugString(true,
							SchedulingPhase.STEADY));// toString());

		node = node.getNext();
		while (node != null) {
			if (node.isFilterSlice()) {
				WorkNodeContent f = node.getAsFilter().getFilter();
				out.append("\\n" + node.toString() + "{" + "}");
				if (f.isTwoStage())
					out.append("\\npre:(peek, pop, push): ("
							+ f.getPreworkPeek() + ", " + f.getPreworkPop()
							+ "," + f.getPreworkPush());
				out.append(")\\n(peek, pop, push: (" + f.getPeekInt() + ", "
						+ f.getPopInt() + ", " + f.getPushInt() + ")");
				out.append("\\nMult: init " + f.getInitMult() + ", steady "
						+ f.getSteadyMult());
				if (layout != null)
					out.append("\\nTile: "
							+ layout.getComputeNode(slice.getWorkNode())
									.getUniqueId());
				out.append("\\n *** ");
			} else {
				if (fullInfo)
					out.append("\\n"
							+ node.getAsOutput().debugString(true,
									SchedulingPhase.INIT)
							+ "\\n"
							+ node.getAsOutput().debugString(true,
									SchedulingPhase.STEADY));

			}
			/*
			 * else { //out.append("\\n" + node.toString()); }
			 */
			node = node.getNext();
		}
		return out.toString();
	}

	/*
	 * work estimate for filter needed in various places.
	 */
	private int workEst(WorkNode node) {
		return MultiLevelSplitsJoins.IDENTITY_WORK
				* node.getFilter().getSteadyMult();
	}

}
