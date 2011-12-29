package at.dms.kjc.sir.lowering.partition;

import at.dms.kjc.iterator.IterFactory;
import at.dms.kjc.iterator.SIRFeedbackLoopIter;
import at.dms.kjc.iterator.SIRFilterIter;
import at.dms.kjc.iterator.SIRPhasedFilterIter;
import at.dms.kjc.iterator.SIRPipelineIter;
import at.dms.kjc.iterator.SIRSplitJoinIter;
import at.dms.kjc.sir.SIRContainer;
import at.dms.kjc.sir.SIRFeedbackLoop;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRPhasedFilter;
import at.dms.kjc.sir.SIRPipeline;
import at.dms.kjc.sir.SIRSplitJoin;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.StreamVisitor;
import at.dms.kjc.sir.lowering.fusion.Lifter;

public class SJToPipe implements StreamVisitor {

    private SJToPipe() {}

    /**
     * Lift everything we can in <pre>str</pre> and its children
     * <br/>
     * Will not convert the top-level SIRStream with which it is called.
     * Replaces children as possible in a bottom-up fashion.
     * Therefore, it does not need to return a new Stream.
     */
    public static void doit(SIRStream str) {
        IterFactory.createFactory().createIter(str).accept(new SJToPipe());
        Lifter.lift(str);
    }

    /**
     * PLAIN-VISITS 
     */
        
    /* visit a filter */
    @Override
	public void visitFilter(SIRFilter self,
                            SIRFilterIter iter) {
    }

    /* visit a phased filter */
    @Override
	public void visitPhasedFilter(SIRPhasedFilter self,
                                  SIRPhasedFilterIter iter) {
    }
  
    /**
     * PRE-VISITS 
     */
        
    /* pre-visit a pipeline */
    @Override
	public void preVisitPipeline(SIRPipeline self,
                                 SIRPipelineIter iter) {
    }

    /* pre-visit a splitjoin */
    @Override
	public void preVisitSplitJoin(SIRSplitJoin self,
                                  SIRSplitJoinIter iter) {
    }

    /* pre-visit a feedbackloop */
    @Override
	public void preVisitFeedbackLoop(SIRFeedbackLoop self,
                                     SIRFeedbackLoopIter iter) {
    }

    /**
     * POST-VISITS
     */
        
    /* post-visit a pipeline */
    @Override
	public void postVisitPipeline(SIRPipeline self,
                                  SIRPipelineIter iter) {
        convertChildren(self);
    }

    /* post-visit a splitjoin */
    @Override
	public void postVisitSplitJoin(SIRSplitJoin self,
                                   SIRSplitJoinIter iter) {
        convertChildren(self);
    }

    /* post-visit a feedbackloop */
    @Override
	public void postVisitFeedbackLoop(SIRFeedbackLoop self,
                                      SIRFeedbackLoopIter iter) {
        convertChildren(self);
    }

    private void convertChildren(SIRContainer cont) {
        for (int i=0; i<cont.size(); i++) {
            SIRStream child = cont.get(i);
            if (child instanceof SIRSplitJoin) {
                cont.replace(child, RefactorSplitJoin.convertToPipeline((SIRSplitJoin)child));
            }
        }
    }
}
