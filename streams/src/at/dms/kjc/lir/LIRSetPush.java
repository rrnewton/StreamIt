package at.dms.kjc.lir;

import at.dms.kjc.JExpression;
import at.dms.kjc.SLIRVisitor;

/**
 * This sets how many items are pushed by this stream.
 */
public class LIRSetPush extends LIRNode {

    /**
     * The encode function.
     */
    private int push;

    // ----------------------------------------------------------------------
    // CONSTRUCTORS
    // ----------------------------------------------------------------------

    /**
     * Construct a node.
     */
    public LIRSetPush(JExpression streamContext,
                      int push) {
        super(streamContext);
        this.push = push;
    }

    public void accept(SLIRVisitor v)
    {
        v.visitSetPush(this, this.getStreamContext(), this.push);
    }
}
