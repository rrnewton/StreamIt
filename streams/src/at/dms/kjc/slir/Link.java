package at.dms.kjc.slir;

/**
 * The link class represents a link between two SSG's. The Rate information
 * stores the (possibly dynamic) rate information of the source and destination.
 * 
 * @author mgordon
 * 
 */
public class Link {
	
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
	public Link(InputPort inputPort, OutputPort outputPort) {
		this.inputPort = inputPort;
		this.outputPort = outputPort;
	}
		
	public Link() {
		// TODO Auto-generated constructor stub
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


    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.slir.Link other = new at.dms.kjc.slir.Link();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.Link other) {
        other.inputPort = (at.dms.kjc.slir.InputPort)at.dms.kjc.AutoCloner.cloneToplevel(this.inputPort);
        other.outputPort = (at.dms.kjc.slir.OutputPort)at.dms.kjc.AutoCloner.cloneToplevel(this.outputPort);
        other.pushRate = (at.dms.kjc.slir.Rate)at.dms.kjc.AutoCloner.cloneToplevel(this.pushRate);
        other.popRate = (at.dms.kjc.slir.Rate)at.dms.kjc.AutoCloner.cloneToplevel(this.popRate);
        other.peekRate = (at.dms.kjc.slir.Rate)at.dms.kjc.AutoCloner.cloneToplevel(this.peekRate);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
