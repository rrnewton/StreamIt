package at.dms.kjc.sir;

import at.dms.kjc.CType;
import at.dms.kjc.JExpression;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.sir.lowering.Propagator;

/**
 * This represents a StreaMIT filter that has some compiler-defined
 * functionality.  The init and work functions are not implemented by
 * the user.
 */
public class SIRPredefinedFilter extends SIRFilter implements Cloneable {

    public SIRPredefinedFilter() {
        super();
    }

    public SIRPredefinedFilter(SIRContainer parent,
                               String ident,
                               JFieldDeclaration[] fields, 
                               JMethodDeclaration[] methods, 
                               JExpression peek, JExpression pop, JExpression push, 
                               CType inputType, CType outputType) {
        super(parent,
              ident,
              fields,
              methods,
              peek, pop, push,
              /* work */ new JMethodDeclaration("SIRPredefinedFilter " + ident),
              /* input type */ inputType,
              /* output type */ outputType);
    }

    @Override
	public boolean needsInit() {
        return false;
    }

    @Override
	public boolean needsWork() {
        return false;
    }

    @Override
	public String getTypeNameInC() {
        return "ContextContainer";
    }

    /**
     * Uses <propagator> to propagate constants into predefined fields
     * of this.  To be overridden by implementors.
     */
    public void propagatePredefinedFields(Propagator propagator) {
    }

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    @Override
	public Object deepClone() {
        at.dms.kjc.sir.SIRPredefinedFilter other = new at.dms.kjc.sir.SIRPredefinedFilter();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.sir.SIRPredefinedFilter other) {
        super.deepCloneInto(other);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}


