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
	 * Translate the segmented SIR graph into a StreamGraph
	 * @param segmentedGraph
	 * @param numCores
	 * @return
	 */
	public StreamGraph translate(SegmentedSIRGraph segmentedGraph) {

		List<SIRStream> ssgs = segmentedGraph.getStaticSubGraphs();

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
			indexToSSG.put(i, ssg);
			i++;
		}

		Map<Integer, List<Integer>> connections = segmentedGraph
				.getConnections();
		for (Integer src : connections.keySet()) {
			List<Integer> links = connections.get(src);
			for (Integer dst : links) {
				StaticSubGraph ssgSrc = indexToSSG.get(src);
				StaticSubGraph ssgDst = indexToSSG.get(dst);
				assert (ssgSrc != null) : "Can't find ssg for src filter="
						+ src;
				assert (ssgDst != null) : "Can't find ssg for dst filter="
						+ dst;
				OutputPort outputPort = ssgSrc.getOutputPort();
				InputPort inputPort = ssgDst.getInputPort();
				InterSSGEdge edge = new InterSSGEdge(outputPort, inputPort);
				inputPort.addLink(edge);
				outputPort.addLink(edge);
			}
		}				
		return streamGraph;
	}

}
