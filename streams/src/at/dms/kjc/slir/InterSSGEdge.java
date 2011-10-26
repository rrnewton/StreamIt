package at.dms.kjc.slir;

import at.dms.kjc.CType;

/**
 * The InterSSGEdge class represents a link between two SSG's. The Rate information
 * stores the (possibly dynamic) rate information of the source and destination.
 *
 * @author mgordon
 *
 */


public class InterSSGEdge extends Edge<OutputPort, InputPort> {

	/** The destination port for this link */
	private InputPort inputPort;

	/** The source port for this link */
	private OutputPort outputPort;

	/** defined on the source OutputPort */
	private Rate pushRate;

	/** defined on the destination InputPort */
	private Rate popRate;

	/** defined on the destination InputPort */
	private Rate peekRate;

	/** Create a new Link */
	public InterSSGEdge(OutputPort src, InputPort dst) {
		super(src, dst);
	}


	public Rate getPushRate() {
		return pushRate;
	}

	public void setPushRate(Rate pushRate) {
		this.pushRate = pushRate;
	}

	public Rate getPopRate() {
		return popRate;
	}

	public void setPopRate(Rate popRate) {
		this.popRate = popRate;
	}

	public Rate getPeekRate() {
		return peekRate;
	}

	public void setPeekRate(Rate peekRate) {
		this.peekRate = peekRate;
	}

	/* (non-Javadoc)
	 * @see at.dms.kjc.slir.Edge#getType()
	 */
	@Override
	public CType getType() {
		// TODO Auto-generated method stub
		return null;
	}
}