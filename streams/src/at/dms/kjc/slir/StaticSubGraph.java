package at.dms.kjc.slir;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import at.dms.kjc.backendSupport.Layout;
import at.dms.kjc.backendSupport.MultiLevelSplitsJoins;
import at.dms.kjc.sir.SIRFilter;

/**
 * 
 * 
 * @author mgordon and soule
 *
 */
public class StaticSubGraph {


	public Filter[] io;

	private Filter[] filterGraph;

	private InputPort inputPort;

	private OutputPort outputPort;

	private StreamGraph parent;

	private List<Filter> roots;

	/** This hashmap maps a Slice to the FilterSliceNode that
	 * has the most work;
	 */ 
	protected HashMap<Filter, WorkNode> bottleNeckFilter;


	protected HashMap[] exeCounts;

	protected HashMap <SIRFilter, WorkNodeContent> sirToContent;

	// Slice->Long for bottleNeck work estimation
	protected HashMap<Filter, Long> sliceBNWork;

	protected LinkedList<Filter> topSlices;

	//  filtercontent -> work estimation
	protected HashMap<WorkNodeContent, Long> workEstimation;

	// TODO calculate protected WorkEstimate work;

	// TODO calculate private HashMap[] exeCounts; // calculuate when done

	public StaticSubGraph(InputPort inputPort, OutputPort outputPort,
			List<Filter> roots) {
		super();
		this.inputPort = inputPort;
		this.outputPort = outputPort;
		this.roots = roots;
		topSlices = new LinkedList<Filter>();
		sliceBNWork = new HashMap<Filter, Long>();
		bottleNeckFilter = new HashMap<Filter, WorkNode>();
		sirToContent = new HashMap<SIRFilter, WorkNodeContent>();
	}

	public StaticSubGraph(StreamGraph parent) {
		this.parent = parent;
		roots = new ArrayList<Filter>();
		this.roots = roots;
		topSlices = new LinkedList<Filter>();
		sliceBNWork = new HashMap<Filter, Long>();
		bottleNeckFilter = new HashMap<Filter, WorkNode>();
		sirToContent = new HashMap<SIRFilter, WorkNodeContent>();
	}

	/**
	 * Update all the necessary state to add node to slice.
	 * 
	 * @param node The node to add.
	 * @param slice The slice to add the node to.
	 */
	public void addFilterToSlice(WorkNode node, 
			Filter slice) {
		long workEst = workEst(node);

		//add the node to the work estimation
		if (!workEstimation.containsKey(node.getFilter()))
			workEstimation.put(node.getFilter(), workEst);

		if (workEst > sliceBNWork.get(slice).intValue()) {
			sliceBNWork.put(slice, workEst);
			bottleNeckFilter.put(slice, node);
		}
	}

	public void addRoot(Filter filter) {
		roots.add(filter);
	}

	/**
	 * Add the slice to the list of top slices, roots of the forest.
	 */
	public void addTopSlice(Filter slice) {
		topSlices.add(slice);
	}

	/**
	 * Does the the slice graph contain slice (perform a simple linear
	 * search).
	 * 
	 * @param slice The slice to query.
	 * 
	 * @return True if the slice graph contains slice.
	 */
	public boolean containsSlice(Filter slice) {
		Filter[] sliceGraph = getSliceGraph();
		for (int i = 0; i < sliceGraph.length; i++) 
			if (sliceGraph[i] == slice)
				return true;
		return false;
	}

	/**
	 * Force creation of kopi methods and fields for predefined filters.
	 */
	public void createPredefinedContent() {
		for (Filter s : getSliceGraph()) {
			if (s.getWorkNode().getFilter() instanceof PredefinedContent) {
				((PredefinedContent)s.getWorkNode().getFilter()).createContent();
			}
		}

	}


	// dump the the completed partition to a dot file
	public void dumpGraph(String filename) {
		Filter[] sliceGraph = getSliceGraph();
		StringBuffer buf = new StringBuffer();
		buf.append("digraph Flattend {\n");
		buf.append("size = \"8, 10.5\";\n");

		for (int i = 0; i < sliceGraph.length; i++) {
			Filter slice = sliceGraph[i];
			assert slice != null;
			buf.append(slice.hashCode() + " [ " + 
					sliceName(slice) + 
					"\" ];\n");
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

	public void dumpGraph(String filename, Layout layout) {
		dumpGraph(filename, layout, true);
	}

	// dump the the completed partition to a dot file
	public void dumpGraph(String filename, Layout layout, boolean fullInfo) {
		Filter[] sliceGraph = getSliceGraph();
		StringBuffer buf = new StringBuffer();
		buf.append("digraph Flattend {\n");
		buf.append("size = \"8, 10.5\";\n");

		for (int i = 0; i < sliceGraph.length; i++) {
			Filter slice = sliceGraph[i];
			assert slice != null;
			buf.append(slice.hashCode() + " [ " + 
					sliceName(slice, layout, fullInfo) + 
					"\" ];\n");
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



	public Filter[] getFilterGraph() {
		return filterGraph;	
	}

	/**
	 * @param node The Filter 
	 * @return The work estimation for the filter slice node for one steady-state
	 * mult of the filter.
	 */
	public long getFilterWork(WorkNode node) {
		return workEstimation.get(node.getFilter()).longValue();
	}

	/**            
	 * @param node
	 * @return The work estimation for the filter for one steady-state 
	 * multiplied by the steady-state multiplier
	 */
	public long getFilterWorkSteadyMult(WorkNode node)  {
		return getFilterWork(node)  * parent.getSteadyMult();
	} 

	public InputPort getInputPort() {
		return inputPort;
	}

	public OutputPort getOutputPort() {
		return outputPort;
	}

	public List<Filter> getRoots() {
		return roots;
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
	 * @return The work estimation for the slice (the estimation for the filter that does the
	 * most work for one steady-state mult of the filter multipled by the steady state multiplier.
	 */
	public long getSliceBNWork(Filter slice) {
		assert sliceBNWork.containsKey(slice);
		return sliceBNWork.get(slice).longValue() * parent.getSteadyMult();
	}

	/**
	 * Get all slices
	 * @return All the slices of the slice graph. 
	 */
	public Filter[] getSliceGraph() {
		//new slices may have been added so we need to reconstruct the graph each time
		LinkedList<Filter> sliceGraph = 
				DataFlowOrder.getTraversal(topSlices.toArray(new Filter[topSlices.size()]));

		return sliceGraph.toArray(new Filter[sliceGraph.size()]);
	}

	/**
	 *  Get just top level slices in the slice graph.
	 * @return top level slices
	 */
	public Filter[] getTopSlices() {
		assert topSlices != null;
		return topSlices.toArray(new Filter[topSlices.size()]);
	}

	/**
	 * The cost of 1 firing of the filter, to be run after the steady multiplier
	 * has been accounted for in the steady multiplicity of each filter content.
	 * 
	 * @param node
	 * @return 
	 */
	public long getWorkEstOneFiring(WorkNode node) {
		return (getFilterWork(node) / (node.getFilter().getSteadyMult() / parent.getSteadyMult()));
	}

	/**
	 * Check for I/O in slice
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
		for (Filter cur : topSlices) {
			if (cur == slice)
				return true;
		}
		return false;
	}

	/**
	 * remove this slice from the list of top slices, roots of the forest.
	 */
	public void removeTopSlice(Filter slice) {
		assert topSlices.contains(slice);
		topSlices.remove(slice);
	}


	public void setFilterGraph(Filter[] filterGraph) {
		this.filterGraph = filterGraph;	
	}

	public void setInputPort(InputPort inputPort) {
		this.inputPort = inputPort;
	}

	public void setOutputPort(OutputPort outputPort) {
		this.outputPort = outputPort;
	}



	public void setRoots(List<Filter> roots) {
		this.roots = roots;
	}

	/**
	 * Set the slice graph to slices, where the only difference between the 
	 * previous slice graph and the new slice graph is the addition of identity
	 * slices (meaning slices with only an identities filter).
	 *  
	 * @param slices The new slice graph.
	 */
	public void setSliceGraphNewIds(Filter[] slices) {
		//add the new filters to the necessary structures...
		for (int i = 0; i < slices.length; i++) {
			if (!containsSlice(slices[i])) {
				WorkNode filter = slices[i].getWorkNode();
				assert filter.toString().startsWith("Identity");

				if (!workEstimation.containsKey(filter)) {
					//for a work estimation of an identity filter
					//multiple the estimated cost of on item by the number
					//of items that passes through it (determined by the schedule mult).
					workEstimation.put(filter.getFilter(), 
							(long)(MultiLevelSplitsJoins.IDENTITY_WORK *
									filter.getFilter().getSteadyMult()));
				}

				//remember that that the only filter, the id, is the bottleneck..
				if (!sliceBNWork.containsKey(slices[i])) {
					sliceBNWork.put(slices[i], 
							workEstimation.get(filter.getFilter()));;
				}
				if (!bottleNeckFilter.containsKey(slices[i])) {
					bottleNeckFilter.put(slices[i], filter);
				}

			}
		}
		//now set the new slice graph...
		setSliceGraph(slices);
	}


	/**
	 * Set the slice graph to slices.
	 * 
	 * @param slices The slice list to install as the new slice graph.
	 */
	private void setSliceGraph(Filter[] slices) {

		//perform some checks on the slice graph...
		for (int i = 0; i < slices.length; i++) {
			assert sliceBNWork.containsKey(slices[i]) : slices[i];
			//this doesn't get filled till later
			//assert bottleNeckFilter.containsKey(slices[i]) : slices[i];
			assert workEstimation.containsKey(slices[i].getWorkNode().getFilter()) : slices[i].getWorkNode().getFilter();

		}
	}


	/*
	 * work estimate for filter needed in various places. 
	 */
	private int workEst(WorkNode node) {
		return MultiLevelSplitsJoins.IDENTITY_WORK *
				node.getFilter().getSteadyMult();
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
			Edge[][] dests = ((OutputNode) node).getDests(SchedulingPhase.STEADY);
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


	protected long getWorkEstimate(WorkNodeContent fc) {
		assert workEstimation.containsKey(fc);
		return workEstimation.get(fc).longValue();
	}

	//return a string with all of the names of the filterslicenodes
	// and blue if linear
	protected  String sliceName(Filter slice) {
		InternalFilterNode node = slice.getInputNode();

		StringBuffer out = new StringBuffer();

		//do something fancy for linear slices!!!
		if (((WorkNode)node.getNext()).getFilter().getArray() != null)
			out.append("color=cornflowerblue, style=filled, ");

		out.append("label=\"" + node.getAsInput().debugString(true));//toString());

		node = node.getNext();
		while (node != null ) {
			if (node.isFilterSlice()) {
				WorkNodeContent f = node.getAsFilter().getFilter();
				out.append("\\n" + node.toString() + "{"
						+ getWorkEstimate(f)
						+ "}");
				if (f.isTwoStage())
					out.append("\\npre:(peek, pop, push): (" + 
							f.getPreworkPeek() + ", " + f.getPreworkPop() + "," + f.getPreworkPush());
				out.append(")\\n(peek, pop, push: (" + 
						f.getPeekInt() + ", " + f.getPopInt() + ", " + f.getPushInt() + ")");
				out.append("\\nMult: init " + f.getInitMult() + ", steady " + f.getSteadyMult());
				out.append("\\n *** ");
			}
			else {
				out.append("\\n" + node.getAsOutput().debugString(true));
			}
			/*else {
                //out.append("\\n" + node.toString());
            }*/
			node = node.getNext();
		}
		return out.toString();
	}


	//return a string with all of the names of the filterslicenodes
	// and blue if linear
	protected  String sliceName(Filter slice, Layout layout, boolean fullInfo) {
		InternalFilterNode node = slice.getInputNode();

		StringBuffer out = new StringBuffer();

		//do something fancy for linear slices!!!
		if (((WorkNode)node.getNext()).getFilter().getArray() != null)
			out.append("color=cornflowerblue, style=filled, ");

		out.append("label=\"" + slice.hashCode() + "\\n");
		if (fullInfo)
			out.append(node.getAsInput().debugString(true, SchedulingPhase.INIT) + "\\n" +
					node.getAsInput().debugString(true, SchedulingPhase.STEADY));//toString());

		node = node.getNext();
		while (node != null ) {
			if (node.isFilterSlice()) {
				WorkNodeContent f = node.getAsFilter().getFilter();
				out.append("\\n" + node.toString() + "{"
						+ "}");
				if (f.isTwoStage())
					out.append("\\npre:(peek, pop, push): (" + 
							f.getPreworkPeek() + ", " + f.getPreworkPop() + "," + f.getPreworkPush());
				out.append(")\\n(peek, pop, push: (" + 
						f.getPeekInt() + ", " + f.getPopInt() + ", " + f.getPushInt() + ")");
				out.append("\\nMult: init " + f.getInitMult() + ", steady " + f.getSteadyMult());
				if (layout != null) 
					out.append("\\nTile: " + layout.getComputeNode(slice.getWorkNode()).getUniqueId());
				out.append("\\n *** ");
			}
			else {
				if (fullInfo)
					out.append("\\n" + node.getAsOutput().debugString(true, SchedulingPhase.INIT) + "\\n" +
							node.getAsOutput().debugString(true, SchedulingPhase.STEADY));

			}
			/*else {
                //out.append("\\n" + node.toString());
            }*/
			node = node.getNext();
		}
		return out.toString();
	}


}
