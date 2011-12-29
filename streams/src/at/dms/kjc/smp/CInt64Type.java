package at.dms.kjc.smp;

import at.dms.kjc.CLongType;

final class CInt64Type extends CLongType {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4543256562027638512L;
	public static CInt64Type Int64 = new CInt64Type();

    /**
     * Returns a string representation of this type.
     */
    @Override
	public String toString() {
        return "uint64_t";
    }
}

