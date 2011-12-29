package at.dms.kjc.sir;

import at.dms.compiler.PositionedError;
import at.dms.kjc.AttributeVisitor;
import at.dms.kjc.CBodyContext;
import at.dms.kjc.CodeSequence;
import at.dms.kjc.JStatement;
import at.dms.kjc.KjcVisitor;
import at.dms.kjc.SLIRAttributeVisitor;
import at.dms.kjc.SLIRVisitor;

/**
 * This class represents an annotation in the IR.  It represents an
 * empty statement that is used as a placeholder for auxilliary
 * information between different passes of the compiler.
 */
public class SIRMarker extends JStatement {

    /**
     * Create an SIRMarker.
     */
    public SIRMarker() {
        super(null, null);
    }

    // ----------------------------------------------------------------------
    // SEMANTIC ANALYSIS
    // ----------------------------------------------------------------------

    /**
     * Analyses the statement (semantically) - NOT SUPPORTED YET.
     */
    @Override
	public void analyse(CBodyContext context) throws PositionedError {
        at.dms.util.Utils.fail("Analysis of SIR nodes not supported yet.");
    }

    // ----------------------------------------------------------------------
    // CODE GENERATION
    // ----------------------------------------------------------------------

    /**
     * Accepts the specified visitor.
     */
    @Override
	public void accept(KjcVisitor p) {
        if (p instanceof SLIRVisitor) {
            ((SLIRVisitor)p).visitMarker(this);
        } else {
            // otherwise, do nothing
        }
    }

    /**
     * Accepts the specified attribute visitor.
     * @param   p               the visitor
     */
    @Override
	public Object accept(AttributeVisitor p) {
        if (p instanceof SLIRAttributeVisitor) {
            return ((SLIRAttributeVisitor)p).visitMarker(this);
        } else {
            return this;
        }
    }

    /**
     * Generates a sequence of bytescodes - NOT SUPPORTED YET.
     */
    @Override
	public void genCode(CodeSequence code) {
        at.dms.util.Utils.fail("Codegen of SIR nodes not supported yet.");
    }

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    @Override
	public Object deepClone() {
        at.dms.kjc.sir.SIRMarker other = new at.dms.kjc.sir.SIRMarker();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.sir.SIRMarker other) {
        super.deepCloneInto(other);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
