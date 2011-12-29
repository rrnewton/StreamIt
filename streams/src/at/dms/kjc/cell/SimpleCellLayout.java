package at.dms.kjc.cell;

import java.util.HashMap;

import at.dms.kjc.backendSupport.BasicSpaceTimeSchedule;
import at.dms.kjc.backendSupport.Layout;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InternalFilterNode;

public class SimpleCellLayout implements Layout<CellPU> {

    private HashMap<InternalFilterNode,CellPU> layout;
    BasicSpaceTimeSchedule spaceTime;
    CellChip cellChip;
    
    public SimpleCellLayout(BasicSpaceTimeSchedule spaceTime, CellChip cellChip) {
        this.spaceTime = spaceTime;
        this.cellChip = cellChip;
    }
    
    @Override
	public CellPU getComputeNode(InternalFilterNode node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
	public void runLayout() {
        Filter[] schedule = spaceTime.getSchedule();
        for (int i=0; i<schedule.length; i++) {
            Filter s = schedule[i];
            //if (s.getTail().isFileInput() s.getHead().isInputSlice())
        }
    }

    @Override
	public void setComputeNode(InternalFilterNode node, CellPU computeNode) {
        // TODO Auto-generated method stub

    }

}
