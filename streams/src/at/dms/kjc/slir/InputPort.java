package at.dms.kjc.slir;

import java.util.LinkedList;
import java.util.List;

public abstract class InputPort {
	protected List<Link> links;
	
	protected InputPort() {
		links = new LinkedList<Link>();
	}
}
