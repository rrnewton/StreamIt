package at.dms.kjc.tilera.arrayassignment;

import at.dms.kjc.*;
import at.dms.kjc.tilera.*;

public class LoopedAssignments implements AAStatement {
    public static final int MIN_LOOP_ITERATIONS = 10;
    
    String dstBufName;
    String srcBufName;
    int dstStartIndex;
    int srcStartIndex;
    String dstOffsetName;
    String srcOffsetName;
    int iterations;
    
    public LoopedAssignments(SingleAAStmt stmt) {
        this.dstBufName = stmt.dstBufName;
        if (dstOffsetName == null)
            dstOffsetName = "";
        this.dstOffsetName = stmt.dstOffsetName;
        this.dstStartIndex = stmt.dstIndex;

        this.srcBufName = stmt.srcBufName;
        if (srcOffsetName == null)
            srcOffsetName = "";
        this.srcOffsetName = stmt.srcOffsetName;

        this.srcStartIndex = stmt.srcIndex;
        iterations = 1;
    }
    
    public LoopedAssignments(String dstBufName, String dstOffsetName, int dstStartIndex, 
            String srcBufName, String srcOffsetName, int srcStartIndex) {
        this.dstBufName = dstBufName;
        if (dstOffsetName == null)
            dstOffsetName = "";
        this.dstOffsetName = dstOffsetName;
        this.dstStartIndex = dstStartIndex;

        this.srcBufName = srcBufName;
        if (srcOffsetName == null)
            srcOffsetName = "";
        this.srcOffsetName = srcOffsetName;

        this.srcStartIndex = srcStartIndex;
        iterations = 1;
    }
    
    /**
     *  Decide if we should add this stmt to the current loop by checking all of the fields, if so, 
     *  return this loopedAssignment, otherwise, return a new looped assignment with the statement added.
     */
    public LoopedAssignments addStmt(SingleAAStmt stmt) {
        if (this.dstBufName == stmt.dstBufName &&
                this.srcBufName == stmt.srcBufName &&
                this.dstOffsetName == stmt.dstOffsetName &&
                this.srcOffsetName == stmt.srcOffsetName &&
                this.dstStartIndex + iterations == stmt.dstIndex &&
                this.srcStartIndex + iterations == stmt.srcIndex) {
            iterations++;
            return this;
        } else {
            return new LoopedAssignments(stmt);
        }
    }
     
    public void addIteration() {
        iterations ++;
    }
    
    public JStatement toJStmt() {
        String dstOffset = dstOffsetName.equals("") ? "" : dstOffsetName + " + ";
        String srcOffset = srcOffsetName.equals("") ? "" : srcOffsetName + " + ";

        if (iterations > MIN_LOOP_ITERATIONS) {
            //MAKE A LOOP
            String iv = "__ias__";
            String loop = "for (int " + iv + " = 0; " + iv + " < " + iterations + "; " + iv + "++) ";
            loop += dstBufName + "[" + dstOffset + dstStartIndex + " + " + iv + "] = " + 
                    srcBufName + "[" + srcOffset + srcStartIndex + " + " + iv + "]";
            return Util.toStmt(loop);
        } else {
            //omit the statements by one by one
            JBlock block = new JBlock();
            for (int i = 0; i < iterations; i++) {
                String stmt  = dstBufName + "[" + dstOffset + (dstStartIndex + i) + "] = " + 
                srcBufName + "[" + srcOffset + (srcStartIndex + i) + "]"; 
                block.addStatement(Util.toStmt(stmt));
            }
            return block;
        }
        
    }
}
