package at.dms.kjc.smp;

import java.util.Map;
import java.util.Set;
import at.dms.classfile.Constants;
import at.dms.kjc.CClassType;
import at.dms.kjc.CStdType;
import at.dms.kjc.CType;
import at.dms.kjc.JBlock;
import at.dms.kjc.JBooleanLiteral;
import at.dms.kjc.JEmittedTextExpression;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JFormalParameter;
import at.dms.kjc.JMethodCallExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JStatement;
import at.dms.kjc.JThisExpression;
import at.dms.kjc.JVariableDefinition;
import at.dms.kjc.JWhileStatement;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.backendSupport.InterSSGChannel;
import at.dms.kjc.common.ALocalVariable;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InputPort;
import at.dms.kjc.slir.InterSSGEdge;
import at.dms.kjc.slir.OutputContent;
import at.dms.kjc.slir.WorkNode;
import at.dms.util.Utils;

/**
 * This class represents the code stored on a single thread. There may be
 * multiple threads associated with a single core. In the completely static
 * case, there will be only one thread on the code, that is, the original
 * main__* thread.
 * 
 * @author soule
 * 
 */
public class SMPThreadCodeStore { // extends ComputeCodeStore<Core> {

    /******** Inherited from Compute Code Store *************/

    protected static final boolean CODE = true;

    /**
     * this method calls all the initialization routines and the steady state
     * routines
     */
    protected JMethodDeclaration   mainMethod;

    protected String               myMainName;

    /** block for the steady-state, as calculated currently */
    protected JBlock               steadyLoop;

    /**
     * the block that executes each slicenode's init schedule, as calculated
     * currently
     */
    protected JBlock               initBlock;

    /** the block executed after the steadyLoop */
    protected JBlock               cleanupBlock;

    /******** Inherited from Compute Code Store *************/

    /** The core code store that holds this thread */
    SMPComputeCodeStore            coreCodeStore;

    /**
     * Constructor: steady state loops indefinitely, no pointer back to compute
     * node.
     */
    public SMPThreadCodeStore(SMPComputeCodeStore coreCodeStore, String name) {
        myMainName = name;
        this.coreCodeStore = coreCodeStore;
        mainMethod = createThreadMethod(myMainName);
        steadyLoop = new JBlock(null, new JStatement[0], null);
        addSteadyLoop();

    }

    public SMPThreadCodeStore(SMPComputeCodeStore coreCodeStore, String name,
            Core nodeType) {
        myMainName = name;
        this.coreCodeStore = coreCodeStore;
        mainMethod = createThreadMethod(myMainName);
        steadyLoop = new JBlock(null, new JStatement[0], null);
        this.addSteadyLoop();
    }

    /**
     * Constructor: caller will add code to bound number of iterations, code
     * store has pointer back to a compute node.
     * 
     * @param parent
     *            a ComputeNode.
     * @param iterationBound
     *            a variable that will be defined locally in
     */
    public SMPThreadCodeStore(SMPComputeCodeStore coreCodeStore, String name,
            Core parent, ALocalVariable iterationBound) {
        myMainName = name;
        this.coreCodeStore = coreCodeStore;
        mainMethod = createThreadMethod(myMainName);
        steadyLoop = new JBlock(null, new JStatement[0], null);
        addSteadyLoop(iterationBound);
    }

    public void addExpressionFirst(JExpression expr) {
        mainMethod.addStatementFirst(new JExpressionStatement(expr));
    }

    public void addFunctionCallFirst(JMethodDeclaration func, JExpression[] args) {
        mainMethod.addStatementFirst(new JExpressionStatement(null,
                new JMethodCallExpression(null, new JThisExpression(null), func
                        .getName(), args), null));
    }

    public void addFunctionCallFirst(String funcName, JExpression[] args) {
        mainMethod.addStatementFirst(new JExpressionStatement(null,
                new JMethodCallExpression(null, new JThisExpression(null),
                        funcName, args), null));
    }

    public void addInitStatement(JStatement stmt) {
        if (stmt != null)
            initBlock.addStatement(stmt);
    }

    public void addInitStatementFirst(JStatement stmt) {
        if (stmt != null)
            initBlock.addStatementFirst(stmt);
    }

    public void addParameter(JFormalParameter jFormalParameter) {
        mainMethod.addParameter(new JFormalParameter(CVoidPtrType.VoidPtr,
                "arg"));
    }

    /**
     * Add code to print the output written to the file writer mapped to this
     * core.
     */
    public void addPrintOutputCode(InputRotatingBuffer buf, WorkNode workNode) {

        // We print the address buffer after it has been rotated, so that it
        // points to the section
        // of the filewriter buffer that is about to be written to, but was
        // written to 2 steady-states ago

        WorkNode fileW = buf.filterNode;
        System.out.println("SMPThreadCodeStore.addPrintOutputCode workNode="
                + workNode.toString() + " fileW=" + fileW.toString());
        System.out.println("SMPThreadCodeStore.addPrintOutputCode filter="
                + workNode.toString() + " fileW.isFileOutput()="
                + fileW.isFileOutput() + " buf=" + buf.toString()
                + " buf.getRotationLength()=" + buf.getRotationLength());
        assert fileW.isFileOutput();
        // because of this scene we need a rotation length of 2
        // assert buf.getRotationLength() == 2: buf.getRotationLength();
        // make sure that each of the inputs wrote to the file writer in the
        // primepump stage
    
        
        int outputs = fileW.getWorkNodeContent().getSteadyMult();
        String type = ((OutputContent) fileW.getWorkNodeContent()).getType() == CStdType.Integer ? "%d"
                : "%f";
        String cast = ((OutputContent) fileW.getWorkNodeContent()).getType() == CStdType.Integer ? "(int)"
                : "(float)";
        String bufferName = buf.getAddressRotation(workNode).currentWriteBufName;
        // create the loop
        String stmt = "";
        String multiplierName = fileW.getWorkNodeContent().getName()
                + "_multiplier";

        JVariableDefinition multiplierVar = new JVariableDefinition(null, 0,
                CStdType.Integer, multiplierName, null);

        coreCodeStore.addExternField(new JFieldDeclaration(multiplierVar));

        if (KjcOptions.outputs < 0) {
            stmt = "if (" + multiplierName + ") {\n" + "  int _i_ = 0;\n"
                    + "  for (_i_ = 0; _i_ < " + outputs + "; _i_++) { \n"
                    + "fprintf(output, \"" + type + "\\n\", " + cast
                    + bufferName + "[_i_]); \n" + "  }\n" + "}\n";

        } else {
            stmt = "if (" + multiplierName + ") {\n" + "  int _i_ = 0;\n"
                    + "  for (_i_ = 0; _i_ < " + outputs + "; _i_++) { \n"
                    + "  fprintf(output, \"" + type + "\\n\", " + cast
                    + bufferName + "[_i_]); \n";
            if (KjcOptions.perftest) {
                stmt += "  if (currOutputs == maxIgnored) {  start_time(); } \n";
            }
            stmt += "        currOutputs++;\n"
                    + "  if (currOutputs == maxOutputs) {  streamit_exit(0); } \n"
                    + "  }\n" + "}\n";
        }
        addSteadyLoopStatement(Util.toStmt(stmt));
    }

    public void addPrintOutputCode(InterSSGChannel buf, WorkNode workNode,
            SMPBackEndFactory backEndFactory) {

        InterSSGEdge edge = buf.getEdge();
        InputPort inputPort = edge.getDest();

        Filter destFilter = inputPort.getSSG().getTopFilters()[0];

        WorkNode fileW = destFilter.getWorkNode();
        System.out.println("SMPThreadCodeStore.addPrintOutputCode workNode="
                + workNode.toString() + " fileW=" + fileW.toString());

        assert fileW.isFileOutput();
        int outputs = fileW.getWorkNodeContent().getSteadyMult();

        CType tapeType = ((OutputContent) fileW.getWorkNodeContent()).getType();

        String type = tapeType == CStdType.Integer ? "%d" : "%f";
        String cast = tapeType == CStdType.Integer ? "(int)" : "(float)";
        // String bufferName =
        // buf.getAddressRotation(workNode).currentWriteBufName;

        // create the loop
        String stmt = "";

        String popName = buf.popMethodName();

        Map<Filter, Integer> filterToThreadId = ThreadMapper.getMapper()
                .getFilterToThreadId();

        int threadIndex = filterToThreadId.get(inputPort.getSSG()
                .getTopFilters()[0]);

        String threadId = Integer.toString(threadIndex);

        String buffer = "dyn_buf_" + threadId;

        System.out.println("SMPThreadCodeStore threadId=" + threadId);

        if (KjcOptions.threadopt) {
            if (threadIndex == -1) {
                Filter prevFilter = ProcessFilterWorkNode
                        .getPreviousFilter(inputPort.getSSG().getTopFilters()[0]
                                .getWorkNode());
                Core core = SMPBackend.scheduler.getComputeNode(prevFilter
                        .getWorkNode());
                threadIndex = ThreadMapper.coreToThread(core.coreID);
            }
            buffer = "dyn_buf_" + buf.getId();
        }

        String popCall;
        if (KjcOptions.threadopt) {
            popCall = popName + "(" + buffer + ", " + threadIndex + ", "
                    + threadIndex + ", 0,  NULL)";
        } else {
            popCall = popName + "(" + buffer + ", " + threadId + ", 0, NULL)";
        }

        if (KjcOptions.outputs < 0) {
            stmt = "int _i_ = 0;\n" + "for (int _i_ = 0; _i_ < " + outputs
                    + "; _i_++) { \n";
            if (KjcOptions.lockfree) {
                stmt += "    if (" + buffer + "->head != " + buffer
                        + "->tail) {\n";
            } else {
                stmt += "    if (" + buffer + "->size > 0) {\n";
            }
            stmt += "        fprintf(output, \"" + type + "\\n\", " + cast
                    + popCall + ");\n " + "    }\n" + "}\n";

        } else {
            stmt = "int _i_ = 0;\n" + "for (_i_ = 0; _i_ < " + outputs
                    + "; _i_++) {\n";
            if (KjcOptions.lockfree) {
                stmt += "    if (" + buffer + "->head != " + buffer
                        + "->tail) {\n";
            } else {
                stmt += "    if (" + buffer + "->size > 0) {\n";
            }
            stmt += "        fprintf(output, \"" + type + "\\n\", " + cast
                    + popCall + ");\n";
            if (KjcOptions.perftest) {
                stmt += "  if (currOutputs == maxIgnored) {  start_time(); } \n";
            }
            stmt += "        currOutputs++;\n"
                    + "        if (currOutputs == maxOutputs) {  streamit_exit(0); } \n"
                    + "    }\n" + "}\n";
        }
        addSteadyLoopStatement(Util.toStmt(stmt));
    }

    public void addStatementFirst(JExpressionStatement statement) {
        mainMethod.addStatementFirst(statement);
    }

    public void addStatementToSteadyLoop(JStatement statement) {
        steadyLoop.addStatementFirst(statement);
    }

    public void addSteadyLoop() {
        // enable the profiler right before the steady loop on tilera
        if (KjcOptions.tilera > 0 && KjcOptions.profile) {
            mainMethod.addStatement(new JExpressionStatement(
                    new JEmittedTextExpression("profiler_enable()")));
            mainMethod.addStatement(new JExpressionStatement(
                    new JEmittedTextExpression("profiler_clear()")));
        }

        // add it to the while statement
        mainMethod.addStatement(new JWhileStatement(null, new JBooleanLiteral(
                null, true), steadyLoop, null));
    }

    public void addSteadyLoop(ALocalVariable iterationBound) {
        mainMethod.addStatement(at.dms.util.Utils.makeForLoop(
                steadyLoop,
                iterationBound.getRef()));
    }

    // /**
    // * @return the block contatining any cleanup code.
    // */
    // public JBlock getCleanupBlock() {
    // return cleanupBlock;
    // }
    //
    /**
     * @param stmt
     *            statement to add after any other statements in steady-state
     *            code.
     * 
     */
    public void addSteadyLoopStatement(JStatement stmt) {
        if (stmt != null)
            steadyLoop.addStatement(stmt);
    }

    public void addSteadyLoopStatementFirst(JStatement stmt) {
        if (stmt != null)
            steadyLoop.addStatementFirst(stmt);
    }

    public void addSteadyThreadCall(int threadIndex) {
        Utils.addSetFlag(
                steadyLoop,
                threadIndex,
                "MASTER",
                "MASTER",
                "ASLEEP");
        Utils.addSetFlag(
                steadyLoop,
                threadIndex,
                "DYN_READER",
                "DYN_READER",
                "AWAKE");
        Utils.addSignal(
                steadyLoop,
                threadIndex,
                "DYN_READER");
        Utils.addCondWait(
                steadyLoop,
                threadIndex,
                "MASTER",
                "MASTER",
                Utils.makeEqualityCondition(
                        "ASLEEP",
                        "thread_to_sleep[" + threadIndex + "][MASTER]"));
    }

    public void addSteadyThreadCall(int threadIndex, int nextIndex) {
        Utils.addSetFlag(
                steadyLoop,
                threadIndex,
                "ASLEEP");
        Utils.addSetFlag(
                steadyLoop,
                nextIndex,
                "AWAKE");
        Utils.addSignal(
                steadyLoop,
                nextIndex);

    }

    public void addSteadyThreadWait(int threadIndex) {
        Utils.addCondWait(
                steadyLoop,
                threadIndex,
                Utils.makeEqualityCondition(
                        "ASLEEP",
                        "thread_to_sleep[" + threadIndex + "]"));
    }

    /**
     * 
     * @param steadyBlock
     */
    public void addThreadHelper(int threadIndex, int nextIndex,
            JStatement steadyBlock) {

        System.out
                .println("SMPThreadCodeStore.addThreadHelper optimized called()");

        JBlock methodBody = new JBlock();
        JBlock loopBody = new JBlock();

        if (!KjcOptions.nobind) {

            WorkNode[] filterArray = new WorkNode[coreCodeStore.getFilters()
                    .size()];
            coreCodeStore.getFilters().toArray(
                    filterArray);
            Core core = SMPBackend.scheduler.getComputeNode(filterArray[0]);
            int coreNum = core.getCoreID();
            methodBody.addStatement(new JExpressionStatement(
                    new JEmittedTextExpression("setCPUAffinity(" + coreNum
                            + ")")));

        }

        Utils.addCondWait(
                loopBody,
                threadIndex,
                Utils.makeEqualityCondition(
                        "ASLEEP",
                        "thread_to_sleep[" + threadIndex + "]"));
        loopBody.addStatement(steadyBlock);
        Utils.addSetFlag(
                loopBody,
                threadIndex,
                "ASLEEP");
        Utils.addSetFlag(
                loopBody,
                nextIndex,
                "AWAKE");
        Utils.addSignal(
                loopBody,
                nextIndex);

        JStatement loop = null;
        if (KjcOptions.iterations != -1) {
            // addSteadyLoop(iterationBound);
            ALocalVariable var = ALocalVariable.makeVar(
                    CStdType.Integer,
                    "maxSteadyIter");
            loop = at.dms.util.Utils.makeForLoop(
                    loopBody,
                    var.getRef());

        } else {
            loop = new JWhileStatement(null, new JBooleanLiteral(null, true),
                    loopBody, null);
        }

        methodBody.addStatement(loop);
        methodBody.addStatement(new JExpressionStatement(
                new JEmittedTextExpression("pthread_exit(NULL)")));
        JFormalParameter p = new JFormalParameter(CVoidPtrType.VoidPtr, "x");

        String threadName = "helper_" + threadIndex;

        System.out
                .println("SMPThreadCodeStore.addThreadHelper creating JMethodDeclaration="
                        + threadName);

        JMethodDeclaration threadHelper = new JMethodDeclaration(
                CVoidPtrType.VoidPtr, threadName, new JFormalParameter[] { p },
                methodBody);
        addHelperThreadMethod(threadHelper);
    }

    /**
     * 
     * @param steadyBlock
     */
    public void addThreadHelper(int threadIndex, JStatement steadyBlock) {

        System.out.println("SMPThreadCodeStore.addThreadHelper called()");

        JBlock methodBody = new JBlock();
        JBlock loopBody = new JBlock();

        if (!KjcOptions.nobind) {
            WorkNode[] filterArray = new WorkNode[coreCodeStore.getFilters()
                    .size()];
            coreCodeStore.getFilters().toArray(
                    filterArray);
            Core core = SMPBackend.scheduler.getComputeNode(filterArray[0]);
            int coreNum = core.getCoreID();
            methodBody.addStatement(new JExpressionStatement(
                    new JEmittedTextExpression("setCPUAffinity(" + coreNum
                            + ")")));

        }

        Utils.addCondWait(
                loopBody,
                threadIndex,
                "DYN_READER",
                "DYN_READER",
                Utils.makeEqualityCondition(
                        "ASLEEP",
                        "thread_to_sleep[" + threadIndex + "][DYN_READER]"));
        loopBody.addStatement(steadyBlock);
        Utils.addSetFlag(
                loopBody,
                threadIndex,
                "DYN_READER",
                "DYN_READER",
                "ASLEEP");
        Utils.addSetFlag(
                loopBody,
                threadIndex,
                "MASTER",
                "MASTER",
                "AWAKE");
        Utils.addSignal(
                loopBody,
                threadIndex,
                "MASTER");

        JStatement loop = null;
        if (KjcOptions.iterations != -1) {
            // addSteadyLoop(iterationBound);
            ALocalVariable var = ALocalVariable.makeVar(
                    CStdType.Integer,
                    "maxSteadyIter");
            loop = at.dms.util.Utils.makeForLoop(
                    loopBody,
                    var.getRef());

        } else {
            loop = new JWhileStatement(null, new JBooleanLiteral(null, true),
                    loopBody, null);
        }

        methodBody.addStatement(loop);
        methodBody.addStatement(new JExpressionStatement(
                new JEmittedTextExpression("pthread_exit(NULL)")));
        JFormalParameter p = new JFormalParameter(CVoidPtrType.VoidPtr, "x");

        String threadName = "helper_" + threadIndex;

        System.out
                .println("SMPThreadCodeStore.addThreadHelper creating JMethodDeclaration="
                        + threadName);

        JMethodDeclaration threadHelper = new JMethodDeclaration(
                CVoidPtrType.VoidPtr, threadName, new JFormalParameter[] { p },
                methodBody);
        addHelperThreadMethod(threadHelper);
    }

    /**
     * Return the helper thread methods in this store;
     * 
     * @return the helper thread methods in this store;
     */
    public Set<JMethodDeclaration> getDynamicThreadHelperMethods() {
        return null;
        // return helperThreadMethods;
    }

    public JMethodDeclaration getMethod() {
        return mainMethod;
    }

    public String getMethodName() {
        return myMainName;
    }

    /**
     * Set the helper thread methods in this store;
     * 
     * @param methods
     *            the helper thread methods in this store;
     */
    public void setDynamicThreadHelperMethods(Set<JMethodDeclaration> methods) {
        // helperThreadMethods = methods;
    }

    public void setMainFunction(JMethodDeclaration mainMethod) {
        this.mainMethod = mainMethod;
        setMyMainName(mainMethod.getName());
    }

    private void addHelperThreadMethod(JMethodDeclaration threadHelper) {
        coreCodeStore.addHelperThreadMethod(threadHelper);

    }

    private JMethodDeclaration createThreadMethod(String funcName) {
        JMethodDeclaration method = new JMethodDeclaration(null,
                Constants.ACC_PUBLIC, CStdType.Void, funcName,
                JFormalParameter.EMPTY, CClassType.EMPTY, new JBlock(null,
                        new JStatement[0], null), null, null);
        method.addParameter(new JFormalParameter(CVoidPtrType.VoidPtr, "arg"));
        method.setReturnType(CVoidPtrType.VoidPtr);
        initBlock = new JBlock(null, new JStatement[0], null);
        method.addStatement(initBlock);
        return method;
    }

    private void setMyMainName(String name) {
        myMainName = name;
    }

}
