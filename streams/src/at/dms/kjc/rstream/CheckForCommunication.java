package at.dms.kjc.rstream;

import java.util.ListIterator;

import at.dms.kjc.CType;
import at.dms.kjc.JExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JStatement;
import at.dms.kjc.SLIREmptyVisitor;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRPeekExpression;
import at.dms.kjc.sir.SIRPopExpression;
import at.dms.kjc.sir.SIRPushExpression;

/**
 * This class check to see whether a filter contains push
 * pop or peek statements.
 *
 *
 * @author Michael Gordon
 */
    
class CheckForCommunication extends SLIREmptyVisitor
{
    private boolean found;
    
    private CheckForCommunication() 
    {
        found = false;
    }
    
    /**
     * This function returns true if *filter* contains
     * a push, pop, or peek statement in any of its 
     * methods.
     *
     * @param filter The filter we want to check for communication.
     *
     *
     * @return true if the filter contains a push, pop or peek statement.
     * 
     */
    public static boolean check(SIRFilter filter) 
    {
        CheckForCommunication checker = new CheckForCommunication();

        //iterate over the methods to check for a comm. exp.
        JMethodDeclaration[] methods = filter.getMethods();
        for (int i = 0; i < methods.length; i++) {
            //iterate over the statements
            for (ListIterator it = methods[i].getStatementIterator();
                 it.hasNext(); ){
                ((JStatement)it.next()).accept(checker);
            }
        }
        return checker.found;
    }
    
    /**
     * Visitor method for pop expressions, set found to true
     * if we get here.  Called by the visitor.
     *
     */
    public void visitPopExpression(SIRPopExpression oldSelf,
                                   CType oldTapeType) {
      
        found = true;
    }
    
    /**
     * Visitor method for peek expressions, set found to true
     * if we get here.  Called by the visitor.
     *
     */
    public void visitPeekExpression(SIRPeekExpression oldSelf,
                                    CType oldTapeType,
                                    JExpression oldArg) {
        found = true;
    }
    
    /**
     * Visitor method for push expressions, set found to true
     * if we get here.  Called by the visitor.
     *
     */
    public void visitPushExpression(SIRPushExpression self,
                                    CType tapeType,
                                    JExpression arg) {
        found = true;
    }
}
