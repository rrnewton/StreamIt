package at.dms.kjc;

import at.dms.kjc.sir.*;
import java.lang.reflect.*;

/**
 * This provides the toplevel interface for StreaMIT.
 */
public class StreaMITMain {
    private static Object params[] = new Object[4];
    private static Class paramTypes[] = new Class[4];
    
    //Only use from backends after done with params
    public static void clearParams() {
	params[0]=null;
	params[1]=null;
	params[2]=null;
	params[3]=null;
	params=null;
	paramTypes[0]=null;
	paramTypes[1]=null;
	paramTypes[2]=null;
	paramTypes[3]=null;	
    }

    /**
     * Prints out C code for the program being compiled.
     */
    public static void compile(JCompilationUnit[] app) {
	//using the raw backend to generate uniprocessor code
	//so set the number of tiles to 1 and 
	//turn partitioning on...
	if (KjcOptions.standalone || KjcOptions.rstream) {
	    KjcOptions.raw = 1;
	    KjcOptions.partition_dp = true;
	}
	
	if (KjcOptions.altcodegen &&
	    KjcOptions.standalone)
	    at.dms.util.Utils.fail("The options altcodegen and standalone are mutually exclusive.");

	if (KjcOptions.altcodegen &&
	    KjcOptions.decoupled)
	    at.dms.util.Utils.fail("The options altcodegen and decoupled are mutually exclusive.");

	if (KjcOptions.outputs > 0 &&
	    KjcOptions.numbers > 0)
	    at.dms.util.Utils.fail("The options outputs and numbers are mutually exclusive.");
	    
	if (KjcOptions.magic_net &&
	    KjcOptions.decoupled)
	    at.dms.util.Utils.fail("The options magic_net and decoupled are mutually exclusive.");
	
	System.err.println("Starting Kopi2SIR..");

	if(!KjcOptions.graph)
	    System.out.println("/*");
	Kopi2SIR k2s = new Kopi2SIR(app);
	SIRStream stream = null;
	for (int i = 0; i < app.length; i++) {
	    //System.err.println("Visiting "+i+" of "+(app.length-1));
	    SIRStream top = (SIRStream)app[i].accept(k2s);
	    if (top != null)
		stream = top;
	}

	System.err.println("Done Kopi2SIR..");

	SemanticChecker.doCheck(stream);

        System.out.println("Out of semantic checker.");
	System.err.println("Done Semantic Check..");

        String backendClass = null;
        String backendMethod = "run";
        if (KjcOptions.backend != null) {
            backendClass = KjcOptions.backend;
        } else if (KjcOptions.graph) {
            backendClass = "streamit.eclipse.grapheditor.graph.GraphEncoder";
	} else if (KjcOptions.rstream) {
	    backendClass = "at.dms.kjc.rstream.StrToRStream";
        } else if (KjcOptions.raw != -1) {
            System.out.println("*/");
            if (KjcOptions.spacetime) {
                backendClass = "at.dms.kjc.spacetime.SpaceTimeBackend";
            } else {
                backendClass = "at.dms.kjc.raw.RawBackend";
            }
        } else if (KjcOptions.cluster != -1) {
            System.out.println("*/");
            backendClass = "at.dms.kjc.cluster.ClusterBackend";
        } else {
            backendClass = "at.dms.kjc.sir.lowering.Flattener";
            backendMethod = "flatten";
        }
        
        // To find a method, we need its name and signature.  To
        // invoke it, we need to stuff the parameters into an
        // Object[]; given this, it's easy to get the types.
        params[0] = stream;
        params[1] = k2s.getInterfaces();
        params[2] = k2s.getInterfaceTables();
        params[3] = k2s.getStructures();
	
	stream=null;
	k2s=null;
	
        try {
            paramTypes[0] = Class.forName("at.dms.kjc.sir.SIRStream");
            for (int i = 1; i < 4; i++)
                paramTypes[i] = params[i].getClass();
            Class theBackend = Class.forName(backendClass);
            Method theMethod =
                theBackend.getMethod(backendMethod, paramTypes);
            theMethod.invoke(null, params);        
        } catch (ClassNotFoundException e) {
            System.err.println("*** The class " + e.getMessage() +
                               " does not exist.");
        } catch (NoSuchMethodException e) {
            System.err.println("*** The backend method " +
                               backendClass + "." + backendMethod + "()" +
                               " does not exist.");
        } catch (IllegalAccessException e) {
            System.err.println("*** Not allowed to invoke backend " +
                               backendClass);
        } catch (InvocationTargetException e) {
            // Loses debugging information on the exception, sigh.
            // We can't blindly rethrow the exception because it might
            // not be a RuntimeException.  I hate Java.  Die as best we can.
	    e.getTargetException().printStackTrace();
        }
    }
}




