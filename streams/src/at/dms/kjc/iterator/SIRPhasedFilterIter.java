package at.dms.kjc.iterator;

import streamit.scheduler2.iriter.FilterIter;
import streamit.scheduler2.iriter.Iterator;
import at.dms.kjc.sir.SIRPhasedFilter;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.StreamVisitor;


/**
 * IterFactory uses this for SIRPhasedFilter.
 *
 * Includes extra methods as appropriate.
 */

public class SIRPhasedFilterIter extends SIRIterator implements FilterIter 
{
    /**
     * The object this iterator points at.
     */
    private SIRPhasedFilter obj;
    
    /**
     * Returns new iterator for <obj> with no parent.
     */
    SIRPhasedFilterIter(IterFactory _factory, SIRPhasedFilter obj) {
        super(_factory);
        this.obj = obj;
    }

    /**
     * Returns new iterator for <obj> in position <pos> of parent <parent>.
     */
    SIRPhasedFilterIter(IterFactory _factory, SIRPhasedFilter obj, SIRIterator parent, int pos) {
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
	public int getNumInitStages() {
        if (obj.getInitPhases() == null) return 0;
        return obj.getInitPhases().length;
    }

    @Override
	public int getInitPeekStage(int phase) {
        return obj.getInitPhases()[phase].getPeekInt();
    }
    
    @Override
	public int getInitPushStage(int phase) {
        return obj.getInitPhases()[phase].getPushInt();
    }

    @Override
	public int getInitPopStage(int phase) {
        return obj.getInitPhases()[phase].getPopInt();
    }

    @Override
	public Object getInitFunctionStage(int phase) {
        return obj.getInitPhases()[phase];
    }

    // In particular, everything from here on down we had better be able
    // to straightforwardly implement.
    @Override
	public int getNumWorkPhases() {
        if (obj.getPhases() == null) return 0;
        return obj.getPhases().length;
    }

    @Override
	public int getPeekPhase(int phase) {
        return obj.getPhases()[phase].getPeekInt();
    }
    
    @Override
	public int getPopPhase(int phase) {
        return obj.getPhases()[phase].getPopInt();
    }

    @Override
	public int getPushPhase(int phase) {
        return obj.getPhases()[phase].getPushInt();
    }
    
    @Override
	public Object getWorkFunctionPhase(int phase) {
        return obj.getPhases()[phase];
    }
    
    @Override
	public void accept(StreamVisitor v) {
        v.visitPhasedFilter(obj, this);
    }

    @Override
	public Iterator getUnspecializedIter() {
        return this;
    }
}
