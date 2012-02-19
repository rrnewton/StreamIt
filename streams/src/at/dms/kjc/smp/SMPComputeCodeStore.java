package at.dms.kjc.smp;

import java.util.HashMap;
import java.util.HashSet;
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
import at.dms.util.Utils;

public class SMPComputeCodeStore extends ComputeCodeStore<Core> {

    /**
     * The set of token names used for synchronization within an SSG for
     * non-pipelined filters.
     */
    private static Set<String> tokenNames = new HashSet<String>();

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

        Core destCore = SMPBackend.scheduler.getComputeNode(destFilter
                .getWorkNode());
        Core srcCore = SMPBackend.scheduler.getComputeNode(srcFilter
                .getWorkNode());

        System.out
        .println("SMPComputeCodeStore.generatePrintOutputCodeDynamic destFilter.getWorkNode()="
                + destFilter.getWorkNode() + " is on core " + destCore);
        System.out
        .println("SMPComputeCodeStore.generatePrintOutputCodeDynamic srcFilter.getWorkNode()="
                + srcFilter.getWorkNode() + " is on core " + srcCore);

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

        Core core = SMPBackend.scheduler.getComputeNode(firstInputFilter);

        SMPComputeCodeStore codeStore = core.getComputeCode();

        OutputContent fileOutput = (OutputContent) fileW.getWorkNodeContent();

        codeStore.addPrintOutputCode(
                buf,
                firstInputFilter);
        addOpen(
                codeStore,
                fileOutput);

    }


    protected Map<Integer, SMPThreadCodeStore> threads = new HashMap<Integer, SMPThreadCodeStore>();

    /**
     * Constructor: steady state loops indefinitely, no pointer back to compute
     * node.
     */
    public SMPComputeCodeStore() {
        super();        
        threads.put(1, new SMPThreadCodeStore());
        threads.get(1).setMainName("__main__");    
    }

    public SMPComputeCodeStore(Core nodeType) {
        super(nodeType);        
        threads.put(1, new SMPThreadCodeStore(nodeType));
        threads.get(1).setMainName("__main__");                       
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
        threads.put(1, new SMPThreadCodeStore(parent, iterationBound));
        threads.get(1).setMainName("__main__");                      
    }

    public void addExpressionFirst(JExpression expr) {
        threads.get(1).addExpressionFirst(expr);      
    }

    public void addExternField(JFieldDeclaration jFieldDeclaration) {
        threads.get(1).addExternField( jFieldDeclaration);           
    }

    /**
     * Remember that this filter is mapped to this core.
     * 
     * @param filter
     *            The filter we are mapping to this core.
     */
    public void addFilter(WorkNode filter) {
        threads.get(1).addFilter(filter);       
    }


    public void addFunctionCallFirst(JMethodDeclaration func, JExpression[] args) {
        threads.get(1).addFunctionCallFirst(func, args);    
    }

    public void addFunctionCallFirst(String funcName, JExpression[] args) {
        threads.get(1).addFunctionCallFirst(funcName, args);    
    }


    public void addPrintOutputCode(InputRotatingBuffer buf, WorkNode workNode) {
        threads.get(1).addPrintOutputCode( buf,  workNode);
    }

    public void addPrintOutputCode(InterSSGChannel buf, WorkNode workNode, SMPBackEndFactory backEndFactory) {
        threads.get(1).addPrintOutputCode(
                buf,
                workNode,
                backEndFactory);
    }

    /**
     * Add stmt to the beginning of the method that will perform the allocation
     * of buffers and receive addresses of buffers from downstream cores.
     * 
     * @param stmt
     *            The statement to add to the end of the method
     */
    public void addStatementFirstToBufferInit(JStatement stmt) {
        threads.get(1).addStatementFirstToBufferInit(stmt);
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
        threads.get(1).addStatementFirstToBufferInit(txt);     
    }

    /**
     * Add stmt to the end of code block that contains buffer declarations
     * 
     * @param stmt
     *            The statement to add to the end of the code block
     */
    public void addStatementToBufferDecls(JStatement stmt) {
        threads.get(1).addStatementToBufferDecls(stmt);           
    }

    /**
     * Add stmt to the end of the method that will perform the allocation of
     * buffers and receive addresses of buffers from downstream cores.
     * 
     * @param stmt
     *            The statement to add to the end of the method
     */
    public void addStatementToBufferInit(JStatement stmt) {        
        threads.get(1).addStatementToBufferInit(stmt);
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
        threads.get(1).addStatementToBufferInit(txt); 
    }


    public void addStatementToSteadyLoop(JStatement statement) {
        threads.get(1).addStatementToSteadyLoop(statement);
    }

    public void addSteadyThreadCall(int threadIndex) {
        threads.get(1).addSteadyThreadCall( threadIndex);        
    }

    public void addSteadyThreadCall(int threadIndex, int nextIndex) {
        threads.get(1).addSteadyThreadCall( threadIndex, nextIndex);            
    }

    public void addSteadyThreadWait(int threadIndex) {       
        threads.get(1).addSteadyThreadWait( threadIndex);
    }

    /**
     * 
     * @param steadyBlock
     */
    public void addThreadHelper(int threadIndex, int nextIndex,
            JStatement steadyBlock) {
        threads.get(1).addThreadHelper( threadIndex,  nextIndex,
                steadyBlock);              
    }

    /**
     * 
     * @param steadyBlock
     */
    public void addThreadHelper(int threadIndex, JStatement steadyBlock) {
        threads.get(1).addThreadHelper(threadIndex, steadyBlock);                
    }

    /**
     * Append str to the text that will appear outside of any function near the
     * top of the code for this core.
     * 
     * @param str
     *            The string to add
     */
    public void appendTxtToGlobal(String str) {
        threads.get(1).appendTxtToGlobal(str);
    }

    public void generateNumbersCode() {
        threads.get(1).generateNumbersCode();                
    }

    /**
     * Return the method that initializes the rotating buffers and communicates
     * addresses.
     * 
     * @return the method that initializes the rotating buffers and communicates
     *         addresses.
     */
    public JMethodDeclaration getBufferInitMethod() {
        return threads.get(1).getBufferInitMethod();
    }

    /**
     * Return the helper thread methods in this store;
     * 
     * @return the helper thread methods in this store;
     */
    public Set<JMethodDeclaration> getDynamicThreadHelperMethods() {        
        return threads.get(1).getDynamicThreadHelperMethods();
    }

    public Map<String, JFieldDeclaration> getExternFields() {
        return threads.get(1).getExternFields();
    }

    /**
     * return all of the filters that are mapped to this core.
     * 
     * @return all of the filters that are mapped to this core.
     */
    public Set<WorkNode> getFilters() {
        return threads.get(1).getFilters();
    }

    /**
     * Return the string to add to the global portion of the c file
     * 
     * @return the string to add to the global portion of the c file
     */
    public String getGlobalText() {
        return threads.get(1).getGlobalText();        
    }

    /**
     * get name for MAIN method in this code store.
     * 
     * @return name from a JMethodDeclaration
     */
    public String getMyMainName() {
        if (threads == null || threads.get(1) == null) {
            return "";
        }        
        return threads.get(1).getMyMainName();
    }

    public Core getParent() {
        return threads.get(1).getParent(); 
    }

    /**
     * Set the helper thread methods in this store;
     * 
     * @param methods
     *            the helper thread methods in this store;
     */
    public void setDynamicThreadHelperMethods(Set<JMethodDeclaration> methods) {
        threads.get(1).setDynamicThreadHelperMethods(methods);
    }

    /**
     * Set that this core (code store) has code written to it and thus it needs
     * to be considered during code generation.
     */
    public void setHasCode() {
        threads.get(1).setHasCode();
    }

    /**
     * Set name of main function
     * 
     * @param main
     *            The new name of the main function
     */
    public void setMainName(String mainName) {
        threads.get(1).setMainName(mainName);       
    }

    /**
     * Return true if we should generate code for this core, false if no code
     * was ever generated for this core.
     * 
     * @return true if we should generate code for this core, false if no code
     *         was ever generated for this core.
     */
    public boolean shouldGenerateCode() {        
        return threads.get(1).shouldGenerateCode();
    }


    /////////////////////////// BEGIN INTERFACE ////////////////////////////



    public void addCleanupStatement(JStatement stmt) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).addCleanupStatement(stmt);
        }
    }

    public void addField(JFieldDeclaration field) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).addField(field);
        }
    }

    @Override
    public void addFields(JFieldDeclaration[] f) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).addFields(f);
        }
    }

    public void addInitFunctionCall(JMethodDeclaration init) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).addInitFunctionCall(init);
        }
    }

    public void addInitStatement(JStatement stmt) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).addInitStatement(stmt);
        }
    }

    public void addInitStatementFirst(JStatement stmt) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).addInitStatementFirst(stmt);
        }
    }

    @Override
    public void addMethod(JMethodDeclaration method) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).addMethod(method);
        }
    }

    @Override
    public void addMethods(JMethodDeclaration[] m) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).addMethods(m);
        }
    }

    public void addSteadyLoop() {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).addSteadyLoop();
        }
    }

    public void addSteadyLoop(ALocalVariable iterationBound) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).addSteadyLoop(iterationBound);
        }
    }

    public void addSteadyLoopStatement(JStatement stmt) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).addSteadyLoopStatement(stmt);
        }
    }

    public void addSteadyLoopStatementFirst(JStatement stmt) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).addSteadyLoopStatementFirst(stmt);
        }
    }

    @Override
    public JFieldDeclaration[] getFields() {
        if (threads != null && threads.get(1) != null) {
            return threads.get(1).getFields();
        }
        return fields;
    }

    public JMethodDeclaration getMainFunction() {
        if (threads != null && threads.get(1) != null) {
            return threads.get(1).getMainFunction();
        }
        return mainMethod;
    }

    @Override
    public JMethodDeclaration[] getMethods() {
        if (threads != null && threads.get(1) != null) {
            return threads.get(1).getMethods();
        }
        return methods;
    }

    public boolean hasMethod(JMethodDeclaration meth) {
        if (threads != null && threads.get(1) != null) {
            return threads.get(1).hasMethod(meth);
        }
        return false;
    }

    public void setFields(JFieldDeclaration[] f) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).setFields(f);
        }
    }

    public void setMainFunction(JMethodDeclaration mainMethod) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).setMainFunction(mainMethod);
        }
    }

    public void setMethods(JMethodDeclaration[] m) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).setMethods(m);
        }
    }

    public void setMyMainName(String mainName) {
        if (threads != null && threads.get(1) != null) {
            threads.get(1).setMyMainName(mainName);
        }
    }


}
