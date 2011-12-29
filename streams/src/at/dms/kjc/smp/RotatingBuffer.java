package at.dms.kjc.smp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import at.dms.kjc.CType;
import at.dms.kjc.JArrayAccessExpression;
import at.dms.kjc.JExpression;
import at.dms.kjc.JFieldAccessExpression;
import at.dms.kjc.JStatement;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.backendSupport.BasicSpaceTimeSchedule;
import at.dms.kjc.backendSupport.IntraSSGChannel;
import at.dms.kjc.slir.IntraSSGEdge;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.slir.WorkNodeInfo;

/**
 * A rotating buffer represents a block of memory that a filter reads from or
 * writes to that is rotated because we are double buffering. This class
 * generates code that implements initialization of all the buffers for the
 * application including allocation, setting up the rotation structure, and
 * communicating shared addresses.
 * 
 * Note the we are not using extraCount of Channel for double buffering
 * accounting, instead we are using rotationLength.
 * 
 * @author mgordon
 * 
 */
public abstract class RotatingBuffer extends IntraSSGChannel {

	/**
	 * Return the input buffer associated with the filter node.
	 * 
	 * @param fsn
	 *            The filter node in question.
	 * @return The input buffer of the filter node.
	 */
	public static InputRotatingBuffer getInputBuffer(WorkNode fsn) {
		if (!inputBuffers.containsKey(fsn) && KjcOptions.sharedbufs
				&& FissionGroupStore.isFizzed(fsn.getParent())
				&& FissionGroupStore.isUnfizzedSlice(fsn.getParent())) {
			return inputBuffers.get(FissionGroupStore.getFizzedSlices(fsn
					.getParent())[0].getWorkNode());
		} else {
			return inputBuffers.get(fsn);
		}
	}

	/**
	 * Return the set of all the InputBuffers that are mapped to Core t.
	 */
	public static Set<InputRotatingBuffer> getInputBuffersOnCore(Core t) {
		HashSet<InputRotatingBuffer> set = new HashSet<InputRotatingBuffer>();
		for (InputRotatingBuffer b : inputBuffers.values()) {
			if (SMPBackend.scheduler.getComputeNode(b.getFilterNode())
					.equals(t))
				set.add(b);
		}

		return set;
	}

	public static RotatingBuffer getOutputBuffer(WorkNode fsn) {
		if (!outputBuffers.containsKey(fsn) && KjcOptions.sharedbufs
				&& FissionGroupStore.isFizzed(fsn.getParent())
				&& FissionGroupStore.isUnfizzedSlice(fsn.getParent())) {
			assert FissionGroupStore.isUnfizzedSlice(fsn.getParent());
			return outputBuffers.get(FissionGroupStore.getFizzedSlices(fsn
					.getParent())[0].getWorkNode());
		} else {
			return outputBuffers.get(fsn);
		}
	}

	/**
	 * Return the set of all the InputBuffers that are mapped to Core t.
	 */
	public static Set<RotatingBuffer> getOutputBuffersOnCore(Core t) {
		HashSet<RotatingBuffer> set = new HashSet<RotatingBuffer>();
		for (RotatingBuffer b : outputBuffers.values()) {
			if (SMPBackend.scheduler.getComputeNode(b.getFilterNode())
					.equals(t))
				set.add(b);
		}

		return set;
	}

	public static void setInputBuffer(WorkNode node, InputRotatingBuffer buf) {
		inputBuffers.put(node, buf);
	}

	public static void setOutputBuffer(WorkNode node, OutputRotatingBuffer buf) {
		outputBuffers.put(node, buf);
	}

	/** the core this buffer is mapped to */
	protected Core parent;

	/** the filter this buffer is associated with */
	protected WorkNode filterNode;
	/** the filter info object for the filter that contains this buffer */
	protected WorkNodeInfo filterInfo;

	/** the names of the individual buffers */
	protected String[] bufferNames;
	/** array size in elements of each buffer of the rotation */
	protected int bufSize;

	/** type of array: array of element type */
	protected CType bufType;

	/** the data transfer statements that are generated for this output buffer */
	protected BufferTransfers transferCommands;

	/** a set of all the buffer types in the application */
	protected static HashSet<String> types;

	/** prefix of the variable name for the rotating buffers */
	public static String rotTypeDefPrefix = "__rotating_buffer_";

	/** maps each WorkNode to Input/OutputRotatingBuffers */
	protected static HashMap<WorkNode, InputRotatingBuffer> inputBuffers;

	protected static HashMap<WorkNode, OutputRotatingBuffer> outputBuffers;

	static {
		types = new HashSet<String>();
		inputBuffers = new HashMap<WorkNode, InputRotatingBuffer>();
		outputBuffers = new HashMap<WorkNode, OutputRotatingBuffer>();
	}

	/**
	 * Generate the code necessary to communicate the addresses of the shared
	 * input buffers of all input rotational structures to the sources that will
	 * write to the buffer
	 * 
	 * @param schedule
	 */
	protected static void communicateAddresses(BasicSpaceTimeSchedule schedule) {
		// handle all the filters that are mapped to compute cores
		// this will handle all filters except file writers and file readers

		StaticSubGraph ssg = schedule.getSSG();

		for (Core ownerCore : SMPBackend.chip.getCores()) {
			SMPComputeCodeStore cs = ownerCore.getComputeCode();
									
			for (WorkNode filter : cs.getFilters()) {
				if (ssg.containsFilter(filter.getParent())) {
					// System.out.println("RotatingBuffer.communicateAddresses filter="
					// + filter.toString());
					communicateAddressesForFilter(filter, ownerCore);
				}
			}
		}

		// now handle the file writers
		for (WorkNode fileWriter : ProcessFileWriter.getFileWriterFilters())
			communicateAddressesForFilter(fileWriter,
					ProcessFileWriter.getAllocatingCore(fileWriter));

		// now handle the file readers

	}

	private static void communicateAddressesForFilter(WorkNode filter,
			Core ownerCore) {
		InputRotatingBuffer buf = RotatingBuffer.getInputBuffer(filter);

		// if this filter does not have an input buffer, then continue
		if (buf == null)
			return;

		for (SourceAddressRotation addr : buf.getAddressBuffers()) {
			Core srcCore = addr.parent;

			// we might have a file reader as a source, if so, don't send the
			// addresses to it
			if (!srcCore.isComputeNode())
				continue;

			// create declarations of the pointers to shared buffers on the
			// source core
			addr.declareBuffers();

			// communicate addresses of shared buffers
			for (int b = 0; b < buf.rotationLength; b++)
				srcCore.getComputeCode().addStatementToBufferInit(
						addr.bufferNames[b] + " = " + buf.bufferNames[b]);

			// setup the rotation structure at the source
			addr.setupRotation();
		}
	}

	/**
	 * Create all the input and output buffers necessary for the slice graph.
	 * Each filter that produces output will have an output buffer and each
	 * filter that expects input will have an input buffer.
	 * 
	 * This call also creates code for allocating the rotating buffers and
	 * communicating the addresses of shared buffers.
	 * 
	 * @param schedule
	 *            The spacetime schedule of the application
	 */

	public static void createBuffers(BasicSpaceTimeSchedule schedule) {
		// have to create input buffers first because when we have a lack of a
		// shared input buffer, we create an output buffer
		InputRotatingBuffer.createInputBuffers(schedule);
		OutputRotatingBuffer.createOutputBuffers(schedule);

		// now that all the buffers are created, create the pointers to them
		// that live on other cores, and create the transfer commands
		for (InputRotatingBuffer buf : inputBuffers.values()) {
			buf.createAddressBuffers();
			buf.createTransferCommands();
		}
		for (OutputRotatingBuffer buf : outputBuffers.values()) {
			buf.createAddressBuffers();
			buf.createTransferCommands();
		}

		// now add the typedefs needed for the rotating buffers to structs.h
		// rotTypeDefs();

		// now that all the buffers are allocated, we create a barrier on all
		// the cores
		// so that we wait for all the shared memory to be allocated
		SMPComputeCodeStore.addBufferInitBarrier();
		// generate the code for the address communication stage
		communicateAddresses(schedule);

	}

	/**
	 * Create the typedef for the rotating buffer structure, one for each type
	 * we see in the program (each channel type).
	 */
	public static void rotTypeDefs() {
		for (String type : types) {
			SMPBackend.structs_h.addLineSC("typedef struct __rotating_struct_"
					+ type + "__" + " *__rot_ptr_" + type + "__");
			SMPBackend.structs_h.addText("typedef struct __rotating_struct_"
					+ type + "__ {\n");
			SMPBackend.structs_h.addText("\t" + type + " *buffer;\n");
			SMPBackend.structs_h.addText("\t__rot_ptr_" + type + "__ next;\n");
			SMPBackend.structs_h
					.addText("} " + rotTypeDefPrefix + type + ";\n");
		}
	}

	protected RotatingBuffer(IntraSSGEdge edge, WorkNode fsn, Core parent) {
		super(edge);
		this.parent = parent;
		filterNode = fsn;
		filterInfo = WorkNodeInfo.getFilterInfo(fsn);
	}

	/**
	 * Allocate the constituent buffers of this rotating buffer structure
	 */
	protected void allocBuffers() {
		for (int i = 0; i < rotationLength; i++) {
			SMPComputeCodeStore cs;

			// if we have a file writer then the code has to be put on the
			// allocating core not the off chip memory!
			// else we are dealing with a regular buffer on a core, put on
			// parent core
			if (filterNode.isFileOutput())
				cs = ProcessFileWriter.getAllocatingCore(filterNode)
						.getComputeCode();
			else
				cs = this.parent.getComputeCode();

			if (KjcOptions.sharedheap) {
				// add the declaration to the global header file of the extern
				// variable
				this.parent
						.getMachine()
						.getOffChipMemory()
						.getComputeCode()
						.appendTxtToGlobal(
								"extern " + this.getType().toString() + "* "
										+ bufferNames[i] + ";\n");
				// add the definition to the file
				cs.appendTxtToGlobal(this.getType().toString() + "* "
						+ bufferNames[i] + ";\n");
				// create the allocation at the beginning of the buffer and
				// address init method
				cs.addStatementFirstToBufferInit(bufferNames[i] + " = ("
						+ this.getType().toString() + "*) malloc("
						+ this.getBufferSize() + " * sizeof("
						+ this.getType().toString() + "))");

			} else {

				this.parent
						.getMachine()
						.getOffChipMemory()
						.getComputeCode()
						.appendTxtToGlobal(
								"extern " + this.getType().toString() + " "
										+ bufferNames[i] + "["
										+ this.getBufferSize() + "];\n");

				cs.appendTxtToGlobal(this.getType().toString() + " "
						+ bufferNames[i] + "[" + this.getBufferSize() + "];\n");
			}
			/*
			 * //create pointers to constituent buffers
			 * this.parent.getMachine().
			 * getOffChipMemory().getComputeCode().appendTxtToGlobal( "extern "
			 * + this.getType().toString() + "* " + bufferNames[i] + ";\n");
			 * 
			 * cs.appendTxtToGlobal(this.getType().toString() + "* " +
			 * bufferNames[i] + ";\n");
			 * 
			 * //malloc the steady buffer cs.addStatementToBufferInit(new
			 * JExpressionStatement(new JEmittedTextExpression( bufferNames[i] +
			 * " = (" + this.getType() + "*) malloc(" + this.getBufferSize() +
			 * " * sizeof(" + this.getType() + "))")));
			 */
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#beginSteadyRead()
	 */
	public List<JStatement> beginPrimePumpRead() {
		return new LinkedList<JStatement>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#beginSteadyWrite()
	 */
	public List<JStatement> beginPrimePumpWrite() {
		return new LinkedList<JStatement>();
	}

	public void createAddressBuffers() {

	}

	/**
	 * Generate the code necessary to allocate the buffers, setup the rotation
	 * structure, and communicate addresses.
	 * 
	 * @param input
	 *            true if this is an input buffer
	 */
	protected void createInitCode() {
		this.setBufferNames();
		this.allocBuffers();
		this.setupRotation();
	}

	public void createTransferCommands() {
		if (KjcOptions.sharedbufs)
			transferCommands = new SharedBufferRemoteWritesTransfers(this);
		else
			transferCommands = new BufferRemoteWritesTransfers(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#endSteadyRead()
	 */
	public List<JStatement> endPrimePumpRead() {
		return new LinkedList<JStatement>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#endSteadyWrite()
	 */
	public List<JStatement> endPrimePumpWrite() {
		return new LinkedList<JStatement>();
	}

	/**
	 * Return the number of elements for each rotation of this buffer
	 * 
	 * @return the maximum size for this buffer for one rotation
	 */
	public int getBufferSize() {
		return bufSize;
	}

	/**
	 * DO NOT USE, WE ARE NOT USING EXTRACOUNT FOR DOUBLE BUFFERING ACCOUNTING!
	 */
	@Override
	public int getExtraCount() {
		assert false;
		return extraCount;
	}

	/**
	 * Return the filter this buffer is associated with.
	 * 
	 * @return Return the filter this buffer is associated with.
	 */
	public WorkNode getFilterNode() {
		return filterNode;
	}

	/**
	 * Return the number of buffers that comprise this rotating buffer.
	 * 
	 * @return the number of buffers that comprise this rotating buffer.
	 */
	public int getRotationLength() {
		return rotationLength;
	}

	public abstract JArrayAccessExpression readBufRef(JExpression offset);

	/**
	 * Set the names of the buffers that comprise this rotating buffer.
	 */
	protected void setBufferNames() {
		bufferNames = new String[rotationLength];
		for (int i = 0; i < rotationLength; i++) {
			bufferNames[i] = this.getIdent() + "_Buf_" + i;
		}
	}

	protected abstract void setBufferSize();

	/**
	 * DO NOT USE, WE ARE NOT USING EXTRACOUNT FOR DOUBLE BUFFERING ACCOUNTING!
	 */
	@Override
	public void setExtraCount(int extracount) {
		assert false;
		this.extraCount = extracount;
	}

	/**
	 * Generate the code to setup the structure of the rotating buffer as a
	 * circular linked list.
	 */
	protected abstract void setupRotation();

	/** Create an array reference given an offset */
	public abstract JFieldAccessExpression writeBufRef();
}
