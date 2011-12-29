/*
 * IRPrinter.java: Print Kopi IR
 * David Maze &lt;dmaze@cag.lcs.mit.edu&gt;
 */

package at.dms.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.ListIterator;

import at.dms.compiler.JavaStyleComment;
import at.dms.compiler.JavadocComment;
import at.dms.kjc.CClassType;
import at.dms.kjc.CModifier;
import at.dms.kjc.CType;
import at.dms.kjc.JArrayAccessExpression;
import at.dms.kjc.JArrayInitializer;
import at.dms.kjc.JArrayLengthExpression;
import at.dms.kjc.JAssignmentExpression;
import at.dms.kjc.JBinaryExpression;
import at.dms.kjc.JBitwiseExpression;
import at.dms.kjc.JBlock;
import at.dms.kjc.JBreakStatement;
import at.dms.kjc.JCastExpression;
import at.dms.kjc.JCatchClause;
import at.dms.kjc.JClassDeclaration;
import at.dms.kjc.JClassExpression;
import at.dms.kjc.JClassImport;
import at.dms.kjc.JCompilationUnit;
import at.dms.kjc.JCompoundAssignmentExpression;
import at.dms.kjc.JCompoundStatement;
import at.dms.kjc.JConditionalExpression;
import at.dms.kjc.JConstructorBlock;
import at.dms.kjc.JConstructorCall;
import at.dms.kjc.JConstructorDeclaration;
import at.dms.kjc.JContinueStatement;
import at.dms.kjc.JDoStatement;
import at.dms.kjc.JEmittedTextExpression;
import at.dms.kjc.JEmptyStatement;
import at.dms.kjc.JEqualityExpression;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionListStatement;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JFieldAccessExpression;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JForStatement;
import at.dms.kjc.JFormalParameter;
import at.dms.kjc.JIfStatement;
import at.dms.kjc.JInstanceofExpression;
import at.dms.kjc.JInterfaceDeclaration;
import at.dms.kjc.JLabeledStatement;
import at.dms.kjc.JLiteral;
import at.dms.kjc.JLocalVariableExpression;
import at.dms.kjc.JMethodCallExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JNameExpression;
import at.dms.kjc.JNewArrayExpression;
import at.dms.kjc.JPackageImport;
import at.dms.kjc.JPackageName;
import at.dms.kjc.JParenthesedExpression;
import at.dms.kjc.JPhylum;
import at.dms.kjc.JPostfixExpression;
import at.dms.kjc.JPrefixExpression;
import at.dms.kjc.JQualifiedAnonymousCreation;
import at.dms.kjc.JQualifiedInstanceCreation;
import at.dms.kjc.JRelationalExpression;
import at.dms.kjc.JReturnStatement;
import at.dms.kjc.JShiftExpression;
import at.dms.kjc.JStatement;
import at.dms.kjc.JSuperExpression;
import at.dms.kjc.JSwitchGroup;
import at.dms.kjc.JSwitchLabel;
import at.dms.kjc.JSwitchStatement;
import at.dms.kjc.JSynchronizedStatement;
import at.dms.kjc.JThisExpression;
import at.dms.kjc.JThrowStatement;
import at.dms.kjc.JTryCatchStatement;
import at.dms.kjc.JTryFinallyStatement;
import at.dms.kjc.JTypeDeclaration;
import at.dms.kjc.JTypeDeclarationStatement;
import at.dms.kjc.JTypeNameExpression;
import at.dms.kjc.JUnaryExpression;
import at.dms.kjc.JUnaryPromote;
import at.dms.kjc.JUnqualifiedAnonymousCreation;
import at.dms.kjc.JUnqualifiedInstanceCreation;
import at.dms.kjc.JVariableDeclarationStatement;
import at.dms.kjc.JVariableDefinition;
import at.dms.kjc.JWhileStatement;
import at.dms.kjc.SLIRVisitor;
import at.dms.kjc.lir.LIRFileReader;
import at.dms.kjc.lir.LIRFileWriter;
import at.dms.kjc.lir.LIRFunctionPointer;
import at.dms.kjc.lir.LIRIdentity;
import at.dms.kjc.lir.LIRMainFunction;
import at.dms.kjc.lir.LIRNode;
import at.dms.kjc.lir.LIRRegisterReceiver;
import at.dms.kjc.lir.LIRSetBodyOfFeedback;
import at.dms.kjc.lir.LIRSetChild;
import at.dms.kjc.lir.LIRSetDecode;
import at.dms.kjc.lir.LIRSetDelay;
import at.dms.kjc.lir.LIRSetEncode;
import at.dms.kjc.lir.LIRSetJoiner;
import at.dms.kjc.lir.LIRSetLoopOfFeedback;
import at.dms.kjc.lir.LIRSetParallelStream;
import at.dms.kjc.lir.LIRSetPeek;
import at.dms.kjc.lir.LIRSetPop;
import at.dms.kjc.lir.LIRSetPush;
import at.dms.kjc.lir.LIRSetSplitter;
import at.dms.kjc.lir.LIRSetStreamType;
import at.dms.kjc.lir.LIRSetTape;
import at.dms.kjc.lir.LIRSetWork;
import at.dms.kjc.lir.LIRStreamType;
import at.dms.kjc.lir.LIRWorkEntry;
import at.dms.kjc.lir.LIRWorkExit;
import at.dms.kjc.sir.SIRCreatePortal;
import at.dms.kjc.sir.SIRDynamicToken;
import at.dms.kjc.sir.SIRInitStatement;
import at.dms.kjc.sir.SIRInterfaceTable;
import at.dms.kjc.sir.SIRIterationExpression;
import at.dms.kjc.sir.SIRJoinType;
import at.dms.kjc.sir.SIRLatency;
import at.dms.kjc.sir.SIRLatencyMax;
import at.dms.kjc.sir.SIRLatencyRange;
import at.dms.kjc.sir.SIRLatencySet;
import at.dms.kjc.sir.SIRMarker;
import at.dms.kjc.sir.SIRMessageStatement;
import at.dms.kjc.sir.SIRPeekExpression;
import at.dms.kjc.sir.SIRPopExpression;
import at.dms.kjc.sir.SIRPortal;
import at.dms.kjc.sir.SIRPrintStatement;
import at.dms.kjc.sir.SIRPushExpression;
import at.dms.kjc.sir.SIRRangeExpression;
import at.dms.kjc.sir.SIRRegReceiverStatement;
import at.dms.kjc.sir.SIRRegSenderStatement;
import at.dms.kjc.sir.SIRSplitType;
import at.dms.kjc.sir.SIRStream;
import at.dms.kjc.sir.lowering.JVectorLiteral;

public class IRPrinter extends Utils implements SLIRVisitor
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6037564323674828127L;

	/**
     * Amount the current line of text should be indented by
     */
    protected int indent;
    
    /**
     * Where printed text should go
     */
    protected BufferedWriter p;

    /**
     * Default constructor.  Writes IR to standard output.
     */
    public IRPrinter()
    {
        indent = 0;
        p = new BufferedWriter(new OutputStreamWriter(System.out));
    }

    /**
     * Build an IRPrinter for a particular file.
     *
     * @param filename  Name of the file to write IR to
     */
    public IRPrinter(String filename)
    {
        indent = 0;
        try
            {
                p = new BufferedWriter(new FileWriter(filename));
            }
        catch (IOException e)
            {
                System.err.println(e);
                System.exit(1);
            }
    }

    /**
     * Flush any pending output.
     */
    public void close()
    {
        try
            {
                p.newLine();
                p.flush();
            }
        catch (IOException e)
            {
                System.err.println(e);
                System.exit(1);
            }
    }

    /**
     * Print a newline and the current indent.
     */
    protected void printNewline()
    {
        int i;
        try
            {
                p.newLine();
                for (i = 0; i < indent; i++) {
                    p.write(' ');
                }
                p.flush();
            }
        catch (IOException e)
            {
                System.err.println(e);
                System.exit(1);
            }
    }
    
    /**
     * Print the start of a block.
     */
    protected void printStart(char delim, String name)
    {
        printNewline();
        printData(delim);
        printData(name);
        indent += 2;
    }

    /**
     * Print arbitrary string data to p.
     *
     * @param data  Data to write
     */
    protected void printData(String data)
    {
        try
            {
                p.write(data);
                p.flush();
            }
        catch (IOException e)
            {
                System.err.println(e);
                System.exit(1);
            }
    }

    /**
     * Print an arbitrary single character.
     *
     * @param data  Character to write
     */
    protected void printData(char data)
    {
        try
            {
                p.write(data);
                p.flush();
            }
        catch (IOException e)
            {
                System.err.println(e);
                System.exit(1);
            }
    }
    
    /**
     * Print the end of a block.
     */
    protected void printEnd(char delim)
    {
        printData(delim);
        indent -= 2;
    }

    /**
     * Begin an object block.
     */
    protected void blockStart(String name)
    {
        printStart('[', name);
    }
    
    /**
     * End an object block.
     */
    protected void blockEnd()
    {
        printEnd(']');
    }

    /**
     * Begin an attribute block.
     */
    protected void attrStart(String name)
    {
        printStart('(', name);
    }
    
    /**
     * End an attribute block.
     */
    protected void attrEnd()
    {
        printEnd(')');
    }
    
    /**
     * Print a single-line attribute block.
     */
    protected void attrPrint(String name, String body)
    {
        attrStart(name);
        printData(" ");
        if (body == null) {
            printData("NULL");
        } else {
            printData(body);
        }
        attrEnd();
    }

    /**
     * Print an attribute block for a Kjc object.
     */
    protected void attrPrint(String name, JPhylum body)
    {
        if (body == null)
            return;

        attrStart(name);
        body.accept(this);
        attrEnd();
    }

    /**
     * Print a multi-line attribute block corresponding to a list
     * of objects.
     */
    protected void attrList(String name, Object[] body)
    {
        if (body == null || body.length == 0)
            return;
        
        attrStart(name);
        for (int i = 0; i < body.length; i++)
            {
                printNewline();
                printData(body[i].toString());
            }
        attrEnd();
    }

    // ----------------------------------------------------------------------
    // COMPILATION UNIT
    // ----------------------------------------------------------------------

    /**
     * visits a compilation unit
     */
    @Override
	public void visitCompilationUnit(JCompilationUnit self,
                                     JPackageName packageName,
                                     JPackageImport[] importedPackages,
                                     JClassImport[] importedClasses,
                                     JTypeDeclaration[] typeDeclarations)
    {
        blockStart("CompilationUnit");

        if (packageName.getName().length() > 0)
            packageName.accept(this);

        for (int i = 0; i < importedPackages.length ; i++)
            importedPackages[i].accept(this);

        for (int i = 0; i < importedClasses.length ; i++)
            importedClasses[i].accept(this);

        for (int i = 0; i < typeDeclarations.length ; i++)
            typeDeclarations[i].accept(this);

        blockEnd();
    }

    // ----------------------------------------------------------------------
    // TYPE DECLARATION
    // ----------------------------------------------------------------------

    /**
     * visits a class declaration
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
                                      JTypeDeclaration[] decls)
    {
        blockStart("ClassDeclaration");
        
        attrPrint("modifiers", CModifier.toString(modifiers));
        attrPrint("class", ident);
        attrPrint("extends", superName);
        attrList("implements", interfaces);

        visitClassBody(decls, fields, methods, body);
        
        blockEnd();
    }
    
    /**
     * visits a class body
     */
    @Override
	public void visitClassBody(JTypeDeclaration[] decls,
                               JFieldDeclaration[] fields,
                               JMethodDeclaration[] methods,
                               JPhylum[] body)
    {
        blockStart("ClassBody");

        for (int i = 0; i < decls.length ; i++)
            decls[i].accept(this);
        for (int i = 0; i < methods.length ; i++)
            methods[i].accept(this);
        for (int i = 0; i < fields.length ; i++)
            fields[i].accept(this);
        if (body!=null) {
            for (int i = 0; i < body.length ; i++)
                body[i].accept(this);
        }

        blockEnd();
    }

    /**
     * visits a class declaration
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
                                           JMethodDeclaration[] methods)
    {
        blockStart("InnerClassDeclaration");
        
        attrPrint("modifiers", CModifier.toString(modifiers));
        attrPrint("class", ident);
        attrPrint("extends", superName);
        attrList("implements", interfaces);

        visitClassBody(decls, fields, methods, body);
        
        blockEnd();
    }
    

    /**
     * visits an interface declaration
     */
    @Override
	public void visitInterfaceDeclaration(JInterfaceDeclaration self,
                                          int modifiers,
                                          String ident,
                                          CClassType[] interfaces,
                                          JPhylum[] body,
                                          JMethodDeclaration[] methods)
    {
        blockStart("InterfaceDeclaration");
        attrPrint("modifiers", String.valueOf(modifiers));
        attrPrint("name", ident);
        attrList("implements", interfaces);
        visitClassBody(new JTypeDeclaration[0], 
                       JFieldDeclaration.EMPTY(),
                       methods, body);
        blockEnd();
    }

    // ----------------------------------------------------------------------
    // METHODS AND FIELDS
    // ----------------------------------------------------------------------

    /**
     * visits a field declaration
     */
    @Override
	public void visitFieldDeclaration(JFieldDeclaration self,
                                      int modifiers,
                                      CType type,
                                      String ident,
                                      JExpression expr)
    {
        blockStart("FieldDeclaration");
        attrPrint("modifiers", CModifier.toString(modifiers));
        attrPrint("type", type.toString());
        attrPrint("name", ident);
        if (expr != null) expr.accept(this);
        blockEnd();
    }

    /**
     * visits a method declaration
     */
    @Override
	public void visitMethodDeclaration(JMethodDeclaration self,
                                       int modifiers,
                                       CType returnType,
                                       String ident,
                                       JFormalParameter[] parameters,
                                       CClassType[] exceptions,
                                       JBlock body)
    {
        blockStart("MethodDeclaration");
        attrPrint("modifiers", CModifier.toString(modifiers));
        attrPrint("returns", returnType.toString());
        attrPrint("name", ident);
        attrStart("parameters");
        for (int i = 0; i < parameters.length; i++)
            parameters[i].accept(this);
        attrEnd();
        attrList("throws", exceptions);
        if (body != null)
            body.accept(this);
        blockEnd();
    }
    
    /**
     * visits a method declaration
     */
    @Override
	public void visitConstructorDeclaration(JConstructorDeclaration self,
                                            int modifiers,
                                            String ident,
                                            JFormalParameter[] parameters,
                                            CClassType[] exceptions,
                                            JConstructorBlock body)
    {
        blockStart("ConstructorDeclaration");
        attrPrint("modifiers", String.valueOf(modifiers));
        attrPrint("name", ident);
        attrStart("parameters");
        for (int i = 0; i < parameters.length; i++)
            parameters[i].accept(this);
        attrEnd();
        attrList("exceptions", exceptions);
        body.accept(this);
        blockEnd();
    }

    // ----------------------------------------------------------------------
    // STATEMENTS
    // ----------------------------------------------------------------------

    /**
     * visits a while statement
     */
    @Override
	public void visitWhileStatement(JWhileStatement self,
                                    JExpression cond,
                                    JStatement body)
    {
        blockStart("WhileStatement");
        cond.accept(this);
        body.accept(this);
        blockEnd();
    }

    /**
     * visits a variable declaration statement
     */
    @Override
	public void visitVariableDeclarationStatement
        (JVariableDeclarationStatement self,
         JVariableDefinition[] vars)
    {
        blockStart("VariableDeclarationStatement");
        for (int i = 0; i < vars.length; i++)
            vars[i].accept(this);
        blockEnd();
    }

    /**
     * visits a variable declaration statement
     */
    @Override
	public void visitVariableDefinition(JVariableDefinition self,
                                        int modifiers,
                                        CType type,
                                        String ident,
                                        JExpression expr)
    {
        blockStart("VariableDefinition");
        attrPrint("modifiers", String.valueOf(modifiers));
        attrPrint("type", type.toString());
        attrPrint("name", ident);
        if (expr != null) expr.accept(this);
        blockEnd();
    }
    
    /**
     * visits a try-catch statement
     */
    @Override
	public void visitTryCatchStatement(JTryCatchStatement self,
                                       JBlock tryClause,
                                       JCatchClause[] catchClauses)
    {
        blockStart("TryCatchStatement");
        tryClause.accept(this);
        for (int i = 0; i < catchClauses.length; i++)
            catchClauses[i].accept(this);
        blockEnd();
    }

    /**
     * visits a try-finally statement
     */
    @Override
	public void visitTryFinallyStatement(JTryFinallyStatement self,
                                         JBlock tryClause,
                                         JBlock finallyClause)
    {
        blockStart("TryFinallyStatement");
        tryClause.accept(this);
        finallyClause.accept(this);
        blockEnd();
    }

    /**
     * visits a throw statement
     */
    @Override
	public void visitThrowStatement(JThrowStatement self,
                                    JExpression expr)
    {
        blockStart("ThrowStatement");
        expr.accept(this);
        blockEnd();
    }

    /**
     * visits a synchronized statement
     */
    @Override
	public void visitSynchronizedStatement(JSynchronizedStatement self,
                                           JExpression cond,
                                           JStatement body)
    {
        blockStart("SynchronizedStatement");
        cond.accept(this);
        body.accept(this);
        blockEnd();
    }

    /**
     * visits a switch statement
     */
    @Override
	public void visitSwitchStatement(JSwitchStatement self,
                                     JExpression expr,
                                     JSwitchGroup[] body)
    {
        blockStart("SwitchStatement");
        expr.accept(this);
        for (int i = 0; i < body.length; i++)
            body[i].accept(this);
        blockEnd();
    }

    /**
     * visits a return statement
     */
    @Override
	public void visitReturnStatement(JReturnStatement self,
                                     JExpression expr)
    {
        blockStart("Return");
        if (expr != null) expr.accept(this);
        blockEnd();
    }

    /**
     * visits a labeled statement
     */
    @Override
	public void visitLabeledStatement(JLabeledStatement self,
                                      String label,
                                      JStatement stmt)
    {
        blockStart("LabeledStatement");
        attrPrint("label", label);
        stmt.accept(this);
        blockEnd();
    }

    /**
     * visits a if statement
     */
    @Override
	public void visitIfStatement(JIfStatement self,
                                 JExpression cond,
                                 JStatement thenClause,
                                 JStatement elseClause)
    {
        blockStart("IfStatement");
        cond.accept(this);
        thenClause.accept(this);
        if (elseClause != null) elseClause.accept(this);
        blockEnd();
    }

    /**
     * visits a for statement
     */
    @Override
	public void visitForStatement(JForStatement self,
                                  JStatement init,
                                  JExpression cond,
                                  JStatement incr,
                                  JStatement body)
    {
        blockStart("ForStatement");
        attrPrint("init", init);
        attrPrint("cond", cond);
        attrPrint("incr", incr);
        body.accept(this);
        blockEnd();
    }

    /**
     * visits a compound statement
     */
    @Override
	public void visitCompoundStatement(JCompoundStatement self,
                                       JStatement[] body)
    {
        blockStart("CompoundStatement");
        for (int i = 0; i < body.length; i++)
            body[i].accept(this);
        blockEnd();
    }

    /**
     * visits an expression statement
     */
    @Override
	public void visitExpressionStatement(JExpressionStatement self,
                                         JExpression expr)
    {
        blockStart("ExpressionStatement");
        expr.accept(this);
        blockEnd();
    }

    /**
     * visits an expression list statement
     */
    @Override
	public void visitExpressionListStatement(JExpressionListStatement self,
                                             JExpression[] expr)
    {
        blockStart("ExpressionListStatement");
        for (int i = 0; i < expr.length; i++)
            expr[i].accept(this);
        blockEnd();
    }

    /**
     * visits a empty statement
     */
    @Override
	public void visitEmptyStatement(JEmptyStatement self)
    {
        blockStart("EmptyStatement");
        blockEnd();
    }

    /**
     * visits a do statement
     */
    @Override
	public void visitDoStatement(JDoStatement self,
                                 JExpression cond,
                                 JStatement body)
    {
        blockStart("DoStatement");
        attrPrint("cond", cond);
        body.accept(this);
        blockEnd();
    }

    /**
     * visits a continue statement
     */
    @Override
	public void visitContinueStatement(JContinueStatement self,
                                       String label)
    {
        blockStart("ContinueStatement");
        attrPrint("label", label);
        blockEnd();
    }

    /**
     * visits a break statement
     */
    @Override
	public void visitBreakStatement(JBreakStatement self,
                                    String label)
    {
        blockStart("BreakStatement");
        attrPrint("label", label);
        blockEnd();
    }

    /**
     * visits an expression statement
     */
    @Override
	public void visitBlockStatement(JBlock self,
                                    JavaStyleComment[] comments)
    {
        blockStart("BlockStatement");
        for (ListIterator it = self.getStatementIterator(); it.hasNext(); ) {
            ((JStatement)it.next()).accept(this);
        }
        // comments
        blockEnd();
    }

    /**
     * visits a type declaration statement
     */
    @Override
	public void visitTypeDeclarationStatement(JTypeDeclarationStatement self,
                                              JTypeDeclaration decl)
    {
        blockStart("TypeDeclarationStatement");
        decl.accept(this);
        blockEnd();
    }

    // ----------------------------------------------------------------------
    // EXPRESSION
    // ----------------------------------------------------------------------

    /**
     * visits an unary plus expression
     */
    @Override
	public void visitUnaryPlusExpression(JUnaryExpression self,
                                         JExpression expr)
    {
        blockStart("UnaryPlusExpression");
        expr.accept(this);
        blockEnd();
    }

    /**
     * visits an unary minus expression
     */
    @Override
	public void visitUnaryMinusExpression(JUnaryExpression self,
                                          JExpression expr)
    {
        blockStart("UnaryMinusExpression");
        expr.accept(this);
        blockEnd();
    }

    /**
     * visits a bitwise complement expression
     */
    @Override
	public void visitBitwiseComplementExpression(JUnaryExpression self,
                                                 JExpression expr)
    {
        blockStart("BitwiseComplementExpression");
        expr.accept(this);
        blockEnd();
    }

    /**
     * visits a logical complement expression
     */
    @Override
	public void visitLogicalComplementExpression(JUnaryExpression self,
                                                 JExpression expr)
    {
        blockStart("LogicalComplementExpression");
        expr.accept(this);
        blockEnd();
    }

    /**
     * visits a type name expression
     */
    @Override
	public void visitTypeNameExpression(JTypeNameExpression self,
                                        CType type)
    {
        blockStart("TypeNameExpression");
        printData(' ');
        printData(type.toString());
        blockEnd();
    }

    /**
     * visits a this expression
     */
    @Override
	public void visitThisExpression(JThisExpression self,
                                    JExpression prefix)
    {
        blockStart("ThisExpression");
        attrPrint("prefix", prefix);
        blockEnd();
    }

    /**
     * visits a super expression
     */
    @Override
	public void visitSuperExpression(JSuperExpression self)
    {
        blockStart("SuperExpression");
        blockEnd();
    }

    /**
     * visits a shift expression
     */
    @Override
	public void visitShiftExpression(JShiftExpression self,
                                     int oper,
                                     JExpression left,
                                     JExpression right)
    {
        blockStart("ShiftExpression");
        attrPrint("oper", String.valueOf(oper));
        attrPrint("left", left);
        attrPrint("right", right);
        blockEnd();
    }

    /**
     * visits a shift expressiona
     */
    @Override
	public void visitRelationalExpression(JRelationalExpression self,
                                          int oper,
                                          JExpression left,
                                          JExpression right)
    {
        blockStart("RelationalExpression");
        attrPrint("oper", String.valueOf(oper));
        attrPrint("left", left);
        attrPrint("right", right);
        blockEnd();
    }

    /**
     * visits a prefix expression
     */
    @Override
	public void visitPrefixExpression(JPrefixExpression self,
                                      int oper,
                                      JExpression expr)
    {
        blockStart("PrefixExpression");
        attrPrint("oper", String.valueOf(oper));
        expr.accept(this);
        blockEnd();
    }

    /**
     * visits a postfix expression
     */
    @Override
	public void visitPostfixExpression(JPostfixExpression self,
                                       int oper,
                                       JExpression expr)
    {
        blockStart("PostfixExpression");
        attrPrint("oper", String.valueOf(oper));
        expr.accept(this);
        blockEnd();
    }

    /**
     * visits a parenthesed expression
     */
    @Override
	public void visitParenthesedExpression(JParenthesedExpression self,
                                           JExpression expr)
    {
        // Not parenthesized?
        blockStart("ParenthesedExpression");
        expr.accept(this);
        blockEnd();
    }

    /**
     * Visits an unqualified anonymous class instance creation expression.
     */
    @Override
	public void visitQualifiedAnonymousCreation
        (JQualifiedAnonymousCreation self,
         JExpression prefix,
         String ident,
         JExpression[] params,
         JClassDeclaration decl)
    {
        blockStart("QualifiedAnonymousCreation");
        attrPrint("prefix", prefix);
        attrPrint("name", ident);
        attrStart("params");
        for (int i = 0; i < params.length; i++)
            params[i].accept(this);
        attrEnd();
        attrPrint("decl", decl);
        blockEnd();
    }

    /**
     * Visits an unqualified instance creation expression.
     */
    @Override
	public void visitQualifiedInstanceCreation(JQualifiedInstanceCreation self,
                                               JExpression prefix,
                                               String ident,
                                               JExpression[] params)
    {
        blockStart("QualifiedInstanceCreation");
        attrPrint("prefix", prefix);
        attrPrint("name", ident);
        attrStart("params");
        for (int i = 0; i < params.length; i++)
            params[i].accept(this);
        attrEnd();
        blockEnd();
    }
    
    /**
     * Visits an unqualified anonymous class instance creation expression.
     */
    @Override
	public void visitUnqualifiedAnonymousCreation
        (JUnqualifiedAnonymousCreation self,
         CClassType type,
         JExpression[] params,
         JClassDeclaration decl)
    {
        blockStart("UnqualifiedAnonymousCreation");
        attrPrint("type", type.toString());
        attrStart("params");
        for (int i = 0; i < params.length; i++)
            params[i].accept(this);
        attrEnd();
        attrPrint("decl", decl);
        blockEnd();
    }

    /**
     * Visits an unqualified instance creation expression.
     */
    @Override
	public void visitUnqualifiedInstanceCreation
        (JUnqualifiedInstanceCreation self,
         CClassType type,
         JExpression[] params)
    {
        blockStart("UnqualifiedInstanceCreation");
        attrPrint("type", type.toString());
        attrStart("params");
        for (int i = 0; i < params.length; i++)
            params[i].accept(this);
        attrEnd();
        blockEnd();
    }

    /**
     * visits an array allocator expression
     */
    @Override
	public void visitNewArrayExpression(JNewArrayExpression self,
                                        CType type,
                                        JExpression[] dims,
                                        JArrayInitializer init)
    {
        blockStart("NewArrayExpression");
        attrPrint("type", type.toString());
        attrStart("dims");
        for (int i = 0; i < dims.length; i++) {
            // could be null if you're doing something like "new int[10][]"
            if (dims[i]!=null) {
                dims[i].accept(this);
            }
        }
        attrEnd();
        attrPrint("init", init);
        blockEnd();
    }

    /**
     * visits a name expression
     */
    @Override
	public void visitNameExpression(JNameExpression self,
                                    JExpression prefix,
                                    String ident)
    {
        blockStart("NameExpression");
        attrPrint("prefix", prefix);
        attrPrint("name", ident);
        blockEnd();
    }

    /**
     * visits an array allocator expression
     */
    @Override
	public void visitBinaryExpression(JBinaryExpression self,
                                      String oper,
                                      JExpression left,
                                      JExpression right)
    {
        blockStart("BinaryExpression");
        printData(' ');
        printData(oper);
        left.accept(this);
        right.accept(this);
        blockEnd();
    }

    /**
     * visits a method call expression
     */
    @Override
	public void visitMethodCallExpression(JMethodCallExpression self,
                                          JExpression prefix,
                                          String ident,
                                          JExpression[] args)
    {
        blockStart("MethodCallExpression");
        attrPrint("prefix", prefix);
        attrPrint("name", ident);
        attrStart("args");
        for (int i = 0; i < args.length; i++)
            args[i].accept(this);
        attrEnd();
        blockEnd();
    }
    
    /**
     * visits a local variable expression
     */
    @Override
	public void visitLocalVariableExpression(JLocalVariableExpression self,
                                             String ident)
    {
        blockStart("LocalVariableExpression");
        printData(' ');
        printData(ident);
        blockEnd();
    }

    /**
     * visits an instanceof expression
     */
    @Override
	public void visitInstanceofExpression(JInstanceofExpression self,
                                          JExpression expr,
                                          CType dest)
    {
        blockStart("InstanceOfExpression");
        attrPrint("type", dest.toString());
        expr.accept(this);
        blockEnd();
    }

    /**
     * visits an equality expression
     */
    @Override
	public void visitEqualityExpression(JEqualityExpression self,
                                        boolean equal,
                                        JExpression left,
                                        JExpression right)
    {
        blockStart("EqualityExpression");
        attrPrint("equal", String.valueOf(equal));
        attrPrint("left", left);
        attrPrint("right", right);
        blockEnd();
    }

    /**
     * visits a conditional expression
     */
    @Override
	public void visitConditionalExpression(JConditionalExpression self,
                                           JExpression cond,
                                           JExpression left,
                                           JExpression right)
    {
        blockStart("ConditionalExpression");
        attrPrint("cond", cond);
        attrPrint("left", left);
        attrPrint("right", right);
        blockEnd();
    }

    /**
     * visits a compound expression
     */
    @Override
	public void visitCompoundAssignmentExpression
        (JCompoundAssignmentExpression self,
         int oper,
         JExpression left,
         JExpression right)
    {
        blockStart("CompoundAssignmentExpression");
        attrPrint("oper", String.valueOf(oper));
        attrPrint("left", left);
        attrPrint("right", right);
        blockEnd();
    }

    /**
     * visits a field expression
     */
    @Override
	public void visitFieldExpression(JFieldAccessExpression self,
                                     JExpression left,
                                     String ident)
    {
        blockStart("FieldExpression");
        attrPrint("left", left);
        attrPrint("name", ident);
        blockEnd();
    }

    /**
     * visits a class expression
     */
    @Override
	public void visitClassExpression(JClassExpression self,
                                     CType type)
    {
        blockStart("ClassExpression");
        printData(' ');
        printData(type.toString());
        blockEnd();
    }

    /**
     * visits a cast expression
     */
    @Override
	public void visitCastExpression(JCastExpression self,
                                    JExpression expr,
                                    CType type)
    {
        blockStart("CastExpression");
        attrPrint("type", type.toString());
        expr.accept(this);
        blockEnd();
    }

    /**
     * visits a cast expression
     */
    @Override
	public void visitUnaryPromoteExpression(JUnaryPromote self,
                                            JExpression expr,
                                            CType type)
    {
        blockStart("UnaryPromoteExpression");
        attrPrint("type", type.toString());
        expr.accept(this);
        blockEnd();
    }

    /**
     * visits a compound assignment expression
     */
    @Override
	public void visitBitwiseExpression(JBitwiseExpression self,
                                       int oper,
                                       JExpression left,
                                       JExpression right)
    {
        blockStart("BitwiseExpression");
        attrPrint("oper", String.valueOf(oper));
        attrPrint("left", left);
        attrPrint("right", right);
        blockEnd();
    }
    
    /**
     * visits an assignment expression
     */
    @Override
	public void visitAssignmentExpression(JAssignmentExpression self,
                                          JExpression left,
                                          JExpression right)
    {
        blockStart("AssignmentExpression");
        left.accept(this);
        right.accept(this);
        blockEnd();
    }

    /**
     * visits an array length expression
     */
    @Override
	public void visitArrayLengthExpression(JArrayLengthExpression self,
                                           JExpression prefix)
    {
        blockStart("ArrayLengthExpression");
        prefix.accept(this);
        blockEnd();
    }

    /**
     * visits an array length expression
     */
    @Override
	public void visitArrayAccessExpression(JArrayAccessExpression self,
                                           JExpression prefix,
                                           JExpression accessor)
    {
        blockStart("ArrayAccessExpression");
        attrPrint("prefix", prefix);
        attrPrint("accessor", accessor);
        blockEnd();
    }

    /**
     * visits an array length expression
     */
    @Override
	public void visitComments(JavaStyleComment[] comments) { }

    /**
     * visits an array length expression
     */
    @Override
	public void visitComment(JavaStyleComment comment) { }

    /**
     * visits an array length expression
     */
    @Override
	public void visitJavadoc(JavadocComment comment) { }

    // ----------------------------------------------------------------------
    // OTHERS
    // ----------------------------------------------------------------------

    /**
     * visits an array length expression
     */
    @Override
	public void visitSwitchLabel(JSwitchLabel self,
                                 JExpression expr)
    {
        blockStart("SwitchLabel");
        expr.accept(this);
        blockEnd();
    }

    /**
     * visits an array length expression
     */
    @Override
	public void visitSwitchGroup(JSwitchGroup self,
                                 JSwitchLabel[] labels,
                                 JStatement[] stmts)
    {
        blockStart("SwitchGroup");
        attrStart("labels");
        for (int i = 0; i < labels.length; i++)
            labels[i].accept(this);
        attrEnd();
        attrStart("stmts");
        for (int i = 0; i < stmts.length; i++)
            stmts[i].accept(this);
        attrEnd();
        blockEnd();
    }

    /**
     * visits an array length expression
     */
    @Override
	public void visitCatchClause(JCatchClause self,
                                 JFormalParameter exception,
                                 JBlock body)
    {
        blockStart("CatchClause");
        exception.accept(this);
        body.accept(this);
        blockEnd();
    }

    /**
     * visits an array length expression
     */
    @Override
	public void visitFormalParameters(JFormalParameter self,
                                      boolean isFinal,
                                      CType type,
                                      String ident)
    {
        blockStart("FormalParameter");
        if (isFinal) attrPrint("final", "true");
        attrPrint("type", type.toString());
        attrPrint("name", ident);
        blockEnd();
    }

    /**
     * visits an array length expression
     */
    @Override
	public void visitConstructorCall(JConstructorCall self,
                                     boolean functorIsThis,
                                     JExpression[] params)
    {
        blockStart("ConstructorCall");
        attrPrint("this", String.valueOf(functorIsThis));
        attrStart("params");
        for (int i = 0; i < params.length; i++)
            params[i].accept(this);
        attrEnd();
        blockEnd();
    }

    /**
     * visits an array initializer expression
     */
    @Override
	public void visitArrayInitializer(JArrayInitializer self,
                                      JExpression[] elems)
    {
        blockStart("ArrayInitializer");
        for (int i = 0; i < elems.length; i++)
            elems[i].accept(this);
        blockEnd();
    }

    /**
     * visits a boolean literal
     */
    @Override
	public void visitBooleanLiteral(boolean value)
    {
        blockStart("BooleanLiteral");
        if (value)
            printData(" true");
        else
            printData(" false");
        blockEnd();
    }

    /**
     * visits a byte literal
     */
    @Override
	public void visitByteLiteral(byte value)
    {
        blockStart("ByteLiteral");
        printData(' ');
        printData(String.valueOf(value));
        blockEnd();
    }

    /**
     * visits a character literal
     */
    @Override
	public void visitCharLiteral(char value)
    {
        blockStart("CharLiteral");
        printData(' ');
        printData(value);
        blockEnd();
    }

    /**
     * visits a double literal
     */
    @Override
	public void visitDoubleLiteral(double value)
    {
        blockStart("DoubleLiteral");
        printData(' ');
        printData(String.valueOf(value));
        blockEnd();
    }

    /**
     * visits a float literal
     */
    @Override
	public void visitFloatLiteral(float value)
    {
        blockStart("FloatLiteral");
        printData(' ');
        printData(String.valueOf(value));
        blockEnd();
    }

    /**
     * visits a int literal
     */
    @Override
	public void visitIntLiteral(int value)
    {
        blockStart("IntLiteral");
        printData(' ');
        printData(String.valueOf(value));
        blockEnd();
    }

    /**
     * visits a long literal
     */
    @Override
	public void visitLongLiteral(long value)
    {
        blockStart("LongLiteral");
        printData(' ');
        printData(String.valueOf(value));
        blockEnd();
    }

    /**
     * visits a short literal
     */
    @Override
	public void visitShortLiteral(short value)
    {
        blockStart("ShortLiteral");
        printData(' ');
        printData(String.valueOf(value));
        blockEnd();
    }

    /**
     * visits a string literal
     */
    @Override
	public void visitStringLiteral(String value)
    {
        blockStart("StringLiteral");
        printData(' ');
        printData('\"');
        printData(value);
        printData('\"');
        blockEnd();
    }

    /**
     * visits a null literal
     */
    @Override
	public void visitNullLiteral()
    {
        blockStart("NullLiteral");
        blockEnd();
    }

    /**
     * visits a package name declaration
     */
    @Override
	public void visitPackageName(String name)
    {
        blockStart("PackageName");
        printData(" ");
        printData(name);
        blockEnd();
    }

    /**
     * visits a package import declaration
     */
    @Override
	public void visitPackageImport(String name)
    {
        blockStart("PackageImport");
        printData(" ");
        printData(name);
        blockEnd();
    }

    /**
     * visits a class import declaration
     */
    @Override
	public void visitClassImport(String name)
    {
        blockStart("ClassImport");
        printData(" ");
        printData(name);
        blockEnd();
    }

    /**
     * SIR NODES
     */

    /**
     * Visits an init statement.
     */
    @Override
	public void visitInitStatement(SIRInitStatement self,
                                   SIRStream target) {
        blockStart("SIRInitStatement");
        attrPrint("target ", target.toString());
        attrStart("args");
        List args = self.getArgs();
        for (int i=0; i<args.size(); i++) {
            ((JExpression)args.get(i)).accept(this);
        }
        attrEnd();
        blockEnd();
    }

    /**
     * Visits an interface table.
     */
    @Override
	public void visitInterfaceTable(SIRInterfaceTable self)
    {
        blockStart("SIRInterfaceTable");
        attrPrint("interface", self.getIface().getIdent());
        attrStart("methods");
        for (int i = 0; i < self.getMethods().length; i++)
            printData(self.getMethods()[i].getName());
        attrEnd();
        blockEnd();
    }

    /**
     * Visits a latency.
     */
    @Override
	public void visitLatency(SIRLatency self) {
        blockStart("SIRLatency");
        if (self==SIRLatency.BEST_EFFORT) {
            printData("BEST EFFORT");
        }
        blockEnd();
    }


    /**
     * Visits a max latency.
     */
    @Override
	public void visitLatencyMax(SIRLatencyMax self) {
        blockStart("SIRLatencyMax");
        attrPrint("max", String.valueOf(self.getMax()));
        blockEnd();
    }


    /**
     * Visits a latency range.
     */
    @Override
	public void visitLatencyRange(SIRLatencyRange self) {
        blockStart("SIRLatencyRange");
        attrPrint("max", String.valueOf(self.getMax()));
        attrPrint("min", String.valueOf(self.getMin()));
        blockEnd();
    }


    /**
     * Visits a latency set.
     */
    @Override
	public void visitLatencySet(SIRLatencySet self) {
        blockStart("SIRLatencySet");
        Utils.fail("Printing list of latencies not implemented yet.");
        blockEnd();
    }


    /**
     * Visits a message statement.
     */
    @Override
	public void visitMessageStatement(SIRMessageStatement self,
                                      JExpression portal,
                                      String iname,
                                      String ident,
                                      JExpression[] args,
                                      SIRLatency latency) {
        blockStart("SIRMessageStatement");
        attrStart("portal");
        portal.accept(this);
        attrEnd();
        attrPrint("iname", iname);
        attrPrint("ident", ident);
        attrStart("args");
        for (int i = 0; i < args.length; i++)
            args[i].accept(this);
        attrEnd();
        attrStart("latency");
        latency.accept(this);
        attrEnd();
        blockEnd();
    }


    @Override
	public void visitRangeExpression(SIRRangeExpression self) {
        blockStart("SIRRangeExpression");

        // min
        attrStart("min");
        self.getMin().accept(this);
        attrEnd();
        // ave
        attrStart("ave");
        self.getAve().accept(this);
        attrEnd();
        // max
        attrStart("max");
        self.getMax().accept(this);
        attrEnd();

        blockEnd();
    }

    @Override
	public void visitDynamicToken(SIRDynamicToken self) {
        blockStart("DynamicToken");
        blockEnd();
    }

    /**
     * Visits a peek expression.
     */
    @Override
	public void visitPeekExpression(SIRPeekExpression self,
                                    CType tapeType,
                                    JExpression arg) {
        blockStart("SIRPeekExpression");
        attrStart("arg");
        arg.accept(this);
        attrEnd();
        blockEnd();
    }

    @Override
    public void visitIterationExpression(
		SIRIterationExpression sirIterationExpression) {
	blockStart("SIRIterationExpression");
	blockEnd();
    }

    /**
     * Visits a pop expression.
     */
    @Override
	public void visitPopExpression(SIRPopExpression self,
                                   CType tapeType) {
        blockStart("SIRPopExpression");
        blockEnd();
    }

    /**
     * Visits a message-receiving portal.
     */
    @Override
	public void visitPortal(SIRPortal self)
    {
        blockStart("SIRPortal");
        blockEnd();
    }

    /**
     * Visits a print statement.
     */
    @Override
	public void visitPrintStatement(SIRPrintStatement self,
                                    JExpression arg) {
        blockStart("SIRPrintStatement");
        attrStart("arg");
        arg.accept(this);
        attrEnd();
        blockEnd();
    }

    @Override
	public void visitCreatePortalExpression(SIRCreatePortal self) {
        blockStart("SIRCreatePortalExpression");
        blockEnd();
    }
    

    /**
     * Visits a push expression.
     */
    @Override
	public void visitPushExpression(SIRPushExpression self,
                                    CType tapeType,
                                    JExpression arg) {
        blockStart("SIRPushExpression");
        attrStart("arg");
        arg.accept(this);
        attrEnd();
        blockEnd();
    }

//    /**
//     * Visit a phase-invocation statement.
//     */
//    public void visitPhaseInvocation(SIRPhaseInvocation self,
//                                     JMethodCallExpression call,
//                                     JExpression peek,
//                                     JExpression pop,
//                                     JExpression push)
//    {
//        blockStart("SIRPhaseInvocation");
//        attrPrint("call", call);
//        attrPrint("peek", peek);
//        attrPrint("pop", pop);
//        attrPrint("push", push);
//        blockEnd();
//    }

    /**
     * Visits a register-receiver statement.
     */
    @Override
	public void visitRegReceiverStatement(SIRRegReceiverStatement self,
                                          JExpression portal,
                                          SIRStream receiver,
                                          JMethodDeclaration[] methods) {
        blockStart("SIRRegReceiveStatement");
        attrStart("portal");
        portal.accept(this);
        attrEnd();
        attrStart("methods");
        for (int i = 0; i < methods.length; i++)
            methods[i].accept(this);
        attrEnd();
        blockEnd();
    }


    /**
     * Visits a register-sender statement.
     */
    @Override
	public void visitRegSenderStatement(SIRRegSenderStatement self,
                                        String portal,
                                        SIRLatency latency) {
        Utils.fail("Printing reg. sender statements unimplemented");
    }

    /**
     * Visits a marker statement.
     */
    @Override
	public void visitMarker(SIRMarker self) {
        blockStart("SIRMarker");
        blockEnd();
    }

    /**
     * LIR NODES.
     */

    /**
     * Visits a function pointer.
     */
    @Override
	public void visitFunctionPointer(LIRFunctionPointer self,
                                     String name) {
        blockStart("LIRFunctionPointer");
        attrPrint("name", name);
        blockEnd();
    }

    
    /**
     * Visits an LIR node.
     */
    @Override
	public void visitNode(LIRNode self) {
        blockStart("LIRNode");
        blockEnd();
    }

    /**
     * Visits an LIR register-receiver statement.
     */
    @Override
	public void visitRegisterReceiver(LIRRegisterReceiver self,
                                      JExpression streamContext,
                                      SIRPortal portal,
                                      String childName,
                                      SIRInterfaceTable itable) {
        blockStart("LIRRegisterReceiver");
        attrStart("parentContext");
        streamContext.accept(this);
        attrEnd();
        attrStart("portal");
        portal.accept(this);
        attrEnd();
        attrPrint("childName", childName);
        attrStart("itable");
        itable.accept(this);
        attrEnd();
        blockEnd();
    }

    /**
     * Visits a child registration node.
     */
    @Override
	public void visitSetChild(LIRSetChild self,
                              JExpression streamContext,
                              String childType,
                              String childName) {
        blockStart("LIRSetChild");
        attrStart("parentContext");
        streamContext.accept(this);
        attrEnd();
        attrPrint("childType", childType);
        attrPrint("childName", childName);
        blockEnd();
    }

    
    /**
     * Visits a decoder registration node.
     */
    @Override
	public void visitSetDecode(LIRSetDecode self,
                               JExpression streamContext,
                               LIRFunctionPointer fp) {
        blockStart("LIRSetDecode");
        attrStart("streamContext");
        streamContext.accept(this);
        attrEnd();
        attrStart("decode_function");
        fp.accept(this);
        attrEnd();
        blockEnd();
    }


    /**
     * Visit a feedback loop delay node.
     */
    @Override
	public void visitSetDelay(LIRSetDelay self,
                              JExpression data,
                              JExpression streamContext,
                              int delay,
                              CType type,
                              LIRFunctionPointer fp)
    {
        blockStart("LIRSetDelay");
        attrPrint("data", data);
        attrPrint("streamContext", streamContext);
        attrPrint("delay", String.valueOf(delay));
        attrPrint("type", type.toString());
        attrStart("fp");
        fp.accept(this);
        attrEnd();
        blockEnd();
    }

    
    /**
     * Visits an encoder registration node.
     */
    @Override
	public void visitSetEncode(LIRSetEncode self,
                               JExpression streamContext,
                               LIRFunctionPointer fp) {
        blockStart("LIRSetEncode");
        attrStart("streamContext");
        streamContext.accept(this);
        attrEnd();
        attrStart("encode_function");
        fp.accept(this);
        attrEnd();
        blockEnd();
    }

    /**
     * Visits a joiner-setting node.
     */
    @Override
	public void visitSetJoiner(LIRSetJoiner self,
                               JExpression streamContext,
                               SIRJoinType type,
                               int ways,
                               int[] weights) {
        blockStart("LIRSetJoiner");
        attrStart("streamContext");
        streamContext.accept(this);
        attrEnd();
        attrPrint("type", type.toString());
        attrPrint("ways", String.valueOf(ways));
        if (weights != null)
            {
                attrStart("weights");
                for (int i = 0; i < ways; i++)
                    printData(String.valueOf(weights[i]));
                attrEnd();
            }
        blockEnd();
    }
    
    /**
     * Visits a peek-rate-setting node.
     */
    @Override
	public void visitSetPeek(LIRSetPeek self,
                             JExpression streamContext,
                             int peek) {
        blockStart("LIRSetPeek");
        attrStart("streamContext");
        streamContext.accept(this);
        attrEnd();
        attrPrint("peek_count", String.valueOf(peek));
        blockEnd();
    }

    
    /**
     * Visits a pop-rate-setting node.
     */
    @Override
	public void visitSetPop(LIRSetPop self,
                            JExpression streamContext,
                            int pop) {
        blockStart("LIRSetPop");
        attrStart("streamContext");
        streamContext.accept(this);
        attrEnd();
        attrPrint("pop_count", String.valueOf(pop));
        blockEnd();
    }

    
    /**
     * Visits a push-rate-setting node.
     */
    @Override
	public void visitSetPush(LIRSetPush self,
                             JExpression streamContext,
                             int push) {
        blockStart("LIRSetPush");
        attrStart("streamContext");
        streamContext.accept(this);
        attrEnd();
        attrPrint("push_count", String.valueOf(push));
        blockEnd();
    }

    /**
     * Visits a splitter-setting node.
     */
    @Override
	public void visitSetSplitter(LIRSetSplitter self,
                                 JExpression streamContext,
                                 SIRSplitType type,
                                 int ways,
                                 int[] weights) {
        blockStart("LIRSetSplitter");
        attrStart("streamContext");
        streamContext.accept(this);
        attrEnd();
        attrPrint("type", type.toString());
        attrPrint("ways", String.valueOf(ways));
        if (weights != null)
            {
                attrStart("weights");
                for (int i = 0; i < ways; i++)
                    printData(String.valueOf(weights[i]));
                attrEnd();
            }
        blockEnd();
    }
    

    /**
     * Visits a stream-type-setting node.
     */
    @Override
	public void visitSetStreamType(LIRSetStreamType self,
                                   JExpression streamContext,
                                   LIRStreamType streamType) {
        blockStart("LIRSetStreamType");
        attrStart("streamContext");
        streamContext.accept(this);
        attrEnd();
        attrPrint("stream_type", streamType.toString());
        blockEnd();
    }

    
    /**
     * Visits a work-function-setting node.
     */
    @Override
	public void visitSetWork(LIRSetWork self,
                             JExpression streamContext,
                             LIRFunctionPointer fn) {
        blockStart("LIRSetWork");
        attrStart("streamContext");
        streamContext.accept(this);
        attrEnd();
        attrStart("work_function");
        fn.accept(this);
        attrEnd();
        blockEnd();
    }

    /**
     * Visits a tape-setter.
     */
    @Override
	public void visitSetTape(LIRSetTape self,
                             JExpression streamContext,
                             JExpression srcStruct,
                             JExpression dstStruct,
                             CType type,
                             int size) {
        blockStart("LIRSetTape");
        attrStart("streamContext");
        streamContext.accept(this);
        attrEnd();
        attrStart("srcStruct");
        srcStruct.accept(this);
        attrEnd();
        attrStart("dstStruct");
        dstStruct.accept(this);
        attrEnd();
        attrPrint("type", type.toString());
        attrPrint("size", String.valueOf(size));
        blockEnd();
    }

    /**
     * Visits a main function contents.
     */
    @Override
	public void visitMainFunction(LIRMainFunction self,
                                  String typeName,
                                  LIRFunctionPointer init,
                                  List<JStatement> initStatements) {
        blockStart("LIRMainFunction");
        attrPrint("typeName", typeName);
        attrStart("init");
        init.accept(this);
        attrEnd();
        printData("init statements:");
        for (ListIterator<JStatement> it = initStatements.listIterator(); it.hasNext(); ) {
            it.next().accept(this);
        }
        blockEnd();
    }

    /**
     * Visits a set body of feedback loop.
     */
    @Override
	public void visitSetBodyOfFeedback(LIRSetBodyOfFeedback self,
                                       JExpression streamContext,
                                       JExpression childContext,
                                       CType inputType,
                                       CType outputType,
                                       int inputSize,
                                       int outputSize) {
        blockStart("LIRSetBodyOfFeedback");
        attrStart("streamContext");
        streamContext.accept(this);
        attrEnd();
        attrStart("childContext");
        childContext.accept(this);
        attrEnd();
        attrPrint("input type", inputType.toString());
        attrPrint("output type", outputType.toString());
        attrPrint("input size", String.valueOf(inputSize));
        attrPrint("output size", String.valueOf(outputSize));
        blockEnd();
    }

    /**
     * Visits a set loop of feedback loop.
     */
    @Override
	public void visitSetLoopOfFeedback(LIRSetLoopOfFeedback self,
                                       JExpression streamContext,
                                       JExpression childContext,
                                       CType inputType,
                                       CType outputType,
                                       int inputSize,
                                       int outputSize) {
        blockStart("LIRSetLoopOfFeedback");
        attrStart("streamContext");
        streamContext.accept(this);
        attrEnd();
        attrStart("childContext");
        childContext.accept(this);
        attrEnd();
        attrPrint("input type", inputType.toString());
        attrPrint("output type", outputType.toString());
        attrPrint("input size", String.valueOf(inputSize));
        attrPrint("output size", String.valueOf(outputSize));
        blockEnd();
    }


    /**
     * Visits a file reader.
     */
    @Override
	public void visitFileReader(LIRFileReader self) {
        blockStart("LIRFileReader");
        attrStart("streamContext");
        self.getStreamContext().accept(this);
        attrEnd();
        attrPrint("file name", self.getFileName());
        blockEnd();
    }
    
    /**
     * Visits a file writer.
     */
    @Override
	public void visitFileWriter(LIRFileWriter self) {
        blockStart("LIRFileWriter");
        attrStart("streamContext");
        self.getStreamContext().accept(this);
        attrEnd();
        attrPrint("file name", self.getFileName());
        blockEnd();
    }
    
    /**
     * Visits an identity creator.
     */
    @Override
	public void visitIdentity(LIRIdentity self) {
        blockStart("LIRIdentity");
        attrStart("streamContext");
        self.getStreamContext().accept(this);
        attrEnd();
        blockEnd();
    }
    
    /**
     * Visits a set a parallel stream.
     */
    @Override
	public void visitSetParallelStream(LIRSetParallelStream self,
                                       JExpression streamContext,
                                       JExpression childContext,
                                       int position,
                                       CType inputType,
                                       CType outputType,
                                       int inputSize,
                                       int outputSize) {
        blockStart("LIRSetParallelStream");
        attrStart("streamContext");
        streamContext.accept(this);
        attrEnd();
        attrStart("childContext");
        childContext.accept(this);
        attrEnd();
        attrPrint("position", String.valueOf(position));
        attrPrint("input type", inputType.toString());
        attrPrint("output type", outputType.toString());
        attrPrint("input size", String.valueOf(inputSize));
        attrPrint("output size", String.valueOf(outputSize));
        blockEnd();
    }

    /**
     * Visits a work function entry.
     */
    @Override
	public void visitWorkEntry(LIRWorkEntry self)
    {
        blockStart("LIRWorkEntry");
        attrStart("streamContext");
        self.getStreamContext().accept(this);
        attrEnd();
        blockEnd();
    }

    /**
     * Visits a work function exit.
     */
    @Override
	public void visitWorkExit(LIRWorkExit self)
    {
        blockStart("LIRWorkExit");
        attrStart("streamContext");
        self.getStreamContext().accept(this);
        attrEnd();
        blockEnd();
    }

    /**
     * Visits a vector literal.
     */
    @Override
	public void visitVectorLiteral(JVectorLiteral self, JLiteral scalar) {
        blockStart("VectorLiteral");
        scalar.accept(this);
        blockEnd();
    }

    @Override
	public void visitEmittedTextExpression(JEmittedTextExpression self, Object[] parts) {
        blockStart("Text");
        for (Object part : parts) {
            if (part instanceof JExpression) {
                ((JExpression)part).accept(this);
            } else if (part instanceof CType) {
                attrPrint("type", ((CType)part).toString());
            } else {
                assert part instanceof String;
                attrPrint("text", (String)part);
            }
        }
        blockEnd();
        
    }
}
