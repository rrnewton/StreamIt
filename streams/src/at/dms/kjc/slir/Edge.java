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
}
