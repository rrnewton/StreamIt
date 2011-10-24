package at.dms.kjc.common;

import java.util.HashSet;
import java.util.Set;

import at.dms.util.*;
import at.dms.kjc.iterator.*;

import at.dms.kjc.*;
import at.dms.kjc.sir.*;
import at.dms.kjc.sir.lowering.partition.*;


public class HasIterationExpression extends SLIRReplacingVisitor {

    private boolean found;
    private final static String ITER_VAR_NAME = "iterationCount";

    public static Set<SIRFilter> doIt(SIRStream str) {
        WorkEstimate work = WorkEstimate.getWorkEstimate(str);
        WorkList workList = work.getSortedFilterWork();

        Set<SIRFilter> iterFilters = new HashSet<SIRFilter>();
        
        /* Debugging printer */ 
	/*       
        SIRPrinter printer = new SIRPrinter("SIR_sugar.txt");
        IterFactory.createFactory().createIter(str).accept(printer);
        printer.close();
        */

        for (int j = 0; j < workList.size(); j++) {
            SIRFilter filter = workList.getFilter(j);

            for (int i = 0; i < filter.getMethods().length; i++) {
                HasIterationExpression pass = new HasIterationExpression();
                filter.getMethods()[i].accept(pass);

                if (pass.found) {
                    // Filter found iteration expression.  
                    // Add to set to be desugared.
                    iterFilters.add(filter);
                }
            }
        }

        for (SIRFilter filter : iterFilters) {
            JVariableDefinition iterVar = new JVariableDefinition(
                Constants.ACC_PRIVATE,
                CStdType.Integer,
                ITER_VAR_NAME,
                new JIntLiteral(0));
            JFieldDeclaration iterField = new JFieldDeclaration(iterVar);
            filter.addField(iterField);

            filter.getWork().addStatement(
                new JExpressionStatement(
                    new JPostfixExpression(
                        Constants.OPE_POSTINC,
                        new JFieldAccessExpression(
                            new JThisExpression(),
                            ITER_VAR_NAME))));
        }

        /* Debugging printer */
        /*
        printer = new SIRPrinter("SIR_desugar.txt");
        IterFactory.createFactory().createIter(str).accept(printer);
        printer.close();
        */

        return iterFilters;
    }

    public Object visitIterationExpression(SIRIterationExpression iter) {
        this.found = true;
        JExpression jFieldCall = new JFieldAccessExpression(ITER_VAR_NAME);
        return jFieldCall;
    }

}
