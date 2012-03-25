package at.dms.kjc.smp;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import at.dms.kjc.slir.OutputContent;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.WorkNode;

public class ProcessFileWriter {
    private static int totalOutputs = 0;
    protected WorkNode filterNode;
    protected SchedulingPhase phase;
    protected SMPBackEndFactory factory;
    protected SMPComputeCodeStore codeStore;
    protected OutputContent fileOutput;
    protected static HashMap<WorkNode, Core> allocatingCores;
    protected Core allocatingCore; 

    static {
        allocatingCores = new HashMap<WorkNode, Core>();
    }
    
    public ProcessFileWriter (WorkNode filter, SchedulingPhase phase, SMPBackEndFactory factory) {
        this.filterNode = filter;
        this.fileOutput = (OutputContent)filter.getWorkNodeContent();
        this.phase = phase;
        this.factory = factory;
    }
    
    public static int getTotalOutputs() {
        return totalOutputs;
    }
    
    public static Set<WorkNode> getFileWriterFilters() {
        return allocatingCores.keySet();
    }
    
    /**
     * Return the core that this file writer's buffer should be allocated on.
     * @param fo  The file writer
     */
    public static Core getAllocatingCore(WorkNode fo) {      
        assert fo.isFileOutput();    
    
        if (!allocatingCores.containsKey(fo)) {
            //Core allocatingCore = nextAllocatingCore(fo);
            Core allocatingCore = SMPBackend.getComputeNode(fo);
            
            allocatingCores.put(fo, allocatingCore);
        }
        
        return allocatingCores.get(fo);
    }
    
    /** 
     * Decide on the allocating core for the file writer and create the shared, uncacheable heap
     * on that core the output will be written to.
     */
    public void processFileWriter() {
        //do nothing if faking io
        if (phase == SchedulingPhase.INIT) {
            int outputs = filterNode.getWorkNodeContent().getSteadyMult();
            totalOutputs += outputs;
            assert allocatingCores.containsKey(filterNode);
            allocatingCore = allocatingCores.get(filterNode);
            codeStore = allocatingCore.getComputeCode();

            //codeStore.appendTxtToGlobal("int OUTPUT;\n");
            //codeStore.appendTxtToGlobal("FILE *output;\n");
	    //codeStore.addStatementFirstToBufferInit(Util.toStmt("output = fopen(\"" + fileOutput.getFileName() + "\", \"w\")"));
            //JBlock block = new JBlock();
            //codeStore.addStatementFirstToBufferInit(block);
        }
    }
    
    /**
     * @return The core we should allocate this file reader on.  Remember that 
     * the file reader is allocated to off-chip memory.  We just cycle through the cores
     * if there is more than one file reader, one reader per core.
     */
    private static Core nextAllocatingCore(WorkNode fo) {
        
        System.out.println("ProcessFileWriter.nextAllocatingCore fo=" + fo);
        
        
        List<Core> reverseOrder = SMPBackend.chip.getCores(); 
        //Collections.reverse(reverseOrder);
        
        if(allocatingCores.get(fo) != null)
            return allocatingCores.get(fo);

        // Try cores that are not yet allocating and already have existing code
        for (Core core : reverseOrder) {
            if (!allocatingCores.containsValue(core) && core.getComputeCode().shouldGenerateCode()) {
                allocatingCores.put(fo, core);
		System.out.println(core);
                return core;
            }
        }

        // Try cores that are not yet allocating, but do not already have code
        for (Core core : reverseOrder) {
            if (!allocatingCores.containsValue(core)) {
                allocatingCores.put(fo, core);
                return core;
            }
        }

        assert false : "Too many file readers for this chip (one per core)!";
        return null;
    }
}
