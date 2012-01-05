package at.dms.kjc.slir;

import at.dms.kjc.CType;
import at.dms.kjc.JStatement;
import at.dms.kjc.sir.SIRPredefinedFilter;

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
    
    
    @Override
	public abstract void createContent();
    
    /**
     * Returns filename of OutputContent.
     *
     * @return The filename
     */
    public abstract String getFileName();
    
    /**
     * Return a statement closing the file. Only valid after calling
     * {@link #createContent()}.
     * 
     * @return statement to put in cleanup section of code.
     */
    public abstract JStatement closeFile();
    
    /**
     * Get the type of the file writer .
     * 
     * @return The type.
     */
    public abstract CType getType();

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    @Override
	public Object deepClone() { at.dms.util.Utils.fail("Error in auto-generated cloning methods - deepClone was called on an abstract class."); return null; }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.OutputContent other) {
        super.deepCloneInto(other);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
