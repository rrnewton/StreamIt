/**
 * 
 */
package at.dms.kjc.slir;

import at.dms.kjc.CType;

/**
 * @author soule
 *
 */
public interface Edge {

	/**
	 * @return
	 */
	CType getType();
	
	public <T> T getDest();
	
	public <T> T getSrc();
	
}
