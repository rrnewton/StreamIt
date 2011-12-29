package at.dms.kjc.smp;

import java.util.Map;
import java.util.Set;

import at.dms.kjc.CStdType;
import at.dms.kjc.CType;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.StaticSubGraph;

public class ThreadMapper {

	private static ThreadMapper mapper = null;

	private static int threadId = 0;

	/**
	 * Private constructor for singleton
	 */
	private ThreadMapper() {/* do nothing */
	}

	/**
	 * Provides access to the singleton class
	 * @return the single thread mapper
	 */
	public static ThreadMapper getMapper() {
		if (mapper == null) {
			mapper = new ThreadMapper();
		}
		return mapper;
	}

	/**
	 * Assign a unique id to each thread for dynamic readers
	 * @param ssg The ssg that contains the dyamic reader
	 * @param filterToThreadId a mapping from filter to thread id
	 * @param dominated the set of dominated filters
	 * @param dominators a mapping of dominator filter to dominated filter
	 * @param threadIdToType a mapping of thread to its input type
	 */
	public void assignThreads(StaticSubGraph ssg,
			Map<Filter, Integer> filterToThreadId, Set<String> dominated,
			Map<String, String> dominators, Map<Integer, String> threadIdToType) {

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

				System.out.println("ThreadMapper.assignThreads filter = "
						+ dynamicReader.getWorkNode().toString()
						+ " has threadId=" + threadId + " and type="
						+ type.toString());

				filterToThreadId.put(dynamicReader, threadId);

				if (type != CStdType.Void && type != null) {
					threadIdToType.put(threadId, type.toString());
					Filter[] filterGraph = ssg.getFilterGraph();
					for (Filter filter : filterGraph) {
						// Don't dominate yourself
						if (dynamicReader.getWorkNodeContent().getName()
								.equals(filter.getWorkNodeContent().getName()))
							continue;
						// Don't dominate a FileWriter, because its
						// not really a filter
						if (filter.getWorkNode().isFileOutput()) {
							continue;
						}
						
						if (isDynamicInput) {
							dominated
									.add(filter.getWorkNodeContent().getName());
						}
						
						dominators.put(dynamicReader.getWorkNodeContent()
								.getName(), filter.getWorkNodeContent()
								.getName());
					}

				}
				threadId++;
			}

		}

	}
}
