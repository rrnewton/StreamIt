package streamittest;

import junit.framework.*;

/**
 * StreamITTestCase is the base class for all streamit
 * test cases. This class provides some useful methods.
 * $Id: StreamITTestCase.java,v 1.19 2003-01-29 22:13:42 aalamb Exp $
 **/
class StreamITTestCase extends TestCase {
    static final String EXAMPLE_PATH  = "apps/examples/";
    static final String TESTS_PATH    = "apps/tests/";
    static final String APPS_PATH     = "apps/applications/";
    static final String BENCH_PATH    = "apps/benchmarks/";
    static final String PLDI_PATH     = "apps/benchmarks/pldi-03/";

    static final int    DEFAULT_FLAGS = CompilerInterface.NONE;
    
    /** Compiler interface for this test to use **/
    CompilerInterface compiler;

    /**
     * Create a new StreamITTestCase with the default compiler options.
     **/
    public StreamITTestCase(String name) {
	this(name, DEFAULT_FLAGS);
    }
    
    /**
     * Create a new StreamItTestCase with the
     * specified compiler flags.
     **/
    public StreamITTestCase(String name, int flags) {
	super(name);
	// create a compiler interface for the test case to use (set up the compiler options)
	this.compiler = CompilerInterface.createCompilerInterface(flags);
    }

    /**
     * Converts new syntax to old syntax in root using the streamit
     * frontend.
     */
    public void doSyntaxConvertTest(String root,
                                    String filein,
                                    String fileout)
    {
        // actually do the conversion
        boolean result = compiler.streamITConvert(root, filein, fileout);
        // assemble the id of this test
        String idMessage = ("Convert " + root + filein + " to " + fileout +
			    "(" + compiler.getOptionsString() + ")");

	// if the compilation was successful, print a success message
	if (result == true) {
	    ResultPrinter.printSuccess(idMessage);
	}

	// use JUnit framework to assert that the test was ok
    	assertTrue(idMessage, result);
    }        

    /**
     * Compiles the specified filename in root using
     * the streamit compiler and then gcc or rcc depecding on target.
     **/
    public void doCompileTest(String root,
			      String filename) {
	// acutally do the compilation
	boolean result = compiler.streamITCompile(root,
						  filename);
	// assemble the id of this test
	String idMessage = ("Compile " + root + filename +
			    "(" + compiler.getOptionsString() + ")");

	// if the compilation was successful, print a success message
	if (result == true) {
	    ResultPrinter.printSuccess(idMessage);
	}

	// use JUnit framework to assert that the test was ok
    	assertTrue(idMessage, result);
    }

    /**
     * Tries to execute the program that was generated by running
     * doCompileTest(root, filename). returns true if execution
     * doesn't terminate with a non 0 eit status.
     *
     * Assumes 0 initiailization outputs and 1 steady-state output per graph cycle 
     **/
    public void doRunTest(String root,
			  String filename) {
	doRunTest(root, filename, 0, 1);
    }
    


    /**
     * Tries to execute the program that was generated by running
     * doCompileTest(root, filename). returns true if execution
     * doesn't terminate with a non 0 eit status.
     **/
    public void doRunTest(String root,
			  String filename,
			  int initializationOutputs,
			  int steadyStateOutputs) {
	// test execution
	boolean result = compiler.streamITRun(root,
					      filename,
					      initializationOutputs,
					      steadyStateOutputs);
	// make an id string for this test
	String idMessage = ("Executing " + root + filename +
			    "(" + compiler.getOptionsString() + ")");

	// if success, print a message
	if (result == true) {
	    ResultPrinter.printSuccess(idMessage);
	}

	// use junit framwork to test for success
	assertTrue(idMessage, result);

    }

    /**
     * Compares the output generated by doRunTest with the contents of the specified
     * data file.
     **/
    public void doCompareTest(String root,
			      String filename,
			      String datafile) {

	// test that the output produced is the same as what is expected
	boolean result = compiler.streamITCompare(root,
						  filename,
						  datafile);
	String idMessage = ("Verify output " + root +
			    filename + "(" + compiler.getOptionsString() + ")"); 
	
	// if success, print a success message
	if (result == true) {
	    ResultPrinter.printSuccess(idMessage);
	}
	
	// use JUnit framework
	assertTrue(idMessage, result);
    }

    /**
     * Performs streamit compile, gcc compile, execution, and comparison.
     * @param root is root directory path.
     * @param filename is the streamit program file.
     * @param datafile is the file with known correct data.
     * note: assumes that one output item is produced in steady state
     * and no items are produced as a part of initialization
     **/
    public void doCompileRunVerifyTest(String root,
				       String filename,
				       String datafile) {
	doCompileRunVerifyTest(root, filename, datafile,0,1);
    }
	
    /**
     * Performs streamit compile, gcc compile, execution, and comparison.
     * @param root is root directory path.
     * @param filename is the streamit program file.
     * @param datafile is the file with known correct data.
     * @param initializationOutputs is the number of outputs generated by initialization
     * @param ssOutputs is the number of outputs generated each steady state cycle
     **/
    public void doCompileRunVerifyTest(String root,
				       String filename,
				       String datafile,
				       int initializationOutputs,
				       int ssOutputs) {
	

	// run the compilation tests
	doCompileTest(root, filename);

	// try to run the program
	doRunTest(root, filename, initializationOutputs, ssOutputs);

	// compare the output
	doCompareTest(root, filename, datafile);
    }

	

    public void doMake(String root) {
	assertTrue("make for " + root,
		   compiler.runMake(root));
    }

    public void doMake(String root, String target) {
	assertTrue("make for " + root + " target " + target,
		   compiler.runMake(root, target));
    }

    /**
     * Returns true if the compiler flags contain the option to
     * compile to a <i>x<i> raw chip (eg 16 tiles). This is used to
     * add tests conditionally to a test suite (tests known not to
     * compile (eg fit) on raw <i> aren't included.
     **/
    public static boolean flagsContainRaw(int i, int flags) {
 	return ((flags & CompilerInterface.RAW[i]) == CompilerInterface.RAW[i]);
    }

    /**
     * Returns true if the passed flags contain raw[x] for some x.
     **/
    public static boolean flagsContainRaw(int flags) {
	return CompilerInterface.rawTarget(flags);
    }
    
    /**
     * Returns true if the compiler flags contain the option to
     * compile with partitioning turned on. This is used
     * to add tests conditionally to a test suite.
     **/
    public static boolean flagsContainPartition(int flags) {
 	return ((flags & CompilerInterface.PARTITION) == CompilerInterface.PARTITION);
    }

    /**
     * Returns true if the compiler flags contain the option to
     * compile with maximal fusion. This is used to add tests
     * conditionally to a test suite.
     **/
    public static boolean flagsContainFusion(int flags) {
 	return ((flags & CompilerInterface.FUSION) == CompilerInterface.FUSION);
    }
}
	    
