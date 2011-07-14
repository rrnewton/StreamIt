package at.dms.kjc;

import java.util.List;

import at.dms.kjc.lir.LIRFileReader;
import at.dms.kjc.lir.LIRFileWriter;
import at.dms.kjc.lir.LIRFunctionPointer;
import at.dms.kjc.lir.LIRMainFunction;
import at.dms.kjc.lir.LIRNode;
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
 * Implementation of an Attributed Visitor Design Pattern for KJC.
 *
 */
public interface SLIRAttributeVisitor<T> extends AttributeVisitor<T> {

    /**
     * SIR NODES.
     */

    /**
     * Visits an init statement.
     */
    T visitInitStatement(SIRInitStatement self,
                              SIRStream target);

    /**
     * Visits an interface table.
     */
    T visitInterfaceTable(SIRInterfaceTable self);
    
    /**
     * Visits a latency.
     */
    T visitLatency(SIRLatency self);

    /**
     * Visits a max latency.
     */
    T visitLatencyMax(SIRLatencyMax self);

    /**
     * Visits a latency range.
     */
    T visitLatencyRange(SIRLatencyRange self);

    /**
     * Visits a latency set.
     */
    T visitLatencySet(SIRLatencySet self);

    T visitCreatePortalExpression(SIRCreatePortal self);

    /**
     * Visits a message statement.
     */
    T visitMessageStatement(SIRMessageStatement self,
                                 JExpression portal,
                                 String iname,
                                 String ident,
                                 JExpression[] args,
                                 SIRLatency latency);

    /**
     * Visits a range expression.
     */
    T visitRangeExpression(SIRRangeExpression self);

    /**
     * Visits a dynamic token.
     */
    T visitDynamicToken(SIRDynamicToken self);
    
    /**
     * Visits a pop expression.
     */
    T visitIterationExpression(SIRIterationExpression self);

    /**
     * Visits a peek expression.
     */
    T visitPeekExpression(SIRPeekExpression self,
                               CType tapeType,
                               JExpression arg);

    /**
     * Visits a pop expression.
     */
    T visitPopExpression(SIRPopExpression self,
                              CType tapeType);

    /**
     * Visits a message-receiving portal.
     */
    T visitPortal(SIRPortal self);

    /**
     * Visits a print statement.
     */
    T visitPrintStatement(SIRPrintStatement self,
                               JExpression arg);

    /**
     * Visits a push expression.
     */
    T visitPushExpression(SIRPushExpression self,
                               CType tapeType,
                               JExpression arg);

    /**
     * Visits a register-receiver statement.
     */
    T visitRegReceiverStatement(SIRRegReceiverStatement self,
                                     JExpression portal,
                                     SIRStream receiver,
                                     JMethodDeclaration[] methods);

    /**
     * Visits a register-sender statement.
     */
    T visitRegSenderStatement(SIRRegSenderStatement self,
                                   String portal,
                                   SIRLatency latency);


    /**
     * Visit SIRMaker.
     */
    T visitMarker(SIRMarker self);

    /**
     * LIR NODES.
     */

    /**
     * Visits a function pointer.
     */
    T visitFunctionPointer(LIRFunctionPointer self,
                                String name);
    
    /**
     * Visits an LIR node.
     */
    T visitNode(LIRNode self);

    /**
     * Visits a child registration node.
     */
    T visitSetChild(LIRSetChild self,
                         JExpression streamContext,
                         String childType,
                         String childName);
    
    /**
     * Visits a decoder registration node.
     */
    T visitSetDecode(LIRSetDecode self,
                          JExpression streamContext,
                          LIRFunctionPointer fp);

    /**
     * Visits a feedback loop delay node.
     */
    T visitSetDelay(LIRSetDelay self,
                         JExpression data,
                         JExpression streamContext,
                         int delay,
                         CType type,
                         LIRFunctionPointer fp);
    
    /**
     * Visits an encoder registration node.
     */
    T visitSetEncode(LIRSetEncode self,
                          JExpression streamContext,
                          LIRFunctionPointer fp);

    /**
     * Visits a joiner-setting node.
     */
    T visitSetJoiner(LIRSetJoiner self,
                          JExpression streamContext,
                          SIRJoinType type,
                          int ways,
                          int[] weights);
    
    /**
     * Visits a peek-rate-setting node.
     */
    T visitSetPeek(LIRSetPeek self,
                        JExpression streamContext,
                        int peek);
    
    /**
     * Visits a pop-rate-setting node.
     */
    T visitSetPop(LIRSetPop self,
                       JExpression streamContext,
                       int pop);
    
    /**
     * Visits a push-rate-setting node.
     */
    T visitSetPush(LIRSetPush self,
                        JExpression streamContext,
                        int push);

    
    /**
     * Visits a file reader.
     */
    T visitFileReader(LIRFileReader self);

    /**
     * Visits a file writer.
     */
    T visitFileWriter(LIRFileWriter self);
    
    /**
     * Visits a splitter-setting node.
     */
    T visitSetSplitter(LIRSetSplitter self,
                            JExpression streamContext,
                            SIRSplitType type,
                            int ways,
                            int[] weights);
    
    /**
     * Visits a stream-type-setting node.
     */
    T visitSetStreamType(LIRSetStreamType self,
                              JExpression streamContext,
                              LIRStreamType streamType);
    
    /**
     * Visits a work-function-setting node.
     */
    T visitSetWork(LIRSetWork self,
                        JExpression streamContext,
                        LIRFunctionPointer fn);

    /**
     * Visits a tape registerer.
     */
    T visitSetTape(LIRSetTape self,
                        JExpression streamContext,
                        JExpression srcStruct,
                        JExpression dstStruct,
                        CType type,
                        int size);

    /**
     * Visits a main function contents.
     */
    T visitMainFunction(LIRMainFunction self,
                             String typeName,
                             LIRFunctionPointer init,
                             List<JStatement> initStatements);


    /**
     * Visits a set body of feedback loop.
     */
    T visitSetBodyOfFeedback(LIRSetBodyOfFeedback self,
                                  JExpression streamContext,
                                  JExpression childContext,
                                  CType inputType,
                                  CType outputType,
                                  int inputSize,
                                  int outputSize);

    /**
     * Visits a set loop of feedback loop.
     */
    T visitSetLoopOfFeedback(LIRSetLoopOfFeedback self,
                                  JExpression streamContext,
                                  JExpression childContext,
                                  CType inputType,
                                  CType outputType,
                                  int inputSize,
                                  int outputSize);

    /**
     * Visits a set a parallel stream.
     */
    T visitSetParallelStream(LIRSetParallelStream self,
                                  JExpression streamContext,
                                  JExpression childContext,
                                  int position,
                                  CType inputType,
                                  CType outputType,
                                  int inputSize,
                                  int outputSize);
    /**
     * Visits a vector literal
     */
    T visitVectorLiteral(JVectorLiteral self, JLiteral scalar);
}
