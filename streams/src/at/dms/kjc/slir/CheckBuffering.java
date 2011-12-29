/**
 * 
 */
package at.dms.kjc.slir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import at.dms.kjc.CType;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRIdentity;
import at.dms.kjc.sir.lowering.RenameAll;

/**
 * <p>After synch removal and conversion from the stream graph to the slice
 * graph, input slice nodes (joiners) or output slice nodes (splitter) may
 * not execute an integral number of times.</p>
 * <p>For an input slice node, each incoming edge may not have the same multiplicity 
 * and/or for an output slice node, the upstream filter may push a number of nodes 
 * that does not divide evenly by the total weights of the edges of the output node.</p> 
 * <p>Check that neither of these cases is true.  For now, if so just die.<p>
 * 
 * @author mgordon
 *
 */
public class CheckBuffering {
    private StaticSubGraph ssg;
    private HashSet<Filter> editedSlices;
    private boolean limitTiles;
    private int totalTiles;
    
    /**
     * Fix the input slice node and output slice node multiplicity to be
     * integral in the init stage.
     *  
     * @param spaceTime The space time schedule.
     */
    public static void doit(StaticSubGraph spaceTime, boolean limitTiles, int totalTiles) {
        System.out.println("Checking if init schedule is legal...");
        new CheckBuffering(spaceTime,limitTiles,totalTiles).doitInternal();
    }
    
    private CheckBuffering(StaticSubGraph st, boolean limit, int tt) {
        this.ssg = st;
        this.limitTiles = limit;
        this.totalTiles = tt;
        editedSlices = new HashSet<Filter>();
    }
    
    private void doitInternal() {
        checkOutputNodes();
        checkInputNodes();
    }
    
    /**
     * Iterate over the output slice nodes of the slice graph and 
     * see if any off them don't execute an integral number of times 
     * based on the items produced by the upstream filter.  If non-integer,
     * then create an id filter to buffer items to make it integral.
     */
    private void checkOutputNodes() {
        for (int t = 0; t < ssg.getFilterGraph().length; t++) {
            Filter slice = ssg.getFilterGraph()[t];
            //check all the outgoing edges to see if they are balanced in the init
            //and fix them if we have to
            checkOutputNode(slice.getOutputNode());
        }
        //don't forget to reset the filter infos after each iteration
        WorkNodeInfo.reset();
    }
    
    /**
     * Make sure that output slice node performs an integral number of 
     * iterations in the initialization stage.
     * 
     * @param output The output slice node
     */
    private void checkOutputNode(OutputNode output) {
        WorkNode filterNode = output.getPrevFilter();
        WorkNodeContent filter = filterNode.getWorkNodeContent();
        
        //do nothing if nothing is pushed...
        if (filter.initItemsPushed() == 0) 
            return;
        
        if (filter.initItemsPushed() % output.totalWeights(SchedulingPhase.INIT) != 0) {
           assert false : "Problem with init schedule: weights not integral of push amount for " + filter;
        }
    }
    
    
    /**
     * Make sure that all edges of an input slice node executes an equal number
     * of times.  Do this for all input slice nodes. 
     *
     */
    private void checkInputNodes() {
        //check all the edges of the slice graph to see if they are 
        //balanced
        boolean change = false;
        do {
            //System.out.println("Iteration " + i++);
            change = false;
            for (int t = 0; t < ssg.getFilterGraph().length; t++) {
                Filter slice = ssg.getFilterGraph()[t];
                //check all the outgoing edges to see if they are balanced in the init
                //and fix them if we have to
                boolean currentChange 
                    = fixInputNodeBuffering(slice.getInputNode());
                if (currentChange)
                    change = true;
            }
            //don't forget to reset the filter infos after each iteration
            WorkNodeInfo.reset();
            
        } while (change);
    }
    
    /**
     * 
     * 
     * @param input The input slice node to fix, if needed.
     * 
     * @return true if buffering was added to the correct this input
     * slice node.
     */
    private boolean fixInputNodeBuffering(InputNode input) {
        
        Iterator<InterFilterEdge> edges = input.getSourceSet(SchedulingPhase.INIT).iterator();
        HashMap<InterFilterEdge, Double> mults = new HashMap<InterFilterEdge, Double>();
        double minMult = Double.MAX_VALUE;

        //System.out.println("***** " + input + " " + input.getNext());
        
        if (!edges.hasNext())
            return false;
        
        //find the edge that has the smallest multiplicity in the
        //initialization stage
        while(edges.hasNext()) {
            InterFilterEdge edge = edges.next();
            
            double mult = 
                (initItemsPushed(edge) / input.getWeight(edge, SchedulingPhase.INIT));
            
            mults.put(edge, new Double(mult));
            //remember the min, it will be the new multiplicity for all the edges
            if (mult < minMult)
                minMult = mult;
        }
       
        //make the target minimum multiplicity an integral number 
        minMult = Math.floor(minMult);
       
        //now add buffering so that the input slice node receives exactly the
        //right amount of data from each input to perform the minimum number
        //of iterations
              
        //re-iterate over the edges and if they don't have init mult
        //minMult then add buffering by adding an upstream identity 
        //filter
        boolean changes = false;
        
        edges = input.getSourceSet(SchedulingPhase.INIT).iterator();
        while (edges.hasNext()) {
            InterFilterEdge edge = edges.next();
            
            double myMult = mults.get(edge).doubleValue();
            //if the mult is not equal to the target mult, we must buffer
            if (myMult != minMult) {
                //assert false : "Error in init stage: not all incoming edges of input node execute the same amount of times.";
            }
        }
        
        return changes;
    }
    
    /**
     * 
     * @param slice The downstream slice
     * @param incoming
     * @param itemsToPassInit
     */
    private void addNewBufferingSlice(Filter upSlice, InterFilterEdge edge, int itemsToPassInit) {
        System.out.println("Adding new buffering slice at edge: " + edge);
        CType type = edge.getType(); 
        
        //the downstream slice
        Filter downSlice = edge.getDest().getParent();
        
        //the new input of the new slice
        InputNode newInput = new InputNode(new int[]{1});
        //create the identity filter for the new slice
        SIRFilter identity = new SIRIdentity(type);
        RenameAll.renameAllFilters(identity);
        
        //create the identity filter node...
        WorkNode filter = 
            new WorkNode(new WorkNodeContent(identity));
       
        //create the new output slice node
        OutputNode newOutput = new OutputNode(new int[]{1});
        //set the intra-slice connections
        newInput.setNext(filter);
        filter.setPrevious(newInput);
        filter.setNext(newOutput);
        newOutput.setPrevious(filter);
        
        //the new slice
        Filter bufferingSlice = new Filter(ssg);
        bufferingSlice.setInputNode(newInput);
        bufferingSlice.setOutputNode(newOutput);
        bufferingSlice.setWorkNode(filter);
        bufferingSlice.finish();
        
        //create the new edge that will exist between the new slice and the
        //downstream slice
        InterFilterEdge newEdge = new InterFilterEdge(newOutput, edge.getDest());
        
        //now install the edge at the input of the downstream slice instead 
        //of the old edge
        downSlice.getInputNode().replaceEdge(edge, newEdge, SchedulingPhase.INIT);
        
        //reset the dest of the existing edge to be the new buffering slice
        edge.setDest(newInput);
                
        //set the sources and dests of the new input and new output
        newInput.setSources(new InterFilterEdge[]{edge});
        newOutput.setDests(new InterFilterEdge[][]{{newEdge}});
        
        System.out.println("   with new input: " + newInput);
        System.out.println("   with new output: " + newOutput);
        
        //set the mults of the new identity
        filter.getWorkNodeContent().setInitMult(itemsToPassInit);
        WorkNodeContent prev = upSlice.getOutputNode().getPrevFilter().getWorkNodeContent();
        
        //calc the number of steady items
        int steadyItems = (int) 
             ((prev.getSteadyMult() * prev.getPushInt()) *  
                edge.getSrc().ratio(edge, SchedulingPhase.INIT));
        
        System.out.println("   with initMult: " + itemsToPassInit + 
                ", steadyMult: " + steadyItems);
        
        filter.getWorkNodeContent().setSteadyMult(steadyItems);
       
    }
    
    /**
     * Return true if we can legally add the buffering required for slice to the
     * inside of the slice by adding a id filter to the end of the slice.
     * 
     * This checks if there are less than num_tiles filters in the slice and if
     * the buffering required will screw up any of the filters in the init stage.
     * 
     * @param slice The slice
     * @param initItemsSent The new number of items sent to the output slice node 
     * in the init stage.
     * @return True if we can add the required buffering to the inside of the slice.
     */
    private boolean legalToAddBufferingInSlice(Filter slice, int initItemsSent) {
        if (KjcOptions.greedysched || KjcOptions.noswpipe)
            return false;
        if (limitTiles && 1 >= totalTiles)
            return false;
        
        OutputNode output = slice.getOutputNode();
        
        Iterator<InterFilterEdge> edges = output.getDestSet(SchedulingPhase.INIT).iterator(); 
        while (edges.hasNext()) {
            InterFilterEdge edge = edges.next();
            WorkNodeInfo downstream = WorkNodeInfo.getFilterInfo(edge.getDest().getNextFilter());
            
            int itemsRecOnEdge = (int) (initItemsSent *
                    output.ratio(edge, SchedulingPhase.INIT));
            int itemsNeededOnEdge = (int) (downstream.initItemsNeeded * 
                    edge.getDest().ratio(edge, SchedulingPhase.INIT));
            
            if (itemsRecOnEdge < itemsNeededOnEdge) {
                System.out.println("Cannot add buffering inside slice: " + edge);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Calculate the number of items that should be passed on to the 
     * output slice node by the new ID filter, meaning the remainder that 
     * are produced by the filter are buffered by the ID.
     * 
     * @param slice The slice we are buffering.
     * @param edge The edge that requires the buffering.
     * @param inputMult The multiplicity that is now required for the edge.
     * 
     * @return The number of items that the ID must pass to the output
     * slice node.
     */
    private int itemsToPass(Filter slice, InterFilterEdge edge, double inputMult) {
        assert slice == edge.getSrc().getParent();
        OutputNode output = edge.getSrc();
        InputNode input = edge.getDest();
        
        //the number of items that should now flow over this edge 
        //in the init stage
        double edgeItems = (inputMult * input.getWeight(edge, SchedulingPhase.INIT));

        //System.out.println("  Edge Items = " + edgeItems + " = " + 
        //        inputMult +  " * " +  
        //                input.getWeight(edge));
        
        //System.out.println("  Items to pass = " + edgeItems + " * " +
        //        ((double)output.totalWeights() / 
        //                ((double)output.getWeight(edge))));
        
        //this is the number of items that need to be passed to the
        //output slice node inorder for edgeItems to flow on edge
        return (int)(((double)output.totalWeights(SchedulingPhase.INIT) / 
                ((double)output.getWeight(edge, SchedulingPhase.INIT))) *
                edgeItems);
    }
    
    
    /**
     * The number of items pushed onto this edge in the initialization
     * stage.
     * 
     * @param edge The edge.
     * @return The number of items pushed onto this edge in the initialization
     * stage.
     */
    private double initItemsPushed(InterFilterEdge edge) {
        return edge.getSrc().getPrevFilter().getWorkNodeContent().initItemsPushed() *
        edge.getSrc().ratio(edge, SchedulingPhase.INIT);
    }
}
