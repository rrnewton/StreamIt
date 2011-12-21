package at.dms.kjc.smp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import at.dms.kjc.CStdType;
import at.dms.kjc.CType;
import at.dms.kjc.backendSupport.BasicSpaceTimeSchedule;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InterSSGEdge;
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

	public void assignThreads(StaticSubGraph ssg,
			Map<Filter, Integer> filterToThreadId,
			Set<String> dominated, 
			Map<String, String> dominators,
			Map<Integer, String> threadIdToType
			) {


		boolean isDynamicInput = ssg.hasDynamicInput();
		Filter f = ssg.getTopFilters()[0];

		boolean isStateful = f.getWorkNodeContent().isStateful();

		/* Check if it has a dynamic pop rate */
		if (isDynamicInput || isStateful) {
			Filter[] topFilters = ssg.getTopFilters();

			for (int i = 0; i < topFilters.length; i++) {
				Filter dynamicReader = topFilters[i];

				if (CStdType.Void == dynamicReader.getInputNode().getType()) {
						continue;					
				}
										
				CType type = dynamicReader.getWorkNodeContent().getInputType();				

				System.out.println("**** ThreadMapper.assignThreads filter = " + dynamicReader.getWorkNode().toString()
						+ " has threadId=" + threadId
						+ " and type=" + type.toString()
						);

				filterToThreadId.put(dynamicReader, threadId);


				if (type != CStdType.Void && type != null) {					
					threadIdToType.put(threadId, type.toString());												
					Filter[] filterGraph = ssg.getFilterGraph();				
					for (Filter filter : filterGraph) {
						if (dynamicReader.getWorkNodeContent().getName().equals(filter.getWorkNodeContent().getName()))
							continue;		
//						System.out.println("ThreadMapper.assignThreads Filter "
//								+ dynamicReader.getWorkNodeContent().getName()
//								+ " dominates "
//								+ filter.getWorkNodeContent().getName()
//								);
						if (isDynamicInput) {
							dominated.add(filter.getWorkNodeContent().getName());
						}
						dominators.put(dynamicReader.getWorkNodeContent().getName(), filter.getWorkNodeContent().getName());
					}

				}
				threadId++;
			}

		}

	}
}
