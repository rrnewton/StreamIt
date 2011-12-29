/*
 * Copyright (C) 1990-2001 DMS Decision Management Systems Ges.m.b.H.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

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
 * This is a visitor that just recurses into children at every node
 * and returns that node.  It can be extended to do some mutation at a
 * given node.
 */
public class SLIREmptyAttributeVisitor extends EmptyAttributeVisitor 
    implements SLIRAttributeVisitor<Object> {

    /**
     * SIR NODES.
     */

    /**
     * Visits an init statement.
     */
    @Override
	public Object visitInitStatement(SIRInitStatement self,
                                     SIRStream target) {
        for (int i=0; i<self.getArgs().size(); i++) {
            self.getArgs().get(i).accept(this);
        }
        return self;
    }

    /**
     * Visits an interface table.
     */
    @Override
	public Object visitInterfaceTable(SIRInterfaceTable self) { 
        return self;
    }

    /**
     * Visits a latency.
     */
    @Override
	public Object visitLatency(SIRLatency self) {
        return self;
    }

    /**
     * Visits a max latency.
     */
    @Override
	public Object visitLatencyMax(SIRLatencyMax self) {
        return self;
    }

    /**
     * Visits a latency range.
     */
    @Override
	public Object visitLatencyRange(SIRLatencyRange self) {
        return self;
    }

    /**
     * Visits a latency set.
     */
    @Override
	public Object visitLatencySet(SIRLatencySet self) {
        return self;
    }

    @Override
	public Object visitCreatePortalExpression(SIRCreatePortal self) {
        return self;
    }

    /**
     * Visits a message statement.
     */
    @Override
	public Object visitMessageStatement(SIRMessageStatement self,
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
        return self;
    }

    /**
     * Visits a range expression.
     */
    @Override
	public Object visitRangeExpression(SIRRangeExpression self) {
        self.getMin().accept(this);
        self.getAve().accept(this);
        self.getMax().accept(this);
        return self;
    }

    /**
     * Visits a dynamic token.
     */
    @Override
	public Object visitDynamicToken(SIRDynamicToken self) {
        return self;
    }

    /**
     * Visits a peek expression.
     */
    @Override
	public Object visitPeekExpression(SIRPeekExpression self,
                                      CType tapeType,
                                      JExpression arg) {
        arg.accept(this);
        return self;
    }

    /**
     * Visits a file reader.
     */
    @Override
	public Object visitFileReader(LIRFileReader self) {
        self.getStreamContext().accept(this);
        return self;
    }

    /**
     * Visits a file writer.
     */
    @Override
	public Object visitFileWriter(LIRFileWriter self) {
        self.getStreamContext().accept(this);
        return self;
    }
    

    /**
     * Visits an iteration expression.
     */
	@Override
	public Object visitIterationExpression(SIRIterationExpression self) {
		return self;
	}
    
    /**
     * Visits a pop expression.
     */
    @Override
	public Object visitPopExpression(SIRPopExpression self,
                                     CType tapeType) {
        return self;
    }

    /**
     * Visits a message-receiving portal.
     */
    @Override
	public Object visitPortal(SIRPortal self)
    {
        return self;
    }

    /**
     * Visits a print statement.
     */
    @Override
	public Object visitPrintStatement(SIRPrintStatement self,
                                      JExpression arg) {
        arg.accept(this);
        return self;
    }

    /**
     * Visits a push expression.
     */
    @Override
	public Object visitPushExpression(SIRPushExpression self,
                                      CType tapeType,
                                      JExpression arg) {
        arg.accept(this);
        return self;
    }

    /**
     * Visits a register-receiver statement.
     */
    @Override
	public Object visitRegReceiverStatement(SIRRegReceiverStatement self,
                                            JExpression portal,
                                            SIRStream receiver,
                                            JMethodDeclaration[] methods) {
        portal.accept(this);
        return self;
    }


    /**
     * Visits a vector literal
     */
    @Override
	public Object visitVectorLiteral(JVectorLiteral self, JLiteral scalar) {
        scalar.accept(this);
        return self;
    }

    /**
     * Visits a register-sender statement.
     */
    @Override
	public Object visitRegSenderStatement(SIRRegSenderStatement self,
                                          String portal,
                                          SIRLatency latency) {
        latency.accept(this);
        return self;
    }

    /**
     * Visit SIRMarker.
     */
    @Override
	public Object visitMarker(SIRMarker self) {
        return self;
    }

    /**
     * LIR NODES.
     */

    /**
     * Visits a function pointer.
     */
    @Override
	public Object visitFunctionPointer(LIRFunctionPointer self,
                                       String name) {
        return self;
    }
    
    /**
     * Visits an LIR node.
     */
    @Override
	public Object visitNode(LIRNode self) {
        return self;
    }

    /**
     * Visits a child registration node.
     */
    @Override
	public Object visitSetChild(LIRSetChild self,
                                JExpression streamContext,
                                String childType,
                                String childName) {
        streamContext.accept(this);
        return self;
    }
    
    /**
     * Visits a decoder registration node.
     */
    @Override
	public Object visitSetDecode(LIRSetDecode self,
                                 JExpression streamContext,
                                 LIRFunctionPointer fp) {
        streamContext.accept(this);
        fp.accept(this);
        return self;
    }

    /**
     * Visits a feedback loop delay node.
     */
    @Override
	public Object visitSetDelay(LIRSetDelay self,
                                JExpression data,
                                JExpression streamContext,
                                int delay,
                                CType type,
                                LIRFunctionPointer fp) {
        data.accept(this);
        streamContext.accept(this);
        fp.accept(this);
        return self;
    }
    
    /**
     * Visits an encoder registration node.
     */
    @Override
	public Object visitSetEncode(LIRSetEncode self,
                                 JExpression streamContext,
                                 LIRFunctionPointer fp) {
        streamContext.accept(this);
        fp.accept(this);
        return self;
    }

    /**
     * Visits a joiner-setting node.
     */
    @Override
	public Object visitSetJoiner(LIRSetJoiner self,
                                 JExpression streamContext,
                                 SIRJoinType type,
                                 int ways,
                                 int[] weights) {
        streamContext.accept(this);
        return self;
    }
    
    /**
     * Visits a peek-rate-setting node.
     */
    @Override
	public Object visitSetPeek(LIRSetPeek self,
                               JExpression streamContext,
                               int peek) {
        streamContext.accept(this);
        return self;
    }
    
    /**
     * Visits a pop-rate-setting node.
     */
    @Override
	public Object visitSetPop(LIRSetPop self,
                              JExpression streamContext,
                              int pop) {
        streamContext.accept(this);
        return self;
    }
    
    /**
     * Visits a push-rate-setting node.
     */
    @Override
	public Object visitSetPush(LIRSetPush self,
                               JExpression streamContext,
                               int push) {
        streamContext.accept(this);
        return self;
    }

    /**
     * Visits a splitter-setting node.
     */
    @Override
	public Object visitSetSplitter(LIRSetSplitter self,
                                   JExpression streamContext,
                                   SIRSplitType type,
                                   int ways,
                                   int[] weights) {
        streamContext.accept(this);
        return self;
    }
    
    /**
     * Visits a stream-type-setting node.
     */
    @Override
	public Object visitSetStreamType(LIRSetStreamType self,
                                     JExpression streamContext,
                                     LIRStreamType streamType) {
        streamContext.accept(this);
        return self;
    }
    
    /**
     * Visits a work-function-setting node.
     */
    @Override
	public Object visitSetWork(LIRSetWork self,
                               JExpression streamContext,
                               LIRFunctionPointer fn) {
        streamContext.accept(this);
        fn.accept(this);
        return self;
    }

    /**
     * Visits a tape registerer.
     */
    @Override
	public Object visitSetTape(LIRSetTape self,
                               JExpression streamContext,
                               JExpression srcStruct,
                               JExpression dstStruct,
                               CType type,
                               int size) {
        streamContext.accept(this);
        srcStruct.accept(this);
        dstStruct.accept(this);
        return self;
    }

    /**
     * Visits a main function contents.
     */
    @Override
	public Object visitMainFunction(LIRMainFunction self,
                                    String typeName,
                                    LIRFunctionPointer init,
                                    List<JStatement> initStatements) {
        init.accept(this);
        for (int i=0; i<initStatements.size(); i++) {
            initStatements.get(i).accept(this);
        }
        return self;
    }


    /**
     * Visits a set body of feedback loop.
     */
    @Override
	public Object visitSetBodyOfFeedback(LIRSetBodyOfFeedback self,
                                         JExpression streamContext,
                                         JExpression childContext,
                                         CType inputType,
                                         CType outputType,
                                         int inputSize,
                                         int outputSize) {
        streamContext.accept(this);
        childContext.accept(this);
        return self;
    }


    /**
     * Visits a set loop of feedback loop.
     */
    @Override
	public Object visitSetLoopOfFeedback(LIRSetLoopOfFeedback self,
                                         JExpression streamContext,
                                         JExpression childContext,
                                         CType inputType,
                                         CType outputType,
                                         int inputSize,
                                         int outputSize) {
        streamContext.accept(this);
        childContext.accept(this);
        return self;
    }

    /**
     * Visits a set a parallel stream.
     */
    @Override
	public Object visitSetParallelStream(LIRSetParallelStream self,
                                         JExpression streamContext,
                                         JExpression childContext,
                                         int position,
                                         CType inputType,
                                         CType outputType,
                                         int inputSize,
                                         int outputSize) {
        streamContext.accept(this);
        childContext.accept(this);
        return self;
    }
}
