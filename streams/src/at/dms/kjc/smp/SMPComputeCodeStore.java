package at.dms.kjc.smp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import at.dms.kjc.CStdType;
import at.dms.kjc.JBlock;
import at.dms.kjc.JEmittedTextExpression;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JFormalParameter;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JStatement;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.backendSupport.ComputeCodeStore;
import at.dms.kjc.backendSupport.InterSSGChannel;
import at.dms.kjc.common.ALocalVariable;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InputPort;
import at.dms.kjc.slir.InterSSGEdge;
import at.dms.kjc.slir.OutputContent;
import at.dms.kjc.slir.OutputPort;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.WorkNode;

public class SMPComputeCodeStore extends ComputeCodeStore<Core> {

    /**
     * Append a barrier instruction to all of the (abstract) cores in the steady
     * state method.
     */
    public static void addBarrierSteady() {
        // don't do anything for the single core case
        if (SMPBackend.chip.size() == 1)
            return;

        for (int t = 0; t < SMPBackend.chip.size(); t++) {
            SMPComputeCodeStore cs = SMPBackend.chip.getNthComputeNode(
                    t).getComputeCode();
            cs.addSteadyLoopStatement(Util.toStmt("/* Steady-State Barrier */"));
            cs.addSteadyLoopStatement(Util.toStmt("barrier_wait(&barrier)"));
            cs.setHasCode();
        }
    }

    /**
     * Append a barrier instruction to all of the cores in the buffer init
     * method.
     */
    public static void addBufferInitBarrier() {
        // don't do anything for the single core case
        if (SMPBackend.chip.size() == 1)
            return;

        System.out.println("ERROR ERROR ERROR: TODO: too many barriers added!");

        for (int t = 0; t < SMPBackend.chip.size(); t++) {
            SMPComputeCodeStore cs = SMPBackend.chip.getNthComputeNode(
                    t).getComputeCode();
            cs.addStatementToBufferInit("barrier_wait(&barrier)");
            cs.setHasCode();
        }
    }

    /**
     * Add a new name to the set of token names used for intra-SSG
     * synchronization.
     * 
     * @param token
     *            the token name.
     */
    public static void addTokenName(String token) {
        tokenNames.add(token);
    }

    /**
     * For each of the file writers generate code to print the output for each
     * steady state. This is somewhat limited right now, so there are lots of
     * asserts.
     */
    public static void generatePrintOutputCode(SMPBackEndFactory backEndFactory) {
        // Generate code for the static communication channels
        Set<InputRotatingBuffer> fwb = InputRotatingBuffer
                .getFileWriterBuffers();
        // assert fwb.size() == 1;
        // for each of the file writer input buffers, so for each of the file
        // writers, find one of its sources, and add code to the source's
        // core to print the outputs at the end of each steady state
        for (InputRotatingBuffer buf : fwb) {
            generatePrintOutputCodeStatic(buf);
        }

        // Generate code for the dynamic communication channels
        Set<InterSSGChannel> buffers = InterSSGChannel.getFileWriterBuffers();
        // assert buffers.size() != 1;
        for (InterSSGChannel buf : buffers) {
            generatePrintOutputCodeDynamic(
                    buf,
                    backEndFactory);
        }
    }

    /**
     * Get the set of token names used for intra-SSG synchronization.
     * 
     * @return the set of token names.
     */
    public static Set<String> getTokenNames() {
        return tokenNames;
    }

    private static void addOpen(SMPComputeCodeStore codeStore,
            OutputContent fileOutput) {
        codeStore.appendTxtToGlobal("FILE *output;\n");

        // Special case for strings "stdout" and "stderr"
        // which are treated as keywords in the StreamIt
        // front end, but as converted to string when parsed
        if ("stdout".equals(fileOutput.getFileName())) {
            codeStore.addStatementFirstToBufferInit(Util
                    .toStmt("output = stdout"));
        } else if ("stderr".equals(fileOutput.getFileName())) {
            codeStore.addStatementFirstToBufferInit(Util
                    .toStmt("output = stderr"));
        } else {
            codeStore.addStatementFirstToBufferInit(Util
                    .toStmt("output = fopen(\"" + fileOutput.getFileName()
                            + "\", \"w\")"));
        }
    }

    /**
     * Generate the print output code for the dynamic communication channel.
     * 
     * @param buf
     *            the dynamic communication channel.
     */
    private static void generatePrintOutputCodeDynamic(InterSSGChannel buf,
            SMPBackEndFactory backEndFactory) {
        InterSSGEdge edge = buf.getEdge();
        InputPort inputPort = edge.getDest();
        OutputPort outputPort = edge.getSrc();
        Filter destFilter = inputPort.getSSG().getTopFilters()[0];
        Filter srcFilter = outputPort.getSSG().getFilterGraph()[outputPort
                .getSSG().getFilterGraph().length - 1];

        Core srcCore = SMPBackend.getComputeNode(srcFilter.getWorkNode());

        SMPComputeCodeStore codeStore = srcCore.getComputeCode();
        OutputContent fileOutput = (OutputContent) destFilter
                .getWorkNodeContent();

        codeStore.addPrintOutputCode(
                buf,
                srcFilter.getWorkNode(),
                backEndFactory);
        addOpen(
                codeStore,
                fileOutput);
    }

    /**
     * Generate the print output code for the static communication channel;
     * 
     * @param buf
     *            the static communication channel
     */
    private static void generatePrintOutputCodeStatic(InputRotatingBuffer buf) {

        WorkNode fileW = buf.filterNode;

        // find the core of the first input to the file writer
        WorkNode firstInputFilter = fileW.getParent().getInputNode()
                .getSources(
                        SchedulingPhase.STEADY)[0].getSrc().getParent()
                .getWorkNode();

        if (KjcOptions.sharedbufs
                && FissionGroupStore.isFizzed(firstInputFilter.getParent()))
            firstInputFilter = FissionGroupStore
                    .getFizzedSlices(firstInputFilter.getParent())[0]
                    .getWorkNode();

        Core core = SMPBackend.getComputeNode(firstInputFilter);

        SMPComputeCodeStore codeStore = core.getComputeCode();

        OutputContent fileOutput = (OutputContent) fileW.getWorkNodeContent();

        codeStore.addPrintOutputCode(
                buf,
                firstInputFilter);
        addOpen(
                codeStore,
                fileOutput);

    }

    /* A map of all the threads on this core */
    protected Map<Integer, SMPThreadCodeStore> threads            = new HashMap<Integer, SMPThreadCodeStore>();

    /** The name of the bufferInit method */
    public static final String                 bufferInitMethName = "buffer_and_address_init";

    /**
     * The set of token names used for synchronization within an SSG for
     * non-pipelined filters.
     */
    private static Set<String>                 tokenNames         = new HashSet<String>();

    /**
     * Append a barrier instruction to all of the cores in the init/primepump
     * stage.
     */
    public static void addBarrierInit() {
        // don't do anything for the single core case
        // if(SMPBackend.chip.size() == 1)
        // return;

        for (int t = 0; t < SMPBackend.chip.size(); t++) {
            SMPComputeCodeStore cs = SMPBackend.chip.getNthComputeNode(
                    t).getComputeCode();
            cs.addInitStatement(Util.toStmt("barrier_wait(&barrier)"));
            cs.setHasCode();
        }
    }

    /** Any text that should appear outside a function declaration in the c code */
    private StringBuffer                     globalTxt           = new StringBuffer();

    /** True if this CoreCodeStore has code appended to it */
    private boolean                          hasCode             = false;

    /** Code block containing declarations of all buffers */
    protected JBlock                         bufferDecls         = new JBlock();

    /**
     * The method that will malloc the buffers and receive addresses from
     * downstream cores
     */
    protected JMethodDeclaration             bufferInit;

    /**
     * Set of fields that should be externally defined on this core. I am using
     * a Map to store values because JFieldDeclaration does not define equals
     * and hashCode, so storing in a Set does not prevent duplicates correctly.
     */
    protected Map<String, JFieldDeclaration> externFields        = new HashMap<String, JFieldDeclaration>();
    /** set of FilterSliceNodes that are mapped to this core */
    protected Set<WorkNode>                  filters;

    /** set of JMethodDeclaration for the helper thread on this core */
    protected Set<JMethodDeclaration>        helperThreadMethods = new HashSet<JMethodDeclaration>();

    /** The "main" thread on this core. */
    private SMPThreadCodeStore               mainThread;

    /** The core that this CodeStore holds code for */
    private Core                             core                = null;

    /**
     * Constructor: steady state loops indefinitely, no pointer back to compute
     * node.
     */
    public SMPComputeCodeStore() {
        super();
        setMyMainName("__main__");
        removeDefaultMain();
        createMainThread();
    }

    public SMPComputeCodeStore(Core nodeType) {
        super(nodeType);
        this.core = nodeType;
        setMyMainName("__main__");
        removeDefaultMain();
        createMainThread(nodeType);
        filters = new HashSet<WorkNode>();
        helperThreadMethods = new HashSet<JMethodDeclaration>();
        createBufferInitMethod();
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
    public SMPComputeCodeStore(Core parent, ALocalVariable iterationBound) {
        super(parent, iterationBound);
        this.core = parent;
        setMyMainName("__main__");
        removeDefaultMain();
        createMainThread(
                parent,
                iterationBound);
        filters = new HashSet<WorkNode>();
        createBufferInitMethod();
    }

    public void addCallNextToThread(int threadIndex, int nextThread) {
        threads.get(
                threadIndex).addCallNextToMain(
                threadIndex,
                nextThread);
    }

    public void addExpressionFirst(JExpression expr) {
        getMain().addStatementFirst(
                new JExpressionStatement(expr));
    }

    public void addExternField(JFieldDeclaration jFieldDeclaration) {
        // There is a problem with this code. We do not want to store
        // duplicates of the same jFieldDeclaration. However, the
        // JFieldDeclaration
        // class does not define equals and hashCode, so the Set implementation
        // does not know about duplicate entries. Rather than changing the
        // Kopi compiler, I'm going to check for equality here by comparing
        // the names and types.
        externFields.put(
                jFieldDeclaration.getVariable().getIdent(),
                jFieldDeclaration);
    }

    /**
     * Remember that this filter is mapped to this core.
     * 
     * @param filter
     *            The filter we are mapping to this core.
     */
    public void addFilter(WorkNode filter) {
        filters.add(filter);
        this.setHasCode();
    }

    public void addFunctionCallFirst(int threadId, JMethodDeclaration func,
            JExpression[] args) {

        assert threads.get(threadId) != null : "SMPComputeCodeStore.addFunctionCallFirst threadId="
                + threadId + " not found.";

        threads.get(
                threadId).addFunctionCallFirst(
                func,
                args);
    }

    public void addFunctionCallFirst(int threadId, String funcName,
            JExpression[] args) {
        assert threads.get(threadId) != null : "SMPComputeCodeStore.addFunctionCallFirst threadId="
                + threadId + " not found.";

        threads.get(
                threadId).addFunctionCallFirst(
                funcName,
                args);
    }

    public void addFunctionCallFirst(JMethodDeclaration func, JExpression[] args) {

        getMain().addFunctionCallFirst(
                func,
                args);
    }

    public void addFunctionCallFirst(String funcName, JExpression[] args) {
        getMain().addFunctionCallFirst(
                funcName,
                args);
    }

    public void addHelperThreadMethod(JMethodDeclaration threadHelper) {
        helperThreadMethods.add(threadHelper);
        addMethod(threadHelper);
    }

    @Override
    public void addInitStatement(JStatement stmt) {
        if (stmt != null)
            getMain().addInitStatement(
                    stmt);
    }

    @Override
    public void addInitStatementFirst(JStatement stmt) {
        if (stmt != null)
            getMain().addInitStatementFirst(
                    stmt);
    }

    /**
     * Add stmt to the beginning of the method that will perform the allocation
     * of buffers and receive addresses of buffers from downstream cores.
     * 
     * @param stmt
     *            The statement to add to the end of the method
     */
    public void addStatementFirstToBufferInit(JStatement stmt) {
        bufferInit.getBody().addStatementFirst(
                stmt);
        this.setHasCode();
    }

    /**
     * Add txt to the beginning of the method that will perform the allocation
     * of buffers and receive addresses of buffers from downstream cores. Don't
     * use ; or newline
     * 
     * @param txt
     *            The statement to add to the end of the method
     */
    public void addStatementFirstToBufferInit(String txt) {
        JStatement stmt = new JExpressionStatement(new JEmittedTextExpression(
                txt));
        bufferInit.getBody().addStatementFirst(
                stmt);
        this.setHasCode();
    }

    /**
     * Add stmt to the end of code block that contains buffer declarations
     * 
     * @param stmt
     *            The statement to add to the end of the code block
     */
    public void addStatementToBufferDecls(JStatement stmt) {
        bufferDecls.addStatement(stmt);
    }

    /**
     * Add stmt to the end of the method that will perform the allocation of
     * buffers and receive addresses of buffers from downstream cores.
     * 
     * @param stmt
     *            The statement to add to the end of the method
     */
    public void addStatementToBufferInit(JStatement stmt) {
        bufferInit.getBody().addStatement(
                stmt);
        this.setHasCode();
    }

    /**
     * Add txt to the end of the method that will perform the allocation of
     * buffers and receive addresses of buffers from downstream cores. Don't use
     * ; or newline
     * 
     * @param txt
     *            The statement to add to the end of the method
     */
    public void addStatementToBufferInit(String txt) {
        JStatement stmt = new JExpressionStatement(new JEmittedTextExpression(
                txt));
        bufferInit.getBody().addStatement(
                stmt);
        this.setHasCode();
    }

    public void addStatementToSteadyLoop(int threadId, JStatement statement) {
        threads.get(
                threadId).addStatementToSteadyLoop(
                statement);
    }

//    public void addStatementToSteadyLoop(JStatement statement) {
//        this.addStatementToSteadyLoop(
//                0,
//                statement);
//    }

    public void addSteadyLoopStatement(int index, JStatement stmt) {
                
        threads.get(
                index).addSteadyLoopStatement(
                stmt);

    }

    @Override
    public void addSteadyLoopStatement(JStatement stmt) {
        getMain().addSteadyLoopStatement(
                stmt);
    }

    @Override
    public void addSteadyLoopStatementFirst(JStatement stmt) {
        getMain().addSteadyLoopStatementFirst(
                stmt);
    }

    public void addSteadyThreadCall(int threadIndex) {
        getMain().addSteadyThreadCall(
                threadIndex);
    }

    public void addSteadyThreadCall(int threadIndex, int nextIndex) {
        getMain().addSteadyThreadCall(
                threadIndex,
                nextIndex);
    }

    public void addSteadyThreadWait(int threadIndex) {
        getMain().addSteadyThreadWait(
                threadIndex);
    }

    /**
     * Find the correct thread, add the code to the steadyLoop of the correct
     * thread.
     * 
     * @param threadIndex
     * @param nextIndex
     * @param steadyBlock
     */
    public void addThreadHelper(int threadIndex, int nextIndex,
            JStatement steadyBlock) {

        System.out.println("SMPThreadCodeStore.addThreadHelper threadIndex="
                + threadIndex + " nextIndex=" + nextIndex);

        SMPThreadCodeStore thread;
        if (threads.containsKey(threadIndex)) {
            thread = threads.get(threadIndex);
        } else {
            thread = new SMPThreadCodeStore(this, threadIndex);
            threads.put(
                    threadIndex,
                    thread);
        }
        thread.addSteadyBlock(
                threadIndex,
                nextIndex,
                steadyBlock);

    }

    public void addThreadHelperNonOpt(WorkNode workNode, int threadIndex, JStatement steadyBlock) {
        getMain().addThreadHelperNonOpt(
                workNode,
                threadIndex,
                steadyBlock);
    }

    /**
     * Append str to the text that will appear outside of any function near the
     * top of the code for this core.
     * 
     * @param str
     *            The string to add
     */
    public void appendTxtToGlobal(String str) {
        globalTxt.append(str);
        this.setHasCode();
    }

    public void generateNumbersCode() {
        appendTxtToGlobal("uint64_t __last_cycle__ = 0;\n");
        appendTxtToGlobal("int __iteration__ = ITERATIONS;\n");

        appendTxtToGlobal("void __printSSCycleAvg() {\n");
        appendTxtToGlobal("  uint64_t __cur_cycle__ = rdtsc();\n");
        appendTxtToGlobal("  if(__last_cycle__ != 0) {\n");
        if (ProcessFileWriter.getTotalOutputs() > 0) {
            appendTxtToGlobal("    printf(\"Average cycles per SS for %d iterations: %llu, avg cycles per output: %llu\\n\", \n"
                    + "      ITERATIONS, (__cur_cycle__ - __last_cycle__) / ITERATIONS, \n"
                    + "      (((__cur_cycle__ - __last_cycle__) / ITERATIONS) / ((uint64_t)"
                    + ProcessFileWriter.getTotalOutputs() + ")));\n");
        } else {
            appendTxtToGlobal("    printf(\"Average cycles per SS for %d iterations: %llu \\n\", ITERATIONS, (__cur_cycle__ - __last_cycle__) / ITERATIONS);\n");
        }
        appendTxtToGlobal("    fflush(stdout);\n");
        appendTxtToGlobal("  }\n");
        appendTxtToGlobal("  __last_cycle__ = rdtsc();\n");
        appendTxtToGlobal("  __iteration__ = 0;\n");
        appendTxtToGlobal("}\n");

        addSteadyLoopStatement(Util.toStmt("__iteration__++"));
        addSteadyLoopStatement(Util
                .toStmt("if (__iteration__ >= ITERATIONS) __printSSCycleAvg()"));
    }

    /**
     * Return the method that initializes the rotating buffers and communicates
     * addresses.
     * 
     * @return the method that initializes the rotating buffers and communicates
     *         addresses.
     */
    public JMethodDeclaration getBufferInitMethod() {
        return bufferInit;
    }

    /**
     * Return the core that this compute store is associated with.
     * @return the core that this compute store is associated with.
     */
    public Core getCore() {
        assert core != null : "Compute store has a null core.";
        return core;
    }

    /**
     * Return the helper thread methods in this store;
     * 
     * @return the helper thread methods in this store;
     */
    public Set<JMethodDeclaration> getDynamicThreadHelperMethods() {
        return helperThreadMethods;
    }

    /**
     * Return the external fields.
     * @return the external fields.
     */
    public Map<String, JFieldDeclaration> getExternFields() {
        return externFields;
    }

    /**
     * return all of the filters that are mapped to this core.     
     * @return all of the filters that are mapped to this core.
     */
    public Set<WorkNode> getFilters() {
        return filters;
    }

    /**
     * Return the string to add to the global portion of the c files
     * @return the string to add to the global portion of the c file
     */
    public String getGlobalText() {
        return globalTxt.toString();
    }

    @Override
    public JMethodDeclaration getMainFunction() {
        if (threads != null && getMain() != null) {
            return getMain().getMethod();
        }
        return mainMethod;
    }

    @Override
    public JMethodDeclaration[] getMethods() {
        return methods;
    }

    @Override
    public String getMyMainName() {
        if (threads != null && getMain() != null) {
            return getMain().getMethodName();
        }
        if (myMainName == null) {
            setMyMainName(mainName);
        }
        return myMainName;
    }

    public Core getParent() {
        return parent;
    }

    /**
     * Set the helper thread methods in this store;
     * 
     * @param methods
     *            the helper thread methods in this store;
     */
    public void setDynamicThreadHelperMethods(Set<JMethodDeclaration> methods) {
        helperThreadMethods = methods;
    }

    /**
     * Set that this core (code store) has code written to it and thus it needs
     * to be considered during code generation.
     */
    public void setHasCode() {
        hasCode = true;
    }

    @Override
    public void setMainFunction(JMethodDeclaration mainMethod) {
        if (threads != null && getMain() != null) {
            getMain().setMainFunction(
                    mainMethod);
        }
        this.mainMethod = mainMethod;
        setMyMainName(mainMethod.getName());
    }

    /**
     * Set name of main function
     * 
     * @param main
     *            The new name of the main function
     */
    public void setMainName(String mainName) {
        assert (mainName != null && mainName != "");
        setMyMainName(mainName);
    }

    @Override
    public void setMethods(JMethodDeclaration[] m) {
        this.methods = m;
    }

    /**
     * Return true if we should generate code for this core, false if no code
     * was ever generated for this core.
     * 
     * @return true if we should generate code for this core, false if no code
     *         was ever generated for this core.
     */
    public boolean shouldGenerateCode() {
        return hasCode;
    }

    /**
     * Add code to print the output written to the file writer mapped to this
     * core.
     */
    private void addPrintOutputCode(InputRotatingBuffer buf, WorkNode workNode) {
        getMain().addPrintOutputCode(
                buf,
                workNode);
    }

    /**
     * Add code to print the output written to the file writer mapped to this
     * core.
     */
    private void addPrintOutputCode(InterSSGChannel buf, WorkNode workNode,
            SMPBackEndFactory backEndFactory) {
        getMain().addPrintOutputCode(
                buf,
                workNode,
                backEndFactory);
    }

    /**
     * Create the method that will malloc the buffers and receive the (
     * addresses from downstream cores
     */
    private void createBufferInitMethod() {
        bufferInit = new JMethodDeclaration(CStdType.Void, bufferInitMethName
                + "__n" + parent.getCoreID(), new JFormalParameter[0],
                new JBlock());
    }

    private SMPThreadCodeStore createMainThread() {
        SMPThreadCodeStore thread = new SMPThreadCodeStore(this, "__main__");
        int coreId = ThreadMapper.getFirstCore();
        return createMainThreadHelper(
                coreId,
                thread);
    }

    private SMPThreadCodeStore createMainThread(Core nodeType) {
        SMPThreadCodeStore thread = new SMPThreadCodeStore(this, "__main__",
                nodeType);
        return createMainThreadHelper(
                nodeType.coreID,
                thread);
    }

    private SMPThreadCodeStore createMainThread(Core parent,
            ALocalVariable iterationBound) {
        SMPThreadCodeStore thread = new SMPThreadCodeStore(this, "__main__",
                parent, iterationBound);
        return createMainThreadHelper(
                parent.coreID,
                thread);
    }

    private SMPThreadCodeStore createMainThreadHelper(int coreId,
            SMPThreadCodeStore thread) {
        addMethod(thread.getMethod());
        mainThread = thread;
        int threadId = ThreadMapper.coreToThread(coreId);
        System.out
                .println("**** SMPComputeCodeStore.createMainThreadHelper threadId="
                        + threadId);
        threads.put(
                threadId,
                thread);
        return thread;
    }

    /**
     * Get the thread representing the "main" thread
     * 
     * @return
     */
    private SMPThreadCodeStore getMain() {
        return mainThread;
    }

    private void removeDefaultMain() {
        List<JMethodDeclaration> cleanMethods = new ArrayList<JMethodDeclaration>();
        for (JMethodDeclaration method : methods) {
            if (!method.getName().equals(
                    "__main__")) {
                cleanMethods.add(method);
            }
        }
        setMethods(cleanMethods.toArray(new JMethodDeclaration[0]));
    }

}
