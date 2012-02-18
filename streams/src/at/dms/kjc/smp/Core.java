package at.dms.kjc.smp;

import java.util.Map;

import at.dms.kjc.CStdType;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.backendSupport.ComputeNode;
import at.dms.kjc.common.ALocalVariable;
import at.dms.kjc.slir.Filter;

public class Core extends ComputeNode<SMPComputeCodeStore> {
    /** the core ID */
    protected int coreID;
    /** the parent machine */
    protected SMPMachine machine;

    /**
     * Construct a new ComputeNode of chip. 
     * 
     * @param machine The parent Machine.
     */
    public Core(int coreID, SMPMachine machine) 
    {
        super();
        this.machine = machine;
        this.coreID = coreID;
        setUniqueId(coreID);
        
        if(KjcOptions.iterations == -1)
        	computeCode = new SMPComputeCodeStore(this);
        else
        	computeCode = new SMPComputeCodeStore(this, ALocalVariable.makeVar(CStdType.Integer, "maxSteadyIter"));
    }
    
    /**
     * Return the core number of this core
     * 
     * @return The core number
     */
    public int getCoreID() {
       return getUniqueId();
    }
    
    /**
     * Return the Machine we are a part of.
     * @return the Machine we are a part of.
     */
    public SMPMachine getMachine() {
        return machine;
    }
    
    @Override
	public SMPComputeCodeStore getComputeCode() {
        return computeCode;
    }
    
    public boolean isComputeNode() {
        return true;
    }
}
