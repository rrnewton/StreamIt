package at.dms.kjc.slir;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import at.dms.kjc.CType;

/**
 * Each slice is terminated by an OutputSliceNode that has single input (the last filter) 
 * and multiple outputs (to downstream slices through edges). There is a possibility that 
 * an output slice node could have a different schedule for the initialization stage. 
 * This is not the common case, so most methods assume there is not a separate schedule and use the 
 * single (steady and init) weights/dests.
 * 
 * @author mgordon
 */
public class OutputNode extends InternalFilterNode implements at.dms.kjc.DeepCloneable {
    public static final String[] DO_NOT_CLONE_THESE_FIELDS = 
        { "weights", "dests", "initWeights", "initDests" };
    
    /** the (round-robin) weight for each edge used for the steady and for init if this
     * node does not have a separate init pattern.
     */
    private int[] weights;
    /** Ordered array of sets of edges
     * The order in the outer array corresponds to the order of weights.
     * The inner array is just a set: the elements correspond to 
     * elements of duplicate splitters fused with the top-level
     * round-robin splitter (by synch removal).
     * A round-robin splitter of size n would be an Edge[n][1]
     * A duplicate splitter of size n would be an Edge[1][n]
     */
    private InterFilterEdge[][] dests;
    /** the weights for init if this node requires a different init splitting pattern */
    private int[] initWeights;
    /** the dest array for init if this node requires a differnt init splitting pattern */
    private InterFilterEdge[][] initDests; 
        /** unique identifier for this node */
    private String ident;
    /** used to generate unique id */
    private static int unique = 0;
    /** used to initialize the weights array */
    private static int[] EMPTY_WEIGHTS = new int[0];
    /** used to initialize the weights array */
    private static InterFilterEdge[][] EMPTY_DESTS = new InterFilterEdge[0][0];

    
    /**
     * Construct a new output slice node based on the arrays weights
     * and dests.
     * 
     * @param weights The array of weights
     * @param dests The array of dests.
     */

    public OutputNode(int[] weights, InterFilterEdge[][] dests) {
        // this.parent = parent;
        assert weights.length == dests.length : "weights must equal sources";
        ident = "output" + unique;
        unique++;
        if (weights.length == 1)
            this.weights = new int[]{1};
        else 
            this.weights = weights;
        this.dests = dests;
    }

    /**
     * Construct a new output slice node based on the lists weights
     * and dests.
     * 
     * @param weights The list of weights
     * @param dests The list of dests.
     */
    public OutputNode(LinkedList<Integer> weights, 
            LinkedList<LinkedList<InterFilterEdge>> dests) {
        assert weights.size() == dests.size();
        ident = "output" + unique++;
        //convert the weights list
        set(weights, dests, SchedulingPhase.STEADY);
    }
    
    
    /**
     * Construct a new output slice node based on the array weights.
     * Dests is to be set later.
     * 
     * @param weights The array of weights
     */
    public OutputNode(int[] weights) {
        ident = "output" + unique;
        unique++;
        if (weights.length == 1)
            this.weights = new int[]{1};
        else 
            this.weights = weights;
        dests = EMPTY_DESTS;
    }

    /**
     * Construct a nre output slice node.
     * Weights and dests to be st later.
     *
     */
    public OutputNode() {
        ident = "output" + unique;
        unique++;
        weights = EMPTY_WEIGHTS;
        dests = EMPTY_DESTS;
    }
    
    /** Set the weights for the steady state (and for init if this
     * node does not require a different pattern for init) */
    public void setWeights(int[] newW) {
        this.weights = newW;
    }

    /** Set the weights for the init stage, this means that init will 
     * have a splitting pattern that is different from steady 
     * */
    public void setInitWeights(int[] newW) {
        this.initWeights = newW;
    }
    
    /**
     * Set the steady weights and dests of this input slice node to 
     * weights and dests.
     * 
     * @param weights List of integer weights.
     * @param dests List of Lists of Edge for splitting pattern.
     */
    public void set(LinkedList<Integer> weights, 
            LinkedList<LinkedList<InterFilterEdge>> dests, SchedulingPhase phase) {
        int[] newWeights;
        InterFilterEdge[][] newDests;
        
        if (weights.size() == 1) 
            newWeights = new int[]{1};
        else {
            newWeights = new int[weights.size()];
            for (int i = 0; i < weights.size(); i++)
                newWeights[i] = weights.get(i).intValue();
        }

        //convert the dests list
        int i = 0;
        newDests = new InterFilterEdge[dests.size()][];
        for(LinkedList<InterFilterEdge> dest : dests)
            newDests[i++] = dest.toArray(new InterFilterEdge[0]);

        if (SchedulingPhase.INIT == phase) {
            setInitWeights(newWeights);
            setInitDests(newDests);
        } else {
            setWeights(newWeights);
            setDests(newDests);
        }
    }
    
    /**
     * Set the steady weights and dests of this input slice node to 
     * weights and dests.
     * 
     * @param weights Array of integer weights
     * @param dests Array of Edge arrays for splitting pattern.
     */
    public void set(int[] weights, InterFilterEdge[][] dests, SchedulingPhase phase) {
        if (SchedulingPhase.INIT == phase) {
            setInitWeights(weights);
            setInitDests(dests);
        } else {
            setWeights(weights);
            setDests(dests);
        }
    }
        
    /** @return the weights */
    public int[] getWeights(SchedulingPhase phase) {
        if (phase == SchedulingPhase.INIT && initWeights != null)
            return initWeights;
        return weights;
    }

    /** 
     * @return the weights for the initialization stage, note that this may be null
     * if the splitting pattern for init is the same as steady. 
     */
    public int[] getInitWeights() {
        return initWeights;
    }
    
    /** @return whether previous filter was FileInput */
    public boolean isFileInput() {
        return ((WorkNode) getPrevious()).isFileInput();
    }

    /** @return dests */
    public InterFilterEdge[][] getDests(SchedulingPhase phase) {
        if (phase == SchedulingPhase.INIT && initDests != null)
            return initDests;
        return dests;
    }

    /** Set dests */
    public void setDests(InterFilterEdge[][] dests) {
        this.dests = dests;
    }

    /** 
     * Return the initialization pattern for splitting.  Note that this may be null
     * if the pattern is the same as the steady pattern. 
     * @return dests 
     */
    public InterFilterEdge[][] getInitDests() {
        return initDests;
    }

    /** 
     * Set the initialization pattern for splitting.
     */
    public void setInitDests(InterFilterEdge[][] newDests) {
        this.initDests = newDests;
    }

    /** @return unique string */
    public String getIdent() {
        return ident;
    }

    /** @return total of weights */
    public int totalWeights(SchedulingPhase phase) {
        int sum = 0;
        for (int i = 0; i < getWeights(phase).length; i++)
            sum += getWeights(phase)[i];
        return sum;
    }

    /**
     * Combine the weights of adjacent outputs that have equal 
     * destinations.
     * This operation exists as a cleanup operation for synch removal.
     * Code generation for Edges may rely on {@link InputNode#canonicalize()}
     * being run on all input nodes whose edges are combined by canonicalize.
     */
    public void canonicalize(SchedulingPhase phase) {
        if (getWeights(phase).length == 0)
            return;

        int[] weights = new int[getWeights(phase).length];
        InterFilterEdge[][] edges = new InterFilterEdge[getWeights(phase).length][];
        int curPort = 0;

        //add the first port to the new edges and weights
        edges[0] = getDests(phase)[0];
        weights[0] = getWeights(phase)[0];

        for(int i = 1 ; i < getWeights(phase).length ; i++) {
            if(Util.setCompare(edges[curPort], getDests(phase)[i])) {
                weights[curPort] += getWeights(phase)[i];
            }
            else {
                curPort++;
                edges[curPort] = getDests(phase)[i];
                weights[curPort] = getWeights(phase)[i];
            }
        }

        InterFilterEdge[][] newEdges = new InterFilterEdge[curPort + 1][];
        int[] newWeights = new int[curPort + 1];
        
        System.arraycopy(edges, 0, newEdges, 0, curPort + 1);
        System.arraycopy(weights, 0, newWeights, 0, curPort + 1);
        
        //if all has been reduced to a single port then the weight should be 1 on 
        //the lone port
        if (curPort == 0) {
        	newWeights[0] = 1;
        }

        //set the new weights and the dests
        set(newWeights, newEdges, phase);
    }

    /*
    public void canonicalize(SchedulingPhase phase) {
        if (getWeights(phase).length == 0)
            return;
        LinkedList<LinkedList<InterSliceEdge>> edges = new LinkedList<LinkedList<InterSliceEdge>>();
        LinkedList<Integer> newWeights = new LinkedList<Integer>();

        //add the first port to the new edges and weights
        LinkedList<InterSliceEdge> port = new LinkedList<InterSliceEdge>();
        Util.add(port, getDests(phase)[0]);
        edges.add(port);
        newWeights.add(new Integer(getWeights(phase)[0]));
        
        for (int i = 1; i < getDests(phase).length; i++) {
            if (Util.setCompare(edges.get(edges.size() - 1), getDests(phase)[i])) {
                Integer newWeight = 
                    newWeights.get(newWeights.size() - 1).intValue() + 
                    getWeights(phase)[i];
                newWeights.remove(newWeights.size() - 1);
                newWeights.add(newWeight);
            }
            else {
                //not equal, so create a new port and add it and the weight
                port = new LinkedList<InterSliceEdge>();
                Util.add(port, getDests(phase)[i]);
                edges.add(port);
                newWeights.add(new Integer(getWeights(phase)[i]));
            }
        }

        //set the new weights and the dests
        set(newWeights, edges, phase);
    }
    */
    
    /**
     * Return the width of this splitter meaning the number
     * of connections it has to downstream slices, including 
     * all the edges of a duplicated item, counting each unique 
     * edge once.
     * 
     * @return The width of this splitter.
     */
    public int getWidth(SchedulingPhase phase) {
        return getDestSet(phase).size();
    }
    
    /**
     * Return true if the weight duplicates to edge during the scheduling phase.
     */
    public boolean weightDuplicatesTo(int weight, InterFilterEdge edge, SchedulingPhase phase) {
        InterFilterEdge[][] dests = getDests(phase);
        
        for (int d = 0; d < dests[weight].length; d++) {
            if (dests[weight][d] == edge)
                return true;
        }
        return false;
    }
    
    /**
     * Return a list of the edges with each edge appearing once
     * and ordered by the order in which each edge appears in the
     * split pattern.
     * 
     * @return The list.
     */
    public LinkedList<InterFilterEdge> getDestSequence(SchedulingPhase phase) {
        
        LinkedList<InterFilterEdge> list = new LinkedList<InterFilterEdge>();
        for (int i = 0; i < getDests(phase).length; i++) {
            for (int j = 0; j < getDests(phase)[i].length; j++) 
                if (!list.contains(getDests(phase)[i][j]))
                    list.add(getDests(phase)[i][j]);
        }
        return list;
    }
    
    /**
     * return the number of items sent by this output slice node on all instances of a particular edge.
     */
    public int getWeight(InterFilterEdge in, SchedulingPhase phase) {
        int sum = 0;

        for (int i = 0; i < getDests(phase).length; i++) {
            for (int j = 0; j < getDests(phase)[i].length; j++) {
                if (getDests(phase)[i][j] == in) {
                    sum += getWeights(phase)[i];
                    break;
                }
            }
        }
        return sum;
    }

    /** type is output type of previous filter */
    public CType getType() {
        return getPrevFilter().getWorkNodeContent().getOutputType();
    }

    /**
     * Return a set of all the destination slices of this output slice node.
     * 
     * @return a set of all the destination slices of this output slice node.
     */
    public Set<Filter> getDestSlices(SchedulingPhase phase) {
        HashSet<Filter> dests = new HashSet<Filter>();
        
        for (InterFilterEdge edge : getDestSet(phase)) {
            dests.add(edge.getDest().getParent());
        }
        
        return dests;
    }
    
    /**
     * Return a list of the dests in round-robin order flattening
     * the duplicates.  
     * 
     * @return A list of the dests in round-robin order flattening
     * the duplicates.  
     */ 
    public InterFilterEdge[] getDestList(SchedulingPhase phase) {
        
        LinkedList<InterFilterEdge> edges = new LinkedList<InterFilterEdge>();
        for (int i = 0; i < getDests(phase).length; i++) {
            for (int j = 0; j < getDests(phase)[i].length; j++)
                edges.add(getDests(phase)[i][j]);
        }
        return edges.toArray(new InterFilterEdge[edges.size()]);
    }
    
    /**
     * Return the set of the outgoing edges of this OutputSliceNode.
     * 
     * @return The set of the outgoing edges of this OutputSliceNode.
     */
    public Set<InterFilterEdge> getDestSet(SchedulingPhase phase) {
        HashSet<InterFilterEdge> set = new HashSet<InterFilterEdge>();
        for (int i = 0; i < getDests(phase).length; i++) {
            for (int j = 0; j < getDests(phase)[i].length; j++)
                set.add(getDests(phase)[i][j]);
        }
        return set;
    }

    /**
     * @return true if each output edge appears only once in the schedule of splitting
     * for both init and steady
     */
    public boolean singleAppearance() {
        return singleAppearance(SchedulingPhase.STEADY) && singleAppearance(SchedulingPhase.INIT);
    }
    
    /**
     * @return true if each output edge appears only once in the schedule of splitting
     */
    public boolean singleAppearance(SchedulingPhase phase) {
        return getDestSet(phase).size() == getDestList(phase).length;
    }
    
    /**
     * Return true if this output has one output in the steady state and one or 0 outputs
     * in the init stage.
     */
    public boolean oneOutput(SchedulingPhase phase) {
        return (getWeights(phase).length == 1 && getDests(phase).length == 1 && getDests(phase)[0].length == 1);
    }
    
    /** don't call me, only here so that the spacetime backend can compile */
    public boolean oneOutput() {
        assert false;
        return false;
    }

    public InterFilterEdge getSingleEdge(SchedulingPhase phase) {
        assert oneOutput(phase) : "Calling getSingleEdge() on OutputSlice with less/more than one output";
        //System.out.println(getParent() + " " + phase);
        return getDests(phase)[0][0];
    }
    
    public boolean noOutputs() {
        return noOutputs(SchedulingPhase.INIT) && noOutputs(SchedulingPhase.STEADY);
    }

    public boolean noOutputs(SchedulingPhase phase) {
                return getWeights(phase).length == 0;
    }
    
    public boolean isDuplicateSplitter(SchedulingPhase phase) {
        return (getWeights(phase).length == 1 && getDests(phase).length == 1 && getDests(phase)[0].length >= 2);
    }
    
    public boolean isRRSplitter(SchedulingPhase phase) {
        return (getWeights(phase).length >=2 && getDests(phase).length >= 2);
    }
    
    public boolean isSplitter(SchedulingPhase phase) {
        return (isDuplicateSplitter(phase) || isRRSplitter(phase));
    }

    /**
     * return an iterator that iterates over the inputslicenodes in descending
     * order of the number of items sent to the inputslicenode
     */
    public List<InterFilterEdge> getSortedOutputs(SchedulingPhase phase) {
        LinkedList<InterFilterEdge>sortedOutputs = new LinkedList<InterFilterEdge>();  
        // if there are no dest just return an empty iterator
        if (weights.length == 0) {
            return sortedOutputs;
        }
        // just do a simple linear insert over the dests
        // only has to be done once
        Vector<InterFilterEdge> sorted = new Vector<InterFilterEdge>();
        Iterator<InterFilterEdge> destsIt = getDestSet(phase).iterator();
        // add one element
        sorted.add(destsIt.next());
        while (destsIt.hasNext()) {
            InterFilterEdge current = destsIt.next();
            // add to end if it is less then everything
            if (getWeight(current, phase) <= getWeight(sorted.get(sorted
                    .size() - 1), phase))
                sorted.add(current);
            else { // otherwise find the correct place to add it
                for (int i = 0; i < sorted.size(); i++) {
                    // if this is the correct place to insert it,
                    // add it and break
                    if (getWeight(current, phase) > getWeight(sorted.get(i), phase)) {
                        sorted.add(i, current);
                        break;
                    }
                }
            }
        }
        assert sorted.size() == getDestSet(phase).size() : "error "
            + sorted.size() + "!= " + getDestSet(phase).size();
        sortedOutputs = (LinkedList<InterFilterEdge>)sorted.subList(0, sorted.size());

        return sortedOutputs;
    }

    public WorkNode getPrevFilter() {
        return (WorkNode) getPrevious();
    }

    /**
     * Return the number of items that are sent along the <edge> in <phase>.
     */
    public int itemsSentOn(InterFilterEdge edge, SchedulingPhase phase) {
        int totalItems = WorkNodeInfo.getFilterInfo(getPrevFilter()).totalItemsSent(phase);
        
        double items = totalItems * ratio(edge, phase);
        
        assert items == Math.floor(items);
        return (int)(items);
    }
    
    public double ratio(InterFilterEdge edge, SchedulingPhase phase) {
        if (totalWeights(phase) == 0)
            return 0.0;
        return ((double) getWeight(edge, phase) / (double) totalWeights(phase));
    }

    public String  debugString(boolean escape) {
        return debugString(escape, SchedulingPhase.STEADY);
    }
    
    public String debugString(boolean escape, SchedulingPhase phase) {
        String newLine = "\n";
        StringBuffer buf = new StringBuffer();
        if (escape)
            newLine = "\\n";
        buf.append("***** " + this.toString() + " "+ phase + " *****" + newLine);
        for (int i = 0; i < getWeights(phase).length; i++) {
            buf.append("* Weight = " + getWeights(phase)[i] + newLine);
            
            for (int j = 0; j < getDests(phase)[i].length; j++)
                buf.append("  " + getDests(phase)[i][j] + newLine);
        }
        buf.append("**********" + newLine);
        return buf.toString();
    }


    public boolean hasFileOutput() {
        Iterator dests = getDestSet(SchedulingPhase.STEADY).iterator();
        while (dests.hasNext()) {
            if (((InterFilterEdge) dests.next()).getDest().isFileOutput())
                return true;
        }
        return false;
    }

    public Set<InputNode> fileOutputs() {
        HashSet<InputNode> fileOutputs = new HashSet<InputNode>();
        Iterator dests = getDestSet(SchedulingPhase.STEADY).iterator();
        while (dests.hasNext()) {
            InterFilterEdge edge = (InterFilterEdge) dests.next();
            if (edge.getDest().isFileOutput())
                fileOutputs.add(edge.getDest());
        }
        return fileOutputs;
    }
    
    /**
     * Return the sum of the weights before index of the weights array.
     */
    public int weightBefore(int index, SchedulingPhase phase) {
        assert index < getWeights(phase).length: phase + " " + this.getParent();
        int total = 0;
                
        for (int i = 0; i < index; i++)
            total += getWeights(phase)[i];
        
        return total;
    }
    
    /**
     * Return true if this output has the typical fission peeking pattern which is
     * a bunch to one edge, then some duplicated to that edge and another edge
     * 
     * Also return true if it is a simple pattern, meaning no splitting
     * 
     * @param phase
     * 
     */
    public boolean peekingFissionPattern(SchedulingPhase phase) {
        if (noOutputs(phase) || oneOutput(phase))
            return true;
    
        //these checks might die because of null pointers if there aren't 2 outputs etc.
        //so just catch them and return false.
        try {
            if (getWidth(phase) == 2 && getDests(phase).length == 2 &&
                    getDests(phase)[0].length == 1 &&
                    (getDests(phase)[1][0] == getDests(phase)[0][0] ||
                            getDests(phase)[1][1] == getDests(phase)[0][0])) {
                return true;
            }
        } catch (Exception e) {
            
        }
        return false;
    }   
    
    /**
     * Return the sum of weights for edges before this edge appears in the splitting schedule.
     * This output slice node must be single appearance.
     * 
     * @param edge The edge in question
     * @return The total weights before edge
     */
    public int weightBefore(InterFilterEdge edge, SchedulingPhase phase) {
        assert singleAppearance();
        int total = 0;
        
        for (int w = 0; w < getWeights(phase).length; w++ ) {
            boolean found = false;
            //see if the edge is in this dest list
            for (int d = 0; d < getDests(phase)[w].length; d++) {
                if (getDests(phase)[w][d] == edge) {
                    found = true;
                    break;
                }
            }
            if (found) {
                return total;
            }
            total += getWeights(phase)[w];
        }
        assert false;
        return 0;
    }
    
    /*
     * public int itemsReceived(boolean init, boolean primepump) { return
     * FilterInfo.getFilterInfo(getPrevFilter()).totalItemsSent(init,
     * primepump); }
     * 
     * public int itemsSent(boolean init, boolean primepump) {
     *  }
     */

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.slir.OutputNode other = new at.dms.kjc.slir.OutputNode();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.OutputNode other) {
        super.deepCloneInto(other);
        other.weights = this.weights;
        other.dests = this.dests;
        other.initWeights = this.initWeights;
        other.initDests = this.initDests;
        other.ident = (java.lang.String)at.dms.kjc.AutoCloner.cloneToplevel(this.ident);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
