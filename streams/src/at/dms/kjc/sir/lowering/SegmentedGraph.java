/**
 * A SegmentedGraph is a structure that contains each of the static subgraphs
 * from a SIRStream with dynamic regions. Each segment has a source and sink.
 * The SegmentedGraph also maintains connectivity and rate information.
 */
package at.dms.kjc.sir.lowering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.dms.kjc.sir.SIRStream;

public class SegmentedGraph {
	
	/** This is the list of all pipelines created */
	private List<SIRStream> staticSubGraphs = null;
	
	/**
	 * Maps source to sinks to indicate connections between static
	 * subgraphs. Divergent operators will have multiple connections.
	 */
	private Map<SIRStream, List<SIRStream>> connections = null;

	/**
	 *  Create a new SegmentedGraph.
	 */
	public SegmentedGraph() {
		staticSubGraphs = new ArrayList<SIRStream>();
		connections = new HashMap<SIRStream, List<SIRStream>>();
	}

   /**
    * Add a new static subsection to the graph. Each subsection 
    * must be a pipeline with a source, some number of other filters, 
    * and a sink.
    * 
    * @param pipeline subsection to add
    * @return void
    */
	public void addPipe(SIRStream pipeline) {
		staticSubGraphs.add(pipeline);
	}

	/**
	 * Returns the connections between static subsections.
	 * @return the connections between static subsections.
	 */
	public Map<SIRStream, List<SIRStream>> getConnections() {
		return connections;
	}

	/**
	 * Returns the list of static subsections
	 * @return the list of static subsections
	 */
	public List<SIRStream> getStaticSubGraphs() {
		return staticSubGraphs;
	}

	public SIRStream getStaticSubGraph0() {
		assert staticSubGraphs.size() == 1 : "SegmentedGraph.getStaticSubGraph0 staticSubGraph should have only one subsection in static case.";
		return staticSubGraphs.get(0);
	}

}
