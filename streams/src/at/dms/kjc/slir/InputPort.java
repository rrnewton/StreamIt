package at.dms.kjc.slir;

import java.util.LinkedList;
import java.util.List;

public abstract class InputPort {
	protected List<InterSSGEdge> links;
	
	protected InputPort() {
		links = new LinkedList<InterSSGEdge>();
	}
}
