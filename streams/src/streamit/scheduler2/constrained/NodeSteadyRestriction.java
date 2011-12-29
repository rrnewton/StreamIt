package streamit.scheduler2.constrained;

public class NodeSteadyRestriction extends Restriction
{
    final LatencyNode node;
    
    NodeSteadyRestriction(
                          LatencyNode _node,
                          int numSteadyState,
                          StreamInterface _parent)
    {
        super(
              _node,
              new P2PPortal(
                            true,
                            _node,
                            _node,
                            numSteadyState,
                            numSteadyState,
                            _parent));
                
        node = _node;
    }

    @Override
	public boolean notifyExpired()
    {
        // this restriction should never be removed or unblocked!
        portal.getParent().doneSteadyState(node);
        return false;
    }

    @Override
	public void useRestrictions(Restrictions _restrictions)
    {
        super.useRestrictions(_restrictions);
        setMaxExecutions(portal.getMaxLatency());
    }
}
