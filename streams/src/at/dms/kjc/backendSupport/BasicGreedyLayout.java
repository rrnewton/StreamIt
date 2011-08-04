/**
 * 
 */
package at.dms.kjc.backendSupport;

import java.util.*;

import at.dms.kjc.slir.*;


/**
 * Use greedy bin packing to allocate slices to compute nodes.
 * @author mgordon / dimock
 *
 */
public class BasicGreedyLayout<T extends ComputeNode> implements Layout<T> {
    private HashMap<InternalFilterNode, T> assignment;
    private SpaceTimeScheduleAndSlicer spaceTime;
    private int numBins;
    private LinkedList<InternalFilterNode>[] bins;
    private int[] binWeight;
    //private int[] searchOrder;
    private int totalWork;
    private T[] nodes;
    private StreamGraph slicer;
    
    /**
     * Constructor
     * @param spaceTime
     * @param nodes
     */
    public BasicGreedyLayout(SpaceTimeScheduleAndSlicer spaceTime, T[] nodes) {
        this.slicer = (StreamGraph)spaceTime.getSlicer();
        this.spaceTime = spaceTime;
        this.nodes = nodes;
        this.numBins = nodes.length;
     
        bins = new LinkedList[numBins];
        binWeight = new int[numBins];
        for (int i = 0; i < numBins; i++) {
            bins[i] = new LinkedList<InternalFilterNode>();
            binWeight[i] = 0;
        }
    }
    
    public HashMap<InternalFilterNode, T> getAssignment() {
        return assignment;
    }
    
    
    public T getComputeNode(InternalFilterNode node) {
        return assignment.get(node);
    }
   
    public void setComputeNode(InternalFilterNode node, T tile) {
        assignment.put(node, tile);
    }
    public void runLayout() {
        assignment = new HashMap<InternalFilterNode, T>();
        pack();

        System.out.println("IdealWork = " + totalWork / numBins);
        System.out.println("Greedy max tile Work Cost = " + maxBinWeight());
    }
    
    private void pack() {
        //now sort the filters by work
        LinkedList<InternalFilterNode> sortedList = new LinkedList<InternalFilterNode>();
        LinkedList<Filter> scheduleOrder;
        
    
        //if we are software pipelining then sort the traces by work
        Filter[] tempArray = (Filter[]) spaceTime.getSlicer().getSliceGraph().clone();
        Arrays.sort(tempArray, new CompareFilterWork(slicer));
        scheduleOrder = new LinkedList<Filter>(Arrays.asList(tempArray));
        //reverse the list, we want the list in descending order!
        Collections.reverse(scheduleOrder);
        

        
        for (int i = 0; i < scheduleOrder.size(); i++) {
            Filter slice = scheduleOrder.get(i);
            sortedList.add(slice.getInputNode().getNextFilter());
        }
        
        Iterator<InternalFilterNode> sorted = sortedList.iterator();
        
        //perform the packing
        while (sorted.hasNext()) {
            InternalFilterNode snode = sorted.next();
            if (snode instanceof WorkNode) {
                WorkNode fnode = (WorkNode) snode;
                int bin = findMinBin();

                bins[bin].add(fnode);
                assignment.put(fnode, nodes[bin]);
                binWeight[bin] += slicer
                        .getFilterWorkSteadyMult(fnode);
                totalWork += slicer
                        .getFilterWorkSteadyMult(fnode);
                System.out.println(" Placing: "
                        + fnode
                        + " work = "
                        + slicer.getFilterWorkSteadyMult(
                                fnode) + " on bin " + bin + ", bin work = "
                        + binWeight[bin]);
                if (snode.getPrevious() instanceof InputNode) {
                    assignment.put(snode.getPrevious(),nodes[bin]);
                }
                if (snode.getNext() instanceof OutputNode) {
                    assignment.put(snode.getNext(),nodes[bin]);
                }
            
            }

        }
    }
    
    private int findMinBin() {
        int minWeight = Integer.MAX_VALUE;
        int minBin = -1;
        for (int i = 0; i < numBins; i++) {
            //int index = searchOrder[i]; 
            if (binWeight[/*index*/ i] < minWeight) {
                minBin = /*index*/ i;
                minWeight = binWeight[/*index*/ i];
            }
        }
        return minBin;
    }
    
    /**
     * get the bin weights (Estimated max computation at each node).
     * @return
     */
    public int[] getBinWeights() {
        return binWeight;
    }
    
    private boolean gotMaxBinWeight = false;
    private int maxBinWeight;
    
    /**
     * get maximum bin weight (Estimated max computation at any node).
     * @return
     */
    public int maxBinWeight() {
        if (!gotMaxBinWeight) {
            maxBinWeight = -1;
            // find max bin
            for (int i = 0; i < numBins; i++) {
                if (binWeight[i] > maxBinWeight) {
                    maxBinWeight = binWeight[i];
                }
            }
            gotMaxBinWeight = true;
        }

        return maxBinWeight;
    }
}