package at.dms.kjc.sir.lowering;

import streamit.scheduler.*;

import at.dms.kjc.sir.lowering.fusion.*;
import at.dms.kjc.sir.lowering.fission.*;
import at.dms.util.IRPrinter;
import at.dms.util.SIRPrinter;
import at.dms.kjc.*;
import at.dms.kjc.sir.*;
import at.dms.kjc.lir.*;

/**
 * This is the main class for decomposing the high SIR into
 * lower-level function calls.
 */
public class Flattener {

    /**
     * Flattens <str> into a low IR representation, given that <interfaces>
     * are all the top-level interfaces declared in the program and 
     * <interfaceTables> represents the mapping from interfaces to methods
     * that implement a given interface in a given class.
     */
    public static JClassDeclaration flatten(SIRStream str,
					    JInterfaceDeclaration[] 
					    interfaces,
					    SIRInterfaceTable[]
					    interfaceTables,
                                            SIRStructure[] structs) {
	// DEBUGGING PRINTING
	System.out.println("--------- ON ENTRY TO FLATTENER ----------------");
	SIRPrinter printer1 = new SIRPrinter();
	str.accept(printer1);
	printer1.close();

	// propagate constants and unroll loops
	ConstantProp.propagateAndUnroll(str);
	//FieldProp.doPropagate(str);

	//Raise Variable Declaration to front of blocks
	str.getInit().accept(new VarDeclRaiser());	
	
	AdjustGranularity.doit(str, -1);

	if (StreamItOptions.partition) {
	    Partitioner.doit(str, 
			     StreamItOptions.rawRows *
			     StreamItOptions.rawColumns);
	}

	/*
	SIRFilter toDuplicate = ((SIRFilter)
				 ((SIRPipeline)
				  ((SIRPipeline)str).get(1)).get(0));
	System.err.println("Trying to duplicate " + toDuplicate);
	StatelessDuplicate.doit(toDuplicate, 2);
	*/

	if (StreamItOptions.fusion) {
	    System.out.println("Running Fusion");
	    FuseSplit.doFlatten(str);
	    FuseAll.fuse(str);
	    // DEBUGGING PRINTING
	    System.out.println("--------- AFTER FUSION ------------");
	    printer1 = new SIRPrinter();
	    str.accept(printer1);
	    printer1.close();
	    
	}
	
        // flatten split/joins with duplicate splitters and RR joiners
        //str = SJFlatten.doFlatten(str);

        // do constant propagation on fields
        if (StreamItOptions.constprop) {
	    System.out.println("Running Constant Propagation of Fields");
	    FieldProp.doPropagate(str);
	}

	// DEBUGGING PRINTING
	System.out.println("--------- AFTER CONSTANT PROP / FUSION --------");
	printer1 = new SIRPrinter();
	str.accept(printer1);
	printer1.close();

	// name the components
	Namer.assignNames(str);
	// make single structure
	JClassDeclaration flatClass = Structurer.structure(str, 
							   interfaces,
							   interfaceTables,
                                                           structs);
	// build schedule as set of higher-level work functions
	Schedule schedule = SIRScheduler.buildWorkFunctions(str, flatClass);
	// add LIR hooks to init and work functions
	LowerInitFunctions.lower(str, schedule);
        LowerWorkFunctions.lower(str);

	// DEBUGGING PRINTING
	System.out.println("----------- AFTER FLATTENER ------------------");
	IRPrinter printer = new IRPrinter();
	flatClass.accept(printer);
	printer.close();

	return flatClass;
    }
   
}
