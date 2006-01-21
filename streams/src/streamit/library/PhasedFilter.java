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

package streamit.library;

import streamit.scheduler2.Scheduler;

// Last edited by Matthew Drake

/** 
 * A PhasedFilter is designed so that one phase executes per work execution.
 * In practice, this means that a work cycle ends immediately following the
 * execution of any phase. This phase could have been called within the work() 
 * body. PhasedFilters run their work function in their own thread, and implementations
 * of PhasedFilters are expected to call contextSwitch() after a phase execution.
 */

public abstract class PhasedFilter extends Filter implements Runnable
{
    public PhasedFilter() { super(); }
    public PhasedFilter(int a) { super(a); }

    private boolean firstWork = true;
    /**
     * A variable to transfer exceptions between threads.  (We have
     * two threads, and the one that runs the actual work function is
     * different than the one called by the scheduler.  So we have to
     * stand on our head to move the exception to the caller).
     */
    private RuntimeException pendingException = null;

    public void doWork() {
	synchronized (this) {
	    try {
		if (firstWork) {
		    firstWork = false;
		    Thread t = new Thread(this);
		    t.start();
		    wait();
		} else {
		    notify();
		    wait();
		}
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    // if a pending exception was found, raise it in this thread
	    if (pendingException != null) {
		// clear the exception
		RuntimeException e = pendingException;
		pendingException = null;
		// throw the exception
		throw e;
	    }
	}
    }

    public void run() {
	while (true) {
	    try {
		prepareToWork();
		work();
		cleanupWork();
	    } catch (NoPushPopException e) {
		// the exception is caught by the other thread, so
		// move it through a local variable so the other
		// thread will see it.
		pendingException = e;
		try {
		    synchronized(this) {
			notify();
			wait();
		    }
		} catch (InterruptedException ie) {
		    ie.printStackTrace();
		}
	    }
	}
    }

    /**
     * contextSwitch is called so that the PhasedFilter's work loop can pause
     * and end a work cycle.
     */
    protected void contextSwitch() {
        synchronized (this) {
            try {
		numExecutions++;
                notify();
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Placeholder so that java input to Kopi will compile.
     */
    public void phase(WorkFunction f) {
    }
}



