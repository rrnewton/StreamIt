package at.dms.kjc.sir;

import java.util.Map;

import at.dms.kjc.CType;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.lir.LIRStreamType;
import at.dms.util.Utils;

/**
 * A StreamIt phased filter.  Like SIRFilter, this has constant
 * overall I/O rates; however, the filter is divided into a set of
 * phases, which execute in some statically determined order.  Thus,
 * where the execution model for a normal filter is "wait until the
 * peek rate for the entire filter can be satisfied", or a phased
 * filter we only wait until the current phase can execute.
 *
 * As of Aug 2006, phases have been removed from the StreamIt
 * language, so the full generality of this class is not utilized.
 */
public class SIRPhasedFilter extends SIRStream implements Cloneable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3398549610544132067L;

	/**
     * The input and output types.  Each type is void if and only if this
     * is a source or sink, respectively.  This means that *all* phases
     * must have a 0 (peek and pop) or (push) rate.
     */
    private CType inputType, outputType;

    /**
     * Indicates whether the filter is stateful or not.  Initially set
     * according to user definition.
     */
    private boolean stateful;

    /**
     * Indicates whether the filter uses an iteration count.
     */
    private boolean isIterationFilter;
    
    /**
     * Indicates whether the filter is a product of fission.
     */
    private boolean isFissed;
    
    /**
     * Array of phases run by the filter.
     */
    private JMethodDeclaration[] initPhases, phases;

    /**
     * Indicates whether the filter has a print statement or not.
     */
	protected boolean hasIO;

    public SIRPhasedFilter() 
    {
        this(null);
    }

    public SIRPhasedFilter(String ident)
    {
        this(null, ident, 
             JFieldDeclaration.EMPTY(), 
             JMethodDeclaration.EMPTY(),
             JMethodDeclaration.EMPTY(),
             JMethodDeclaration.EMPTY(),
             null, null);
    }
    
    public SIRPhasedFilter(SIRContainer parent,
                           String ident,
                           JFieldDeclaration[] fields,
                           JMethodDeclaration[] methods,
                           JMethodDeclaration[] initPhases,
                           JMethodDeclaration[] phases,
                           CType inputType,
                           CType outputType)
    {
        super(parent, ident, fields, methods);
        this.initPhases = initPhases;
        this.phases = phases;
        this.inputType = inputType;
        this.outputType = outputType;
        this.hasIO = false;
    }

    /**
     * Returns the type of this stream.
     */
    @Override
	public LIRStreamType getStreamType() 
    {
        // Might want to create a new type.
        return LIRStreamType.LIR_FILTER;
    }

    /**
     * Copies the state of filter other into this.  Fields that are
     * objects will be shared instead of cloned.
     */
    public void copyState(SIRPhasedFilter other)
    {
        this.work = other.work;
        this.init = other.init;
        this.inputType = other.inputType;
        this.outputType = other.outputType;
        this.setParent(other.getParent());
        this.fields = other.fields;
        this.methods = other.methods;
        this.initPhases = other.initPhases;
        this.phases = other.phases;
        this.ident = other.ident;
        this.stateful = other.stateful;
        this.isFissed = other.isFissed;
        this.isIterationFilter = other.isIterationFilter;
        this.hasIO = other.hasIO;
    }
    
    /**
     * Accepts attribute visitor v at this node.
     */
    @Override
	public Object accept(AttributeStreamVisitor v)
    {
        return v.visitPhasedFilter(this,
                                   fields,
                                   methods,
                                   init,
                                   work,
                                   initPhases,
                                   phases,
                                   inputType,
                                   outputType);
    }
    
    public void setInputType(CType t){
        this.inputType = t;
    }
    @Override
	public CType getInputType(){
        return inputType;
    }

    public void setOutputType(CType t) {
        this.outputType = t;
    }
    @Override
	public CType getOutputType() {
        return this.outputType;
    }

    public boolean isStateful() {
        return stateful;
    }
    
    public void setStateful(boolean stateful) {    
        this.stateful = stateful;
    }
    
    /**
     * returns true is filter has a print statement.
     */
    public boolean hasIO() {
    	return hasIO;
    }

    public boolean isIterationFilter() {
        return isIterationFilter;
    }
    public void setIterationFilter(boolean isIterationFilter) {
        this.isIterationFilter = isIterationFilter;
    }

    public boolean isFissed() {
        return isFissed;
    }
    public void setFissed(boolean isFissed) {
        this.isFissed = isFissed;
    }

    /**
     * Set the filters flag for determining if it has a print statement.
     * @param hasIO
     */
    public void setIO(boolean hasIO) {
    	this.hasIO = hasIO;
    }

    public JMethodDeclaration[] getInitPhases() {
        return initPhases;
    }
    
    public void setInitPhases(JMethodDeclaration[] initPhases) {
        this.initPhases = initPhases;
    }
    
    public JMethodDeclaration[] getPhases() {
        return phases;
    }

    public void setPhases(JMethodDeclaration[] phases) {
        this.phases = phases;
    }

    @Override
	public String toString() {
        return "SIRPhasedFilter name=" + getName();
    }

    @Override
	public int getPushForSchedule(Map<SIROperator, int[]>[] counts) {
        // not implementing this right now because I'm unclear if
        // there is a distinct execution count for each phase.  can
        // fix without too much trouble later. --bft
        Utils.fail("Don't yet support getPushForSchedule for phased filters.");
        return -1;
    }

    @Override
	public int getPopForSchedule(Map<SIROperator, int[]>[] counts) {
        // not implementing this right now because I'm unclear if
        // there is a distinct execution count for each phase.  can
        // fix without too much trouble later. --bft
        Utils.fail("Don't yet support getPopForSchedule for phased filters.");
        return -1;
    }

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    @Override
	public Object deepClone() {
        at.dms.kjc.sir.SIRPhasedFilter other = new at.dms.kjc.sir.SIRPhasedFilter();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.sir.SIRPhasedFilter other) {
        super.deepCloneInto(other);
        other.inputType = (at.dms.kjc.CType)at.dms.kjc.AutoCloner.cloneToplevel(this.inputType);
        other.outputType = (at.dms.kjc.CType)at.dms.kjc.AutoCloner.cloneToplevel(this.outputType);
        other.stateful = this.stateful;
        other.isFissed = this.isFissed;
        other.isIterationFilter = this.isIterationFilter;
        other.hasIO = this.hasIO;
        other.initPhases = (at.dms.kjc.JMethodDeclaration[])at.dms.kjc.AutoCloner.cloneToplevel(this.initPhases);
        other.phases = (at.dms.kjc.JMethodDeclaration[])at.dms.kjc.AutoCloner.cloneToplevel(this.phases);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
