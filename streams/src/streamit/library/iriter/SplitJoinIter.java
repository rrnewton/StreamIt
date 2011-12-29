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

import streamit.library.NullJoiner;
import streamit.library.NullSplitter;
import streamit.library.SplitJoin;
import streamit.misc.Pair;

/**
 * This is the implementation of the SplitJoin iterator, as required by
 * scheduler2.
 * 
 * This class will correctly answer all the inquiries about the filter. 
 * All the inquiries are basically relayed back to the underlying 
 * library SplitJoin. Some answers are memoised to reduce computation.
 */

public class SplitJoinIter
    extends streamit.misc.DestroyedClass
    implements streamit.scheduler2.iriter.SplitJoinIter
{
    public static boolean fineGrained = false;

    final boolean splitterFineGrained;

    SplitJoinIter(SplitJoin _splitjoin, IterFactory _factory)
    {
        splitjoin = _splitjoin;
        factory = _factory;

        if (splitjoin.getSplitter()
            instanceof streamit.library.DuplicateSplitter)
            {
                splitterFineGrained = false;
            }
        else
            {
                splitterFineGrained = fineGrained;
            }
        
        splitjoin.getSplitter().useSJ(this);
        splitjoin.getJoiner().useSJ(this);
    }

    SplitJoin splitjoin;
    IterFactory factory;
    Pair[] splitWorks = null;
    Pair[] joinWorks = null;

    @Override
	public Object getObject()
    {
        return splitjoin;
    }

    @Override
	public streamit.scheduler2.iriter.Iterator getUnspecializedIter()
    {
        return new Iterator(splitjoin, factory);
    }

    @Override
	public int getNumChildren()
    {
        return splitjoin.getNumChildren();
    }

    @Override
	public streamit.scheduler2.iriter.Iterator getChild(int n)
    {
        return new Iterator(splitjoin.getChildN(n), factory);
    }

    @Override
	public int getFanOut()
    {
        return getNumChildren();
    }

    @Override
	public int getSplitterNumWork()
    {
        if (splitjoin.getSplitter() instanceof NullSplitter || splitjoin.getSplitter().getConsumption() == 0)
            {
                return 0;
            }
        else
            {
                if (splitterFineGrained)
                    {
                        return splitjoin.getSplitter().getConsumption();
                    }
                else
                    return 1;
            }
    }

    @Override
	public Object getSplitterWork(int nWork)
    {
        assert nWork >= 0 && nWork < getSplitterNumWork();

        return splitjoin.getSplitter().getWork(nWork);
    }

    @Override
	public int getJoinerNumWork()
    {
        if (splitjoin.getJoiner() instanceof NullJoiner || splitjoin.getJoiner().getProduction() == 0)
            {
                return 0;
            }
        else
            {
                if (fineGrained)
                    {
                        return splitjoin.getJoiner().getProduction();
                    }
                else
                    return 1;
            }
    }

    @Override
	public Object getJoinerWork(int nWork)
    {
        assert nWork >= 0 && nWork < getJoinerNumWork();

        return splitjoin.getJoiner().getWork(nWork);
    }

    int splitWeights[] = null, joinWeights[] = null;
    int fineSplitPushWeights[][], fineJoinPushWeights[][];

    @Override
	public int[] getSplitPushWeights(int nWork)
    {
        if (splitWeights == null)
            {
                splitWeights = splitjoin.getSplitter().getWeights();

                fineSplitPushWeights =
                    new int[getSplitterNumWork()][getFanOut()];

                int n = 0, m = 0;
                for (int total = 0; total < getSplitterNumWork(); total++)
                    {
                        while (n >= splitWeights[m])
                            {
                                n = 0;
                                m++;
                            }
                        fineSplitPushWeights[total][m] = 1;
                        n++;
                    }
            }

        if (splitterFineGrained)
            return fineSplitPushWeights[nWork];
        else
            return splitWeights;
    }

    @Override
	public int getFanIn()
    {
        return getNumChildren();
    }

    @Override
	public int[] getJoinPopWeights(int nWork)
    {
        if (joinWeights == null)
            {
                joinWeights = splitjoin.getJoiner().getWeights();

                fineJoinPushWeights = new int[getJoinerNumWork()][getFanIn()];

                int n = 0, m = 0;
                for (int total = 0; total < getJoinerNumWork(); total++)
                    {
                        while (n >= joinWeights[m])
                            {
                                n = 0;
                                m++;
                            }
                        fineJoinPushWeights[total][m] = 1;
                        n++;
                    }
            }

        if (fineGrained)
            return fineJoinPushWeights[nWork];
        else
            return joinWeights;
    }

    @Override
	public int getSplitPop(int nWork)
    {
        int throughput = splitjoin.getSplitter().getConsumption();
        if (splitterFineGrained && throughput != 0)
            return 1;
        else
            return throughput;
    }

    @Override
	public int getJoinPush(int nWork)
    {
        int throughput  = splitjoin.getJoiner().getProduction();
        
        if (fineGrained && throughput != 0)
            return 1;
        else
            return throughput;
    }

    @Override
	public boolean equals(Object other)
    {
        if (!(other instanceof SplitJoinIter))
            return false;
        SplitJoinIter otherSJ = (SplitJoinIter)other;
        return otherSJ.getObject() == this.getObject();
    }

    @Override
	public int hashCode()
    {
        return splitjoin.hashCode();
    }
}
