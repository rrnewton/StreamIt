package at.dms.kjc.slir;

/**
 * The InterSSGEdge class represents a link between two SSG's. The Rate information
 * stores the (possibly dynamic) rate information of the source and destination.
 *
 * @author mgordon
 *
 */


public class InterSSGEdge implements Edge {

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
	public InterSSGEdge(InputPort inputPort, OutputPort outputPort) {
		this.inputPort = inputPort;
		this.outputPort = outputPort;
	}

	public InputPort getInputPort() {
		return inputPort;
	}

	public void setInputPort(InputPort inputPort) {
		this.inputPort = inputPort;
	}

	public OutputPort getOutputPort() {
		return outputPort;
	}
	public void setOutputPort(OutputPort outputPort) {
		this.outputPort = outputPort;
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
}
