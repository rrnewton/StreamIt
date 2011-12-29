package at.dms.kjc.smp;

import at.dms.kjc.KjcOptions;
import at.dms.kjc.ObjectDeepCloner;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InterFilterEdge;
import at.dms.kjc.slir.MutableStateExtractor;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.slir.WorkNodeInfo;
import at.dms.kjc.slir.fission.FissionGroup;
import at.dms.kjc.slir.fission.Fissioner;

public class StatelessFissioner {

    /** the slice we are fissing */
    private Filter slice;
    /** the amount we are fizzing slice by */
    private int fizzAmount;
    /** the filter of the slice we are fissing */
    private WorkNode filter;
    /** the filter info of the filter of the slice we are fissing */
    private WorkNodeInfo fInfo;

    private int sliceSteadyMult;
    /** the fission products of the slice */
    private Filter[] sliceClones;

    public static FissionGroup doit(Filter slice, StaticSubGraph ssg, int fissAmount) {
        if(!KjcOptions.sharedbufs) {
            return Fissioner.doit(slice, ssg, fissAmount);
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
    public static boolean canFizz(Filter filter, boolean debug) {

        // Get information on Slice rates
        WorkNodeInfo.reset();

        WorkNode workNode = filter.getWorkNode();
        
        // Check to see if Slice has file reader/writer.  Don't fizz file
        // reader/writer
        if(workNode.isPredefined()) {
            if(debug) System.out.println("Can't fizz: Filter contains file reader/writer: " + filter);
            return false;
        }

        // Check to make sure that Slice is stateless
        if(MutableStateExtractor.hasMutableState(filter.getWorkNode().getWorkNodeContent())) {
            if(debug) System.out.println("Can't fizz: Filter is not stateless: " + filter);
            return false;
        }

        // Check to see if FilterSliceNode contains a linear filter.  At the
        // moment, we can't fizz linear filters
        if(workNode.getWorkNodeContent().isLinear()) {
            if(debug) System.out.println("Can't fizz: Filter contains linear filter, presently unsupported: " + filter);
            return false;
        }
        
        if (workNode.hasIO() && KjcOptions.regtest) {        	 
        	if(debug) System.out.println("Can't fizz: Filter contains print functions, presently unsupported: " + filter);
        	 return false;
        }

        // Dominators can't be fizzed 
        if (filter.isTopFilter()) {
        	System.out.println("Can't fizz: Filter is a dominator: " + filter);
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
        this.fInfo = WorkNodeInfo.getFilterInfo(filter);

        sliceSteadyMult = fInfo.steadyMult;
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
        String origName = slice.getWorkNode().getWorkNodeContent().getName();
        for(int x = 0 ; x < fizzAmount ; x++)
            sliceClones[x].getWorkNode().getWorkNodeContent().setName(origName + "_fizz" + fizzAmount + "_clone" + x);

        // Modify name of original Slice
        slice.getWorkNode().getWorkNodeContent().setName(origName + "_fizz" + fizzAmount);
        
        // Calculate new steady-state multiplicity based upon fizzAmount.  
        // Because work is equally shared among all Slice clones, steady-state 
        // multiplicity is divided by fizzAmount for each Slice clone
        int newSteadyMult = sliceSteadyMult / fizzAmount;

        for(int x = 0 ; x < fizzAmount ; x++)
            sliceClones[x].getWorkNode().getWorkNodeContent().setSteadyMult(newSteadyMult);
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
            sliceClones[x].getWorkNode().getWorkNodeContent().setPrework(null);
            sliceClones[x].getWorkNode().getWorkNodeContent().setInitMult(0);
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
