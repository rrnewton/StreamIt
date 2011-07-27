package at.dms.kjc.sir.lowering;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.dms.kjc.sir.SIRStream;

public class SegmentedGraph {
	/* This is the list of all pipelines created */
	private List<SIRStream> staticSubGraphs = null;
	
	/*
	 * Maps source to sinks to indicate connections between static
	 * subgraphs. Divergent operators will have multiple connections.
	 */
	private Map<SIRStream, List<SIRStream>> connections = null;

	public SegmentedGraph(List<SIRStream> staticSubGraphs,
			Map<SIRStream, List<SIRStream>> connections) {
		this.staticSubGraphs = staticSubGraphs;
		this.connections = connections;
	}

	public SegmentedGraph(ArrayList<SIRStream> staticSubGraphs2,
			Map<SIRStream, List<SIRStream>> connections2) {
		// TODO Auto-generated constructor stub
	}

	public void addPipe(SIRStream pipeline) {
		staticSubGraphs.add(pipeline);
	}

	public Map<SIRStream, List<SIRStream>> getConnections() {
		return connections;
	}

	public List<SIRStream> getStaticSubGraphs() {
		return staticSubGraphs;
	}

	public void setConnections(Map<SIRStream, List<SIRStream>> connections) {
		this.connections = connections;
	}

	public void setStaticSubGraphs(List<SIRStream> staticSubGraphs) {
		this.staticSubGraphs = staticSubGraphs;
	}
}
