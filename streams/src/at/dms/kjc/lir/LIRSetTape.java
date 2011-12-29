package at.dms.kjc.lir;

import at.dms.kjc.CType;
import at.dms.kjc.JExpression;
import at.dms.kjc.SLIRVisitor;

/**
 * This specifies a tape connection between two streams.  I assume
 * that the runtime system assigns the actual tape to the appropriate
 * field of each structure.  
 *
 *  create_tape(d->child1, d->child2, sizeof(int), 1);
 */
public class LIRSetTape extends LIRNode {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2830739165471953084L;

	/**
     * The source structure (containing fields, children, tapes, streamContext)
     */
    private JExpression srcStruct;

    /**
     * The target structure (containing fields, children, tapes, streamContext)
     */
    private JExpression dstStruct;

    /**
     * The type of the items on the tape.
     */
    private CType type;

    /**
     * The size of the tape, in items.
     */
    private int size;

    // ----------------------------------------------------------------------
    // CONSTRUCTORS
    // ----------------------------------------------------------------------

    /**
     * Construct a node.
     */
    public LIRSetTape(JExpression streamContext,
                      JExpression srcStruct,
                      JExpression dstStruct,
                      CType type,
                      int size) {
        super(streamContext);
        this.srcStruct = srcStruct;
        this.dstStruct = dstStruct;
        this.type = type;
        this.size = size;
    }

    @Override
	public void accept(SLIRVisitor v)
    {
        v.visitSetTape(this, 
                       this.getStreamContext(), 
                       srcStruct,
                       dstStruct,
                       type,
                       size);
    }
}
