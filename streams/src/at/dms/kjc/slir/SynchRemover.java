package at.dms.kjc.slir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import at.dms.kjc.flatgraph.DataFlowTraversal;

/**
 * The new synch remover to replace Jasp's old synch remover in converting a SIR graph to a slice graph (not yet integrated and tested).
 * @author qjli
 *
 */
public class SynchRemover {

    // The topmost slice of the graph
    private Filter topSlice;
    
    private LinkedList<Filter> slicelist;
    
    // List of all identity filters (including splitters and joiners) found
    // during the first pass through the stream graph. These will be processed
    // in the removeSynch() method
    private final LinkedList<Filter> identities = new LinkedList<Filter>();
    
    // Maps each output edge to its steady-state sequence of outputs
    private final HashMap<InterFilterEdge,SSSequence> outMap = 
        new HashMap<InterFilterEdge,SSSequence>();
    
    /**
     * Constructs a new <tt>SynchRemover</tt>.
     * @param topSlice The root slice of the stream graph
     */
    public SynchRemover(Filter topSlice) {
        this.topSlice = topSlice;
        slicelist = DataFlowOrder.getTraversal(new Filter[]{topSlice});
    }
    
    /**
     * Perform the synch removal.
     *
     */
    public void removeSync() {
        System.out.println("Starting SynchRemover...\n");
        createSSSequences();
        removeSynch();
        System.out.println("SynchRemover done!\n");
    }
    
    public Filter[] getSliceGraph() {
        return slicelist.toArray(new Filter[slicelist.size()]);
    }
    
    /**
     * Starting with <tt>current</tt>, recursively visits every downstream
     * Slice and performs the following:
     * - if Slice is not an identity, creates SSSequence for each Slice's
     * output edges
     * - if Slice is an identity, adds it to the list of identities to be
     * processed later
     * @param current
     */
    private void createSSSequences() {
        for (Filter current : slicelist) {

            System.out.println(current.toString());

            for (int i=0; i<current.getInputNode().getWeights(SchedulingPhase.STEADY).length; i++) {
                System.out.println(current.getInputNode().getWeights(SchedulingPhase.STEADY)[i]);
            }

            if (!isIdentity(current)) {
                // create steady-state output sequence
                createSSOutput(current);
            } else {
                System.out.println("Found identity..." + current.toString());
                // add identities (including splitters and joiners) to the list
                // to be processed later
                identities.add(current);
            }
        }
    }
    
    /**
     * For a given non-identity Slice, creates the steady state output sequence
     * for all of the outgoing InterSliceEdges. 
     * @param current
     */
    private void createSSOutput(Filter current) {
        // index to keep track of the output
        int index = 1;
        int sumweights = current.getOutputNode().totalWeights(SchedulingPhase.STEADY);
        int[] weights = current.getOutputNode().getWeights(SchedulingPhase.STEADY);
        InterFilterEdge[][] outputs = current.getOutputNode().getDests(SchedulingPhase.STEADY);
        // outer loop loops over duplicate copies
        for (int i=0; i<outputs.length; i++) {
            int weight = weights[i];
            // each duplicated output gets the same SSSequence
            for (int j=0; j<outputs[i].length; j++) {
                SSSequence seq = createSSSequence(current, index, weight, sumweights);
                outMap.put(outputs[i][j], seq);
            }
            // increment index
            index+=weight;
        }
    }
    

    private void removeSynch() {
        System.out.println("Total of " + identities.size() + " identity slices");
        while (!identities.isEmpty()) {
            HashMap<InterFilterEdge,SSSequence> inMap = new HashMap<InterFilterEdge,SSSequence>();
            
            Filter id = identities.removeFirst();
            Filter[] parents = id.getDependencies(SchedulingPhase.STEADY);
            int[] inweights = id.getInputNode().getWeights(SchedulingPhase.STEADY);
            int inweightsum = id.getInputNode().totalWeights(SchedulingPhase.STEADY);
            
            // Array to store output SSSequences of each parent slice 
            SSSequence[] parentOutputSeqs = new SSSequence[parents.length];
            // Array to store edge between each parent and current slice
            InterFilterEdge[] edges = new InterFilterEdge[parents.length];
            
            // If any parents are identities and haven't yet been processed,
            // add this slice back to the end of the list and loop again
            if (listContainsAny(identities, parents)) {
                identities.add(id);
                continue;
            }
            
            System.out.println("Starting remove sync for slice: " + id.toString());
            
            // number of times to repeat inputs
            int tempinmult = 1;
            
            // Go through parents to calculate lcm's and extract output SSSequences
            // from the HashMap of InterSliceEdges
            for (int i=0; i<parents.length; i++) {
                Filter parent = parents[i];
                
                // Find output SSSequence from the InterSliceEdge
                InterFilterEdge e = getEdgeBetween(parent, id);
                if (e != null) System.out.println("Found edge!");
                else System.out.println("Didn't find edge!");
                SSSequence output = outMap.get(e);
                // Store in the arrays for later use
                parentOutputSeqs[i] = output;
                edges[i] = e;
                
                // calculate lcm for this particular input
                int partialmult = lcm(output.length(), inweights[i])/inweights[i];
                // update running lcm over all inputs
                tempinmult = lcm(tempinmult, partialmult);
            }
            
            // calculate overall multiplicity for inputs and outputs
            int outweightsum = id.getOutputNode().totalWeights(SchedulingPhase.STEADY);
            int lcm = lcm(inweightsum*tempinmult, outweightsum);
            int outmult = lcm/outweightsum;
            int inmult = lcm/inweightsum;
            
            // create output sequences for each child of each parent and store
            // in inMap, keyed by the edge
            for (int i=0; i<parents.length; i++) {
                // retrieve information for ith parent
                Filter parent = parents[i];
                InterFilterEdge[][] parentOutEdges = parent.getOutputNode().getDests(SchedulingPhase.STEADY);
                int[] weights = parent.getOutputNode().getWeights(SchedulingPhase.STEADY);
                
                // calculate number of times this parent's outputs have to be
                // repeated
                SSSequence seq = parentOutputSeqs[i];
                int parentRepeatMult = inmult*inweights[i]/seq.length();
                
                // iterate through all of parent's outgoing edges
                for (int j=0; j<parentOutEdges.length; j++) {
                    int weight = weights[j];
                    InterFilterEdge[] dupes = parentOutEdges[j];
                    // All SSSequences for duplicates should be the same --
                    // just get the first one
                    SSSequence outseq = outMap.get(dupes[0]);
                    ArrayList<SSElement> list = new ArrayList<SSElement>();
                    // create steady-state output for each child
                    for (int k=0; k<parentRepeatMult*weight; k++) {
                        SSElement temp = outseq.getNext();
                        // update repeat multiplier
                        SSElement newelt = new SSElement(temp.slice, temp.num, temp.repeat*parentRepeatMult);
                        list.add(newelt);
                    }
                    // store in inMap, keyed by the edge
                    for (int l=0; l<dupes.length; l++) {
                        inMap.put(dupes[l], new SSSequence(list));
                    }
                }
            }
            
            // combine all input sequences into a single sequence
            ArrayList<SSElement> out = new ArrayList<SSElement>();
            for (int i=0; i<inmult; i++) {
                for (int j=0; j<parents.length; j++) {
                    InterFilterEdge e = edges[j];
                    SSSequence in = inMap.get(e);
                    for (int k=0; k<inweights[j]; k++) {
                        out.add(in.getNext());
                    }
                }
            }
            Iterator<SSElement> iter = out.iterator();
            
            // Distribute single sequence over all of the output Slices
            InterFilterEdge[][] outedges = id.getOutputNode().getDests(SchedulingPhase.STEADY);
            SSSequence[] outseqs = new SSSequence[outedges.length];
            int[] outweights = id.getOutputNode().getWeights(SchedulingPhase.STEADY);
            
            for (int i=0; i<outmult; i++) {
                for (int j=0; j<outedges.length; j++) {
                    if (outseqs[j] == null) {
                        outseqs[j] = new SSSequence();
                    }
                    int weight = outweights[j];
                    for (int k=0; k<weight; k++) {
                        outseqs[j].add(iter.next());
                    }
                }
            }
            for (int i=0; i<outedges.length; i++) {
                for (int j=0; j<outedges[i].length; j++) {
                    inMap.put(outedges[i][j], new SSSequence(outseqs[i]));
                }
            }
            
            // create new outgoing edges for all of the parent slices
            for (int i=0; i<parents.length; i++) {
                Filter parent = parents[i];
                InterFilterEdge[][] parentOutEdges = parent.getOutputNode().getDests(SchedulingPhase.STEADY);
                int[] weights = parent.getOutputNode().getWeights(SchedulingPhase.STEADY);
                HashMap<Integer,LinkedList<InterFilterEdge>> newEdgesMap = 
                    new HashMap<Integer,LinkedList<InterFilterEdge>>();
                System.out.println("creating new outgoing edges for parents");
                for (int j=0; j<parentOutEdges.length; j++) {
                    for (int k=0; k<parentOutEdges[j].length; k++) {
                        if (parentOutEdges[j][k].getDest().getParent() == id)
                            continue;
                        SSSequence seq = inMap.get(parentOutEdges[j][k]);
                        for (int l=0; l<seq.length(); l++) {
                            SSElement elt = seq.getNext();
                            System.out.println("ee" + elt.num);
                            if (!newEdgesMap.containsKey(elt.num)) {
                                System.out.println("adding: " + elt.num);
                                newEdgesMap.put(elt.num, new LinkedList<InterFilterEdge>());
                            }
                            LinkedList<InterFilterEdge> array = newEdgesMap.get(elt.num);
                            InterFilterEdge newedge = new InterFilterEdge(parent.getOutputNode(), parentOutEdges[j][k].getDest());
                            array.add(newedge);
                        }
                    }
                }
                for (int j=0; j<outedges.length; j++) {
                    for (int k=0; k<outedges[j].length; k++) {
                        InterFilterEdge outedge = outedges[j][k];
                        SSSequence seq = inMap.get(outedge);
                        for (int l=0; l<seq.length(); l++) {
                            SSElement elt = seq.getNext();
                            if (elt.slice != parent)
                                continue;
                            System.out.println(elt.num);
                            if (!newEdgesMap.containsKey(elt.num)) {
                                System.out.println("adding: " + elt.num);
                                newEdgesMap.put(elt.num, new LinkedList<InterFilterEdge>());
                            }
                            LinkedList<InterFilterEdge> array = newEdgesMap.get(elt.num);
                            InterFilterEdge newedge = new InterFilterEdge(parent.getOutputNode(), outedge.getDest());
                            array.add(newedge);
                        }
                    }
                }
                LinkedList<Integer> newweights = new LinkedList<Integer>();
                for (int j=0; j<newEdgesMap.size(); j++) {
                    newweights.add(new Integer(1));
                }
                
                LinkedList<LinkedList<InterFilterEdge>> newedges =
                    new LinkedList<LinkedList<InterFilterEdge>>();
                for (int j=1; j<=newEdgesMap.size(); j++) {
                    assert newEdgesMap.containsKey(j);
                    System.out.println(j + newEdgesMap.get(j).toString());
                    newedges.add(newEdgesMap.get(j));                    
                }
                parent.getOutputNode().set(newweights, newedges, SchedulingPhase.STEADY);
            }
            
            id.getInputNode().setSources(new InterFilterEdge[0]);
            id.getInputNode().setWeights(new int[0]);
            id.getOutputNode().setDests(new InterFilterEdge[0][]);
            id.getOutputNode().setWeights(new int[0]);
            System.out.println("Removed sync for slice: " + id.toString() + "\n");
        }
    }
    

    
    // ---------------------- Util methods ---------------------------------
    
    private static int lcm(int a, int b) {
        return a*b/gcd(a,b);
    }
    
    private static int gcd(int a, int b) {
        if (a%b == 0)
            return b;
        else return gcd(b, a%b);
    }
    
    private static boolean isIdentity(Filter slice) {
    	if (!slice.getWorkNodeContent().getName().startsWith("Identity"))
                return false;
    	return true;
    }

    private static InterFilterEdge getEdgeBetween(Filter from, Filter to) {
        for (InterFilterEdge e : from.getOutputNode().getDestSequence(SchedulingPhase.STEADY)) {
            if (e.getDest() == to.getInputNode())
                return e;
        }
        return null;
    }
    
    /**
     * Returns true if any of the Slices in array <tt>parents</tt> is contained
     * in the LinkedList <tt>identities</tt>.
     * @param identities
     * @param parents
     * @return
     */
    private static boolean listContainsAny(LinkedList<Filter> identities, Filter[] parents) {
        for (int i=0; i<parents.length; i++) {
            if (identities.contains(parents[i]))
                return true;
        }
        return false;
    }
    
    /**
     * Creates an SSSequence using the Slice <tt>current</tt>.
     * @param current The Slice
     * @param index The starting index of output
     * @param weight The weight of this output Edge
     * @param sumweights The total sum of weights coming out of this Slice
     * @return
     */
    private static SSSequence createSSSequence(Filter current, int index, int weight, int sumweights) {
        ArrayList<SSElement> list = new ArrayList<SSElement>();
        for (int i=0; i<weight; i++) {
            SSElement elt = new SSElement(current, index, sumweights);
            list.add(elt);
            index++;
        }
        return new SSSequence(list);
    }
    
    
    /**
     * Creates an array that expands a list of slices and associated weights by
     * explicitly listing each slice  number of times equal to its weight.
     * This entire array is possibly repeated an integral number of times.
     * For example:
     * slices = [A,B,C], weights = [2,3,1], sumWeights = 6, mult = 3
     * => [A,A,B,B,B,C,A,A,B,B,B,C,A,A,B,B,B,C]
     * 
     * @param slices Array of slices to be expanded
     * @param weights Array of corresponding weights for each slice
     * @param sumWeights Sum of all of the weights
     * @param mult The number of times to repeat the entire array
     * @return
     */
    private static Filter[] makeRepeatedArray(Filter[] slices, int[] weights,
            int sumWeights, int mult) {
        Filter[] repeatedArray = new Filter[sumWeights*mult];
        int l = 0;
        for (int i=0; i<mult; i++) {
            for (int j=0; j<slices.length; j++) {
                for (int k=0; k<weights[j]; k++) {
                    repeatedArray[l] = slices[j];
                    l++;
                }
            }
        }
        return repeatedArray;
    }
    
    private static ArrayList<Filter> makeRepeatedList(Filter[] slices, int[] weights,
            int mult) {
        ArrayList<Filter> list = new ArrayList<Filter>();
        for (int i=0; i<mult; i++) {
            for (int j=0; j<slices.length; j++) {
                for (int k=0; k<weights[j]; k++) {
                    list.add(slices[j]);
                }
            }
        }
        return list;
    }
    
    private static ArrayList<Filter> makeRepeatedOutputList(Filter s, int mult) {
        return makeRepeatedList(getChildSlices(s), s.getOutputNode().getWeights(SchedulingPhase.STEADY), mult);
    }

    /**
     * Creates lists for the new edges and weights, given an array of Slices
     * that explicitly lists each occurrence of the Slice in the schedule.
     * For example:
     * [A,A,B,B,B,C] => newEdges = [A,B,C], newWeights = [2,3,1]
     * 
     * @param repeatedOutput The array of repeated slices
     * @param newWeights The list to store the condensed weights
     * @param newSlices The list to store the condensed slices
     */
    private static void createNewSlicesWeightsLists(Filter[] repeatedOutput,
            LinkedList<Integer> newWeights, LinkedList<Filter> newSlices) {
        
        if (repeatedOutput == null || repeatedOutput.length == 0)
            return;
                
        // Keeps track of the previous slice in order to count the number of
        // repeated slices
        Filter prev = repeatedOutput[0];
        int count = 0;
        for (int i=0; i<repeatedOutput.length; i++) {
            Filter curr = repeatedOutput[i];
            // If the current slice is the same as the previous, increment count
            if (curr == prev) {
                count++;
            } else {
                // The current slice is different from the previous slice
                // Add the previous group of slices to the edge and weight lists
                newSlices.add(prev);
                newWeights.add(new Integer(count));
                // Reset prev and count for the next group
                prev = curr;
                count = 1;
            }
        }
        // The last group of slices will not be added in the loop. Add it now.
        newSlices.add(prev);
        newWeights.add(new Integer(count));
    }
    
    /**
     * Creates new outgoing edges between the OutputSliceNode and the list of
     * output Slices. If splitter is RR, creates a separate LinkedList for each
     * Edge. If splitter is duplicate, creates a single LinkedList for all Edges.
     * @param slice
     * @param outputs
     * @param isRR
     * @return
     */
    private static LinkedList<LinkedList<InterFilterEdge>> createNewOutgoingEdges(
            OutputNode slice, LinkedList<Filter> outputs, boolean isRR) {
        LinkedList<LinkedList<InterFilterEdge>> newEdges = new LinkedList<LinkedList<InterFilterEdge>>();
        
        if (isRR) {
            for (Filter output : outputs) {
                LinkedList<InterFilterEdge> temp = new LinkedList<InterFilterEdge>();
                InterFilterEdge e = new InterFilterEdge(slice, output.getInputNode());
                temp.add(e);
                newEdges.add(temp);
            }
        } else {
            LinkedList<InterFilterEdge> temp = new LinkedList<InterFilterEdge>();
            for (Filter output : outputs) {
                InterFilterEdge e = new InterFilterEdge(slice, output.getInputNode());
                temp.add(e);
            }
            newEdges.add(temp);
        }
        
        return newEdges;
    }
    
    /**
     * Creates new incoming edges to the InputSliceNode from the list of input
     * Slices.
     * @param slice
     * @param inputs
     * @return
     */
    private static LinkedList<InterFilterEdge> createNewIncomingEdges(InputNode slice,
            LinkedList<Filter> inputs) {
        LinkedList<InterFilterEdge> newEdges = new LinkedList<InterFilterEdge>();
        
        for (Filter input : inputs) {
            InterFilterEdge e = new InterFilterEdge(input.getOutputNode(), slice);
            newEdges.add(e);
        }
        
        return newEdges;
    }
    
    /**
     * Finds all of the parent slices upstream from the current slice.
     * @param slice The current slice
     * @return Array of parent slices
     */
    private static Filter[] getParentSlices(Filter slice) {
        LinkedList<Filter> parents = new LinkedList<Filter>();
        LinkedList<InterFilterEdge> inEdges = slice.getInputNode().getSourceList(SchedulingPhase.STEADY);
        
        for (InterFilterEdge e : inEdges) {
            Filter parent = e.getSrc().getParent();
            parents.add(parent);
        }
        
        return parents.toArray(new Filter[0]);
    }
    
    /**
     * Finds all of the child slices downstream from the current slice.
     * @param slice The current slice
     * @return Array of child slices
     */
    private static Filter[] getChildSlices(Filter slice) {
        LinkedList<Filter> children = new LinkedList<Filter>();
        InterFilterEdge[] outEdges = slice.getOutputNode().getDestList(SchedulingPhase.STEADY);
        
        for (int i=0; i<outEdges.length; i++) {
            Filter child = outEdges[i].getDest().getParent();
            children.add(child);
        }
        
        return children.toArray(new Filter[0]);
    }
}

//class IOFunction {
//    Slice slice;
//    int[] inputWeights;
//    int[] outputWeights;
//    
//    IOFunction(Slice s, int[] iw, int[] ow) {
//        slice = s;
//        inputWeights = iw;
//        outputWeights = ow;
//    }
//    
//    int inToOut(int inputTap, int tapePos) {
//        return 0;
//    }
//    
//    int inToOut(IOPair inPair) {
//        return 0;
//    }
//    
//    IOPair outToPair(int outNum) {
//        return null;
//    }
//    
//    IOPair getOutput(int inputTape, int tapePos) {
//        return outToPair(inToOut(inputTape, tapePos));
//    }
//    
//    IOPair getOutput(IOPair inPair) {
//        return outToPair(inToOut(inPair));
//    }
//}
//
//class IOPair {
//    int tape;
//    int pos;
//    
//    IOPair(int t, int p) {
//        tape = t;
//        pos = p;
//    }
//}

class SSSequence {
    ArrayList<SSElement> list;
    int index;
    //int sumweights;
    int redos;
    
    SSSequence() {
        list = new ArrayList<SSElement>();
        index = 0;
        redos = 0;
    }
    
    SSSequence(ArrayList<SSElement> list) {
        this.list = new ArrayList<SSElement>(list);
        index = 0;
        redos = 0;
    }
    
    SSSequence(SSSequence dupe) {
        this.list = new ArrayList<SSElement>(dupe.list);
        this.index = dupe.index;
        this.redos = dupe.redos;
    }
    
    SSElement getNext() {
        SSElement temp;
        if (index >= list.size()) {
            index = 0;
            redos++;
            temp = list.get(0);
        } else {
            temp = list.get(index);
            index++;
        }
        return new SSElement(temp.slice, temp.num + redos*temp.repeat, temp.repeat);
    }
    
    int length() {
        return list.size();
    }
    
    void add(SSElement s) {
        list.add(s);
    }
}

class SSElement {
    Filter slice;
    int num;
    int repeat;
    
    SSElement(Filter slice, int num) {
        this.slice = slice;
        this.num = num;
        this.repeat = 1;
    }
    
    SSElement(Filter slice, int num, int repeat) {
        this.slice = slice;
        this.num = num;
        this.repeat = repeat;
    }
    
    Filter getSlice() {
        return slice;
    }
    
    int getNum() {
        return num;
    }
    
    int getRepeat() {
        return repeat;
    }
    
    boolean equals(SSElement elt) {
        return (this.slice == elt.slice && this.num == elt.num);
    }
}