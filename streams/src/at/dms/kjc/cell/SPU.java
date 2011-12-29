package at.dms.kjc.cell;


public class SPU extends CellPU {

    public SPU(int uniqueId) {
        super(uniqueId);
    }
    
    @Override
	public boolean isPPU() {
        return false;
    }
    
    @Override
	public boolean isSPU() {
        return true;
    }  
}
