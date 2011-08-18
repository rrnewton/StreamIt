/**
 * 
 */
package at.dms.kjc.backendSupport;

import java.util.Set;

import at.dms.kjc.slir.InterSSGEdge;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.smp.Core;

/**
 * @author soule
 *
 */
public class InterSSGChannel extends Channel<InterSSGEdge> {

	// TODO: Override more methods in here for declarations to be output
	
	
	/**
	 * @param edge
	 */
	protected InterSSGChannel(InterSSGEdge edge) {
		super(edge);
	}
		
    /**
     * Return the input buffer associated with the filter node.
     * 
     * @param fsn The filter node in question.
     * @param ssg 
     * @return The input buffer of the filter node.
     */
    public static InterSSGChannel getInputBuffer(WorkNode fsn, StaticSubGraph ssg) {
    	InterSSGEdge edge = ssg.getInputPort().getLinks().get(0);
    	return new InterSSGChannel(edge);
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
    	return new InterSSGChannel(edge);
	}

	public static Set<InterSSGChannel> getInputBuffersOnCore(Core n) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Set<InterSSGChannel> getOutputBuffersOnCore(Core n) {
		// TODO Auto-generated method stub
		return null;
	}
}
