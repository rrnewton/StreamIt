package at.dms.kjc.slir;

/**
 * The link class represents a link between two SSG's.  The Rate information 
 * stores the (possibly dynamic) rate information of the source and destination.
 * 
 * @author mgordon
 *
 */
public class Link {
	private InputPort inputPort;
	private OutputPort outputPort;
	/** defined on the source outputport */
	private Rate pushRate;
	/** defined on the destination inputport */
	private Rate popRate;
	private Rate peekRate;
}
