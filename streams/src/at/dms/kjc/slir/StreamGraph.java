package at.dms.kjc.slir;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import at.dms.kjc.KjcOptions;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.linear.LinearAnalyzer;
import at.dms.kjc.sir.lowering.partition.WorkEstimate;
import at.dms.kjc.backendSupport.*;
import at.dms.kjc.common.CommonUtils;

/**
 * 
 * @author mgordon
 * 
 */
public class StreamGraph {

	protected int steadyMult;

	// sirfilter -> work estimation
	// TODO: DELETE
	protected WorkEstimate work;

	// TODO: We need to fix the hierarchy. StreamGraph is being used
	// more like an ssg, but it should represent multiple ssgs.
	List<StaticSubGraph> ssgs;

		
	public StreamGraph() {
		steadyMult = KjcOptions.steadymult;
		ssgs = new ArrayList<StaticSubGraph>();
	}

	public StaticSubGraph getSSG(int i) {
		return ssgs.get(i);
	}

	public StaticSubGraph getSSG() {
		assert ssgs.size() == 1 : "Calling getSSG() on a graph with dynamic rates, and thus multiple SSGs!";
		return ssgs.get(0);
	}

	public int getSteadyMult() {
		return steadyMult;
	}

	public void setSteadyMult(int steadyMult) {
		this.steadyMult = steadyMult;
	}

	public int getNumSSGs() {
		return ssgs.size();
	}
	
	public void addSSG(StaticSubGraph ssg) {
		 ssgs.add(ssg);
	}
}
