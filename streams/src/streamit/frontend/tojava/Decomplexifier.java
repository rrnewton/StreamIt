/*
 * Decomplexifier.java: convert complex expressions to real arithmetic
 * David Maze <dmaze@cag.lcs.mit.edu>
 * $Id: Decomplexifier.java,v 1.2 2002-07-11 20:57:26 dmaze Exp $
 */

package streamit.frontend.tojava;

import streamit.frontend.nodes.*;

/**
 * Convert complex expressions into separate real and imaginary parts.
 * Doing this requires a symbol table (to get the types of variables),
 * plus a temporary symbol generator.  If an expression is real, it
 * remains unchanged; otherwise it is replaced with a new temporary
 * variable, and code is generated to initialize the temporary.
 */
public class Decomplexifier
{
    /** Record class for the results of decomplexification. */
    public static class Result
    {
        public String statements;
        public Expression exp;

        public Result(String stmts, Expression e)
        {
            statements = stmts;
            exp = e;
        }
        
        public Result(Expression e)
        {
            this("", e);
        }
    }

    /** Turn an expression into separate real and complex parts,
     * generating temporary variables if need be.  The return value is
     * a Result, whose statements need to be executed before the
     * current statement and whose expression should replace exp. */
    public static Result decomplexify(Expression exp, TempVarGen varGen,
                                      NodesToJava n2j)
    {
        // If the expression is complex, generate a temporary,
        // generate the appropriate assign statements, and return
        // the temporary.
        if (exp instanceof ExprComplex)
        {
            int num = varGen.nextVar("Complex");
            String varName = varGen.varName(num);
            Expression varExpr = new ExprVar(varName);
            Result result = new Result(varExpr);
            Expression lhsr = new ExprField(varExpr, "real");
            Expression lhsi = new ExprField(varExpr, "imag");
            ExprComplex cplx = (ExprComplex)exp;
            
            result.statements += (String)lhsr.accept(n2j) + " = " +
                (String)cplx.getReal().accept(n2j) + ";\n";
            result.statements += (String)lhsi.accept(n2j) + " = " +
                (String)cplx.getImag().accept(n2j) + ";\n";
            return result;
        }
        
        return new Result(exp);
    }
}

