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

package streamit.scheduler2.hierarchical;

import streamit.scheduler2.Schedule;
import streamit.scheduler2.base.StreamFactory;
import streamit.scheduler2.iriter.FeedbackLoopIter;

/**
 * This class provides the required functions to implement a schduler
 * for a FeedbackLoop.  Mostly, it simply implements wrappers for functions
 * in StreamInterface and passes them on to the StreamAlgorithm.  This
 * is necessary, 'cause Java doesn't support multiple inheritance.
 * 
 * @version 2
 * @author  Michal Karczmarek
 */

abstract public class FeedbackLoop
    extends streamit.scheduler2.base.FeedbackLoop
    implements StreamInterfaceWithSnJ
{
    final private StreamAlgorithmWithSnJ algorithm =
        new StreamAlgorithmWithSnJ(this);

    final private PhasingSchedule splitPhases[];
    final private PhasingSchedule joinPhases[];

    public FeedbackLoop(FeedbackLoopIter iterator, StreamFactory factory)
    {
        super(iterator, factory);

        // pre-compute the splitter phases:
        {
            splitPhases =
                new PhasingSchedule[feedbackLoop.getSplitterNumWork()];

            int nPhase;
            for (nPhase = 0;
                 nPhase < feedbackLoop.getSplitterNumWork();
                 nPhase++)
                {
                    Schedule sched =
                        new Schedule(
                                     feedbackLoop.getSplitterWork(nPhase),
                                     feedbackLoop.getUnspecializedIter());
                    int pushAmount = feedbackLoop.getSplitPushWeights(nPhase)[0];
                    splitPhases[nPhase] =
                        new PhasingSchedule(this, sched, 0, 0, pushAmount);
                }
        }

        // pre-compute the joiner phases
        {
            joinPhases = new PhasingSchedule[feedbackLoop.getJoinerNumWork()];

            int nPhase;
            for (nPhase = 0;
                 nPhase < feedbackLoop.getJoinerNumWork();
                 nPhase++)
                {
                    Schedule sched =
                        new Schedule(
                                     feedbackLoop.getJoinerWork(nPhase),
                                     feedbackLoop.getUnspecializedIter());
                    int popAmount = feedbackLoop.getJoinPopWeights(nPhase)[0];
                    joinPhases[nPhase] =
                        new PhasingSchedule(this, sched, popAmount, popAmount, 0);
                }
        }
    }

    /**
     * compute the initialization and steady state schedules
     */
    @Override
	abstract public void computeSchedule();

    /**
     * Returns the hierarchical body of a feedback loop.  This function will
     * assert if the body does not implement hierarchical.StreamInterface.
     * @return the hierarchical body of the feedback loop
     */
    protected StreamInterface getHierarchicalBody()
    {
        if (!(getBody() instanceof StreamInterface))
            {
                ERROR("This feedback loop contains a body that is not hierarchical");
            }

        return (StreamInterface) getBody();
    }

    /**
     * Returns the hierarchical feedback path of a feedback loop.  
     * This function will assert if the body does not implement 
     * hierarchical.StreamInterface.
     * @return the hierarchical feedback path of the feedback loop
     */
    protected StreamInterface getHierarchicalLoop()
    {
        if (!(getLoop() instanceof StreamInterface))
            {
                ERROR("This feedback loop contains a feedback path that is not hierarchical");
            }

        return (StreamInterface) getLoop();
    }

    @Override
	public int getNumSplitPhases()
    {
        return feedbackLoop.getSplitterNumWork();
    }

    @Override
	public PhasingSchedule getSplitPhase(int nPhase)
    {
        assert nPhase >= 0 && nPhase < getNumSplitPhases();
        return splitPhases[nPhase];
    }

    @Override
	public int getNumJoinPhases()
    {
        return feedbackLoop.getJoinerNumWork();
    }

    @Override
	public PhasingSchedule getJoinPhase(int nPhase)
    {
        assert nPhase >= 0 && nPhase < getNumJoinPhases();
        return joinPhases[nPhase];
    }

    @Override
	public PhasingSchedule getSplitterPhases(int nPhases)
    {
        return algorithm.getSplitterPhases(nPhases);
    }
    
    @Override
	public PhasingSchedule getJoinerPhases(int nPhases)
    {
        return algorithm.getJoinerPhases(nPhases);
    }
    
    @Override
	public streamit.scheduler2.base.StreamInterface getTop()
    {
        return this;
    }

    @Override
	public streamit.scheduler2.base.StreamInterface getBottom()
    {
        return this;
    }

    // These functions implement wrappers for StreamAlgorithm
    // I have to use this stupid style of coding to accomodate
    // Java with its lack of multiple inheritance

    @Override
	public int getInitPeek()
    {
        return algorithm.getInitPeek();
    }

    @Override
	public int getInitPop()
    {
        return algorithm.getInitPop();
    }

    @Override
	public int getInitPush()
    {
        return algorithm.getInitPush();
    }

    @Override
	public int getNumInitStages()
    {
        return algorithm.getNumInitStages();
    }

    @Override
	public int getInitStageNumPeek(int stage)
    {
        return algorithm.getInitStageNumPeek(stage);
    }

    @Override
	public int getInitStageNumPop(int stage)
    {
        return algorithm.getInitStageNumPop(stage);
    }

    @Override
	public int getInitStageNumPush(int stage)
    {
        return algorithm.getInitStageNumPush(stage);
    }

    @Override
	public PhasingSchedule getInitScheduleStage(int stage)
    {
        return algorithm.getInitScheduleStage(stage);
    }

    @Override
	public PhasingSchedule getPhasingInitSchedule()
    {
        return algorithm.getPhasingInitSchedule();
    }

    @Override
	public Schedule getInitSchedule()
    {
        return algorithm.getInitSchedule();
    }

    public void addInitScheduleStage(PhasingSchedule newStage)
    {
        algorithm.addInitScheduleStage(newStage);
    }

    @Override
	public int getNumSteadyPhases()
    {
        return algorithm.getNumSteadyPhases();
    }

    @Override
	public int getSteadyPhaseNumPeek(int phase)
    {
        return algorithm.getSteadyPhaseNumPeek(phase);
    }

    @Override
	public int getSteadyPhaseNumPop(int phase)
    {
        return algorithm.getSteadyPhaseNumPop(phase);
    }

    @Override
	public int getSteadyPhaseNumPush(int phase)
    {
        return algorithm.getSteadyPhaseNumPush(phase);
    }

    @Override
	public PhasingSchedule getSteadySchedulePhase(int phase)
    {
        return algorithm.getSteadySchedulePhase(phase);
    }

    @Override
	public PhasingSchedule getPhasingSteadySchedule()
    {
        return algorithm.getPhasingSteadySchedule();
    }

    @Override
	public Schedule getSteadySchedule()
    {
        return algorithm.getSteadySchedule();
    }

    @Override
	public void addSteadySchedulePhase(PhasingSchedule newPhase)
    {
        algorithm.addSteadySchedulePhase(newPhase);
    }

    public void advanceChildInitSchedule(StreamInterface child)
    {
        algorithm.advanceChildInitSchedule(child, 1);
    }

    public void advanceChildInitSchedule(
                                         StreamInterface child,
                                         int numStages)
    {
        algorithm.advanceChildInitSchedule(child, numStages);
    }

    public void advanceChildSteadySchedule(StreamInterface child)
    {
        algorithm.advanceChildSteadySchedule(child, 1);
    }

    public void advanceChildSteadySchedule(
                                           StreamInterface child,
                                           int numPhases)
    {
        algorithm.advanceChildSteadySchedule(child, numPhases);
    }

    public PhasingSchedule getChildInitStage(
                                             StreamInterface child,
                                             int nStage)
    {
        return algorithm.getChildInitStage(child, nStage);
    }

    public PhasingSchedule getChildNextInitStage(StreamInterface child)
    {
        return algorithm.getChildInitStage(child, 0);
    }
    public PhasingSchedule getChildSteadyPhase(
                                               StreamInterface child,
                                               int nPhase)
    {
        return algorithm.getChildSteadyPhase(child, nPhase);
    }

    public PhasingSchedule getChildNextSteadyPhase(StreamInterface child)
    {
        return algorithm.getChildSteadyPhase(child, 0);
    }

    // These functions implement wrappers for StreamAlgorithmWithSnJ
    // I have to use this stupid style of coding to accomodate
    // Java with its lack of multiple inheritance

    public void advanceSplitSchedule(int numPhases)
    {
        algorithm.advanceSplitSchedule(numPhases);
    }

    public void advanceSplitSchedule()
    {
        algorithm.advanceSplitSchedule(1);
    }

    public void advanceJoinSchedule(int numPhases)
    {
        algorithm.advanceJoinSchedule(numPhases);
    }

    public void advanceJoinSchedule()
    {
        algorithm.advanceJoinSchedule(1);
    }

    public PhasingSchedule getSplitSteadyPhase(int nPhase)
    {
        return algorithm.getSplitSteadyPhase(nPhase);
    }
    
    public PhasingSchedule getNextSplitSteadyPhase ()
    {
        return getSplitSteadyPhase (0);
    }

    public PhasingSchedule getJoinSteadyPhase(int nPhase)
    {
        return algorithm.getJoinSteadyPhase(nPhase);
    }
    
    public PhasingSchedule getNextJoinSteadyPhase ()
    {
        return getJoinSteadyPhase (0);
    }

    @Override
	public SplitFlow getSplitSteadyPhaseFlow (int nPhase)
    {
        return algorithm.getSplitSteadyPhaseFlow (nPhase);
    }
    
    public SplitFlow getNextSplitSteadyPhaseFlow ()
    {
        return getSplitSteadyPhaseFlow (0);
    }
    
    @Override
	public JoinFlow getJoinSteadyPhaseFlow (int nPhase)
    {
        return algorithm.getJoinSteadyPhaseFlow (nPhase);
    }

    public JoinFlow getNextJoinSteadyPhaseFlow ()
    {
        return getJoinSteadyPhaseFlow (0);
    }
    
    public PhasingSchedule getChildPhases(StreamInterface child, int nPhases)
    {
        return algorithm.getChildPhases(child, nPhases);
    }
}
