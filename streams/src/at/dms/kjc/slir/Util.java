package at.dms.kjc.slir;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import at.dms.kjc.backendSupport.FilterInfo;

public class Util {

	/**
     * Return the number of items that are sent along the <edge> in <phase>.
     */
    public int itemsSentOn(Channel edge, SchedulingPhase phase) {
        int totalItems = FilterInfo.getFilterInfo(getPrevFilter()).totalItemsSent(phase);
        
        double items = totalItems * ratio(edge, phase);
        
        assert items == Math.floor(items);
        return (int)(items);
    }
	
    public int itemsReceivedOn(Channel edge, SchedulingPhase phase) {
        double totalItems = FilterInfo.getFilterInfo(getNextFilter()).totalItemsReceived(phase);
        
        double items = totalItems * ratio(edge, phase);
        assert items == Math.floor(items);
        
        return (int)items;
    }
    
    public boolean hasFileOutput() {
        Iterator dests = getDestSet(SchedulingPhase.STEADY).iterator();
        while (dests.hasNext()) {
            if (((Channel) dests.next()).getDest().isFileOutput())
                return true;
        }
        return false;
    }

    public Set<InputSliceNode> fileOutputs() {
        HashSet<InputSliceNode> fileOutputs = new HashSet<InputSliceNode>();
        Iterator dests = getDestSet(SchedulingPhase.STEADY).iterator();
        while (dests.hasNext()) {
            Channel edge = (Channel) dests.next();
            if (edge.getDest().isFileOutput())
                fileOutputs.add(edge.getDest());
        }
        return fileOutputs;
    }
    
    
}
