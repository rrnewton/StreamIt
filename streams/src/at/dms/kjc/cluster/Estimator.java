
package at.dms.kjc.cluster;

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
 * The class allows forcing of the recalculation of estimates of 
 * code and local variable size for the entire stream program.
 * Forcing is necessary since {@link CodeEstimate} caches 
 * old values.
 * @see CodeEstimate
 */

class Estimator implements StreamVisitor {

    //public Estimator() {}

    /**
     * Force recalculation of code and locals size.
     * @param str the top level stream 
     */

    public static void estimate(SIRStream str) {
        Estimator est = new Estimator();
        System.err.print("Estimating Code size of Filters...");
        IterFactory.createFactory().createIter(str).accept(est);
        System.err.println(" done.");
    }

    /**
     * Recalculate code and locals size for a filter
     */

    @Override
	public void visitFilter(SIRFilter filter,
                            SIRFilterIter iter) { 

        int code, locals;
    
        CodeEstimate est = CodeEstimate.estimate(filter);
        code = est.getCodeSize();
        locals = est.getLocalsSize();

        //System.out.println("Estimator Filter: "+filter+" Code: "+code+" Locals: "+locals);
    
    }

    /**
     * Phased Filters are not supported!
     */

    @Override
	public void visitPhasedFilter(SIRPhasedFilter self,
                                  SIRPhasedFilterIter iter) {
        // This is a stub; it'll get filled in once we figure out how phased
        // filters should actually work.
    }
    
    /**
     * PRE-VISITS 
     */
        
    /* pre-visit a pipeline */
    @Override
	public void preVisitPipeline(SIRPipeline self, SIRPipelineIter iter) {}
    
    /* pre-visit a splitjoin */
    @Override
	public void preVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {}
    
    /* pre-visit a feedbackloop */
    @Override
	public void preVisitFeedbackLoop(SIRFeedbackLoop self, SIRFeedbackLoopIter iter) {}
    
    /**
     * POST-VISITS 
     */
        
    /* post-visit a pipeline */
    @Override
	public void postVisitPipeline(SIRPipeline self, SIRPipelineIter iter) {}
   
    /* post-visit a splitjoin */
    @Override
	public void postVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {}

    /* post-visit a feedbackloop */
    @Override
	public void postVisitFeedbackLoop(SIRFeedbackLoop self, SIRFeedbackLoopIter iter) {}


}


