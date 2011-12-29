package at.dms.kjc.cell;

import at.dms.kjc.backendSupport.BasicSpaceTimeSchedule;
import at.dms.kjc.backendSupport.NoSWPipeLayout;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InputContent;
import at.dms.kjc.slir.OutputContent;
import at.dms.kjc.slir.WorkNode;

/**
 * Subclass of NoSWPipeLayout that keeps all I/O filters on PPU and all non-I/O filtrers on SPUs
 * @author dimock
 *
 */
public class CellNoSWPipeLayout extends NoSWPipeLayout<CellPU,CellChip> {
    public CellNoSWPipeLayout(BasicSpaceTimeSchedule spaceTime, CellChip chip) {
        super(spaceTime,chip);
    }
    
    @Override
    public void swapAssignment() {
        WorkNode filter1 = (WorkNode)assignedFilters.get(rand.nextInt(assignedFilters.size()));
        if (filter1.getWorkNodeContent() instanceof OutputContent || filter1.getWorkNodeContent() instanceof InputContent) {
            // I/O filters stay where initially assigned: on PPU.
            return;
        }
        // place on a random SPU
        //assignment.put(filter1, chip.getNthComputeNode(rand.nextInt(chip.size()-1) + 1));
    }

    @Override
    public void initialPlacement() {
        int spuTile = 0;
        assert chip.getNthComputeNode(0) instanceof PPU;
        for (Filter slice : scheduleOrder) {
        	if (slice.getInputNode().getNextFilter().getWorkNodeContent() instanceof OutputContent ||
                    slice.getInputNode().getNextFilter().getWorkNodeContent() instanceof InputContent ) {
                assignment.put(slice.getInputNode().getNextFilter(), chip.getNthComputeNode(0));
            } else {
                assignment.put(slice.getInputNode().getNextFilter(), chip.getNthComputeNode(spuTile+1));
                spuTile += 1;
                spuTile = spuTile % (chip.size() - 1);
            }
            assignedFilters.add(slice.getInputNode().getNextFilter());
        }
    }
}
