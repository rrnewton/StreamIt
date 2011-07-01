package at.dms.kjc.slir;

import at.dms.kjc.CType;

public class Channel {
    public static final String[] DO_NOT_CLONE_THESE_FIELDS = { "src", "dest" };
    /**
     * Source of directed edge
     */
    protected Filter src;

    /**
     * Destination of directed edge
     */
    protected Filter dest;

    /**
     * Caches type for {@link #getType()} calls
     */
    private CType type;

	public Filter getDest() {
		return dest;
	}

	public Filter getSrc() {
		return src;
	}
}
