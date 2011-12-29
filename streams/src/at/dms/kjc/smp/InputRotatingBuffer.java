package at.dms.kjc.smp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import at.dms.classfile.Constants;
import at.dms.kjc.CClassType;
import at.dms.kjc.CStdType;
import at.dms.kjc.CType;
import at.dms.kjc.JArrayAccessExpression;
import at.dms.kjc.JBlock;
import at.dms.kjc.JEmittedTextExpression;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JFieldAccessExpression;
import at.dms.kjc.JFormalParameter;
import at.dms.kjc.JLocalVariableExpression;
import at.dms.kjc.JMethodCallExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JStatement;
import at.dms.kjc.JThisExpression;
import at.dms.kjc.JVariableDefinition;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.backendSupport.BasicSpaceTimeSchedule;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InterFilterEdge;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.slir.WorkNodeInfo;
import at.dms.kjc.slir.fission.FissionGroup;
import at.dms.util.Utils;

/**
 * 
 * 
 * @author mgordon
 * 
 */
public class InputRotatingBuffer extends RotatingBuffer {

	/**
	 * return all the input buffers of the file writers of this application
	 */
	public static Set<InputRotatingBuffer> getFileWriterBuffers() {
		return fileWriterBuffers;
	}

	/** the name of the read rotation structure, (always points to its head) */
	protected String readRotStructName;
	/** the name of the pointer to the current read rotation of this buffer */
	protected String currentReadRotName;

	/** the name of the pointer to the read buffer of the current rotation */
	protected String currentReadBufName;
	/**
	 * all the address buffers that are on the cores that feed this input buffer
	 */
	protected SourceAddressRotation[] addressBufs;

	/** a map from source FilterSliceNode to address buf */
	protected HashMap<WorkNode, SourceAddressRotation> addrBufMap;
	/** true if what feeds this inputbuffer is a file reader */
	protected boolean upstreamFileReader;
	/**
	 * the name of the pointer to the current rotation of this buffer that the
	 * file reader should read into
	 */
	protected String currentFileReaderRotName;

	/**
	 * the name of the pointer to the read buffer of the current rotation that
	 * the file reader should read into
	 */
	protected String currentFileReaderBufName;

	/**
	 * InputRotatingBuffers for fizzed filters will use shared constituent
	 * buffers. This HashMap will store the names of the shared constituent
	 * buffers
	 */
	protected static HashMap<WorkNode, String> sharedBufferNames;

	/** stores InputRotatingBuffers for file writers */
	protected static HashSet<InputRotatingBuffer> fileWriterBuffers;

	static {
		sharedBufferNames = new HashMap<WorkNode, String>();
		fileWriterBuffers = new HashSet<InputRotatingBuffer>();
	}

	public static void createInputBuffer(Filter slice,
			BasicSpaceTimeSchedule schedule) {

		if (!slice.getInputNode().noInputs()) {
			assert slice.getInputNode().totalWeights(SchedulingPhase.STEADY) > 0;
			Core parent = SMPBackend.scheduler.getComputeNode(slice
					.getWorkNode());

			// create the new buffer, the constructor will put the buffer in the
			// hashmap
			InputRotatingBuffer buf = new InputRotatingBuffer(
					slice.getWorkNode(), parent);

			buf.setRotationLength(schedule);
			buf.setBufferSize();
			buf.createInitCode();
			buf.createAddressBufs();
		}
	}

	/**
	 * Create all the input buffers necessary for this slice graph. Iterate over
	 * the steady-state schedule, visiting each slice and creating an input
	 * buffer for the filter of the slice. Also set the rotation lengths based
	 * on the prime pump schedule.
	 * 
	 * @param schedule
	 *            The spacetime schedule of the slices
	 */
	public static void createInputBuffers(BasicSpaceTimeSchedule schedule) {

		StaticSubGraph ssg = schedule.getSSG();

		// for (Filter slice : schedule.getScheduleList()) {
		for (Filter slice : ssg.getFilterGraph()) {
			// System.out.println("InputRotatingBuffer.createInputBuffers calling on slice="
			// + slice.getWorkNode().toString());

			if (KjcOptions.sharedbufs && FissionGroupStore.isFizzed(slice)) {
				assert FissionGroupStore.isUnfizzedSlice(slice);

				FissionGroup group = FissionGroupStore.getFissionGroup(slice);
				for (Filter fizzedSlice : group.fizzedSlices)
					createInputBuffer(fizzedSlice, schedule);
			} else {
				createInputBuffer(slice, schedule);
			}
		}
	}

	JMethodDeclaration popManyCode = null;

	/**
	 * Create a new input buffer that is associated with the filter node.
	 * 
	 * @param filterNode
	 *            The filternode for which to create a new input buffer.
	 */
	private InputRotatingBuffer(WorkNode filterNode, Core parent) {
		super(filterNode.getEdgeToPrev(), filterNode, parent);

		bufType = filterNode.getWorkNodeContent().getInputType();
		types.add(bufType.toString());
		setInputBuffer(filterNode, this);

		readRotStructName = this.getIdent() + "read_rot_struct";
		currentReadRotName = this.getIdent() + "_read_current";
		currentReadBufName = this.getIdent() + "_read_buf";

		currentFileReaderRotName = this.getIdent() + "_fr_current";
		currentFileReaderBufName = this.getIdent() + "_fr_buf";

		if (KjcOptions.sharedbufs
				&& FissionGroupStore.isFizzed(filterNode.getParent())) {
			// System.out.println(filterNode + " is fizzed");
			if (!sharedBufferNames.containsKey(filterNode)) {
				// System.out.println("  first InputRotatingBuffer, setting base name of: "
				// + this.getIdent());
				Filter[] fizzedSlices = FissionGroupStore
						.getFizzedSlices(filterNode.getParent());

				for (Filter slice : fizzedSlices)
					sharedBufferNames.put(slice.getWorkNode(), this.getIdent());
			}
		}

		// if we have a file reader source for this filter, right now
		// we only support a single input for a filter that is feed by a file
		upstreamFileReader = filterNode.getParent().getInputNode()
				.hasFileInput();
		if (upstreamFileReader) {
			assert filterNode.getParent().getInputNode()
					.getWidth(SchedulingPhase.INIT) <= 1
					&& filterNode.getParent().getInputNode()
							.getWidth(SchedulingPhase.STEADY) <= 1;
		}

		addrBufMap = new HashMap<WorkNode, SourceAddressRotation>();
	}

	/**
	 * Adds code to wait for a token before proceeding. This provides
	 * synchronization when there is no software pipelining in an SSG
	 * @param filter The filter that must wait before executing.
	 * @param phase The phase of the schedule that is executing
	 */
	private List<JStatement> addTokenWait(WorkNode filter, SchedulingPhase phase,  List<JStatement> list) {
		Core filterCore = SMPBackend.scheduler.getComputeNode(filter);
		InterFilterEdge[] srcEdges = filter.getParent().getInputNode()
				.getSources(phase);
		Set<InterFilterEdge> edgeSet = new HashSet<InterFilterEdge>();
		// remove duplicate edges if they exist
		for (InterFilterEdge e : srcEdges) {
			edgeSet.add(e);
		}
		for (InterFilterEdge e : edgeSet) {
			WorkNode src = e.getSrc().getParent().getWorkNode();
			// Need to special case for FileReaders, which won't be
			// parallelized.
			if (src.isFileInput()) {
				continue;
			}
			Core srcCore = SMPBackend.scheduler.getComputeNode(src);
			if (!srcCore.equals(filterCore)) {
				String tokenName = src + "_to_" + filter + "_token";
				list.add(Util.toStmt("while (" + tokenName
						+ " == 0)"));
				list.add(Util.toStmt(tokenName + " = 0"));
			}
		}
		return list;
	}
	
	/**
	 * Allocate the constituent buffers of this rotating buffer structure
	 */
	@Override
	protected void allocBuffers() {
		// System.out.println("Inside InputRotatingBuffer.allocBuffers()");

		if (KjcOptions.sharedbufs
				&& FissionGroupStore.isFizzed(filterNode.getParent())) {
			// System.out.println("  " + filterNode + " is fizzed");
			if (sharedBufferNames.get(filterNode).equals(this.getIdent())) {
				// System.out.println("  " + filterNode + " allocated by me: " +
				// this.getIdent());
				super.allocBuffers();
			}
		} else {
			// System.out.println("  " + filterNode + " is NOT fizzed");
			super.allocBuffers();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#assignFromPeekMethod()
	 */
	@Override
	public JMethodDeclaration assignFromPeekMethod() {
		String valName = "__val";
		JFormalParameter val = new JFormalParameter(CStdType.Integer, valName);
		String offsetName = "__offset";
		JFormalParameter offset = new JFormalParameter(CStdType.Integer,
				offsetName);
		JBlock body = new JBlock();
		JMethodDeclaration retval = new JMethodDeclaration(null,
		/*
		 * at.dms.kjc.Constants.ACC_PUBLIC | at.dms.kjc.Constants.ACC_STATIC |
		 */Constants.ACC_INLINE, CStdType.Void, assignFromPeekMethodName(),
				new JFormalParameter[] { val, offset }, CClassType.EMPTY, body,
				null, null);
		body.addStatement(new JExpressionStatement(new JEmittedTextExpression(
				"/* assignFromPeekMethod not yet implemented */")));
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#assignFromPeekMethodName()
	 */
	@Override
	public String assignFromPeekMethodName() {
		return "__peekv_" + unique_id;
		// return "__peekv" + this.getIdent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#assignFromPopMethod()
	 */
	@Override
	public JMethodDeclaration assignFromPopMethod() {
		String parameterName = "__val";
		JFormalParameter val = new JFormalParameter(CStdType.Integer,
				parameterName);
		JBlock body = new JBlock();
		JMethodDeclaration retval = new JMethodDeclaration(null,
		/*
		 * at.dms.kjc.Constants.ACC_PUBLIC | at.dms.kjc.Constants.ACC_STATIC |
		 */Constants.ACC_INLINE, CStdType.Void, assignFromPopMethodName(),
				new JFormalParameter[] { val }, CClassType.EMPTY, body, null,
				null);
		body.addStatement(new JExpressionStatement(new JEmittedTextExpression(
				"/* assignFromPopMethod not yet implemented */")));
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#assignFromPopMethodName()
	 */
	@Override
	public String assignFromPopMethodName() {
		return "__popv_" + unique_id;
		// return "__popv" + this.getIdent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#beginInitRead()
	 */
	@Override
	public List<JStatement> beginInitRead() {
		LinkedList<JStatement> list = new LinkedList<JStatement>();
		list.add(transferCommands.zeroOutTail(SchedulingPhase.INIT));
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#beginInitWrite()
	 */
	@Override
	public List<JStatement> beginInitWrite() {
		assert (false);
		return null;
	}

	@Override
	public List<JStatement> beginPrimePumpRead() {
		List<JStatement> list = new LinkedList<JStatement>();
		list.add(transferCommands.zeroOutTail(SchedulingPhase.PRIMEPUMP));
		list = addTokenWait(filterNode, SchedulingPhase.PRIMEPUMP, list);
		return list;
	}

	@Override
	public List<JStatement> beginPrimePumpWrite() {
		assert (false);
		return null;
	}

	@Override
	public List<JStatement> beginSteadyRead() {
		List<JStatement> list = new LinkedList<JStatement>();
		//list.add(new SIRBeginMarker("beginSteadyRead"));
		list.add(transferCommands.zeroOutTail(SchedulingPhase.STEADY));
		list = addTokenWait(filterNode, SchedulingPhase.STEADY, list);
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#beginSteadyWrite()
	 */
	@Override
	public List<JStatement> beginSteadyWrite() {
		assert (false);
		return null;
	}

	/**
	 * Must be called after setLocalSrcFilter. This creates the address buffers
	 * that other cores use when writing to this input buffer. Each source that
	 * is mapped to a different core than this input buffer has an address
	 * buffer for this input buffer.
	 */
	protected void createAddressBufs() {
		List<SourceAddressRotation> addressBufsList = new LinkedList<SourceAddressRotation>();

		for (Filter src : filterNode.getParent().getInputNode()
				.getSourceFilters(SchedulingPhase.STEADY)) {
			if (KjcOptions.sharedbufs && FissionGroupStore.isFizzed(src)) {
				FissionGroup group = FissionGroupStore.getFissionGroup(src);
				for (Filter fizzedSlice : group.fizzedSlices) {
					Core core = SMPBackend.scheduler.getComputeNode(fizzedSlice
							.getWorkNode());
					SourceAddressRotation rot = new SourceAddressRotation(core,
							this, filterNode, edge);
					addressBufsList.add(rot);
					addrBufMap.put(fizzedSlice.getWorkNode(), rot);
				}
			} else {
				Core core = SMPBackend.scheduler.getComputeNode(src
						.getWorkNode());
				SourceAddressRotation rot = new SourceAddressRotation(core,
						this, filterNode, edge);
				addressBufsList.add(rot);
				addrBufMap.put(src.getWorkNode(), rot);
			}
		}

		addressBufs = addressBufsList.toArray(new SourceAddressRotation[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#dataDecls()
	 */
	@Override
	public List<JStatement> dataDecls() {
		// declare the buffer array
		List<JStatement> retval = new LinkedList<JStatement>();
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#dataDeclsH()
	 */
	@Override
	public List<JStatement> dataDeclsH() {
		return new LinkedList<JStatement>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#endInitRead()
	 */
	@Override
	public List<JStatement> endInitRead() {
		LinkedList<JStatement> list = new LinkedList<JStatement>();
		list.addAll(transferCommands.readTransferCommands(SchedulingPhase.INIT));
		return list;
		// copyDownStatements(SchedulingPhase.INIT));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#endInitWrite()
	 */
	@Override
	public List<JStatement> endInitWrite() {
		assert (false);
		return null;
	}

	@Override
	public List<JStatement> endPrimePumpRead() {
		return endSteadyRead();
	}

	@Override
	public List<JStatement> endPrimePumpWrite() {
		assert (false);
		return null;
	}

	@Override
	public List<JStatement> endSteadyRead() {
		LinkedList<JStatement> list = new LinkedList<JStatement>();
		// copy the copyDown items to the next rotation buffer
		list.addAll(transferCommands
				.readTransferCommands(SchedulingPhase.STEADY));
		// rotate to the next buffer
		list.addAll(rotateStatementsRead());
		// add synchronization between non-pipelined ssgs
		// addIntraSSGSynch(filterNode, SchedulingPhase.STEADY);

		return list;
		// copyDownStatements(SchedulingPhase.STEADY));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#endSteadyWrite()
	 */
	@Override
	public List<JStatement> endSteadyWrite() {
		assert (false);
		return null;
	}

	/**
	 * Return the set of address buffers that are declared on cores that feed
	 * this buffer.
	 * 
	 * @return the set of address buffers that are declared on cores that feed
	 *         this buffer.
	 */
	public SourceAddressRotation[] getAddressBuffers() {
		return addressBufs;
	}

	/**
	 * Return the address buffer rotation for this input buffer, to be used by a
	 * source FilterSliceNode
	 * 
	 * @param filterSliceNode
	 *            The FilterSliceNode
	 * @return the address buffer for this input buffer on the core
	 */
	public SourceAddressRotation getAddressRotation(WorkNode filterSliceNode) {
		return addrBufMap.get(filterSliceNode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#peekMethod()
	 */
	@Override
	public JMethodDeclaration peekMethod() {
		return transferCommands.peekMethod();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#peekMethodName()
	 */
	@Override
	public String peekMethodName() {
		return "__peek_" + unique_id;
		// return "__peek" + this.getIdent();
	}

	/**
	 * Pop many items at once ignoring them. Default method generated here to
	 * call popMethod() repeatedly.
	 */
	@Override
	public JMethodDeclaration popManyMethod() {
		if (popManyCode != null) {
			return popManyCode;
		}
		if (popMethod() == null) {
			return null;
		}

		String formalParamName = "n";
		CType formalParamType = CStdType.Integer;

		JVariableDefinition nPopsDef = new JVariableDefinition(formalParamType,
				formalParamName);
		JExpression nPops = new JLocalVariableExpression(nPopsDef);

		JVariableDefinition loopIndex = new JVariableDefinition(
				formalParamType, "i");

		JStatement popOne = new JExpressionStatement(new JMethodCallExpression(
				popMethodName(), new JExpression[0]));

		JBlock body = new JBlock();
		body.addStatement(Utils.makeForLoop(popOne, nPops, loopIndex));

		popManyCode = new JMethodDeclaration(CStdType.Void,
				popManyMethodName(),
				new JFormalParameter[] { new JFormalParameter(formalParamType,
						formalParamName) }, body);
		return popManyCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#popManyMethodName()
	 */
	@Override
	public String popManyMethodName() {
		return "__popN_" + unique_id;
		// return "__popN" + this.getIdent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#popMethod()
	 */
	@Override
	public JMethodDeclaration popMethod() {
		return transferCommands.popMethod();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#popMethodName()
	 */
	@Override
	public String popMethodName() {
		return "__pop_" + unique_id;
		// return "__pop" + this.getIdent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#beginInitRead()
	 */
	@Override
	public List<JStatement> postPreworkInitRead() {
		return new LinkedList<JStatement>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#pushMethod()
	 */
	@Override
	public JMethodDeclaration pushMethod() {
		assert (false);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#pushMethodName()
	 */
	@Override
	public String pushMethodName() {
		assert (false);
		return null;
	}

	/** Create an array reference given an offset */
	@Override
	public JArrayAccessExpression readBufRef(JExpression offset) {
		JFieldAccessExpression bufAccess = new JFieldAccessExpression(
				new JThisExpression(), currentReadBufName);
		return new JArrayAccessExpression(bufAccess, offset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#readDecls()
	 */
	@Override
	public List<JStatement> readDecls() {
		List<JStatement> retval = new LinkedList<JStatement>();
		retval.addAll(transferCommands.readDecls());
		return retval;
		/*
		 * //declare the tail JStatement tailDecl = new
		 * JVariableDeclarationStatement(tailDefn); List<JStatement> retval =
		 * new LinkedList<JStatement>(); retval.add(tailDecl); return retval;
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#readDeclsExtern()
	 */
	@Override
	public List<JStatement> readDeclsExtern() {
		return new LinkedList<JStatement>();
	}

	protected List<JStatement> rotateStatementsRead() {
		LinkedList<JStatement> list = new LinkedList<JStatement>();
		list.add(Util.toStmt(currentReadRotName + " = " + currentReadRotName
				+ "->next"));
		list.add(Util.toStmt(currentReadBufName + " = " + currentReadRotName
				+ "->buffer"));
		return list;
	}

	protected List<JStatement> rotateStatementsWrite() {
		assert (false);
		return null;
	}

	/**
	 * Set the names of the buffers that comprise this rotating buffer.
	 */
	@Override
	protected void setBufferNames() {
		// System.out.println("Inside InputRotatingBuffer.setBufferNames()");

		String baseName;
		if (KjcOptions.sharedbufs
				&& FissionGroupStore.isFizzed(filterNode.getParent())) {
			// System.out.println("  " + filterNode + " is fizzed");
			baseName = sharedBufferNames.get(filterNode);
			assert baseName != null;
		} else {
			// System.out.println("  " + filterNode + " is NOT fizzed");
			baseName = this.getIdent();
		}

		bufferNames = new String[rotationLength];
		for (int i = 0; i < rotationLength; i++) {
			bufferNames[i] = baseName + "_Buf_" + i;
		}
	}

	/**
	 * Set the buffer size of this input buffer based on the max number of items
	 * it receives.
	 */
	@Override
	protected void setBufferSize() {
		// System.out.println("Inside InputRotatingBuffer.setBufferSize()");

		WorkNodeInfo fi;
		if (KjcOptions.sharedbufs
				&& FissionGroupStore.isFizzed(filterNode.getParent())) {
			// System.out.println("  " + filterNode + " is fizzed");
			fi = FissionGroupStore.getFissionGroup(filterNode.getParent()).unfizzedFilterInfo;
		} else {
			// System.out.println("  " + filterNode + " is NOT fizzed");
			fi = filterInfo;
		}

		bufSize = Math.max(fi.totalItemsReceived(SchedulingPhase.INIT),
				(fi.totalItemsReceived(SchedulingPhase.STEADY) + fi.copyDown));
	}

	/**
	 * Set the rotation length of this rotating buffer
	 */
	protected void setRotationLength(BasicSpaceTimeSchedule schedule) {
		// calculate the rotation length
		int destMult;
		if (KjcOptions.sharedbufs
				&& FissionGroupStore.isFizzed(filterNode.getParent())) {
			destMult = schedule.getPrimePumpMult(FissionGroupStore
					.getUnfizzedSlice(filterNode.getParent()));
		} else {
			destMult = schedule.getPrimePumpMult(filterNode.getParent());
		}
		// first find the max rotation length given the prime pump
		// mults of all the sources
		int maxRotationLength = 0;
		for (Filter src : filterNode.getParent().getInputNode()
				.getSourceFilters(SchedulingPhase.STEADY)) {
			int diff = schedule.getPrimePumpMult(src) - destMult;
			assert diff >= 0;
			if (diff > maxRotationLength) {
				maxRotationLength = diff;
			}
		}
		rotationLength = maxRotationLength + 1;
	}

	/**
	 * Generate the code to setup the structure of the rotating buffer as a
	 * circular linked list.
	 */
	@Override
	protected void setupRotation() {
		String temp = "__temp__";
		SMPComputeCodeStore cs;

		// this is the typedef we will use for this buffer rotation structure
		String rotType = rotTypeDefPrefix + getType().toString();

		// if we are setting up the rotation for a file writer we have to do it
		// on the allocating core
		if (filterNode.isFileOutput()) {
			fileWriterBuffers.add(this);
			cs = ProcessFileWriter.getAllocatingCore(filterNode)
					.getComputeCode();
		} else {
			cs = parent.getComputeCode();
		}

		JBlock block = new JBlock();

		// add the declaration of the rotation buffer of the appropriate
		// rotation type
		cs.appendTxtToGlobal(rotType + " *" + readRotStructName + ";\n");
		// add the declaration of the pointer that points to the current
		// rotation in the rotation structure
		cs.appendTxtToGlobal(rotType + " *" + currentReadRotName + ";\n");
		// add the declaration of the pointer that points to the current buffer
		// in the current rotation
		cs.appendTxtToGlobal(bufType.toString() + " *" + currentReadBufName
				+ ";\n");

		if (upstreamFileReader) {
			// add the declaration of the pointer that points to current in the
			// rotation structure that the file
			// reader should write into
			parent.getComputeCode().appendTxtToGlobal(
					rotType + " *" + currentFileReaderRotName + ";\n");
			// add the declaration of the pointer that points to the current
			// buffer in the current rotation that
			// the file reader should write into
			parent.getComputeCode().appendTxtToGlobal(
					bufType.toString() + " *" + currentFileReaderBufName
							+ ";\n");
		}

		// create a temp var
		if (this.rotationLength > 1)
			block.addStatement(Util.toStmt(rotType + " *" + temp));

		// create the first entry!!
		block.addStatement(Util.toStmt(readRotStructName + " =  (" + rotType
				+ "*)" + "malloc(sizeof(" + rotType + "))"));

		// modify the first entry
		block.addStatement(Util.toStmt(readRotStructName + "->buffer = "
				+ bufferNames[0]));
		if (this.rotationLength == 1)
			block.addStatement(Util.toStmt(readRotStructName + "->next = "
					+ readRotStructName));
		else {
			block.addStatement(Util.toStmt(temp + " = (" + rotType + "*)"
					+ "malloc(sizeof(" + rotType + "))"));

			block.addStatement(Util.toStmt(readRotStructName + "->next = "
					+ temp));

			block.addStatement(Util.toStmt(temp + "->buffer = "
					+ bufferNames[1]));

			for (int i = 2; i < this.rotationLength; i++) {
				block.addStatement(Util.toStmt(temp + "->next =  (" + rotType
						+ "*)" + "malloc(sizeof(" + rotType + "))"));
				block.addStatement(Util.toStmt(temp + " = " + temp + "->next"));
				block.addStatement(Util.toStmt(temp + "->buffer = "
						+ bufferNames[i]));
			}

			block.addStatement(Util.toStmt(temp + "->next = "
					+ readRotStructName));
		}
		block.addStatement(Util.toStmt(currentReadRotName + " = "
				+ readRotStructName));
		block.addStatement(Util.toStmt(currentReadBufName + " = "
				+ currentReadRotName + "->buffer"));

		if (upstreamFileReader) {
			block.addStatement(Util.toStmt(currentFileReaderRotName + " = "
					+ readRotStructName));
			block.addStatement(Util.toStmt(currentFileReaderBufName + " = "
					+ currentReadRotName + "->buffer"));
		}

		cs.addStatementToBufferInit(block);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#topOfWorkSteadyRead()
	 */
	@Override
	public List<JStatement> topOfWorkSteadyRead() {
		return new LinkedList<JStatement>();
	}

	/** Create an array reference given an offset */
	@Override
	public JFieldAccessExpression writeBufRef() {
		assert (false);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.ChannelI#writeDecls()
	 */
	@Override
	public List<JStatement> writeDecls() {
		assert (false);
		return null;
	}
}
