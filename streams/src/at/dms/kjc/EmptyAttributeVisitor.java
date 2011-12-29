/*
 * Copyright (C) 1990-2001 DMS Decision Management Systems Ges.m.b.H.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id: EmptyAttributeVisitor.java,v 1.14 2007-11-26 20:00:35 rabbah Exp $
 */

package at.dms.kjc;

import java.util.ListIterator;

import at.dms.compiler.JavaStyleComment;
import at.dms.compiler.JavadocComment;

/**
 * This is a visitor that just recurses into children at every node
 * and returns that node.  It can be extended to do some mutation at a
 * given node.
 *
 * Suggested from: Max R. Andersen(max@cs.auc.dk) */
public class EmptyAttributeVisitor implements Constants, AttributeVisitor<Object> {

    protected boolean forwards=true; //Determines whether to walk forwards or backwards through blocks

    // ----------------------------------------------------------------------
    // TYPE DECLARATION
    // ----------------------------------------------------------------------

    /**
     * prints a compilation unit
     */
    @Override
	public Object visitCompilationUnit(JCompilationUnit self,
                                       JPackageName packageName,
                                       JPackageImport[] importedPackages,
                                       JClassImport[] importedClasses,
                                       JTypeDeclaration[] typeDeclarations) {
        if (packageName.getName().length() > 0) {
            packageName.accept(this);
        }

        for (int i = 0; i < importedPackages.length ; i++) {
            if (!importedPackages[i].getName().equals("java/lang")) {
                importedPackages[i].accept(this);
            }
        }

        for (int i = 0; i < importedClasses.length ; i++) {
            importedClasses[i].accept(this);
        }

        for (int i = 0; i < typeDeclarations.length ; i++) {
            typeDeclarations[i].accept(this);
        }
        return self;
    }

    // ----------------------------------------------------------------------
    // TYPE DECLARATION
    // ----------------------------------------------------------------------

    /**
     * prints a class declaration
     */
    @Override
	public Object visitClassDeclaration(JClassDeclaration self,
                                        int modifiers,
                                        String ident,
                                        String superName,
                                        CClassType[] interfaces,
                                        JPhylum[] body,
                                        JFieldDeclaration[] fields,
                                        JMethodDeclaration[] methods,
                                        JTypeDeclaration[] decls) {
        visitClassBody(decls, fields, methods, body);
        return self;
    }

    /**
     *
     */
    @Override
	public Object visitClassBody(JTypeDeclaration[] decls,
                                 JFieldDeclaration[] fields,
                                 JMethodDeclaration[] methods,
                                 JPhylum[] body) {
        for (int i = 0; i < decls.length ; i++) {
            decls[i].accept(this);
        }
        for (int i = 0; i < fields.length ; i++) {
            fields[i].accept(this);
        }
        for (int i = 0; i < methods.length ; i++) {
            methods[i].accept(this);
        }
        for (int i = 0; i < body.length ; i++) {
            if (!(body[i] instanceof JFieldDeclaration)) {
                body[i].accept(this);
            }
        }
        for (int i = 0; i < body.length ; i++) {
            if (body[i] instanceof JFieldDeclaration) {
                body[i].accept(this);
            }
        }
        return null;
    }

    /**
     * prints a class declaration
     */
    @Override
	public Object visitInnerClassDeclaration(JClassDeclaration self,
                                             int modifiers,
                                             String ident,
                                             String superName,
                                             CClassType[] interfaces,
                                             JTypeDeclaration[] decls,
                                             JPhylum[] body,
                                             JFieldDeclaration[] fields,
                                             JMethodDeclaration[] methods) {
        for (int i = 0; i < decls.length ; i++) {
            decls[i].accept(this);
        }
        for (int i = 0; i < fields.length ; i++) {
            fields[i].accept(this);
        }
        for (int i = 0; i < methods.length ; i++) {
            methods[i].accept(this);
        }
        for (int i = 0; i < body.length ; i++) {
            body[i].accept(this);
        }
        return self;
    }

    /**
     * prints an interface declaration
     */
    @Override
	public Object visitInterfaceDeclaration(JInterfaceDeclaration self,
                                            int modifiers,
                                            String ident,
                                            CClassType[] interfaces,
                                            JPhylum[] body,
                                            JMethodDeclaration[] methods) {
        for (int i = 0; i < body.length; i++) {
            body[i].accept(this);
        }
        for (int i = 0; i < methods.length; i++) {
            methods[i].accept(this);
        }
        return self;
    }

    // ----------------------------------------------------------------------
    // METHODS AND FIELDS
    // ----------------------------------------------------------------------

    /**
     * prints a field declaration
     */
    @Override
	public Object visitFieldDeclaration(JFieldDeclaration self,
                                        int modifiers,
                                        CType type,
                                        String ident,
                                        JExpression expr) {
        if (expr != null) {
            expr.accept(this);
        }
        // visit static array dimensions
        if (type.isArrayType()) {
            JExpression[] dims = ((CArrayType)type).getDims();
            for (int i=0; i<dims.length; i++) {
                dims[i].accept(this);
            }
        }
        return self;
    }

    /**
     * prints a method declaration
     */
    @Override
	public Object visitMethodDeclaration(JMethodDeclaration self,
                                         int modifiers,
                                         CType returnType,
                                         String ident,
                                         JFormalParameter[] parameters,
                                         CClassType[] exceptions,
                                         JBlock body) {
        for (int i = 0; i < parameters.length; i++) {
            if (!parameters[i].isGenerated()) {
                parameters[i].accept(this);
            }
        }
        if (body != null) {
            body.accept(this);
        }
        return self;
    }

    /**
     * prints a method declaration
     */
    @Override
	public Object visitConstructorDeclaration(JConstructorDeclaration self,
                                              int modifiers,
                                              String ident,
                                              JFormalParameter[] parameters,
                                              CClassType[] exceptions,
                                              JConstructorBlock body)
    {
        for (int i = 0; i < parameters.length; i++) {
            if (!parameters[i].isGenerated()) {
                parameters[i].accept(this);
            }
        }
        body.accept(this);
        return self;
    }

    // ----------------------------------------------------------------------
    // STATEMENT
    // ----------------------------------------------------------------------

    /**
     * prints a while statement
     */
    @Override
	public Object visitWhileStatement(JWhileStatement self,
                                      JExpression cond,
                                      JStatement body) {
        cond.accept(this);
        body.accept(this);
        return self;
    }

    /**
     * prints a variable declaration statement
     */
    @Override
	public Object visitVariableDeclarationStatement(JVariableDeclarationStatement self,
                                                    JVariableDefinition[] vars) {
        for (int i = 0; i < vars.length; i++) {
            vars[i].accept(this);
        }
        return self;
    }

    /**
     * prints a variable declaration statement
     */
    @Override
	public Object visitVariableDefinition(JVariableDefinition self,
                                          int modifiers,
                                          CType type,
                                          String ident,
                                          JExpression expr) {
        if (expr != null) {
            expr.accept(this);
        }
        // visit static array dimensions
        if (type.isArrayType()) {
            JExpression[] dims = ((CArrayType)type).getDims();
            for (int i=0; i<dims.length; i++) {
                dims[i].accept(this);
            }
        }
        return self;
    }

    /**
     * prints a try-catch statement
     */
    @Override
	public Object visitTryCatchStatement(JTryCatchStatement self,
                                         JBlock tryClause,
                                         JCatchClause[] catchClauses) {
        tryClause.accept(this);
        for (int i = 0; i < catchClauses.length; i++) {
            catchClauses[i].accept(this);
        }
        return self;
    }

    /**
     * prints a try-finally statement
     */
    @Override
	public Object visitTryFinallyStatement(JTryFinallyStatement self,
                                           JBlock tryClause,
                                           JBlock finallyClause) {
        tryClause.accept(this);
        if (finallyClause != null) {
            finallyClause.accept(this);
        }
        return self;
    }

    /**
     * prints a throw statement
     */
    @Override
	public Object visitThrowStatement(JThrowStatement self,
                                      JExpression expr) {
        expr.accept(this);
        return self;
    }

    /**
     * prints a synchronized statement
     */
    @Override
	public Object visitSynchronizedStatement(JSynchronizedStatement self,
                                             JExpression cond,
                                             JStatement body) {
        cond.accept(this);
        body.accept(this);
        return self;
    }

    /**
     * prints a switch statement
     */
    @Override
	public Object visitSwitchStatement(JSwitchStatement self,
                                       JExpression expr,
                                       JSwitchGroup[] body) {
        expr.accept(this);
        for (int i = 0; i < body.length; i++) {
            body[i].accept(this);
        }
        return self;
    }

    /**
     * prints a return statement
     */
    @Override
	public Object visitReturnStatement(JReturnStatement self,
                                       JExpression expr) {
        if (expr != null) {
            expr.accept(this);
        }
        return self;
    }

    /**
     * prints a labeled statement
     */
    @Override
	public Object visitLabeledStatement(JLabeledStatement self,
                                        String label,
                                        JStatement stmt) {
        stmt.accept(this);
        return self;
    }

    /**
     * prints a if statement
     */
    @Override
	public Object visitIfStatement(JIfStatement self,
                                   JExpression cond,
                                   JStatement thenClause,
                                   JStatement elseClause) {
        cond.accept(this);
        thenClause.accept(this);
        if (elseClause != null) {
            elseClause.accept(this);
        }
        return self;
    }

    /**
     * prints a for statement
     */
    @Override
	public Object visitForStatement(JForStatement self,
                                    JStatement init,
                                    JExpression cond,
                                    JStatement incr,
                                    JStatement body) {
        if (init != null) {
            init.accept(this);
        }
        if (cond != null) {
            cond.accept(this);
        }
        if (incr != null) {
            incr.accept(this);
        }
        body.accept(this);
        return self;
    }

    /**
     * prints a compound statement
     */
    @Override
	public Object visitCompoundStatement(JCompoundStatement self,
                                         JStatement[] body) {
        for (int i = 0; i < body.length; i++) {
            body[i].accept(this);
        }
        return self;
    }

    /**
     * prints an expression statement
     */
    @Override
	public Object visitExpressionStatement(JExpressionStatement self,
                                           JExpression expr) {
        expr.accept(this);
        return self;
    }

    /**
     * prints an expression list statement
     */
    @Override
	public Object visitExpressionListStatement(JExpressionListStatement self,
                                               JExpression[] expr) {
        for (int i = 0; i < expr.length; i++) {
            expr[i].accept(this);
        }
        return self;
    }

    /**
     * prints a empty statement
     */
    @Override
	public Object visitEmptyStatement(JEmptyStatement self) {
        return self;
    }
    
    /**
     * prints a do statement
     */
    @Override
	public Object visitDoStatement(JDoStatement self,
                                   JExpression cond,
                                   JStatement body) {
        body.accept(this);
        cond.accept(this);
        return self;
    }

    /**
     * prints a continue statement
     */
    @Override
	public Object visitContinueStatement(JContinueStatement self,
                                         String label) {
        return self;
    }

    /**
     * prints a break statement
     */
    @Override
	public Object visitBreakStatement(JBreakStatement self,
                                      String label) {
        return self;
    }

    /**
     * prints an expression statement
     */
    @Override
	public Object visitBlockStatement(JBlock self,
                                      JavaStyleComment[] comments) {
        if(forwards)
            for (ListIterator it = self.getStatementIterator(); it.hasNext(); ) {
                ((JStatement)it.next()).accept(this);
            }
        else {
            JStatement[] body=self.getStatementArray();
            for(int i=body.length-1;i>=0;i--)
                body[i].accept(this);
        }
        return self;
    }

    /**
     * prints a type declaration statement
     */
    @Override
	public Object visitTypeDeclarationStatement(JTypeDeclarationStatement self,
                                                JTypeDeclaration decl) {
        decl.accept(this);
        return self;
    }

    // ----------------------------------------------------------------------
    // EXPRESSION
    // ----------------------------------------------------------------------

    /**
     * prints an unary plus expression
     */
    @Override
	public Object visitUnaryPlusExpression(JUnaryExpression self,
                                           JExpression expr)
    {
        expr.accept(this);
        return self;
    }

    /**
     * prints an unary minus expression
     */
    @Override
	public Object visitUnaryMinusExpression(JUnaryExpression self,
                                            JExpression expr)
    {
        expr.accept(this);
        return self;
    }

    /**
     * prints a bitwise complement expression
     */
    @Override
	public Object visitBitwiseComplementExpression(JUnaryExpression self,
                                                   JExpression expr)
    {
        expr.accept(this);
        return self;
    }

    /**
     * prints a logical complement expression
     */
    @Override
	public Object visitLogicalComplementExpression(JUnaryExpression self,
                                                   JExpression expr)
    {
        expr.accept(this);
        return self;
    }

    /**
     * prints a type name expression
     */
    @Override
	public Object visitTypeNameExpression(JTypeNameExpression self,
                                          CType type) {
        return self;
    }

    /**
     * prints a this expression
     */
    @Override
	public Object visitThisExpression(JThisExpression self,
                                      JExpression prefix) {
        if (prefix != null) {
            prefix.accept(this);
        }
        return self;
    }

    /**
     * prints a super expression
     */
    @Override
	public Object visitSuperExpression(JSuperExpression self) {
        return self;
    }

    /**
     * prints a shift expression
     */
    @Override
	public Object visitShiftExpression(JShiftExpression self,
                                       int oper,
                                       JExpression left,
                                       JExpression right) {
        left.accept(this);
        right.accept(this);
        return self;
    }

    /**
     * prints a shift expressiona
     */
    @Override
	public Object visitRelationalExpression(JRelationalExpression self,
                                            int oper,
                                            JExpression left,
                                            JExpression right) {
        left.accept(this);
        right.accept(this);
        return self;
    }

    /**
     * prints a prefix expression
     */
    @Override
	public Object visitPrefixExpression(JPrefixExpression self,
                                        int oper,
                                        JExpression expr) {
        expr.accept(this);
        return self;
    }

    /**
     * prints a postfix expression
     */
    @Override
	public Object visitPostfixExpression(JPostfixExpression self,
                                         int oper,
                                         JExpression expr) {
        expr.accept(this);
        return self;
    }

    /**
     * prints a parenthesed expression
     */
    @Override
	public Object visitParenthesedExpression(JParenthesedExpression self,
                                             JExpression expr) {
        expr.accept(this);
        return self;
    }

    /**
     * Prints an unqualified anonymous class instance creation expression.
     */
    @Override
	public Object visitQualifiedAnonymousCreation(JQualifiedAnonymousCreation self,
                                                  JExpression prefix,
                                                  String ident,
                                                  JExpression[] params,
                                                  JClassDeclaration decl)
    {
        prefix.accept(this);
        visitArgs(params);
        return self;
    }

    /**
     * Prints an unqualified instance creation expression.
     */
    @Override
	public Object visitQualifiedInstanceCreation(JQualifiedInstanceCreation self,
                                                 JExpression prefix,
                                                 String ident,
                                                 JExpression[] params)
    {
        prefix.accept(this);
        visitArgs(params);
        return self;
    }

    /**
     * Prints an unqualified anonymous class instance creation expression.
     */
    @Override
	public Object visitUnqualifiedAnonymousCreation(JUnqualifiedAnonymousCreation self,
                                                    CClassType type,
                                                    JExpression[] params,
                                                    JClassDeclaration decl)
    {
        visitArgs(params);
        return self;
    }

    /**
     * Prints an unqualified instance creation expression.
     */
    @Override
	public Object visitUnqualifiedInstanceCreation(JUnqualifiedInstanceCreation self,
                                                   CClassType type,
                                                   JExpression[] params)
    {
        visitArgs(params);
        return self;
    }

    /**
     * prints an array allocator expression
     */
    @Override
	public Object visitNewArrayExpression(JNewArrayExpression self,
                                          CType type,
                                          JExpression[] dims,
                                          JArrayInitializer init)
    {
        for (int i = 0; i < dims.length; i++) {
            if (dims[i] != null) {
                dims[i].accept(this);
            }
        }
        if (init != null) {
            init.accept(this);
        }
        return self;
    }

    /**
     * prints a name expression
     */
    @Override
	public Object visitNameExpression(JNameExpression self,
                                      JExpression prefix,
                                      String ident) {
        if (prefix != null) {
            prefix.accept(this);
        }
        return self;
    }

    /**
     * prints an array allocator expression
     */
    @Override
	public Object visitBinaryExpression(JBinaryExpression self,
                                        String oper,
                                        JExpression left,
                                        JExpression right) {
        left.accept(this);
        right.accept(this);
        return self;
    }

    /**
     * prints a method call expression
     */
    @Override
	public Object visitMethodCallExpression(JMethodCallExpression self,
                                            JExpression prefix,
                                            String ident,
                                            JExpression[] args) {
        if (prefix != null) {
            prefix.accept(this);
        }
        visitArgs(args);
        return self;
    }

    /**
     * prints a local variable expression
     */
    @Override
	public Object visitLocalVariableExpression(JLocalVariableExpression self,
                                               String ident) {
        return self;
    }

    /**
     * prints an instanceof expression
     */
    @Override
	public Object visitInstanceofExpression(JInstanceofExpression self,
                                            JExpression expr,
                                            CType dest) {
        expr.accept(this);
        return self;
    }

    /**
     * prints an equality expression
     */
    @Override
	public Object visitEqualityExpression(JEqualityExpression self,
                                          boolean equal,
                                          JExpression left,
                                          JExpression right) {
        left.accept(this);
        right.accept(this);
        return self;
    }

    /**
     * prints a conditional expression
     */
    @Override
	public Object visitConditionalExpression(JConditionalExpression self,
                                             JExpression cond,
                                             JExpression left,
                                             JExpression right) {
        cond.accept(this);
        left.accept(this);
        right.accept(this);
        return self;
    }

    /**
     * prints a compound expression
     */
    @Override
	public Object visitCompoundAssignmentExpression(JCompoundAssignmentExpression self,
                                                    int oper,
                                                    JExpression left,
                                                    JExpression right) {
        left.accept(this);
        right.accept(this);
        return self;
    }

    /**
     * prints a field expression
     */
    @Override
	public Object visitFieldExpression(JFieldAccessExpression self,
                                       JExpression left,
                                       String ident)
    {
        left.accept(this);
        return self;
    }

    /**
     * prints a class expression
     */
    @Override
	public Object visitClassExpression(JClassExpression self, CType type) {
        return self;
    }

    /**
     * prints a cast expression
     */
    @Override
	public Object visitCastExpression(JCastExpression self,
                                      JExpression expr,
                                      CType type)
    {
        expr.accept(this);
        return self;
    }

    /**
     * prints a cast expression
     */
    @Override
	public Object visitUnaryPromoteExpression(JUnaryPromote self,
                                              JExpression expr,
                                              CType type)
    {
        expr.accept(this);
        return self;
    }

    /**
     * prints a compound assignment expression
     */
    @Override
	public Object visitBitwiseExpression(JBitwiseExpression self,
                                         int oper,
                                         JExpression left,
                                         JExpression right) {
        left.accept(this);
        right.accept(this);
        return self;
    }

    /**
     * prints an assignment expression
     */
    @Override
	public Object visitAssignmentExpression(JAssignmentExpression self,
                                            JExpression left,
                                            JExpression right) {
        left.accept(this);
        right.accept(this);
        return self;
    }

    /**
     * prints an array length expression
     */
    @Override
	public Object visitArrayLengthExpression(JArrayLengthExpression self,
                                             JExpression prefix) {
        prefix.accept(this);
        return self;
    }

    /**
     * prints an array length expression
     */
    @Override
	public Object visitArrayAccessExpression(JArrayAccessExpression self,
                                             JExpression prefix,
                                             JExpression accessor) {
        prefix.accept(this);
        accessor.accept(this);
        return self;
    }

    /** visiting emitted text with possible embedded expressions. */
    @Override
	public Object visitEmittedTextExpression(JEmittedTextExpression self, Object[] parts) {
        for (Object part : parts) {
            if (part instanceof JExpression) {
                ((JExpression)part).accept(this);
            }
        }
        return self;
    }
   
    /**
     * prints an array length expression
     */
    @Override
	public Object visitComments(JavaStyleComment[] self) {
        return self;
    }

    /**
     * prints an array length expression
     */
    @Override
	public Object visitComment(JavaStyleComment self) {
        return self;
    }

    /**
     * prints an array length expression
     */
    @Override
	public Object visitJavadoc(JavadocComment self) {
        return self;
    }

    // ----------------------------------------------------------------------
    // UTILS
    // ----------------------------------------------------------------------

    /**
     * visits an switch label
     */
    @Override
	public Object visitSwitchLabel(JSwitchLabel self,
                                   JExpression expr) {
        if (expr != null) {
            expr.accept(this);
        }
        return self;
    }

    /**
     * visits a switch group
     */
    @Override
	public Object visitSwitchGroup(JSwitchGroup self,
                                   JSwitchLabel[] labels,
                                   JStatement[] stmts) {
        for (int i = 0; i < labels.length; i++) {
            labels[i].accept(this);
        }
        for (int i = 0; i < stmts.length; i++) {
            stmts[i].accept(this);
        }
        return self;
    }

    /**
     * visits a catch clause
     */
    @Override
	public Object visitCatchClause(JCatchClause self,
                                   JFormalParameter exception,
                                   JBlock body) {
        exception.accept(this);
        body.accept(this);
        return self;
    }

    /**
     * visits a boolean literal
     */
    @Override
	public Object visitBooleanLiteral(JBooleanLiteral self,
                                      boolean value)
    {
        return self;
    }

    /**
     * visits a byte literal
     */
    @Override
	public Object visitByteLiteral(JByteLiteral self,
                                   byte value)
    {
        return self;
    }

    /**
     * visits a character literal
     */
    @Override
	public Object visitCharLiteral(JCharLiteral self,
                                   char value)
    {
        return self;
    }

    /**
     * visits a double literal
     */
    @Override
	public Object visitDoubleLiteral(JDoubleLiteral self,
                                     double value)
    {
        return self;
    }

    /**
     * visits a float literal
     */
    @Override
	public Object visitFloatLiteral(JFloatLiteral self,
                                    float value)
    {
        return self;
    }

    /**
     * visits a int literal
     */
    @Override
	public Object visitIntLiteral(JIntLiteral self,
                                  int value)
    {
        return self;
    }

    /**
     * visits a long literal
     */
    @Override
	public Object visitLongLiteral(JLongLiteral self,
                                   long value)
    {
        return self;
    }

    /**
     * visits a short literal
     */
    @Override
	public Object visitShortLiteral(JShortLiteral self,
                                    short value)
    {
        return self;
    }

    /**
     * visits a string literal
     */
    @Override
	public Object visitStringLiteral(JStringLiteral self,
                                     String value)
    {
        return self;
    }

    /**
     * visits a null literal
     */
    @Override
	public Object visitNullLiteral(JNullLiteral self)
    {
        return self;
    }

    /**
     * prints an array length expression
     */
    @Override
	public Object visitPackageName(String name) {
        return null;
    }

    /**
     * prints an array length expression
     */
    @Override
	public Object visitPackageImport(String name) {
        return name;
    }

    /**
     * prints an array length expression
     */
    @Override
	public Object visitClassImport(String name) {
        return name;
    }

    /**
     * prints an array length expression
     */
    @Override
	public Object visitFormalParameters(JFormalParameter self,
                                        boolean isFinal,
                                        CType type,
                                        String ident) {

        // visit static array dimensions
        if (type.isArrayType()) {
            JExpression[] dims = ((CArrayType)type).getDims();
            for (int i=0; i<dims.length; i++) {
                dims[i].accept(this);
            }
        }

        return self;
    }

    /**
     * prints an array length expression
     */
    public Object visitArgs(JExpression[] args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                args[i].accept(this);
            }
        }
        return args;
    }

    /**
     * prints an array length expression
     */
    @Override
	public Object visitConstructorCall(JConstructorCall self,
                                       boolean functorIsThis,
                                       JExpression[] params)
    {
        visitArgs(params);
        return self;
    }

    /**
     * prints an array initializer expression
     */
    @Override
	public Object visitArrayInitializer(JArrayInitializer self,
                                        JExpression[] elems)
    {
        visitArgs(elems);
        return self;
    }
}
