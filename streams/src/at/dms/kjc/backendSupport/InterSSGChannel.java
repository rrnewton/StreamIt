/**
 * 
 */
package at.dms.kjc.backendSupport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import at.dms.kjc.JEmittedTextExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JStatement;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InterSSGEdge;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.StreamGraph;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.smp.Core;

/**
 * @author soule
 *
 */
public class InterSSGChannel extends Channel<InterSSGEdge> {

    /** a set of all the buffer types in the application */
    protected static HashSet<String> types;

    /** maps each WorkNode to Input/OutputRotatingBuffers */
    protected static HashMap<WorkNode, InterSSGChannel> inputBuffers;
    protected static HashMap<WorkNode, InterSSGChannel> outputBuffers;

    static {
        types = new HashSet<String>();
        inputBuffers = new HashMap<WorkNode, InterSSGChannel>();
        outputBuffers = new HashMap<WorkNode, InterSSGChannel>();
    }

	private WorkNode filterNode;
		
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
     * @return The input buffer of the filter node.
     */
    public static InterSSGChannel getInputBuffer(WorkNode fsn) {
    	System.out.println("InterSSGChannel.getgetInputBuffer fsn=" + fsn.toString() + " " + 
    	((null == inputBuffers.get(fsn)) ? "null" : "non-null"));
    	return inputBuffers.get(fsn);
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
		return "queue_pop";				
	}

	/**
	 * @return
	 */
	public String popManyMethodName() {
		return "dynamic_buffer_pop_many";				
	}

	public String pushMethodName() {
		return "queue_push";				
	}
	
	/**
	 * @param filterNode
	 * @param ssg
	 * @return
	 */
	public static Channel<InterSSGEdge> getOutputBuffer(WorkNode filterNode,
			StaticSubGraph ssg) {
		// TODO Auto-generated method stub
    	InterSSGEdge edge = ssg.getOutputPort().getLinks().get(0);
    	return new InterSSGChannel(edge);
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	public static Set<InterSSGChannel> getInputBuffersOnCore(Core t) {
		System.out.println("InterSSGChannel::getInputBuffersOnCore(n)");
		System.out.println("InterSSGChannel::getInputBuffersOnCore(n) inputBuffers.size()==" + inputBuffers.size());
		HashSet<InterSSGChannel> set = new HashSet<InterSSGChannel>();        
		for (InterSSGChannel b : inputBuffers.values()) {
			//if (SMPBackend.scheduler.getComputeNode(b.getFilterNode()).equals(t))
				set.add(b);
		}
		return set;
	}

	/**
	 * 
	 * @param node
	 * @param buf
	 */
	public static void setOutputBuffer(WorkNode node, InterSSGChannel buf) {
		outputBuffers.put(node, buf);
	}

	/**
	 * 
	 * @param node
	 * @param buf
	 */
	public static void setInputBuffer(WorkNode node, InterSSGChannel buf) {
		System.out.println("===> InterSSGChannel::setInputBuffer node=" + node.toString());
		inputBuffers.put(node, buf);
	}
	
    /** 
     * Return the filter this buffer is associated with.
     * 
     * @return Return the filter this buffer is associated with.
     */
    public WorkNode getFilterNode() {
        return filterNode;
    }
		

    /**
     * 
     * @param n
     * @return
     */
	public static Set<InterSSGChannel> getOutputBuffersOnCore(Core n) {
		System.out.println("InterSSGChannel::getOutputBuffersOnCore(n)");
		System.out.println("InterSSGChannel::getOutputBuffersOnCore(n) outputBuffers.size()==" + outputBuffers.size());
		HashSet<InterSSGChannel> set = new HashSet<InterSSGChannel>();        
		for (InterSSGChannel b : outputBuffers.values()) {
			//if (SMPBackend.scheduler.getComputeNode(b.getFilterNode()).equals(t))
				set.add(b);
		}
		return set;
	}

	/**
	 * @return
	 */
	public List<JStatement> dataDecls() {
		//		System.out.println("InterSSGChannel::dataDecls()");
		//		List<JStatement> statements = new LinkedList<JStatement>();
		//        JStatement stmt = new JExpressionStatement(new JEmittedTextExpression("queue ctx"));
		//        statements.add(stmt);
		//		return statements;		
		return new LinkedList<JStatement>();
	}

	public List<JStatement> writeDeclsExtern() {
		System.out.println("InterSSGChannel::writeDeclsExtern()");
		List<JStatement> statements = new LinkedList<JStatement>();
		JStatement stmt = new JExpressionStatement(new JEmittedTextExpression("extern queue_ctx_ptr dyn_queue"));
		statements.add(stmt);
		return statements;
	}

	public List<JStatement> readDeclsExtern() {
		System.out.println("InterSSGChannel::readDeclsExtern()");
		List<JStatement> statements = new LinkedList<JStatement>();
		JStatement stmt = new JExpressionStatement(new JEmittedTextExpression("extern queue_ctx_ptr dyn_queue"));
		statements.add(stmt);
		return statements;
	}

	 	 

	/**
	 * @param streamGraph
	 */
	public static void createBuffers(StreamGraph streamGraph) {
		System.out.println("InterSSGChannel::createBuffers()");
		createInputBuffers(streamGraph);	
		createOutputBuffers(streamGraph);	
	}

	/**
	 * 
	 * @param streamGraph
	 */
	private static void createInputBuffers(StreamGraph streamGraph) {
		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		System.out.println("InterSSGChannel::createInputBuffers()");
		System.out.println("InterSSGChannel::createInputBuffers()  streamGraph.getSSGs().size=" +  streamGraph.getSSGs().size());
		int i = 0;
		for ( StaticSubGraph ssg : streamGraph.getSSGs()) {
			System.out.println("InterSSGChannel::createInputBuffers()  ssg=" +  i);
			i++;
			Filter top = ssg.getTopFilters()[0];
			if (ssg.getTopFilters() != null) {
				System.out.println("InterSSGChannel::createInputBuffers()  top=" +  top.getWorkNode().toString());
			} else {
				System.out.println("InterSSGChannel::createInputBuffers()  top=null");
			}
			
			List<InterSSGEdge> links = ssg.getInputPort().getLinks();
			System.out.println("InterSSGChannel::createInputBuffers()  links.size()=" + links.size());
			if (links.size() == 0) {
				continue;
			}
			InterSSGEdge edge = ssg.getInputPort().getLinks().get(0);		    
			InterSSGChannel channel = new InterSSGChannel(edge);
			if (ssg.getTopFilters() != null) {
				top = ssg.getTopFilters()[0];
				setInputBuffer(top.getWorkNode(), channel);
				//inputBuffers.put(top.getWorkNode(), channel);
			} else {
				assert false : "InterSSGChannel::createInputBuffers() : ssg.getTopFilters() is null";			
			}
		}
	}
		
	/**
	 * 
	 * @param streamGraph
	 */
	private static void createOutputBuffers(StreamGraph streamGraph) {
		for ( StaticSubGraph ssg : streamGraph.getSSGs()) {
			List<InterSSGEdge> links = ssg.getOutputPort().getLinks();
			if (links.size() == 0) {
				continue;
			}
			InterSSGEdge edge = ssg.getOutputPort().getLinks().get(0);		    
			InterSSGChannel channel = new InterSSGChannel(edge);
			if (ssg.getTopFilters() != null) {
				Filter top = ssg.getTopFilters()[0];
				outputBuffers.put(top.getWorkNode(), channel);
			} else {
				assert false : "InterSSGChannel::createOutputBuffers() : ssg.getTopFilters() is null";			
			}
		}	

		
	}
	
}
