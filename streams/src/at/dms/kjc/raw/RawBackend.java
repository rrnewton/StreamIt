package at.dms.kjc.raw;

import streamit.scheduler.*;

import at.dms.util.IRPrinter;
import at.dms.util.SIRPrinter;
import at.dms.kjc.*;
import at.dms.kjc.iterator.*;
import at.dms.kjc.sir.*;
import at.dms.kjc.sir.lowering.*;
import at.dms.kjc.sir.lowering.fusion.*;
import at.dms.kjc.sir.lowering.fission.*;
import at.dms.kjc.lir.*;
import java.util.*;
import at.dms.util.Utils;

public class RawBackend {
    //given a flatnode map to the execution count
    public static HashMap initExecutionCounts;
    public static HashMap steadyExecutionCounts;
    //the simulator to be run
    public static Simulator simulator;
    // get the execution counts from the scheduler
    public static HashMap[] executionCounts;

    public static void run(SIRStream str,
			JInterfaceDeclaration[] 
			interfaces,
			SIRInterfaceTable[]
			interfaceTables) {
	System.out.println("Entry to RAW Backend");

	if (StreamItOptions.ratematch)
	    simulator = new RateMatchSim();
	else 
	    simulator = new FineGrainSimulator();

	//this must be run now, FlatIRToC relies on it!!!
	Renamer.renameAll(str);
	
	// move field initializations into init function
	System.out.print("Moving initializers into init functions... ");
	FieldInitMover.moveStreamInitialAssignments(str);
	System.out.println("done.");
	
	// propagate constants and unroll loop
	System.out.println("Running Constant Prop and Unroll...");
	ConstantProp.propagateAndUnroll(str);
	System.out.println("Done Constant Prop and Unroll...");

	// construct stream hierarchy from SIRInitStatements
	ConstructSIRTree.doit(str);

	//SIRPrinter printer1 = new SIRPrinter();
	//str.accept(printer1);
	//printer1.close();

	StreamItDot.printGraph(str, "before.dot");
	
	if (StreamItOptions.fusion) {
	    System.out.println("Running FuseAll...");
	    FuseAll.fuse(str);
	    System.out.println("Done FuseAll...");
	}

	//VarDecl Raise to move array assignments up
	new VarDeclRaiser().raiseVars(str);

        // do constant propagation on fields
        if (StreamItOptions.constprop) {
	    System.out.println("Running Constant Propagation of Fields");
	    FieldProp.doPropagate(str);
	    //System.out.println("Analyzing Branches..");
	    //new BlockFlattener().flattenBlocks(str);
	    //new BranchAnalyzer().analyzeBranches(str);
	}
	
	AdjustGranularity.doit(str, 
			       StreamItOptions.rawRows * 
			       StreamItOptions.rawColumns);
	
	if (StreamItOptions.partition) {
	    Partitioner.doit(str, 
			     StreamItOptions.rawRows *
			     StreamItOptions.rawColumns);
	}

	StreamItDot.printGraph(str, "after.dot");

	//VarDecl Raise to move array assignments up
	new VarDeclRaiser().raiseVars(str);

       	System.out.println("Flattener Begin...");
	executionCounts = SIRScheduler.getExecutionCounts(str);
	RawFlattener rawFlattener = new RawFlattener(str);
	rawFlattener.dumpGraph("flatgraph.dot");
	System.out.println("Flattener End.");
	//create the execution counts for other passes
	createExecutionCounts(str, rawFlattener);
	//see if we can remove any joiners
	JoinerRemoval.run(rawFlattener.top);

	// layout the components (assign filters to tiles)
	
	Layout.simAnnealAssign(rawFlattener.top);
	
	//Layout.handAssign(rawFlattener.top);
	System.out.println("Assign End.");
	//Generate the switch code
	
	CalcBufferSize.createBufferSizePow2(rawFlattener.top);
	System.out.println("Switch Code Begin...");
	SwitchCode.generate(rawFlattener.top);
	//	SwitchCode.dumpCode();
	System.out.println("Switch Code End.");
	//Generate the tile code
	System.out.println("Tile Code begin...");
	TileCode.generateCode(rawFlattener.top);
	System.out.println("Tile Code End.");
	//generate the makefiles
	System.out.println("Creating Makefile.");
	MakefileGenerator.createMakefile();
	System.out.println("Exiting");
	System.exit(0);
    }

    //helper function to add everything in a collection to the set
    public static void addAll(HashSet set, Collection c) 
    {
	Iterator it = c.iterator();
	while (it.hasNext()) {
	    set.add(it.next());
	}
    }
   
    private static void createExecutionCounts(SIRStream str,
					      RawFlattener rawFlattener) {
	// make fresh hashmaps for results
	HashMap[] result = { initExecutionCounts = new HashMap(), 
			     steadyExecutionCounts = new HashMap()} ;

	// then filter the results to wrap every filter in a flatnode,
	// and ignore splitters
	for (int i=0; i<2; i++) {
	    for (Iterator it = executionCounts[i].keySet().iterator();
		 it.hasNext(); ){
		SIROperator obj = (SIROperator)it.next();
		int val = ((int[])executionCounts[i].get(obj))[0];
		//System.err.println("execution count for " + obj + ": " + val);
		/** This bug doesn't show up in the new version of
		 * FM Radio - but leaving the comment here in case
		 * we need to special case any other scheduler bugsx.
		 
		 if (val==25) { 
		 System.err.println("Warning: catching scheduler bug with special-value "
		 + "overwrite in RawBackend");
		 val=26;
		 }
		*/
		if ((i == 0) &&
		    (obj.getName().startsWith("Fused__StepSource") ||
		     obj.getName().startsWith("Fused_FilterBank")))
		    val++;
		
		if (rawFlattener.getFlatNode(obj) != null)
		    result[i].put(rawFlattener.getFlatNode(obj), 
				  new Integer(val));
	    }
	}
	
	//Schedule the new Identities and Splitters introduced by RawFlattener
	for(int i=0;i<RawFlattener.needsToBeSched.size();i++) {
	    FlatNode node=(FlatNode)RawFlattener.needsToBeSched.get(i);
	    int initCount=-1;
	    if(initExecutionCounts.get(node.incoming[0])!=null)
		initCount=((Integer)initExecutionCounts.get(node.incoming[0])).intValue();
	    if((initCount==-1)&&(executionCounts[0].get(node.incoming[0].contents)!=null))
		initCount=((int[])executionCounts[0].get(node.incoming[0].contents))[0];
	    int steadyCount=-1;
	    if(steadyExecutionCounts.get(node.incoming[0])!=null)
		steadyCount=((Integer)steadyExecutionCounts.get(node.incoming[0])).intValue();
	    if((steadyCount==-1)&&(executionCounts[1].get(node.incoming[0].contents)!=null))
		steadyCount=((int[])executionCounts[1].get(node.incoming[0].contents))[0];
	    if(node.contents instanceof SIRIdentity) {
		if(initCount>=0)
		    initExecutionCounts.put(node,new Integer(initCount));
		if(steadyCount>=0)
		    steadyExecutionCounts.put(node,new Integer(steadyCount));
	    } else if(node.contents instanceof SIRSplitter) {
		int[] weights=node.weights;
		FlatNode[] edges=node.edges;
		int sum=0;
		for(int j=0;j<weights.length;j++)
		    sum+=weights[j];
		for(int j=0;j<edges.length;j++) {
		    if(initCount>=0)
			initExecutionCounts.put(edges[j],new Integer((initCount*weights[j])/sum));
		    if(steadyCount>=0)
			steadyExecutionCounts.put(edges[j],new Integer((steadyCount*weights[j])/sum));
		}
	    } else if(node.contents instanceof SIRJoiner) {
		FlatNode oldNode=rawFlattener.getFlatNode(node.contents);
		if(executionCounts[0].get(node.oldContents)!=null)
		    result[0].put(node,new Integer(((int[])executionCounts[0].get(node.oldContents))[0]));
		if(executionCounts[1].get(node.oldContents)!=null)
		    result[1].put(node,new Integer(((int[])executionCounts[1].get(node.oldContents))[0]));
	    }
	}
	
	//now, in the above calculation, an execution of a joiner node is 
	//considered one cycle of all of its inputs.  For the remainder of the
	//raw backend, I would like the execution of a joiner to be defined as
	//the joiner passing one data item down stream
	for (int i=0; i < 2; i++) {
	    Iterator it = result[i].keySet().iterator();
	    while(it.hasNext()){
		FlatNode node = (FlatNode)it.next();
		if (node.contents instanceof SIRJoiner) {
		    int oldVal = ((Integer)result[i].get(node)).intValue();
		    int cycles=oldVal*((SIRJoiner)node.contents).oldSumWeights;
		    if((node.schedMult!=0)&&(node.schedDivider!=0))
			cycles=(cycles*node.schedMult)/node.schedDivider;
		    result[i].put(node, new Integer(cycles));
		}
		if (node.contents instanceof SIRSplitter) {
		    int sum = 0;
		    for (int j = 0; j < node.ways; j++)
			sum += node.weights[j];
		    int oldVal = ((Integer)result[i].get(node)).intValue();
		    result[i].put(node, new Integer(sum*oldVal));
		}
	    }
	}
    }
    
    //debug function
    //run me after layout please
    public static void printCounts(HashMap counts) {
	Iterator it = counts.keySet().iterator();
	while(it.hasNext()) {
	    FlatNode node = (FlatNode)it.next();
	    //	if (Layout.joiners.contains(node)) 
	    System.out.println(node.contents.getName() + " " +
			       ((Integer)counts.get(node)).intValue());
	}
    }

    

    //simple helper function to find the topmost pipeline
    private static SIRStream getTopMostParent (FlatNode node) 
    {
	SIRContainer[] parents = node.contents.getParents();
	return parents[parents.length -1];
    }
}
