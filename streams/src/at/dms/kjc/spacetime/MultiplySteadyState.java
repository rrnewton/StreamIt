package at.dms.kjc.spacetime;

import at.dms.kjc.slir.*;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashSet;
import at.dms.kjc.*;

public class MultiplySteadyState {
    public static void doit(Filter[] traces) {
        
        assert KjcOptions.steadymult > 0 : "Illegal steadymult argument";
       /*
        for (int i = 0; i < schedule.partitioner.io.length; i++) {
            schedule.partitioner.io[i].getHead().getNextFilter().getFilter()
                .multSteadyMult(KjcOptions.steadymult);
        }
        */
        Iterator<SliceNode> sliceNodes = at.dms.kjc.slir.Util.sliceNodeTraversal(traces);
        while (sliceNodes.hasNext()) {
            SliceNode sliceNode = sliceNodes.next();
            if (sliceNode.isFilterSlice()) {
                ((WorkNode) sliceNode).getFilter().multSteadyMult(KjcOptions.steadymult);
            }
        }

    }
}