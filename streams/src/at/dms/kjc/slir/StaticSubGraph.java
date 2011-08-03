package at.dms.kjc.slir;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 
 * @author mgordon and soule
 *
 */
public class StaticSubGraph {
	
    private InputPort inputPort;

    private OutputPort outputPort;

    private List<Filter> roots;
    
    private Filter[] filterGraph;

	public StaticSubGraph() {
		roots = new ArrayList<Filter>();
	}
    
	public StaticSubGraph(InputPort inputPort, OutputPort outputPort,
			List<Filter> roots) {
		super();
		this.inputPort = inputPort;
		this.outputPort = outputPort;
		this.roots = roots;
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

	public List<Filter> getRoots() {
		return roots;
	}

	public void setRoots(List<Filter> roots) {
		this.roots = roots;
	}

	public void addRoot(Filter filter) {
		roots.add(filter);
	}
    
	public void setFilterGraph(Filter[] filterGraph) {
		this.filterGraph = filterGraph;	
	}

	public Filter[] getFilterGraph() {
		return filterGraph;	
	}

}
