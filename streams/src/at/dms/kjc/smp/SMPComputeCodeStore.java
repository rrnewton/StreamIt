package at.dms.kjc.smp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.dms.kjc.CStdType;
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
import at.dms.kjc.JWhileStatement;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.backendSupport.ComputeCodeStore;
import at.dms.kjc.common.ALocalVariable;
import at.dms.kjc.slir.OutputContent;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.WorkNode;
import at.dms.util.Utils;

public class SMPComputeCodeStore extends ComputeCodeStore<Core> {

	/**
	 * The set of token names used for synchronization within
	 * an SSG for non-pipelined filters.
	 */
	private static Set<String> tokenNames = new HashSet<String>();

	/**
	 * Get the set of token names used for intra-SSG synchronization.
	 * @return the set of token names.
	 */
	public static Set<String> getTokenNames() {
		return tokenNames;
	}

	/**
	 * Add a new name to the set of token names used for 
	 * intra-SSG synchronization.
	 * @param token the token name.
	 */
	public static void addTokenName(String token) {
		tokenNames.add(token);
	}

	/**
	 * Append a barrier instruction to all of the cores in the init/primepump
	 * stage.
	 */
	public static void addBarrierInit() {
		// don't do anything for the single core case
		// if(SMPBackend.chip.size() == 1)
		// return;

		for (int t = 0; t < SMPBackend.chip.size(); t++) {
			SMPComputeCodeStore cs = SMPBackend.chip.getNthComputeNode(t)
					.getComputeCode();
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
			SMPComputeCodeStore cs = SMPBackend.chip.getNthComputeNode(t)
					.getComputeCode();
			cs.addSteadyLoopStatement(Util.toStmt("/* Steady-State Barrier */"));
			cs.addSteadyLoopStatement(Util.toStmt("barrier_wait(&barrier)"));
			cs.setHasCode();
		}
	}

	private static boolean addBufferInitBarrierFlag = false;

	/**
	 * Append a barrier instruction to all of the cores in the buffer init
	 * method.
	 */
	public static void addBufferInitBarrier() {
		// don't do anything for the single core case
		if (SMPBackend.chip.size() == 1)
			return;

		for (int t = 0; t < SMPBackend.chip.size(); t++) {
			SMPComputeCodeStore cs = SMPBackend.chip.getNthComputeNode(t)
					.getComputeCode();
			cs.addStatementToBufferInit("barrier_wait(&barrier)");
			cs.setHasCode();
		}
	}

	/**
	 * For each of the file writers generate code to print the output for each
	 * steady state. This is somewhat limited right now, so there are lots of
	 * asserts.
	 */
	public static void generatePrintOutputCode() {
		Set<InputRotatingBuffer> fwb = InputRotatingBuffer
				.getFileWriterBuffers();
		if (fwb.size() == 0)
			return;
		// for now asser that we only have one file writer
		assert fwb.size() == 1;
		// for each of the file writer input buffers, so for each of the file
		// writers,
		// find one of its sources, and add code to the source's core to print
		// the outputs
		// at the end of each steady state
		for (InputRotatingBuffer buf : fwb) {
			WorkNode fileW = buf.filterNode;

			// find the core of the first input to the file writer
			WorkNode firstInputFilter = fileW.getParent().getInputNode()
					.getSources(SchedulingPhase.STEADY)[0].getSrc().getParent()
					.getWorkNode();

			if (KjcOptions.sharedbufs
					&& FissionGroupStore.isFizzed(firstInputFilter.getParent()))
				firstInputFilter = FissionGroupStore
						.getFizzedSlices(firstInputFilter.getParent())[0]
						.getWorkNode();

			Core core = SMPBackend.scheduler.getComputeNode(firstInputFilter);

			SMPComputeCodeStore codeStore = core.getComputeCode();

			OutputContent fileOutput = (OutputContent) fileW
					.getWorkNodeContent();

			codeStore.addPrintOutputCode(buf, firstInputFilter);
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
	}

	/** True if this CoreCodeStore has code appended to it */
	private boolean hasCode = false;
	/** Code block containing declarations of all buffers */
	protected JBlock bufferDecls = new JBlock();

	/**
	 * The method that will malloc the buffers and receive addresses from
	 * downstream cores
	 */
	protected JMethodDeclaration bufferInit;

	/** The name of the bufferInit method */
	public static final String bufferInitMethName = "buffer_and_address_init";

	/** Any text that should appear outside a function declaration in the c code */
	private StringBuffer globalTxt = new StringBuffer();

	/** set of FilterSliceNodes that are mapped to this core */
	protected Set<WorkNode> filters;

	/** set of JMethodDeclaration for the helper thread on this core */
	protected Set<JMethodDeclaration> helperThreadMethods = new HashSet<JMethodDeclaration>();

	/**
	 * Set of fields that should be externally defined on this core. I am using
	 * a Map to store values because JFieldDeclaration does not define equals
	 * and hashCode, so storing in a Set does not prevent duplicates correctly.
	 */
	protected Map<String, JFieldDeclaration> externFields = new HashMap<String, JFieldDeclaration>();

	/**
	 * Constructor: steady state loops indefinitely, no pointer back to compute
	 * node.
	 */
	public SMPComputeCodeStore() {
		super();
		setMyMainName("__main__");
	}

	public SMPComputeCodeStore(Core nodeType) {
		super(nodeType);
		setMyMainName("__main__");
		filters = new HashSet<WorkNode>();
		helperThreadMethods = new HashSet<JMethodDeclaration>();

		createBufferInitMethod();

		mainMethod.addParameter(new JFormalParameter(CVoidPtrType.VoidPtr,
				"arg"));
		mainMethod.setReturnType(CVoidPtrType.VoidPtr);
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
		setMyMainName("__main__");
		filters = new HashSet<WorkNode>();
		createBufferInitMethod();

		mainMethod.addParameter(new JFormalParameter(CVoidPtrType.VoidPtr,
				"arg"));
		mainMethod.setReturnType(CVoidPtrType.VoidPtr);
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

	/**
	 * Add code to print the output written to the file writer mapped to this
	 * core.
	 */
	private void addPrintOutputCode(InputRotatingBuffer buf, WorkNode filter) {
		// We print the address buffer after it has been rotated, so that it
		// points to the section
		// of the filewriter buffer that is about to be written to, but was
		// written to 2 steady-states
		// ago

		WorkNode fileW = buf.filterNode;

		System.out.println("SMPComputeCodeStore.addPrintOutputCode filter="
				+ filter.toString() + " fileW.isFileOutput()="
				+ fileW.isFileOutput() + " buf=" + buf.toString()
				+ " buf.getRotationLength()=" + buf.getRotationLength());

		assert fileW.isFileOutput();
		// because of this scene we need a rotation length of 2
		// assert buf.getRotationLength() == 2: buf.getRotationLength();
		// make sure that each of the inputs wrote to the file writer in the
		// primepump stage
		/*
		 * for (InterFilterEdge edge : fileW.getParent().getInputNode()
		 * .getSourceSet(SchedulingPhase.STEADY)) { //assert
		 * SMPBackend.scheduler.getGraphSchedule().getPrimePumpMult( //
		 * edge.getSrc().getParent()) == 1:
		 * SMPBackend.scheduler.getGraphSchedule().getPrimePumpMult( //
		 * edge.getSrc().getParent()); }
		 */
		int outputs = fileW.getWorkNodeContent().getSteadyMult();
		String type = ((OutputContent) fileW.getWorkNodeContent()).getType() == CStdType.Integer ? "%d"
				: "%f";
		String cast = ((OutputContent) fileW.getWorkNodeContent()).getType() == CStdType.Integer ? "(int)"
				: "(float)";
		String bufferName = buf.getAddressRotation(filter).currentWriteBufName;
		// create the loop
		
		
		String stmt = "";
		if (KjcOptions.outputs < 0) {
		    stmt = "for (int _i_ = 0; _i_ < " + outputs + "; _i_++) { " 
                    +    "fprintf(output, \"" + type + "\\n\", " + cast + bufferName + "[_i_]); "       
                    + "}";
            
		} else {
		    stmt = "for (int _i_ = 0; _i_ < " + outputs + "; _i_++) { " 
                    +    "fprintf(output, \"" + type + "\\n\", " + cast + bufferName + "[_i_]); "
                    +    "if (--maxOutputs == 0) {  exit(0); } "
		            + "}";
		}
						
		addSteadyLoopStatement(Util.toStmt(stmt));
						
	}	

	/**
	 * Add stmt to the beginning of the method that will perform the allocation
	 * of buffers and receive addresses of buffers from downstream cores.
	 * 
	 * @param stmt
	 *            The statement to add to the end of the method
	 */
	public void addStatementFirstToBufferInit(JStatement stmt) {
		bufferInit.getBody().addStatementFirst(stmt);
		this.setHasCode();
	}

	public void addExpressionFirst(JExpression expr) {
		mainMethod.addStatementFirst(new JExpressionStatement(expr));
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
		bufferInit.getBody().addStatementFirst(stmt);
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
		bufferInit.getBody().addStatement(stmt);
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
		bufferInit.getBody().addStatement(stmt);
		this.setHasCode();
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

	private void createBufferInitMethod() {
		// create the method that will malloc the buffers and receive the
		// addresses from downstream cores
		bufferInit = new JMethodDeclaration(CStdType.Void, bufferInitMethName
				+ "__n" + parent.getCoreID(), new JFormalParameter[0],
				new JBlock());
		// addMethod(bufferInit);
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
	 * return all of the filters that are mapped to this core.
	 * 
	 * @return all of the filters that are mapped to this core.
	 */
	public Set<WorkNode> getFilters() {
		return filters;
	}

	/**
	 * Return the string to add to the global portion of the c file
	 * 
	 * @return the string to add to the global portion of the c file
	 */
	public String getGlobalText() {
		return globalTxt.toString();
	}

	public Core getParent() {
		return parent;
	}

	/**
	 * Set that this core (code store) has code written to it and thus it needs
	 * to be considered during code generation.
	 */
	public void setHasCode() {
		hasCode = true;
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
	 * 
	 * @param steadyBlock
	 */
	public void addThreadHelper(int threadIndex, JStatement steadyBlock) {

		System.out.println("SMPComputeCodeStore.addThreadHelper called()");

		JBlock methodBody = new JBlock();
		JBlock loopBody = new JBlock();
		Utils.addCondWait(
				loopBody,
				threadIndex,
				"DYN_READER",
				"DYN_READER",
				Utils.makeEqualityCondition("ASLEEP", "thread_to_sleep["
						+ threadIndex + "][DYN_READER]"));
		loopBody.addStatement(steadyBlock);
		Utils.addSetFlag(loopBody, threadIndex, "DYN_READER", "DYN_READER",
				"ASLEEP");
		Utils.addSetFlag(loopBody, threadIndex, "MASTER", "MASTER", "AWAKE");
		Utils.addSignal(loopBody, threadIndex, "MASTER");

		JStatement loop = null;
		if (KjcOptions.iterations != -1) {
			// addSteadyLoop(iterationBound);
			ALocalVariable var = ALocalVariable.makeVar(CStdType.Integer,
					"maxSteadyIter");
			loop = at.dms.util.Utils.makeForLoop(loopBody, var.getRef());

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
				.println("SMPComputeCodeStore.addThreadHelper creating JMethodDeclaration="
						+ threadName);

		JMethodDeclaration threadHelper = new JMethodDeclaration(
				CVoidPtrType.VoidPtr, threadName, new JFormalParameter[] { p },
				methodBody);
		addHelperThreadMethod(threadHelper);
	}

	private void addHelperThreadMethod(JMethodDeclaration threadHelper) {
		helperThreadMethods.add(threadHelper);
		addMethod(threadHelper);
	}

	public void addSteadyThreadCall(int threadIndex) {
		Utils.addSetFlag(steadyLoop, threadIndex, "MASTER", "MASTER", "ASLEEP");
		Utils.addSetFlag(steadyLoop, threadIndex, "DYN_READER", "DYN_READER",
				"AWAKE");
		Utils.addSignal(steadyLoop, threadIndex, "DYN_READER");
		Utils.addCondWait(
				steadyLoop,
				threadIndex,
				"MASTER",
				"MASTER",
				Utils.makeEqualityCondition("ASLEEP", "thread_to_sleep["
						+ threadIndex + "][MASTER]"));
	}

	public void addExternField(JFieldDeclaration jFieldDeclaration) {
		// There is a problem with this code. We do not want to store
		// duplicates of the same jFieldDeclaration. However, the
		// JFieldDeclaration
		// class does not define equals and hashCode, so the Set implementation
		// does not know about duplicate entries. Rather than changing the
		// Kopi compiler, I'm going to check for equality here by comparing
		// the names and types.
		externFields.put(jFieldDeclaration.getVariable().getIdent(),
				jFieldDeclaration);
	}

	public Map<String, JFieldDeclaration> getExternFields() {
		return externFields;
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
	 * Set the helper thread methods in this store;
	 * 
	 * @param methods
	 *            the helper thread methods in this store;
	 */
	public void setDynamicThreadHelperMethods(Set<JMethodDeclaration> methods) {
		helperThreadMethods = methods;
	}

}
