package at.dms.kjc.sir.lowering.partition.dynamicprog;

import at.dms.kjc.sir.SIRSplitJoin;

class DPConfigSplitJoin extends DPConfigContainer {

    public DPConfigSplitJoin(SIRSplitJoin sj, DynamicProgPartitioner partitioner) {
        super(sj, partitioner, wrapInArray(sj.size()), 1);
        assert sj.getRectangularHeight()==1:
            "Require sj's with height of 1 now, but" + sj.getIdent() + " has height of " + sj.getRectangularHeight();
    }
    
    /**
     * Wraps <i> in a 1-element array
     */
    private static int[] wrapInArray(int i) {
        int[] result = { i };
        return result;
    }

    protected DPConfig childConfig(int x, int y) {
        assert y==0: "Looking for y=" + y + " in DPConfigSplitJoin.get";
        return partitioner.getConfig(cont.get(x));
    }
}
