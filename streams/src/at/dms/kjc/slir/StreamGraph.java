package at.dms.kjc.slir;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.linear.LinearAnalyzer;
import at.dms.kjc.sir.lowering.partition.WorkEstimate;

/**
 * This class represents the overall stream graph of the application.  It includes
 * one or more static subgraphs.
 * 
 * @author mgordon
 *
 */
//public class StreamGraph {
	

// TODO: StreamGraph should NOT extend SIRSlicer. However, I need to 
// move the Slicer functionality into StreamGraph. Therefore, as a temporary
// fix, I am adding the inheritance. This will change as I remove the Slicer.
public class StreamGraph extends SIRSlicer {

	List<StaticSubGraph> roots;

	/**
	 * Construct a new stream graph with no static subgraphs.
	 */
//	public StreamGraph() {
//		roots = new LinkedList<StaticSubGraph>();
//	}
//	
	
    public StreamGraph(UnflatFilter[] topFilters, HashMap[] exeCounts,
            LinearAnalyzer lfa, WorkEstimate work, int maxPartitions) {
        super(topFilters, exeCounts, lfa, work, maxPartitions);
        workEstimation = new HashMap<WorkNodeContent, Long>();
    
        roots = new LinkedList<StaticSubGraph>();	
    }
	
	/**
	 * Add a static subgraph to this stream graph as a root
	 * 
	 * @param g The static subgraph to add
	 */
	public void addRoot(StaticSubGraph g) {
		roots.add(g);
	}
	
	/**
	 * Return the ith root.
	 * 
	 * @param i The index to retrieve 
	 * @return The static subgraph at index i
	 */
	public StaticSubGraph getRoot(int i) {
		return roots.get(i);
	}

	@Override
	public Filter[] partition() {
		// TODO Auto-generated method stub
		return null;
	}

	public void flatten(SIRStream str, HashMap[] executionCounts) {
		// TODO Auto-generated method stub
		
	}
}
