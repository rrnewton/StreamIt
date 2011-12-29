package at.dms.kjc.lir;

import at.dms.kjc.JExpression;
import at.dms.kjc.SLIRVisitor;

/**
 * This represents allocation and initialization of a child stream
 * that is an identity filter.  It roughly corresponds to an LIRSetChild,
 * except that the type is a library-supported file writer instead of
 * an SIR-defined structure.  This can frequently be eliminated, though
 * we have no support for directly connecting splitters and joiners in
 * the uniprocessor backend at the moment.
 */
public class LIRIdentity extends LIRNode 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 5624122818998666451L;
	/**
     * The name of the child (e.g. child1)
     */
    private String childName;
    
    /**
     * Construct a node.
     */
    public LIRIdentity(JExpression streamContext, String childName)
    {
        super(streamContext);
        this.childName = childName;
    }
    
    public String getChildName() 
    {
        return childName;
    }
    
    @Override
	public void accept(SLIRVisitor v)
    {
        v.visitIdentity(this);
    }
}

