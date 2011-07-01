package at.dms.kjc.lir;

import at.dms.kjc.JExpression;
import at.dms.kjc.SLIRVisitor;

/**
 * This appears at each exit from a work function.
 */
public class LIRWorkExit extends LIRNode
{
    public LIRWorkExit(JExpression streamContext)
    {
        super(streamContext);
    }
    public void accept(SLIRVisitor v)
    {
        v.visitWorkExit(this);
    }
}
