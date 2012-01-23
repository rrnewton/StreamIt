/**
 * 
 */
package at.dms.kjc.slir;

import at.dms.kjc.CType;

/**
 * @author soule
 *
 */
public abstract class Edge<S,T> {

	protected S src;
	protected T dst;
	protected CType type;
	
	public static final String[] DO_NOT_CLONE_THESE_FIELDS = { "src", "dst" };
	
	/**
	 * @return
	 */
	public abstract CType getType();
	
	public Edge(S src, T dst) {
		this.src = src;
		this.dst = dst;
	}
	
	public Edge() {
		
	}
	
	public T getDest() {
		return dst;
	}
	
	public S getSrc() {
		return src;
	}

    public void setSrc(S src) {
    	this.src = src;
    }

    public void setDest(T dest) {
    	this.dst = dest;
    }

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() { at.dms.util.Utils.fail("Error in auto-generated cloning methods - deepClone was called on an abstract class."); return null; }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.Edge other) {
        other.src = at.dms.kjc.AutoCloner.cloneToplevel(this.src);
        other.dst = at.dms.kjc.AutoCloner.cloneToplevel(this.dst);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
