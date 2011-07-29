package at.dms.kjc.slir;

import java.util.HashMap;

import at.dms.kjc.backendSupport.FilterInfo;

/**
 *  An InterSliceEdge represents an edge in the partitioned stream graph between slices.
 *  But it actually connects {@link OutputNode}s to {@link InputSliceNodes}.
 * 
 * @author mgordon
 *
 */
public class InterFilterEdge extends Edge implements at.dms.kjc.DeepCloneable, Comparable<InterFilterEdge>{
    private static HashMap<EdgeDescriptor, InterFilterEdge> edges =
        new HashMap<EdgeDescriptor, InterFilterEdge>();
    
    /**
     * No argument constructor, FOR AUTOMATIC CLONING ONLY.
     */
    private InterFilterEdge() {
        super();
    }
    
    /**
     * Full constructor, (type will be inferred from src / dest).
     * @param src   Source of directed edge as OutputSliceNode
     * @param dest  Destination of directed edga as InputSliceNode
     */
    public InterFilterEdge(OutputNode src, InputNode dest) {
        super(src,dest);
        
        //make sure we did not create this edge before!
        EdgeDescriptor edgeDscr = new EdgeDescriptor(src, dest);      
        InterFilterEdge edge = edges.get(edgeDscr);
        assert (edge == null) : "trying to create 2 identical edges";
        //remember this edge
        edges.put(edgeDscr, this);
    }

    /**
     * Partial constructor: {@link #setDest(InputNode)} later.
     * @param src 
     */
    public InterFilterEdge(OutputNode src) {
        super();
        this.src = src;        
    }

    /**
     * Partial constructor: {@link #setSrc(OutputNode)} later.
     * @param dest
     */
    public InterFilterEdge(InputNode dest) {
        super();
        this.dest = dest;
    }

    public static InterFilterEdge getEdge(OutputNode src, InputNode dest) {
        EdgeDescriptor edgeDscr = new EdgeDescriptor(src, dest);      

        InterFilterEdge edge = edges.get(edgeDscr);

        return edge;
    }
    
    @Override
    public OutputNode getSrc() {
        
        return (OutputNode)src;
    }

    @Override
    public InputNode getDest() {
        return (InputNode)dest;
    }

    @Override
    public void setSrc(SliceNode src) {
        assert src instanceof OutputNode;
        
        //make sure we did not create this edge before!
        EdgeDescriptor edgeDscr = new EdgeDescriptor((OutputNode)src, getDest());      
        InterFilterEdge edge = edges.get(edgeDscr);
        assert (edge == null) : "trying to create 2 identical edges";
        //remember this edge
        edges.put(edgeDscr, this);
        
        super.setSrc(src);
    }

    @Override
    public void setDest(SliceNode dest) {
        assert dest instanceof InputNode;
        //make sure we did not create this edge before!
        EdgeDescriptor edgeDscr = new EdgeDescriptor(getSrc(), (InputNode)dest);      
        InterFilterEdge edge = edges.get(edgeDscr);
        assert (edge == null) : "trying to create 2 identical edges";
        //remember this edge
        edges.put(edgeDscr, this);
        
        super.setDest(dest);
    }
    /**
     * The number of items that traverse this edge in the initialization
     * stage.
     * 
     * @return The number of items that traverse this edge in the initialization
     * stage. 
     */
    public int initItems() {
        int itemsReceived, itemsSent;

        FilterInfo next = FilterInfo.getFilterInfo((WorkNode) ((InputNode)dest)
                                                   .getNext());
        
        itemsSent = (int) ((double) next.initItemsReceived() * ((InputNode)dest).ratio(this, SchedulingPhase.INIT));
        //System.out.println(next.initItemsReceived()  + " * " + ((InputSliceNode)dest).ratio(this));
        
        // calculate the items the output slice sends
        FilterInfo prev = FilterInfo.getFilterInfo((WorkNode) ((OutputNode)src)
                                                   .getPrevious());
        itemsReceived = (int) ((double) prev.initItemsSent() * ((OutputNode)src).ratio(this, SchedulingPhase.INIT));

        if (itemsSent != itemsReceived) {
            System.out.println("*** Init: Items received != Items Sent!");
            System.out.println(prev + " -> " + next);
            System.out.println("Mult: " + prev.getMult(SchedulingPhase.INIT) + " " +  
                    next.getMult(SchedulingPhase.INIT));
            System.out.println("Push: " + prev.prePush + " " + prev.push);
            System.out.println("Pop: " + next.pop);
            System.out.println("Init items Sent * Ratio: " + prev.initItemsSent() + " * " +
                    ((OutputNode)src).ratio(this, SchedulingPhase.INIT));
            System.out.println("Items Received: " + next.initItemsReceived(true));
            System.out.println("Ratio received: " + ((InputNode)dest).ratio(this, SchedulingPhase.INIT));
            
        }
        
        // see if they are different
        assert (itemsSent == itemsReceived) : "Calculating init stage: items received != items send on buffer: "
            + src + " (" + itemsSent + ") -> (" + itemsReceived + ") "+ dest;

        return itemsSent;
    }

    /**
     * @return The amount of items (not counting typesize) that flows 
     * over this edge in the steady state.
     */
    public int steadyItems() {
        int itemsReceived, itemsSent;

        // calculate the items the input slice receives
        FilterInfo next = FilterInfo.getFilterInfo(((InputNode)dest).getNextFilter());
        itemsSent = (int) ((next.steadyMult * next.pop) * ((double) ((InputNode)dest)
                                                           .getWeight(this, SchedulingPhase.STEADY) / ((InputNode)dest).totalWeights(SchedulingPhase.STEADY)));

        // calculate the items the output slice sends
        FilterInfo prev = FilterInfo.getFilterInfo((WorkNode) ((OutputNode)src)
                                                   .getPrevious());
        itemsReceived = (int) ((prev.steadyMult * prev.push) * ((double) ((OutputNode)src)
                                                                .getWeight(this, SchedulingPhase.STEADY) / ((OutputNode)src).totalWeights(SchedulingPhase.STEADY)));

        assert (itemsSent == itemsReceived) : "Calculating steady state: items received != items sent on buffer "
            + itemsSent + " " + itemsReceived + " " + prev + " " + next;

        return itemsSent;
    }

   /**
    * The number of items sent over this link in one call of the link in the prime
    * pump stage, the link might be used many times in the prime pump stage conceptually 
    * using the rotating buffers.
    * 
    * @return ...
    */
    public int primePumpItems() {
        return (int) ((double) FilterInfo.getFilterInfo(((OutputNode)src).getPrevFilter())
                      .totalItemsSent(SchedulingPhase.PRIMEPUMP) * ((OutputNode)src).ratio(this, SchedulingPhase.STEADY));
    }
    
    /**
     * Compare two intersliceedges based on the number of steady items
     * 
     * @param other the other interslice edge
     * @return -1, 0, 1
     */
    public int compareTo(InterFilterEdge other) {
        if (this.steadyItems() < other.steadyItems())
            return -1;
        else if (this.steadyItems() > other.steadyItems())
            return 1;
        return 0;
    }
    private static class EdgeDescriptor {
        public OutputNode src;
        public InputNode dest;

        public EdgeDescriptor(OutputNode src, InputNode dest) {
            this.src = src;
            this.dest = dest;
        }
        
        public EdgeDescriptor(Filter src, Filter dest) {
            this(src.getOutputNode(), dest.getInputNode());
        }

        public EdgeDescriptor(InterFilterEdge edge) {
            this(edge.getSrc(), edge.getDest());
        }
        
        public boolean equals(Object obj) {
            if(obj instanceof EdgeDescriptor) {
                EdgeDescriptor edge = (EdgeDescriptor)obj;
                
                if(this.src.equals(edge.src) &&
                   this.dest.equals(edge.dest))
                    return true;
                
                return false;
            }
            
            return false;
        }
        
        public int hashCode() {
            return src.hashCode() + dest.hashCode();
        }
    }
}
