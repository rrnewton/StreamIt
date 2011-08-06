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
public class SIRToSLIR {

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

		for (SIRStream str : subgraphs) {

			InputPort inputPort = new UnaryInputPort();
			OutputPort outputPort = new UnaryOutputPort();
			currentSSG = new StaticSubGraph(streamGraph, str, inputPort, outputPort);								
			streamGraph.addSSG(currentSSG);
		
		}

		return streamGraph;
	}


}
