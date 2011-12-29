package at.dms.kjc.lir;

import at.dms.kjc.JExpression;
import at.dms.kjc.SLIRVisitor;

/**
 * This registers a stream's work function with the runtime system.
 */
public class LIRSetWork extends LIRNode {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4133980656629336026L;
	/**
     * The work function.
     */
    private LIRFunctionPointer work;

    // ----------------------------------------------------------------------
    // CONSTRUCTORS
    // ----------------------------------------------------------------------

    /**
     * Construct a node.
     */
    public LIRSetWork(JExpression streamContext,
                      LIRFunctionPointer work) {
        super(streamContext);
        this.work = work;
    }

    @Override
	public void accept(SLIRVisitor v) {
        v.visitSetWork(this, this.getStreamContext(), this.work);
    }
}
