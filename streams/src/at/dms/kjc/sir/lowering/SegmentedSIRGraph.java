package at.dms.kjc.sir.lowering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.dms.kjc.JIntLiteral;
import at.dms.kjc.iterator.IterFactory;
import at.dms.kjc.iterator.SIRFeedbackLoopIter;
import at.dms.kjc.iterator.SIRFilterIter;
import at.dms.kjc.iterator.SIRPhasedFilterIter;
import at.dms.kjc.iterator.SIRPipelineIter;
import at.dms.kjc.iterator.SIRSplitJoinIter;
import at.dms.kjc.sir.SIRContainer;
import at.dms.kjc.sir.SIRDummySink;
import at.dms.kjc.sir.SIRDummySource;
import at.dms.kjc.sir.SIRFeedbackLoop;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIROperator;
import at.dms.kjc.sir.SIRPhasedFilter;
import at.dms.kjc.sir.SIRPipeline;
import at.dms.kjc.sir.SIRSplitJoin;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.StreamVisitor;

/**
 * A SegmentedGraph is a structure that contains each of the static subgraphs
 * from a SIRStream with dynamic regions. Each segment has a source and sink.
 * The SegmentedGraph also maintains connectivity and rate information.
 * 
 * @author soule
 * 
 */
public class SegmentedSIRGraph implements StreamVisitor {

	/**
	 * Maps source to sinks to indicate connections between static subgraphs.
	 * Divergent operators will have multiple connections.
	 */
	private Map<SIRStream, List<SIRStream>> connections = null;

	/** An identifier to distinguish pipeline names */
	private int pipelineId = 0;

	/** This is the list of all pipelines created */
	private List<SIRStream> staticSubGraphs = null;

	/** a flag to turn on debugging */
	boolean debug = true;

	/**
	 * Create a new empty SegmentedGraph.
	 */
	public SegmentedSIRGraph() {
		staticSubGraphs = new ArrayList<SIRStream>();
		connections = new HashMap<SIRStream, List<SIRStream>>();
	}

	/**
	 * Add a connection between one ssg to another
	 * @param connections the map of all connections
	 * @param src the source of one connect
	 * @param dst the destination of one connection
	 */
	private void addConnection(Map<SIRStream, List<SIRStream>> connections, SIRStream src, SIRStream dst) {
		System.out.println("SegmentedSIRGraph.addConnection src=" + src + " dst=" + dst);
		if (!connections.containsKey(src)) {
			connections.put(src, new ArrayList<SIRStream>());
		}
		connections.get(src).add(dst);
	}
	
	/**
	 * Add a new static subsection to the graph. Each subsection must be a
	 * pipeline with a source, some number of other filters, and a sink.
	 * 
	 * @param pipeline
	 *            subsection to add
	 * @return void
	 */
	public void addToSegmentedGraph(SIRStream pipeline) {
		staticSubGraphs.add(pipeline);
	}

	private SIRFilter createSink(SIRFilter pred) {
		SIRFilter sink = new SIRDummySink(pred.getOutputType());
		return sink;
	}
	
	private SIRFilter createSource(SIRFilter succ) {
		SIRFilter source = new SIRDummySource(succ.getInputType());
		return source;
	}
	
	/**
	 * Returns the connections between static subsections.
	 * 
	 * @return the connections between static subsections.
	 */
	public Map<SIRStream, List<SIRStream>> getConnections() {
		return connections;
	}

	/**
	 * Sets the connections between static subsections.
	 * 	 
	 */
	public void setConnections(Map<SIRStream, List<SIRStream>> connections) {
		this.connections = connections;
	}
	
	/**
	 * Returns the list of static subsections
	 * 
	 * @return the list of static subsections
	 */
	public List<SIRStream> getStaticSubGraphs() {
		return staticSubGraphs;
	}

	public SegmentedSIRGraph init(SIRStream str, boolean isDynamic) {
		if (!isDynamic) {
			System.out.println("SegmentedSIRGraph.init !Dynamic, no partitioning performed.");
			this.addToSegmentedGraph(str);
			return this;
		}
		//startPipeline();
		IterFactory.createFactory().createIter(str).accept(this);
		//endPipeline();
		System.out.println("SegmentedSIRGraph.init() isDynamic=true, number of partitions=" + staticSubGraphs.size());
		return this;
	}

	private boolean isDynamicPop(SIRFilter second) {
		return second.getPop().isDynamic();
	}

	private boolean isDynamicPush(SIRFilter self) {
		return self.getPush().isDynamic();
	}

	private boolean isCut(SIRFilter sirStream, SIRFilter sirStream2) {		
		if (isDynamicPush(sirStream) || isDynamicPop(sirStream2)) {
			return true;
		}
		if (sirStream.isStateful() || sirStream2.isStateful()) {
			return true;
		}
		return false;
	}

	/**
	 * Remove the hierarchy from the pipeline, and just
	 * return all children in an in-order traversal.
	 * @param pipeline the top level container
	 * @return all of the descendants.
	 */
	private List <SIROperator> getAllChildren(SIRPipeline pipeline) {
		List<SIROperator> allChildren = new ArrayList<SIROperator>();
		List<SIROperator> children = pipeline.getChildren();
		for (SIROperator child : children) {
			if (child instanceof SIRPipeline) {
				allChildren.addAll(getAllChildren((SIRPipeline)child));
			} else {
				// This will add splitjoins and feedback loops as
				// whole components.
				allChildren.add(child);			
			}
		}
		return allChildren;
	}
	
	
	@Override
	public void postVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) { /* Do nothing yet */
	}
	
	
	@Override
	public void postVisitPipeline(SIRPipeline pipeline, SIRPipelineIter iter) {
		// We only want to do this at the top level
		if (pipeline.getParent() != null) {
			return;
		}
		
		System.out.println("BIG TIME! SegmentedSIRGraph.postVisitPipeline called");		
		List <SIROperator> allChildren = getAllChildren(pipeline);	
		String name = uniquePipelineName();	
		SIRPipeline currentPipeline = new SIRPipeline(null, name);
		
		for (int i = 0; i < allChildren.size()-1; i++) {		
									
			// First we want to check for illegal graphs. For now, 
			// we will say that if a component is followed or preceded
			// by a dynamic filter, then the graph is illegal
			if ((allChildren.get(i) instanceof SIRContainer) &&
					(allChildren.get(i+1) instanceof SIRFilter) ) {
				assert !isDynamicPop((SIRFilter)allChildren.get(i+1)) : 
					"Illegal graph! A container cannot be followed by a dynamic pop rate.";					
			}
			
			if ((allChildren.get(i) instanceof SIRFilter) &&
					(allChildren.get(i+1) instanceof SIRContainer) ) {
				assert !isDynamicPush((SIRFilter)allChildren.get(i)) : 
					"Illegal graph! A container cannot be preceded by a dynamic push rate.";					
			}

			
			currentPipeline.add((SIRStream)allChildren.get(i));
			
			if (allChildren.get(i) instanceof SIRContainer) {				
				continue;
			}
			
			if ((allChildren.get(i) instanceof SIRFilter) &&
					(allChildren.get(i+1) instanceof SIRFilter) ) {
				if (isCut((SIRFilter)allChildren.get(i), (SIRFilter)allChildren.get(i+1))) {
					SIRFilter filter = (SIRFilter)allChildren.get(i);
					filter.setPush(new JIntLiteral(1));
					SIRFilter sink = createSink(filter);					
					currentPipeline.add(sink);
					addToSegmentedGraph(currentPipeline);					
					currentPipeline = new SIRPipeline(null, uniquePipelineName());
					SIRFilter next = (SIRFilter)allChildren.get(i+1);
					SIRFilter source = createSource(next);
					addConnection(connections, filter, next);
					currentPipeline.add(source);				
					next.setPop(new JIntLiteral(1));
					next.setPeek(new JIntLiteral(1));										
				}
			}									
		}		
		// add the last filter to the pipeline
		currentPipeline.add((SIRStream) allChildren.get(allChildren.size()-1));
		addToSegmentedGraph(currentPipeline);
	}
	
	@Override
	public void postVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) { 
		
	}

	@Override
	public void preVisitFeedbackLoop(SIRFeedbackLoop self,
			SIRFeedbackLoopIter iter) { 
		
	}

	@Override
	public void preVisitPipeline(SIRPipeline self, SIRPipelineIter iter) { 
		System.out.println("SegmentedSIRGraph.preVisitPipeline called");
	}

	@Override
	public void preVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
		/* Do nothing yet */
	}

	private String uniquePipelineName() {
		pipelineId++;
		return "SSGPipeline" + pipelineId;
	}
	
	private void setDynamic(SIRStream filter, boolean dynamic) {
		filter.setDynamic(dynamic);
		SIRStream parent = filter.getParent();
		while (null != parent) {
			parent.setDynamic(dynamic);
			parent = parent.getParent();
		}
	}
	
	@Override
	public void visitFilter(SIRFilter filter, SIRFilterIter iter) {		
		if (isDynamicPush(filter) || isDynamicPop(filter)) {
			setDynamic(filter, true);
		}

//		SIRFilter filter = (SIRFilter) ObjectDeepCloner.deepCopy(self);
//		boolean isSource = CStdType.Void == filter.getInputType();			
//		boolean isSink = CStdType.Void == filter.getOutputType();			
//				
//		// If this is a completely static filter, then we
//		// just add it to the current pipeline
//		//if (!isDynamicPush(filter) && !(isDynamicPop(filter))) {
//		if (!isDynamicPush(filter) && !(isDynamicPop(filter)) 
//					&& !filter.isStateful()){					
//			pipelineChildren.add(filter);
//			if (isSink) {
//				System.out.println("SegmentedSIRGraph.visitFilter filter =" + filter.getName() + "NotDynamicPush, NotDynamicPop");
//				//endPipeline();
//			}
//		}
//							
//		else if (filter.isStateful()) {						
//			if (isSource) {
//				filter.setPush(new JIntLiteral(1));
//				pipelineChildren.add(filter);
//				pipelineChildren.add(createSink(filter, iter));
//				endPipeline();
//				startPipeline();
//			} else if (isSink) {
//				SIRFilter source = createSource(filter);
//				pipelineChildren.add(source);
//				filter.setPop(new JIntLiteral(1));
//				filter.setPeek(new JIntLiteral(1));
//				pipelineChildren.add(filter);
//				//endPipeline();
//			} else {
//				endPipeline();
//				startPipeline();		
//				SIRFilter source = createSource(filter);
//				pipelineChildren.add(source);
//				filter.setPop(new JIntLiteral(1));
//				filter.setPeek(new JIntLiteral(1));
//				filter.setPush(new JIntLiteral(1));
//				pipelineChildren.add(filter);
//				SIRFilter sink = createSink(filter, iter);
//				pipelineChildren.add(sink);
//				endPipeline();
//				startPipeline();
//			}
//		}
//		
//		else if (isDynamicPush(filter) && !(isDynamicPop(filter))) {
//			filter.setPush(new JIntLiteral(1));
//			pipelineChildren.add(filter);
//			pipelineChildren.add(createSink(filter, iter));
//			endPipeline();
//			startPipeline();
//		}
//
//		else if (!isDynamicPush(filter) && (isDynamicPop(filter))) {
//			endPipeline();
//			startPipeline();
//			pipelineChildren.add(createSource(filter));
//			filter.setPop(new JIntLiteral(1));
//			filter.setPeek(new JIntLiteral(1));
//			pipelineChildren.add(filter);
//		}
//
//		else { // if (isDynamicPush(filter) && (isDynamicPop(filter))) {
//			endPipeline();
//			startPipeline();
//			pipelineChildren.add(createSource(filter));
//			filter.setPop(new JIntLiteral(1));
//			filter.setPeek(new JIntLiteral(1));
//			filter.setPush(new JIntLiteral(1));
//			pipelineChildren.add(filter);
//			pipelineChildren.add(createSink(filter, iter));
//			endPipeline();
//			startPipeline();
//		}
	}

	@Override
	public void visitPhasedFilter(SIRPhasedFilter self, SIRPhasedFilterIter iter) {
		throw new java.lang.RuntimeException(
				"Dynamic rates do not support SIRPhasedFilters.");
	}

	public void visitPipeline(SIRFilter self, SIRFilterIter iter) {		
		System.out.println("SegmentedSIRGraph.visitPipeline called");
	}

}
