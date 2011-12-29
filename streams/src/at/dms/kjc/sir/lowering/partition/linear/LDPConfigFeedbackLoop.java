package at.dms.kjc.sir.lowering.partition.linear;

import at.dms.kjc.sir.SIRFeedbackLoop;

class LDPConfigFeedbackLoop extends LDPConfigContainer {
    private static final int[] WIDTH = { 1, 1 };

    public LDPConfigFeedbackLoop(SIRFeedbackLoop cont, LinearPartitioner partitioner) {
        super(cont, partitioner, WIDTH, 2);
    }

    @Override
	protected LDPConfig childConfig(int x, int y) {
        assert x==0;
        return partitioner.getConfig(cont.get(y));
    }

}
