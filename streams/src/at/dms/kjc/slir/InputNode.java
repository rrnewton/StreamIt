package at.dms.kjc.slir;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import at.dms.kjc.CType;
import at.dms.kjc.backendSupport.FilterInfo;
import at.dms.util.Utils;

/**
 * Each Slice is started by an InputSlice Node that is either a joiner connecting several other slices, 
 * or a connection to a single other slice.  There is a possibility that an input slice node could have 
 * a different schedule for the initialization stage. This is not the common case, so most methods 
 * assume there is not a separate schedule and use the single (steady and init) weights/sources.
 * 
 * Has an array of weights and corresponding {@link Channel}s.
 */
public class InputNode extends InternalFilterNode implements at.dms.kjc.DeepCloneable {
    public static final String[] DO_NOT_CLONE_THESE_FIELDS = 
        { "weights", "sources", "initWeights", "initSources" };
    
    /** the incoming round robin weights for this input slice node for the steady and for init 
     * if the initWeights are null.
     */
    private int[] weights;
    /** the sources that correspond to the weights for the steady and for init if initWeights/initSources
     * are null
     */
    private Channel[] sources;
    /** if this inputslicenode requires a different joiner pattern for init, this will encode the weights */
    private int[] initWeights;
    /** if this inputslicenode requires a different joiner patter for init, this will encode the sources */
    private Channel[] initSources;
    /** used to construct a unique identifier */
    private static int unique = 0;
    /** unique identifier */
    private String ident;
    /** used if no joining is performed* */
    private static int[] EMPTY_WEIGHTS = new int[0];
    /** used if no joining is performed */
    private static Channel[] EMPTY_SRCS = new Channel[0];

    /** Creator */
    public InputNode(int[] weights, Channel[] sources) {
        // this.parent = parent;
        if (weights.length != sources.length)
            Utils.fail("Add comment later");
        this.sources = sources;
        if (weights.length == 1)
            this.weights = new int[]{1};
        else 
            this.weights = weights;
        ident = "input" + unique;
        unique++;
    }

    /** Constructor */
    public InputNode(int[] weights, OutputNode[] sources) {
        // this.parent = parent;
        if (weights.length != sources.length)
            Utils.fail("Add comment later");
        // this.sources = sources;
        this.sources = new Channel[sources.length];
        for (int i = 0; i < sources.length; i++)
            this.sources[i] = new Channel(sources[i].getParent(), this.getParent());
        
        if (weights.length == 1)
            this.weights = new int[]{1};
        else 
            this.weights = weights;
        ident = "input" + unique;
        unique++;
    }

    /** Constructor: weights, edges to be set later. */
    public InputNode(int[] weights) {
        // this.parent = parent;
        sources = EMPTY_SRCS;
        if (weights.length == 1)
            this.weights = new int[]{1};
        else 
            this.weights = weights;
        ident = "input" + unique;
        unique++;
    }

    /** Constructor: no edges, no weights */
    public InputNode() {
        // this.parent = parent;
        sources = EMPTY_SRCS;
        weights = EMPTY_WEIGHTS;
        ident = "input" + unique;
        unique++;
    }

    /**
     * Merge neighboring edges and weights if the neighboring edges
     * are actually the same Edge object. 
     * This operation exists as a cleanup operation for synch removal.
     * Code generation for Edges may rely on {@link OutputNode#canonicalize()}
     * being run on all output nodes whose edges are combined by canonicalize.
     */
    public void canonicalize(SchedulingPhase phase) {
        if (getWeights(phase).length == 0)
            return;

        int[] weights = new int[getWeights(phase).length];
        Channel[] edges = new Channel[getWeights(phase).length];
        int curPort = 0;

        //add the first port to the new edges and weights
        edges[0] = getSources(phase)[0];
        weights[0] = getWeights(phase)[0];

        for(int i = 1 ; i < getWeights(phase).length ; i++) {
            if(edges[curPort].equals(getSources(phase)[i])) {
                weights[curPort] += getWeights(phase)[i];
            }
            else {
                curPort++;
                edges[curPort] = getSources(phase)[i];
                weights[curPort] = getWeights(phase)[i];
            }
        }

        Channel[] newEdges = new Channel[curPort + 1];
        int[] newWeights = new int[curPort + 1];

        System.arraycopy(edges, 0, newEdges, 0, curPort + 1);
        System.arraycopy(weights, 0, newWeights, 0, curPort + 1);

        //set the new weights and the dests
        set(newWeights, newEdges, phase);
    }

    /*
    public void canonicalize(SchedulingPhase phase) {
        //do nothing for 0 length joiners
        if (getSources(phase).length == 0)
            return;
        
        LinkedList<Integer> newWeights = new LinkedList<Integer>();
        LinkedList<InterSliceEdge> newEdges = new LinkedList<InterSliceEdge>();

        //add the first edge and weight
        newWeights.add(new Integer(getWeights(phase)[0]));
        newEdges.add(getSources(phase)[0]);
        
        for (int i = 1; i < getSources(phase).length; i++) {
            if (getSources(phase)[i] == newEdges.get(newEdges.size() - 1)) {
                //this src is equal to the last one, so add the weights together!
                Integer newWeight = 
                    new Integer(newWeights.get(newWeights.size() - 1).intValue() + 
                                getWeights(phase)[i]);
                newWeights.remove(newWeights.size() - 1);
                newWeights.add(newWeight);
            }
            else {
                //not equal, start a new entry and weight
                newEdges.add(getSources(phase)[i]);
                newWeights.add(new Integer(getWeights(phase)[i]));
            }
        }
        set(newWeights, newEdges, phase);
    }
    */
    
    /** InputSliceNode is FileOutput if FilterSliceNode is FileOutput.*/
    public boolean isFileOutput() {
        return parent.getWorkNode().isFileOutput();
    }

    /** Returns unique string identifying slice. */
    public String getIdent() {
        return ident;
    }

    public boolean singleAppearance() {
        return singleAppearance(SchedulingPhase.STEADY) && singleAppearance(SchedulingPhase.INIT);
    }
    
    /**
     * @return true if each input edge appears only once in the schedule of joining
     */
    public boolean singleAppearance(SchedulingPhase phase) {
        return getSourceSet(phase).size() == getSourceList(phase).size();
    }
    
    /**
     * Return true if this input slice node has an incoming edge from <node> in <phase>.
     */
    public boolean hasEdgeFrom(SchedulingPhase phase, WorkNode node) {
        for (Channel edge : getSourceSet(phase)) {
            if (edge.getSrc().getWorkNode() == node) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * return the edge that goes from node's outputslicenode to this inputslicenode
     * 
     */
    public Channel getEdgeFrom(SchedulingPhase phase, WorkNode node) {
        Channel ret = null;
        
        for (Channel edge : getSourceSet(phase)) {
            if (edge.getSrc().getWorkNode() == node) {
                assert ret == null;
                ret = edge;
            }
        }
        
        assert ret != null : "cannot find edge to " + node + " in getEdgeFrom() in " + this + " InputSliceNode for " + phase;
        return ret;
    }
    
    /**
     * return the sum of the weights that appear before index in the joining schedule
     */
    public int weightBefore(int index, SchedulingPhase phase) {
        assert index < weights.length;
        int total = 0;
        
        for (int i = 0; i < index; i++) {
            total += getWeights(phase)[i];
        }
 
        return total;
    }
    
    /**
     * return the sum of the weights that appear before this edge in the joining schedule
     * 
     * @param edge the edge in question
     * 
     * @return the sum of the weights before edge
     */
    public int weightBefore(Channel edge, SchedulingPhase phase) {
        assert singleAppearance(phase);
        
        int total = 0;
        for (int i = 0; i < getWeights(phase).length; i++) {
            if (getSources(phase)[i] == edge) 
                return total;
            
            total += getWeights(phase)[i];
        }
        assert false;
        return 0;
    }
    
    /**
     * Return the number of unique inputs (Edges) to this join.
     * 
     * @return The width of the join.
     */
    public int getWidth(SchedulingPhase phase) {
        return getSourceSet(phase).size();
    }
    
    /**
     * Return the number of items that traverse this edge
     * on one iteration of this input slice node, remember
     * that a single edge can appear multiple times in the joining
     * pattern.
     * 
     * @param edge The edge to query.
     * @return The number of items passing on the edge.
     */
    public int getItems(Channel edge, SchedulingPhase phase) {
        int items = 0;
        
        for (int i = 0; i < getSources(phase).length; i++) {
            if (getSources(phase)[i] == edge) {
                items += getWeights(phase)[i];
            }
        }
        
        return items;
    }

    /** @return array of edge weights */
    public int[] getWeights(SchedulingPhase phase) {
        if (phase == SchedulingPhase.INIT && initWeights != null)
            return initWeights;
        
        return weights;
    }

    /** @return array of edges */
    public Channel[] getSources(SchedulingPhase phase) {
        if (phase == SchedulingPhase.INIT && initSources != null)
            return initSources;
        
        return sources;
    }


    /** @return array of edge weights for the init schedule */
    public int[] getInitWeights() {
        return initWeights;
    }

    /** @return array of edges for the init schedule*/
    public Channel[] getInitSources() {
        return initSources;
    }

    
    /**
     * Set the weights and sources array of this input slice node
     * to the weights list and the edges list.
     * 
     * @param weights The list of weights (Integer).
     * @param edges The list of edges.
     */
    public void set(LinkedList<Integer> weights, 
            LinkedList<Channel> edges, SchedulingPhase phase) {
        int[] intArr = new int[weights.size()]; 
        
        for (int i = 0; i < weights.size(); i++)
            intArr[i] = weights.get(i).intValue();
        if (SchedulingPhase.INIT == phase) {
            setInitWeights(intArr);
            setInitSources(edges.toArray(new Channel[edges.size()]));
        } else {
            setWeights(intArr);
            setSources(edges.toArray(new Channel[edges.size()]));
        }
    }

    /**
     * Set the weights and sources array of this input slice node
     * to the weights list and the edges list.
     * 
     * @param weights The array of weights (integer).
     * @param edges The array of edges.
     */
    public void set(int[] weights, Channel[] edges, SchedulingPhase phase) {
        if (SchedulingPhase.INIT == phase) {
            setInitWeights(weights);
            setInitSources(edges);
        } else {
            setWeights(weights);
            setSources(edges);
        }
    }
    
    /**
     * Set the initialization weights.
     * 
     * @param newWeights The new weights
     */
    public void setInitWeights(int[] newWeights) {
        if (newWeights != null && newWeights.length == 1)
            this.initWeights = new int[]{1};
        else 
            this.initWeights = newWeights;
    }
    
    /**
     * If the initialization pattern needs to be different from steady,
     * set the weights to newWeights.
     * 
     * @param newWeights
     */
    public void setWeights(int[] newWeights) {
        if (newWeights.length == 1)
            this.weights = new int[]{1};
        else 
            this.weights = newWeights;
    }
    
    /** Set the source edges. (shares, does not copy.) */
    public void setSources(Channel[] sources) {
        this.sources = sources;
    }

    /**
     * If the initialization pattern needs to be different from steady,
     * set the sources to newSrcs.  (shares, does not copy)
     * 
     * @param newSrcs The new sources.
     */
    public void setInitSources(Channel[] newSrcs) {
        this.initSources = newSrcs;
    }
    
    /** @return total weight of all edges */
    public int totalWeights(SchedulingPhase phase) {
        int sum = 0;
        for (int i = 0; i < getWeights(phase).length; i++)
            sum += getWeights(phase)[i];
        return sum;
    }

    /** @return total weight on all connections to a single Edge. 
     * @param out The Edge that we are interested in*/
    public int getWeight(Channel out, SchedulingPhase phase) {
        int sum = 0;

        for (int i = 0; i < getSources(phase).length; i++)
            if (getSources(phase)[i] == out)
                sum += getWeights(phase)[i];

        return sum;
    }
    
    /**
     * Does sources have a single element in phase
     *  
     * @return true if there is a single element in sources. */
    public boolean oneInput(SchedulingPhase phase) {
        return getSources(phase).length == 1;
    }
    
    /** don't call me, only here so that the spacetime compiler can compile */
    public boolean oneInput() {
        assert false;
        return false;
    }
    
    /** 
     * Is a joiner if there are at least 2 sources (even if same Edge object).
     * @return is a joiner.
     */
    public boolean isJoiner(SchedulingPhase phase) {
        return getSources(phase).length >= 2;
    }

    /** Get the singleton edge. 
     * Must have only one input in sources.
     * @return the edge, or throw AssertionError
     */
    public Channel getSingleEdge(SchedulingPhase phase) {
        assert oneInput(phase) : "Calling getSingeEdge() on InputSlice with less/more than one input";
        return getSources(phase)[0];
    }

    /** 
     * Get the following FilterSliceNode.
     * @return
     */
    public WorkNode getWorkNode() {
        return parent.getWorkNode();
    }

    /** Are there no inputs?
     * 
     * @return
     */
    public boolean noInputs(SchedulingPhase phase) {
        return getSources(phase).length == 0;
    }
    
    public boolean noInputs() {
        return noInputs(SchedulingPhase.INIT) && noInputs(SchedulingPhase.STEADY);
    }

    public int itemsReceivedOn(Channel edge, SchedulingPhase phase) {
        double totalItems = FilterInfo.getFilterInfo(getWorkNode()).totalItemsReceived(phase);
        
        double items = totalItems * ratio(edge, phase);
        assert items == Math.floor(items);
        
        return (int)items;
    }
    
    /** return ratio of weight of edge to totalWeights().
     * 
     * @param edge
     * @return  0.0 if totalWeights() == 0, else ratio.
     */
    public double ratio(Channel edge, SchedulingPhase phase) {
        if (totalWeights(phase) == 0)
            return 0.0;
        return ((double) getWeight(edge, phase) / (double) totalWeights(phase));
    }

    /**
     * Return a list of the edges with each edge appearing once
     * and ordered by the order in which each edge appears in the
     * join pattern.
     * 
     * @return The list.
     */
    public LinkedList<Channel> getSourceSequence(SchedulingPhase phase) {
        LinkedList<Channel> list = new LinkedList<Channel>();
        for (int i = 0; i < getSources(phase).length; i++) {
            if (!list.contains(getSources(phase)[i]))
                list.add(getSources(phase)[i]);
        }
        return list;
    }
    
    /**
     * Return a set of all the slices that are inputs to this slice.
     * 
     * @return a set of all the slices that are inputs to this slice.
     */
    public Set<Filter> getSourceSlices(SchedulingPhase phase) {
        HashSet<Filter> slices = new HashSet<Filter>();
        for (Channel edge : getSourceList(phase)) {
            slices.add(edge.getSrc());
        }
        return slices;
    }
    
    /**
     * Return a linked list of the sources pattern.
     * 
     * @return The linked list of the sources pattern.
     */
    public LinkedList<Channel> getSourceList(SchedulingPhase phase) {
       LinkedList<Channel> list = new LinkedList<Channel>();
       for (int i = 0; i < getSources(phase).length; i++)
           list.add(getSources(phase)[i]);
       return list;
    }
    
    public Set<Channel> getSourceSet(SchedulingPhase phase) {
        HashSet<Channel> set = new HashSet<Channel>();
        for (int i = 0; i < getSources(phase).length; i++)
            set.add(getSources(phase)[i]);
        return set;
    }
    
    /**
     * Return a string that gives some information for this input slice node.
     * If escape is true, then escape the new lines "\\n".
     *  
     * @param escape Should we escape the new lines?
     * @return The string.
     */
    public String debugString(boolean escape) {
        return debugString(escape, SchedulingPhase.STEADY);
    }

    /**
     * Return a string that gives some information for this input slice node.
     * If escape is true, then escape the new lines "\\n".
     *  
     * @param escape Should we escape the new lines?
     * @return The string.
     */
    public String debugString(boolean escape, SchedulingPhase phase) {
        String newLine = "\n";
        if (escape)
            newLine = "\\n";

        StringBuffer buf = new StringBuffer();
        buf.append("***** " + this.toString() + " " + phase + " *****" + newLine);
        for (int i = 0; i < getSources(phase).length; i++) {
            buf.append("  weight " + getWeights(phase)[i] + ": " + getSources(phase)[i].toString()
                       + newLine);
        }
        buf.append("**********" + newLine);
        return buf.toString();
    }

    public boolean hasFileInput() {
        for (int i = 0; i < sources.length; i++) {
            if (sources[i].getSrc().getOutputNode().isFileInput())
                return true;
        }
        return false;
    }

    /**
     * In the sources array for the input, replace all instances of 
     * oldEdge with newEdge.
     * 
     * @param oldEdge The edge to replace.
     * @param newEdge The edge to install.
     */
    public void replaceEdge(Channel oldEdge, Channel newEdge, SchedulingPhase phase) {
        if (phase == SchedulingPhase.INIT) {
            for (int i = 0; i < initSources.length; i++) {
                if (initSources[i] == oldEdge)
                    initSources[i] = newEdge;
            }

        }
        else {
            for (int i = 0; i < sources.length; i++) {
                if (sources[i] == oldEdge)
                    sources[i] = newEdge;
            }
        }
    }

    public CType getType() {
        return getWorkNode().getFilter().getInputType();
    }


    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.slir.InputNode other = new at.dms.kjc.slir.InputNode();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.InputNode other) {
        super.deepCloneInto(other);
        other.weights = this.weights;
        other.sources = this.sources;
        other.initWeights = this.initWeights;
        other.initSources = this.initSources;
        other.ident = (java.lang.String)at.dms.kjc.AutoCloner.cloneToplevel(this.ident);
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
