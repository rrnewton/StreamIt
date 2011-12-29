package at.dms.kjc.cell;


public class PPU extends CellPU {
    
    public PPU(int uniqueId) {
        super(uniqueId);
    }
    
    @Override
	public boolean isPPU() {
        return true;
    }
    
    @Override
	public boolean isSPU() {
        return false;
    }
}
