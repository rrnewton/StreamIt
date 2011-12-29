package at.dms.kjc.spacedynamic;

import java.util.Hashtable;
import java.util.LinkedList;

import at.dms.kjc.JArrayAccessExpression;
import at.dms.kjc.JAssignmentExpression;
import at.dms.kjc.JBlock;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JFormalParameter;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JLocalVariableExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JNewArrayExpression;

public class ArrayCopy {
    //Only handles rectangle multi dim arrays now
    public static void acceptInit(JMethodDeclaration init,Hashtable constants) {
        JBlock body=init.getBody();
        JFormalParameter[] params=init.getParameters();
        for(int i=params.length-1;i>=0;i--) {
            LinkedList<JIntLiteral> dims=new LinkedList<JIntLiteral>();
            Object val=constants.get(params[i]);
            Object temp=val;
            while(temp instanceof Object[]) {
                dims.add(new JIntLiteral(((Object[])temp).length));
                temp=((Object[])temp)[0];
            }
            if(dims.size()>0) {
                dumpAssign(val,body,new JLocalVariableExpression(null,params[i]));
                body.addStatementFirst(new JExpressionStatement
                                       (null,
                                        new JAssignmentExpression(null,
                                                                  new JLocalVariableExpression
                                                                  (null,params[i]), 
                                                                  new JNewArrayExpression(null,
                                                                                          params[i].getType(),
                                                                                          dims.toArray(new JExpression[0]),null)),null));
            }
        }
    }
    
    private static void dumpAssign(Object array,JBlock body,JExpression prefix) {
        if(array instanceof JExpression) {
            if(((JExpression)array).isConstant())
                body.addStatementFirst(new JExpressionStatement(null,new JAssignmentExpression(null,prefix,(JExpression)array),null));
        } else if(array instanceof Object[]) {
            for(int i=((Object[])array).length-1;i>=0;i--)
                dumpAssign(((Object[])array)[i],body,new JArrayAccessExpression(null,prefix,new JIntLiteral(i)));
        } else {
            System.err.println("WARNING: Non Array input to dumpAssign"+array);
        }
    }
}
