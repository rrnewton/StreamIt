package at.dms.kjc.smp;

import java.util.Map;
import java.util.WeakHashMap;

import at.dms.kjc.CType;
import at.dms.kjc.JEmittedTextExpression;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JMethodCallExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JStatement;
import at.dms.kjc.JThisExpression;
import at.dms.kjc.SLIRReplacingVisitor;
import at.dms.kjc.backendSupport.Channel;
import at.dms.kjc.backendSupport.CodeStoreHelper;
import at.dms.kjc.backendSupport.InterSSGChannel;
import at.dms.kjc.sir.SIRBeginMarker;
import at.dms.kjc.sir.SIREndMarker;
import at.dms.kjc.sir.SIRPeekExpression;
import at.dms.kjc.sir.SIRPopExpression;
import at.dms.kjc.sir.SIRPushExpression;
import at.dms.kjc.slir.FileOutputContent;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InternalFilterNode;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.slir.WorkNodeInfo;

/**
 * Process a FilterSliceNode creating code in the code store.
 * 
 * 
 * @author dimock and mgorodon
 * 
 */
public class ProcessFilterWorkNode {

	/** print debugging info? */
	public static boolean debug = false;

	private static int uid = 0;

	/** set of filters for which we have written basic code. */
	// uses WeakHashMap to be self-cleaning, but now have to insert some value.
	protected static Map<InternalFilterNode, Boolean> basicCodeWritten = new WeakHashMap<InternalFilterNode, Boolean>();

	/**
	 * Get code for a filter. If code not yet made, then makes it.
	 * 
	 * @param filter
	 *            A FilterSliceNode for which we want code.
	 * @param inputChannel
	 *            The input channel -- specified routines to call to replace
	 *            peek, pop.
	 * @param outputChannel
	 *            The output channel -- specified routeines to call to replace
	 *            push.
	 * @param backEndBits
	 * @return
	 */
	public static CodeStoreHelper getFilterCode(WorkNode filter,
			Channel inputChannel, Channel outputChannel,
			SMPBackEndFactory backEndBits, boolean isDynamicPop,
			boolean isDynamicPush) {

		System.out.println("ProcessFilterWorkNode.getFilterCode");

		CodeStoreHelper filterCode = CodeStoreHelper
				.findHelperForSliceNode(filter);
		if (filterCode == null) {
			filterCode = makeFilterCode(filter, inputChannel, outputChannel,
					backEndBits, isDynamicPop, isDynamicPush);
			CodeStoreHelper.addHelperForSliceNode(filter, filterCode);
		}
		return filterCode;
	}

	public static int getUid() {
		return uid++;
	}

	private static PushPopReplacingVisitor pushPopReplacingVisitor = new PushPopReplacingVisitor();

	/**
	 * Take a code unit (here a FilterContent) and return one with all push,
	 * peek, pop replaced with calls to channel routines. Clones the input
	 * methods and munges on the clones, further changes to the returned code
	 * will not affect the methods of the input code unit.
	 * 
	 * @param code
	 *            The code (fields and methods)
	 * @param inputChannel
	 *            The input channel -- specifies routines to call to replace
	 *            peek, pop.
	 * @param outputChannel
	 *            The output channel -- specifies routines to call to replace
	 *            push.
	 * @return a CodeStoreHelper with no push, peek, or pop instructions in the
	 *         methods.
	 */
	private static CodeStoreHelper makeFilterCode(WorkNode filter,
			Channel inputChannel, Channel outputChannel,
			SMPBackEndFactory backEndFactory, final boolean isDynamicPop,
			final boolean isDynamicPush) {

		final String peekName;
		final String popName;
		final String pushName;
		final String popManyName;

		System.out.println("ProcessFilterWorkNode.makeFilterCode");

		if (inputChannel != null) {
			peekName = inputChannel.peekMethodName();
			popName = inputChannel.popMethodName();
			popManyName = inputChannel.popManyMethodName();
		} else {
			peekName = "/* peek from non-existent channel */";
			popName = "/* pop() from non-existent channel */";
			popManyName = "/* pop(N) from non-existent channel */";
		}

		System.out.println("ProcessFilterWorkNode.makeFilterCode filter="
				+ filter + " popName=" + popName);

		if (outputChannel != null) {
			pushName = outputChannel.pushMethodName();
		} else {
			pushName = "/* push() to non-existent channel */";
		}

		System.out.println("ProcessFilterWorkNode.makeFilterCode filter="
				+ filter + " popName=" + popName + " pushName=" + pushName);

		CodeStoreHelper helper = backEndFactory.getCodeStoreHelper(filter);
		JMethodDeclaration[] methods = helper.getMethods();

		Map<Filter, Integer> filterToThreadId = backEndFactory
				.getFilterToThreadId();
		Map<String, String> dominators = backEndFactory.getDominators();

		System.out.println("ProcessFilterWorkNode.makeFilterCode filter="
				+ filter + " ID=" + filterToThreadId.get(filter.getParent()));

		System.out.println("** ProcessFilterWorkNode.makeFilterCode filter="
				+ filter + " dominates=" + dominators.get(filter.toString()));

		// relies on fact that a JMethodDeclaration is not replaced so
		// work, init, preWork are still identifiable after replacement.

		pushPopReplacingVisitor.init(filter, peekName, popManyName, popName,
				pushName, filterToThreadId, dominators, isDynamicPop,
				isDynamicPush);

		for (JMethodDeclaration method : methods) {

			//

			method.accept(pushPopReplacingVisitor);

			// Add markers to code for debugging of emitted code:
			String methodName = "filter " + filter.getFilter().getName() + "."
					+ method.getName();

			method.addStatementFirst(new SIRBeginMarker(methodName));
			method.addStatement(new SIREndMarker(methodName));
		}

		return helper;
	}

	protected CodeStoreHelper filterCode;
	protected SMPComputeCodeStore codeStore;
	protected WorkNode filterNode;
	protected SchedulingPhase whichPhase;

	protected SMPBackEndFactory backEndFactory;

	protected Core location;

	public ProcessFilterWorkNode() {
		/* do nothing */
	}

	protected void additionalInitProcessing() {

	}

	protected void additionalPreInitProcessing() {

	}

	protected void additionalPrimePumpProcessing() {

	}

	protected void additionalSteadyProcessing() {

	}

	/**
	 * Create code for a FilterSliceNode. May request creation of channels, and
	 * cause Process{Input/Filter/Output}SliceNode to be called for other slice
	 * nodes.
	 * 
	 * @param filterNode
	 *            the filterNode that needs code generated.
	 * @param whichPhase
	 *            a scheduling phase {@link SchedulingPhase}
	 * @param backEndFactory
	 *            a BackEndFactory to access layout, etc.
	 */
	public void doit(WorkNode filterNode, SchedulingPhase whichPhase,
			SMPBackEndFactory backEndFactory) {
		this.filterNode = filterNode;
		this.whichPhase = whichPhase;
		this.backEndFactory = backEndFactory;
		location = backEndFactory.getLayout().getComputeNode(filterNode);
		assert location != null;
		codeStore = location.getComputeCode();
		// remember that this tile has code that needs to execute
		codeStore.setHasCode();

		System.out.println("smp.ProcessFitlerWorkNode.doit(), filter="
				+ filterNode);

		filterCode = CodeStoreHelper.findHelperForSliceNode(filterNode);
		// We should only generate code once for a filter node.

		StaticSubGraph ssg = backEndFactory.getScheduler().getGraphSchedule()
				.getSSG();
		boolean hasDynamicInput = false;
		boolean hasDynamicOutput = false;

		Filter[] graph = ssg.getFilterGraph();
		int last = graph.length - 1;

		System.out
				.println("smp.ProcessFitlerWorkNode.doit(), graph[0].getWorkNode()="
						+ graph[0].getWorkNode()
						+ "graph[last].getWorkNode()="
						+ graph[last].getWorkNode());

		// A particular filter will only have dynamic input if it is
		// the top node of an SSG, and if the SSG has dynamic input.
		if (filterNode.equals(graph[0].getWorkNode())) {
			hasDynamicInput = ssg.hasDynamicInput();
		}

		if (filterNode.equals(graph[last].getWorkNode())) {
			hasDynamicOutput = ssg.hasDynamicOutput();
		}

		System.out.println("smp.ProcessFitlerWorkNode.doit(), filter="
				+ filterNode + "hasDynamicInput=" + hasDynamicInput
				+ " hasDynamicOutput=" + hasDynamicOutput);

		if (filterCode == null) {

			Channel inputBuffer = null;
			Channel outputBuffer = null;

			// boolean isDynamic = ssg.hasDynamicInput() ||
			// ssg.hasDynamicOutput();

			if (hasDynamicInput) {
				inputBuffer = InterSSGChannel.getInputBuffer(filterNode);
			} else if (backEndFactory.sliceHasUpstreamChannel(filterNode
					.getParent())) {
				inputBuffer = InputRotatingBuffer.getInputBuffer(filterNode);
			}

			if (hasDynamicOutput) {
				outputBuffer = InterSSGChannel.getOutputBuffer(filterNode, ssg);
			} else if (backEndFactory.sliceHasDownstreamChannel(filterNode
					.getParent())) {
				outputBuffer = OutputRotatingBuffer.getOutputBuffer(filterNode);
			}

			System.out.println("ProcessFitlerWorkNode.doit filter="
					+ filterNode + " filterCode=null");
			System.out.println("ProcessFitlerWorkNode.doit filter="
					+ filterNode + " inputBuffer is null == "
					+ (inputBuffer == null));

			filterCode = getFilterCode(filterNode, inputBuffer, outputBuffer,
					backEndFactory, hasDynamicInput, hasDynamicOutput);
		}

		// TODO: Check here if I should be printing the steady state
		// or if I should start a thread.

		switch (whichPhase) {
		case PREINIT:
			standardPreInitProcessing();
			additionalPreInitProcessing();
			break;
		case INIT:
			standardInitProcessing();
			additionalInitProcessing();
			break;
		case PRIMEPUMP:
			standardPrimePumpProcessing(hasDynamicInput);
			additionalPrimePumpProcessing();
			break;
		case STEADY:
			standardSteadyProcessing(hasDynamicInput);
			additionalSteadyProcessing();
			break;
		}
	}

	protected void standardInitProcessing() {
		// Have the main function for the CodeStore call out init.
		codeStore.addInitFunctionCall(filterCode.getInitMethod());
		JMethodDeclaration workAtInit = filterCode.getInitStageMethod();
		if (workAtInit != null) {
			// if there are calls to work needed at init time then add
			// method to general pool of methods
			codeStore.addMethod(workAtInit);
			// and add call to list of calls made at init time.
			// Note: these calls must execute in the order of the
			// initialization schedule -- so caller of this routine
			// must follow order of init schedule.
			codeStore.addInitStatement(new JExpressionStatement(null,
					new JMethodCallExpression(null, new JThisExpression(null),
							workAtInit.getName(), new JExpression[0]), null));
		}
	}

	protected void standardPreInitProcessing() {

	}

	protected void standardPrimePumpProcessing(boolean hasDynamicInput) {
		// TODO: We need to change this so we have the correct prime pump
		// processing.
		System.out
				.println("ProcessFilterWorkNode.standardPrimePumpProcessing, hasDynamicInput="
						+ hasDynamicInput);
		if (hasDynamicInput) {
			System.out
					.println("WARNING: need to change ProcessFilterWorkNode.standardPrimePumpProcessing to have the correct schedule");
			return;
		}
		JMethodDeclaration primePump = filterCode.getPrimePumpMethod();
		if (primePump != null && !codeStore.hasMethod(primePump)) {
			// Add method -- but only once
			codeStore.addMethod(primePump);
		}
		if (primePump != null) {
			// for each time this method is called, it adds another call
			// to the primePump routine to the initialization.
			codeStore.addInitStatement(new JExpressionStatement(null,
					new JMethodCallExpression(null, new JThisExpression(null),
							primePump.getName(), new JExpression[0]), null));

		}
	}

	protected void standardSteadyProcessing(boolean isDynamicPop) {
		JStatement steadyBlock = filterCode.getSteadyBlock();

				
		if (!basicCodeWritten.containsKey(filterNode)) {
			codeStore.addFields(filterCode.getFields());
			codeStore.addMethods(filterCode.getUsefulMethods());
			if (filterNode.getFilter() instanceof FileOutputContent) {
				codeStore.addCleanupStatement(((FileOutputContent) filterNode
						.getFilter()).closeFile());
			}
			basicCodeWritten.put(filterNode, true);
		}

		System.out
				.println("ProcessFilterWorkNode.standardSteadyProcessing Filter"
						+ filterNode.getFilter().getName()
						+ " isDynamicPop="
						+ isDynamicPop);
		if (isDynamicPop) {
			Map<Filter, Integer> filterToThreadId = backEndFactory
					.getFilterToThreadId();		
			String threadId = filterToThreadId.get(filterNode.getParent())
					.toString();
			int threadIndex = Integer.parseInt(threadId);
			codeStore.addThreadHelper(threadIndex, steadyBlock);
			codeStore.addSteadyThreadCall(threadIndex);

		} else {
			codeStore.addSteadyLoopStatement(steadyBlock);
		}
		if (debug) {
			// debug info only: expected splitter and joiner firings.
			System.err.print("(Filter" + filterNode.getFilter().getName());
			System.err.print(" "
					+ WorkNodeInfo.getFilterInfo(filterNode).getMult(
							SchedulingPhase.INIT));
			System.err.print(" "
					+ WorkNodeInfo.getFilterInfo(filterNode).getMult(
							SchedulingPhase.STEADY));
			System.err.println(")");
			System.err.print("(Joiner joiner_"
					+ filterNode.getFilter().getName());
			System.err.print(" "
					+ WorkNodeInfo.getFilterInfo(filterNode)
							.totalItemsReceived(SchedulingPhase.INIT));
			System.err.print(" "
					+ WorkNodeInfo.getFilterInfo(filterNode)
							.totalItemsReceived(SchedulingPhase.STEADY));
			System.err.println(")");
			System.err.print("(Splitter splitter_"
					+ filterNode.getFilter().getName());
			System.err.print(" "
					+ WorkNodeInfo.getFilterInfo(filterNode).totalItemsSent(
							SchedulingPhase.INIT));
			System.err.print(" "
					+ WorkNodeInfo.getFilterInfo(filterNode).totalItemsSent(
							SchedulingPhase.STEADY));
			System.err.println(")");
		}

	}

	private static class PushPopReplacingVisitor extends SLIRReplacingVisitor {

		String peekName;
		String popManyName;
		String popName;
		String pushName;
		Map<Filter, Integer> filterToThreadId;
		boolean isDynamicPop;
		boolean isDynamicPush;
		WorkNode filter;
		Map<String, String> dominators;

		public void init(WorkNode filter, String peekName, String popManyName,
				String popName, String pushName,
				Map<Filter, Integer> filterToThreadId,
				Map<String, String> dominators, boolean isDynamicPop,
				boolean isDynamicPush) {
			this.filter = filter;
			this.peekName = peekName;
			this.popManyName = popManyName;
			this.popName = popName;
			this.pushName = pushName;
			this.filterToThreadId = filterToThreadId;
			this.dominators = dominators;
			this.isDynamicPop = isDynamicPop;
			this.isDynamicPush = isDynamicPush;
		}

		public Object visitPeekExpression(SIRPeekExpression self,
				CType tapeType, JExpression arg) {
			JExpression newArg = (JExpression) arg.accept(this);
			JExpression methodCall = new JMethodCallExpression(peekName,
					new JExpression[] { newArg });
			methodCall.setType(tapeType);
			return methodCall;
		}

		@Override
		public Object visitPopExpression(SIRPopExpression self, CType tapeType) {

			if (self.getNumPop() > 1) {
				return new JMethodCallExpression(popManyName,
						new JExpression[] { new JIntLiteral(self.getNumPop()) });
			} else {
				if (isDynamicPop) {
					String threadId = filterToThreadId.get(filter.getParent())
							.toString();
					String buffer = "dyn_buf_" + threadId;
					JExpression dyn_queue = new JEmittedTextExpression(buffer);
					JExpression index = new JEmittedTextExpression(threadId);
					JExpression dominated = new JEmittedTextExpression("&"
							+ dominators.get(filter.toString()) + "_multiplier");

					JExpression methodCall = new JMethodCallExpression(popName,
							new JExpression[] { dyn_queue, index, dominated });
					methodCall.setType(tapeType);
					return methodCall;
				} else {
					JExpression methodCall = new JMethodCallExpression(popName,
							new JExpression[0]);
					methodCall.setType(tapeType);
					return methodCall;
				}
			}
		}

		@Override
		public Object visitPushExpression(SIRPushExpression self,
				CType tapeType, JExpression arg) {
			JExpression newArg = (JExpression) arg.accept(this);
			if (isDynamicPush) {
				String threadId = filterToThreadId.get(filter.getParent())
						.toString();
				String buffer = "dyn_buf_" + threadId;
				JExpression dyn_queue = new JEmittedTextExpression(buffer);
				return new JMethodCallExpression(pushName, new JExpression[] {
						dyn_queue, newArg });
			} else {
				return new JMethodCallExpression(pushName,
						new JExpression[] { newArg });

			}
		}

	}

}
