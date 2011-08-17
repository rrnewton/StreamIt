/**
 * 
 */
package at.dms.kjc.smp;

import at.dms.kjc.KjcOptions;
import at.dms.kjc.backendSupport.Channel;
import at.dms.kjc.slir.Edge;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InterSSGEdge;
import at.dms.kjc.slir.OutputPort;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.WorkNode;

/**
 * @author soule
 *
 */
public class DynamicBuffer extends Channel {

	/**
	 * @param edge
	 */
	protected DynamicBuffer(Edge edge) {
		super(edge);
	}
		
    /**
     * Return the input buffer associated with the filter node.
     * 
     * @param fsn The filter node in question.
     * @param ssg 
     * @return The input buffer of the filter node.
     */
    public static DynamicBuffer getInputBuffer(WorkNode fsn, StaticSubGraph ssg) {
    	InterSSGEdge edge = ssg.getInputPort().getLinks().get(0);
    	return new DynamicBuffer(edge);
    }

	/**
	 * @return
	 */
	public String peekMethodName() {
		return "dynamic_buffer_peek";				
	}

	/**
	 * @return
	 */
	public String popMethodName() {
		return "dynamic_buffer_pop";				
	}

	/**
	 * @return
	 */
	public String popManyMethodName() {
		return "dynamic_buffer_pop_many";				
	}

	public String pushMethodName() {
		return "dynamic_buffer_push";				
	}
	
	/**
	 * @param filterNode
	 * @param ssg
	 * @return
	 */
	public static Channel getOutputBuffer(WorkNode filterNode,
			StaticSubGraph ssg) {
		// TODO Auto-generated method stub
    	InterSSGEdge edge = ssg.getOutputPort().getLinks().get(0);
    	return new DynamicBuffer(edge);
	}
}
