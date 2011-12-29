package at.dms.kjc.lir;

import at.dms.kjc.JExpression;
import at.dms.kjc.SLIRVisitor;
import at.dms.kjc.sir.SIRSplitType;

/**
 * This gives the run-time system information about the splitter for
 * a feedback loop or split/join structure.  This includes the
 * split policy, the number of branches, and the ratios for a round-robin
 * splitter.
 */
public class LIRSetSplitter extends LIRNode 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 4961274589857154718L;

	/**
     * The type of the splitter.
     */
    private SIRSplitType type;
    
    /**
     * The number of items that the splitter pushes to.
     */
    private int ways;
    
    /**
     * For round-robin splitters, the number of items the splitter pushes
     * to each output tape in one execution cycle.
     */
    private int[] weights;
    
    public LIRSetSplitter(JExpression streamContext, SIRSplitType type,
                          int ways, int[] weights)
    {
        super(streamContext);
        this.type = type;
        this.ways = ways;
        if (weights == null)
            this.weights = null;
        else
            {
                this.weights = new int[ways];
                for (int i = 0; i < ways; i++)
                    this.weights[i] = weights[i];
            }
    }

    public SIRSplitType getSplitType()
    {
        return type;
    }
    
    public int getWays()
    {
        return ways;
    }

    public void getWeights(int[] weights)
    {
        if (this.weights != null)
            for (int i = 0; i < ways; i++)
                weights[i] = this.weights[i];
    }
    
    @Override
	public void accept(SLIRVisitor v) 
    {
        int[] weights = null;
        if (this.weights != null)
            {
                weights = new int[ways];
                getWeights(weights);
            }
        v.visitSetSplitter(this, getStreamContext(), type, ways, weights);
    }
}

                           
