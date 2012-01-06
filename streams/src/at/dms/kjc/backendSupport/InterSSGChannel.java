/**
 * 
 */
package at.dms.kjc.backendSupport;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import at.dms.kjc.CType;
import at.dms.kjc.JStatement;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InputPort;
import at.dms.kjc.slir.InterSSGEdge;
import at.dms.kjc.slir.OutputPort;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.StreamGraph;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.smp.Core;
import at.dms.kjc.smp.ProcessFileWriter;
import at.dms.kjc.smp.SMPBackend;

/**
 * @author soule
 * 
 */
public class InterSSGChannel extends Channel<InterSSGEdge> {

	/** a set of all the buffer types in the application */
	protected static HashSet<String> types;
	
	/* return all the input buffers of the file writers of this application */
    public static Set<InterSSGChannel> getFileWriterBuffers() {
        return fileWriterBuffers;
    }
    
    /** stores InputRotatingBuffers for file writers */
    protected static HashSet<InterSSGChannel> fileWriterBuffers;
 	
	/** maps each WorkNode to Input/OutputRotatingBuffers */
	protected static HashMap<WorkNode, InterSSGChannel> inputBuffers;
	protected static HashMap<WorkNode, InterSSGChannel> outputBuffers;

	protected static HashMap<String, String> bufferVariables;
	protected static HashMap<String, String> createMethods;

	static {
		types = new HashSet<String>();
		inputBuffers = new HashMap<WorkNode, InterSSGChannel>();
		outputBuffers = new HashMap<WorkNode, InterSSGChannel>();
		fileWriterBuffers = new HashSet<InterSSGChannel>(); 
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
		System.out
				.println("InterSSGChannel.createInputBuffers streamGraph.getSSGs().size()="
						+ streamGraph.getSSGs().size());

		for (StaticSubGraph srcSSG : streamGraph.getSSGs()) {
			OutputPort outputPort = srcSSG.getOutputPort();
			if (outputPort == null) {
				continue;
			}
			for (InterSSGEdge edge : outputPort.getLinks()) {
				System.out.println("InterSSGChannel.createInputBuffers edge="
						+ edge.toString());
				InterSSGChannel channel = new InterSSGChannel(edge);
				CType bufType = edge.getType();
				types.add(bufType.toString());
				Filter top = edge.getDest().getSSG().getTopFilters()[0];
				inputBuffers.put(top.getWorkNode(), channel);
				
				if (top.getWorkNode().isFileOutput()) {
				    fileWriterBuffers.add(channel);
				}
				
				if (top.getWorkNode().isFileOutput()) {
				    ProcessFileWriter.getAllocatingCore(top.getWorkNode());
				}				
				
			}
		}
	}

	/**
	 * 
	 * @param streamGraph
	 */
	private static void createOutputBuffers(StreamGraph streamGraph) {

		for (StaticSubGraph srcSSG : streamGraph.getSSGs()) {
			InputPort inputPort = srcSSG.getInputPort();
			if (inputPort == null) {
				continue;
			}
			for (InterSSGEdge edge : inputPort.getLinks()) {
				System.out.println("InterSSGChannel.createOutputBuffers edge="
						+ edge.toString());
				InterSSGChannel channel = new InterSSGChannel(edge);
				CType bufType = edge.getType();
				types.add(bufType.toString());
				Filter top = srcSSG.getTopFilters()[0];
				outputBuffers.put(top.getWorkNode(), channel);
				
                if (top.getWorkNode().isFileOutput()) {
                    ProcessFileWriter.getAllocatingCore(top.getWorkNode());
                }               

				
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

				System.out
						.println("InterSSGChannel.getInputBuffersOnCore core="
								+ t.getCoreID() + " filter="
								+ f.getWorkNode().toString());

				System.out
						.println("InterSSGChannel.getInputBuffersOnCore computeNode="
								+ SMPBackend.scheduler.getComputeNode(
										f.getWorkNode()).getCoreID());

				if (SMPBackend.scheduler.getComputeNode(f.getWorkNode())
						.equals(t)) {
					System.out
							.println("InterSSGChannel.getInputBuffersOnCore adding b");
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
		if (ssg.getOutputPort().getLinks().size() == 0) {
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
	@Override
	public String peekMethodName() {
		//return "dynamic_buffer_peek";
		String type = edge.getType().toString();
		return type + "_queue_peek";
	}

	/**
	 * @return
	 */
	@Override
	public String popManyMethodName() {
		//return "dynamic_buffer_pop_many";
		String type = edge.getType().toString();
		return type + "_queue_pop_many";
	}

	/**
	 * @return
	 */
	@Override
	public String popMethodName() {
		String type = edge.getType().toString();
		return type + "_queue_pop";
	}

	@Override
	public String pushMethodName() {
		String type = edge.getType().toString();
		return type + "_queue_push";
	}

}
