/*
 * NameAnonymousFunctions.java: replace anonymous functions with named ones
 * David Maze <dmaze@cag.lcs.mit.edu>
 * $Id: NameAnonymousFunctions.java,v 1.3 2003-01-09 19:48:37 dmaze Exp $
 */

package streamit.frontend.tojava;

import streamit.frontend.nodes.*;

public class NameAnonymousFunctions extends FEReplacer
{
    public Object visitFunction(Function func)
    {
        func = (Function)super.visitFunction(func);
        if (func.getName() != null) return func;
        String name = null;
        switch(func.getCls())
        {
        case Function.FUNC_INIT: name = "init"; break;
        case Function.FUNC_WORK: name = "work"; break;
        case Function.FUNC_HANDLER: return func;
        case Function.FUNC_HELPER: return func;
        default: return func;
        }
        return new Function(func.getContext(), func.getCls(),
                            name, func.getReturnType(),
                            func.getParams(), func.getBody());
    }
    
    public Object visitFuncWork(FuncWork func)
    {
        func = (FuncWork)super.visitFuncWork(func);
        if (func.getName() != null) return func;
        return new FuncWork(func.getContext(), func.getCls(), "work",
                            func.getBody(), func.getPeekRate(),
                            func.getPopRate(), func.getPushRate());
    }
}
