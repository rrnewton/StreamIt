package at.dms.kjc.smp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import at.dms.kjc.JInterfaceDeclaration;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.backendSupport.BasicSpaceTimeSchedule;
import at.dms.kjc.backendSupport.CommonPasses;
import at.dms.kjc.backendSupport.GeneratePrimePump;
import at.dms.kjc.backendSupport.InterSSGChannel;
import at.dms.kjc.sir.SIRGlobal;
import at.dms.kjc.sir.SIRHelper;
import at.dms.kjc.sir.SIRInterfaceTable;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.SIRStructure;
import at.dms.kjc.slir.DataFlowOrder;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.FilterWorkEstimate;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.StreamGraph;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.slir.WorkNodeInfo;

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

    private static StreamGraph streamGraph;

    public static Core getComputeNode(WorkNode filterNode) {
        return streamGraph.getComputeNode(filterNode);
    }

    public static void run(SIRStream str, JInterfaceDeclaration[] interfaces,
            SIRInterfaceTable[] interfaceTables, SIRStructure[] structs,
            SIRHelper[] helpers, SIRGlobal global) {

        //turn sw pipelining 
        KjcOptions.noswpipe = true;

        if (KjcOptions.smp > 16) {
            setupLargeConfig();
        }

        // create cores in desired amount and order
        int[] cores = new int[KjcOptions.smp];
        for (int x = 0; x < KjcOptions.smp; x++)
            cores[x] = coreOrder[x];
        chip = new SMPMachine(cores);

        checkArguments();
        setScheduler();

        // create a new structs.h file for typedefs etc.
        structs_h = new Structs_h(structs);

        // The usual optimizations and transformation to slice graph
        CommonPasses commonPasses = new CommonPasses();
        // perform standard optimizations, use the number of cores the user
        // wants to target
        streamGraph = commonPasses.run(str, interfaces,
                interfaceTables, structs, helpers, global, chip.size());

        streamGraph.simplifyFilters(chip.size());


        if (streamGraph.getSSGs().size() > 1) {
            isDynamic = true;
        }

        InterSSGChannel.createBuffers(streamGraph);      

        List<BasicSpaceTimeSchedule> graphSchedules = 
                new ArrayList<BasicSpaceTimeSchedule>();

        List<TMDBinPackFissAll> schedulers = 
                new ArrayList<TMDBinPackFissAll>();

        int i = 0;
        for (StaticSubGraph ssg : streamGraph.getSSGs()) {		    
            schedulers.add(new TMDBinPackFissAll());
            scheduler = schedulers.get(i);
            graphSchedules.add(generateSchedules(ssg, i));           
            i++;
        }

        streamGraph.runLayout();                   
        
        for (StaticSubGraph ssg : streamGraph.getSSGs()) {
            ThreadMapper.getMapper().assignThreads(ssg);
        }

        i = 0;    
        for (BasicSpaceTimeSchedule graphSchedule : graphSchedules) {
            scheduler = schedulers.get(i);
            generateCode(graphSchedule);       
            i++;
        }

        streamGraph.dumpGraph("final_graph.dot");

        RotatingBuffer.rotTypeDefs();

        InterSSGChannel.createDynamicQueues();

        emitCode();

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

    public static void setComputeNode(WorkNode workNode, Core core) {
        streamGraph.setComputeNode(workNode, core);              
    }

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

    private static void emitCode() {

        // generate code for file writer
        SMPComputeCodeStore.generatePrintOutputCode(backEndFactory);

        if (KjcOptions.numbers > 0)
            chip.getNthComputeNode(0).getComputeCode().generateNumbersCode();

        // emit c code for all cores
        new EmitSMPCode(backEndFactory, isDynamic).doit();

        // dump structs.h file
        structs_h.writeToFile();

        printFinalWorkAssignments();

        if (isDynamic) {
            dynamicQueueCodeGenerator.writeToFiles();
        }

        System.exit(0);
    }

    private static void generateCode(BasicSpaceTimeSchedule graphSchedule) {     

        // create all buffers and set the rotation lengths
        RotatingBuffer.createBuffers(graphSchedule);

        // now convert to Kopi code plus communication commands
        backEndFactory = new SMPBackEndFactory(chip, scheduler, streamGraph);
        backEndFactory.getBackEndMain().run(graphSchedule, backEndFactory);

        // calculate computation to communication ratio
        if (KjcOptions.sharedbufs && KjcOptions.numbers > 0) {
            calculateCompCommRatio(graphSchedule);
        }

    }

    private static BasicSpaceTimeSchedule generateSchedules(StaticSubGraph ssg, int ssgNum) {	          

        // dump slice graph to dot file
        ssg.dumpGraph("traces_ssg" + ssgNum +".dot", null);

        // partition the slice graph based on the scheduling policy
        BasicSpaceTimeSchedule graphSchedule = new BasicSpaceTimeSchedule(ssg);
        scheduler.setGraphSchedule(graphSchedule);
        scheduler.run(chip.size());
        WorkNodeInfo.reset();

        // generate schedules for initialization, primepump and steady-state
        scheduleSlices(graphSchedule);

        // if load balancing, find candidiate fission groups to load balance
        if (KjcOptions.loadbalance) {
            LoadBalancer.findCandidates();
            LoadBalancer.instrumentMainMethods();
        }

        return graphSchedule;
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
