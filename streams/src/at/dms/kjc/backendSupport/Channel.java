/**
 * 
 */
package at.dms.kjc.backendSupport;

import at.dms.kjc.slir.Edge;

/**
 * @author soule
 *
 */
public abstract class Channel {

    protected Edge theEdge;
	
    protected Channel(Edge edge) {
        assert edge != null;
        this.theEdge = edge;
    }

	/**
	 * @return
	 */
	public String peekMethodName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	public String popMethodName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	public String popManyMethodName() {
		// TODO Auto-generated method stub
		return null;
	}

	
    
}
