package at.dms.kjc.cell;

import java.util.ArrayList;
import java.util.HashMap;

import at.dms.kjc.backendSupport.ComputeNode;
import at.dms.kjc.slir.InternalFilterNode;

public abstract class CellPU extends ComputeNode<CellComputeCodeStore> {

    private HashMap<InternalFilterNode,CellComputeCodeStore> computeCodeStores;
    private HashMap<InternalFilterNode,CellComputeCodeStore> initComputeCodeStores;
    
    protected CellPU(int uniqueId) {
        super();
        setUniqueId(uniqueId);
        computeCode = new CellComputeCodeStore(this);
        computeCodeStores = new HashMap<InternalFilterNode,CellComputeCodeStore>();
        initComputeCodeStores = new HashMap<InternalFilterNode,CellComputeCodeStore>();
    }
    
    public CellComputeCodeStore getComputeCodeStore(InternalFilterNode s) {
        CellComputeCodeStore cs = computeCodeStores.get(s);
        if (cs == null) {
            cs = new CellComputeCodeStore(this, s);
            computeCodeStores.put(s, cs);
        }
        return cs;
    }
    
    public ArrayList<CellComputeCodeStore> getComputeCodeStores() {
        ArrayList<CellComputeCodeStore> codestores = new ArrayList<CellComputeCodeStore>();
        for (CellComputeCodeStore cs : computeCodeStores.values()) {
            codestores.add(cs);
        }
        return codestores;
    }
    
    public int getNumComputeCodeStores() {
        return computeCodeStores.size();
    }
    
    
    public CellComputeCodeStore getInitComputeCodeStore(InternalFilterNode s) {
        CellComputeCodeStore cs = initComputeCodeStores.get(s);
        if (cs == null) {
            cs = new CellComputeCodeStore(this, s);
            initComputeCodeStores.put(s, cs);
        }
        return cs;
    }
    
    public ArrayList<CellComputeCodeStore> getInitComputeCodeStores() {
        ArrayList<CellComputeCodeStore> codestores = new ArrayList<CellComputeCodeStore>();
        for (CellComputeCodeStore cs : initComputeCodeStores.values()) {
            codestores.add(cs);
        }
        return codestores;
    }
    
    public int getNumInitComputeCodeStores() {
        return initComputeCodeStores.size();
    }
    
    abstract public boolean isPPU();
    abstract public boolean isSPU();
}
