package at.dms.kjc.slir;

import java.util.LinkedList;
import java.util.List;

public abstract class OutputPort {
	protected List<InterSSGEdge> links;
	
	protected OutputPort() {
		links = new LinkedList<InterSSGEdge>();
	}
}
