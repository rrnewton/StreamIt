package at.dms.kjc.lir;

import at.dms.kjc.JExpression;
import at.dms.kjc.SLIRVisitor;

/**
 * This appears at the entry to a work function.
 */
public class LIRWorkEntry extends LIRNode
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -1308221219622831356L;
	public LIRWorkEntry(JExpression streamContext)
    {
        super(streamContext);
    }
    @Override
	public void accept(SLIRVisitor v)
    {
        v.visitWorkEntry(this);
    }
}
