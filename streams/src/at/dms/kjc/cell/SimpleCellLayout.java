package at.dms.kjc.cell;

import java.util.HashMap;

import at.dms.kjc.backendSupport.Layout;
import at.dms.kjc.backendSupport.SpaceTimeScheduleAndSlicer;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.SliceNode;

public class SimpleCellLayout implements Layout<CellPU> {

    private HashMap<SliceNode,CellPU> layout;
    SpaceTimeScheduleAndSlicer spaceTime;
    CellChip cellChip;
    
    public SimpleCellLayout(SpaceTimeScheduleAndSlicer spaceTime, CellChip cellChip) {
        this.spaceTime = spaceTime;
        this.cellChip = cellChip;
    }
    
    public CellPU getComputeNode(SliceNode node) {
        // TODO Auto-generated method stub
        return null;
    }

    public void runLayout() {
        Filter[] schedule = spaceTime.getSchedule();
        for (int i=0; i<schedule.length; i++) {
            Filter s = schedule[i];
            //if (s.getTail().isFileInput() s.getHead().isInputSlice())
        }
    }

    public void setComputeNode(SliceNode node, CellPU computeNode) {
        // TODO Auto-generated method stub

    }

}
