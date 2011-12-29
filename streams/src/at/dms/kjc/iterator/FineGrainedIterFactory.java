package at.dms.kjc.iterator;

import at.dms.kjc.sir.SIRFeedbackLoop;
import at.dms.kjc.sir.SIRSplitJoin;

/**
 * This class is a fine-grained iterator factory.  
 * 
 * The difference 
 * between it and IterFactory is that it provides a separate phase for
 * each item that is passed through a roundrobin splitter or joiner.
 */

public class FineGrainedIterFactory extends IterFactory {

    /**
     * Constructor should stay protected so that it's only returned
     * from static methods in this package.
     */
    FineGrainedIterFactory() {}

    @Override
	public SIRSplitJoinIter createIter(SIRSplitJoin obj) {
        return new SIRFineGrainedSplitJoinIter(this, obj);
    }

    @Override
	public SIRFeedbackLoopIter createIter(SIRFeedbackLoop obj) {
        return new SIRFineGrainedFeedbackLoopIter(this, obj);
    }

    @Override
	SIRSplitJoinIter createIter(SIRSplitJoin obj, SIRIterator parent, int pos) {
        return new SIRFineGrainedSplitJoinIter(this, obj, parent, pos);
    }

    @Override
	SIRFeedbackLoopIter createIter(SIRFeedbackLoop obj, SIRIterator parent, int pos) {
        return new SIRFineGrainedFeedbackLoopIter(this, obj, parent, pos);
    }

}
