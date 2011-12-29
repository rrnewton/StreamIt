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
 * $Id: KjcEmptyVisitor.java,v 1.10 2007-02-01 21:11:31 dimock Exp $
 */

package at.dms.kjc;

import java.util.ListIterator;

import at.dms.compiler.JavaStyleComment;
import at.dms.compiler.JavadocComment;

/**
 * This is a visitor that just recurses into children at every node.
 * It can be extended to add some functionality at a given node.
 *
 * Suggested from: Max R. Andersen(max@cs.auc.dk) */
public class KjcEmptyVisitor implements Constants, KjcVisitor {

    // ----------------------------------------------------------------------
    // TYPE DECLARATION
    // ----------------------------------------------------------------------

    /**
     * prints a compilation unit
     */
    @Override
	public void visitCompilationUnit(JCompilationUnit self,
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
    }

    // ----------------------------------------------------------------------
    // TYPE DECLARATION
    // ----------------------------------------------------------------------

    /**
     * prints a class declaration
     */
    @Override
	public void visitClassDeclaration(JClassDeclaration self,
                                      int modifiers,
                                      String ident,
                                      String superName,
                                      CClassType[] interfaces,
                                      JPhylum[] body,
                                      JFieldDeclaration[] fields,
                                      JMethodDeclaration[] methods,
                                      JTypeDeclaration[] decls) {
        visitClassBody(decls, fields, methods, body);
    }

    /**
     *
     */
    @Override
	public void visitClassBody(JTypeDeclaration[] decls,
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
    }

    /**
     * prints a class declaration
     */
    @Override
	public void visitInnerClassDeclaration(JClassDeclaration self,
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
    }

    /**
     * prints an interface declaration
     */
    @Override
	public void visitInterfaceDeclaration(JInterfaceDeclaration self,
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
    }

    // ----------------------------------------------------------------------
    // METHODS AND FIELDS
    // ----------------------------------------------------------------------

    /**
     * prints a field declaration
     */
    @Override
	public void visitFieldDeclaration(JFieldDeclaration self,
                                      int modifiers,
                                      CType type,
                                      String ident,
                                      JExpression expr) {
        if (expr != null) {
            expr.accept(this);
        }
        // also descend into the vardef
        self.getVariable().accept(this);
    }

    /**
     * prints a method declaration
     */
    @Override
	public void visitMethodDeclaration(JMethodDeclaration self,
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
    }

    /**
     * prints a method declaration
     */
    @Override
	public void visitConstructorDeclaration(JConstructorDeclaration self,
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
    }

    // ----------------------------------------------------------------------
    // STATEMENT
    // ----------------------------------------------------------------------

    /**
     * prints a while statement
     */
    @Override
	public void visitWhileStatement(JWhileStatement self,
                                    JExpression cond,
                                    JStatement body) {
        cond.accept(this);
        body.accept(this);
    }

    /**
     * prints a variable declaration statement
     */
    @Override
	public void visitVariableDeclarationStatement(JVariableDeclarationStatement self,
                                                  JVariableDefinition[] vars) {
        for (int i = 0; i < vars.length; i++) {
            vars[i].accept(this);
        }
    }

    /**
     * prints a variable declaration statement
     */
    @Override
	public void visitVariableDefinition(JVariableDefinition self,
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
    }

    /**
     * prints a try-catch statement
     */
    @Override
	public void visitTryCatchStatement(JTryCatchStatement self,
                                       JBlock tryClause,
                                       JCatchClause[] catchClauses) {
        tryClause.accept(this);
        for (int i = 0; i < catchClauses.length; i++) {
            catchClauses[i].accept(this);
        }
    }

    /**
     * prints a try-finally statement
     */
    @Override
	public void visitTryFinallyStatement(JTryFinallyStatement self,
                                         JBlock tryClause,
                                         JBlock finallyClause) {
        tryClause.accept(this);
        if (finallyClause != null) {
            finallyClause.accept(this);
        }
    }

    /**
     * prints a throw statement
     */
    @Override
	public void visitThrowStatement(JThrowStatement self,
                                    JExpression expr) {
        expr.accept(this);
    }

    /**
     * prints a synchronized statement
     */
    @Override
	public void visitSynchronizedStatement(JSynchronizedStatement self,
                                           JExpression cond,
                                           JStatement body) {
        cond.accept(this);
        body.accept(this);
    }

    /**
     * prints a switch statement
     */
    @Override
	public void visitSwitchStatement(JSwitchStatement self,
                                     JExpression expr,
                                     JSwitchGroup[] body) {
        expr.accept(this);
        for (int i = 0; i < body.length; i++) {
            body[i].accept(this);
        }
    }

    /**
     * prints a return statement
     */
    @Override
	public void visitReturnStatement(JReturnStatement self,
                                     JExpression expr) {
        if (expr != null) {
            expr.accept(this);
        }
    }

    /**
     * prints a labeled statement
     */
    @Override
	public void visitLabeledStatement(JLabeledStatement self,
                                      String label,
                                      JStatement stmt) {
        stmt.accept(this);
    }

    /**
     * prints a if statement
     */
    @Override
	public void visitIfStatement(JIfStatement self,
                                 JExpression cond,
                                 JStatement thenClause,
                                 JStatement elseClause) {
        cond.accept(this);
        thenClause.accept(this);
        if (elseClause != null) {
            elseClause.accept(this);
        }
    }

    /**
     * prints a for statement
     */
    @Override
	public void visitForStatement(JForStatement self,
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
    }

    /**
     * prints a compound statement
     */
    @Override
	public void visitCompoundStatement(JCompoundStatement self,
                                       JStatement[] body) {
        visitCompoundStatement(body);
    }

    /**
     * prints a compound statement
     */
    public void visitCompoundStatement(JStatement[] body) {
        for (int i = 0; i < body.length; i++) {
            body[i].accept(this);
        }
    }

    /**
     * prints an expression statement
     */
    @Override
	public void visitExpressionStatement(JExpressionStatement self,
                                         JExpression expr) {
        expr.accept(this);
    }

    /**
     * prints an expression list statement
     */
    @Override
	public void visitExpressionListStatement(JExpressionListStatement self,
                                             JExpression[] expr) {
        for (int i = 0; i < expr.length; i++) {
            expr[i].accept(this);
        }
    }

    /**
     * prints a empty statement
     */
    @Override
	public void visitEmptyStatement(JEmptyStatement self) {
    }

    /**
     * prints a do statement
     */
    @Override
	public void visitDoStatement(JDoStatement self,
                                 JExpression cond,
                                 JStatement body) {
        body.accept(this);
        cond.accept(this);
    }

    /**
     * prints a continue statement
     */
    @Override
	public void visitContinueStatement(JContinueStatement self,
                                       String label) {
    }

    /**
     * prints a break statement
     */
    @Override
	public void visitBreakStatement(JBreakStatement self,
                                    String label) {
    }

    /**
     * prints an expression statement
     */
    @Override
	public void visitBlockStatement(JBlock self,
                                    JavaStyleComment[] comments) {
        for (ListIterator it = self.getStatementIterator(); it.hasNext(); ) {
            ((JStatement)it.next()).accept(this);
        }
    }

    /**
     * prints a type declaration statement
     */
    @Override
	public void visitTypeDeclarationStatement(JTypeDeclarationStatement self,
                                              JTypeDeclaration decl) {
        decl.accept(this);
    }

    // ----------------------------------------------------------------------
    // EXPRESSION
    // ----------------------------------------------------------------------

    /**
     * prints an unary plus expression
     */
    @Override
	public void visitUnaryPlusExpression(JUnaryExpression self,
                                         JExpression expr)
    {
        expr.accept(this);
    }

    /**
     * prints an unary minus expression
     */
    @Override
	public void visitUnaryMinusExpression(JUnaryExpression self,
                                          JExpression expr)
    {
        expr.accept(this);
    }

    /**
     * prints a bitwise complement expression
     */
    @Override
	public void visitBitwiseComplementExpression(JUnaryExpression self,
                                                 JExpression expr)
    {
        expr.accept(this);
    }

    /**
     * prints a logical complement expression
     */
    @Override
	public void visitLogicalComplementExpression(JUnaryExpression self,
                                                 JExpression expr)
    {
        expr.accept(this);
    }

    /**
     * prints a type name expression
     */
    @Override
	public void visitTypeNameExpression(JTypeNameExpression self,
                                        CType type) {
    }

    /**
     * prints a this expression
     */
    @Override
	public void visitThisExpression(JThisExpression self,
                                    JExpression prefix) {
        if (prefix != null) {
            prefix.accept(this);
        }
    }

    /**
     * prints a super expression
     */
    @Override
	public void visitSuperExpression(JSuperExpression self) {
    }

    /**
     * prints a shift expression
     */
    @Override
	public void visitShiftExpression(JShiftExpression self,
                                     int oper,
                                     JExpression left,
                                     JExpression right) {
        left.accept(this);
        right.accept(this);
    }

    /**
     * prints a shift expressiona
     */
    @Override
	public void visitRelationalExpression(JRelationalExpression self,
                                          int oper,
                                          JExpression left,
                                          JExpression right) {
        left.accept(this);
        right.accept(this);
    }

    /**
     * prints a prefix expression
     */
    @Override
	public void visitPrefixExpression(JPrefixExpression self,
                                      int oper,
                                      JExpression expr) {
        expr.accept(this);
    }

    /**
     * prints a postfix expression
     */
    @Override
	public void visitPostfixExpression(JPostfixExpression self,
                                       int oper,
                                       JExpression expr) {
        expr.accept(this);
    }

    /**
     * prints a parenthesed expression
     */
    @Override
	public void visitParenthesedExpression(JParenthesedExpression self,
                                           JExpression expr) {
        expr.accept(this);
    }

    /**
     * Prints an unqualified anonymous class instance creation expression.
     */
    @Override
	public void visitQualifiedAnonymousCreation(JQualifiedAnonymousCreation self,
                                                JExpression prefix,
                                                String ident,
                                                JExpression[] params,
                                                JClassDeclaration decl)
    {
        prefix.accept(this);
        visitArgs(params);
    }

    /**
     * Prints an unqualified instance creation expression.
     */
    @Override
	public void visitQualifiedInstanceCreation(JQualifiedInstanceCreation self,
                                               JExpression prefix,
                                               String ident,
                                               JExpression[] params)
    {
        prefix.accept(this);
        visitArgs(params);
    }

    /**
     * Prints an unqualified anonymous class instance creation expression.
     */
    @Override
	public void visitUnqualifiedAnonymousCreation(JUnqualifiedAnonymousCreation self,
                                                  CClassType type,
                                                  JExpression[] params,
                                                  JClassDeclaration decl)
    {
        visitArgs(params);
    }

    /**
     * Prints an unqualified instance creation expression.
     */
    @Override
	public void visitUnqualifiedInstanceCreation(JUnqualifiedInstanceCreation self,
                                                 CClassType type,
                                                 JExpression[] params)
    {
        visitArgs(params);
    }

    /**
     * prints an array allocator expression
     */
    @Override
	public void visitNewArrayExpression(JNewArrayExpression self,
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
    }

    /**
     * prints a name expression
     */
    @Override
	public void visitNameExpression(JNameExpression self,
                                    JExpression prefix,
                                    String ident) {
        if (prefix != null) {
            prefix.accept(this);
        }
    }

    /**
     * prints an array allocator expression
     */
    @Override
	public void visitBinaryExpression(JBinaryExpression self,
                                      String oper,
                                      JExpression left,
                                      JExpression right) {
        left.accept(this);
        right.accept(this);
    }

    /**
     * prints a method call expression
     */
    @Override
	public void visitMethodCallExpression(JMethodCallExpression self,
                                          JExpression prefix,
                                          String ident,
                                          JExpression[] args) {
        if (prefix != null) {
            prefix.accept(this);
        }
        visitArgs(args);
    }

    /**
     * prints a local variable expression
     */
    @Override
	public void visitLocalVariableExpression(JLocalVariableExpression self,
                                             String ident) {
    }

    /**
     * prints an instanceof expression
     */
    @Override
	public void visitInstanceofExpression(JInstanceofExpression self,
                                          JExpression expr,
                                          CType dest) {
        expr.accept(this);
    }

    /**
     * prints an equality expression
     */
    @Override
	public void visitEqualityExpression(JEqualityExpression self,
                                        boolean equal,
                                        JExpression left,
                                        JExpression right) {
        left.accept(this);
        right.accept(this);
    }

    /**
     * prints a conditional expression
     */
    @Override
	public void visitConditionalExpression(JConditionalExpression self,
                                           JExpression cond,
                                           JExpression left,
                                           JExpression right) {
        cond.accept(this);
        left.accept(this);
        right.accept(this);
    }

    /**
     * prints a compound expression
     */
    @Override
	public void visitCompoundAssignmentExpression(JCompoundAssignmentExpression self,
                                                  int oper,
                                                  JExpression left,
                                                  JExpression right) {
        left.accept(this);
        right.accept(this);
    }

    /**
     * prints a field expression
     */
    @Override
	public void visitFieldExpression(JFieldAccessExpression self,
                                     JExpression left,
                                     String ident)
    {
        left.accept(this);
    }

    /**
     * prints a class expression
     */
    @Override
	public void visitClassExpression(JClassExpression self, CType type) {
    }

    /**
     * prints a cast expression
     */
    @Override
	public void visitCastExpression(JCastExpression self,
                                    JExpression expr,
                                    CType type)
    {
        expr.accept(this);
    }

    /**
     * prints a cast expression
     */
    @Override
	public void visitUnaryPromoteExpression(JUnaryPromote self,
                                            JExpression expr,
                                            CType type)
    {
        expr.accept(this);
    }

    /**
     * prints a compound assignment expression
     */
    @Override
	public void visitBitwiseExpression(JBitwiseExpression self,
                                       int oper,
                                       JExpression left,
                                       JExpression right) {
        left.accept(this);
        right.accept(this);
    }

    /**
     * prints an assignment expression
     */
    @Override
	public void visitAssignmentExpression(JAssignmentExpression self,
                                          JExpression left,
                                          JExpression right) {
        left.accept(this);
        right.accept(this);
    }

    /**
     * prints an array length expression
     */
    @Override
	public void visitArrayLengthExpression(JArrayLengthExpression self,
                                           JExpression prefix) {
        prefix.accept(this);
    }

    /**
     * prints an array length expression
     */
    @Override
	public void visitArrayAccessExpression(JArrayAccessExpression self,
                                           JExpression prefix,
                                           JExpression accessor) {
        prefix.accept(this);
        accessor.accept(this);
    }

    /** visiting emitted text with possible embedded expressions. */
    @Override
	public void visitEmittedTextExpression(JEmittedTextExpression self, Object[] parts) {
        for (Object part : parts) {
            if (part instanceof JExpression) {
                ((JExpression)part).accept(this);
            }
        }
    }

    /**
     * prints an array length expression
     */
    @Override
	public void visitComments(JavaStyleComment[] comments) {
    }

    /**
     * prints an array length expression
     */
    @Override
	public void visitComment(JavaStyleComment comment) {
    }

    /**
     * prints an array length expression
     */
    @Override
	public void visitJavadoc(JavadocComment comment) {
    }

    // ----------------------------------------------------------------------
    // UTILS
    // ----------------------------------------------------------------------

    /**
     * prints an array length expression
     */
    @Override
	public void visitSwitchLabel(JSwitchLabel self,
                                 JExpression expr) {
        if (expr != null) {
            expr.accept(this);
        }
    }

    /**
     * prints an array length expression
     */
    @Override
	public void visitSwitchGroup(JSwitchGroup self,
                                 JSwitchLabel[] labels,
                                 JStatement[] stmts) {
        for (int i = 0; i < labels.length; i++) {
            labels[i].accept(this);
        }
        for (int i = 0; i < stmts.length; i++) {
            stmts[i].accept(this);
        }
    }

    /**
     * prints an array length expression
     */
    @Override
	public void visitCatchClause(JCatchClause self,
                                 JFormalParameter exception,
                                 JBlock body) {
        exception.accept(this);
        body.accept(this);
    }

    /**
     * prints a boolean literal
     */
    @Override
	public void visitBooleanLiteral(boolean value) {
    }

    /**
     * prints a byte literal
     */
    @Override
	public void visitByteLiteral(byte value) {
    }

    /**
     * prints a character literal
     */
    @Override
	public void visitCharLiteral(char value) {
    }

    /**
     * prints a double literal
     */
    @Override
	public void visitDoubleLiteral(double value) {
    }

    /**
     * prints a float literal
     */
    @Override
	public void visitFloatLiteral(float value) {
    }

    /**
     * prints a int literal
     */
    @Override
	public void visitIntLiteral(int value) {
    }

    /**
     * prints a long literal
     */
    @Override
	public void visitLongLiteral(long value) {
    }

    /**
     * prints a short literal
     */
    @Override
	public void visitShortLiteral(short value) {
    }

    /**
     * prints a string literal
     */
    @Override
	public void visitStringLiteral(String value) {
    }

    /**
     * prints a null literal
     */
    @Override
	public void visitNullLiteral() {
    }

    /**
     * prints an array length expression
     */
    @Override
	public void visitPackageName(String name) {
    }

    /**
     * prints an array length expression
     */
    @Override
	public void visitPackageImport(String name) {
    }

    /**
     * prints an array length expression
     */
    @Override
	public void visitClassImport(String name) {
    }

    /**
     * prints an array length expression
     */
    @Override
	public void visitFormalParameters(JFormalParameter self,
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
    }

    /**
     * prints an array length expression
     */
    public void visitArgs(JExpression[] args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                args[i].accept(this);
            }
        }
    }

    /**
     * prints an array length expression
     */
    @Override
	public void visitConstructorCall(JConstructorCall self,
                                     boolean functorIsThis,
                                     JExpression[] params)
    {
        visitArgs(params);
    }

    /**
     * prints an array initializer expression
     */
    @Override
	public void visitArrayInitializer(JArrayInitializer self,
                                      JExpression[] elems)
    {
        for (int i = 0; i < elems.length; i++) {
            elems[i].accept(this);
        }
    }
}
