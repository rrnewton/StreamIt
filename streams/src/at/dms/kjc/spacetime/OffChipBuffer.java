package at.dms.kjc.spacetime;

import at.dms.util.Utils;
import java.util.Vector;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import at.dms.kjc.*;

/**
 * This abstract class represents a buffer in the partitioner slice graph.  A 
 * buffer appears between slices and inside slices between the input node and the first
 * filter and between the last filter and the output node.  
 * 
 * @author mgordon
 *
 */
public abstract class OffChipBuffer {
    /** The sending or receiving tile*/
    protected RawTile owner;
    /** unique ident for the buffer */
    protected String ident;
    protected static int unique_id;
    /** the store for all OffChipBuffers, indexed by src, dest */
    protected static HashMap bufferStore;
    /** the size of the buffer in the steady stage */ 
    protected Address sizeSteady;
    /** the type of the buffer */ 
    protected CType type;
    /** the dram that we are reading/writing */
    protected StreamingDram dram;
    /** the source slice (trace) */  
    protected TraceNode source;
    /** the destination slice (trace) */
    protected TraceNode dest;
    /** true if this buffer uses static net */
    protected boolean staticNet;
    /** the rotation length of this buffer for software pipelining **/
    protected int rotationLength;
    
    static {
        unique_id = 0;
        bufferStore = new HashMap();
    }

    protected OffChipBuffer(TraceNode src, TraceNode dst) {
        rotationLength = 1;
        source = src;
        dest = dst;

        ident = "__buf_" + /* owner.getIODevice().getPort() + */"_" + unique_id
            + "__";
        unique_id++;
        setType();
    }
    
    /**
     * @return Returns true if this buffer uses staticNet.
     */
    public boolean isStaticNet() {
        return staticNet;
    }

    /**
     * @param staticNet The staticNet to set.
     */
    public void setStaticNet(boolean staticNet) {
        this.staticNet = staticNet;
    }

  
    
    public abstract boolean redundant();

    /**
     * if this buffer is redundant return the first upstream buffer that is not
     * redundant, return null if this is a input->filter buffer with no input or
     * a filter->output buffer with no output
     */
    public abstract OffChipBuffer getNonRedundant();

    // return true if the inputtracenode does anything necessary
    public static boolean unnecessary(InputTraceNode input) {
        if (input.noInputs())
            return true;
        if (input.oneInput()
            && (InterTraceBuffer.getBuffer(input.getSingleEdge()).getDRAM() == IntraTraceBuffer
                .getBuffer(input, (FilterTraceNode) input.getNext())
                .getDRAM()))
            return true;
        return false;
    }

    // return true if outputtracenode does anything
    public static boolean unnecessary(OutputTraceNode output) {
        if (output.noOutputs())
            return true;
        if (output.oneOutput()
            && (IntraTraceBuffer.getBuffer(
                                           (FilterTraceNode) output.getPrevious(), output)
                .getDRAM() == InterTraceBuffer.getBuffer(
                                                         output.getSingleEdge()).getDRAM()))
            return true;
        return false;
    }

    public void setDRAM(StreamingDram DRAM) {
        // assert !redundant() : "calling setDRAM() on redundant buffer";
        this.dram = DRAM;
        if (source.isOutputTrace() && dest.isInputTrace() && redundant())
            SpaceTimeBackend.println("*Redundant: " + this.toString());

    }

    public boolean isAssigned() {
        return dram != null;
    }

    public StreamingDram getDRAM() {
        assert dram != null : "need to assign buffer to streaming dram "
            + this.toString();
        // assert !redundant() : "calling getDRAM() on redundant buffer";
        return dram;
    }

    /** 
     * @param i
     * @return The string for the InterTraceBuffer of rotation i.  
     * It just return getIdent() for IntraTraceBuffers.
     */
    public String getIdent(int i) {
        assert !redundant() : this.toString() + " is redundant";
        assert i < rotationLength : "Trying to use a buffer rotation length that is too large";
        return ident + "_" + i;
    }
    
    public String getIdent() {
        assert !redundant() : this.toString() + " is redundant";
        return ident;
    }

    public String getIdentPrefix() {
        assert !redundant();
        return ident;
    }

    public CType getType() {
        return type;
    }

    protected abstract void setType();

    // return of the buffers of this stream program
    public static Set getBuffers() {
        HashSet set = new HashSet();
        Iterator sources = bufferStore.keySet().iterator();
        while (sources.hasNext()) {
            set.add(bufferStore.get(sources.next()));
        }
        return set;
    }

    public Address getSize() {
        return sizeSteady;
    }
    
    public int getRotationLength() {
        return rotationLength;
    }
    
    abstract protected void calculateSize();

    /**
     * return the neighboring tile of the dram this buffer is assigned to
     */
    public RawTile getOwner() {
        assert (dram != null) : "owner not set yet";
        return dram.getNeighboringTile();
    }

    public String toString() {
        return source + "->" + dest + "[" + dram + "]";
    }

    public TraceNode getSource() {
        return source;
    }

    public TraceNode getDest() {
        return dest;
    }

    public boolean isIntraTrace() {
        return (this instanceof IntraTraceBuffer);
    }

    public boolean isInterTrace() {
        return (this instanceof InterTraceBuffer);
    }
    
    /**
     * Iterate over all the buffers and set the rotation length of each buffer
     * based on the prime pump schedule and the multiplicity difference between the source node
     * and the dest node.
     * 
     * @param spaceTime The SpaceTimeSchedule
     */
    public static void setRotationLengths(SpaceTimeSchedule spaceTime) {
        Iterator buffers = getBuffers().iterator();
        //iterate over the buffers and communicate each buffer
        //address from its declaring tile to the tile neighboring
        //the dram it is assigned to
        while (buffers.hasNext()) {
            OffChipBuffer buffer = (OffChipBuffer)buffers.next();
            //set the rotation length for the buffer
            if (buffer.isInterTrace())
                setRotationLength(spaceTime, (InterTraceBuffer)buffer);
        }
    }
    
    /**
     * Set the rotation length of the buffer based on the multiplicities 
     * of the source trace and the dest trace in the prime pump schedule and add one
     * so we can double buffer also!
     * 
     * @param buffer
     */
    private static void setRotationLength(SpaceTimeSchedule spaceTimeSchedule, InterTraceBuffer buffer) {
        int sourceMult = spaceTimeSchedule.getPrimePumpMult(buffer.getSource().getParent());
        int destMult = spaceTimeSchedule.getPrimePumpMult(buffer.getDest().getParent());
        //fix for file readers and writers!!!!
        
        int length = 0;
        
        //if we have either of these cases we are not rotating this buffer
        //and it a probably a buffer that will never be generated because it is
        //a connected to a file reader or a file writer...
        if (sourceMult < destMult || sourceMult == destMult)
            length = 0;
        else 
            length = sourceMult - destMult + 1; 
      
        buffer.rotationLength = length;
        
        //this is buffer is redundant, meaning it is just a copy of its its upstream 
        //output trace node, then we have to set the rotation for its upstream
        //output trace node!!
        if (length > 1 && buffer.redundant()) {
            System.out.println("Setting upstream rotation length " + length);
            IntraTraceBuffer upstream = IntraTraceBuffer.getBuffer((FilterTraceNode)buffer.source.getPrevious(), 
                    (OutputTraceNode)buffer.source);
            upstream.rotationLength = length;
        }
    }
}
