package at.dms.kjc.spacedynamic;

import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.SLIRReplacingVisitor;
import at.dms.kjc.flatgraph.FlatNode;
import at.dms.kjc.flatgraph.FlatVisitor;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRPrintStatement;

//remove all print statements in code
public class RemovePrintStatements implements FlatVisitor {
    
    public static void doIt(FlatNode top) {
        top.accept(new RemovePrintStatements(), null, true);
    }
    
    @Override
	public void visitNode(FlatNode node) {
        if (node.isFilter()) {
            SIRFilter filter = (SIRFilter)node.contents;
            for (int i = 0; i < filter.getMethods().length; i++)
                filter.getMethods()[i].accept(new RemovePrintStatementsHelper());
        }
    }

    static class RemovePrintStatementsHelper extends SLIRReplacingVisitor {


        @Override
		public Object visitPrintStatement(SIRPrintStatement oldself,
                                          JExpression exp) {

            SIRPrintStatement self = (SIRPrintStatement)
                super.visitPrintStatement(oldself, exp);
    
            return new JExpressionStatement(null, self.getArg(), null);
        }
    
    }
}
