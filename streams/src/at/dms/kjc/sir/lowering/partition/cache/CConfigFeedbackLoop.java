package at.dms.kjc.sir.lowering.partition.cache;

import at.dms.kjc.sir.SIRFeedbackLoop;

class CConfigFeedbackLoop extends CConfigPipeline {

    public CConfigFeedbackLoop(SIRFeedbackLoop loop, CachePartitioner partitioner) {
        super(loop, partitioner);
    }

}
