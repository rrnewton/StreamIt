package at.dms.kjc.slir;

import at.dms.kjc.CType;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JMethodDeclaration;


public class Filter {

	/* Input distribution Pattern Fields */
		
	/** The incoming round robin weights for this filter for the steady and for init 
     * if the initWeights are null. */
    protected int[] inputWeights;
    /** if this filter requires a different joiner pattern for init, this will encode the weights */
    protected int[] initInputWeights;
    /** The sources that correspond to the weights for the steady and for init if initWeights/initSources
     * are null
     */
    protected Channel[] sources;
    /** if this filter requires a different joiner patter for init, this will encode the sources */
    protected Channel[] initSources;
    
    /* Output distribution pattern fields */
    
    /** the (round-robin) weight for each edge used for the steady and for init if this
     * node does not have a separate init pattern.
     */
    protected int[] outputWeights;
    /** Ordered array of sets of channels
     * The order in the outer array corresponds to the order of weights.
     * The inner array is just a set: the elements correspond to 
     * elements of duplicate splitters fused with the top-level
     * round-robin splitter (by synch removal).
     * A round-robin splitter of size n would be an Edge[n][1]
     * A duplicate splitter of size n would be an Edge[1][n]
     */
    protected Channel[][] outputDests;
    /** the weights for init if this node requires a different init splitting pattern */
    protected int[] initOutputWeights;
    /** the dest array for init if this node requires a different init splitting pattern */
    protected Channel[][] initOutputDests; 

    /* computation fields */
    /** Static unique id used in new name if one FilterContent created from another. */
    protected static int unique_ID = 0; 
    /** The unique id given to this FilterContent for use in constructing names */
    protected int my_unique_ID;
    /** Filter name */
    protected String name; 
    /** PreWork and Work method declarations */
    protected JMethodDeclaration[] prework, steady; 
    /** Input and output types */
    protected CType inputType,outputType; 
    /** Multiplicities from scheduler */
    protected int initMult, steadyMult; 
    /** Other method declarations */
    protected JMethodDeclaration[] methods;
    /** Init function for filter */
    protected JMethodDeclaration initFunction; 
    /** Is true when two-stage filter */
    protected boolean is2stage; 
    /** Field declarations */  
    protected JFieldDeclaration[] fields; 
    /** For linear filters, the pop count **/
    protected int popCount;
    /** For linear filters, the peek count **/
    protected int peek;
    
    

    
}
