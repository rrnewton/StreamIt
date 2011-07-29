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
 * @author soule
 *
 */
public class DynamismFinder implements StreamVisitor {

	private boolean isDynamic = false;
	
	Set<SIRStream> dynamicFilters = null;
	
	public DynamismFinder() {
		// Do nothing
	}
	
	public boolean find(SIRStream str) {
		dynamicFilters = new HashSet<SIRStream>();
		isDynamic = false;
		IterFactory.createFactory().createIter(str).accept(this);
		return isDynamic;
	}

	
	/* (non-Javadoc)
	 * @see at.dms.kjc.sir.StreamVisitor#visitFilter(at.dms.kjc.sir.SIRFilter, at.dms.kjc.iterator.SIRFilterIter)
	 */
	@Override
	public void visitFilter(SIRFilter self, SIRFilterIter iter) {
		// TODO Auto-generated method stub
		if (isDynamicPop(self)) {
			isDynamic = true;
			dynamicFilters.add(self);
		}
		if (isDynamicPush(self)) {			
			isDynamic = true;		
			dynamicFilters.add(self);
		}		
	}

	/* (non-Javadoc)
	 * @see at.dms.kjc.sir.StreamVisitor#visitPhasedFilter(at.dms.kjc.sir.SIRPhasedFilter, at.dms.kjc.iterator.SIRPhasedFilterIter)
	 */
	@Override
	public void visitPhasedFilter(SIRPhasedFilter self, SIRPhasedFilterIter iter) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see at.dms.kjc.sir.StreamVisitor#preVisitPipeline(at.dms.kjc.sir.SIRPipeline, at.dms.kjc.iterator.SIRPipelineIter)
	 */
	@Override
	public void preVisitPipeline(SIRPipeline self, SIRPipelineIter iter) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see at.dms.kjc.sir.StreamVisitor#preVisitSplitJoin(at.dms.kjc.sir.SIRSplitJoin, at.dms.kjc.iterator.SIRSplitJoinIter)
	 */
	@Override
	public void preVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		// TODO Auto-generated method stub
		if (self.getSplitter().getType().isDynamic()) {
			// If it is not dynamic, we can add the splitter to the pipeline			
		}
	}

	/* (non-Javadoc)
	 * @see at.dms.kjc.sir.StreamVisitor#preVisitFeedbackLoop(at.dms.kjc.sir.SIRFeedbackLoop, at.dms.kjc.iterator.SIRFeedbackLoopIter)
	 */
	@Override
	public void preVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see at.dms.kjc.sir.StreamVisitor#postVisitPipeline(at.dms.kjc.sir.SIRPipeline, at.dms.kjc.iterator.SIRPipelineIter)
	 */
	@Override
	public void postVisitPipeline(SIRPipeline self, SIRPipelineIter iter) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see at.dms.kjc.sir.StreamVisitor#postVisitSplitJoin(at.dms.kjc.sir.SIRSplitJoin, at.dms.kjc.iterator.SIRSplitJoinIter)
	 */
	@Override
	public void postVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see at.dms.kjc.sir.StreamVisitor#postVisitFeedbackLoop(at.dms.kjc.sir.SIRFeedbackLoop, at.dms.kjc.iterator.SIRFeedbackLoopIter)
	 */
	@Override
	public void postVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) {
		// TODO Auto-generated method stub
		
	}

	private boolean isDynamicPop(SIRFilter filter) {
		return filter.getPop().isDynamic();
	}

	private boolean isDynamicPush(SIRFilter filter) {
		return filter.getPush().isDynamic();
	}

	
}
