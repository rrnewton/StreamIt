package at.dms.kjc.rstream;

import java.util.Iterator;
import java.util.LinkedList;

import at.dms.kjc.CType;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.sir.EmptyAttributeStreamVisitor;
import at.dms.kjc.sir.SIRFeedbackLoop;
import at.dms.kjc.sir.SIRFileReader;
import at.dms.kjc.sir.SIRFileWriter;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRJoiner;
import at.dms.kjc.sir.SIROperator;
import at.dms.kjc.sir.SIRPipeline;
import at.dms.kjc.sir.SIRSplitJoin;
import at.dms.kjc.sir.SIRSplitter;
import at.dms.kjc.sir.SIRStream;

/**
 * This class visits the stream graph and converts all 
 * SIRFileReaders and SIRFileWriters to normal (non-predefined)
 * filters with explicit calls to fopen in the init and fscanf or
 * fprintf in the work function.  This is done because the partitioner
 * does not currently support fusion of predefinied filters.
 *
 * @author Michael Gordon
 * 
 */
public class ConvertFileFilters extends EmptyAttributeStreamVisitor 
{
    /**
     * The main entry-point to this class, visit all filters in 
     * str and convert as above.
     * @param str the toplevel of the stream graph
     */
    public static void doit(SIRStream str) 
    {
        str.accept(new ConvertFileFilters());
    
    } 
    
    /**
     * Visit each stream in a pipeline and reset the pipeline
     * to be the children returned by the attribute visitor.
     *
     * @param self the pipeline
     * @param fields the fields of the pipeline
     * @param methods the methods of the pipeline     
     * @param init the init function of the pipeline
     *
     * @return The new pipeline.
     * 
     */
    @Override
	public Object visitPipeline(SIRPipeline self,
                                JFieldDeclaration[] fields,
                                JMethodDeclaration[] methods,
                                JMethodDeclaration init) {
        /** visit each child. **/
        LinkedList<SIRStream> newChildren = new LinkedList<SIRStream>();
        Iterator childIter = self.getChildren().iterator();
        while(childIter.hasNext()) {
            SIROperator currentChild = (SIROperator)childIter.next();
            newChildren.add((SIRStream)currentChild.accept(this));
        }
        self.setChildren(newChildren);
        return self;
    }

    /**
     * Visit a splitjoin and reset the parallel children to be
     * the children returned by the attribute visitor.
     */
    @Override
	public Object visitSplitJoin(SIRSplitJoin self,
                                 JFieldDeclaration[] fields,
                                 JMethodDeclaration[] methods,
                                 JMethodDeclaration init,
                                 SIRSplitter splitter,
                                 SIRJoiner joiner) {
        LinkedList<SIRStream> newChildren = new LinkedList<SIRStream>();
        // visit splitter
        self.getSplitter().accept(this);
        // visit children
        Iterator childIter = self.getChildren().iterator();
        while(childIter.hasNext()) {
            SIROperator currentChild = (SIROperator)childIter.next();
            //don't visit splitter or joiner again.
            if (currentChild instanceof SIRSplitter || 
                currentChild instanceof SIRJoiner)
                continue;
            newChildren.add((SIRStream) currentChild.accept(this));
        }
        // visit joiner 
        self.getJoiner().accept(this);
        self.setParallelStreams(newChildren);

        return self;
    }

    /**
     * Visit a feedbackloop and reset the loop and body to be the
     * loop and body as returned by the attribute stream visitor
     */
    @Override
	public Object visitFeedbackLoop(SIRFeedbackLoop self,
                                    JFieldDeclaration[] fields,
                                    JMethodDeclaration[] methods,
                                    JMethodDeclaration init,
                                    JMethodDeclaration initPath) {
        // visit the body
        self.setBody((SIRStream)self.getBody().accept(this));
        // visit the loop
        self.setLoop((SIRStream)self.getLoop().accept(this));
        return self;
    }

    /**
     * Visit a filter, if the filter is an SIRFile Reader or Writer
     * replace it with a FileReader or FileWriter, respectively.
     */
    @Override
	public Object visitFilter(SIRFilter self,
                              JFieldDeclaration[] fields,
                              JMethodDeclaration[] methods,
                              JMethodDeclaration init,
                              JMethodDeclaration work,
                              CType inputType, CType outputType) {
        super.visitFilter(self, fields, methods, init, work, inputType, outputType);

    
        if (self instanceof SIRFileReader) {
            System.out.println("Replacing file reader " + self);
            return new FileReader((SIRFileReader)self);
        }
        else if (self instanceof SIRFileWriter) {
            //do something here...
            System.out.println("Replacing file writer " + self);        
            return new File_Writer((SIRFileWriter)self);
        }
        else {
            return self;
        }
    }
    
}
