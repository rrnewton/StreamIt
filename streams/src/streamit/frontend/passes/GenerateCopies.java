package streamit.frontend.passes;

import streamit.frontend.nodes.*;
import java.util.Collections;

/**
 * Generate code to copy structures and arrays elementwise.  In StreamIt,
 * assigning one composite object to another copies all of its members
 * (there are no references); this pass makes that copying explicit.
 * It also generates temporary variables for push, pop, and peek
 * statements to ensure that languages with references do not see
 * false copies.
 *
 * @author  David Maze &lt;dmaze@cag.lcs.mit.edu&gt;
 * @version $Id: GenerateCopies.java,v 1.1 2003-07-31 18:46:00 dmaze Exp $
 */
public class GenerateCopies extends SymbolTableVisitor
{
    private TempVarGen varGen;
   
    /**
     * Create a new copy generator.
     *
     * @param varGen  global temporary variable generator
     */
    public GenerateCopies(TempVarGen varGen)
    {
        super(null);
        this.varGen = varGen;
    }

    /**
     * Checks if variables of type type can be implemented as
     * a reference in Java or elsewhere.  This is true for arrays,
     * structures, and complex numbers.
     */
    private boolean needsCopy(Type type)
    {
        if (type instanceof TypeArray)
            return true;
        if (type instanceof TypeStruct)
            return true;
        if (type instanceof TypeStructRef)
            return true;
        if (type.isComplex())
            return true;
        return false;
    }

    /**
     * Checks if the result of the given expression can be implemented
     * as a reference in Java or elsewhere.
     */
    private boolean needsCopy(Expression expr)
    {
        return needsCopy(getType(expr));
    }

    /**
     * Use <code>addStatement</code> to add a statement assigning
     * <code>expr</code> to a new temporary, and return the expression
     * for the temporary.
     *
     * @param expr  expression to copy
     * @param deep  if true, generate a deep copy, as in {@link makeCopy}.
     * @return      variable expression for the temporary
     */
    private Expression assignToTemp(Expression expr, boolean deep)
    {
        String tempName = varGen.nextVar();
        Expression tempVar = new ExprVar(expr.getContext(), tempName);
        Type type = getType(expr);
        addStatement(new StmtVarDecl(expr.getContext(), type, tempName, null));
        symtab.registerVar(tempName, type, null, SymbolTable.KIND_LOCAL);
        if (deep)
            makeCopy(expr, tempVar);
        else
            addStatement(new StmtAssign(expr.getContext(), tempVar, expr));
        return tempVar;
    }

    /**
     * Use <code>addStatement</code> to generate a deep copy of the
     * (idempotent) expression in <code>from</code> into the (lvalue)
     * expression in <code>to</code>.
     */
    private void makeCopy(Expression from, Expression to)
    {
        // Assume that from and to have the same type.  What are we copying?
        Type type = getType(from);
        if (type instanceof TypeArray)
            makeCopyArray(from, to, (TypeArray)type);
        else if (type instanceof TypeStruct)
            makeCopyStruct(from, to, (TypeStruct)type);
        else if (type.isComplex())
            makeCopyComplex(from, to);
        else
            addStatement(new StmtAssign(to.getContext(), to, from));
    }

    private void makeCopyArray(Expression from, Expression to, TypeArray type)
    {
        // We need to generate a for loop, since from our point of
        // view, the array bounds may not be constant.
        String indexName = varGen.nextVar();
        ExprVar index = new ExprVar(null, indexName);
        Type intType = new TypePrimitive(TypePrimitive.TYPE_INT);
        Statement init =
            new StmtVarDecl(null, intType, indexName,
                            new ExprConstInt(null, 0));
        symtab.registerVar(indexName, intType, null, SymbolTable.KIND_LOCAL);
        Expression cond =
            new ExprBinary(null, ExprBinary.BINOP_LT, index, type.getLength());
        Statement incr =
            new StmtAssign(null, index,
                           new ExprBinary(null, ExprBinary.BINOP_ADD,
                                          index, new ExprConstInt(null, 1)));
        // Need to make a deep copy.  Existing machinery uses
        // addStatement(); visiting a StmtBlock will save this.
        // So, create a block containing a shallow copy, then
        // visit:
        Statement body =
            new StmtBlock(null,
                          Collections.singletonList(new StmtAssign(null,
                                                                   to,
                                                                   from)));
        body = (Statement)body.accept(this);

        // Now generate the loop, we have all the parts.
        addStatement(new StmtFor(null, incr, cond, incr, body));
    }

    private void makeCopyStruct(Expression from, Expression to,
                                TypeStruct type)
    {
        for (int i = 0; i < type.getNumFields(); i++)
        {
            String fname = type.getField(i);
            makeCopy(new ExprField(from.getContext(), from, fname),
                     new ExprField(to.getContext(), to, fname));
        }
    }

    private void makeCopyComplex(Expression from, Expression to)
    {
        addStatement
            (new StmtAssign(to.getContext(),
                            new ExprField(to.getContext(), to, "real"),
                            new ExprField(from.getContext(), from, "real")));
        addStatement
            (new StmtAssign(to.getContext(),
                            new ExprField(to.getContext(), to, "imag"),
                            new ExprField(from.getContext(), from, "imag")));
    }

    public Object visitExprPeek(ExprPeek expr)
    {
        Expression result = (Expression)super.visitExprPeek(expr);
        if (needsCopy(result))
            result = assignToTemp(result, false);
        return result;
    }
    
    public Object visitExprPop(ExprPop expr)
    {
        Expression result = (Expression)super.visitExprPop(expr);
        if (needsCopy(result))
            result = assignToTemp(result, false);
        return result;
    }

    public Object visitStmtAssign(StmtAssign stmt)
    {
        // recurse:
        Statement result = (Statement)super.visitStmtAssign(stmt);
        if (result instanceof StmtAssign) // it probably is:
        {
            stmt = (StmtAssign)result;
            if (needsCopy(stmt.getRHS()))
            {
                // drops op!  If there are compound assignments
                // like "a += b" here, we lose.  There shouldn't be,
                // though, since those operators aren't well-defined
                // for structures and arrays and this should be run
                // after complex prop.
                makeCopy(stmt.getRHS(), stmt.getLHS());
                return null;
            }
        }
        return result;
    }

    public Object visitStmtPush(StmtPush expr)
    {
        Expression value = (Expression)expr.getValue().accept(this);
        if (needsCopy(value))
            value = assignToTemp(value, true);
        return new StmtPush(expr.getContext(), value);
    }
}
