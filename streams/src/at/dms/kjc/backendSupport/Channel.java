/**
 * 
 */
package at.dms.kjc.backendSupport;

import at.dms.kjc.slir.Edge;

/**
 * @author soule
 *
 */
public class Channel {

    protected Edge theEdge;
	
    protected Channel(Edge edge) {
        assert edge != null;
        this.theEdge = edge;
    }

	/**
	 * @return
	 */
	public String peekMethodName() {
		assert false;
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	public String popMethodName() {
		// TODO Auto-generated method stub
		assert false;		
		return null;
	}

	/**
	 * @return
	 */
	public String popManyMethodName() {
		assert false;
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	public String pushMethodName() {
		assert false;
		// TODO Auto-generated method stub
		return null;
	}

	
    
}
