package at.dms.kjc.smp;
import java.util.List;
import java.util.Map;

import at.dms.kjc.backendSupport.BasicSpaceTimeSchedule;
import at.dms.kjc.slir.DataFlowOrder;
import at.dms.kjc.slir.Filter;

public class ThreadMapper {

	public ThreadMapper() {/* do nothing */}

	public void assignThreads(BasicSpaceTimeSchedule graphSchedule, Map<Filter, Integer> threadMap) { 
        List<Filter> filters = DataFlowOrder.getTraversal(graphSchedule.getSSG().getTopFilters());

        
        for (Filter filter : filters) {
        	System.out.println("workNodeContent is " + filter.getWorkNodeContent().getName());
        	
        }
        
        
	}
}
