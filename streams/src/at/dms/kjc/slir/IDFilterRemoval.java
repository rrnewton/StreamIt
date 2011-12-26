package at.dms.kjc.slir;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Arrays;

import at.dms.kjc.slir.fission.*;

public class IDFilterRemoval {
    private Filter idSlice;
    private InputNode idInput;
    private OutputNode idOutput;
    private InputNode[] dsInputs;
    private int[] dsInputIndices;
    private HashMap<InternalFilterNode, Integer> indexToIndex;
    private OutputNode[] usOutputs;
    private int[] usOutputIndices;
    
    public static void doit(Filter slice) {
        assert slice.getWorkNode().getWorkNodeContent() instanceof IDFilterContent : 
            "Trying to remove a non ID slice";
        
        IDFilterRemoval remover = new IDFilterRemoval(slice);
        WorkNodeInfo.reset();
    }
    
    private IDFilterRemoval(Filter s) {
        idSlice = s;
        idInput = idSlice.getInputNode();
        idOutput = idSlice.getOutputNode();

        if (!idInput.balancedInput(SchedulingPhase.INIT) ||
        		!idInput.balancedInput(SchedulingPhase.STEADY)) {
        	System.out.println("Cannot remove ID filter: " + idSlice);
        	return;
        }
        //some special cases
        if (s.getOutputNode().noOutputs() || s.getInputNode().noInputs())  
        	removeIDNoIO();
        
        removeID(SchedulingPhase.INIT);
        removeID(SchedulingPhase.STEADY);
    }
  
    
    private void removeIDNoIO() {
    	assert idSlice.getInputNode().noInputs() || idSlice.getOutputNode().noOutputs();
    	
    	// if this id slice has no i/o, then we can just remove it
    	// and the downstream filters become new roots
    	
    	for (InterFilterEdge edge : idSlice.getOutputNode().getDestSet(SchedulingPhase.STEADY)) {
    		idSlice.getParent().addTopSlice(edge.getDest().getParent());
    	}
    	
    	//if this id was a root, remove it from the list of roots
    	if (idSlice.getParent().isTopFilter(idSlice)) {
    		idSlice.getParent().removeTopSlice(idSlice);
    	}
    }
    
    private void removeID(SchedulingPhase phase) {

    	//check to see if there is buffering in the id, if so, need to change multiplicities
    	checkAndFixIDFilter(phase); 
    	//unroll 
    	unroll(phase);
    	//remove
    	remove(phase);
    	//re-roll
    	reroll(phase);
    }
    
    
    private void checkAndFixIDFilter(SchedulingPhase phase) {
    	WorkNodeInfo fi = WorkNodeInfo.getFilterInfo(idSlice.getWorkNode());
    	if (fi.totalItemsReceived(phase) > fi.totalItemsSent(phase)) {
    		
    		//System.out.println("Needed to adjust ID multiplicity to remove it: " + idSlice + " " + phase + " " + max);
    		idSlice.getWorkNode().getWorkNodeContent().setMult(phase, fi.totalItemsReceived(phase));
    		WorkNodeInfo.reset();
    	}
    }
    
    private void remove(SchedulingPhase phase) {    	
    	assert idInput.getSources(phase).length == idOutput.getDests(phase).length : idSlice.getWorkNode() + " " + phase + " " +
            "input: " + idInput.getSources(phase).length + " output: " + idOutput.getDests(phase).length;
        InterFilterEdge[] idSources = idInput.getSources(phase);
        InterFilterEdge[][] idDests = idOutput.getDests(phase);
        
        
        for (int idIndex = 0; idIndex < idSources.length; idIndex++) {
            OutputNode src = idSources[idIndex].getSrc();
            
            InputNode[] dests = new InputNode[idDests[idIndex].length];
            for (int i = 0; i < idDests[idIndex].length; i++)
                dests[i] = idDests[idIndex][i].getDest();

            //replace the ref to the id in the inputslicenodes with the src
            for (int i = 0; i < dests.length; i++) {
                replaceSrc(dests[i], src, phase);   
            }
            
            //replace the ref to id in the outputslicenode with the dests
            replaceDest(src, dests, phase);

        }
    }
    
    private void reroll(SchedulingPhase phase) {
        for (OutputNode output : usOutputs) 
            DistributionUnroller.roll(output.getParent());
        for (InputNode input : dsInputs)
            DistributionUnroller.roll(input.getParent());
    }
    
    private void unroll(SchedulingPhase phase) {
        indexToIndex = new HashMap<InternalFilterNode, Integer>();
        //unroll all the upstream output slice nodes
        LinkedList<OutputNode> outputs = new LinkedList<OutputNode>();
        for (InterFilterEdge edge : idInput.getSourceSet(phase)) {
            outputs.add(edge.getSrc());
            indexToIndex.put(edge.getSrc(), outputs.size() - 1);
            DistributionUnroller.unroll(edge.getSrc());
        }
        usOutputs = (OutputNode[])outputs.toArray(new OutputNode[0]);
        usOutputIndices = new int[usOutputs.length];
        Arrays.fill(usOutputIndices, -1);
        
        //unroll all the downstream input slice nodes
        LinkedList<InputNode> inputs = new LinkedList<InputNode>();
        for (InterFilterEdge edge : idOutput.getDestSet(phase)) {
            inputs.add(edge.getDest());
            indexToIndex.put(edge.getDest(), inputs.size() - 1);
            DistributionUnroller.unroll(edge.getDest());
        }
        dsInputs = (InputNode[])inputs.toArray(new InputNode[0]);
        dsInputIndices = new int[dsInputs.length];
        Arrays.fill(dsInputIndices, -1);
        
        //unroll the input and output of the identity 
        DistributionUnroller.unroll(idInput);
        DistributionUnroller.unroll(idOutput);
    }
    
    private void replaceDest(OutputNode output, InputNode[] dests, 
            SchedulingPhase phase) {
        assert indexToIndex.containsKey(output);
        int index = indexToIndex.get(output);
        InterFilterEdge[][] schedule = output.getDests(phase);
        
        //create all the edges
        InterFilterEdge[] destEdges = new InterFilterEdge[dests.length];
        for (int i = 0; i < dests.length; i++) {
            InterFilterEdge edge = InterFilterEdge.getEdge(output, dests[i]);
            if (edge == null)
                edge = new InterFilterEdge(output, dests[i]);
            destEdges[i] = edge;
        }
        
        int current = usOutputIndices[index] + 1;
        for (; current < schedule.length; current++) {
            //see if ID is in the current dest []
            if (containsIDDest(schedule[current])) {
                //if so, remove id from the dest[] and add destEdges
                schedule[current] = replaceIDEdge(schedule[current], destEdges);
                //remember where we stopped
                usOutputIndices[index] = current;
                return;
            }
        }
        //no need for an assert there, because we don't have to find a dest for all items produced
        //if we dont' find a dest, it will be buffered.
        //assert false : "Error in ID removal: " + idSlice;
    }
    
    private InterFilterEdge[] replaceIDEdge(InterFilterEdge[] oldDests, InterFilterEdge[] toAdd) {
        assert containsIDDest(oldDests);
        InterFilterEdge[] newEdges = new InterFilterEdge[oldDests.length + toAdd.length - 1]; 
        int index = 0;
        
        //copy over all the edges except the one to the ID
        for (int i = 0; i < oldDests.length; i++) {
            if (oldDests[i].getDest() == idInput) 
                continue;
            newEdges[index++] = oldDests[i];
        }
        //now add the edges from toAdd which point to the dests bypassing the ID filter
        for (int i = 0; i < toAdd.length; i++) {
            newEdges[index++] = toAdd[i];
        }
        
        assert index == newEdges.length;
        
        return newEdges;
    }
    
    /**
     * return true if any of the edges has the ID's input node as a dest.
     */
    private boolean containsIDDest(InterFilterEdge[] edges) {
        for (InterFilterEdge edge : edges) {
            if (edge.getDest() == idInput) 
                return true;
        }
        return false;
    }
    
    
    /**
     * Replace the next edge from ID->input in input's join schedule with 
     * the edge from output->input. 
     */
    private void replaceSrc(InputNode input, OutputNode output, 
            SchedulingPhase phase) {
        //find the index into the index array
        assert indexToIndex.containsKey(input);
        int index = indexToIndex.get(input);
        InterFilterEdge[] srcs = input.getSources(phase);
        //we might have created this edge before, so let's check
        InterFilterEdge newEdge = InterFilterEdge.getEdge(output, input);
        if (newEdge == null)  //if not, create it
            newEdge = new InterFilterEdge(output, input);
        
        InterFilterEdge oldEdge = InterFilterEdge.getEdge(idOutput, input);
        
        int current = dsInputIndices[index] + 1;
        //find the next index into the input node that received from the ID
        //and replace it with the new edge that bypasses the ID
        for (; current < srcs.length; current++) {
            if (srcs[current] == oldEdge) {
                srcs[current] = newEdge;
                //remember where we stopped
                dsInputIndices[index] = current;
                return;
            }
        }
        assert false : "Error in ID removal";
    }
}
