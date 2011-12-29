package at.dms.kjc.sir.lowering.partition;


/**
 * Idempotent transform on a stream graph.  An idempotent transform is
 * one that itself does not necessarily need to be performed... not
 * changing any filters in the graph.
 */

public abstract class IdempotentTransform extends StreamTransform {

    /**
     * This is idempotent if preds and succs are idempotent.
     */
    @Override
	protected boolean isIdempotent() {
        boolean ok = true;
        // test preds
        for (int i=0; i<getPredSize(); i++) {
            ok = ok && getPred(i).isIdempotent();
        }
        // test succ's
        for (int i=0; i<getSuccSize(); i++) {
            ok = ok && getSucc(i).isIdempotent();
        }
        return ok;
    }
}
