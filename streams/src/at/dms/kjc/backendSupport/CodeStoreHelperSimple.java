package at.dms.kjc.backendSupport;

import at.dms.classfile.Constants;
import at.dms.kjc.CClassType;
import at.dms.kjc.CStdType;
import at.dms.kjc.JBlock;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JFormalParameter;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JMethodCallExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JStatement;
import at.dms.kjc.JThisExpression;
import at.dms.kjc.JVariableDefinition;
import at.dms.kjc.sir.SIRBeginMarker;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.slir.WorkNodeContent;
import at.dms.kjc.slir.WorkNodeInfo;
import at.dms.util.Utils;

/**
 * CodeStore helper routines for FilterSliceNode that does not need a peek buffer. 
 * @author dimock
 */
public class CodeStoreHelperSimple extends CodeStoreHelper {

    private static String exeIndex1Name = "__EXEINDEX__1__";
    private JVariableDefinition exeIndex1;
    private boolean exeIndex1Used;
    private JVariableDefinition useExeIndex1() {
        if (exeIndex1Used) return exeIndex1;
        else {
            exeIndex1 = new JVariableDefinition(null, 
                    0, 
                    CStdType.Integer,
                    exeIndex1Name + uniqueID,
                    null);
            exeIndex1Used = true;
            this.addField(new JFieldDeclaration(exeIndex1));
            return exeIndex1;
        }
    }
    
    /**
     * Constructor
     * @param node          A filter slice node to wrap code for.
     * @param backEndBits   The back end factory as a source of data and back end specific functions.
     */
    public CodeStoreHelperSimple(WorkNode node, BackEndFactory backEndBits) {
        super(node,node.getAsFilter().getWorkNodeContent(),backEndBits);
    }

    /**
     * Calculate and return the method that implements the init stage 
     * computation for this filter.  It should be called only once in the 
     * generated code.
     * <p>
     * This does not include the call to the init function of the filter.
     * That is done in {@link RawComputeCodeStore#addInitFunctionCall}. 
     * 
     * @return The method that implements the init stage for this filter.
     */
    @Override
    public JMethodDeclaration getInitStageMethod() {
        JBlock statements = new JBlock();
        assert internalFilterNode instanceof WorkNode;
        WorkNodeContent filter = ((WorkNode) internalFilterNode).getWorkNodeContent();

        // channel code before work block
        if (backEndFactory.sliceHasUpstreamChannel(internalFilterNode.getParent())) {
            for (JStatement stmt : backEndFactory.getChannel(
                    internalFilterNode.getPrevious().getEdgeToNext()).beginInitRead()) {
                statements.addStatement(stmt);
            }
        }
        if (backEndFactory.sliceHasDownstreamChannel(internalFilterNode.getParent())) {
            for (JStatement stmt : backEndFactory.getChannel(
                    internalFilterNode.getEdgeToNext()).beginInitWrite()) {
                statements.addStatement(stmt);
            }
        }
        // add the calls for the work function in the initialization stage
        if (WorkNodeInfo.getFilterInfo((WorkNode) internalFilterNode).isTwoStage()) {

            JMethodCallExpression initWorkCall = new JMethodCallExpression(
                    null, new JThisExpression(null), filter.getInitWork()
                            .getName(), new JExpression[0]);

            statements.addStatement(new JExpressionStatement(initWorkCall));

            if (backEndFactory.sliceHasUpstreamChannel(internalFilterNode.getParent())) {
                for (JStatement stmt : backEndFactory.getChannel(
                        internalFilterNode.getPrevious().getEdgeToNext())
                        .postPreworkInitRead()) {
                    statements.addStatement(stmt);
                }
            }
        }

        statements.addStatement(generateInitWorkLoop(filter));

        // channel code after work block
        if (backEndFactory.sliceHasUpstreamChannel(internalFilterNode.getParent())) {
            for (JStatement stmt : backEndFactory.getChannel(
                    internalFilterNode.getPrevious().getEdgeToNext()).endInitRead()) {
                statements.addStatement(stmt);
            }
        }
        if (backEndFactory.sliceHasDownstreamChannel(internalFilterNode.getParent())) {
            for (JStatement stmt : backEndFactory.getChannel(
                    internalFilterNode.getEdgeToNext()).endInitWrite()) {
                statements.addStatement(stmt);
            }
        }

        return new JMethodDeclaration(null, Constants.ACC_PUBLIC,
                CStdType.Void, initStage + uniqueID, JFormalParameter.EMPTY,
                CClassType.EMPTY, statements, null, null);
    }

    /**
     * Generate the loop for the work function firings in the initialization
     * schedule. This does not include receiving the necessary items for the
     * first firing. This is handled in
     * {@link DirectCommunication#getInitStageMethod}. This block will generate
     * code to receive items for all subsequent calls of the work function in
     * the init stage plus the class themselves.
     * 
     * @param filter
     *            The filter
     * @param generatedVariables
     *            The vars to use.
     * 
     * @return The code to fire the work function in the init stage.
     */
    private JStatement generateInitWorkLoop(WorkNodeContent filter)
    {
        WorkNodeInfo filterInfo = WorkNodeInfo.getFilterInfo((WorkNode)internalFilterNode);
        JBlock block = new JBlock();

        //clone the work function and inline it
        JStatement workBlock = getWorkFunctionCall();
    
        //if we are in debug mode, print out that the filter is firing
//        if (SpaceTimeBackend.FILTER_DEBUG_MODE) {
//            block.addStatement
//                (new SIRPrintStatement(null,
//                                       new JStringLiteral(null, filter.getName() + " firing (init)."),
//                                       null));
//        }

        if (workBlock == null) {
            workBlock = new SIRBeginMarker("Empty Work Block!!");
        }
        block.addStatement(workBlock);
    
        //return the for loop that executes the block init - 1
        //times
        return Utils.makeForLoopFieldIndex(block, useExeIndex1(), 
                           new JIntLiteral(filterInfo.initMult));
    }

    @Override
    public JMethodDeclaration getPrimePumpMethod() {
        return super.getPrimePumpMethodForFilter(WorkNodeInfo.getFilterInfo((WorkNode)internalFilterNode));
    }

    @Override
    public JBlock getSteadyBlock() {
    	    
    	
        JBlock statements = new JBlock();
        // channel code before work block
        if (backEndFactory.sliceHasUpstreamChannel(internalFilterNode.getParent())) {
            for (JStatement stmt : backEndFactory.getChannel(
                    internalFilterNode.getPrevious().getEdgeToNext()).beginSteadyRead()) {
                statements.addStatement(stmt);
            }
        }
        if (backEndFactory.sliceHasDownstreamChannel(internalFilterNode.getParent())) {
            for (JStatement stmt : backEndFactory.getChannel(
                    internalFilterNode.getEdgeToNext()).beginSteadyWrite()) {
                statements.addStatement(stmt);
            }
        }
        // iterate work function as needed
        statements.addStatement(getWorkFunctionBlock(WorkNodeInfo
                .getFilterInfo((WorkNode) internalFilterNode).steadyMult));
        // channel code after work block
        if (backEndFactory.sliceHasUpstreamChannel(internalFilterNode.getParent())) {
            for (JStatement stmt : backEndFactory.getChannel(
                    internalFilterNode.getPrevious().getEdgeToNext()).endSteadyRead()) {
                statements.addStatement(stmt);
            }
        }
        if (backEndFactory.sliceHasDownstreamChannel(internalFilterNode.getParent())) {
            for (JStatement stmt : backEndFactory.getChannel(
                    internalFilterNode.getEdgeToNext()).endSteadyWrite()) {
                statements.addStatement(stmt);
            }
        }
        return statements;
    }
}
