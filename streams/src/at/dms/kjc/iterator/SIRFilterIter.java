package at.dms.kjc.iterator; 

import streamit.scheduler2.iriter.FilterIter;
import streamit.scheduler2.iriter.Iterator;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.SIRTwoStageFilter;
import at.dms.kjc.sir.StreamVisitor;

/**
 * IterFactory uses this for SIRFilter.
 *
 * Includes extra methods as appropriate.
 */

public class SIRFilterIter extends SIRIterator implements FilterIter {

    /**
     * The object this iterator points at.
     */
    private SIRFilter obj;

    /**
     * Returns new iterator for <obj> with no parent.
     */
    SIRFilterIter(IterFactory _factory, SIRFilter obj) {
        super(_factory);
        this.obj = obj;
    }

    /**
     * Returns new iterator for <obj> in position <pos> of parent <parent>.
     */
    SIRFilterIter(IterFactory _factory, SIRFilter obj, SIRIterator parent, int pos) {
        super(_factory, parent, pos);
        this.obj = obj;
    }

    @Override
	public FilterIter isFilter() {
        return this;
    }

    /**
     * Return the stream pointed to by this.
     */
    @Override
	public SIRStream getStream() {
        checkValidity();
        return obj;
    }

    @Override
	public int getNumInitStages () {
        if (obj instanceof SIRTwoStageFilter) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
	public int getInitPeekStage (int phase) {
        if (obj instanceof SIRTwoStageFilter) {
            return ((SIRTwoStageFilter)obj).getInitPeekInt();
        } else {
            return -1;
        }
    }

    @Override
	public int getInitPopStage (int phase) {
        if (obj instanceof SIRTwoStageFilter) {
            return ((SIRTwoStageFilter)obj).getInitPopInt();
        } else {
            return -1;
        }
    }

    @Override
	public int getInitPushStage (int phase) {
        if (obj instanceof SIRTwoStageFilter) {
            return ((SIRTwoStageFilter)obj).getInitPushInt();
        } else {
            return -1;
        }
    }

    @Override
	public Object getInitFunctionStage (int phase) {
        if (obj instanceof SIRTwoStageFilter) {
            return ((SIRTwoStageFilter)obj).getInitWork();
        } else {
            return null;
        }
    }
    
    @Override
	public int getNumWorkPhases () {
        return 1;
    }

    /** Get number of peeks for phase. Has never handled the phase input. Now returns estimate if number not available. */
    @Override
	public int getPeekPhase (int phase) {
        return obj.getPeekInt();
        //return obj.getPeekEstimate(); // getPeekInt();
   }

    /** Get number of peeks for phase. Has never handled the phase input. Now returns estimate if number not available. */
    @Override
	public int getPopPhase (int phase) {
        return obj.getPopInt();
        //return obj.getPopEstimate(); // getPopInt();
    }

    /** Get number of peeks for phase. Has never handled the phase input. Now returns estimate if number not available. */
    @Override
	public int getPushPhase (int phase) {
        return obj.getPushInt();
        //return obj.getPushEstimate(); // getPushInt();
    }

    @Override
	public Object getWorkFunctionPhase (int phase) {
        return obj.getWork();
    }

    @Override
	public void accept(StreamVisitor v) {
        v.visitFilter(obj, this);
    }

    /**
     * This function is needed by the scheduler, but isn't useful from
     * the compiler.
     */
    @Override
	public Iterator getUnspecializedIter() {
        return this;
    }
}
