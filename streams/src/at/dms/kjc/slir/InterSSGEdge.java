package at.dms.kjc.slir;

import at.dms.kjc.CType;

/**
 * The InterSSGEdge class represents a link between two SSG's. The Rate information
 * stores the (possibly dynamic) rate information of the source and destination.
 *
 * @author mgordon
 *
 */


public class InterSSGEdge extends Edge<OutputPort, InputPort> {

	/** defined on the source OutputPort */
	private Rate pushRate;

	/** defined on the destination InputPort */
	private Rate popRate;

	/** defined on the destination InputPort */
	private Rate peekRate;

	/** Create a new Link */
	public InterSSGEdge(OutputPort src, InputPort dst) {
		super(src, dst);
		System.out.println("InterSSGEdge(src,dst)");		
	}

	@Override
	public String toString() {
		Filter[] inputFilterGraph = src.getSSG().getFilterGraph();
		Filter[] outputFilterGraph = dst.getSSG().getFilterGraph();
		WorkNodeContent srcContent = inputFilterGraph[0].getWorkNodeContent();
		WorkNodeContent dstContent = outputFilterGraph[outputFilterGraph.length - 1].getWorkNodeContent();		
		return "InterSSGEdge " +  srcContent + " -> " + dstContent + " (type=" + getType() + ")";		
	}

	public Rate getPushRate() {
		return pushRate;
	}

	public void setPushRate(Rate pushRate) {
		this.pushRate = pushRate;
	}

	public Rate getPopRate() {
		return popRate;
	}

	public void setPopRate(Rate popRate) {
		this.popRate = popRate;
	}

	public Rate getPeekRate() {
		return peekRate;
	}

	public void setPeekRate(Rate peekRate) {
		this.peekRate = peekRate;
	}

	/* (non-Javadoc)
	 * @see at.dms.kjc.slir.Edge#getType()
	 */
	@Override
	public CType getType() {
		if (type != null) {
			return type;
		}

		Filter[] inputFilterGraph = src.getSSG().getFilterGraph();
		Filter[] outputFilterGraph = dst.getSSG().getFilterGraph();
		WorkNodeContent srcContent = inputFilterGraph[inputFilterGraph.length - 1].getWorkNodeContent();
		WorkNodeContent dstContent = outputFilterGraph[0].getWorkNodeContent();
		CType srcType = srcContent.getOutputType();
		CType dstType = dstContent.getInputType();
		type = srcType;
		
		System.out.println( "InterSSGEdge.getType() calculating type: " + 
				srcContent + " -> " + dstContent + "has type=" + type);		
		
		assert srcType.equals(dstType) : "InterSSGEdge.getType() Error calculating type: " + 
		srcContent + "(" + srcType  + ") -> " + dstContent + "(" + dstType  + ")";
		return type;

	}



}
