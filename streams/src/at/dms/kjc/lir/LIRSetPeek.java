package at.dms.kjc.lir;

import at.dms.kjc.JExpression;
import at.dms.kjc.SLIRVisitor;

/**
 * This sets how many items are peeked by this stream.
 */
public class LIRSetPeek extends LIRNode {

    /**
     * The encode function.
     */
    private int peek;

    // ----------------------------------------------------------------------
    // CONSTRUCTORS
    // ----------------------------------------------------------------------

    /**
     * Construct a node.
     */
    public LIRSetPeek(JExpression streamContext,
                      int peek) {
        super(streamContext);
        this.peek = peek;
    }

    @Override
	public void accept(SLIRVisitor v)
    {
        v.visitSetPeek(this, this.getStreamContext(), this.peek);
    }
}
