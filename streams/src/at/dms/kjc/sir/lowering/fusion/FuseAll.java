package at.dms.kjc.sir.lowering.fusion;

import at.dms.kjc.*;
import at.dms.kjc.sir.*;
import at.dms.kjc.iterator.*;
import at.dms.kjc.sir.lowering.*;
import at.dms.kjc.lir.*;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * This class fuses all the pipelines that it can in a stream graph.
 * We need to fuse all the children of the current stream we're
 * visiting (rather than the stream itself) so that the iterators
 * don't get confused.
 */
public class FuseAll implements StreamVisitor {
    private FuseAll() {}

    /**
     * Fuse everything we can in <str>
     */
    public static void fuse(SIRStream str) {
	// try fusing toplevel separately since noone contains it
	FuseAll fuseAll = new FuseAll();
	boolean hasFused = true;
	while (hasFused) {
	    try {
		IterFactory.createIter(str).accept(fuseAll);
		hasFused = false;
	    } catch (SuccessfulFuseException e) {}
	}
    }

    /**
     * PLAIN-VISITS 
     */
	    
    /* visit a filter */
    public void visitFilter(SIRFilter self,
			    SIRFilterIter iter) {
    }
  
    /**
     * PRE-VISITS 
     */
	    
    /* pre-visit a pipeline */
    public void preVisitPipeline(SIRPipeline self,
				 SIRPipelineIter iter) {
    }

    /* pre-visit a splitjoin */
    public void preVisitSplitJoin(SIRSplitJoin self,
				  SIRSplitJoinIter iter) {
    }

    /* pre-visit a feedbackloop */
    public void preVisitFeedbackLoop(SIRFeedbackLoop self,
				     SIRFeedbackLoopIter iter) {
    }

    /**
     * POST-VISITS 
     */
	    
    /* post-visit a pipeline */
    public void postVisitPipeline(SIRPipeline self,
				  SIRPipelineIter iter) {
	int elim = FusePipe.fuse(self);
	if (elim > 0) {
	    throw new SuccessfulFuseException();
	}
    }

    /* post-visit a splitjoin */
    public void postVisitSplitJoin(SIRSplitJoin self,
				   SIRSplitJoinIter iter) {
	SIRStream result = FuseSplit.fuse(self);
	if (result!=self) {
	    throw new SuccessfulFuseException();
	}
    }

    /* post-visit a feedbackloop */
    public void postVisitFeedbackLoop(SIRFeedbackLoop self,
				      SIRFeedbackLoopIter iter) {
    }
}

/**
 * This exists only for the sake of efficiency, to do a long jump back
 * up to the top of the fusion loop.  For some reason we only get
 * maximal fusion if we always consider fusing things from the very
 * top; fusing within the visitor doesn't quite do the right thing.
 */
class SuccessfulFuseException extends RuntimeException {
    public SuccessfulFuseException() { super(); }
    public SuccessfulFuseException(String str) { super(str); }
}
