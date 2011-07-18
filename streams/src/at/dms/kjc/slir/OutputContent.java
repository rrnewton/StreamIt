package at.dms.kjc.slir;

import at.dms.kjc.sir.SIRPredefinedFilter;
import at.dms.kjc.slicegraph.UnflatFilter;

/**
 * Predefined FilterContent for output. Could be restructured to be
 * interface but it was easier to extend PredefinedContent this way
 * and can hold code common for predefined output.
 * @author jasperln
 */
public abstract class OutputContent extends PredefinedContent implements at.dms.kjc.DeepCloneable {
    /**
     * No argument constructor, FOR AUTOMATIC CLONING ONLY.
     */
    protected OutputContent() {
        super();
    }

    /**
     * Copy constructor for OutputContent.
     * @param content The OutputContent to copy.
     */
    public OutputContent(OutputContent content) {
        super(content);
    }

    /**
     * Construct OutputContent from SIRPredefinedFilter
     * @param filter The SIRPredefinedFilter used to contruct the OutputContent.
     */
    public OutputContent(SIRPredefinedFilter filter) {
        super(filter);
    }

    /**
     * Construct OutputContent from UnflatFilter.
     * @param unflat The UnflatFilter used to contruct the OutputContent.
     */
    public OutputContent(UnflatFilter unflat) {
        super(unflat);
    }
    
    public abstract void createContent();

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() { at.dms.util.Utils.fail("Error in auto-generated cloning methods - deepClone was called on an abstract class."); return null; }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.OutputContent other) {
        super.deepCloneInto(other);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
