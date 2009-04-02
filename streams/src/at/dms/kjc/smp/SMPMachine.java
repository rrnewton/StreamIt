package at.dms.kjc.smp;

import java.util.LinkedList;
import at.dms.kjc.backendSupport.ComputeNodesI;

public class SMPMachine implements ComputeNodesI<CoreCodeStore> {
    protected int numCores;
    protected Core[] cores;
    protected OffChipMemory offChipMemory;
    
    /**
     * Data structures for a machine with a number of cores
     *   
     * @param numCores
     */
    public SMPMachine(int numCores) {
        this.numCores = numCores;
        
        // room for ComputeNode's, to be filled in by subclass
        cores = new Core[numCores];
        for (int x = 0 ; x < numCores ; x++)
            cores[x] = new Core(x, this);

        offChipMemory = new OffChipMemory(this);
    }
    
    public SMPMachine(int[] coreIDOrder) {
    	this.numCores = coreIDOrder.length;
    	
    	// Make sure that set of core IDs is contiguous starting from 0
    	for (int x = 0 ; x < numCores ; x++) {
    		boolean xFound = false;
    		
    		for(int y = 0 ; y < numCores ; y++)
    			if (coreIDOrder[y] == x)
    				xFound = true;

    		if(!xFound) {
    			System.out.println("Set of core IDs is not contiguous starting from 0");
    			assert(false);
    		}
    	}
    	
    	// Construct cores with IDs in requested order
    	cores = new Core[numCores];
    	for (int x = 0 ; x < numCores ; x++)
    		cores[x] = new Core(coreIDOrder[x], this);
    	
    	offChipMemory = new OffChipMemory(this);
    }
    
    public SMPMachine() {
        this(1);
    }
    
    public OffChipMemory getOffChipMemory() {
        return offChipMemory;
    }
    
    public LinkedList<Core> getCores() {
        LinkedList<Core> ts = new LinkedList<Core>();
        for (int x = 0; x < cores.length; x++)
            ts.add(cores[x]);

        return ts;
    }
    
    public boolean canAllocateNewComputeNode() {
        return false;
    }
    
    public Core getNthComputeNode(int n) {
        assert !(n > numCores || n < 0) : "out of bounds in getNthComputeNode() of Machine";
        return cores[n];
    }

    public boolean isValidComputeNodeNumber(int nodeNumber) {
        if(nodeNumber >= 0 && nodeNumber < numCores)
            return true;
        
        return false;
    }

    public int newComputeNode() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int size() {
        return numCores;
    }

    /** 
     * Return the number of cores that have code that needs to execute.
     * 
     * @return the number of cores that have code that needs to execute.
     */
    public int coresWithCompute() {
        int mappedCores = 0;
        
        for (Core t : getCores()) {
            if (t.getComputeCode().shouldGenerateCode()) 
                mappedCores++;
        }
        
        return mappedCores;
    }
    
    public Core getNextCore(Core core) {
    	int coreID = core.getCoreID();
    	
    	for (int x = 0 ; x < numCores - 1 ; x++)
    		if(cores[x].getCoreID() == coreID)
    			return cores[x + 1];
    	
    	return null;
    }
}
