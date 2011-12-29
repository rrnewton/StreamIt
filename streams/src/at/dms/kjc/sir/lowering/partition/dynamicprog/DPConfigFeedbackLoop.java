package at.dms.kjc.sir.lowering.partition.dynamicprog;

import at.dms.kjc.sir.SIRFeedbackLoop;

class DPConfigFeedbackLoop extends DPConfigContainer {
    private static final int[] WIDTH = { 1, 1 };

    public DPConfigFeedbackLoop(SIRFeedbackLoop cont, DynamicProgPartitioner partitioner) {
        super(cont, partitioner, WIDTH, 2);
    }

    @Override
	protected DPConfig childConfig(int x, int y) {
        assert x==0;
        return partitioner.getConfig(cont.get(y));
    }

}
