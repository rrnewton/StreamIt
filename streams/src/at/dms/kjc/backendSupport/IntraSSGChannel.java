package at.dms.kjc.backendSupport;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import at.dms.kjc.CStdType;
import at.dms.kjc.CType;
import at.dms.kjc.JBlock;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JFormalParameter;
import at.dms.kjc.JLocalVariableExpression;
import at.dms.kjc.JMethodCallExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JStatement;
import at.dms.kjc.JVariableDefinition;
import at.dms.kjc.slir.InternalFilterNode;
import at.dms.kjc.slir.IntraSSGEdge;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.Util;
import at.dms.util.Utils;

/**
 * A IntraSSGChannel is an implementation of an IntraSSGEdge in a back end.
 * It refers to nodes in a slice graph like an edge does, but a Buffer also
 * contains code that a back end can emit to pass data between nodes in the slice graph.
 * 
 * @author dimock
 */
public class IntraSSGChannel extends Channel<IntraSSGEdge<InternalFilterNode, InternalFilterNode>> {
		
    /**
     * Technical note: a Buffer in a backend implements an Edge in a slice graph
     * This data structure uses an Edge to store source, destination, and type information.
     *
     * XXX fix this? is sharing edges with the sliceGraph
     * can see wanting to optimize buffers to remove useless edges
     * and wanting to change source and dest,
     * presumably without changing slice graph. 
     */
    //protected IntraSSGEdge theEdge;
    /** unique ident for the buffer */
    protected String ident;
    /** used to make sure ident is unique */
    protected int unique_id;
    private static int unique_id_generator;
    /** the store for all Buffers, indexed by edge.
     */
    protected static HashMap<IntraSSGEdge<InternalFilterNode, InternalFilterNode>, IntraSSGChannel> bufferStore;
    /** the rotation length of this buffer for software pipelining 
     * Includes any length from extraLength field. **/
    protected int rotationLength;
    /** set to 1 for double bufferring or higher if even more extra buffers desired */
    protected int extraCount;

    static {
        unique_id_generator = 0;
        bufferStore = new HashMap<IntraSSGEdge<InternalFilterNode, InternalFilterNode>, IntraSSGChannel>();
    }

    /**
     * Create a channel given an edge.
     * Subclasses should provide factories for their channel types.
     * @param edge
     */
    protected IntraSSGChannel(IntraSSGEdge<InternalFilterNode, InternalFilterNode> edge) {
        super(edge);
        edge.getType(); // side effect of catching error if source and dest types not the same
        unique_id = unique_id_generator++;
        ident = "__chan__" + unique_id + "__";
        rotationLength = 1;
    }
    
    /**
     * Create a buffer given src and dst SliceNode's
     * @param src
     * @param dst
     */
    protected IntraSSGChannel(InternalFilterNode src, InternalFilterNode dst) {
        this(Util.srcDstToEdge(src, dst, SchedulingPhase.STEADY));
    }
    
    
    /**
     * Determine whther a channel for an edge exists in our collection of channels.
     * @param edge  Edge that the channel should implement
     * @return an existing channel if there is one, else null.
     */
    public static IntraSSGChannel findChannel(IntraSSGEdge edge) {
        return bufferStore.get(edge);
    }

    /**
     * Add a Channel to our collection of channels.
     * @param c Channel to add to our collection of channels. 
     */
    public static void addChannel(IntraSSGChannel c) {
        bufferStore.put((IntraSSGEdge) c.edge, c);
    }
    
    /**
     * Reset the buffer store and create all number buffer objects.  
     * Used if one wants to munge the trace graph.
     */
    public static void reset() {
        unique_id_generator = 0;
        bufferStore = new HashMap<IntraSSGEdge<InternalFilterNode, InternalFilterNode>, IntraSSGChannel>();        
    }
    
    /**
     * For debugging.
     */
    public static void printBuffers() {
        for (IntraSSGChannel buf : bufferStore.values()) {
            System.out.println(buf);
        }
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#getIdent()
     */
    public String getIdent() {
        return ident;
    }
    
    /** @return of the buffers of this stream program */
    public static Collection<IntraSSGChannel> getBuffers() {
        return bufferStore.values();
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#getType()
     */
    public CType getType() {
        return edge.getType();
    }


    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#getExtraCount()
     */
    public int getExtraCount() {
        return extraCount;
    }
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#setExtralength(int)
     */
    public void setExtraCount(int extracount) {
        this.extraCount = extracount;
    }
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#getSource()
     */
    public InternalFilterNode getSource() {
        return edge.getSrc();
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#getDest()
     */
    public InternalFilterNode getDest() {
        return edge.getDest();
    }

    /**
     * Set rotation count: number of buffers needed during primePump phase.
     * Also adds in extracount to set up double-bufferring if desired.
     * @param sched BasicSpaceTimeSchedule gives primePump multiplicities.
     */
    public static void setRotationLengths(BasicSpaceTimeSchedule sched) {
        for (IntraSSGChannel buf : getBuffers()) {
            setRotationLength(buf, sched);
        }
    }
    
    /**
     * Set the rotation length of each buffer based on the multiplicities 
     * of the source trace and the dest trace in the prime pump schedule and add
     * in extraCount field to enable double bufferring if desired.
     * 
     * @param buffer
     */
    private static void setRotationLength(IntraSSGChannel buffer, BasicSpaceTimeSchedule spaceTimeSchedule) {
        int sourceMult = spaceTimeSchedule.getPrimePumpMult(buffer.getSource().getParent());
        int destMult = spaceTimeSchedule.getPrimePumpMult(buffer.getDest().getParent());

        // if source run more often than dest, then need extra space.
        if (sourceMult > destMult) {
            buffer.rotationLength = sourceMult - destMult + buffer.extraCount; 
        } else {
            buffer.rotationLength = buffer.extraCount;
        }
        
        //System.out.println("Setting rotation length: " + buffer + " " + length);
    }
    
    
    /* Lots of JBlock, JMethodDeclaration, ... here 
     * Set to empty values, since many do not need to be overridden in many backends. 
     */
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#popMethodName()
     */
    public String popMethodName() {
        return "__pop_" + unique_id;
    }
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#popMethod()
     */
    public JMethodDeclaration popMethod() {
        return null;
    }
    
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#popManyMethodName()
     */
    public String popManyMethodName() {
        return "__popN_" + unique_id;
    }
 
    JMethodDeclaration popManyCode = null;
    
    /**
     * Pop many items at once ignoring them.
     * Default method generated here to call popMethod() repeatedly.
     */
    public JMethodDeclaration popManyMethod() {
        if (popManyCode != null) {
            return popManyCode;
        }
        if (popMethod() == null) {
            return null;
        }
        
        String formalParamName = "n";
        CType formalParamType = CStdType.Integer;
        
        JVariableDefinition nPopsDef = new JVariableDefinition(formalParamType, formalParamName);
        JExpression nPops = new JLocalVariableExpression(nPopsDef);
        
        JVariableDefinition loopIndex = new JVariableDefinition(formalParamType, "i");
        
        JStatement popOne = new JExpressionStatement(
                new JMethodCallExpression(popMethodName(),new JExpression[0]));
        
        JBlock body = new JBlock();
        body.addStatement(Utils.makeForLoop(popOne, nPops, loopIndex));
        
        popManyCode = new JMethodDeclaration(CStdType.Void,
                popManyMethodName(),
                new JFormalParameter[]{new JFormalParameter(formalParamType, formalParamName)},
                body);
        return popManyCode;
     }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#assignFromPopMethodName()
     */
    public String assignFromPopMethodName() {
        return "__popv_" + unique_id;
    }
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#assignFromPopMethod()
     */
    public JMethodDeclaration assignFromPopMethod() {
        return null;
    }
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#peekMethodName()
     */
    public String peekMethodName() {
        return "__peek_" + unique_id;
    }
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#peekMethod()
     */
    public JMethodDeclaration peekMethod() {
        return null;
    }
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#assignFromPeekMethodName()
     */
    public String assignFromPeekMethodName() {
        return "__peekv_" + unique_id;
    }
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#assignFromPeekMethod()
     */
    public JMethodDeclaration assignFromPeekMethod() {
        return null;
    }
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#pushMethodName()
     */
    public String pushMethodName() {
        return "__push_" + unique_id;
    }
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#pushMethod()
     */
    public JMethodDeclaration pushMethod() {
        return null;
    }
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#beginInitRead()
     */
    public List<JStatement> beginInitRead() {
        return new LinkedList<JStatement>(); 
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#beginInitRead()
     */
    public List<JStatement> postPreworkInitRead() {
        return new LinkedList<JStatement>(); 
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#endInitRead()
     */
    public List<JStatement> endInitRead() {
        return new LinkedList<JStatement>(); 
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#beginInitWrite()
     */
    public List<JStatement> beginInitWrite() {
        return new LinkedList<JStatement>(); 
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#endInitWrite()
     */
    public List<JStatement> endInitWrite() {
        return new LinkedList<JStatement>(); 
    }
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#beginSteadyRead()
     */
    public List<JStatement> beginSteadyRead() {
        return new LinkedList<JStatement>(); 
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#endSteadyRead()
     */
    public List<JStatement> endSteadyRead() {
        return new LinkedList<JStatement>(); 
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#beginSteadyWrite()
     */
    public List<JStatement> beginSteadyWrite() {
        return new LinkedList<JStatement>(); 
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#endSteadyWrite()
     */
    public List<JStatement> endSteadyWrite() {
        return new LinkedList<JStatement>(); 
    }
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#topOfWorkSteadyRead()
     */
    public List<JStatement> topOfWorkSteadyRead() {
        return new LinkedList<JStatement>(); 
    }
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#topOfWorkSteadyWrite()
     */
    public List<JStatement> topOfWorkSteadyWrite() {
        return new LinkedList<JStatement>(); 
    }
 
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#dataDeclsH()
     */
    public List<JStatement> dataDeclsH() {
        return new LinkedList<JStatement>();
    }
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#dataDecls()
     */
    public List<JStatement> dataDecls() {
        return new LinkedList<JStatement>();
    }
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#readDeclsExtern()
     */
    public List<JStatement> readDeclsExtern() {
        return new LinkedList<JStatement>();
    }   
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#readDecls()
     */
    public List<JStatement> readDecls() {
        return new LinkedList<JStatement>();
    }   
    
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#writeDeclsExtern()
     */
    public List<JStatement> writeDeclsExtern() {
        return new LinkedList<JStatement>();
    }   
    
    /* (non-Javadoc)
     * @see at.dms.kjc.backendSupport.ChannelI#writeDecls()
     */
    public List<JStatement> writeDecls() {
        return new LinkedList<JStatement>();
    }   
    
}
