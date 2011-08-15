package at.dms.kjc.backendSupport;

import at.dms.kjc.slir.IntraSSGEdge;

import java.util.*;

import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JStatement;
/**
 * A Channel with the single purpose of holding the name of a push() routine.
 * Such a channel is used to connect a filter to a splitter
 * when the splitter and the filter are laid out on the same ComputeNode.
 * 
 * @author dimock
 *
 */
public class UnbufferredPushChannel extends IntraSSGChannel {

    private String pushName;
    
    /**
     * Make a new Channel or return an already-made channel.
     * @param edge     The edge that this channel implements.
     * @param pushName  The name of the push routine that will be used.
     * @return A channel for the passed edge with a where pushMethodName() returns <b>pushName</b>.
     */
    public static UnbufferredPushChannel getChannel(IntraSSGEdge edge, String pushName) {
        IntraSSGChannel oldChan = IntraSSGChannel.bufferStore.get(edge);
        if (oldChan == null) {
            UnbufferredPushChannel chan = new UnbufferredPushChannel(edge, pushName);
            IntraSSGChannel.bufferStore.put(edge, chan);
            return chan;
       } else {
            assert oldChan instanceof UnbufferredPushChannel 
                && oldChan.popMethodName().equals(pushName);
            return (UnbufferredPushChannel)oldChan;
        }
    }
    
    private Collection<IntraSSGChannel> produceWriteHeadersFor = new LinkedList<IntraSSGChannel>();

    /** 
     * Add a channel to produce upstream (write) headers for:
     * @param c  a Channel connected to the splitter that this channel calls.
     */
    public void addChannelForHeaders(IntraSSGChannel c) {
        produceWriteHeadersFor.add(c);
    }
    
    private UnbufferredPushChannel(IntraSSGEdge edge, String pushName) {
        super(edge);
        this.pushName = pushName;
    }
    
    @Override
    public String pushMethodName() {
        return pushName;
    }
    
    public void updatePushMethodName(String pushName) {
        this.pushName = pushName;
    }
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#beginInitWrite()
     */
    @Override
    public List<JStatement> beginInitWrite() {
        LinkedList<JStatement> retval = new LinkedList<JStatement>();
        for (IntraSSGChannel c : produceWriteHeadersFor) {
            retval.addAll(c.beginInitWrite());
        }
        return retval;
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#endInitWrite()
     */
    @Override
    public List<JStatement> endInitWrite() {
        LinkedList<JStatement> retval = new LinkedList<JStatement>();
        for (IntraSSGChannel c : produceWriteHeadersFor) {
            retval.addAll(c.endInitWrite());
        }
        return retval;
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#beginSteadyWrite()
     */
    @Override
    public List<JStatement> beginSteadyWrite() {
        LinkedList<JStatement> retval = new LinkedList<JStatement>();
        for (IntraSSGChannel c : produceWriteHeadersFor) {
            retval.addAll(c.beginSteadyWrite());
        }
        return retval;
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#endSteadyWrite()
     */
    @Override
    public List<JStatement> endSteadyWrite() {
        LinkedList<JStatement> retval = new LinkedList<JStatement>();
        for (IntraSSGChannel c : produceWriteHeadersFor) {
            retval.addAll(c.endSteadyWrite());
        }
        return retval;
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#topOfWorkSteadyWrite()
     */
    @Override
    public List<JStatement> topOfWorkSteadyWrite() {
        LinkedList<JStatement> retval = new LinkedList<JStatement>();
        for (IntraSSGChannel c : produceWriteHeadersFor) {
            retval.addAll(c.topOfWorkSteadyWrite());
        }
        return retval;
    }
   
}
