package at.dms.kjc.rstream;
import at.dms.kjc.JExpression;
import at.dms.kjc.JLocalVariable;

class DoLoopInformation 
{
    JExpression init;
    JExpression cond;
    JExpression incr;
    JLocalVariable induction;
}

