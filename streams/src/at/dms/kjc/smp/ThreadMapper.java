package at.dms.kjc.smp;

import java.util.Map;
import java.util.Set;

import at.dms.kjc.backendSupport.BasicSpaceTimeSchedule;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.StaticSubGraph;

public class ThreadMapper {

	private static ThreadMapper mapper = null;

	private static int threadId = 0;

	private ThreadMapper() {/* do nothing */
	}

	public static ThreadMapper getMapper() {
		if (mapper == null) {
			mapper = new ThreadMapper();
		}
		return mapper;
	}

	public void assignThreads(BasicSpaceTimeSchedule graphSchedule,
			Map<Filter, Integer> threadMap,
			Set<String> dominated, 
			Map<String, String> dominators
			) {

		StaticSubGraph ssg = graphSchedule.getSSG();
						
		/* Check if it has a dynamic pop rate */
		if (ssg.hasDynamicInput()) {
			Filter[] topFilters = ssg.getTopFilters();

			for (int i = 0; i < topFilters.length; i++) {
				Filter dynamicReader = topFilters[i];
				System.out.println("ThreadMapper.assignThreads Filter "
						+ dynamicReader.getWorkNodeContent().getName()
						+ " is a dynamic reader");
				threadMap.put(dynamicReader, threadId);
				
				Filter[] filterGraph = ssg.getFilterGraph();
				
				for (Filter filter : filterGraph) {
					if (dynamicReader.getWorkNodeContent().getName().equals(filter.getWorkNodeContent().getName()))
						continue;		
					System.out.println("ThreadMapper.assignThreads Filter "
						+ dynamicReader.getWorkNodeContent().getName()
						+ " dominates "
						+ filter.getWorkNodeContent().getName()
						);
					dominated.add(filter.getWorkNodeContent().getName());
					dominators.put(dynamicReader.getWorkNodeContent().getName(), filter.getWorkNodeContent().getName());
				}
				
				
				threadId++;
			}

		}

	}
}
