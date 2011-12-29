
package at.dms.kjc.sir.lowering;

import at.dms.kjc.CType;
import at.dms.kjc.JExpression;
import at.dms.kjc.JStatement;
import at.dms.kjc.SLIREmptyVisitor;
import at.dms.kjc.sir.SIRPeekExpression;
import at.dms.kjc.sir.SIRPopExpression;
import at.dms.kjc.sir.SIRPushExpression;

public class FindTapeOps extends SLIREmptyVisitor {

    public static boolean findTapeOps(JStatement body) {
        FindTapeOps test = new FindTapeOps();
        body.accept(test);
        //System.out.println("FindTapeOps.findTapeOps result = "+test.tape_op);
        return test.tape_op;
    }


    // private fields and methods

    private boolean tape_op;

    public FindTapeOps() { tape_op = false; }
    
    @Override
	public void visitPushExpression(SIRPushExpression self,
                                    CType tapeType,
                                    JExpression val)
    {
        tape_op = true;
    }
    
    @Override
	public void visitPopExpression(SIRPopExpression self,
                                   CType tapeType)
    {
        tape_op = true;
    }    

    @Override
	public void visitPeekExpression(SIRPeekExpression self,
                                    CType tapeType,
                                    JExpression num)
    {
        tape_op = true;
    }
}
