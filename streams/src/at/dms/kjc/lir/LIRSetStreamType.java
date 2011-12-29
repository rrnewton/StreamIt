package at.dms.kjc.lir;

import at.dms.kjc.JExpression;
import at.dms.kjc.SLIRVisitor;

/**
 * This sets the type of a stream.
 */
public class LIRSetStreamType extends LIRNode {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5711354921634272284L;
	/**
     * The encode function.
     */
    private LIRStreamType streamType;

    // ----------------------------------------------------------------------
    // CONSTRUCTORS
    // ----------------------------------------------------------------------

    /**
     * Construct a node.
     */
    public LIRSetStreamType(JExpression streamContext,
                            LIRStreamType streamType) {
        super(streamContext);
        this.streamType = streamType;
    }

    @Override
	public void accept(SLIRVisitor v)
    {
        v.visitSetStreamType(this, 
                             this.getStreamContext(), 
                             this.streamType);
    }
}
