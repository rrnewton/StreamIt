package at.dms.kjc.iterator; 

import streamit.scheduler2.iriter.Iterator;
import streamit.scheduler2.iriter.IteratorBase;
import at.dms.kjc.sir.SIRRecursiveStub;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.StreamVisitor;


/**
 * IterFactory uses this for SIRRecursiveStub.
 *
 * Includes extra methods as appropriate.
 */

public class SIRRecursiveStubIter extends SIRIterator implements IteratorBase {

    /**
     * Object pointed to by this iterator.
     */
    private SIRRecursiveStub obj;

    /**
     * Returns new iterator for <obj> with no parent.
     */
    SIRRecursiveStubIter(IterFactory _factory, SIRRecursiveStub obj) {
        super(_factory);
        this.obj = obj;
    }

    /**
     * Returns new iterator for <obj> in position <pos> of parent <parent>.
     */
    SIRRecursiveStubIter(IterFactory _factory, SIRRecursiveStub obj, SIRIterator parent, int pos) {
        super(_factory, parent, pos);
        this.obj = obj;
    }

    /**
     * Return the stream pointed to by this.
     */
    @Override
	public SIRStream getStream() {
        checkValidity();
        return obj;
    }

    /**
     * Do nothing at a RecursiveStub node.
     */
    @Override
	public void accept(StreamVisitor v) {
    }

    /**
     * This function is needed by the scheduler, but isn't useful from
     * the compiler.
     */
    public Iterator getUnspecializedIter() {
        return this;
    }
}
