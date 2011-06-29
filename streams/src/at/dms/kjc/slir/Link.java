package at.dms.kjc.slir;

public class Link {
	private InputPort inputPort;
	private OutputPort outputPort;
	/** defined on the source outputport */
	private Rate pushRate;
	/** defined on the destination inputport */
	private Rate popRate;
	private Rate peekRate;
}
