package at.dms.kjc.sir.lowering.partition.linear;

import at.dms.kjc.sir.SIRSplitJoin;

class LDPConfigSplitJoin extends LDPConfigContainer {

    public LDPConfigSplitJoin(SIRSplitJoin sj, LinearPartitioner partitioner) {
        super(sj, partitioner, wrapInArray(sj.size()), 1);
        assert sj.getRectangularHeight()==1:
            "Require sj's with height of 1 now.";
    }
    
    /**
     * Wraps <pre>i</pre> in a 1-element array
     */
    private static int[] wrapInArray(int i) {
        int[] result = { i };
        return result;
    }

    protected LDPConfig childConfig(int x, int y) {
        assert y==0: "Looking for y=" + y + " in LDPConfigSplitJoin.get";
        return partitioner.getConfig(cont.get(x));
    }
}
