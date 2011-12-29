package at.dms.kjc.sir.lowering;

import at.dms.kjc.CStdType;
import at.dms.kjc.JAddExpression;
import at.dms.kjc.JExpression;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JFloatLiteral;
import at.dms.kjc.JMethodCallExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.KjcVisitor;
import at.dms.kjc.SLIREmptyVisitor;
import at.dms.kjc.iterator.IterFactory;
import at.dms.kjc.iterator.SIRIterator;
import at.dms.kjc.sir.EmptyStreamVisitor;
import at.dms.kjc.sir.SIRStream;

/**
 * This class converts calls "round(x)" to "floor(x+0.5)".  Oddly,
 * with single precision, this seems to be required to get the right
 * answer on someb applications (e.g., MPEG).
 */
public class RoundToFloor extends EmptyStreamVisitor {

    /**
     * Converts all calls in methods and fields of <str> or children.
     */ 
    public static void doit(SIRStream str) {
        IterFactory.createFactory().createIter(str).accept(new RoundToFloor());
    }

    @Override
	public void preVisitStream(SIRStream self,
                               SIRIterator iter) {
        // visit methods
        JMethodDeclaration[] methods = self.getMethods();
        for (int i=0; i<methods.length; i++) {
            methods[i].accept(rtf);
        }
        // visit fields (to hit initializers)
        JFieldDeclaration[] fields = self.getFields();
        for (int i=0; i<fields.length; i++) {
            fields[i].accept(rtf);
        }
    }

    /**
     * Class to visit Kopi nodes to do conversion.  (RTF stands for
     * Round-To-Floor).
     */
    KjcVisitor rtf = new SLIREmptyVisitor() {
            @Override
			public void visitMethodCallExpression(JMethodCallExpression self,
                                                  JExpression prefix,
                                                  String ident,
                                                  JExpression[] args) {
                super.visitMethodCallExpression(self, prefix, ident, args);
                // do conversion
                if (ident.equals("round")) {
                    // should have only one argument
                    assert args.length == 1;
                    // change ident to floor
                    self.setIdent("floor");
                    // add 0.5 to argument
                    self.setArgs(new JExpression[] { 
                        new JAddExpression(new JFloatLiteral(0.5f), args[0])
                    });
                    self.setTapeType(CStdType.Float);
                }
            }
        };
}
