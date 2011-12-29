/*
 * Copyright 2003 by the Massachusetts Institute of Technology.
 *
 * Permission to use, copy, modify, and distribute this
 * software and its documentation for any purpose and without
 * fee is hereby granted, provided that the above copyright
 * notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting
 * documentation, and that the name of M.I.T. not be used in
 * advertising or publicity pertaining to distribution of the
 * software without specific, written prior permission.
 * M.I.T. makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is"
 * without express or implied warranty.
 */

package streamit.library.iriter;

import streamit.library.FeedbackLoop;
import streamit.library.NullJoiner;
import streamit.library.NullSplitter;


/**
 * This is the implementation of the FeedbackLoop iterator, as required by
 * scheduler2.
 * 
 * This class will correctly answer all the inquiries about the filter. 
 * All the inquiries are basically relayed back to the underlying 
 * library FeedbackLoop. Some answers are memoised to reduce computation.
 */

public class FeedbackLoopIter
    extends streamit.misc.DestroyedClass
    implements streamit.scheduler2.iriter.FeedbackLoopIter
{
    FeedbackLoopIter (FeedbackLoop _feedback, IterFactory _factory)
    {
        feedback = _feedback;
        factory = _factory;
        feedback.getSplitter().useFL(this);
        feedback.getJoiner().useFL(this);
    }

    FeedbackLoop feedback;
    IterFactory factory;
    
    @Override
	public Object getObject ()
    {
        return feedback;
    }
    
    @Override
	public streamit.scheduler2.iriter.Iterator getUnspecializedIter()
    {
        return new Iterator(feedback, factory);
    }
    
    @Override
	public int getDelaySize()
    {
        return feedback.getDelay();
    }
    
    @Override
	public streamit.scheduler2.iriter.Iterator getBodyChild ()
    {
        return new Iterator (feedback.getBody (), factory);
    }

    @Override
	public streamit.scheduler2.iriter.Iterator getLoopChild ()
    {
        return new Iterator (feedback.getLoop (), factory);
    }

    @Override
	public int getFanOut () { return 2; }

    @Override
	public int getSplitterNumWork ()
    {
        if (feedback.getSplitter() instanceof NullSplitter)
            {
                return 0;
            } else {
                return 1;
            }
    }
    
    @Override
	public Object getSplitterWork(int nWork)
    {
        assert nWork >= 0 && nWork < getSplitterNumWork ();
        return  feedback.getSplitter();
    }
    
    @Override
	public int getJoinerNumWork ()
    {
        if (feedback.getJoiner() instanceof NullJoiner)
            {
                return 0;
            } else {
                return 1;
            }
    }
    
    @Override
	public Object getJoinerWork(int nWork)
    {
        assert nWork >= 0 && nWork < getJoinerNumWork ();
        return  feedback.getJoiner();
    }
    
    @Override
	public int[] getSplitPushWeights (int nWork)
    {
        return feedback.getSplitter ().getWeights ();
    }
    
    @Override
	public int getFanIn () { return 2; }

    @Override
	public int[] getJoinPopWeights (int nWork)
    {
        return feedback.getJoiner ().getWeights ();
    }
    
    @Override
	public int getSplitPop (int nWork)
    {
        return feedback.getSplitter ().getConsumption ();
    }
    
    @Override
	public int getJoinPush (int nWork)
    {
        return feedback.getJoiner ().getProduction ();
    }
    
    @Override
	public boolean equals(Object other)
    {
        if (!(other instanceof FeedbackLoopIter)) return false;
        FeedbackLoopIter otherLoop = (FeedbackLoopIter) other;
        return otherLoop.getObject() == this.getObject();
    }
    
    @Override
	public int hashCode()
    {
        return feedback.hashCode();
    }
}

