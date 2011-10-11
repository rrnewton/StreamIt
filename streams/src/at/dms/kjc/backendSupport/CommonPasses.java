/**
 * 
 */
package at.dms.kjc.backendSupport;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.dms.kjc.JInterfaceDeclaration;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.ObjectDeepCloner;
import at.dms.kjc.StreamItDot;
import at.dms.kjc.common.CheckStatefulFilters;
import at.dms.kjc.common.ConvertLocalsToFields;
import at.dms.kjc.sir.SIRContainer;
import at.dms.kjc.sir.SIRGlobal;
import at.dms.kjc.sir.SIRHelper;
import at.dms.kjc.sir.SIRInterfaceTable;
import at.dms.kjc.sir.SIRPortal;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.SIRStructure;
import at.dms.kjc.sir.SemanticChecker;
import at.dms.kjc.sir.lowering.SegmentedSIRGraph;
import at.dms.kjc.sir.lowering.ArrayInitExpander;
import at.dms.kjc.sir.lowering.ConstantProp;
import at.dms.kjc.sir.lowering.ConstructSIRTree;
import at.dms.kjc.sir.lowering.DynamismFinder;
import at.dms.kjc.sir.lowering.EnqueueToInitPath;
import at.dms.kjc.sir.lowering.FieldProp;
import at.dms.kjc.sir.lowering.Flattener;
import at.dms.kjc.sir.lowering.IntroduceMultiPops;
import at.dms.kjc.sir.lowering.RenameAll;
import at.dms.kjc.sir.lowering.RoundToFloor;
import at.dms.kjc.sir.lowering.SimplifyArguments;
import at.dms.kjc.sir.lowering.SimplifyPopPeekPush;
import at.dms.kjc.sir.lowering.StaticsProp;
import at.dms.kjc.sir.lowering.VarDeclRaiser;
import at.dms.kjc.sir.lowering.VectorizeEnable;
import at.dms.kjc.sir.lowering.fission.FissionReplacer;
import at.dms.kjc.sir.lowering.fusion.FuseAll;
import at.dms.kjc.sir.lowering.fusion.FusePipelines;
import at.dms.kjc.sir.lowering.fusion.Lifter;
import at.dms.kjc.sir.lowering.partition.ManualPartition;
import at.dms.kjc.sir.lowering.partition.SJToPipe;
import at.dms.kjc.sir.lowering.partition.WorkEstimate;
import at.dms.kjc.slir.AddBuffering;
import at.dms.kjc.slir.DataFlowOrder;
import at.dms.kjc.slir.InstallInitDistributions;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.StreamGraph;
import at.dms.kjc.slir.SIRToSLIR;
import at.dms.kjc.slir.WorkNodeInfo;

/**
 * Common passes, useful in new back ends.
 * 
 * @author dimock
 * 
 */
public class CommonPasses {
	/** field that may be useful later */
	private StreamGraph streamGraph;

	/** stores pre-modified str for statistics gathering */
	private SIRStream origSTR;

	/**
	 * stores the association between names of global variables duplicated
	 * locally
	 */
	private Map<String, Set<String>> associatedGlobals;

	/** number of cores to process for. */
	private int numCores;

	/**
	 * if vectorizing early for some reason, remember so as not to try again
	 * later. Seems to be necessary because vectorization does not set up types
	 * correctly on some introduced expressions e.g. __tmp2__7.a[1]
	 */

	private boolean vectorizedEarly = false;

	/**
	 * Top level method for executing passes common to some current and all
	 * future StreamIt compilers.
	 * 
	 * @param str
	 *            SIRStream from {@link at.dms.kjc.Kopi2SIR}
	 * @param interfaces
	 *            JInterfaceDeclaration[] from {@link at.dms.kjc.Kopi2SIR}
	 * @param interfaceTables
	 *            SIRInterfaceTable[] from {@link at.dms.kjc.Kopi2SIR}
	 * @param structs
	 *            SIRStructure[] from {@link at.dms.kjc.Kopi2SIR}
	 * @param helpers
	 *            SIRHelper[] from {@link at.dms.kjc.Kopi2SIR}
	 * @param global
	 *            SIRGlobal from {@link at.dms.kjc.Kopi2SIR}
	 * @param numCores
	 *            Number of {@link at.dms.kjc.backendSupport.ComputeNode}'s to
	 *            use in partitioning.
	 * 
	 * @return a slice graph: the optimized program in
	 *         {@link at.dms.kjc.slir.Filter Slice} representation.
	 */
	public StreamGraph run(SIRStream str, JInterfaceDeclaration[] interfaces,
			SIRInterfaceTable[] interfaceTables, SIRStructure[] structs,
			SIRHelper[] helpers, SIRGlobal global, int numCores) {

		this.numCores = numCores;

		// make arguments to functions be three-address code so can replace max,
		// min, abs
		// and possibly others with macros, knowing that there will be no side
		// effects.
		SimplifyArguments.simplify(str);

		// propagate constants and unroll loop
		System.out.println("Running Constant Prop and Unroll...");
		Set<SIRGlobal> theStatics = new HashSet<SIRGlobal>();
		if (global != null)
			theStatics.add(global);

		associatedGlobals = StaticsProp.propagate(str, theStatics);
		ConstantProp.propagateAndUnroll(str);
		ConstantProp.propagateAndUnroll(str, true);
		System.out.println("Done Constant Prop and Unroll...");

		IntroduceMultiPops.doit(str);

		// convert round(x) to floor(0.5+x) to avoid obscure errors
		RoundToFloor.doit(str);
		// add initPath functions for feedback loops
		EnqueueToInitPath.doInitPath(str);

		// construct stream hierarchy from SIRInitStatements
		ConstructSIRTree.doit(str);

		// VarDecl Raise to move array assignments up
		new VarDeclRaiser().raiseVars(str);

		// do constant propagation on fields
		System.out.println("Running Constant Field Propagation...");
		FieldProp.doPropagate(str);
		System.out.println("Done Constant Field Propagation...");

		// expand array initializers loaded from a file
		ArrayInitExpander.doit(str);

		// Currently do not support messages in these back ends.
		// TODO: add support for messages.
		if (SIRPortal.findMessageStatements(str)) {
			throw new AssertionError(
					"Teleport messaging is not yet supported in the Raw backend.");
		}

		// I _think_ this is not needed, that parent pointers
		// in SIRStreams can not be incorrect at this point,
		// but leaving from old code.
		if (str instanceof SIRContainer) {
			((SIRContainer) str).reclaimChildren();
		}

		// ManualPartition.printGraph(str, "numbered.dot");

		// if we are gathering statistics, clone the original stream graph
		// so that we can gather statictics on it, not on the modified graph
		if (KjcOptions.stats) {
			origSTR = (SIRStream) ObjectDeepCloner.deepCopy(str);
		}

		// splitjoin optimization on SIR graph can not be
		// done after fusion, and should not affect fusable
		// pipelines, so do it here.
		Lifter.liftAggressiveSync(str);
		DynamismFinder.Result result = new DynamismFinder().find(str);
		return doStaticPassesSegmentedSIRGraph(new SegmentedSIRGraph().init(str, result.isDynamic()));
	}

	/**
	 * Loop over each SIR in the segmentedGraph and apply optimizations
	 * 
	 * @param segmentedGraph
	 * @return The SLIR representation of each optimized SSG
	 */
	private StreamGraph doStaticPassesSegmentedSIRGraph(SegmentedSIRGraph segmentedGraph) {
		SegmentedSIRGraph optimizedGraph = new SegmentedSIRGraph();
		System.out.println("CommonPasses::segmentedGraph.getStaticSubGraphs().size()=" + segmentedGraph.getStaticSubGraphs().size());
		for (SIRStream str : segmentedGraph.getStaticSubGraphs()) {
			SemanticChecker.doCheck(str);
			str = doStaticPassSIRStream(str);
			optimizedGraph.addToSegmentedGraph(str);
		}
		streamGraph = new SIRToSLIR().translate(optimizedGraph, numCores);
		return streamGraph;

	}

	/**
	 * Equivalent to one call of CommonPass run before re-factoring
	 * to have multiple static subgraphs.
	 * @param str the SIRStream on which to do the single pass
	 * @return The modified SIRStream
	 */
	private SIRStream doStaticPassSIRStream(SIRStream str) {

		// Checks that all filters with mutable states are labeled with
		// stateful keyword
		CheckStatefulFilters.doit(str);

		if (KjcOptions.fusion || KjcOptions.dup >= 1 || KjcOptions.noswpipe) {
			// if we are about to fuse filters, we should perform
			// any vectorization now, since vectorization can not work inside
			// fused sections, and vectorization should map pipelines of
			// stateless filters to pipelines of stateless filters.

			StreamItDot.printGraph(str, "before-vectorize.dot");
			SimplifyPopPeekPush.simplify(str);
			VectorizeEnable.vectorizeEnable(str, null);
			vectorizedEarly = true;
			StreamItDot.printGraph(str, "after-vectorize.dot");
		}

		// fuse entire str to one filter if possible
		if (KjcOptions.fusion)
			str = FuseAll.fuse(str, false);

		WorkEstimate work = WorkEstimate.getWorkEstimate(str);
		work.printGraph(str, "work_estimate.dot");

		if (KjcOptions.tilera != -1) {
			// running the tilera backend

			System.out.println("SIR Filters: "
					+ at.dms.kjc.tilera.TMD.countFilters(str));
			System.out.println("SIR Peeking Filters: "
					+ at.dms.kjc.tilera.TMD.countPeekingFilters(str));

			DuplicateBottleneck dup = new DuplicateBottleneck();
			dup.percentStateless(str);
			str = FusePipelines.fusePipelinesOfStatelessStreams(str);
			StreamItDot.printGraph(str, "after-fuse-stateless.dot");

			if (!at.dms.kjc.tilera.TMD.allLevelsFit(str, KjcOptions.tilera
					* KjcOptions.tilera)) {
				System.out
						.println("Have to fuse the graph because at least one level has too many filters...");
				str = at.dms.kjc.tilera.TMD.SIRFusion(str, KjcOptions.tilera
						* KjcOptions.tilera);
			}
			if (KjcOptions.dup == 1) {
				dup.smarterDuplicate(str, numCores);
			}

		} else if (KjcOptions.smp != -1) {
			// running the smp backend

			System.out.println("SIR Filters: "
					+ at.dms.kjc.smp.TMD.countFilters(str));
			System.out.println("SIR Peeking Filters: "
					+ at.dms.kjc.smp.TMD.countPeekingFilters(str));

			DuplicateBottleneck dup = new DuplicateBottleneck();
			dup.percentStateless(str);
			str = FusePipelines.fusePipelinesOfStatelessStreams(str);
			StreamItDot.printGraph(str, "after-fuse-stateless.dot");

			// if we have a user defined partition, the user wants control
			// otherwise, if any level is too big for the chip, then fuse
			if (KjcOptions.optfile == null)
				str = at.dms.kjc.smp.SMPBackend.scheduler.SIRFusion(str,
						KjcOptions.smp);

			if (KjcOptions.dup == 1) {
				dup.smarterDuplicate(str, numCores);
			}

		} else {
			// some backend other than tilera and smp
			// for right now, we use the dup parameter to specify the type
			// of data-parallelization we are using
			// if we want to enable the data-parallelization
			// stuff from asplos 06, use dup == 1
			if (KjcOptions.dup == 1) {
				DuplicateBottleneck dup = new DuplicateBottleneck();
				dup.percentStateless(str);
				str = FusePipelines.fusePipelinesOfStatelessStreams(str);
				StreamItDot.printGraph(str, "after-fuse-stateless.dot");
				dup.smarterDuplicate(str, numCores);
			} else if (KjcOptions.dup == numCores) {
				// if we want to use fine-grained parallelization
				// then set dup to be the number of tiles (cores)
				DuplicateBottleneck dup = new DuplicateBottleneck();
				System.out.println("Fine-Grained Data Parallelism...");
				dup.duplicateFilters(str, numCores);
			}
		}

		// If not software-pipelining, don't expect to
		// split the stream graph horizontally so fuse
		// pipelines down into individual filters.
		if (KjcOptions.noswpipe)
			str = FusePipelines.fusePipelinesOfFilters(str);

		// Print stream graph after fissing and fusing.
		StreamItDot.printGraph(str, "canonical-graph.dot");

		// this must run before vertical fission
		str = Flattener.doLinearAnalysis(str);
		str = Flattener.doStateSpaceAnalysis(str);

		// vertical fission requested.
		if (KjcOptions.fission > 1) {
			System.out.println("Running Vertical Fission...");
			FissionReplacer.doit(str, KjcOptions.fission);
			Lifter.lift(str);
			System.out.println("Done Vertical Fission...");
		}

		// run user-defined transformations if enabled
		if (KjcOptions.optfile != null) {
			System.err.println("Running User-Defined Transformations...");
			str = ManualPartition.doit(str);
			System.err.println("Done User-Defined Transformations...");
		}

		/* StaticsProp.propagateIntoFilters(str,theStatics); */

		// If requiested, convert splitjoins (below top level)
		// to pipelines of filters.
		if (KjcOptions.sjtopipe) {
			SJToPipe.doit(str);
		}

		StreamItDot.printGraph(str, "before-partition.dot");

		// VarDecl Raise to move array assignments up
		new VarDeclRaiser().raiseVars(str);
		// VarDecl Raise to move peek index up so
		// constant prop propagates the peek buffer index
		// ?? does this really need to be done twice?
		new VarDeclRaiser().raiseVars(str);

		// Make sure all variables have different names.
		// This must be run now, later pass rely on distinct names.
		RenameAll.renameOverAllFilters(str);

		// Raise all pushes, pops, peeks to statement level
		// (several phases above introduce new peeks, pops, pushes
		// including but not limited to doLinearAnalysis)
		// needed before vectorization
		if (!(KjcOptions.linearreplacement || KjcOptions.linearreplacement2
				|| KjcOptions.linearreplacement3 || KjcOptions.atlas
				|| KjcOptions.linearpartition
				|| KjcOptions.frequencyreplacement || KjcOptions.redundantreplacement)) {
			// for now, do not run in combination with linear replacements
			// because some linear expressions do not have type set
			if (!vectorizedEarly && KjcOptions.tilera <= 0
					&& KjcOptions.smp <= 0) {
				SimplifyPopPeekPush.simplify(str);
			}
		} else if (KjcOptions.vectorize > 0) {
			System.err
					.println("Linear analysis + vectorization unsupported, because 3-address\n"
							+ "code cannot infer types of some expressions created.  Can fix\n"
							+ "by setting types in linear analysis, or by adding type inference.");
			System.exit(1);
		}

		// If vectorization enabled, create (fused streams of) vectorized
		// filters.
		// the top level compile script should not allow vectorization to be
		// enabled
		// for processor types that do not support short vectors.
		if (!vectorizedEarly) {
			VectorizeEnable.vectorizeEnable(str, null);
		}

		StreamItDot.printGraph(str, "after-partition.dot");

		// convert locals to fields if desired (can avoid stack overflow for
		// huge programs)
		if (KjcOptions.localstoglobals) {
			ConvertLocalsToFields.doit(str);
		}

		return str;

	}

	/**
	 * Create schedules for init, prime-pump and steady phases. Affected by
	 * KjcOptions.spacetime, KjcOptions.noswpipe. Not called for Tilera!
	 * 
	 * @return a Scheduler from which the schedules for the phases may be
	 *         extracted.
	 */
	public BasicSpaceTimeSchedule scheduleSlices(StaticSubGraph ssg) {
		// Set schedules for initialization, priming (if --spacetime), and
		// steady state.
		BasicSpaceTimeSchedule schedule = new BasicSpaceTimeSchedule(ssg);
		// set init schedule in standard order
		schedule.setInitSchedule(DataFlowOrder.getTraversal(ssg
				.getFilterGraph()));
		// set prime pump schedule (if --spacetime and not --noswpipe)

		new GeneratePrimePump(schedule).schedule(ssg.getFilterGraph());

		// set steady schedule in standard order unless --spacetime in which
		// case in
		// decreasing order of estimated work
		new BasicGenerateSteadyStateSchedule(schedule, ssg).schedule();
		return schedule;
	}

	/**
	 * Get the slicer used in
	 * {@link #run(SIRStream, JInterfaceDeclaration[], SIRInterfaceTable[], SIRStructure[], SIRHelper[], SIRGlobal, int)
	 * run}.
	 * 
	 * @return the slicer
	 */
	public StaticSubGraph getSSG0() {
		assert streamGraph.getNumSSGs() == 1;
		return streamGraph.getSSG(0);
	}

	/**
	 * Get the original stream for statistics gathering. Returns null unless
	 * KjcOptions.stats
	 * 
	 * @return the stream before any graph structure modifications.
	 */
	public SIRStream getOrigSTR() {
		return origSTR;
	}

	/**
	 * Get the names of globals that may have been copied into multiple places.
	 * 
	 * @return
	 */
	public Map<String, Set<String>> getAssociatedGlobals() {
		return associatedGlobals;
	}
}
