//===========================================================================
//
//   FILE: SIRPrinter.java:
//   
//   Author: Michael Gordon
//   Date: Tue Oct  2 19:49:17 2001
//
//   Function:  print the sir
//
//===========================================================================


package at.dms.util;

import java.io.IOException;

import at.dms.kjc.JExpression;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.iterator.SIRFeedbackLoopIter;
import at.dms.kjc.iterator.SIRFilterIter;
import at.dms.kjc.iterator.SIRPhasedFilterIter;
import at.dms.kjc.iterator.SIRPipelineIter;
import at.dms.kjc.iterator.SIRSplitJoinIter;
import at.dms.kjc.sir.SIRFeedbackLoop;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRJoinType;
import at.dms.kjc.sir.SIRJoiner;
import at.dms.kjc.sir.SIRPhasedFilter;
import at.dms.kjc.sir.SIRPipeline;
import at.dms.kjc.sir.SIRSplitJoin;
import at.dms.kjc.sir.SIRSplitType;
import at.dms.kjc.sir.SIRSplitter;
import at.dms.kjc.sir.StreamVisitor;

public class SIRPrinter extends IRPrinter implements StreamVisitor {
    
    /* visit a structure */

    /* I don't think a structure can show up in the stream graph
     * anymore, so commenting this out.  --bft 
     *
     public void visitStructure(SIRStructure self,
     SIRStream parent,
     JFieldDeclaration[] fields) {
     blockStart("Structure");
     attrStart("Parent");
     if (parent == null)
     printData("null");
     attrEnd();
     for (int i = 0; i < fields.length; i++)
     fields[i].accept(this);
     blockEnd();
     }
    */

    /**
     * Build an IRPrinter for a particular file.
     *
     * @param filename  Name of the file to write IR to
     */
    public SIRPrinter(String filename) {
        super(filename);
    }
    
    /* visit a filter */
    @Override
	public void visitFilter(SIRFilter self,
                            SIRFilterIter iter) {
        blockStart("Filter");
        attrStart("Parent");
        if (iter.getParent() == null)
            printData("null");
        attrEnd();
        JFieldDeclaration[] fields = self.getFields();
        for (int i = 0; i < fields.length; i++)
            fields[i].accept(this);
        JMethodDeclaration[] methods = self.getMethods();
        for (int i = 0; i < methods.length; i++)
            methods[i].accept(this);
        attrPrint("peek", self.getPeek());
        attrPrint("pop", self.getPop());
        attrPrint("push", self.getPush());
        if (self.getInputType() != null)
            attrPrint("InputType", self.getInputType().toString());
        if (self.getOutputType() != null)
            attrPrint("OutputType", self.getOutputType().toString());
        blockEnd();
    }
        
    /* visit a filter */
    @Override
	public void visitPhasedFilter(SIRPhasedFilter self,
                                  SIRPhasedFilterIter iter) {
        blockStart("PhasedFilter");
        attrStart("Parent");
        if (iter.getParent() == null)
            printData("null");
        attrEnd();
        JFieldDeclaration[] fields = self.getFields();
        for (int i = 0; i < fields.length; i++)
            fields[i].accept(this);
        JMethodDeclaration[] methods = self.getMethods();
        for (int i = 0; i < methods.length; i++)
            methods[i].accept(this);
        JMethodDeclaration[] phases = self.getPhases();
        if (phases != null) {
            blockStart("phases");
            for (int i = 0; i < phases.length; i++)
                {
                    attrPrint("peek", phases[i].getPeek());
                    attrPrint("pop", phases[i].getPop());
                    attrPrint("push", phases[i].getPush());
                    phases[i].accept(this);
                }
            blockEnd();
        }
        if (self.getInputType() != null)
            attrPrint("InputType", self.getInputType().toString());
        if (self.getOutputType() != null)
            attrPrint("OutputType", self.getOutputType().toString());
        blockEnd();
    }
        
    String getSplitString(SIRSplitType s) {
        if (s == SIRSplitType.ROUND_ROBIN)
            return "Round-Robin";
        if (s == SIRSplitType.DUPLICATE)
            return "Duplicate";
        if (s == SIRSplitType.NULL)
            return "Null";
        if (s == SIRSplitType.WEIGHTED_RR)
            return "Weighted Round-Robin";
        else return "Unknown";
    }


    String getJoinString(SIRJoinType s) {
        if (s == SIRJoinType.ROUND_ROBIN)
            return "Round-Robin";
        if (s == SIRJoinType.COMBINE)
            return "Combine";
        if (s == SIRJoinType.NULL)
            return "Null";
        if (s == SIRJoinType.WEIGHTED_RR)
            return "Weighted Round-Robin";
        else return "Unknown";
    }


    /* visit a splitter */
    public void visitSplitter(SIRSplitter self) {
        blockStart("Splitter");
        attrPrint("Type", getSplitString(self.getType()));
        attrStart("Weights");
        JExpression[] weights = self.getInternalWeights();
        if (weights != null)
            for (int i = 0; i < weights.length; i++)
                weights[i].accept(this);
        attrEnd();
        blockEnd();
    }
    
    /* visit a joiner */
    public void visitJoiner(SIRJoiner self) {
        blockStart("Joiner");
        attrPrint("Type", getJoinString(self.getType()));
        attrStart("Weights");
        JExpression[] weights = self.getInternalWeights();
        if (weights != null)
            for (int i = 0; i < weights.length; i++)
                weights[i].accept(this);
        attrEnd();
        blockEnd();
    }

    
    protected void printData(int data) {
        try
            {
                p.write(data);
            }
        catch (IOException e)
            {
                System.err.println(e);
                System.exit(1);
            }
    }
 

    protected void attrPrint(String name, int i) {
        attrStart(name);
        printData(" ");
        printData(i);
        attrEnd();
    }

  
    /**
     * PRE-VISITS 
     */
        
    /* pre-visit a pipeline */
    @Override
	public void preVisitPipeline(SIRPipeline self,
                                 SIRPipelineIter iter) {

        blockStart("Pipeline");
        attrStart("Parent");
        if (iter.getParent() == null)
            printData("null");
        attrEnd();
        JFieldDeclaration[] fields = self.getFields();
        for (int i = 0; i < fields.length; i++)
            fields[i].accept(this);
        JMethodDeclaration[] methods = self.getMethods();
        for (int i = 0; i < methods.length; i++)
            methods[i].accept(this);
        blockEnd();
    }

    /* pre-visit a splitjoin */
    @Override
	public void preVisitSplitJoin(SIRSplitJoin self,
                                  SIRSplitJoinIter iter) {
        blockStart("SplitJoin");
        attrStart("Parent");
        if (iter.getParent() == null)
            printData("null");
        attrEnd();

        JFieldDeclaration[] fields = self.getFields();
        for (int i = 0; i < fields.length; i++)
            fields[i].accept(this);
        JMethodDeclaration[] methods = self.getMethods();
        for (int i = 0; i < methods.length; i++)
            methods[i].accept(this);
        blockEnd();
        visitSplitter(self.getSplitter());
        visitJoiner(self.getJoiner());
    }

    /* pre-visit a feedbackloop */
    @Override
	public void preVisitFeedbackLoop(SIRFeedbackLoop self,
                                     SIRFeedbackLoopIter iter) {
        blockStart("FeedbackLoop");
        attrStart("Parent");
        if (iter.getParent() == null)
            printData("null");
        attrEnd();
        attrStart("delay");
        self.getDelay().accept(this);
        attrEnd();
        attrStart("InitPath");
        self.getInitPath().accept(this);
        attrEnd();
        JFieldDeclaration[] fields = self.getFields();
        for (int i = 0; i < fields.length; i++)
            fields[i].accept(this);
        JMethodDeclaration[] methods = self.getMethods();
        for (int i = 0; i < methods.length; i++)
            methods[i].accept(this);
        blockEnd();
        visitJoiner(self.getJoiner());
        visitSplitter(self.getSplitter());
    }

    /**
     * POST-VISITS 
     */
        
    /* post-visit a pipeline */
    @Override
	public void postVisitPipeline(SIRPipeline self,
                                  SIRPipelineIter iter) {
    }

    /* post-visit a splitjoin */
    @Override
	public void postVisitSplitJoin(SIRSplitJoin self,
                                   SIRSplitJoinIter iter) {
    }

    /* post-visit a feedbackloop */
    @Override
	public void postVisitFeedbackLoop(SIRFeedbackLoop self,
                                      SIRFeedbackLoopIter iter) {
    }
}
