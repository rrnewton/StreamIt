package at.dms.kjc.slir;

import java.util.ArrayList;
import java.util.List;

import at.dms.kjc.KjcOptions;

/**
 * 
 * @author mgordon
 * 
 */
public class StreamGraph {

	protected int steadyMult;

	// TODO: We need to fix the hierarchy. StreamGraph is being used
	// more like an ssg, but it should represent multiple ssgs.
	List<StaticSubGraph> ssgs;

		
	public StreamGraph() {
		steadyMult = KjcOptions.steadymult;
		ssgs = new ArrayList<StaticSubGraph>();
	}

	public List<StaticSubGraph> getSSGs() {
		return ssgs;
	}
	
	public StaticSubGraph getSSG(int i) {
		return ssgs.get(i);
	}

	public StaticSubGraph getSSG0() {
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
	

	/**
	 * This method performs some standard cleanup on the slice graph. On return,
	 * file readers and file writers are expanded to contain Kopi code to read
	 * and write files. The slice graph will have any rate skew corrected and
	 * will be converted to SimpleSlice's. The FilterInfo class will be usable.
	 * 
	 * Spacetime does not use this code since it allows general slices and
	 * generates its own code for file readers and file writers.
	 */
	public void simplifyFilters(int numCores) {
		for ( StaticSubGraph ssg : ssgs) {
			simplifyStaticSubGraph(ssg, numCores);
		}		
	}
	
	/**
	 * This method performs some standard cleanup on the slice graph. On return,
	 * file readers and file writers are expanded to contain Kopi code to read
	 * and write files. The slice graph will have any rate skew corrected and
	 * will be converted to SimpleSlice's. The FilterInfo class will be usable.
	 * 
	 * Spacetime does not use this code since it allows general slices and
	 * generates its own code for file readers and file writers.
	 */
	public StaticSubGraph simplifyStaticSubGraph(StaticSubGraph ssg, int numCores) {
		// Create code for predefined content: file readers, file writers.
		ssg.createPredefinedContent();
		// guarantee that we are not going to hack properties of filters in the
		// future
		WorkNodeInfo.canUse();
		// now we require that all input and output slice nodes have separate
		// init distribution pattern
		// for splitting and joining in the init stage (could be null or could
		// be equal to steady or could be
		// different)
		/*
		 * if (KjcOptions.nopartition) { for (FilterSliceNode id :
		 * ((FlattenAndPartition)getSlicer()).generatedIds) {
		 * IDSliceRemoval.doit(id.getParent()); } }
		 */
		InstallInitDistributions.doit(ssg.getFilterGraph());
		// fix any rate skew introduced in conversion to Slice graph.
		AddBuffering.doit(ssg, false, numCores);
		// decompose any pipelines of filters in the Slice graph.
		// slicer.ensureSimpleSlices();
		return ssg;
	}



    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.slir.StreamGraph other = new at.dms.kjc.slir.StreamGraph();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.StreamGraph other) {
        other.steadyMult = this.steadyMult;
        other.ssgs = (java.util.List)at.dms.kjc.AutoCloner.cloneToplevel(this.ssgs);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
