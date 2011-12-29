/**
 * 
 */
package at.dms.kjc.sir.lowering;

import java.util.Stack;

import at.dms.kjc.ExpressionVisitorBase;
import at.dms.kjc.JAddExpression;
import at.dms.kjc.JArrayAccessExpression;
import at.dms.kjc.JArrayInitializer;
import at.dms.kjc.JArrayLengthExpression;
import at.dms.kjc.JAssignmentExpression;
import at.dms.kjc.JBinaryArithmeticExpression;
import at.dms.kjc.JBinaryExpression;
import at.dms.kjc.JBitwiseComplementExpression;
import at.dms.kjc.JBitwiseExpression;
import at.dms.kjc.JBooleanLiteral;
import at.dms.kjc.JByteLiteral;
import at.dms.kjc.JCastExpression;
import at.dms.kjc.JCharLiteral;
import at.dms.kjc.JCheckedExpression;
import at.dms.kjc.JClassExpression;
import at.dms.kjc.JCompoundAssignmentExpression;
import at.dms.kjc.JConditionalAndExpression;
import at.dms.kjc.JConditionalExpression;
import at.dms.kjc.JConditionalOrExpression;
import at.dms.kjc.JConstructorCall;
import at.dms.kjc.JDivideExpression;
import at.dms.kjc.JDoubleLiteral;
import at.dms.kjc.JEqualityExpression;
import at.dms.kjc.JExpression;
import at.dms.kjc.JFieldAccessExpression;
import at.dms.kjc.JFloatLiteral;
import at.dms.kjc.JInstanceofExpression;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JLiteral;
import at.dms.kjc.JLocalVariableExpression;
import at.dms.kjc.JLogicalComplementExpression;
import at.dms.kjc.JLongLiteral;
import at.dms.kjc.JMethodCallExpression;
import at.dms.kjc.JMinusExpression;
import at.dms.kjc.JModuloExpression;
import at.dms.kjc.JMultExpression;
import at.dms.kjc.JNameExpression;
import at.dms.kjc.JNewArrayExpression;
import at.dms.kjc.JNullLiteral;
import at.dms.kjc.JParenthesedExpression;
import at.dms.kjc.JPostfixExpression;
import at.dms.kjc.JPrefixExpression;
import at.dms.kjc.JQualifiedAnonymousCreation;
import at.dms.kjc.JQualifiedInstanceCreation;
import at.dms.kjc.JRelationalExpression;
import at.dms.kjc.JShiftExpression;
import at.dms.kjc.JShortLiteral;
import at.dms.kjc.JStringLiteral;
import at.dms.kjc.JSuperExpression;
import at.dms.kjc.JThisExpression;
import at.dms.kjc.JTypeNameExpression;
import at.dms.kjc.JUnaryExpression;
import at.dms.kjc.JUnaryMinusExpression;
import at.dms.kjc.JUnaryPlusExpression;
import at.dms.kjc.JUnaryPromote;
import at.dms.kjc.JUnqualifiedAnonymousCreation;
import at.dms.kjc.JUnqualifiedInstanceCreation;
import at.dms.kjc.sir.SIRCreatePortal;
import at.dms.kjc.sir.SIRDynamicToken;
import at.dms.kjc.sir.SIRInterfaceTable;
import at.dms.kjc.sir.SIRIterationExpression;
import at.dms.kjc.sir.SIRPeekExpression;
import at.dms.kjc.sir.SIRPopExpression;
import at.dms.kjc.sir.SIRPortal;
import at.dms.kjc.sir.SIRPushExpression;
import at.dms.kjc.sir.SIRRangeExpression;
/**
 * A Expression visitor, walking the structure and allowing overridable pre- 
 * or post-visits to the nodes.
 * Used in @{link ThreeAddressCode} and its subclasses to mark code to be 
 * expanded into three-address form. 
 *
 * @author Allyn Dimock
 *
 */
public class ThreeAddressExpressionCheck extends ExpressionVisitorBase<Object,Stack<JExpression>> {
//    /** Set of expressions marked for expansion as three-address code. 
//     * Must be final to be accessible to inner classes.
//     * 
//     */
//    protected final Set<JExpression> marked;
//    
//    
//    /** Constructor: create empty set of marked expressions. */
//    protected ThreeAddressExpressionCheck() {
//        marked = new HashSet<JExpression>();
//    }

    /**
     * Check before walking deeper into expression (prefix check).
     * Override one of preCheck, @{link {@link #postCheck(Stack, JExpression) postCheck}.
     * <p>In you overridden method, you will probably want to update a Set<JExpression> 
     * defined in some surrounding method, to keep track of interesting expressions.</p>
     * @param context  A stack of JExpressions from innermost to outermost
     * @param self  The JExpression being examined.
     * @return probably null may act by side-effecting marked set.
     */
    protected Object preCheck(Stack<JExpression> context, JExpression self) {
        return null;
    }
    
    /**
     * Check on way back out (postfix check).
     * If using this rather than @{link {@link #preCheck(Stack, JExpression) preCheck}
     * then you will need to keep a data structure for results of checking subexpresions.
     * If you don't need results of checking subexpressions then overriding postCheck
     * should be equivalent to overriding preCheck.
     * @param context  A stack of JExpressions from innermost to outermost
     * @param self  The JExpression being examined.
     * @return probably null may act by side-effecting marked set.
     */
    protected Object postCheck(Stack<JExpression> context, JExpression self) {
        return null;
    }
    
    @Override
    public Object visitAdd(JAddExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitArrayAccess(JArrayAccessExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getPrefix().accept(this,context);
        self.getAccessor().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitArrayInitializer(JArrayInitializer self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        // don't descend in array initilaizer //self.getElems()...
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitArrayLength(JArrayLengthExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getPrefix().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitAssignment(JAssignmentExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitBinary(JBinaryExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitBinaryArithmetic(JBinaryArithmeticExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitBitwise(JBitwiseExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitBitwiseComplement(JBitwiseComplementExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getExpr().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitBooleanLiteral(JBooleanLiteral self,
            Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitByteLiteral(JByteLiteral self, Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitCast(JCastExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getExpr().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitCharLiteral(JCharLiteral self, Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitChecked(JCheckedExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        assert false: "Unexpected expression Checked";
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitClass(JClassExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getPrefix().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitCompoundAssignment(JCompoundAssignmentExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitConditional(JConditionalExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getCond().accept(this,context);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitConditionalAnd(JConditionalAndExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitConditionalOr(JConditionalOrExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitConstructorCall(JConstructorCall self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        //
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitCreatePortal(SIRCreatePortal self,
            Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitDivide(JDivideExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitDoubleLiteral(JDoubleLiteral self,
            Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitDynamicToken(SIRDynamicToken self,
            Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitEquality(JEqualityExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
         context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitFieldAccess(JFieldAccessExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getPrefix().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitFloatLiteral(JFloatLiteral self,
            Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitInstanceof(JInstanceofExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        assert false: "Unexpected expression Instanceof";
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitIntLiteral(JIntLiteral self, Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitInterfaceTable(SIRInterfaceTable self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        assert false: "Unexpected expression InterfaceTable";
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitLiteral(JLiteral self, Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
         postCheck(context, self);
        return null;
    }

    @Override
    public Object visitLocalVariable(JLocalVariableExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
         postCheck(context, self);
        return null;
    }

    @Override
    public Object visitLogicalComplement(JLogicalComplementExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getExpr().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitLongLiteral(JLongLiteral self, Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitMethodCall(JMethodCallExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        for (JExpression arg : self.getArgs()) {
            arg.accept(this,context);
        }
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitMinus(JMinusExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitModulo(JModuloExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitMult(JMultExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitName(JNameExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitNewArray(JNewArrayExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        for (JExpression dim : self.getDims()) {
            dim.accept(this,context);
        }
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitNullLiteral(JNullLiteral self, Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitParenthesed(JParenthesedExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getExpr().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitIter(SIRIterationExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        postCheck(context, self);
    	return null;
    }

    @Override
    public Object visitPeek(SIRPeekExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getArg().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitPop(SIRPopExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitPortal(SIRPortal self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        // ?
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitPostfix(JPostfixExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getExpr().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitPrefix(JPrefixExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getExpr().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitPush(SIRPushExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getArg().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitQualifiedAnonymousCreation(
            JQualifiedAnonymousCreation self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        assert false: "Unexpected expression QualifiedAnonymousCreation";        
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitQualifiedInstanceCreation(
            JQualifiedInstanceCreation self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        assert false: "Unexpected expression QualifiedInstanceCreation";
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitRange(SIRRangeExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getMin().accept(this,context);
        self.getAve().accept(this,context);
        self.getMax().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitRelational(JRelationalExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitShift(JShiftExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getLeft().accept(this,context);
        self.getRight().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitShortLiteral(JShortLiteral self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitStringLiteral(JStringLiteral self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitSuper(JSuperExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        assert false: "Unexpected expression Super";
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitThis(JThisExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
         context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitTypeName(JTypeNameExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
         context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitUnary(JUnaryExpression self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getExpr().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitUnaryMinus(JUnaryMinusExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getExpr().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitUnaryPlus(JUnaryPlusExpression self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getExpr().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitUnaryPromote(JUnaryPromote self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        self.getExpr().accept(this,context);
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitUnqualifiedAnonymousCreation(
            JUnqualifiedAnonymousCreation self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        assert false: "Unexpected expression UnqualifiedAnonymousCreation";        
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitUnqualifiedInstanceCreation(
            JUnqualifiedInstanceCreation self, Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        //context.push(self);
        // leaf of expression tree (constructor: gets ignored in StreamIt)
        //context.pop();
        context.pop();
        postCheck(context, self);
        return null;
    }

    @Override
    public Object visitVectorLiteral(JVectorLiteral self,
            Stack<JExpression> context) {
        preCheck(context, self);
        context.push(self);
        //context.push(self);
        // leaf of expression tree
        //context.pop();
        context.pop();
        postCheck(context, self);
        return null;
    }
}
