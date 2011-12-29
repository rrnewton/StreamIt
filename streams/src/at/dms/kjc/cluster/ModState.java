package at.dms.kjc.cluster;

import at.dms.kjc.JArrayAccessExpression;
import at.dms.kjc.JAssignmentExpression;
import at.dms.kjc.JCompoundAssignmentExpression;
import at.dms.kjc.JExpression;
import at.dms.kjc.JFieldAccessExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JPostfixExpression;
import at.dms.kjc.JPrefixExpression;
import at.dms.kjc.SLIREmptyVisitor;

/**
 * Determines if a method modifies the state of a stream node!
 */

public class ModState extends SLIREmptyVisitor {

    boolean mod;

    ModState() {
        mod = false;
    }

    public static boolean methodModsState(JMethodDeclaration self) {
        ModState m = new ModState();

        if (ClusterBackend.debugging)
            System.out.println("=========== ModState: "+self.getName()+" ===========");
        m.visitBlockStatement(self.getBody(), null);
        if (ClusterBackend.debugging)
            System.out.println("============================================");

        return m.mod;
    }

    

    public void visitPrefixExpression(JPrefixExpression self,
                                      int oper,
                                      JExpression expr) {

        if (expr instanceof JFieldAccessExpression) {
            JFieldAccessExpression f_expr = (JFieldAccessExpression)expr;
            if (ClusterBackend.debugging)
                System.out.println("ModState: field "+f_expr.getIdent()+" changed by a prefix expression");
            mod = true;
        }
    }

    public void visitPostfixExpression(JPostfixExpression self,
                                       int oper,
                                       JExpression expr) {

        if (expr instanceof JFieldAccessExpression) {
            JFieldAccessExpression f_expr = (JFieldAccessExpression)expr;
            //System.out.println("ModState: field "+f_expr.getIdent()+" changed by a postfix expression");
            mod = true;     
        }
    }


    public void visitAssignmentExpression(JAssignmentExpression self,
                                          JExpression left,
                                          JExpression right) {

        if (left instanceof JFieldAccessExpression) {
            JFieldAccessExpression f_expr = (JFieldAccessExpression)left;
            if (ClusterBackend.debugging)
                System.out.println("ModState: field "+f_expr.getIdent()+" changed by an assignement expression");
            mod = true;
        }

        if (left instanceof JArrayAccessExpression) {
            JArrayAccessExpression a_expr = (JArrayAccessExpression)left;

            if (a_expr.getPrefix() instanceof JFieldAccessExpression) {
                JFieldAccessExpression f_expr = (JFieldAccessExpression)a_expr.getPrefix();
                if (ClusterBackend.debugging)
                    System.out.println("ModState: field "+f_expr.getIdent()+" changed by an assignement expression");
                mod = true;
            }
        }
    }


    public void visitCompoundAssignmentExpression(JCompoundAssignmentExpression self,
                                                  int oper,
                                                  JExpression left,
                                                  JExpression right) {

        if (left instanceof JFieldAccessExpression) {
            JFieldAccessExpression f_expr = (JFieldAccessExpression)left;
            if (ClusterBackend.debugging)
                System.out.println("ModState: field "+f_expr.getIdent()+" changed by an assignement expression");
            mod = true;
        }

        if (left instanceof JArrayAccessExpression) {
            JArrayAccessExpression a_expr = (JArrayAccessExpression)left;

            if (a_expr.getPrefix() instanceof JFieldAccessExpression) {
                JFieldAccessExpression f_expr = (JFieldAccessExpression)a_expr.getPrefix();
                if (ClusterBackend.debugging)
                    System.out.println("ModState: field "+f_expr.getIdent()+" changed by an assignement expression");
                mod = true;
            }
        }
    }

}
