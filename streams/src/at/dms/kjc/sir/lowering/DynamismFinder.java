/**
 * 
 */
package at.dms.kjc.sir.lowering;

import java.util.HashSet;
import java.util.Set;

import at.dms.kjc.iterator.IterFactory;
import at.dms.kjc.iterator.SIRFeedbackLoopIter;
import at.dms.kjc.iterator.SIRFilterIter;
import at.dms.kjc.iterator.SIRPhasedFilterIter;
import at.dms.kjc.iterator.SIRPipelineIter;
import at.dms.kjc.iterator.SIRSplitJoinIter;
import at.dms.kjc.sir.SIRFeedbackLoop;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRPhasedFilter;
import at.dms.kjc.sir.SIRPipeline;
import at.dms.kjc.sir.SIRSplitJoin;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.StreamVisitor;

/**
 * A visitor to check if a stream graph contains any dynamic sections.
 * 
 * @author soule
 *
 */
public class DynamismFinder implements StreamVisitor {

	/** An internal class to hold the return value for a call to find
	 * 
	 * @author soule
	 
	 */
	public class Result {		
		/** flag set to true if there is a dynamic rate */
		private boolean isDynamic = false;

		/** The set of filters with dynamic rates */
		private Set<SIRStream> dynamicFilters = null;

		public Result() {
			isDynamic = false;
			dynamicFilters = new HashSet<SIRStream>();
		}
		
		public boolean isDynamic() {
			return isDynamic;
		}

		public void setDynamic(boolean isDynamic) {
			this.isDynamic = isDynamic;
		}

		public Set<SIRStream> getDynamicFilters() {
			return dynamicFilters;
		}

		public void setDynamicFilters(Set<SIRStream> dynamicFilters) {
			this.dynamicFilters = dynamicFilters;
		}

		public void addDynamicFilter(SIRStream str) {
			dynamicFilters.add(str);
		}
	
	}
	
	/** An instance of the result class that corresponds to one
	 * call to find.
	 */
	private Result result;
	
	/**
	 * Constructs a new DynamismFinder.
	 */
	public DynamismFinder() {
		// Do nothing
	}

	/**
	 * Vists all filters in str to check for dynamic rates.
	 * @param str the stream to check for dynamic rates.
	 * @return true if str contains a dynamic rate.
	 */
	public Result find(SIRStream str) {		
		result = new Result();		
		IterFactory.createFactory().createIter(str).accept(this);
		return result;
	}
	
	/**
	 * Sets the flag to true if there is a dynamic filter.
	 * @param self the filter to visit
	 * @param iter the iterator visiting
	 * @return void
	 */
	@Override
	public void visitFilter(SIRFilter self, SIRFilterIter iter) {
		if (isDynamicPop(self)) {
			result.setDynamic(true);
			result.addDynamicFilter(self);
		}
		if (isDynamicPush(self)) {	
			result.setDynamic(true);
			result.addDynamicFilter(self);
		}		
	}

	/**
	 * Throws an exception if called. Dynamic rates do not support SIRPhasedFilters.
	 * @param self the filter to visit
	 * @param iter the iterator visiting
	 * @return void
	 */
	@Override
	public void visitPhasedFilter(SIRPhasedFilter self, SIRPhasedFilterIter iter) {
        throw new java.lang.RuntimeException("Dynamic rates do not support SIRPhasedFilters.");
	}

	/**
	 * Visits a Pipeline
	 * @param self the filter to visit
	 * @param iter the iterator visiting
	 * @return void
	 */
	@Override
	public void preVisitPipeline(SIRPipeline self, SIRPipelineIter iter) {
		/* Do nothing */		
	}

	/**
	 * Visits a SplitJoin
	 * @param self the filter to visit
	 * @param iter the iterator visiting
	 * @return void
	 */
	@Override
	public void preVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		// If it is not dynamic, we can add the splitter to the pipeline			
		if (self.getSplitter().getType().isDynamic()) {
			result.setDynamic(true);
			result.addDynamicFilter(self);
		}
	}

	/**
	 * Visits a Feedback loop
	 * @param self the filter to visit
	 * @param iter the iterator visiting
	 * @return void
	 */
	@Override
	public void preVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) {
		// TODO: Check if any of the children are dynamic. If they are, we should signal an error here.		
	}

	/** 
	 * Not used by DynamismFinder class. 
	 */
	@Override
	public void postVisitPipeline(SIRPipeline self, SIRPipelineIter iter) { /* do nothing */ }

	/** 
	 * Not used by DynamismFinder class. 
	 */
	@Override
	public void postVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter)  { /* do nothing */ }

	/** 
	 * Not used by DynamismFinder class. 
	 */
	@Override public void postVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter)  { /* do nothing */ }

	/** 
	 * Check if filter has a dynamic pop rate.
     * @param filter The filter to check.
     * @return true if filter has a dynamic pop rate.
	 */
	private boolean isDynamicPop(SIRFilter filter) {
		return filter.getPop().isDynamic();
	}

	/** 
	 * Check if filter has a dynamic push rate.
     * @param filter The filter to check.
     * @return true if filter has a dynamic push rate.
	 */
	private boolean isDynamicPush(SIRFilter filter) {
		return filter.getPush().isDynamic();
	}
	
}
