/**
 * 
 */
package at.dms.kjc.backendSupport;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.dms.kjc.CEmittedTextType;
import at.dms.kjc.CStdType;
import at.dms.kjc.CType;
import at.dms.kjc.JBlock;
import at.dms.kjc.JEmittedTextExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JFormalParameter;
import at.dms.kjc.JIfStatement;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JStatement;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InputPort;
import at.dms.kjc.slir.InterSSGEdge;
import at.dms.kjc.slir.OutputPort;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.StreamGraph;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.smp.Core;
import at.dms.kjc.smp.SMPBackend;
import at.dms.kjc.smp.Util;
import at.dms.util.Utils;

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

	protected static HashMap<String, String> bufferVariables;	
	protected static HashMap<String, String> createMethods;	

	static {
		types = new HashSet<String>();
		inputBuffers = new HashMap<WorkNode, InterSSGChannel>();
		outputBuffers = new HashMap<WorkNode, InterSSGChannel>();
	}

	/**
	 * @param streamGraph
	 */
	public static void createBuffers(StreamGraph streamGraph) {
		createInputBuffers(streamGraph);
		createOutputBuffers(streamGraph);
	}

	/**
	 * Create the implementation for queues of different types
	 */
	public static void createDynamicQueues() {		
		for (String type : types) {			
			SMPBackend.dynamicQueueCodeGenerator.addQueueType(type);
		}
	}

	/**
	 * 
	 * @param streamGraph
	 */
	private static void createInputBuffers(StreamGraph streamGraph) {
		for (StaticSubGraph ssg : streamGraph.getSSGs()) {
			Filter top = ssg.getTopFilters()[0];
			InputPort inputPort = ssg.getInputPort();
			if (inputPort == null) {
				continue;
			}
			InterSSGEdge edge = ssg.getInputPort().getLinks().get(0);
			
			System.out.println("InterSSGChannel.createInputBuffers edge=" + edge.toString());

			InterSSGChannel channel = new InterSSGChannel(edge);			
			if (ssg.getTopFilters() != null) {
				top = ssg.getTopFilters()[0];
				CType bufType = top.getWorkNode().getFilter().getInputType();
				System.out.println("InterSSGChannel.createInputBuffers creating a dynamic buffer for type " + bufType.toString());
				types.add(bufType.toString());							
				inputBuffers.put(top.getWorkNode(), channel);
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
		for (StaticSubGraph ssg : streamGraph.getSSGs()) {
			OutputPort outputPort = ssg.getOutputPort();
			if (outputPort == null) {
				continue;
			}
			List<InterSSGEdge> links = ssg.getOutputPort().getLinks();
			if (links.size() == 0) {
				continue;
			}
			InterSSGEdge edge = ssg.getOutputPort().getLinks().get(0);
			
			System.out.println("InterSSGChannel.createOutputBuffers edge=" + edge.toString());
			
			InterSSGChannel channel = new InterSSGChannel(edge);
			if (ssg.getTopFilters() != null) {
				Filter top = ssg.getTopFilters()[0];
				outputBuffers.put(top.getWorkNode(), channel);
			} else {
				assert false : "InterSSGChannel::createOutputBuffers() : ssg.getTopFilters() is null";
			}
		}

	}

	public static Collection<InterSSGChannel> getInterSSGChannels() {
		return inputBuffers.values();
	}

	/**
	 * Return the input buffer associated with the filter node.
	 * 
	 * @param fsn
	 *            The filter node in question.
	 * @return The input buffer of the filter node.
	 */
	public static InterSSGChannel getInputBuffer(WorkNode fsn) {
		return inputBuffers.get(fsn);
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	public static Set<InterSSGChannel> getInputBuffersOnCore(Core t) {
		HashSet<InterSSGChannel> set = new HashSet<InterSSGChannel>();		
		for (InterSSGChannel b : inputBuffers.values()) {

			InterSSGEdge edge = b.getEdge();
			OutputPort iport = edge.getSrc();
			StaticSubGraph ssg = iport.getSSG();			

			Filter top[] = ssg.getTopFilters();

			for (Filter f : top) {

				System.out.println("InterSSGChannel.getInputBuffersOnCore core=" + t.getCoreID() 
						+ " filter=" + f.getWorkNode().toString());

				System.out.println("InterSSGChannel.getInputBuffersOnCore computeNode=" + 
						SMPBackend.scheduler.getComputeNode(f.getWorkNode()).getCoreID());

				if (SMPBackend.scheduler.getComputeNode(f.getWorkNode())
						.equals(t)) {
					System.out.println("InterSSGChannel.getInputBuffersOnCore adding b"); 							
					set.add(b);
				}


			}
		}
		return set;
	}

	/**
	 * @param filterNode
	 * @param ssg
	 * @return
	 */
	public static Channel<InterSSGEdge> getOutputBuffer(WorkNode filterNode,
			StaticSubGraph ssg) {
		if (ssg.getOutputPort() == null) {
			return null;
		}
		InterSSGEdge edge = ssg.getOutputPort().getLinks().get(0);
		return new InterSSGChannel(edge);
	}

	/**
	 * 
	 * @param n
	 * @return
	 */
	public static Set<InterSSGChannel> getOutputBuffersOnCore(Core t) {
		HashSet<InterSSGChannel> set = new HashSet<InterSSGChannel>();
		for (InterSSGChannel b : outputBuffers.values()) {

			InterSSGEdge edge = b.getEdge();
			OutputPort port = edge.getSrc();
			StaticSubGraph ssg = port.getSSG();
			Filter top[] = ssg.getFilterGraph();

			if (SMPBackend.scheduler.getComputeNode(top[0].getWorkNode())
					.equals(t))
				set.add(b);
		}
		return set;
	}

	/**
	 * @param edge
	 */
	protected InterSSGChannel(InterSSGEdge edge) {
		super(edge);
	}

	/**
	 * @return
	 */
	public List<JStatement> dataDecls() {
		return new LinkedList<JStatement>();
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
	public String popManyMethodName() {
		return "dynamic_buffer_pop_many";
	}

	/**
	 * @return
	 */
	public String popMethodName() {
		String type = edge.getType().toString();
		return type + "_queue_pop";
	}

	public String pushMethodName() {
		String type = edge.getType().toString();
		return  type + "_queue_push";
	}

}
