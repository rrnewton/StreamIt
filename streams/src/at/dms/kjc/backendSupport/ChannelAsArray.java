package at.dms.kjc.backendSupport;

import java.util.LinkedList;
import java.util.List;

import at.dms.kjc.CClassType;
import at.dms.kjc.CStdType;
import at.dms.kjc.CType;
import at.dms.kjc.JAddExpression;
import at.dms.kjc.JArrayAccessExpression;
import at.dms.kjc.JAssignmentExpression;
import at.dms.kjc.JBlock;
import at.dms.kjc.JCompoundAssignmentExpression;
import at.dms.kjc.JEmittedTextExpression;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JFieldAccessExpression;
import at.dms.kjc.JFormalParameter;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JLocalVariableExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JPostfixExpression;
import at.dms.kjc.JReturnStatement;
import at.dms.kjc.JStatement;
import at.dms.kjc.JVariableDeclarationStatement;
import at.dms.kjc.JVariableDefinition;
import at.dms.kjc.common.CommonUtils;
import at.dms.kjc.slir.IntraSSGEdge;

/**
 * Channel implementation as an array.
 * This implementation should work when there is no need of a copy-down or circular buffer.
 * It can be a superclass for copy-down array channels or circular buffer array channels.
 * 
 * TODO: missing support for multi-buffering, rotating buffers
 * TODO: missing support for arrays passed over channels.
 * @author dimock
 *
 */
public class ChannelAsArray extends IntraSSGChannel {

    /** array size in elements */
    protected int bufSize;
    /** type of array: array of element type */
    protected CType bufType;
    /** array name */
    protected String bufName;
    /** name of variable containing head of array offset */
    protected String headName;
    /** name of variable containing tail of array offset */
    protected String tailName;
    /** definition for array */
    protected JVariableDefinition bufDefn;
    /** definition for head */
    protected JVariableDefinition headDefn;
    /** definition for tail */
    protected JVariableDefinition tailDefn;
    /** reference to whole array, prefix to element access */
    protected JExpression bufPrefix;
    /** reference to head */
    protected JExpression head;
    /** reference to tail */
    protected JExpression tail;
    
    /** Create an array reference given an offset */   
    protected JArrayAccessExpression bufRef(JExpression offset) {
        return new JArrayAccessExpression(bufPrefix,offset);
    }
    
    /** Create statement zeroing out head */
    protected JStatement zeroOutHead() {
        return new JExpressionStatement(
                        new JAssignmentExpression(head, new JIntLiteral(0)));
    }
    
    /** Create statement zeroing out tail */
    protected JStatement zeroOutTail() {
        return new JExpressionStatement(
                new JAssignmentExpression(tail, new JIntLiteral(0)));
    }
    
    /** Actual Buffer size: use for diagnostic info only. */
    public int getBufSize() {
        return bufSize;
    }
    /**
     * Make a new Channel or return an already-made channel.
     * @param edge     The edge that this channel implements.
     * @param other    The channel that this delegates to.
     * @return A channel for this edge, that 
     */
    public static ChannelAsArray getChannel(IntraSSGEdge edge) {
        IntraSSGChannel oldChan = IntraSSGChannel.bufferStore.get(edge);
        if (oldChan == null) {
            ChannelAsArray chan = new ChannelAsArray(edge);
            IntraSSGChannel.bufferStore.put(edge, chan);
            return chan;
       } else {
            assert oldChan instanceof ChannelAsArray; 
            return (ChannelAsArray)oldChan;
        }
    }

    /** Constructor 
     * @param edge should give enough information (indirectly) to calculate buffer size
     */
    public ChannelAsArray(IntraSSGEdge edge) {
        super(edge);
        bufName = this.getIdent() + "buf";
        headName = this.getIdent() + "head";
        tailName = this.getIdent() + "tail";
        bufSize = BufferSize.calculateSize(edge);
        bufDefn = CommonUtils.makeArrayVariableDefn(bufSize,edge.getType(),bufName);
        headDefn = new JVariableDefinition(null,
                at.dms.kjc.Constants.ACC_STATIC,
                CStdType.Integer, headName, null);
        tailDefn = new JVariableDefinition(null,
                at.dms.kjc.Constants.ACC_STATIC,
                CStdType.Integer, tailName, null);
        bufPrefix = new JFieldAccessExpression(bufName);
        bufPrefix.setType(edge.getType());
        head = new JFieldAccessExpression(headName);
        head.setType(CStdType.Integer);
        tail = new JFieldAccessExpression(tailName);
        tail.setType(CStdType.Integer);
    }
    
    /** input_type pop(). */
    public JMethodDeclaration popMethod() {
        JBlock body = new JBlock();
        JMethodDeclaration retval = new JMethodDeclaration(
                null,
                /*at.dms.kjc.Constants.ACC_PUBLIC | at.dms.kjc.Constants.ACC_STATIC |*/ at.dms.kjc.Constants.ACC_INLINE,
                edge.getType(),
                popMethodName(),
                new JFormalParameter[0],
                CClassType.EMPTY,
                body, null, null);
        body.addStatement(
        new JReturnStatement(null,
                bufRef(new JPostfixExpression(at.dms.kjc.Constants.OPE_POSTINC, tail)),null));
        return retval;
    }

    /** void pop(int N). */
    public JMethodDeclaration popManyMethod() {
        String parameterName = "__n";
        JFormalParameter n = new JFormalParameter(
                CStdType.Integer,
                parameterName);
        JLocalVariableExpression nRef = new JLocalVariableExpression(n);
        JBlock body = new JBlock();
        JMethodDeclaration retval = new JMethodDeclaration(
                null,
                /*at.dms.kjc.Constants.ACC_PUBLIC | at.dms.kjc.Constants.ACC_STATIC |*/ at.dms.kjc.Constants.ACC_INLINE,
                CStdType.Void,
                popManyMethodName(),
                new JFormalParameter[]{n},
                CClassType.EMPTY,
                body, null, null);
        body.addStatement(
        new JExpressionStatement(new JCompoundAssignmentExpression(null,
                at.dms.kjc.Constants.OPE_PLUS,
                tail, nRef)));
        return retval;
    }
    
    /** void pop(input_type val)  generally assign if val is not an array, else memcpy */
    public JMethodDeclaration assignFromPopMethod() {
        String parameterName = "__val";
        JFormalParameter val = new JFormalParameter(
                CStdType.Integer,
                parameterName);
        JLocalVariableExpression valRef = new JLocalVariableExpression(val);
        JBlock body = new JBlock();
        JMethodDeclaration retval = new JMethodDeclaration(
                null,
                /*at.dms.kjc.Constants.ACC_PUBLIC | at.dms.kjc.Constants.ACC_STATIC |*/ at.dms.kjc.Constants.ACC_INLINE,
                CStdType.Void,
                assignFromPopMethodName(),
                new JFormalParameter[]{val},
                CClassType.EMPTY,
                body, null, null);
        body.addStatement(
                new JExpressionStatement(
                        new JEmittedTextExpression(
                                "/* assignFromPopMethod not yet implemented */")));
        return retval;
    }
    
    /** input_type peek(int offset) */
    public JMethodDeclaration peekMethod() {
        String parameterName = "__offset";
        JFormalParameter offset = new JFormalParameter(
                CStdType.Integer,
                parameterName);
        JLocalVariableExpression offsetRef = new JLocalVariableExpression(offset);
        JBlock body = new JBlock();
        JMethodDeclaration retval = new JMethodDeclaration(
                null,
                /*at.dms.kjc.Constants.ACC_PUBLIC | at.dms.kjc.Constants.ACC_STATIC |*/ at.dms.kjc.Constants.ACC_INLINE,
                edge.getType(),
                peekMethodName(),
                new JFormalParameter[]{offset},
                CClassType.EMPTY,
                body, null, null);
        body.addStatement(
        new JReturnStatement(null,
                bufRef(new JAddExpression(tail, offsetRef)),null));
        return retval;
    }

    /** void peek(input_type val, int offset)  generally assign if val is not an array, else memcpy */
    public JMethodDeclaration assignFromPeekMethod() {
        String valName = "__val";
        JFormalParameter val = new JFormalParameter(
                CStdType.Integer,
                valName);
        String offsetName = "__offset";
        JFormalParameter offset = new JFormalParameter(
                CStdType.Integer,
                offsetName);
        JBlock body = new JBlock();
        JMethodDeclaration retval = new JMethodDeclaration(
                null,
                /*at.dms.kjc.Constants.ACC_PUBLIC | at.dms.kjc.Constants.ACC_STATIC |*/ at.dms.kjc.Constants.ACC_INLINE,
                CStdType.Void,
                assignFromPeekMethodName(),
                new JFormalParameter[]{val,offset},
                CClassType.EMPTY,
                body, null, null);
         body.addStatement(
                new JExpressionStatement(
                        new JEmittedTextExpression(
                                "/* assignFromPeekMethod not yet implemented */")));
        return retval;
    }

   /** void push(output_type val) */
    public JMethodDeclaration pushMethod() {
        String valName = "__val";
        JFormalParameter val = new JFormalParameter(
                edge.getType(),
                valName);
        JLocalVariableExpression valRef = new JLocalVariableExpression(val);
        JBlock body = new JBlock();
        JMethodDeclaration retval = new JMethodDeclaration(
                null,
                /*at.dms.kjc.Constants.ACC_PUBLIC | at.dms.kjc.Constants.ACC_STATIC |*/ at.dms.kjc.Constants.ACC_INLINE,
                CStdType.Void,
                pushMethodName(),
                new JFormalParameter[]{val},
                CClassType.EMPTY,
                body, null, null);
        body.addStatement(
        new JExpressionStatement(new JAssignmentExpression(
                bufRef(new JPostfixExpression(at.dms.kjc.Constants.OPE_POSTINC,
                        head)),
                valRef)));
        return retval;
     }
    
    /** Statements for beginning of init() on read (downstream) end of buffer */
    public List<JStatement> beginInitRead() {
        List<JStatement> retval = new LinkedList<JStatement>();
        retval.add(zeroOutTail());
        return retval; 
    }

    /** Statements for end of init() on read (downstream) end of buffer */
    public List<JStatement> endInitRead() {
        return new LinkedList<JStatement>(); 
    }

    /** Statements for beginning of init() on write (upstream) end of buffer */
    public List<JStatement> beginInitWrite() {
        List<JStatement> retval = new LinkedList<JStatement>();
        retval.add(zeroOutHead());
        return retval; 
    }

    /** Statements for end of init() on write (upstream) end of buffer */
    public List<JStatement> endInitWrite() {
        return new LinkedList<JStatement>(); 
    }
    
    /** Statements for beginning of steady state iteration on read (downstream) end of buffer */
    public List<JStatement> beginSteadyRead() {
        List<JStatement> retval = new LinkedList<JStatement>();
        retval.add(zeroOutTail());
        return retval; 
    }

    /** Statements for end of steady state iteration on read (downstream) end of buffer */
    public List<JStatement> endSteadyRead() {
        return new LinkedList<JStatement>(); 
    }

    /** Statements for beginning of steady state iteration on write (upstream) end of buffer */
    public List<JStatement> beginSteadyWrite() {
        List<JStatement> retval = new LinkedList<JStatement>();
        retval.add(zeroOutHead());
        return retval; 
    }

    /** Statements for end of steady state iteration on write (upstream) end of buffer */
    public List<JStatement> endSteadyWrite() {
        return new LinkedList<JStatement>(); 
    }
    
    /** Statements for beginning of work function.
     * May be more convenient than at top of steady state if work function iterated. */
    public List<JStatement> topOfWorkSteadyRead() {
        return new LinkedList<JStatement>(); 
    }
    
    /** Statements for beginning of work function.
     * May be more convenient than at top of steady state if work function iterated. */
    public List<JStatement> topOfWorkSteadyWrite() {
        return new LinkedList<JStatement>(); 
    }
 
    /** Statements for data declaration in .h file */
    public List<JStatement> dataDeclsH() {
        return new LinkedList<JStatement>();
    }
    
    /** Statements for data declaration at top of .c / .cpp file */
    public List<JStatement> dataDecls() {
        JStatement arrayDecl = new JVariableDeclarationStatement(bufDefn); 
        List<JStatement> retval = new LinkedList<JStatement>();
        retval.add(arrayDecl);
        return retval;
    }
    
    /** Statements for extern declarations needed for read 
     * in steady state but at global scope in .c / .cpp */
    public List<JStatement> readDeclsExtern() {
        return new LinkedList<JStatement>();
    }   
    
    /** Statements for other declarations needed for read  
     * in steady state but at file scope in .c / .cpp */
    public List<JStatement> readDecls() {
        JStatement headDecl = new JVariableDeclarationStatement(tailDefn);
        List<JStatement> retval = new LinkedList<JStatement>();
        retval.add(headDecl);
        return retval;
    }   
    
    
    /** Statements for extern declarations needed for write 
     * in steady state but at global scope in .c / .cpp */
    public List<JStatement> writeDeclsExtern() {
        return new LinkedList<JStatement>();
    }   
    
    /** Statements for other declarations needed for write
     * in steady state but at file scope in .c / .cpp */
    public List<JStatement> writeDecls() {
        JStatement tailDecl = new JVariableDeclarationStatement(headDefn);
        List<JStatement> retval = new LinkedList<JStatement>();
        retval.add(tailDecl);
        return retval;
    }   

}

