package at.dms.kjc.smp;

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
import at.dms.kjc.KjcOptions;
import at.dms.kjc.ObjectDeepCloner;
import at.dms.kjc.sir.SIRPopExpression;

import at.dms.kjc.backendSupport.FilterInfo;


import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.slir.InterFilterEdge;
import at.dms.kjc.slir.MutableStateExtractor;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.StreamGraph;
import at.dms.kjc.slir.fission.FissionGroup;
import at.dms.kjc.slir.fission.Fissioner;

public class StatelessFissioner {

    /** unique id generator */
    private static int uniqueID;
    private int myID;

    /** the slice we are fissing */
    private Filter slice;
    /** the amount we are fizzing slice by */
    private int fizzAmount;
    /** the filter of the slice we are fissing */
    private WorkNode filter;
    /** the filter info of the filter of the slice we are fissing */
    private FilterInfo fInfo;

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

    /** the fission products of the slice */
    private Filter[] sliceClones;

    public static FissionGroup doit(Filter slice, StreamGraph slicer, int fissAmount) {
        if(!KjcOptions.sharedbufs) {
            return Fissioner.doit(slice, slicer, fissAmount);
        }
        else {
            System.out.println("Performing fission on: " + slice.getWorkNode() + ", fizzAmount: " + fissAmount);
            StatelessFissioner fissioner = new StatelessFissioner(slice, fissAmount);
            if(canFizz(slice, false))
                return fissioner.fizz();
            return null;
        }
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
            if(debug) System.out.println("Can't fizz: Slice contains file reader/writer: " + slice);
            return false;
        }

        // Check to make sure that Slice is stateless
        if(MutableStateExtractor.hasMutableState(slice.getWorkNode().getFilter())) {
            if(debug) System.out.println("Can't fizz: Slice is not stateless: " + slice);
            return false;
        }

        // Check to see if FilterSliceNode contains a linear filter.  At the
        // moment, we can't fizz linear filters
        if(filter.getFilter().isLinear()) {
            if(debug) System.out.println("Can't fizz: Slice contains linear filter, presently unsupported: " + slice);
            return false;
        }
        
        //TODO: make sure the rates match between the slice and its inputs and the slices 
        //and its outputs

        return true;
    }

    private StatelessFissioner(Filter slice, int fizzAmount) {
        this.slice = slice;
        this.fizzAmount = fizzAmount;
        this.filter = slice.getWorkNode();
        this.fInfo = FilterInfo.getFilterInfo(filter);

        slicePeek = fInfo.peek;
        slicePop = fInfo.pop;
        slicePush = fInfo.push;
        slicePrePeek = fInfo.prePeek;
        slicePrePop = fInfo.prePop;
        slicePrePush = fInfo.prePush;
        sliceInitMult = fInfo.initMult;
        sliceSteadyMult = fInfo.steadyMult;
        sliceCopyDown = fInfo.copyDown;

        myID = uniqueID++;
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
        /*
        if(!checks())
            return null;
        */

        createFissedSlices();
        setupInitPhase();

        return new FissionGroup(slice, fInfo, sliceClones);
    }

    private void createFissedSlices() {        
        // Fill array with clones of Slice
        sliceClones = new Filter[fizzAmount];
        for(int x = 0 ; x < fizzAmount ; x++)
            sliceClones[x] = (Filter)ObjectDeepCloner.deepCopy(slice);
        
        // Give each Slice clone a unique name
        String origName = slice.getWorkNode().getFilter().getName();
        for(int x = 0 ; x < fizzAmount ; x++)
            sliceClones[x].getWorkNode().getFilter().setName(origName + "_fizz" + fizzAmount + "_clone" + x);

        // Modify name of original Slice
        slice.getWorkNode().getFilter().setName(origName + "_fizz" + fizzAmount);
        
        // Calculate new steady-state multiplicity based upon fizzAmount.  
        // Because work is equally shared among all Slice clones, steady-state 
        // multiplicity is divided by fizzAmount for each Slice clone
        int newSteadyMult = sliceSteadyMult / fizzAmount;

        for(int x = 0 ; x < fizzAmount ; x++)
            sliceClones[x].getWorkNode().getFilter().setSteadyMult(newSteadyMult);
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
         
        // Don't change 0th initialization schedule, set the rest of the slice 
        // clones' init dests to null

        for(int x = 1 ; x < fizzAmount ; x++) {
            sliceClones[x].getInputNode().setInitWeights(new int[0]);
            sliceClones[x].getInputNode().setInitSources(new InterFilterEdge[0]);
        }
        for(int x = 1 ; x < fizzAmount ; x++) {
            sliceClones[x].getOutputNode().setInitWeights(new int[0]);
            sliceClones[x].getOutputNode().setInitDests(new InterFilterEdge[0][0]);
        }
    }
}
