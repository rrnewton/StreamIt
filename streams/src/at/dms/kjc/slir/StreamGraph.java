package at.dms.kjc.slir;

import java.io.FileWriter;
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
		
		//remove synchronization from the graph (remove ids added by the conversion
		//to flatgraph
		SynchRemoval.doit(ssg);
		
		InstallInitDistributions.doit(ssg.getFilterGraph());
		// fix any rate skew introduced in conversion to Slice graph.
		CheckBuffering.doit(ssg, false, numCores);
		// decompose any pipelines of filters in the Slice graph.
		// slicer.ensureSimpleSlices();
		return ssg;
	}

	/**
	 * Print the entire stream graph as a dot file
	 * @param filename the output file name
	 */
	public void dumpGraph(String filename) {
	    StringBuffer buf = new StringBuffer();
        buf.append("digraph Flattend {\n");
        buf.append("size = \"8, 10.5\";\n");        
        for (int i = 0; i < ssgs.size(); i++) {
            buf = dumpSingle(buf, ssgs.get(i));            
            if (i < ssgs.size()-1) {
                StaticSubGraph srcSSG = ssgs.get(i);
                StaticSubGraph dstSSG = ssgs.get(i+1);
                Filter src = srcSSG.getFilterGraph()[srcSSG.getFilterGraph().length-1];
                Filter dst = dstSSG.getFilterGraph()[0];
                assert src != null;
                assert dst != null;
                buf.append(src.hashCode() + " [ " + srcSSG.filterName(src) + "\" ];\n");
                buf.append(src.hashCode() + " -> " + dst.hashCode() + ";\n");                
            }
            
        }        	    
        buf.append("}\n");	    	    	    
	    try {
	        FileWriter fw = new FileWriter(filename);
	        fw.write(buf.toString());
	        fw.close();
	    } catch (Exception e) {
	        System.err.println("Could not print extracted slices");
	    }
	}
	

	private StringBuffer dumpSingle( StringBuffer buf, StaticSubGraph ssg) {
	    Filter[] filterGraph = ssg.getFilterGraph();
	 	    for (int i = 0; i < filterGraph.length; i++) {
	        Filter filter = filterGraph[i];
	        assert filter != null;
	        buf.append(filter.hashCode() + " [ " + ssg.filterName(filter) + "\" ];\n");
	        Filter[] next = ssg.getNext(filter/* ,parent */);
	        for (int j = 0; j < next.length; j++) {
	            assert next[j] != null;
	            buf.append(filter.hashCode() + " -> " + next[j].hashCode()
	                    + ";\n");
	        }
	    }
	    return buf;
	    // write the file

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
