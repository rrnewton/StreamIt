/**
 * 
 */
package at.dms.kjc.slir;

import java.util.List;

import at.dms.kjc.sir.SIRDummySink;
import at.dms.kjc.sir.SIRDummySource;
import at.dms.kjc.sir.SIROperator;
import at.dms.kjc.sir.SIRPipeline;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.lowering.SegmentedSIRGraph;

/**
 * @author soule
 * 
 */
public class SIRToSLIR {

	public SIRToSLIR() {
		/* do nothing */
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
	public StreamGraph translate(SegmentedSIRGraph segmentedGraph, int numCores) {

		List<SIRStream> ssgs = segmentedGraph.getStaticSubGraphs();

		/* StreamGraph contains all of the StaticSubGraphs */
		StreamGraph streamGraph = new StreamGraph();

		SIRStream str = ssgs.get(0);
		InputPort inputPort = null;
		OutputPort outputPort = null;
		StaticSubGraph src = new StaticSubGraph().init(streamGraph, str,
				inputPort, outputPort);
		streamGraph.addSSG(src);

		for (int i = 1; i < ssgs.size(); i++) {
			StaticSubGraph dst = new StaticSubGraph();
			outputPort = new UnaryOutputPort(dst);
			src.setOutputPort(outputPort);
			inputPort = new UnaryInputPort(src);
			str = ssgs.get(i);
			dst.init(streamGraph, str, inputPort, null);
			InterSSGEdge link = new InterSSGEdge(outputPort, inputPort);
			inputPort.addLink(link);
			outputPort.addLink(link);
			streamGraph.addSSG(dst);
			src = dst;
		}

		return streamGraph;
	}

}
