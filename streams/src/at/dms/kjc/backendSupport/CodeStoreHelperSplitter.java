package at.dms.kjc.backendSupport;

import at.dms.kjc.CClassType;
import at.dms.kjc.CStdType;
import at.dms.kjc.JBlock;
import at.dms.kjc.JFormalParameter;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JStatement;
import at.dms.kjc.slir.InterFilterEdge;
import at.dms.kjc.slir.OutputNode;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.WorkNodeInfo;

public class CodeStoreHelperSplitter extends CodeStoreHelper {
    /**
     * Constructor
     * @param node   The OutputSliceNode for the splitter
      * @param backEndBits  An instance of a subclass of a BackEndFactory to access backend-specific code / data.
     */
    public CodeStoreHelperSplitter(OutputNode node, BackEndFactory backEndBits) {
        super(node, backEndBits);
    }
    @Override
    public JMethodDeclaration getInitStageMethod() {
        if (getWorkMethod() == null) {
            return null;
        }
        // if we have a work method, iterate it enough
        // for downstream filter.
        JBlock statements = new JBlock();
        WorkNodeInfo filterInfo = WorkNodeInfo.getFilterInfo(internalFilterNode.getPrevious().getAsFilter());

        // channel code before work block
        for (InterFilterEdge e : internalFilterNode.getAsOutput().getDestList(SchedulingPhase.INIT)) {
            for (JStatement stmt : backEndFactory.getChannel(e).beginInitWrite()) {
                statements.addStatement(stmt);
            }
        }
        for (JStatement stmt : backEndFactory.getChannel(internalFilterNode.getEdgeToNext()).beginInitWrite()) {
            statements.addStatement(stmt);
        }
        // work block
        statements.addStatement(getWorkFunctionBlock(filterInfo.initItemsReceived()));
        // channel code after work block
        for (InterFilterEdge e : internalFilterNode.getAsOutput().getDestList(SchedulingPhase.INIT)) {
            for (JStatement stmt : backEndFactory.getChannel(e).endInitWrite()) {
                statements.addStatement(stmt);
            }
        }
        for (JStatement stmt : backEndFactory.getChannel(internalFilterNode.getEdgeToNext()).endInitWrite()) {
            statements.addStatement(stmt);
        }
        
        
        return new JMethodDeclaration(null, at.dms.kjc.Constants.ACC_PUBLIC,
                CStdType.Void,
                initStage + uniqueID,
                JFormalParameter.EMPTY,
                CClassType.EMPTY,
                statements,
                null,
                null);
    }

    
    @Override
    public JMethodDeclaration getPrimePumpMethod() {
        if (primePumpMethod != null) {
            return primePumpMethod;
        }
        JBlock statements = new JBlock();
        WorkNodeInfo filterInfo = WorkNodeInfo.getFilterInfo(internalFilterNode.getNext().getAsFilter());
        
        // channel code before work block
        for (InterFilterEdge e : internalFilterNode.getAsOutput().getDestList(SchedulingPhase.STEADY)) {
            for (JStatement stmt : backEndFactory.getChannel(e).beginSteadyWrite()) {
                statements.addStatement(stmt);
            }
        }
        for (JStatement stmt : backEndFactory.getChannel(internalFilterNode.getEdgeToNext()).beginSteadyWrite()) {
            statements.addStatement(stmt);
        }
        // code for a steady-state iteration
        statements.addStatement(getWorkFunctionBlock(filterInfo.totalItemsReceived(SchedulingPhase.PRIMEPUMP)));
        // channel code after work block
        for (InterFilterEdge e : internalFilterNode.getAsOutput().getDestList(SchedulingPhase.STEADY)) {
            for (JStatement stmt : backEndFactory.getChannel(e).endSteadyWrite()) {
                statements.addStatement(stmt);
            }
        }
        for (JStatement stmt : backEndFactory.getChannel(internalFilterNode.getEdgeToNext()).endSteadyWrite()) {
            statements.addStatement(stmt);
        }

        
        //return the method
        primePumpMethod = new JMethodDeclaration(null, at.dms.kjc.Constants.ACC_PUBLIC,
                                      CStdType.Void,
                                      primePumpStage + uniqueID,
                                      JFormalParameter.EMPTY,
                                      CClassType.EMPTY,
                                      statements,
                                      null,
                                      null);
        return primePumpMethod;
    }

    @Override
    public JBlock getSteadyBlock() {
        JBlock statements = new JBlock();
        WorkNodeInfo filterInfo = WorkNodeInfo.getFilterInfo(internalFilterNode.getNext().getAsFilter());
        
        // channel code before work block
        for (InterFilterEdge e : internalFilterNode.getAsOutput().getDestList(SchedulingPhase.STEADY)) {
            for (JStatement stmt : backEndFactory.getChannel(e).beginSteadyWrite()) {
                statements.addStatement(stmt);
            }
        }
        for (JStatement stmt : backEndFactory.getChannel(internalFilterNode.getEdgeToNext()).beginSteadyWrite()) {
            statements.addStatement(stmt);
        }
        // work block / work call
        JStatement work = getWorkFunctionBlock(filterInfo.totalItemsReceived(SchedulingPhase.STEADY));
        if (work != null) { statements.addStatement(work); }
        // channel code after work block
        for (InterFilterEdge e : internalFilterNode.getAsOutput().getDestList(SchedulingPhase.STEADY)) {
            for (JStatement stmt : backEndFactory.getChannel(e).endSteadyWrite()) {
                statements.addStatement(stmt);
            }
        }
        for (JStatement stmt : backEndFactory.getChannel(internalFilterNode.getEdgeToNext()).endSteadyWrite()) {
            statements.addStatement(stmt);
        }
        return statements;
    }
}
