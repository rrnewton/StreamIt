/**
 * 
 */
package at.dms.kjc.slir;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		
		
		Map<SIRStream, StaticSubGraph> sirToSSG = new HashMap<SIRStream, StaticSubGraph>();

		/* Create all the static subgraphs */
		StreamGraph streamGraph = new StreamGraph();
		for (SIRStream sir : ssgs) {
			StaticSubGraph ssg = new StaticSubGraph().init(streamGraph, sir, new UnaryInputPort(), new UnaryOutputPort());
			ssg.getInputPort().setSSG(ssg);
			ssg.getOutputPort().setSSG(ssg);
			streamGraph.addSSG(ssg);
			sirToSSG.put(sir, ssg);
		}
		
		/* Set up all the connections */
		System.out.println("StreamGraph.translate\n\n");
		Map<SIRStream, List<SIRStream>> connections = segmentedGraph.getConnections();		
		for (SIRStream src : connections.keySet()) {
			List<SIRStream> links = connections.get(src);
			for (SIRStream dst : links) {
				System.out.println(src + " --> " + dst);
				StaticSubGraph ssgSrc = sirToSSG.get(src);
				StaticSubGraph ssgDst = sirToSSG.get(dst);
				assert (ssgSrc != null) : "Can't find ssg for src filter=" + src;
				assert (ssgDst != null) : "Can't find ssg for dst filter=" + dst;
				OutputPort outputPort = ssgSrc.getOutputPort();
				InputPort inputPort = ssgDst.getInputPort();
				InterSSGEdge edge = new InterSSGEdge(outputPort, inputPort);
				System.out.println("StreamGraph.translate edge=" + edge);
				inputPort.addLink(edge);
				outputPort.addLink(edge);			
			}
		}
		System.out.println("\n\n");

		
		
		
//
//		SIRStream str = ssgs.get(0);
//		InputPort inputPort = null;
//		OutputPort outputPort = null;
//		
//		StaticSubGraph src = new StaticSubGraph().init(streamGraph, str,
//				inputPort, outputPort);
//
//		streamGraph.addSSG(src);
//
//		for (int i = 1; i < ssgs.size(); i++) {
//			StaticSubGraph dst = new StaticSubGraph();
//			outputPort = new UnaryOutputPort(dst);
//			src.setOutputPort(outputPort);
//			inputPort = new UnaryInputPort(src);
//			str = ssgs.get(i);			
//			dst.init(streamGraph, str, inputPort, null);
//			
//			
//			InterSSGEdge edge = new InterSSGEdge(outputPort, inputPort);
//
//			System.out.println("StreamGraph.translate edge=" + edge);
//			
//			inputPort.addLink(edge);
//			outputPort.addLink(edge);
//			streamGraph.addSSG(dst);
//			src = dst;
//		}

		return streamGraph;
	}

}
