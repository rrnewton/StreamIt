package at.dms.kjc.backendSupport;

import java.util.Map;
import java.util.WeakHashMap;

import at.dms.kjc.CType;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JMethodCallExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JStatement;
import at.dms.kjc.JThisExpression;
import at.dms.kjc.SLIRReplacingVisitor;
import at.dms.kjc.sir.SIRBeginMarker;
import at.dms.kjc.sir.SIREndMarker;
import at.dms.kjc.sir.SIRPeekExpression;
import at.dms.kjc.sir.SIRPopExpression;
import at.dms.kjc.sir.SIRPushExpression;
import at.dms.kjc.slir.FileOutputContent;
import at.dms.kjc.slir.InternalFilterNode;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.slir.WorkNodeInfo;

/**
 * Process a FilterSliceNode creating code in the code store and buffers for
 * connectivity. Provides some standard processing, and has hooks for additional
 * processing.
 * 
 * @author dimock
 * 
 */
public class ProcessFilterWorkNode {
	/**
	 * print debugging info?
	 */
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
			Channel inputChannel, IntraSSGChannel outputChannel,
			BackEndFactory backEndBits) {
		CodeStoreHelper filter_code = CodeStoreHelper
				.findHelperForSliceNode(filter);
		if (filter_code == null) {
			filter_code = makeFilterCode(filter, inputChannel, outputChannel,
					backEndBits);
			CodeStoreHelper.addHelperForSliceNode(filter, filter_code);
		}
		return filter_code;
	}

	public static int getUid() {
		return uid++;
	}

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
			Channel inputChannel, IntraSSGChannel outputChannel,
			BackEndFactory backEndFactory) {

		System.out.println("ProcessFilterSliceNode.makeFilterCode()");

		final String peekName;

		final String popName;
		final String pushName;
		final String popManyName;

		if (inputChannel != null) {
			peekName = inputChannel.peekMethodName();
			popName = inputChannel.popMethodName();
			popManyName = inputChannel.popManyMethodName();
		} else {
			peekName = "/* peek from non-existent channel */";
			popName = "/* pop() from non-existent channel */";
			popManyName = "/* pop(N) from non-existent channel */";
		}

		if (outputChannel != null) {
			pushName = outputChannel.pushMethodName();
		} else {
			pushName = "/* push() to non-existent channel */";
		}

		CodeStoreHelper helper = backEndFactory.getCodeStoreHelper(filter);
		JMethodDeclaration[] methods = helper.getMethods();

		// relies on fact that a JMethodDeclaration is not replaced so
		// work, init, preWork are still identifiable after replacement.
		for (JMethodDeclaration method : methods) {
			method.accept(new SLIRReplacingVisitor() {
				@Override
				public Object visitPeekExpression(SIRPeekExpression self,
						CType tapeType, JExpression arg) {
					JExpression newArg = (JExpression) arg.accept(this);
					return new JMethodCallExpression(peekName,
							new JExpression[] { newArg });
				}

				@Override
				public Object visitPopExpression(SIRPopExpression self,
						CType tapeType) {
					if (self.getNumPop() > 1) {
						return new JMethodCallExpression(popManyName,
								new JExpression[] { new JIntLiteral(self
										.getNumPop()) });
					} else {
						return new JMethodCallExpression(popName,
								new JExpression[0]);
					}
				}

				@Override
				public Object visitPushExpression(SIRPushExpression self,
						CType tapeType, JExpression arg) {
					JExpression newArg = (JExpression) arg.accept(this);
					return new JMethodCallExpression(pushName,
							new JExpression[] { newArg });
				}
			});
			// Add markers to code for debugging of emitted code:
			String methodName = "filter " + filter.getFilter().getName() + "."
					+ method.getName();
			method.addStatementFirst(new SIRBeginMarker(methodName));
			method.addStatement(new SIREndMarker(methodName));
		}

		return helper;
	}

	protected CodeStoreHelper filter_code;
	protected ComputeCodeStore codeStore;
	protected WorkNode filterNode;
	protected SchedulingPhase whichPhase;

	protected BackEndFactory backEndFactory;

	protected ComputeNode location;

	/**
	 * @param filterNode
	 *            the filterNode that needs code generated.
	 * @param whichPhase
	 *            a scheduling phase {@link SchedulingPhase}
	 * @param backEndFactory
	 *            a BackEndFactory to access layout, etc.
	 * */
	public ProcessFilterWorkNode(WorkNode filterNode,
			SchedulingPhase whichPhase, BackEndFactory backEndFactory) {
		this.filterNode = filterNode;
		this.whichPhase = whichPhase;
		this.backEndFactory = backEndFactory;
		setLocationAndCodeStore();
	}

	protected void additionalInitProcessing() {

	}

	protected void additionalPreInitProcessing() {

	}

	protected void additionalPrimePumpProcessing() {

	}

	protected void additionalSteadyProcessing() {
		// ppucodestore.addschedulingcode
	}

	protected void doit() {
		filter_code = CodeStoreHelper.findHelperForSliceNode(filterNode);
		// We should only generate code once for a filter node.

		if (filter_code == null) {
			if (debug) {
				System.err.println("filter "
						+ filterNode.getFilter()
						+ ", make_joiner "
						+ backEndFactory.sliceNeedsJoinerCode(filterNode
								.getParent())
								+ ", make_peek_buffer "
								+ backEndFactory.sliceNeedsPeekBuffer(filterNode
										.getParent())
										+ ", has_upstream_channel "
										+ backEndFactory.sliceHasUpstreamChannel(filterNode
												.getParent())
												+ ", make_splitter "
												+ backEndFactory.sliceNeedsSplitterCode(filterNode
														.getParent())
														+ ", has_downstream_channel "
														+ backEndFactory.sliceHasDownstreamChannel(filterNode
																.getParent()));
			}

			Channel inputChannel = null;

			if (backEndFactory.sliceHasUpstreamChannel(filterNode.getParent())) {
				inputChannel = backEndFactory.getChannel(filterNode
						.getPrevious().getEdgeToNext());
			}

			IntraSSGChannel outputChannel = null;

			if (backEndFactory
					.sliceHasDownstreamChannel(filterNode.getParent())) {
				outputChannel = backEndFactory.getChannel(filterNode
						.getEdgeToNext());
			}

			filter_code = getFilterCode(filterNode, inputChannel,
					outputChannel, backEndFactory);
		}

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
			standardPrimePumpProcessing();
			additionalPrimePumpProcessing();
			break;
		case STEADY:
			standardSteadyProcessing();
			additionalSteadyProcessing();
			break;
		}
	}

	/**
	 * Create code for a FilterSliceNode. May request creation of channels, and
	 * cause Process{Input/Filter/Output}SliceNode to be called for other slice
	 * nodes.
	 */
	public void processFilterSliceNode() {
		doit();
	}

	protected void setLocationAndCodeStore() {
		location = backEndFactory.getLayout().getComputeNode(filterNode);
		assert location != null;
		codeStore = location.getComputeCode();
	}

	protected void standardInitProcessing() {
		// Have the main function for the CodeStore call out init.
		codeStore.addInitFunctionCall(filter_code.getInitMethod());
		JMethodDeclaration workAtInit = filter_code.getInitStageMethod();
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

	protected void standardPrimePumpProcessing() {

		JMethodDeclaration primePump = filter_code.getPrimePumpMethod();
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

	protected void standardSteadyProcessing() {
		JStatement steadyBlock = filter_code.getSteadyBlock();
		// helper has now been used for the last time, so we can write the basic code.
		// write code deemed useful by the helper into the correct ComputeCodeStore.
		// write only once if multiple calls for steady state.
		if (!basicCodeWritten.containsKey(filterNode)) {
			codeStore.addFields(filter_code.getUsefulFields());
			codeStore.addMethods(filter_code.getUsefulMethods());
			if (filterNode.getFilter() instanceof FileOutputContent) {
				codeStore.addCleanupStatement(((FileOutputContent) filterNode
						.getFilter()).closeFile());
			}
			basicCodeWritten.put(filterNode, true);
		}

		codeStore.addSteadyLoopStatement(steadyBlock);

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
}
