package at.dms.kjc.smp;

import at.dms.compiler.JavaStyleComment;
import at.dms.compiler.TokenReference;
import at.dms.kjc.backendSupport.ComputeCodeStore;
import at.dms.kjc.common.ALocalVariable;
import at.dms.kjc.*;

import java.util.HashSet;
import java.util.Set;

import at.dms.kjc.slir.*;
import at.dms.util.Utils;

public class CoreCodeStore extends ComputeCodeStore<Core> {
	/**
	 * Append a barrier instruction to all of the cores in the init/primepump
	 * stage.
	 */
	public static void addBarrierInit() {
		// don't do anything for the single core case
		// if(SMPBackend.chip.size() == 1)
		// return;

		for (int t = 0; t < SMPBackend.chip.size(); t++) {
			CoreCodeStore cs = SMPBackend.chip.getNthComputeNode(t)
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
			CoreCodeStore cs = SMPBackend.chip.getNthComputeNode(t)
					.getComputeCode();
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
			CoreCodeStore cs = SMPBackend.chip.getNthComputeNode(t)
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

			CoreCodeStore codeStore = core.getComputeCode();

			FileOutputContent fileOutput = (FileOutputContent) fileW
					.getFilter();

			codeStore.addPrintOutputCode(buf, firstInputFilter);
			codeStore.appendTxtToGlobal("FILE *output;\n");
			codeStore.addStatementFirstToBufferInit(Util
					.toStmt("output = fopen(\"" + fileOutput.getFileName()
							+ "\", \"w\")"));

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
	protected HashSet<WorkNode> filters;

	/**
	 * Constructor: steady state loops indefinitely, no pointer back to compute
	 * node.
	 */
	public CoreCodeStore() {
		super();
		setMyMainName("__main__");
	}

	/**
	 * Constructor: caller will add code to bound number of iterations, no
	 * pointer back to compute node.
	 * 
	 * @param iterationBound
	 *            a variable that will be defined locally by
	 *            <code>getMainFunction().addAllStatments(0,stmts);</code>
	 */
	public CoreCodeStore(ALocalVariable iterationBound) {
		super(iterationBound);
		setMyMainName("__main__");
	}

	public CoreCodeStore(Core nodeType) {
		super(nodeType);
		setMyMainName("__main__");
		filters = new HashSet<WorkNode>();
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
	public CoreCodeStore(Core parent, ALocalVariable iterationBound) {
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
		assert fileW.isFileOutput();
		// because of this scene we need a rotation length of 2
		assert buf.getRotationLength() == 2;
		// make sure that each of the inputs wrote to the file writer in the
		// primepump stage
		for (InterFilterEdge edge : fileW.getParent().getInputNode()
				.getSourceSet(SchedulingPhase.STEADY)) {
			assert SMPBackend.scheduler.getGraphSchedule().getPrimePumpMult(
					edge.getSrc().getParent()) == 1;
		}
		int outputs = fileW.getFilter().getSteadyMult();
		String type = ((FileOutputContent) fileW.getFilter()).getType() == CStdType.Integer ? "%d"
				: "%f";
		String cast = ((FileOutputContent) fileW.getFilter()).getType() == CStdType.Integer ? "(int)"
				: "(float)";
		String bufferName = buf.getAddressRotation(filter).currentWriteBufName;
		// create the loop
		addSteadyLoopStatement(Util.toStmt("for (int _i_ = 0; _i_ < " + outputs
				+ "; _i_++) fprintf(output, \"" + type + "\\n\", " + cast
				+ bufferName + "[_i_])"));
	}

	public void addReaderThread() {

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

	// TODO: fix this to add more code into the thread
	public void addThreadHelper(JStatement steadyBlock) {
		System.out.println("CoreCodeStore.addThreadHelper called()");
		
		JBlock methodBody = new JBlock();
		JBlock whileBody = new JBlock();
		// JBlock ifBody = new JBlock();
				
		Utils.addCondWait(whileBody, "helperLock", "helperMutex", "helperCond", 
				Utils.makeEqualityCondition("ASLEEP", "helperFlag"));	

		//ifBody.addStatement(Util.toStmt("/* Set Downstream Multiplier */"));

		//Utils.addSetFlag(ifBody, "masterLock", "masterFlag", "AWAKE");
		//Utils.addSignal(ifBody, "masterCond");	
		//Utils.addCondWait(ifBody, "masterLock", "masterMutex", "masterCond", 
		//		Utils.makeEqualityCondition("ASLEEP", "masterFlag"));				

		//JBlock block = (JBlock)steadyBlock;
		
		// ifBody.addStatement(steadyBlock);
		
		// JIfStatement ifStatement = Utils.makeIfStatement(Utils.makeEqualityCondition("size", "0"), ifBody);						
		
		
		
		//block.addStatement(ifStatement);	
		
		whileBody.addStatement(steadyBlock);
		
		JStatement whileLoop = new JWhileStatement(null, new JBooleanLiteral(
				null, true), whileBody, null);
		
		//addSteadyLoopStatement(ifStatement);
		
		// block.addStatement(steadyBlock);
						
		methodBody.addStatement(whileLoop);
		
		JMethodDeclaration threadHelper = new JMethodDeclaration(CStdType.Void, "runMyThread", new JFormalParameter[0],
				methodBody);
		
		addMethod(threadHelper);		
	}	

	// TODO: Fix this so that it calls the thread function
	public void addSteadyThreadCall() {
		System.out.println("CoreCodeStore.addSteadyThreadCall called()");
		Utils.addSetFlag(steadyLoop, "masterLock", "masterFlag", "ALSEEP");
		Utils.addSetFlag(steadyLoop, "helperLock", "readerFlag", "AWAKE");
		Utils.addSignal(steadyLoop, "helperCond");	
		Utils.addCondWait(steadyLoop, "masterLock", "masterMutex", "masterCond", 
					Utils.makeEqualityCondition("ASLEEP", "masterFlag"));		
	}
		
}
