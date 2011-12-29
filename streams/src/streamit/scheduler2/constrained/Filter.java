/*
 * Copyright 2003 by the Massachusetts Institute of Technology.
 * 
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose and without fee is hereby granted, provided
 * that the above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation, and that the name of M.I.T. not be used in advertising or
 * publicity pertaining to distribution of the software without specific,
 * written prior permission. M.I.T. makes no representations about the
 * suitability of this software for any purpose. It is provided "as is" without
 * express or implied warranty.
 */

package streamit.scheduler2.constrained;

import streamit.scheduler2.Schedule;
import streamit.scheduler2.hierarchical.PhasingSchedule;
import streamit.scheduler2.iriter.FilterIter;
import streamit.scheduler2.iriter.Iterator;

public class Filter
    extends streamit.scheduler2.hierarchical.Filter
    implements StreamInterface
{
    final private LatencyGraph graph;

    private LatencyNode latencyNode;

    PhasingSchedule initWorkPhases[];
    PhasingSchedule steadyWorkPhases[];

    int nCurrentPhase = 0;

    public Filter(
                  FilterIter filterIter,
                  Iterator parent,
                  StreamFactory factory)
    {
        super(filterIter);

        graph = factory.getLatencyGraph();

        initWorkPhases = new PhasingSchedule[filterIter.getNumInitStages()];
        steadyWorkPhases =
            new PhasingSchedule[filterIter.getNumWorkPhases()];

        for (int n = 0; n < filterIter.getNumInitStages(); n++)
            {
                initWorkPhases[n] =
                    new PhasingSchedule(
                                        this,
                                        new Schedule(
                                                     filterIter.getInitFunctionStage(n),
                                                     filterIter.getUnspecializedIter()),
                                        filterIter.getInitPeekStage(n),
                                        filterIter.getInitPopStage(n),
                                        filterIter.getInitPushStage(n));

            }

        for (int n = 0; n < filterIter.getNumWorkPhases(); n++)
            {
                steadyWorkPhases[n] =
                    new PhasingSchedule(
                                        this,
                                        new Schedule(
                                                     filterIter.getWorkFunctionPhase(n),
                                                     filterIter.getUnspecializedIter()),
                                        filterIter.getPeekPhase(n),
                                        filterIter.getPopPhase(n),
                                        filterIter.getPushPhase(n));
            }

    }

    @Override
	public StreamInterface getTopConstrainedStream()
    {
        return this;
    }

    @Override
	public StreamInterface getBottomConstrainedStream()
    {
        return this;
    }

    @Override
	public void initiateConstrained()
    {
        latencyNode = graph.addFilter(this);

        // create a schedule for this
    }

    @Override
	public void computeSchedule()
    {
        ERROR("Should not be used for Filters in Constrained Scheduling!");
    }

    @Override
	public void registerConstraint(P2PPortal portal)
    {
        ERROR ("not implemented");
    }
    
    @Override
	public LatencyNode getBottomLatencyNode()
    {
        return latencyNode;
    }

    @Override
	public LatencyNode getTopLatencyNode()
    {
        return latencyNode;
    }

    public LatencyNode getLatencyNode()
    {
        return latencyNode;
    }

    PhasingSchedule getPhaseSchedule(int nPhase)
    {
        if (nPhase < this.getNumDeclaredInitPhases())
            {
                return initWorkPhases[nPhase];
            }
        else
            {
                return steadyWorkPhases[(
                                         nPhase - this.getNumDeclaredInitPhases())
                                        % this.getNumDeclaredSteadyPhases()];
            }
    }

    @Override
	public PhasingSchedule getNextPhase(
                                        Restrictions restrictions,
                                        int nDataAvailable)
    {
        PhasingSchedule phase = new PhasingSchedule(this);
        boolean noMoreData = false;

        while (!noMoreData
               && restrictions.getBlockingRestriction(getLatencyNode()) == null)
            {
                Restriction strongestRestriction =
                    restrictions.getStrongestRestriction(getLatencyNode());

                int nAllowedPhases =
                    (strongestRestriction != null
                     ? strongestRestriction.getNumAllowedExecutions()
                     : -1);

                // BUGBUG this can DEFINITELY be a LOT more efficient!
                int nExecutions = 0;
                while (strongestRestriction == null
                       || nAllowedPhases > nExecutions)
                    {
                        PhasingSchedule schedPhase =
                            getPhaseSchedule(nCurrentPhase);
                        if (schedPhase.getOverallPeek() > nDataAvailable)
                            {
                                noMoreData = true;
                                break;
                            }

                        phase.appendPhase(schedPhase);
                        nDataAvailable -= schedPhase.getOverallPop();
                        nCurrentPhase++;
                        nExecutions++;
                    }

                int executed =
                    restrictions.execute(getLatencyNode(), nExecutions);
                assert executed == nExecutions;
            }
        
        return phase;
    }

    @Override
	public boolean isDoneInitializing()
    {
        return nCurrentPhase >= getNumInitStages();
    }

    @Override
	public void initRestrictionsCompleted(P2PPortal portal)
    {
        ERROR("not implemented!");
    }

    Restrictions restrictions = null;

    @Override
	public void initializeRestrictions(Restrictions _restrictions)
    {
        restrictions = _restrictions;
        // I may need to execute my initialization phases!
        if (getNumInitStages() > 0)
            {
                // Yep, I have initialization phases to consider here!
                FilterInitRestriction restriction =
                    new FilterInitRestriction(this);
                restrictions.add(restriction);
            }
    }

    boolean isFilterDoneSteadyState = false;

    @Override
	public void createSteadyStateRestrictions(int streamNumExecs)
    {
        NodeSteadyRestriction restriction =
            new NodeSteadyRestriction(
                                      getLatencyNode(),
                                      streamNumExecs,
                                      this);
        restrictions.add(restriction);
    }

    @Override
	public void doneSteadyState(LatencyNode node)
    {
        assert node == getLatencyNode();
        isFilterDoneSteadyState = true;
    }

    @Override
	public boolean isDoneSteadyState()
    {
        return isFilterDoneSteadyState;
    }

    @Override
	public void registerNewlyBlockedSteadyRestriction(Restriction restriction)
    {
        // this should only happen if a filter sends a msg to itself!
        ERROR("not implemented");
    }
}
