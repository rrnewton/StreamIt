/**
 * 
 */
package at.dms.kjc.slir;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		//Map<SIRStream, StaticSubGraph> sirToSSG = new HashMap<SIRStream, StaticSubGraph>();
		Map<Integer, StaticSubGraph> indexToSSG = new HashMap<Integer, StaticSubGraph>();

		/* Create all the static subgraphs */
		StreamGraph streamGraph = new StreamGraph();
		int i = 0;
		for (SIRStream sir : ssgs) {
			StaticSubGraph ssg = new StaticSubGraph().init(streamGraph, sir,
					new UnaryInputPort(), new UnaryOutputPort());
			ssg.getInputPort().setSSG(ssg);
			ssg.getOutputPort().setSSG(ssg);
			streamGraph.addSSG(ssg);
			//sirToSSG.put(sir, ssg);
			indexToSSG.put(i, ssg);
			i++;
		}

		/* Set up all the connections */
//		System.out.println("StreamGraph.translate\n\n");
//		Map<SIRStream, List<SIRStream>> connections = segmentedGraph
//				.getConnections();
//		for (SIRStream src : connections.keySet()) {
//			List<SIRStream> links = connections.get(src);
//			for (SIRStream dst : links) {
//				System.out.println(src + " --> " + dst);
//				StaticSubGraph ssgSrc = sirToSSG.get(src);
//				StaticSubGraph ssgDst = sirToSSG.get(dst);
//				assert (ssgSrc != null) : "Can't find ssg for src filter="
//						+ src;
//				assert (ssgDst != null) : "Can't find ssg for dst filter="
//						+ dst;
//				OutputPort outputPort = ssgSrc.getOutputPort();
//				InputPort inputPort = ssgDst.getInputPort();
//				InterSSGEdge edge = new InterSSGEdge(outputPort, inputPort);
//				System.out.println("StreamGraph.translate edge=" + edge);
//				inputPort.addLink(edge);
//				outputPort.addLink(edge);
//			}
//		}
//		System.out.println("\n\n");

		Map<Integer, List<Integer>> connections = segmentedGraph
				.getConnections();
		for (Integer src : connections.keySet()) {
			List<Integer> links = connections.get(src);
			for (Integer dst : links) {
			    //System.out.println(src + " --> " + dst);
				StaticSubGraph ssgSrc = indexToSSG.get(src);
				StaticSubGraph ssgDst = indexToSSG.get(dst);

				assert (ssgSrc != null) : "Can't find ssg for src filter="
						+ src;
				assert (ssgDst != null) : "Can't find ssg for dst filter="
						+ dst;
				OutputPort outputPort = ssgSrc.getOutputPort();
				InputPort inputPort = ssgDst.getInputPort();
				InterSSGEdge edge = new InterSSGEdge(outputPort, inputPort);
				//System.out.println("StreamGraph.translate edge=" + edge);
				inputPort.addLink(edge);
				outputPort.addLink(edge);
			}
		}
		//System.out.println("\n\n");

		
		return streamGraph;
	}

}
