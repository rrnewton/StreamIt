package at.dms.kjc.smp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import at.dms.kjc.backendSupport.BasicSpaceTimeSchedule;
import at.dms.kjc.backendSupport.ComputeNode;
import at.dms.kjc.backendSupport.Layout;
import at.dms.kjc.slir.DataFlowOrder;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InternalFilterNode;
import at.dms.kjc.slir.StaticSubGraph;


public class HandLayout implements Layout {
    
    protected StaticSubGraph ssg;
    protected SMPMachine chip;
    protected LinkedList<Filter> scheduleOrder;
    protected HashMap<InternalFilterNode, Core> assignment;    
        
    public HandLayout(BasicSpaceTimeSchedule spaceTime, SMPMachine chip) {
        this.chip = chip;
        this.ssg = spaceTime.getSSG();
        scheduleOrder = 
            DataFlowOrder.getTraversal(spaceTime.getSSG().getFilterGraph());
        assignment = new HashMap<InternalFilterNode, Core>();
    }

    @Override
	public ComputeNode getComputeNode(InternalFilterNode node) {
        return assignment.get(node);
    }

    /**
     * Given a Buffered reader, get the tile number assignment from the reader
     * for <pre>slice</pre>
     */
    private void assignFromReader(BufferedReader inputBuffer,
                                  Filter slice) {
        // Assign a filter, joiner to a tile
        // perform some error checking.
        while (true) {
            int tileNumber;
            String str = null;

            System.out.print(slice.getWorkNode().toString() + ": ");
            try {
                str = inputBuffer.readLine();
                tileNumber = Integer.valueOf(str).intValue();
            } catch (Exception e) {
                System.out.println("Bad number " + str);
                continue;
            }
            if (tileNumber < 0 || tileNumber >= chip.size()) {
                System.out.println("Bad tile number!");
                continue;
            }
            Core tile = chip.getNthComputeNode(tileNumber);
            if (assignment.values().contains(tile)) {
                System.out.println("Tile Already Assigned!");
                continue;
            }
            // other wise the assignment is valid, assign and break!!
            System.out.println("Assigning " + slice.getWorkNode().toString() + " to tile "
                               + tileNumber);
            setComputeNode(slice.getWorkNode(), tile);
            break;
        }
    }
    
    @Override
	public void runLayout() {
        Iterator<Filter> slices = scheduleOrder.iterator();
                
        System.out.println("Enter desired tile for each filter: ");
        BufferedReader inputBuffer = 
                new BufferedReader(new InputStreamReader(System.in));
        
        while (slices.hasNext()) {
          Filter slice = slices.next();

          

          assignFromReader(inputBuffer, slice);
        }
    }

    @Override
	public void setComputeNode(InternalFilterNode node, ComputeNode tile) {
        assignment.put(node, (Core)tile);
    }

}
