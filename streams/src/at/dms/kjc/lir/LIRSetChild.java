package at.dms.kjc.lir;

import at.dms.kjc.JExpression;
import at.dms.kjc.SLIRVisitor;

/**
 * This represents the allocation and initialization of a child stream
 * structure.  It should be expanded into these four lines in the C
 * version: 
 *
 *  d->child1 = malloc(sizeof(HelloWorld6_1_data));
 *  d->child1->c = create_context(d->child1);
 *  register_child(d->c, d->child1->c);
 */
public class LIRSetChild extends LIRNode {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3561426844909078600L;

	/**
     * The type of data that needs to be allocated for the child. 
     * (e.g. HelloWorld6_1_data)
     */
    private String childType;

    /**
     * The name of the child (e.g. child1)
     */
    private String childName;
    
    // ----------------------------------------------------------------------
    // CONSTRUCTORS
    // ----------------------------------------------------------------------

    /**
     * Construct a node.
     */
    public LIRSetChild(JExpression streamContext,
                       String childType,
                       String childName) {
        super(streamContext);
        this.childType = childType;
        this.childName = childName;
    }

    @Override
	public void accept(SLIRVisitor v)
    {
        v.visitSetChild(this, 
                        this.getStreamContext(), 
                        this.childType,
                        this.childName);
    }
}
