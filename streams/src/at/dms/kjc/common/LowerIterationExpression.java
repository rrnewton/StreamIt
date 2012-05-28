package at.dms.kjc.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.dms.kjc.CStdType;
import at.dms.kjc.Constants;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JFieldAccessExpression;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JPostfixExpression;
import at.dms.kjc.JThisExpression;
import at.dms.kjc.JVariableDefinition;
import at.dms.kjc.SLIRReplacingVisitor;
import at.dms.kjc.iterator.IterFactory;
import at.dms.kjc.iterator.SIRFilterIter;
import at.dms.kjc.iterator.SIRIterator;
import at.dms.kjc.sir.EmptyStreamVisitor;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRIterationExpression;
import at.dms.kjc.sir.SIRStream;


public class LowerIterationExpression extends SLIRReplacingVisitor {
    public final static String ITER_VAR_NAME = "__iterationCount";
    public final static String PREWORK_METHOD_NAME = "prework";

    private boolean found;

    public static Set<SIRFilter> doIt(SIRStream str) {
        SIRIterator it = IterFactory.createFactory().createIter(str);
        final List<SIRFilter> filters = new ArrayList<SIRFilter>();
        it.accept(new EmptyStreamVisitor() {

            @Override
            public void visitFilter(SIRFilter self,
                                    SIRFilterIter iter) {
                filters.add(self);
            }
            
        });

        
        Set<SIRFilter> iterFilters = new HashSet<SIRFilter>();
        
        /* Debugging printer */ 
//        SIRPrinter printer = new SIRPrinter("SIR_sugar.sir");
//        IterFactory.createFactory().createIter(str).accept(printer);
//        printer.close();

        for (SIRFilter filter : filters) {
            JMethodDeclaration prework = null;
            
            for (int i = 0; i < filter.getMethods().length; i++) {
                JMethodDeclaration method = filter.getMethods()[i];

                if (method.getName().equals(PREWORK_METHOD_NAME)) {
                    prework = method;
                }

                LowerIterationExpression pass = new LowerIterationExpression();
                filter.getMethods()[i].accept(pass);

                if (pass.found) {
                    // Filter found iteration expression.  
                    // Add to set to be desugared.
                    filter.setIterationFilter(true);
                    iterFilters.add(filter);
                    if (prework != null) {
                        addIterIncrementStatement(prework);
                    }
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
            addIterIncrementStatement(filter.getWork());
        }

        /* Debugging printer */
//        printer = new SIRPrinter("SIR_desugar.sir");
//        IterFactory.createFactory().createIter(str).accept(printer);
//        printer.close();

        return iterFilters;
    }

    private static void addIterIncrementStatement(JMethodDeclaration method) {
        method.addStatement(
            new JExpressionStatement(
                new JPostfixExpression(
                    Constants.OPE_POSTINC,
                    new JFieldAccessExpression(
                        new JThisExpression(),
                        ITER_VAR_NAME))));
    }

    @Override
	public Object visitIterationExpression(SIRIterationExpression iter) {
        this.found = true;
        JExpression jFieldCall = new JFieldAccessExpression(ITER_VAR_NAME);
        return jFieldCall;
    }

}
