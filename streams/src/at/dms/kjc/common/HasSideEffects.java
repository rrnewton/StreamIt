package at.dms.kjc.common;

import at.dms.kjc.CType;
import at.dms.kjc.JAssignmentExpression;
import at.dms.kjc.JCompoundAssignmentExpression;
import at.dms.kjc.JExpression;
import at.dms.kjc.JMethodCallExpression;
import at.dms.kjc.JPostfixExpression;
import at.dms.kjc.JPrefixExpression;
import at.dms.kjc.JStatement;
import at.dms.kjc.SLIREmptyVisitor;
import at.dms.kjc.sir.InlineAssembly;
import at.dms.kjc.sir.SIRCreatePortal;
import at.dms.kjc.sir.SIRPeekExpression;
import at.dms.kjc.sir.SIRPopExpression;
import at.dms.kjc.sir.SIRPushExpression;
import at.dms.util.Utils;


/**
 * This class determines, given an expression, if the expression
 * has any side effects.
 *
 *
 * @author Michael Gordon
 */

public class HasSideEffects extends SLIREmptyVisitor 
{
    private boolean sideEffect;

    
    /**
     * Return true if an expression has side effects, 
     * a method call, a prefix/postfix expression, an assignment expression
     *
     *
     * @param entry The expression we are interested in
     *
     *
     * @return true if the expression has side effects, false otherwise.
     * 
     */
    public static boolean hasSideEffects(JExpression entry) 
    {
        HasSideEffects hse = new HasSideEffects();
        entry.accept(hse);
        return hse.sideEffect;
    }
    
    public static boolean hasSideEffects(JStatement entry) 
    {
        return HasSideEffects.hasSideEffects(Utils.getExpression(entry));
    }

    private HasSideEffects() 
    {
        sideEffect = false;
    }


    /**
     * prints a prefix expression
     */
    @Override
	public void visitPrefixExpression(JPrefixExpression self,
                                      int oper,
                                      JExpression expr) {
        sideEffect = true;
        expr.accept(this);
    }
    
    /**
     * prints a method call expression
     */
    @Override
	public void visitMethodCallExpression(JMethodCallExpression self,
                                          JExpression prefix,
                                          String ident,
                                          JExpression[] args) {
        sideEffect = true;
        if (prefix != null) {
            prefix.accept(this);
        }
        visitArgs(args);
    }
    
    /**
     * prints an assignment expression
     */
    @Override
	public void visitAssignmentExpression(JAssignmentExpression self,
                                          JExpression left,
                                          JExpression right) {
        sideEffect = true;
        left.accept(this);
        right.accept(this);
    }

    /**
     * prints a compound expression
     */
    @Override
	public void visitCompoundAssignmentExpression(JCompoundAssignmentExpression self,
                                                  int oper,
                                                  JExpression left,
                                                  JExpression right) {
        sideEffect = true;
        left.accept(this);
        right.accept(this);
    }

    /**
     * prints a postfix expression
     */
    @Override
	public void visitPostfixExpression(JPostfixExpression self,
                                       int oper,
                                       JExpression expr) {
        sideEffect = true;
        expr.accept(this);
    }

    @Override
	public void visitCreatePortalExpression(SIRCreatePortal self) {
        sideEffect = true;
    }

    /**
     * Visits a peek expression.
     */
    @Override
	public void visitPeekExpression(SIRPeekExpression self,
                                    CType tapeType,
                                    JExpression arg) {
        arg.accept(this);
    }

    /**
     * Visits a pop expression.
     */
    @Override
	public void visitPopExpression(SIRPopExpression self,
                                   CType tapeType) {
        sideEffect = true;
    }

    /**
     * Visits a push expression.
     */
    @Override
	public void visitPushExpression(SIRPushExpression self,
                                    CType tapeType,
                                    JExpression arg) {
        sideEffect = true;
        arg.accept(this);
    }
  
    /**
     * Visits InlineAssembly
     */
    @Override
	public void visitInlineAssembly(InlineAssembly self,String[] asm,
                                    String[] input,String[] clobber) {
        sideEffect = true;
    
    }

    
}
