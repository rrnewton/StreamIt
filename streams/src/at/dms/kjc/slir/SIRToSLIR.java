/**
 * 
 */
package at.dms.kjc.slir;

import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.lowering.SegmentedSIRGraph;

/**
 * @author soule
 * 
 */
public class SIRToSLIR {

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
	public StreamGraph translate(SegmentedSIRGraph segmentedGraph, int numCores) {
		log(this.getClass().getCanonicalName() + " translate()");

		StreamGraph streamGraph = new StreamGraph();
		InputPort inputPort = null;
		OutputPort outputPort = null;
		OutputPort prevOutputPort = null;
		// TODO: Change this to look up connectivity in the segmentedGraph 
		// This incorrectly assumes a pipeline
		for (SIRStream str : segmentedGraph.getStaticSubGraphs()) {			
			StaticSubGraph ssg = new StaticSubGraph();
			inputPort = new UnaryInputPort(ssg);			
			outputPort = new UnaryOutputPort(ssg);		
			
			if (prevOutputPort != null) {
				Link link = new Link(inputPort, prevOutputPort);				
				inputPort.addLink(link);
				prevOutputPort.addLink(link);
			}


			
			log(this.getClass().getCanonicalName() + " str=" + str.getName());						
			streamGraph.addSSG(new StaticSubGraph().init(streamGraph, str, inputPort, outputPort));		
			prevOutputPort = outputPort;
		}

		return streamGraph;
	}


}
