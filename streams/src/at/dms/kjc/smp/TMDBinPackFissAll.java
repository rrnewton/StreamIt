package at.dms.kjc.smp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import at.dms.kjc.KjcOptions;
import at.dms.kjc.ObjectDeepCloner;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.lowering.fusion.FuseAll;
import at.dms.kjc.sir.lowering.partition.WorkEstimate;
import at.dms.kjc.slir.DataFlowOrder;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.FilterWorkEstimate;
import at.dms.kjc.slir.InternalFilterNode;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.slir.WorkNodeContent;
import at.dms.kjc.slir.WorkNodeInfo;
import at.dms.kjc.slir.fission.FissionGroup;

public class TMDBinPackFissAll extends TMD {

    public final static double FUSE_WORK_CHANGE_THRESHOLD = .90;

    private double DUP_THRESHOLD;
    public static final int FISS_COMP_COMM_THRESHOLD = 10;
    private HashMap<Filter, Integer> fizzAmount;

    public TMDBinPackFissAll() {
        super();
        DUP_THRESHOLD = ((double)KjcOptions.dupthresh) / 100.0;
        fizzAmount = new HashMap<Filter, Integer>();
    }
    
    /** Get the Core for a Slice 
     * @param node the {@link at.dms.kjc.slir.InternalFilterNode} to look up. 
     * @return the Core that should execute the {@link at.dms.kjc.slir.InternalFilterNode}. 
     */
    public Core getComputeNode(InternalFilterNode node) {
        assert layoutMap.keySet().contains(node);
        return layoutMap.get(node);
    }
    
    /** Set the Core for a Slice 
     * @param node         the {@link at.dms.kjc.slir.InternalFilterNode} to associate with ...
     * @param core   The tile to assign the node
     */
    public void setComputeNode(InternalFilterNode node, Core core) {
        assert node != null && core != null;
        //remember what filters each tile has mapped to it
        //System.out.println("Setting " + node + " to core " + core);
        layoutMap.put(node, core);
        if (core.isComputeNode())
            core.getComputeCode().addFilter(node.getAsFilter());
    }
    
    /**
     * Assign the filternodes of the slice graph to tiles on the chip based on the levels
     * of the graph. 
     */
    public void runLayout() {    	
    	System.out.println("TMDBinPackFissAll.runLayout()");
        assert graphSchedule != null : 
            "Must set the graph schedule (multiplicities) before running layout";            	
        
        LinkedList<Filter> slices = DataFlowOrder.getTraversal(graphSchedule.getSSG().getTopFilters());
        HashSet<Filter> fizzedSlices = new HashSet<Filter>();
        HashSet<Filter> unfizzedSlices = new HashSet<Filter>();
        HashSet<Filter> dominators = new HashSet<Filter>();
        	
        // Get work estimates for all slices
        HashMap<WorkNode, Long> workEsts = new HashMap<WorkNode, Long>();
        for(Filter slice : slices) {
        	long workEst = FilterWorkEstimate.getWork(slice);
        	workEsts.put(slice.getWorkNode(), workEst);
        }    
        
        // Categorize slices into predefined, dominators, fizzed and unfizzed slices
        // Predefined filters are automatically added to off-chip memory
        for(Filter slice : slices) {
        	if(slice.getWorkNode().isPredefined())
        		setComputeNode(slice.getWorkNode(), SMPBackend.chip.getOffChipMemory());
        	else if (slice.getStaticSubGraph().isTopFilter(slice)) 
        		dominators.add(slice);
        	else if(FissionGroupStore.isFizzed(slice))
        		fizzedSlices.add(slice);
        	else
        		unfizzedSlices.add(slice);
        }
        
        System.out.println("Number of fizzed slices: " + fizzedSlices.size());
        System.out.println("Number of unfizzed slices: " + unfizzedSlices.size());
        
        // use a global greedy bin packing across all ssgs and cores for the dominators of the ssgs
        for (Filter filter : dominators) {
        	layoutDominator(filter);
        }
        
        // Sort unfizzed slices by estimated work, most work first
        LinkedList<Filter> sortedUnfizzedSlices = new LinkedList<Filter>();
        for(Filter slice : unfizzedSlices) {
        	boolean inserted = false;
        	for(int x = 0 ; x < sortedUnfizzedSlices.size() ; x++) {
        		if(workEsts.get(slice.getWorkNode()) >
        			workEsts.get(sortedUnfizzedSlices.get(x).getWorkNode())) {
        			sortedUnfizzedSlices.add(x, slice);
        			inserted = true;
        			break;
        		}
        	}
        	
        	if(!inserted)
        		sortedUnfizzedSlices.add(slice);
        }
        
        // Attempt to load balance unfizzed slices across cores by using
        // LPT algorithm, which greedily assigns the slice with the most work
        // to the core with the least amount of estimate work
        long[] workAmounts = new long[SMPBackend.chip.size()];
        for(int x = 0 ; x < workAmounts.length ; x++)
        	workAmounts[x] = 0;
        
        for(Filter slice : sortedUnfizzedSlices) {
        	// Find core with minimum amount of work
        	long minWork = Long.MAX_VALUE;
        	int minCore = -1;
        	
        	for(int core = 0 ; core < workAmounts.length ; core++) {
        		if(workAmounts[core] < minWork) {
        			minWork = workAmounts[core];
        			minCore = core;
        		}
        	}
        	
        	// Add slice to core
        	setComputeNode(slice.getWorkNode(), SMPBackend.chip.getNthComputeNode(minCore));
        	workAmounts[minCore] += workEsts.get(slice.getWorkNode());
        }
        
        for(int x = 0 ; x < workAmounts.length ; x++)
        	System.out.println("Core " + x + " has work: " + workAmounts[x]);
        
        // Schedule fizzed slices by assigning fizzed copies sequentially
        // across cores
        HashSet<Filter> alreadyAssigned = new HashSet<Filter>();
        for(Filter slice : fizzedSlices) {
            // If slice already assigned, skip it
            if(alreadyAssigned.contains(slice))
                continue;
        	
        	// Get fizzed copies of slice
        	Filter[] fizzedCopies = FissionGroupStore.getFizzedSlices(slice);
        	
        	// Assign fizzed set sequentially across cores
        	for(int x = 0 ; x < fizzedCopies.length ; x++) {
        		setComputeNode(fizzedCopies[x].getWorkNode(), SMPBackend.chip.getNthComputeNode(x));
        		System.out.println("Assigning " + fizzedCopies[x].getWorkNode() + " to core " + SMPBackend.chip.getNthComputeNode(x).getCoreID());
        	}

            // Mark fizzed set as assigned
            for(Filter fizzedSlice : fizzedCopies)
                alreadyAssigned.add(fizzedSlice);

            // If using shared buffers, then fission does not replace the original
            // unfizzed slice with fizzed slices.  The current 'slice' is the original
            // unfizzed slice.  Set the compute node for 'slice' to the offChipMemory.
            // This is so that when we dump a dot-graph, we have a core to return when
            // we display the 'slice' in the graph.  Returning offChipMemory as the core
            // is sub-optimal, though there's not much else we can do right now
            if(KjcOptions.sharedbufs) {
                assert FissionGroupStore.isUnfizzedSlice(slice);
                setComputeNode(slice.getWorkNode(), SMPBackend.chip.getOffChipMemory());
                alreadyAssigned.add(slice);
            }
        }
    }
    
    /**
     * Run the Time-Multiplexing Data-parallel scheduler.  Right now, it assumes 
     * a pipeline of stateless filters
     */
    public void run(int tiles) {
    	
        //if we are using the SIR data parallelism pass, then don't run TMD
        if (KjcOptions.dup == 1 || KjcOptions.optfile != null) {
        	System.out.println("***** Not using TMD scheduler since an SIR partitioner was used *****!");
        	//multiply steady multiplicity of each filter by KjcOptions.steadyMult
		    LinkedList<Filter> slices = DataFlowOrder.getTraversal(graphSchedule.getSSG().getTopFilters());

		    for (Filter slice : slices) {
		    	WorkNodeContent filter = slice.getWorkNode().getWorkNodeContent();
		    	filter.multSteadyMult(KjcOptions.steadymult);
		    }

		    //must reset the filter info's because we have changed the schedule
	        WorkNodeInfo.reset();
		    return;
        }
        
        //calculate how much to fizz each filter 
        calculateFizzAmounts(tiles);
        
        //calculate multiplicity factor necessary to fizz filters
        int factor = multiplicityFactor(tiles);
        System.out.println("Using fission steady multiplicity factor: " + factor);
                
        //go through and multiply the steady multiplicity of each filter by factor        
        LinkedList<Filter> slices = DataFlowOrder.getTraversal(graphSchedule.getSSG().getTopFilters());

        for (Filter slice : slices) {
            WorkNodeContent filter = slice.getWorkNode().getWorkNodeContent();
            filter.multSteadyMult(factor * KjcOptions.steadymult);
         }
        
        //must reset the filter info's because we have changed the schedule
        WorkNodeInfo.reset();
        
        SMPBackend.scheduler.graphSchedule.getSSG().dumpGraph("before_fission.dot", 
                null, false);
        
        int maxFission = 0;
        int i = 0;
        //go through and perform the fission
        for (Filter slice : slices) {
            if (fizzAmount.containsKey(slice) && fizzAmount.get(slice) > 1) {
                FissionGroup fissionGroup = 
                    StatelessFissioner.doit(slice, graphSchedule.getSSG(), fizzAmount.get(slice));

                if(fissionGroup != null) {
//                    System.out.println("Fissing " + slice.getWorkNode() + " by " + fizzAmount.get(slice));
                    if (fizzAmount.get(slice) > maxFission)
                        maxFission = fizzAmount.get(slice);

                    FissionGroupStore.addFissionGroup(fissionGroup);
                }

                SMPBackend.scheduler.graphSchedule.getSSG().dumpGraph("fission_pass_" + i + ".dot", 
									 null, false);
                i++;
            }
        }
        
        System.out.println("Max fission amount: " + maxFission);
        
        //because we have changed the multiplicities of the FilterContents
        //we have to reset the filter info's because they cache the date of the
        //FilterContents
        WorkNodeInfo.reset();
    }
    
    /**
     * Calculate the amount by which filters can be fizzed.  For now, stateless
     * filters that meet specific criteria are all fizzed by <totalTiles>
     */
    public void calculateFizzAmounts(int totalTiles) {
    	LinkedList<Filter> slices = DataFlowOrder.getTraversal(graphSchedule.getSSG().getTopFilters());
    	
    	// Look for fizzable filters
    	for(Filter slice : slices) {
    		WorkNode fsn = slice.getWorkNode();
    		WorkNodeContent fc = fsn.getWorkNodeContent();
    		
    		long workEst = FilterWorkEstimate.getWork(slice);
    		int commRate = (fc.getPushInt() + fc.getPopInt() * fc.getMult(SchedulingPhase.STEADY));
    		
    		// Predefined filters can't be fizzed, automatically skip
    		if(fsn.isPredefined())
    			continue;
    	
    		// Check if fizzable.  If so, fizz by totalTiles
    		if(StatelessFissioner.canFizz(slice, false)) {
    			if(commRate > 0 && workEst / commRate <= FISS_COMP_COMM_THRESHOLD) {
    				System.out.println("Can fizz, but too much communication: " + fsn);
    			}
    			else {
    				System.out.println("Can fizz: " + fsn + ", fizzing by: " + totalTiles);
    				fizzAmount.put(slice, totalTiles);
    			}
    		}
    		else {
    			System.out.println("Cannot fiss: " + fsn);
    		}
    	}
    }
    
    /**
     * Calculates least common multiple between <a> and <b>
     */
    private int LCM(int a, int b) {
    	int product = a * b;
    	
    	do {
    		if(a < b) {
    			int tmp = a;
    			a = b;
    			b = tmp;
    		}
    		
    		a = a % b;
    	} while(a != 0);
    	
    	return product / b;
    }
    
    /**
     * Determine the factor that we are going to multiple each slice by so that 
     * fission on the slice graph is legal.  Keep trying multiples of the number 
     * tiles until each slice passes the tests for legality.
     *  
     * @param tiles The number of tiles we are targeting 
     * @return the factor to multiply the steady multiplicities by
     */
    private int multiplicityFactor(int tiles) {
        int maxFactor = 1;
        LinkedList<Filter> slices = DataFlowOrder.getTraversal(graphSchedule.getSSG().getTopFilters());
        
        //find minimum steady-state multiplicity factor necessary to meet
        //copyDown and duplication constraints of fission
        for (Filter slice : slices) {
            if (slice.getWorkNode().isPredefined())
                continue;
            
            WorkNodeInfo fi = WorkNodeInfo.getFilterInfo(slice.getWorkNode());
            
            //nothing to do for filters with no input
            if (fi.pop == 0)
                continue;

            if (fizzAmount.containsKey(slice) && fizzAmount.get(slice) > 1) {
            	//this works only for pipelines right now, so just that we have at most
                //one input and at most one output for the slice
                //assert slice.getHead().getSourceSet(SchedulingPhase.STEADY).size() <= 1;
                
                //check that we have reached the threshold for duplicated items
                int threshFactor = 
                    (int)Math.ceil((((double)(fi.peek - fi.pop)) * fizzAmount.get(slice)) / 
                        ((double)(DUP_THRESHOLD * (((double)fi.pop) * ((double)fi.steadyMult)))));

                //this factor makes sure that copydown is less than pop*mult*factor
                int cdFactor = 
                    (int)Math.ceil(((double)fi.copyDown) / ((double)(fi.pop * fi.steadyMult) 
                                                            / (double)(fizzAmount.get(slice))));

                int myFactor = Math.max(cdFactor, threshFactor);

                if (maxFactor < myFactor)
                    maxFactor = myFactor;
            }
        }
        
        //find LCM of all fizz amounts for filters
        int lcm = 1;
        for(Integer fa : fizzAmount.values())
        	lcm = LCM(lcm, fa.intValue());

        //scale up LCM so that it is at least as large as the minimum
        //steady-state multiplicity factor
        lcm = (int)Math.ceil((double)maxFactor / (double)lcm) * lcm;
        
        return lcm;
    }

    @Override
    public SIRStream SIRFusion(SIRStream str, int tiles) {      

//        System.out.println("TMDBinPackFissAll.SIRFusion Performing fusion of stateful filters");

        SIRStream oldStr;
        //get the first work estimate
        WorkEstimate work = WorkEstimate.getWorkEstimate(str);
        //bin pack the shits
        StatefulGreedyBinPacking binPacker = new StatefulGreedyBinPacking(str, tiles, work);
        binPacker.pack();
        //get the max bin weight for the packing
        long oldWork = binPacker.maxBinWeight();
        //the work of the new partitioning
        long newWork = 0;
        //the percentage change
        double workChange;

//        System.out.println("TMDBinPackFissAll.SIRFusion called on str=" + str.getName() );
//    	if (str instanceof SIRFilter) {
//    		System.out.println("TMDBinPackFissAll.SIRFusion before str=" + str.getName() + " isStateful=" + ((SIRFilter)str).isStateful());
//    	}
    	
        
        if(StatefulFusion.countStatefulFilters(str) < KjcOptions.smp) {
            return str;
        }
        
        do {
        	
    		
            oldStr = (SIRStream)ObjectDeepCloner.deepCopy(str);

        	

            
            int numStatefulFilters = StatefulFusion.countStatefulFilters(str);
            int minTiles = at.dms.kjc.sir.lowering.partition.Partitioner.estimateFuseAllResult(str);

            if(numStatefulFilters - 1 <= minTiles + 1) {
                System.out.println("  Fusing all filters");
                str = FuseAll.fuse(str, false);
            }
            else {
                System.out.println("  Attempting to fuse down to " + (numStatefulFilters - 1) + 
                                   " stateful filters");
                str = new StatefulFusion(str, work, numStatefulFilters - 1, false).doFusion();
            }

            work = WorkEstimate.getWorkEstimate(str);

            //greedy bin pack the stateful filters
            binPacker = new StatefulGreedyBinPacking(str, tiles, work);
            binPacker.pack();
            newWork = binPacker.maxBinWeight();

            //find the percentage change in work between the two 
            workChange = ((double)oldWork) / ((double)newWork);

            //remember this as the old work for the next (possible) iteration
            System.out.println(oldWork + " / " + newWork + " = " + workChange);
            oldWork = newWork;

            //if number of stateful filters didn't change, then quit since can't fuse anymore
            int newNumStatefulFilters = StatefulFusion.countStatefulFilters(str);
            if(newNumStatefulFilters == numStatefulFilters || 
               newNumStatefulFilters <= KjcOptions.smp) {
                break;
            }
        } while (workChange >= FUSE_WORK_CHANGE_THRESHOLD);
        
        if(workChange < FUSE_WORK_CHANGE_THRESHOLD) {
            str = oldStr;
        }

        return str;
    }
}
