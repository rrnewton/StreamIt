package streamittest;

import java.io.*;
import java.util.*;



/**
 * This class contains code for executing commands while inside the java
 * runtime system.
 **/
public class Harness {


    /** run command natively, ignoring stdout (eg for gcc, make) **/
    public static boolean executeNative(String[] cmdArray) throws Exception {
	return executeNative(cmdArray, null);
    }

    /**
     * runs a command natively, returns true if command
     * successfully executes and returns 0 exit status. Otherwise
     * returns false and prints error messages and return status
     * via ResultPrinter.printError.
     *
     * Writes standard output from program to outStream.
     **/
    public static boolean executeNative(String[] cmdArray, OutputStream outStream) throws Exception {

	// start the process executing
	Process jProcess = Runtime.getRuntime().exec(cmdArray);
	
	// get hooks into the output and error streams
	BufferedReader jOutStream = new BufferedReader(new InputStreamReader(jProcess.getInputStream()));
	BufferedReader jErrorStream =  new BufferedReader(new InputStreamReader(jProcess.getErrorStream()));

	// wait for the process to be done
	jProcess.waitFor();
	
	// if 0 is not returned, print stdout to result printer for debugging
	if (jProcess.exitValue() != 0) {
	    // print output from so that we know what is going on
	    ResultPrinter.printError("Error running: " + flattenCommandArray(cmdArray));
	    ResultPrinter.printError("return value: " + jProcess.exitValue());
	    ResultPrinter.printError("stdout: ");
	    while (jOutStream.ready()) {
		ResultPrinter.printError("out:" + jOutStream.readLine());
	    }
	    // flush errors to disk
	    ResultPrinter.flushFileWriter();
	}

	if (jErrorStream.ready()) {
	    ResultPrinter.printError("Error messages while running: " + flattenCommandArray(cmdArray));
	    ResultPrinter.printError("err: ");
	    while (jErrorStream.ready()) {
		ResultPrinter.printError(":" + jErrorStream.readLine());
	    }
	    // flush errors to disk
	    ResultPrinter.flushFileWriter();
	    
	}

	if (jProcess.exitValue() != 0) {
	    return false;
	}

	// if we have an output stream to write to,
	// write out stdout there.
	if (outStream != null) {
	    // copy the data in the output stream to the file
	    while(jOutStream.ready()) {
		outStream.write(jOutStream.read());
	    }
	}
	// if we get here, streamit compilation succeeded
	return true;
    }

    /**
     * Expand a filename with a wildcard in it to an array of file names.
     **/
    public static String[] expandFileName(String fileName) {
	Vector filenames = new Vector(); // expanded filenames

	try {
	    // we are going to use ls to expand the filenames for us
	    ByteArrayOutputStream lsBuff = new ByteArrayOutputStream();
	    executeNative(getLsCommandOpts(fileName), lsBuff);
	    
	    // parse ls output:
	    StringTokenizer st = new StringTokenizer(lsBuff.toString(), // output from ls
						     "\n"); // split on newline

	    while(st.hasMoreTokens()) {
		String currentToken = st.nextToken();
		if (!currentToken.equals("")) {
		    filenames.add(currentToken);
		}
	    }
	} catch (Exception e) {
	    throw new RuntimeException("Caught exception expanding filenames: " + e.getMessage());
	}

	// copy from vector to array
	String[] sourceFiles = new String[filenames.size()];
	for (int i=0; i<sourceFiles.length; i++) {
	    sourceFiles[i] = (String)filenames.elementAt(i);
	}
	return sourceFiles;
    }
    
    public static String[] getLsCommandOpts(String path) {
	String[] args = new String[3];
	args[0] = "bash";
	args[1] = "-c";
	args[2] = "ls " + path;
	return args;
    }

    /**
     * Convertes a command array (of strings) into a
     * single string for display purposes.
     **/
    public static String flattenCommandArray(String[] arr) {
	String returnString = "";
	for (int i=0; i<arr.length; i++) {
	    returnString += arr[i] + " ";
	}
	return returnString;
    }
    
    
}
