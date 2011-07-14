package at.dms.kjc.slir;

import java.util.LinkedList;
import java.util.List;

public abstract class OutputPort {
	protected List<Link> links;
	
	protected OutputPort() {
		links = new LinkedList<Link>();
	}
}
