package at.dms.kjc.sir.lowering.partition;

import java.util.*;

import at.dms.kjc.*;
import at.dms.util.*;
import at.dms.kjc.sir.*;
import at.dms.kjc.sir.linear.*;
import at.dms.kjc.sir.lowering.*;
import at.dms.kjc.sir.lowering.fusion.*;

/**
 * LinearReplace transform on a stream graph.
 */

public final class LinearReplaceTransform extends StreamTransform {
    /**
     * Linear analyzer used to construct this.
     */
    private LinearAnalyzer lfa;

    public LinearReplaceTransform(LinearAnalyzer lfa) {
	super();
	this.lfa = lfa;
    }

    /**
     * Perform the transform on <str> and return new stream.
     */
    public SIRStream doMyTransform(SIRStream str) {
	LinearDirectReplacer.doReplace(lfa, str);
	// kind of hard to get a handle on the new stream... return
	// null for now; this shouldn't get dereferenced in linear
	// partitioner
	return null;
    }

    public String toString() {
	return "LinearReplace Transform";
    }

}
