package at.dms.kjc.common;

import java.util.HashSet;
import java.util.Set;

import at.dms.kjc.CStdType;
import at.dms.kjc.Constants;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JFieldAccessExpression;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JPostfixExpression;
import at.dms.kjc.JThisExpression;
import at.dms.kjc.JVariableDefinition;
import at.dms.kjc.SLIRReplacingVisitor;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRIterationExpression;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.lowering.partition.WorkEstimate;
import at.dms.kjc.sir.lowering.partition.WorkList;


public class LowerIterationExpression extends SLIRReplacingVisitor {

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
                LowerIterationExpression pass = new LowerIterationExpression();
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
                at.dms.classfile.Constants.ACC_PRIVATE,
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

    @Override
	public Object visitIterationExpression(SIRIterationExpression iter) {
        this.found = true;
        JExpression jFieldCall = new JFieldAccessExpression(ITER_VAR_NAME);
        return jFieldCall;
    }

}
