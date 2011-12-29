package at.dms.kjc.smp;

import at.dms.kjc.CIntType;

/**
 * Fixed point type used when converting floating point to fixed point.
 * 
 * @author mgordon
 *
 */
public class CFixedPointType extends CIntType {
    /**
	 * 
	 */
	private static final long serialVersionUID = -10402723410945803L;
	public static CFixedPointType FixedPoint = new CFixedPointType();
    
    /**
     * Returns a string representation of this type.
     */
    @Override
	public String toString() {
        return "fixed";
    }
    
}
