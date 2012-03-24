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
import at.dms.kjc.KjcOptions;
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

    /* return all the input buffers of the file readers of this application */
    public static Set<InterSSGChannel> getFileReaderBuffers() {
        return fileReaderBuffers;
    }

    /** stores InputRotatingBuffers for file writers */
    protected static HashSet<InterSSGChannel> fileWriterBuffers;

    /** stores InputRotatingBuffers for file readers */
    protected static HashSet<InterSSGChannel> fileReaderBuffers;

    /** maps each WorkNode to Input/OutputRotatingBuffers */
    protected static HashMap<WorkNode, InterSSGChannel> inputBuffers;
    protected static HashMap<WorkNode, InterSSGChannel> outputBuffers;

    protected static HashMap<String, String> bufferVariables;
    protected static HashMap<String, String> createMethods;

    /* src -> dst -> InterSSGChannel */
    public static HashMap<WorkNode, HashMap<WorkNode, InterSSGChannel>> buffers;
    
    static {
        types = new HashSet<String>();        
        buffers = new HashMap<WorkNode, HashMap<WorkNode, InterSSGChannel>>();        
        inputBuffers = new HashMap<WorkNode, InterSSGChannel>();
        outputBuffers = new HashMap<WorkNode, InterSSGChannel>();
        fileWriterBuffers = new HashSet<InterSSGChannel>(); 
        fileReaderBuffers = new HashSet<InterSSGChannel>(); 
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
        for (InterSSGChannel channel : fileReaderBuffers) {		    
            String type = channel.getEdge().getType().toString();
            SMPBackend.dynamicQueueCodeGenerator.addSource(type);
        };
    }

    private static InterSSGChannel addChannelToBuffers(Filter src, Filter dst, InterSSGChannel channel) {
        HashMap<WorkNode, InterSSGChannel> key2;
        key2 = new HashMap<WorkNode, InterSSGChannel>();
        key2.put(dst.getWorkNode(), channel);
        buffers.put(src.getWorkNode(), key2);
        return channel;
    }
    
    private static InterSSGChannel addChannelIfNew(InterSSGEdge edge) {
        Filter src = edge.getSrc().getSSG().getLastFilter();
        Filter dst = edge.getDest().getSSG().getTopFilters()[0];
        
        HashMap<WorkNode, InterSSGChannel> key2 = buffers.get(src.getWorkNode());                
        if (key2 == null) {
            return addChannelToBuffers(src, dst, new InterSSGChannel(edge));
        }        
        InterSSGChannel value = key2.get(dst.getWorkNode());
        if (value == null) {
            return addChannelToBuffers(src, dst, new InterSSGChannel(edge));             
        }                    
        return value;
    }
    
    /**
     * 
     * @param streamGraph
     */
    private static void createInputBuffers(StreamGraph streamGraph) {
        for (StaticSubGraph srcSSG : streamGraph.getSSGs()) {
            OutputPort outputPort = srcSSG.getOutputPort();
            if (outputPort == null) {
                continue;
            }
            for (InterSSGEdge edge : outputPort.getLinks()) {
                Filter dstTop = edge.getDest().getSSG().getTopFilters()[0];
               
                InterSSGChannel channel;                
                if (KjcOptions.threadopt) {
                    channel = addChannelIfNew(edge);
                } else {
                    channel = new InterSSGChannel(edge);
                }
                                
                CType bufType = edge.getType();
                types.add(bufType.toString());               
                inputBuffers.put(dstTop.getWorkNode(), channel);                                                
                if (dstTop.getWorkNode().isFileOutput()) {
                    fileWriterBuffers.add(channel);
                }


                if (dstTop.getWorkNode().isFileOutput()) {
                    ProcessFileWriter.getAllocatingCore(dstTop.getWorkNode());
                }				

            }
        }
    }

    /**
     * 
     * @param streamGraph
     */
    private static void createOutputBuffers(StreamGraph streamGraph) {

        for (StaticSubGraph dstSSG : streamGraph.getSSGs()) {
            InputPort inputPort = dstSSG.getInputPort();
            if (inputPort == null) {
                continue;
            }
            for (InterSSGEdge edge : inputPort.getLinks()) {             
                InterSSGChannel channel;                
                if (KjcOptions.threadopt) {
                    channel = addChannelIfNew(edge);
                } else {
                    channel = new InterSSGChannel(edge);
                }
                                
                
                CType bufType = edge.getType();
                types.add(bufType.toString());
                Filter dstTop = dstSSG.getTopFilters()[0];
                outputBuffers.put(dstTop.getWorkNode(), channel);
                Filter srcTop = edge.getSrc().getSSG().getTopFilters()[0];

                if (srcTop.getWorkNode().isFileInput()) {                   
                    fileReaderBuffers.add(channel);
                }

                if (dstTop.getWorkNode().isFileOutput()) {
                    ProcessFileWriter.getAllocatingCore(dstTop.getWorkNode());
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
                        + SMPBackend.getComputeNode(
                                f.getWorkNode()).getCoreID());

                if (SMPBackend.getComputeNode(f.getWorkNode())
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
        
        if (KjcOptions.threadopt) {
            return addChannelIfNew(edge);
        }         
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

            if (SMPBackend.getComputeNode(top[0].getWorkNode())
                    .equals(t))
                set.add(b);
        }
        return set;
    }

    /** A unique identifier for each channel */
    private final int id;
    
    /** A static counter for providing the ids */
    private static int ChannelId = 0;
    
    /**
     * @param edge
     */
    protected InterSSGChannel(InterSSGEdge edge) {
        super(edge);
        id = ChannelId;
        ChannelId++;
    }

    /**
     * Get this channel
     * @return
     */
    public int getId() {
        return id;
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
        if (isSource()) { 
            return type + "_queue_peek_source";  
        }       
        return type + "_queue_peek";
    }

    /**
     * @return
     */
    @Override
    public String popManyMethodName() {
        //return "dynamic_buffer_pop_many";
        String type = edge.getType().toString();
        if (isSource()) { 
            return type + "_queue_pop_many_source";  
        }       
        return type + "_queue_pop_many";
    }

    private boolean isSource() {
        OutputPort outputPort = edge.getSrc();
        StaticSubGraph outputSSG = outputPort.getSSG();
        Filter[] filterGraph = outputSSG.getFilterGraph();
        if (filterGraph.length == 1) {        
            if (filterGraph[0].getWorkNode().isFileInput()) {
                return true;
            }                                                                     
        }          
        return false;
    }

    /**
     * @return
     */
    @Override
    public String popMethodName() {
        String type = edge.getType().toString();
        if (isSource()) { 
            return type + "_queue_pop_source"; 
        }
        return type + "_queue_pop";
    }

    @Override
    public String pushMethodName() {
        String type = edge.getType().toString();
        return type + "_queue_push";
    }

}
