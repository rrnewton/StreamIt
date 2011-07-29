package at.dms.kjc.slir.fission;

import java.util.LinkedList;

import at.dms.kjc.CClassType;
import at.dms.kjc.CStdType;
import at.dms.kjc.CType;
import at.dms.kjc.JAddExpression;
import at.dms.kjc.JAssignmentExpression;
import at.dms.kjc.JBlock;
import at.dms.kjc.JEmptyStatement;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JForStatement;
import at.dms.kjc.JFormalParameter;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JLocalVariableExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JRelationalExpression;
import at.dms.kjc.JVariableDeclarationStatement;
import at.dms.kjc.JVariableDefinition;
import at.dms.kjc.ObjectDeepCloner;
import at.dms.kjc.backendSupport.FilterInfo;
import at.dms.kjc.sir.SIRPopExpression;
import at.dms.kjc.slir.*;

public class Fissioner {
    private static int uniqueID;
    /** the slice we are fissing */
    private Filter slice;
    /** the amount we are fizzing slice by */
    private int fizzAmount;
    /** the filter of the slice we are fissing */
    private WorkNode filter;
    /** the filter info of the filter of the slice we are fissing */
    private FilterInfo fInfo;
    /** the identity slice inserted downstream fo the fizzed slices */
    private Filter idOutput;
    /** the identity slice inserted upstream to the fizzed slices */
    private Filter idInput;
    /** the fission products of the slice */
    private Filter[] sliceClones;
    private Filter[] inputsInit;
    private Filter[] inputsSteady;
    private Filter[] outputsInit;
    private Filter[] outputsSteady;
    /** the stats from the original filter, these don't change! */
    private int slicePeek;
    private int slicePop;
    private int slicePush;
    private int slicePrePeek;
    private int slicePrePop;
    private int slicePrePush;
    private int sliceInitMult;
    private int sliceSteadyMult;
    private int sliceCopyDown;
    /** the pop rate (also peek) of the clones) */
    private int newPop; 
    /** the push rate of the clones (multS of slice * push of slice) */
    private int newPush;
    private int myID;
    private boolean isSourceSlice;
    private Slicer slicer;
    
    static {
        uniqueID = 0;
    }
    
    /**
     * Attempt to fiss <slice> by <fissAmount>.  Return true if the fission was successful.
     */
    public static FissionGroup doit(Filter slice, Slicer slicer, int fissAmount) {
        System.out.println("Performing fission on: " + slice.getWorkNode() + ", fizzAmount: " + fissAmount);
        Fissioner fissioner = new Fissioner(slice, slicer, fissAmount);
        if(canFizz(slice, false)) 
            return fissioner.fizz();
        return null;
    }
    
    /**
     * Return true if <slice> can be fissed, meaning it is stateless.  The method 
     * does not check that the schedule allows for fission.
     */
    public static boolean canFizz(Filter slice, boolean debug) {

        // Get information on Slice rates
        FilterInfo.reset();

        WorkNode filter = slice.getWorkNode();
        
        // Check to see if Slice has file reader/writer.  Don't fizz file
        // reader/writer
        if(filter.isPredefined()) {
            if(debug) System.out.println("Can't fizz: Slice contains file reader/writer");
            return false;
        }

        // Check to make sure that Slice is stateless
        if(MutableStateExtractor.hasMutableState(slice.getWorkNode().getFilter())) {
            if(debug) System.out.println("Can't fizz: Slice is not stateless!!");
            return false;
        }

        // Check to see if FilterSliceNode contains a linear filter.  At the
        // moment, we can't fizz linear filters
        if(filter.getFilter().isLinear()) {
            if(debug) System.out.println("Can't fizz: Slice contains linear filter, presently unsupported");
            return false;
        }
        
        //TODO: make sure the rates match between the slice and its inputs and the slices 
        //and its outputs

        return true;
    }

    private Fissioner(Filter s, Slicer slicer, int d) {
        this.slicer = slicer;
        // reset the filter info's just in case things have change
        FilterInfo.reset();

        //we need to know if this is a source slice, if so add the 
        //copies to the slicer's top slices, roots of the forrest
        if (slicer.isTopSlice(s))
            isSourceSlice = true;
        else    
            isSourceSlice = false;
        
        myID = uniqueID++;
        
        this.slice = s;
        this.fizzAmount = d;
        this.fInfo = FilterInfo.getFilterInfo(s.getWorkNode());
        this.filter = slice.getWorkNode();
        slicePeek = fInfo.peek;
        slicePop = fInfo.pop;
        slicePush = fInfo.push;

        slicePrePeek = fInfo.prePeek;
        slicePrePop = fInfo.prePop;
        slicePrePush = fInfo.prePush;

        sliceInitMult = fInfo.initMult;
        sliceSteadyMult = fInfo.steadyMult;
        sliceCopyDown = fInfo.copyDown;
        int i = 0;
        
        inputsInit = new Filter[s.getInputNode().getSourceSet(SchedulingPhase.INIT).size()];
        i = 0;
        for (InterFilterEdge edge : s.getInputNode().getSourceSet(SchedulingPhase.INIT)) {
            inputsInit[i++] = edge.getSrc().getParent();
        }

        inputsSteady = new Filter[s.getInputNode().getSourceSet(SchedulingPhase.STEADY).size()];
        i = 0;
        for (InterFilterEdge edge : s.getInputNode().getSourceSet(SchedulingPhase.STEADY)) {
            inputsSteady[i++] = edge.getSrc().getParent();
        }
        
        outputsInit = new Filter[s.getOutputNode().getDestSet(SchedulingPhase.INIT).size()];
        i = 0; 
        for (InterFilterEdge edge : s.getOutputNode().getDestSet(SchedulingPhase.INIT)) {
            outputsInit[i++] = edge.getDest().getParent();
        }
        
        outputsSteady = new Filter[s.getOutputNode().getDestSet(SchedulingPhase.STEADY).size()];
        i = 0; 
        for (InterFilterEdge edge : s.getOutputNode().getDestSet(SchedulingPhase.STEADY)) {
            outputsSteady[i++] = edge.getDest().getParent();
        }
    }
    
    private boolean checks() {
        // Check copyDown constraint: copyDown < mult * pop
        if  (fInfo.pop > 0 && fInfo.copyDown >= fInfo.steadyMult * fInfo.pop / fizzAmount) { 
            System.out.println("Can't fizz: Slice does not meet copyDown constraint");
            return false;
        }
                   
        return true;
    }
    
    private FissionGroup fizz() {
        if (!checks())
            return null;

        createFissedSlices();
        createIDInputSlice();
        createIDOutputSlice();
        setupInitPhase();
        replaceInputEdges(SchedulingPhase.INIT); replaceInputEdges(SchedulingPhase.STEADY);
        moveJoinToInputID();
        installFissionSplitPattern();
        installSplitJoinIDOutput();
        replaceOutputEdges(SchedulingPhase.INIT); replaceOutputEdges(SchedulingPhase.STEADY);
        //debug();
        synchRemoveIDs();
        
        return new FissionGroup(slice, fInfo, sliceClones);
    }
    
    /**
     * Replace the incoming edges of <phase> for each of the outputs from the original slice
     * so that edges that referenced the original slice now reference idOutput as their source.
     */
    private void replaceOutputEdges(SchedulingPhase phase) {
        Filter[] outputs = (phase == SchedulingPhase.INIT ? outputsInit : outputsSteady);
        
        for (int i = 0; i < outputs.length; i++) {
            InterFilterEdge oldEdge = getEdge(slice, outputs[i]);
            InterFilterEdge newEdge = getEdge(idOutput, outputs[i]);
            InterFilterEdge[] srcs = outputs[i].getInputNode().getSources(phase);
            srcs = replaceEdge(srcs, oldEdge, newEdge);
            int[] weights = outputs[i].getInputNode().getWeights(phase);
            
            if (phase == SchedulingPhase.INIT) {
                outputs[i].getInputNode().setInitSources(srcs);
                outputs[i].getInputNode().setInitWeights(weights);
            }
            else {
                outputs[i].getInputNode().setSources(srcs);
                outputs[i].getInputNode().setWeights(weights);
            }
        }
    }
    
    /**
     * Create the splitting and joining patterns for the output ID.  The joining pattern 
     * is just a round-robin of the clones with weight equal to the new push rate of each.
     * The splitting pattern is the splitting pattern of the original slice with the edges replaced
     * to reference the output ID.
     */
    private void installSplitJoinIDOutput() {
        //set the join of id output to a round robin of the clones with weight
        //equal to their push rate
        InterFilterEdge[] incoming = new InterFilterEdge[fizzAmount];
        int[] inWeights = new int[fizzAmount];
        for (int i = 0; i < fizzAmount; i++) {
            inWeights[i] = newPush;
            incoming[i] = getEdge(sliceClones[i], idOutput);
        }
        
        //install them
        idOutput.getInputNode().setWeights(inWeights);
        idOutput.getInputNode().setSources(incoming);
        
        //set the split of idOutput to the original filter's split pattern
        //with the src of the edges replaced with idOutput
        InterFilterEdge[][] newDests = slice.getOutputNode().getDests(SchedulingPhase.STEADY);
        int newWeights[] = slice.getOutputNode().getWeights(SchedulingPhase.STEADY).clone();
        
        for (int i = 0; i < outputsSteady.length; i++) {
            InterFilterEdge oldEdge = getEdge(slice, outputsSteady[i]);
            InterFilterEdge newEdge = getEdge(idOutput, outputsSteady[i]);
            
            newDests = replaceEdge(newDests, oldEdge, newEdge);
        }
        
        assert newDests.length == newWeights.length;
        
        idOutput.getOutputNode().setWeights(newWeights);
        idOutput.getOutputNode().setDests(newDests);


        //if the original had a separate init pattern, we have to perform the search/replace 
        //of edges on it also
        InterFilterEdge[][] newDestsInit = slice.getOutputNode().getDests(SchedulingPhase.INIT);
        int newWeightsInit[] = slice.getOutputNode().getWeights(SchedulingPhase.INIT).clone();

        for (int i = 0; i < outputsInit.length; i++) {
            InterFilterEdge oldEdge = getEdge(slice, outputsInit[i]);
            InterFilterEdge newEdge = getEdge(idOutput, outputsInit[i]);

            newDestsInit = replaceEdge(newDestsInit, oldEdge, newEdge);
        }

        assert newDestsInit.length == newWeightsInit.length;

        idOutput.getOutputNode().setInitWeights(newWeightsInit);
        idOutput.getOutputNode().setInitDests(newDestsInit);
    }

    /**
     * Calculate and install the splitting pattern for the id intput to the slice clones.
     * This is based on the number of filters we are duplicating to.  See PLDI paper for 
     * more explanation.
     */
    private void installFissionSplitPattern() {
        //calculate the fission split pattern for duplication to as most 2 slices
        //the duplication factor
        int dup = slicePeek - slicePop;
        int extra = 1;
        if (sliceCopyDown - dup == 0) {
            extra = 0;
        }
        
        InterFilterEdge[][] dests = new InterFilterEdge[2 * fizzAmount + extra][];
        int weights[] = new int[2 * fizzAmount + extra];
        //the first weight clone's pop - copydown - dup, so just what is for this filter minus 
        //what is already in the buffer (copydown)
        weights[0] = newPop - sliceCopyDown - dup;
        //the first dest is just the first filter
        dests[0] = new InterFilterEdge[]{getEdge(idInput, sliceClones[0])};
        //the second weight is just the duplication factor
        weights[1] = dup;
        //second dests are the first and second filter
        dests[1] = new InterFilterEdge[]{getEdge(idInput, sliceClones[0]), getEdge(idInput, sliceClones[1])};
        
        //generate the middle dests and weights
        for (int i = 2; i < 2 * fizzAmount; i += 2) {
            weights[i] = newPop - dup - dup;
            dests[i] =  new InterFilterEdge[]{getEdge(idInput, sliceClones[i/2])};
            //now take care of the duplication to neighboring slices
            weights[i + 1] = dup;
            // the second dest of the duplication has to wrap around on the last index
            int secondDest = (i/2) + 1;
            if (secondDest >= fizzAmount) 
                secondDest = 0  ;
            dests[i + 1] = new InterFilterEdge[]{getEdge(idInput, sliceClones[i/2]), 
                    getEdge(idInput, sliceClones[secondDest])};
        }

        if (extra == 1) {
            //now take care of the last weight and dest
            weights[2 * fizzAmount] = sliceCopyDown - dup;
            dests[2 * fizzAmount] = new InterFilterEdge[]{getEdge(idInput, sliceClones[0])};
        }

        //install the weights and edges for the steady state
        idInput.getOutputNode().setWeights(weights);
        idInput.getOutputNode().setDests(dests);
    }
    
    /**
     * Move slice's joining schedule to the ID and replace the references to slice with idInput
     * in the edges.
     */
    private void moveJoinToInputID() {
        InterFilterEdge[] joining = slice.getInputNode().getSources(SchedulingPhase.STEADY);
        InterFilterEdge[] newJoin = new InterFilterEdge[joining.length];
        
        for (int i = 0; i < joining.length; i++) {
            assert joining[i].getDest() == slice.getInputNode();
            InterFilterEdge newEdge = getEdge(joining[i].getSrc().getParent(), idInput);
            newJoin[i] = newEdge;
        }
        
        int[] newWeights = slice.getInputNode().getWeights(SchedulingPhase.STEADY).clone();
        
        idInput.getInputNode().setSources(newJoin);
        idInput.getInputNode().setWeights(newWeights);


        //if we have an init pattern, we have to replace the edges
        InterFilterEdge[] joinInit = slice.getInputNode().getSources(SchedulingPhase.INIT);
        InterFilterEdge[] newJoinInit = new InterFilterEdge[joinInit.length];

        for (int i = 0; i < joinInit.length; i++) {
            assert joinInit[i].getDest() == slice.getInputNode();
            InterFilterEdge newEdge = getEdge(joinInit[i].getSrc().getParent(), idInput);
            newJoinInit[i] = newEdge;
        }

        int[] newWeightsInit = slice.getInputNode().getWeights(SchedulingPhase.INIT).clone();

        idInput.getInputNode().setInitSources(newJoinInit);
        idInput.getInputNode().setInitWeights(newWeightsInit);

    }
    
    /**
     * Query/Replace the edges to original slice with edges to the new input ID in the 
     * outputs of the inputs.  
     */
    private void replaceInputEdges(SchedulingPhase phase) {
        Filter[] inputs = (phase == SchedulingPhase.INIT ? inputsInit : inputsSteady);
        
        for (int i = 0; i < inputs.length; i++) {

            InterFilterEdge edge = getEdge(inputs[i], idInput);
            InterFilterEdge oldEdge = getEdge(inputs[i], slice);
            InterFilterEdge[][] newEdges = replaceEdge(inputs[i].getOutputNode().getDests(phase), oldEdge, edge);
            int[] weights = inputs[i].getOutputNode().getWeights(phase).clone();
            
            if (SchedulingPhase.INIT == phase) {
                inputs[i].getOutputNode().setInitDests(newEdges);
                inputs[i].getOutputNode().setInitWeights(weights);
            }
            else {
                inputs[i].getOutputNode().setDests(newEdges);
                inputs[i].getOutputNode().setWeights(weights);
            }
        }
    }
            
    
    private void setupInitPhase() {
        /*
         * The unfizzed Slice has both an initialization phase and a steady-
         * state phase.  Once the Slice is fizzed, only one of the Slice clones
         * needs to execute the initialization phase.
         *
         * We have chosen that the first Slice clone be the clone to handle
         * initialization.  The remaining Slice clones are simply disabled
         * during initialization.
         */

        // For the first Slice clone, move initialization work into prework.
        // This involves copying the work body into the prework after the
        // original prework body.  The initialization multiplicity is rolled
        // into a loop around the copied work body.
        int newPrePeek = slicePrePeek;
        int newPrePop = slicePrePop;
        int newPrePush = slicePrePush;
        int newInitMult = sliceInitMult;
        
        //new initMult only counts the calls to work in the init stage
        //and if this filter is a 2stage, then the first call is to prework
        if (fInfo.isTwoStage()) {
            newInitMult--;
        }
        
        if(sliceInitMult > 0) {
            JBlock firstWorkBody =
                slice.getWorkNode().getFilter().getWork().getBody();
            
            JBlock newPreworkBody = new JBlock();
            
            if(slice.getWorkNode().getFilter().getPrework() != null &&
               slice.getWorkNode().getFilter().getPrework().length > 0 &&
               slice.getWorkNode().getFilter().getPrework()[0] != null &&
               slice.getWorkNode().getFilter().getPrework()[0].getBody() != null) {
                newPreworkBody.addStatement(slice.getWorkNode().getFilter().getPrework()[0].getBody());
            }

            JVariableDefinition initMultLoopVar =
                new JVariableDefinition(0,
                                        CStdType.Integer,
                                        "initMultCount",
                                        new JIntLiteral(0));

            JVariableDeclarationStatement initMultLoopVarDecl = new JVariableDeclarationStatement(initMultLoopVar);
            newPreworkBody.addStatementFirst(initMultLoopVarDecl);
            
            JRelationalExpression initMultLoopCond =
                new JRelationalExpression(JRelationalExpression.OPE_LT,
                                          new JLocalVariableExpression(initMultLoopVar),
                                          new JIntLiteral(newInitMult));
            
            JExpressionStatement initMultLoopIncr =
                new JExpressionStatement(new JAssignmentExpression(new JLocalVariableExpression(initMultLoopVar),
                                                                   new JAddExpression(new JLocalVariableExpression(initMultLoopVar),
                                                                                      new JIntLiteral(1))));
            
            JForStatement initMultLoop =
                new JForStatement(new JEmptyStatement(),
                                  initMultLoopCond,
                                  initMultLoopIncr,
                                  (JBlock)ObjectDeepCloner.deepCopy(firstWorkBody));
            //don't unroll
            initMultLoop.setUnrolled(true);
            newPreworkBody.addStatement(initMultLoop);
            
            ///if slice did not have an existing prework, create it and install it at sliceClone[0]
            if(slice.getWorkNode().getFilter().getPrework() == null ||
               slice.getWorkNode().getFilter().getPrework().length == 0 ||
               slice.getWorkNode().getFilter().getPrework()[0] == null) {
                JMethodDeclaration newPreworkMethod =
                    new JMethodDeclaration(null,
                                           at.dms.kjc.Constants.ACC_PUBLIC,
                                           CStdType.Void,
                                           "__fission_prework__" + myID,
                                           JFormalParameter.EMPTY,
                                           CClassType.EMPTY,
                                           newPreworkBody,
                                           null,
                                           null);

                sliceClones[0].getWorkNode().getFilter().setPrework(newPreworkMethod);

                newPrePeek = 0;
                newPrePush = 0;
                newPrePop = 0;
            }
            else {
                sliceClones[0].getWorkNode().getFilter().getPrework()[0].setBody(newPreworkBody);
            }
            
            // For the first Slice clone, adjust prework rates to reflect that 
            // initialization work was moved into prework
            
            newPrePeek = Math.max(slicePrePeek,
                                    slicePrePop + (newInitMult * slicePop) + (slicePeek - slicePop));
            newPrePop = slicePrePop + newInitMult * slicePop;
            newPrePush = slicePrePush + newInitMult * slicePush;
            
            sliceClones[0].getWorkNode().getFilter().getPrework()[0].setPeek(newPrePeek);
            sliceClones[0].getWorkNode().getFilter().getPrework()[0].setPop(newPrePop);
            sliceClones[0].getWorkNode().getFilter().getPrework()[0].setPush(newPrePush);
            
            // Since the initialization work has been moved into prework, set
            // the initialization multiplicity of the first Slice clone to 0
            
            sliceClones[0].getWorkNode().getFilter().setInitMult(1);
            
            
        }

        // Disable all other Slice clones in initialization.  This involves
        // disabling prework and seting initialization multiplicty to 0

        for(int x = 1 ; x < fizzAmount ; x++) {
            sliceClones[x].getWorkNode().getFilter().setPrework(null);
            sliceClones[x].getWorkNode().getFilter().setInitMult(0);
        }

        // Since only the first Slice clone executes, it will be the only Slice
        // clone to receive during initialization.
        //
        // If there are multiple source Slices, it is assumed that only the 
        // first source Slice will execute during initialization.  Only the 
        // first source Slice will transmit during initialization.
        //
        // Setup the splitter-joiner schedules to reflect that only the first
        // source Slice transmits and that only the first Slice clone receives.
 
        
        //set the incoming schedule of the 0th clone to receive from the input ID
        InterFilterEdge idTo0 = getEdge(idInput, sliceClones[0]);
        sliceClones[0].getInputNode().setInitWeights(new int[]{1});
        sliceClones[0].getInputNode().setInitSources(new InterFilterEdge[]{idTo0});
        
        //set the outgoing schedule of the 0th clone to send to the output ID
        InterFilterEdge toId = getEdge(sliceClones[0], idOutput);
        sliceClones[0].getOutputNode().setInitWeights(new int[]{1});
        InterFilterEdge[] outgoing = {toId};
        sliceClones[0].getOutputNode().setInitDests(new InterFilterEdge[][]{outgoing});
        
        //set the rest of the slice clones' init dists to null
        for(int x = 1 ; x < fizzAmount ; x++) {
            sliceClones[x].getInputNode().setInitWeights(null);
            sliceClones[x].getInputNode().setInitSources(null);
        }
        for(int x = 1 ; x < fizzAmount ; x++) {
            sliceClones[x].getOutputNode().setInitWeights(null);
            sliceClones[x].getOutputNode().setInitDests(null);
        }
    }
    
    private InterFilterEdge getEdge(Filter s1, Filter s2) {
        InterFilterEdge edge = InterFilterEdge.getEdge(s1.getOutputNode(), s2.getInputNode());
        if (edge == null)
            edge = new InterFilterEdge(s1.getOutputNode(), s2.getInputNode());
        
        return edge;
    }
    
    private void debug() {
        System.out.println("--- INIT ---");
        System.out.println("--- Inputs ---");
        for (int i = 0; i < inputsInit.length; i++) {
            System.out.println(inputsInit[i].getOutputNode().debugString(false, SchedulingPhase.INIT));
        }
        System.out.println("--- IDInput ---");
        System.out.println(idInput.getInputNode().debugString(false, SchedulingPhase.INIT));
        System.out.println(idInput.getOutputNode().debugString(false, SchedulingPhase.INIT));
        System.out.println("--- Clones ---");
        for (int i = 0; i < sliceClones.length; i++) {
            System.out.println(sliceClones[i].getInputNode().debugString(false, SchedulingPhase.INIT));
            System.out.println(sliceClones[i].getOutputNode().debugString(false, SchedulingPhase.INIT));
            
        }
        System.out.println("--- IdOutput ---");
        System.out.println(idOutput.getInputNode().debugString(false, SchedulingPhase.INIT));
        System.out.println(idOutput.getOutputNode().debugString(false, SchedulingPhase.INIT));
        System.out.println("--- Outputs ---");
        for (int i = 0; i < outputsInit.length; i++) {
            System.out.println(outputsInit[i].getInputNode().debugString(false, SchedulingPhase.INIT));
        }
        
        System.out.println("CopyDown: " + sliceCopyDown);
        System.out.println("d: " + (slicePeek - slicePop));
        System.out.println("O_clone: " + (sliceSteadyMult / fizzAmount * slicePop + (slicePeek - slicePop)));
        
        System.out.println("--- STEADY ---");
        System.out.println("--- Inputs ---");
        for (int i = 0; i < inputsSteady.length; i++) {
            System.out.println(inputsSteady[i].getOutputNode().debugString(false, SchedulingPhase.STEADY));
        }
        System.out.println("--- IDInput ---");
        System.out.println(idInput.getInputNode().debugString(false, SchedulingPhase.STEADY));
        System.out.println(idInput.getOutputNode().debugString(false, SchedulingPhase.STEADY));
        System.out.println("--- Clones ---");
        for (int i = 0; i < sliceClones.length; i++) {
            System.out.println(sliceClones[i].getInputNode().debugString(false, SchedulingPhase.STEADY));
            System.out.println(sliceClones[i].getOutputNode().debugString(false, SchedulingPhase.STEADY));
            
        }
        System.out.println("--- IdOutput ---");
        System.out.println(idOutput.getInputNode().debugString(false, SchedulingPhase.STEADY));
        System.out.println(idOutput.getOutputNode().debugString(false, SchedulingPhase.STEADY));
        System.out.println("--- Outputs ---");
        for (int i = 0; i < outputsSteady.length; i++) {
            System.out.println(outputsSteady[i].getInputNode().debugString(false, SchedulingPhase.STEADY));
        }
    }
    
    /**
     * Remove the generated IDs by running the synch removal pass.  
     * After this we are done.
     */
    private void synchRemoveIDs() {
        FilterInfo.reset();
        FilterInfo idI = FilterInfo.getFilterInfo(idInput.getWorkNode());
        FilterInfo idO = FilterInfo.getFilterInfo(idOutput.getWorkNode());

        assert idI.copyDown == 0 : idI.copyDown;
        assert idO.copyDown == 0;
        IDFilterRemoval.doit(idInput);
        IDFilterRemoval.doit(idOutput);
    }
    
    private InterFilterEdge[][] replaceEdge(InterFilterEdge[][] oldEdges, 
            InterFilterEdge oldEdge, InterFilterEdge newEdge) {
        if (oldEdges == null)
            return null;
        
        InterFilterEdge[][] newEdges = new InterFilterEdge[oldEdges.length][];
        
        for (int i = 0; i < newEdges.length; i++) {
            InterFilterEdge[] ret = replaceEdge(oldEdges[i], oldEdge, newEdge);
            newEdges[i] = ret;
        }
        
        return newEdges;
    }
    
    private InterFilterEdge[] replaceEdge(InterFilterEdge[] oldEdges, 
            InterFilterEdge oldEdge, InterFilterEdge newEdge) {
        if (oldEdges == null)
            return null;
        
        InterFilterEdge[] newEdges = new InterFilterEdge[oldEdges.length];
        
        for (int i = 0; i < newEdges.length; i++) {
            if (oldEdges[i] == oldEdge) 
                newEdges[i] = newEdge;
            else
                newEdges[i] = oldEdges[i];
        }
                
        return newEdges;
    }
    
    private void createIDInputSlice() {
        //create the ID slice
        idInput = IDFilterContent.createIDSlice();
        
        //set the init mult of the id
        int items = 0;
        for (int i = 0; i < inputsInit.length; i++) {
            InterFilterEdge edge = InterFilterEdge.getEdge(inputsInit[i].getOutputNode(), slice.getInputNode());
            items += inputsInit[i].getOutputNode().itemsSentOn(edge, SchedulingPhase.INIT);
        }
        idInput.getWorkNode().getFilter().setInitMult(items);
        //set up the init weight and init dest, remember we only send to the left most clone in the init
        if (items > 0) {
            idInput.getOutputNode().setInitWeights(new int[]{1});
            idInput.getOutputNode().setInitDests(new InterFilterEdge[][]{{getEdge(idInput, sliceClones[0])}});
        }
        
        //set the steady mult of the filter
        //check to make sure that we pop all we receive in the steady
        int totalItemsReceived = 0;
        for (int i = 0; i < inputsSteady.length; i++) {
            InterFilterEdge edge = InterFilterEdge.getEdge(inputsSteady[i].getOutputNode(), slice.getInputNode());
            totalItemsReceived += inputsSteady[i].getOutputNode().itemsSentOn(edge, SchedulingPhase.STEADY);
        }
        assert fInfo.steadyMult * fInfo.pop == totalItemsReceived;
        idInput.getWorkNode().getFilter().setSteadyMult(fInfo.steadyMult * fInfo.pop);
        
        //set the join schedule for each of the clones to just receive from the ID
        for (int i = 0; i < sliceClones.length; i++) {
            InterFilterEdge edge = getEdge(idInput, sliceClones[i]);
            sliceClones[i].getInputNode().setWeights(new int[]{1});
            sliceClones[i].getInputNode().setSources(new InterFilterEdge[]{edge});
        }
    }

    private void createIDOutputSlice() {
        idOutput = IDFilterContent.createIDSlice();
        
        //set the init mult of the id
        int initItems = fInfo.totalItemsSent(SchedulingPhase.INIT);
        idOutput.getWorkNode().getFilter().setInitMult(initItems);
        //set the joining dist of the output id to just receive from the left most clone in init
        if (initItems > 0) {
            idOutput.getInputNode().setInitWeights(new int[]{1});
            idOutput.getInputNode().setInitSources(new InterFilterEdge[]{getEdge(sliceClones[0], idOutput)});
        }
               
        //set the steadymult of the id
        int steadyItems = fInfo.totalItemsSent(SchedulingPhase.STEADY);
        idOutput.getWorkNode().getFilter().setSteadyMult(steadyItems);
        
        /*
        int items = 0;
        for (int i = 0; i < outputsSteady.length; i++) {
            InterSliceEdge edge = InterSliceEdge.getEdge(slice.getTail(), outputsSteady[i].getHead());
            items += outputsSteady[i].getHead().itemsReceivedOn(edge, SchedulingPhase.STEADY);
        }
        assert items == steadyItems : items + " != " + steadyItems + " " + slice;
        */
        
        //set the split schedule of each of the clones to just send to the id output
        for (int i = 0; i < sliceClones.length; i++) {
            InterFilterEdge[] edge =  {getEdge(sliceClones[i], idOutput)};
            sliceClones[i].getOutputNode().setWeights(new int[]{1});
            sliceClones[i].getOutputNode().setDests(new InterFilterEdge[][]{edge});
        }
    }
    
    private void createFissedSlices() {
        
        // Fill array with clones of Slice, put original copy first in array
        sliceClones = new Filter[fizzAmount];
        for(int x = 0 ; x < fizzAmount ; x++)
            sliceClones[x] = (Filter)ObjectDeepCloner.deepCopy(slice);

        //if this was a top slice, we need to remove it and add the clones
        if (isSourceSlice) {
            slicer.removeTopSlice(slice);
            for(int x = 0 ; x < fizzAmount ; x++)
                slicer.addTopSlice(sliceClones[x]);
        }
        
        // Give each Slice clone a unique name
        String origName = slice.getWorkNode().getFilter().getName();
        for(int x = 0 ; x < fizzAmount ; x++)
            sliceClones[x].getWorkNode().getFilter().setName(origName + "_fizz" + x);
        
        // Calculate new steady-state multiplicity based upon fizzAmount.  
        // Because work is equally shared among all Slice clones, steady-state 
        // multiplicity is divided by fizzAmount for each Slice clone

        int newSteadyMult = sliceSteadyMult / fizzAmount;

        for(int x = 0 ; x < fizzAmount ; x++)
            sliceClones[x].getWorkNode().getFilter().setSteadyMult(newSteadyMult);


        /**********************************************************************
         *               Roll steady-state multiplicity into loop             *
         **********************************************************************/

        /*
         * To assist code generation, the steady-state multiplicity is rolled
         * into a loop around the work body of each Slice clone.  The steady-
         * state multiplicity of each Slice clone is then set to 1.  
         *
         * The amount of work completed in each execution of a Slice clone stays
         * constant through this transform.  The main benefit of this transform
         * is that code can now be added to execute at the end of each steady-
         * state iteration.
         *
         * This capability is needed in order to remove unneeded elements from
         * each Slice clone at the end of every steady-state iteration.
         *
         * Unfortunately, this transform breaks the initialization multiplicity.
         * Fortunately, the initialization multiplicity is no longer needed at
         * this point since the initialization work has been copied into
         * prework.
         */

        // Get the work body for each Slice
        JBlock origWorkBodies[] = new JBlock[fizzAmount];

        for(int x = 0 ; x < fizzAmount ; x++)
            origWorkBodies[x] =
                sliceClones[x].getWorkNode().getFilter().getWork().getBody();

        // Roll the steady-state multiplicity into a loop around the work
        // body of each Slice.
        for(int x = 0 ; x < fizzAmount ; x++) {

            // Construct new work body
            JBlock newWorkBody = new JBlock();

            // Add declaration for for-loop counter variable
            JVariableDefinition steadyMultLoopVar =
                new JVariableDefinition(0, 
                                        CStdType.Integer,
                                        "steadyMultCount",
                                        new JIntLiteral(0));

            JVariableDeclarationStatement steadyMultLoopVarDecl = new JVariableDeclarationStatement(steadyMultLoopVar);
            newWorkBody.addStatement(steadyMultLoopVarDecl);

            // Add for-loop that wraps around existing work body
            JRelationalExpression steadyMultLoopCond =
                new JRelationalExpression(JRelationalExpression.OPE_LT,
                                          new JLocalVariableExpression(steadyMultLoopVar),
                                          new JIntLiteral(newSteadyMult));

            JExpressionStatement steadyMultLoopIncr = 
                new JExpressionStatement(new JAssignmentExpression(new JLocalVariableExpression(steadyMultLoopVar),
                                                                   new JAddExpression(new JLocalVariableExpression(steadyMultLoopVar),
                                                                                      new JIntLiteral(1))));

            JForStatement steadyMultLoop =
                new JForStatement(new JEmptyStatement(),
                                  steadyMultLoopCond,
                                  steadyMultLoopIncr,
                                  (JBlock)ObjectDeepCloner.deepCopy(origWorkBodies[x]));
            //don't unroll
            steadyMultLoop.setUnrolled(true);
            newWorkBody.addStatement(steadyMultLoop);

            // Set new work body
            sliceClones[x].getWorkNode().getFilter().getWork().setBody(newWorkBody);
        }

        // Now that steady-state multiplicity has been rolled around the work
        // bodies of the Slices, change steady-state multiplicity to 1.
        // Recalculate new Slice rates given new steady-state multiplicity.
            
        int newPeek = slicePop * newSteadyMult + slicePeek - slicePop;
        newPop = slicePop * newSteadyMult;
        newPush = slicePush * newSteadyMult;

        for(int x = 0 ; x < fizzAmount ; x++) {
            sliceClones[x].getWorkNode().getFilter().setSteadyMult(1);
            sliceClones[x].getWorkNode().getFilter().getWork().setPeek(newPeek);
            sliceClones[x].getWorkNode().getFilter().getWork().setPop(newPop);
            sliceClones[x].getWorkNode().getFilter().getWork().setPush(newPush);
        }
        
        /**********************************************************************
         *                 Perform fission hacks on Slice rates               *
         **********************************************************************/
        
        // Normally, Slices remember peek - pop elements between steady-state
        // iterations.  However, after fizzing, these elements no longer need to
        // be remembered between iterations.  These elements therefore need to 
        // be removed at the end of each steady-state iteration
        //
        // This code adds a pop statement to the end of each work body, removing
        // the unneeded peek - pop elements.  The code also adjusts the pop rate
        // to reflect that more elements are being popped.

        if(newPeek -  newPop > 0) {
            // Add pop statement to end of each work body
            for(int x = 0 ; x < fizzAmount ; x++) {
                CType inputType = 
                    sliceClones[x].getWorkNode().getFilter().getInputType();
                
                SIRPopExpression popExpr =
                    new SIRPopExpression(inputType, newPeek - newPop);
                JExpressionStatement popStmnt =
                    new JExpressionStatement(popExpr);
                
                sliceClones[x].getWorkNode().getFilter().getWork().getBody()
                    .addStatement(popStmnt);
            }

            // Adjust pop rates since more elements are now popped
            newPop += (newPeek - newPop);

            for(int x = 0 ; x < fizzAmount ; x++)
                sliceClones[x].getWorkNode().getFilter().getWork().setPop(newPop);
        }
    }
    
}
