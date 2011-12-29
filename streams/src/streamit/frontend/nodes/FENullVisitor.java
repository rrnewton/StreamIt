/*
 * Copyright 2003 by the Massachusetts Institute of Technology.
 *
 * Permission to use, copy, modify, and distribute this
 * software and its documentation for any purpose and without
 * fee is hereby granted, provided that the above copyright
 * notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting
 * documentation, and that the name of M.I.T. not be used in
 * advertising or publicity pertaining to distribution of the
 * software without specific, written prior permission.
 * M.I.T. makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is"
 * without express or implied warranty.
 */

package streamit.frontend.nodes;

/**
 * Implementation of FEVisitor that always returns <code>null</code>.
 * This is intended to be a base class for other visitors that only
 * visit a subset of the node tree, and don't want to return objects
 * of the same type as the parameter.  {@link
 * streamit.frontend.nodes.FEReplacer} is a better default for
 * transformations on the tree.
 *
 * @author  David Maze &lt;dmaze@cag.lcs.mit.edu&gt;
 * @version $Id: FENullVisitor.java,v 1.14 2006-08-23 23:01:08 thies Exp $
 */
public class FENullVisitor implements FEVisitor
{
    @Override
	public Object visitExprArray(ExprArray exp) { return null; }
    @Override
	public Object visitExprArrayInit(ExprArrayInit exp) { return null; }
    @Override
	public Object visitExprBinary(ExprBinary exp) { return null; }
    @Override
	public Object visitExprComplex(ExprComplex exp) { return null; }
    @Override
	public Object visitExprComposite(ExprComposite exp) { return null; }
    @Override
	public Object visitExprConstBoolean(ExprConstBoolean exp) { return null; }
    @Override
	public Object visitExprConstChar(ExprConstChar exp) { return null; }
    @Override
	public Object visitExprConstFloat(ExprConstFloat exp) { return null; }
    @Override
	public Object visitExprConstInt(ExprConstInt exp) { return null; }
    @Override
	public Object visitExprConstStr(ExprConstStr exp) { return null; }
    @Override
	public Object visitExprDynamicToken(ExprDynamicToken exp) { return null; }
    @Override
	public Object visitExprField(ExprField exp) { return null; }
    @Override
	public Object visitExprFunCall(ExprFunCall exp) { return null; }
    @Override
	public Object visitExprHelperCall(ExprHelperCall exp) { return null; }
	@Override
	public Object visitExprIter(ExprIter exprIter) { return null; }
    @Override
	public Object visitExprPeek(ExprPeek exp) { return null; }
    @Override
	public Object visitExprPop(ExprPop exp) { return null; }
    @Override
	public Object visitExprRange(ExprRange exp) { return null; }
    @Override
	public Object visitExprTernary(ExprTernary exp) { return null; }
    @Override
	public Object visitExprTypeCast(ExprTypeCast exp) { return null; }
    @Override
	public Object visitExprUnary(ExprUnary exp) { return null; }
    @Override
	public Object visitExprVar(ExprVar exp) { return null; }
    @Override
	public Object visitFieldDecl(FieldDecl field) { return null; }
    @Override
	public Object visitFunction(Function func) { return null; }
    @Override
	public Object visitFuncWork(FuncWork func) { return null; }
    @Override
	public Object visitProgram(Program prog) { return null; }
    @Override
	public Object visitSCAnon(SCAnon creator) { return null; }
    @Override
	public Object visitSCSimple(SCSimple creator) { return null; }
    @Override
	public Object visitSJDuplicate(SJDuplicate sj) { return null; }
    @Override
	public Object visitSJRoundRobin(SJRoundRobin sj) { return null; }
    @Override
	public Object visitSJWeightedRR(SJWeightedRR sj) { return null; }
    @Override
	public Object visitStmtAdd(StmtAdd stmt) { return null; }
    @Override
	public Object visitStmtAssign(StmtAssign stmt) { return null; }
    @Override
	public Object visitStmtBlock(StmtBlock stmt) { return null; }
    @Override
	public Object visitStmtBody(StmtBody stmt) { return null; }
    @Override
	public Object visitStmtBreak(StmtBreak stmt) { return null; }
    @Override
	public Object visitStmtContinue(StmtContinue stmt) { return null; }
    @Override
	public Object visitStmtDoWhile(StmtDoWhile stmt) { return null; }
    @Override
	public Object visitStmtEmpty(StmtEmpty stmt) { return null; }
    @Override
	public Object visitStmtEnqueue(StmtEnqueue stmt) { return null; }
    @Override
	public Object visitStmtExpr(StmtExpr stmt) { return null; }
    @Override
	public Object visitStmtFor(StmtFor stmt) { return null; }
    @Override
	public Object visitStmtIfThen(StmtIfThen stmt) { return null; }
    @Override
	public Object visitStmtJoin(StmtJoin stmt) { return null; }
    @Override
	public Object visitStmtLoop(StmtLoop stmt) { return null; }
    @Override
	public Object visitStmtPush(StmtPush stmt) { return null; }
    @Override
	public Object visitStmtReturn(StmtReturn stmt) { return null; }
    @Override
	public Object visitStmtSendMessage(StmtSendMessage stmt) { return null; }
    @Override
	public Object visitStmtHelperCall(StmtHelperCall stmt) { return null; }
    @Override
	public Object visitStmtSplit(StmtSplit stmt) { return null; }
    @Override
	public Object visitStmtVarDecl(StmtVarDecl stmt) { return null; }
    @Override
	public Object visitStmtWhile(StmtWhile stmt) { return null; }
    @Override
	public Object visitStreamSpec(StreamSpec spec) { return null; }
    @Override
	public Object visitStreamType(StreamType type) { return null; }
    @Override
	public Object visitOther(FENode node) { return null; }
}
