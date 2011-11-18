package at.dms.kjc.slir;

/**
 * Remove the synchronization caused by an identity filters.  
 *	
 *	 @author mgordon
 *	
 */
public class SynchRemoval {
	
	/**
	 * Remove the synchronization caused by an identity filters.  This 
	 * method calls IDFilterRemoval for all filters that have a worknodecontent
	 * that is a IDFilterContent.
	 * 
	 * @param ssg The SSG to remove synchronization from
	 */
	public static void doit(StaticSubGraph ssg) {
		//loop over all the nodes in the ssg
		//for the identity nodes, call IDRemoval on them
		
		for (Filter filter : ssg.getFilterGraph()) {
			if (filter.getWorkNodeContent().isIdentityContent()) {
				IDFilterRemoval.doit(filter);
			}
		}
		
	}
	
}
