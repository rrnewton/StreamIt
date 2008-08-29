package at.dms.kjc.tilera;

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
import at.dms.kjc.backendSupport.CodeStoreHelper;
import at.dms.kjc.backendSupport.FilterInfo;
import at.dms.kjc.sir.SIRBeginMarker;
import at.dms.kjc.slicegraph.FilterContent;
import at.dms.kjc.slicegraph.FilterSliceNode;
import at.dms.util.Utils;

public class FilterCodeGeneration extends CodeStoreHelper {
 
    private FilterSliceNode filterNode;
    private FilterInfo filterInfo;
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
    public FilterCodeGeneration(FilterSliceNode node, TileraBackEndFactory backEndBits) {
        super(node,node.getAsFilter().getFilter(),backEndBits);
        filterNode = node;
        filterInfo = FilterInfo.getFilterInfo(filterNode);
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
        assert sliceNode instanceof FilterSliceNode;
        FilterContent filter = ((FilterSliceNode) sliceNode).getFilter();

        // channel code before work block
        //slice has input, so we 
        if (backEndBits.sliceHasUpstreamChannel(sliceNode.getParent())) {
            for (JStatement stmt : InputRotatingBuffer.getInputBuffer(filterNode).beginInitRead()) {
                statements.addStatement(stmt);
            }
        }
        if (backEndBits.sliceHasDownstreamChannel(sliceNode.getParent())) {
            for (JStatement stmt : OutputRotatingBuffer.getOutputBuffer(filterNode).beginInitWrite()) {
                statements.addStatement(stmt);
            }
        }
        // add the calls for the work function in the initialization stage
        if (FilterInfo.getFilterInfo((FilterSliceNode) sliceNode).isTwoStage()) {

            JMethodCallExpression initWorkCall = new JMethodCallExpression(
                    null, new JThisExpression(null), filter.getInitWork()
                            .getName(), new JExpression[0]);

            statements.addStatement(new JExpressionStatement(initWorkCall));

            if (backEndBits.sliceHasUpstreamChannel(sliceNode.getParent())) {
                for (JStatement stmt : InputRotatingBuffer.getInputBuffer(filterNode).postPreworkInitRead()) {
                    statements.addStatement(stmt);
                }
            }
        }

        statements.addStatement(generateInitWorkLoop(filter));

        // channel code after work block
        if (backEndBits.sliceHasUpstreamChannel(sliceNode.getParent())) {
            for (JStatement stmt : InputRotatingBuffer.getInputBuffer(filterNode).endInitRead()) {
                statements.addStatement(stmt);
            }
        }
        if (backEndBits.sliceHasDownstreamChannel(sliceNode.getParent())) {
            for (JStatement stmt : OutputRotatingBuffer.getOutputBuffer(filterNode).endInitWrite()) {
                statements.addStatement(stmt);
            }
        }

        statements.addAllStatements(endSchedulingPhase());
        
        return new JMethodDeclaration(null, at.dms.kjc.Constants.ACC_PUBLIC,
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
    private JStatement generateInitWorkLoop(FilterContent filter)
    {
        FilterInfo filterInfo = FilterInfo.getFilterInfo((FilterSliceNode)sliceNode);
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

    public JMethodDeclaration getPrimePumpMethod() {
        if (primePumpMethod != null) {
            return primePumpMethod;
        }
        
        JBlock statements = new JBlock();
        // channel code before work block
        if (backEndBits.sliceHasUpstreamChannel(sliceNode.getParent())) {
            for (JStatement stmt : InputRotatingBuffer.getInputBuffer(filterNode).beginPrimePumpRead()) {
                statements.addStatement(stmt);
            }
        }
        if (backEndBits.sliceHasDownstreamChannel(sliceNode.getParent())) {
            for (JStatement stmt : OutputRotatingBuffer.getOutputBuffer(filterNode).beginPrimePumpWrite()) {
                statements.addStatement(stmt);
            }
        }
        // add the calls to the work function for the priming of the pipeline
        statements.addStatement(getWorkFunctionBlock(filterInfo.steadyMult));
        // channel code after work block
        if (backEndBits.sliceHasUpstreamChannel(sliceNode.getParent())) {
            for (JStatement stmt : InputRotatingBuffer.getInputBuffer(filterNode).endPrimePumpRead()) {
                statements.addStatement(stmt);
            }
        }
        if (backEndBits.sliceHasDownstreamChannel(sliceNode.getParent())) {
            for (JStatement stmt : OutputRotatingBuffer.getOutputBuffer(filterNode).endPrimePumpWrite()) {
                statements.addStatement(stmt);
            }
        }
        statements.addAllStatements(endSchedulingPhase());
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
        
        // channel code before work block
        if (backEndBits.sliceHasUpstreamChannel(sliceNode.getParent())) {
            for (JStatement stmt : InputRotatingBuffer.getInputBuffer(filterNode).beginSteadyRead()) {
                statements.addStatement(stmt);
            }
        }
        
        if (backEndBits.sliceHasDownstreamChannel(sliceNode.getParent())) {
            for (JStatement stmt : OutputRotatingBuffer.getOutputBuffer(filterNode).beginSteadyWrite()) {
                statements.addStatement(stmt);
            }
        }
        
        // iterate work function as needed
        statements.addStatement(getWorkFunctionBlock(FilterInfo
                .getFilterInfo((FilterSliceNode) sliceNode).steadyMult));
        
        // channel code after work block
        if (backEndBits.sliceHasUpstreamChannel(sliceNode.getParent())) {
            for (JStatement stmt : InputRotatingBuffer.getInputBuffer(filterNode).endSteadyRead()) {
                statements.addStatement(stmt);
            }
        }
        if (backEndBits.sliceHasDownstreamChannel(sliceNode.getParent())) {
            for (JStatement stmt : OutputRotatingBuffer.getOutputBuffer(filterNode).endSteadyWrite()) {
                statements.addStatement(stmt);
            }
        }
        statements.addAllStatements(endSchedulingPhase());
    
        return statements;
    }
    
    /**
     * Code returned by this function will be appended to the methods for the init, primepump, and
     * steady stages for this filter.
     * 
     * @return  The block of code to append
     */
    protected JBlock endSchedulingPhase() {
        JBlock block = new JBlock();
        
        if (TileraBackend.scheduler.isTMD()) {
            //block.addStatement(Util.toStmt("ilib_msg_barrier(ILIB_GROUP_SIBLINGS)"));
        }
        
        return block;
    }
}
