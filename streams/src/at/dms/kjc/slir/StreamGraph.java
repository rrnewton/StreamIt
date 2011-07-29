package at.dms.kjc.slir;

import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the overall stream graph of the application.  It includes
 * one or more static subgraphs.
 * 
 * @author mgordon
 *
 */
public class StreamGraph {
	List<StaticSubGraph> roots;

	/**
	 * Construct a new stream graph with no static subgraphs.
	 */
	public StreamGraph() {
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
}
