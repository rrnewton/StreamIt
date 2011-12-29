package at.dms.kjc.cell;

import at.dms.classfile.Constants;
import at.dms.kjc.CClassType;
import at.dms.kjc.CStdType;
import at.dms.kjc.JBlock;
import at.dms.kjc.JExpression;
import at.dms.kjc.JFormalParameter;
import at.dms.kjc.JLocalVariableExpression;
import at.dms.kjc.JMethodCallExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JReturnStatement;
import at.dms.kjc.backendSupport.IntraSSGChannel;
import at.dms.kjc.slir.IntraSSGEdge;

public class InterSPUChannel extends IntraSSGChannel {

    public InterSPUChannel(IntraSSGEdge e) {
        super(e);
    }
    
    @Override
    public String popManyMethodName() {
        return "popn";
    }
    
    @Override
    public JMethodDeclaration popManyMethod() {
        return null;
    }
    
    @Override
    public String popMethodName() {
        return "pop";
    }
    @Override
    public JMethodDeclaration popMethod() {
        JBlock body = new JBlock();
        JMethodDeclaration retval = new JMethodDeclaration(
                null,
                /*at.dms.kjc.Constants.ACC_PUBLIC | at.dms.kjc.Constants.ACC_STATIC |*/ Constants.ACC_INLINE,
                edge.getType(),
                popMethodName(),
                new JFormalParameter[0],
                CClassType.EMPTY,
                body, null, null);
        body.addStatement(
                new JReturnStatement(null,
                                     new JMethodCallExpression(popMethodName(),
                                                               new JExpression[0]),
                                     null));
        return retval;
    }
    
    @Override
    public String pushMethodName() {
        return "push";
    }
    @Override
    public JMethodDeclaration pushMethod() {
        String valName = "__val";
        JFormalParameter val = new JFormalParameter(
                edge.getType(),
                valName);
        JLocalVariableExpression valRef = new JLocalVariableExpression(val);
        JBlock body = new JBlock();
        JMethodDeclaration retval = new JMethodDeclaration(
                null,
                Constants.ACC_INLINE,
                CStdType.Void,
                pushMethodName(),
                new JFormalParameter[]{val},
                CClassType.EMPTY,
                body, null, null);
        body.addStatement(
                new JReturnStatement(null,
                                     new JMethodCallExpression(pushMethodName(),
                                                               new JExpression[]{valRef}),
                                     null));
        return retval;
    }
    
    @Override
    public String peekMethodName() {
        return "peek";
    }
    @Override
    public JMethodDeclaration peekMethod() {
        String parameterName = "__offset";
        JFormalParameter offset = new JFormalParameter(
                CStdType.Integer,
                parameterName);
        JLocalVariableExpression offsetRef = new JLocalVariableExpression(offset);
        JBlock body = new JBlock();
        JMethodDeclaration retval = new JMethodDeclaration(
                null,
                Constants.ACC_INLINE,
                edge.getType(),
                pushMethodName(),
                new JFormalParameter[]{offset},
                CClassType.EMPTY,
                body, null, null);
        body.addStatement(
                new JReturnStatement(null,
                                     new JMethodCallExpression(peekMethodName(),
                                                               new JExpression[]{offsetRef}),
                                     null));
        return retval;
    }
}
