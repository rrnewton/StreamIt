package at.dms.kjc.smp;

import at.dms.kjc.*;
import at.dms.kjc.backendSupport.*;
import at.dms.kjc.sir.*;
import at.dms.kjc.slir.*;

import java.util.*;

public class SMPBackend {
	public static final boolean FAKE_IO = false;

	public static Scheduler scheduler;
	public static SMPMachine chip;
	public static SMPBackEndFactory backEndFactory;
	public static Structs_h structs_h;
	public static DynamicQueueCodeGenerator dynamicQueueCodeGenerator = new DynamicQueueCodeGenerator();
	public static int[] coreOrder = { 0, 8, 1, 9, 2, 10, 3, 11, 4, 12, 5, 13,
			6, 14, 7, 15 };

	private static boolean isDynamic = false;

	private static void calculateCompCommRatio(
			BasicSpaceTimeSchedule graphSchedule) {
		LinkedList<Filter> slices = DataFlowOrder.getTraversal(graphSchedule
				.getSSG().getTopFilters());
		HashSet<Filter> compProcessed = new HashSet<Filter>();
		HashSet<Filter> commProcessed = new HashSet<Filter>();

		long comp = 0;
		long comm = 0;

		for (Filter slice : slices) {
			if (compProcessed.contains(slice))
				continue;

			comp += FilterWorkEstimate.getWork(slice);
			compProcessed.add(slice);
		}

		// Simple communication estimation
		for (Filter slice : slices) {
			if (commProcessed.contains(slice))
				continue;

			WorkNodeInfo info = WorkNodeInfo.getFilterInfo(slice.getWorkNode());
			int totalItemsReceived = info
					.totalItemsReceived(SchedulingPhase.STEADY);

			if (totalItemsReceived == 0)
				continue;

			comm += totalItemsReceived;

			if (FissionGroupStore.isFizzed(slice)) {
				assert info.peek >= info.pop;
				comm += (info.peek - info.pop) * KjcOptions.smp;
			}

			commProcessed.add(slice);
		}

		System.out.println("Final Computation: " + comp);
		System.out.println("Final Communication: " + comm);
		System.out.println("Final Comp/Comm Ratio: " + (float) comp
				/ (float) comm);

	}

	/**
	 * Check arguments to backend to make sure that they are valid
	 */
	private static void checkArguments() {
		// if debugging and number of iterations unspecified, limit number of
		// iterations
		if (KjcOptions.debug && KjcOptions.iterations == -1)
			KjcOptions.iterations = 100;

		// if load-balancing, enable shared buffers
		if (KjcOptions.loadbalance)
			KjcOptions.sharedbufs = true;

		// if load-balancing, but only 1 core, disable load-balancing
		if (KjcOptions.loadbalance && KjcOptions.smp == 1)
			KjcOptions.loadbalance = false;

		// if using old TMD, make sure not using sharedbufs since they're
		// incompatible
		if (KjcOptions.partitioner.equals("oldtmd")) {
			if (KjcOptions.sharedbufs) {
				System.out
						.println("WARNING: Disabling shared buffers due to incompatibility with old TMD scheduler");
				KjcOptions.sharedbufs = false;
			}
		}
	}

	private static void emitCode(Map<Filter, Integer> threadMap,
			Set<String> dominated, Map<String, String> dominators,
			Map<Integer, String> threadIdToType) {

		// generate code for file writer
		SMPComputeCodeStore.generatePrintOutputCode();

		if (KjcOptions.numbers > 0)
			chip.getNthComputeNode(0).getComputeCode().generateNumbersCode();

		// emit c code for all cores
		new EmitSMPCode(backEndFactory, isDynamic, threadMap, dominated,
				dominators, threadIdToType).doit();

		// dump structs.h file
		structs_h.writeToFile();

		printFinalWorkAssignments();

		if (isDynamic) {
			dynamicQueueCodeGenerator.writeToFiles();
		}

		System.exit(0);
	}

	private static void printFinalWorkAssignments() {
		// display final assignment of filters to cores
		System.out.println("Final filter assignments:");
		System.out.println("========================================");
		for (int x = 0; x < KjcOptions.smp; x++) {
			Core core = chip.getNthComputeNode(x);
			Set<WorkNode> filters = core.getComputeCode().getFilters();
			long totalWork = 0;

			System.out.println("Core " + core.getCoreID() + ": ");
			for (WorkNode filter : filters) {
				long work = FilterWorkEstimate.getWork(filter.getParent());
				System.out.format("%16d | " + filter + "\n", work);
				totalWork += work;
			}
			System.out.format("%16d | Total\n", totalWork);
		}

	}

	public static void run(SIRStream str, JInterfaceDeclaration[] interfaces,
			SIRInterfaceTable[] interfaceTables, SIRStructure[] structs,
			SIRHelper[] helpers, SIRGlobal global) {

		checkArguments();
		setScheduler();

		if (KjcOptions.smp > 16) {
			setupLargeConfig();
		}

		// create cores in desired amount and order
		int[] cores = new int[KjcOptions.smp];
		for (int x = 0; x < KjcOptions.smp; x++)
			cores[x] = coreOrder[x];
		chip = new SMPMachine(cores);

		// create a new structs.h file for typedefs etc.
		structs_h = new Structs_h(structs);

		// The usual optimizations and transformation to slice graph
		CommonPasses commonPasses = new CommonPasses();
		// perform standard optimizations, use the number of cores the user
		// wants to target
		StreamGraph streamGraph = commonPasses.run(str, interfaces,
				interfaceTables, structs, helpers, global, chip.size());

		streamGraph.simplifyFilters(chip.size());

		if (streamGraph.getSSGs().size() > 1) {
			isDynamic = true;
		}

		InterSSGChannel.createBuffers(streamGraph);

		Map<Filter, Integer> filterToThreadId = new HashMap<Filter, Integer>();
		Set<String> dominated = new HashSet<String>();
		Map<String, String> dominators = new HashMap<String, String>();
		Map<Integer, String> threadIdToType = new HashMap<Integer, String>();

		for (StaticSubGraph ssg : streamGraph.getSSGs()) {
			ThreadMapper.getMapper().assignThreads(ssg, filterToThreadId,
					dominated, dominators, threadIdToType);
		}

		for (StaticSubGraph ssg : streamGraph.getSSGs()) {

			runSSG(ssg, filterToThreadId, dominated, dominators, threadIdToType);
		}

		RotatingBuffer.rotTypeDefs();

		InterSSGChannel.createDynamicQueues();

		chip.setThreadMap(filterToThreadId);

		emitCode(filterToThreadId, dominated, dominators, threadIdToType);

	}

	private static void runSSG(StaticSubGraph ssg,
			Map<Filter, Integer> filterToThreadId, Set<String> dominated,
			Map<String, String> dominators, Map<Integer, String> threadIdToType) {

//		System.out.println("SMPBackend.runSSG ssg="
//				+ ssg.getTopFilters()[0].getWorkNode());

		// dump slice graph to dot file
		ssg.dumpGraph("traces.dot", null);

		// partition the slice graph based on the scheduling policy
		BasicSpaceTimeSchedule graphSchedule = new BasicSpaceTimeSchedule(ssg);
		scheduler.setGraphSchedule(graphSchedule);
		scheduler.run(chip.size());
		WorkNodeInfo.reset();

		// generate schedules for initialization, primepump and steady-state
		scheduleSlices(graphSchedule);

		// generate layout for filters
		scheduler.runLayout();
		// dump final slice graph to dot file
		graphSchedule.getSSG()
				.dumpGraph("after_slice_partition.dot", scheduler);
		graphSchedule.getSSG().dumpGraph("slice_graph.dot", scheduler, false);
		// if load balancing, find candidiate fission groups to load balance
		if (KjcOptions.loadbalance) {
			LoadBalancer.findCandidates();
			LoadBalancer.instrumentMainMethods();
		}

		// create all buffers and set the rotation lengths
		RotatingBuffer.createBuffers(graphSchedule);

		// now convert to Kopi code plus communication commands
		backEndFactory = new SMPBackEndFactory(chip, scheduler, dominators,
				filterToThreadId);
		backEndFactory.getBackEndMain().run(graphSchedule, backEndFactory);

		// calculate computation to communication ratio
		if (KjcOptions.sharedbufs && KjcOptions.numbers > 0) {
			calculateCompCommRatio(graphSchedule);
		}

	}

	/**
	 * Create schedules for init, prime-pump and steady phases.
	 * 
	 * @return a Scheduler from which the schedules for the phases may be
	 *         extracted.
	 */
	public static void scheduleSlices(BasicSpaceTimeSchedule schedule) {
		StaticSubGraph slicer = schedule.getSSG();

		// set init schedule in standard order
		schedule.setInitSchedule(DataFlowOrder.getTraversal(slicer
				.getFilterGraph()));

		// set the prime pump to be empty
		new GeneratePrimePump(schedule).setEmptySchedule();

		// for space multiplexing on SMP we need to use a different primepump
		// scheduler because
		// we are space multiplexing and we need to prime the pipe more so that
		// everything can fire
		// when ready
		if (at.dms.kjc.smp.SMPBackend.scheduler.isSMD())
			new at.dms.kjc.smp.GeneratePrimePumpScheduleSMD(schedule)
					.schedule(slicer.getFilterGraph());
		else
			new GeneratePrimePump(schedule).schedule(slicer.getFilterGraph());

		// Still need to generate the steady state schedule!
		schedule.setSchedule(DataFlowOrder.getTraversal(slicer.getTopFilters()));

	}

	/**
	 * Set the scheduler field to the correct leaf class that implements a
	 * scheduling policy.
	 */
	private static void setScheduler() {
		if (KjcOptions.partitioner.equals("tmd")) {
			scheduler = new TMDBinPackFissAll();
		} else if (KjcOptions.partitioner.equals("oldtmd")) {
			scheduler = new TMD();
		} else if (KjcOptions.partitioner.equals("smd")) {
			scheduler = new SMD();
		} else {
			System.err.println("Unknown Scheduler Type!");
			System.exit(1);
		}
	}

	/**
	 * For chip sizes over 8 cores, use a larger core order map.
	 */
	private static void setupLargeConfig() {
		assert KjcOptions.smp <= 32 && KjcOptions.smp > 8 : "Only core configurations of 32 cores or less are supported!";
		int[] cores = { 0, 16, 1, 17, 2, 18, 3, 19, 4, 20, 5, 21, 6, 22, 7, 23,
				8, 24, 9, 25, 10, 26, 11, 27, 12, 28, 13, 29, 14, 30, 15, 31 };
		coreOrder = cores;

	}
}
