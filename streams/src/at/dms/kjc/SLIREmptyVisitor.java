/*
 * LIRVisitor.java: visit StreaMIT Low IR nodes
 * $Id: SLIREmptyVisitor.java,v 1.22 2006-10-27 20:48:55 dimock Exp $
 */

package at.dms.kjc;

import java.util.List;

import at.dms.kjc.lir.LIRFileReader;
import at.dms.kjc.lir.LIRFileWriter;
import at.dms.kjc.lir.LIRFunctionPointer;
import at.dms.kjc.lir.LIRIdentity;
import at.dms.kjc.lir.LIRMainFunction;
import at.dms.kjc.lir.LIRNode;
import at.dms.kjc.lir.LIRRegisterReceiver;
import at.dms.kjc.lir.LIRSetBodyOfFeedback;
import at.dms.kjc.lir.LIRSetChild;
import at.dms.kjc.lir.LIRSetDecode;
import at.dms.kjc.lir.LIRSetDelay;
import at.dms.kjc.lir.LIRSetEncode;
import at.dms.kjc.lir.LIRSetJoiner;
import at.dms.kjc.lir.LIRSetLoopOfFeedback;
import at.dms.kjc.lir.LIRSetParallelStream;
import at.dms.kjc.lir.LIRSetPeek;
import at.dms.kjc.lir.LIRSetPop;
import at.dms.kjc.lir.LIRSetPush;
import at.dms.kjc.lir.LIRSetSplitter;
import at.dms.kjc.lir.LIRSetStreamType;
import at.dms.kjc.lir.LIRSetTape;
import at.dms.kjc.lir.LIRSetWork;
import at.dms.kjc.lir.LIRStreamType;
import at.dms.kjc.lir.LIRWorkEntry;
import at.dms.kjc.lir.LIRWorkExit;
import at.dms.kjc.sir.InlineAssembly;
import at.dms.kjc.sir.SIRCreatePortal;
import at.dms.kjc.sir.SIRDynamicToken;
import at.dms.kjc.sir.SIRInitStatement;
import at.dms.kjc.sir.SIRInterfaceTable;
import at.dms.kjc.sir.SIRIterationExpression;
import at.dms.kjc.sir.SIRJoinType;
import at.dms.kjc.sir.SIRLatency;
import at.dms.kjc.sir.SIRLatencyMax;
import at.dms.kjc.sir.SIRLatencyRange;
import at.dms.kjc.sir.SIRLatencySet;
import at.dms.kjc.sir.SIRMarker;
import at.dms.kjc.sir.SIRMessageStatement;
import at.dms.kjc.sir.SIRPeekExpression;
import at.dms.kjc.sir.SIRPopExpression;
import at.dms.kjc.sir.SIRPortal;
import at.dms.kjc.sir.SIRPrintStatement;
import at.dms.kjc.sir.SIRPushExpression;
import at.dms.kjc.sir.SIRRangeExpression;
import at.dms.kjc.sir.SIRRegReceiverStatement;
import at.dms.kjc.sir.SIRRegSenderStatement;
import at.dms.kjc.sir.SIRSplitType;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.lowering.JVectorLiteral;

/**
 * This visitor is for visiting statement-level constructs in the
 * streamit IR.  It visits both high-level constructs like
 * SIRInitStatement that never appear in the LIR, as well as low-level
 * constructs like LIRSetPeek that never appear in the high IR.
 */
public class SLIREmptyVisitor extends KjcEmptyVisitor 
    implements Constants, SLIRVisitor
{

    /**
     * SIR NODES.
     */

    /**
     * Visits an init statement.
     */
    @Override
	public void visitInitStatement(SIRInitStatement self,
                                   SIRStream target) {
        for (int i=0; i<self.getArgs().size(); i++) {
            self.getArgs().get(i).accept(this);
        }
    }

    /**
     * Visits an interface table.
     */
    @Override
	public void visitInterfaceTable(SIRInterfaceTable self) {}

    /**
     * Visits a latency.
     */
    @Override
	public void visitLatency(SIRLatency self) {}

    /**
     * Visits a max latency.
     */
    @Override
	public void visitLatencyMax(SIRLatencyMax self) {}

    /**
     * Visits a latency range.
     */
    @Override
	public void visitLatencyRange(SIRLatencyRange self) {}

    /**
     * Visits a latency set.
     */
    @Override
	public void visitLatencySet(SIRLatencySet self) {}

    @Override
	public void visitCreatePortalExpression(SIRCreatePortal self) {}

    /**
     * Visits a message statement.
     */
    @Override
	public void visitMessageStatement(SIRMessageStatement self,
                                      JExpression portal,
                                      String iname,
                                      String ident,
                                      JExpression[] args,
                                      SIRLatency latency) {
        portal.accept(this);
        for (int i=0; i<args.length; i++) {
            args[i].accept(this);
        }
        latency.accept(this);
    }

    /**
     * Visits a range expression.
     */
    @Override
	public void visitRangeExpression(SIRRangeExpression self) {
        self.getMin().accept(this);
        self.getAve().accept(this);
        self.getMax().accept(this);
    }

    /**
     * Visits a dynamic token.
     */
    @Override
	public void visitDynamicToken(SIRDynamicToken self) {
    }
    
    /**
     * Visits an iteration count expression.
     */
	@Override
	public void visitIterationExpression(
			SIRIterationExpression sirIterationExpression) {
	}
    
    /**
     * Visits a peek expression.
     */
    @Override
	public void visitPeekExpression(SIRPeekExpression self,
                                    CType tapeType,
                                    JExpression arg) {
        arg.accept(this);
    }

    /**
     * Visits a pop expression.
     */
    @Override
	public void visitPopExpression(SIRPopExpression self,
                                   CType tapeType) {
    }

    /**
     * Visits a message-receiving portal.
     */
    @Override
	public void visitPortal(SIRPortal self) {
    }

    /**
     * Visits a print statement.
     */
    @Override
	public void visitPrintStatement(SIRPrintStatement self,
                                    JExpression arg) {
        arg.accept(this);
    }

    /**
     * Visits a push expression.
     */
    @Override
	public void visitPushExpression(SIRPushExpression self,
                                    CType tapeType,
                                    JExpression arg) {
        arg.accept(this);
    }

    /**
     * Visits a register-receiver statement.
     */
    @Override
	public void visitRegReceiverStatement(SIRRegReceiverStatement self,
                                          JExpression portal,
                                          SIRStream receiver,
                                          JMethodDeclaration[] methods) {
        portal.accept(this);
    }

    /**
     * Visits a register-sender statement.
     */
    @Override
	public void visitRegSenderStatement(SIRRegSenderStatement self,
                                        String portal,
                                        SIRLatency latency) {
        latency.accept(this);
    }


    /**
     * Visit SIRMarker.
     */
    @Override
	public void visitMarker(SIRMarker self) {
    }

    /**
     * LIR NODES.
     */

    /**
     * Visits a function pointer.
     */
    @Override
	public void visitFunctionPointer(LIRFunctionPointer self,
                                     String name) {
    }
    
    /**
     * Visits an LIR node.
     */
    @Override
	public void visitNode(LIRNode self) {}

    /**
     * Visits an LIR register-receiver statement.
     */
    @Override
	public void visitRegisterReceiver(LIRRegisterReceiver self,
                                      JExpression streamContext,
                                      SIRPortal portal,
                                      String childName,
                                      SIRInterfaceTable itable) {
        streamContext.accept(this);
        portal.accept(this);
        itable.accept(this);
    }

    /**
     * Visits a child registration node.
     */
    @Override
	public void visitSetChild(LIRSetChild self,
                              JExpression streamContext,
                              String childType,
                              String childName) {
        streamContext.accept(this);
    }
    
    /**
     * Visits a decoder registration node.
     */
    @Override
	public void visitSetDecode(LIRSetDecode self,
                               JExpression streamContext,
                               LIRFunctionPointer fp) {
        streamContext.accept(this);
        fp.accept(this);
    }

    /**
     * Visits a feedback loop delay node.
     */
    @Override
	public void visitSetDelay(LIRSetDelay self,
                              JExpression data,
                              JExpression streamContext,
                              int delay,
                              CType type,
                              LIRFunctionPointer fp) {
        data.accept(this);
        streamContext.accept(this);
        fp.accept(this);
    }
    
    /**
     * Visits a file reader.
     */
    @Override
	public void visitFileReader(LIRFileReader self) {
        self.getStreamContext().accept(this);
    }
    
    /**
     * Visits a file writer.
     */
    @Override
	public void visitFileWriter(LIRFileWriter self) {
        self.getStreamContext().accept(this);
    }

    /**
     * Visits an identity creator.
     */
    @Override
	public void visitIdentity(LIRIdentity self) {
        self.getStreamContext().accept(this);
    }
    
    /**
     * Visits an encoder registration node.
     */
    @Override
	public void visitSetEncode(LIRSetEncode self,
                               JExpression streamContext,
                               LIRFunctionPointer fp) {
        streamContext.accept(this);
        fp.accept(this);
    }

    /**
     * Visits a joiner-setting node.
     */
    @Override
	public void visitSetJoiner(LIRSetJoiner self,
                               JExpression streamContext,
                               SIRJoinType type,
                               int ways,
                               int[] weights) {
        streamContext.accept(this);
    }
    
    /**
     * Visits a peek-rate-setting node.
     */
    @Override
	public void visitSetPeek(LIRSetPeek self,
                             JExpression streamContext,
                             int peek) {
        streamContext.accept(this);
    }
    
    /**
     * Visits a pop-rate-setting node.
     */
    @Override
	public void visitSetPop(LIRSetPop self,
                            JExpression streamContext,
                            int pop) {
        streamContext.accept(this);
    }
    
    /**
     * Visits a push-rate-setting node.
     */
    @Override
	public void visitSetPush(LIRSetPush self,
                             JExpression streamContext,
                             int push) {
        streamContext.accept(this);
    }

    /**
     * Visits a splitter-setting node.
     */
    @Override
	public void visitSetSplitter(LIRSetSplitter self,
                                 JExpression streamContext,
                                 SIRSplitType type,
                                 int ways,
                                 int[] weights) {
        streamContext.accept(this);
    }
    
    /**
     * Visits a stream-type-setting node.
     */
    @Override
	public void visitSetStreamType(LIRSetStreamType self,
                                   JExpression streamContext,
                                   LIRStreamType streamType) {
        streamContext.accept(this);
    }
    
    /**
     * Visits a work-function-setting node.
     */
    @Override
	public void visitSetWork(LIRSetWork self,
                             JExpression streamContext,
                             LIRFunctionPointer fn) {
        streamContext.accept(this);
        fn.accept(this);
    }

    /**
     * Visits a tape registerer.
     */
    @Override
	public void visitSetTape(LIRSetTape self,
                             JExpression streamContext,
                             JExpression srcStruct,
                             JExpression dstStruct,
                             CType type,
                             int size) {
        streamContext.accept(this);
        srcStruct.accept(this);
        dstStruct.accept(this);
    }

    /**
     * Visits a main function contents.
     */
    @Override
	public void visitMainFunction(LIRMainFunction self,
                                  String typeName,
                                  LIRFunctionPointer init,
                                  List<JStatement> initStatements) {
        init.accept(this);
        for (int i=0; i<initStatements.size(); i++) {
            initStatements.get(i).accept(this);
        }
    }


    /**
     * Visits a set body of feedback loop.
     */
    @Override
	public void visitSetBodyOfFeedback(LIRSetBodyOfFeedback self,
                                       JExpression streamContext,
                                       JExpression childContext,
                                       CType inputType,
                                       CType outputType,
                                       int inputSize,
                                       int outputSize) {
        streamContext.accept(this);
        childContext.accept(this);
    }


    /**
     * Visits a set loop of feedback loop.
     */
    @Override
	public void visitSetLoopOfFeedback(LIRSetLoopOfFeedback self,
                                       JExpression streamContext,
                                       JExpression childContext,
                                       CType inputType,
                                       CType outputType,
                                       int inputSize,
                                       int outputSize) {
        streamContext.accept(this);
        childContext.accept(this);
    }

    /**
     * Visits a set a parallel stream.
     */
    @Override
	public void visitSetParallelStream(LIRSetParallelStream self,
                                       JExpression streamContext,
                                       JExpression childContext,
                                       int position,
                                       CType inputType,
                                       CType outputType,
                                       int inputSize,
                                       int outputSize) {
        streamContext.accept(this);
        childContext.accept(this);
    }

    /**
     * Visits a work function entry.
     */
    @Override
	public void visitWorkEntry(LIRWorkEntry self)
    {
        self.getStreamContext().accept(this);
    }

    /**
     * Visits a work function exit.
     */
    @Override
	public void visitWorkExit(LIRWorkExit self)
    {
        self.getStreamContext().accept(this);
    }

    /**
     * Visits InlineAssembly
     */
    public void visitInlineAssembly(InlineAssembly self,String[] asm,String[] input,String[] clobber) {}
    
    /**
     * Visits a vector literal
     */
    @Override
	public void visitVectorLiteral(JVectorLiteral self, JLiteral scalar) {
        scalar.accept(this);
    }

}

