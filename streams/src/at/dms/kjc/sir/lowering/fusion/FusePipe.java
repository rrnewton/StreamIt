package at.dms.kjc.sir.lowering.fusion;

import streamit.scheduler.*;
import streamit.scheduler.simple.*;

import at.dms.util.IRPrinter;
import at.dms.util.Utils;
import at.dms.kjc.*;
import at.dms.kjc.sir.*;
import at.dms.kjc.sir.lowering.*;
import at.dms.kjc.lir.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

public class FusePipe {

    /**
     * This is the name of the field that is used to hold the items
     * that are peeked.
     */
    private static final String PEEK_BUFFER_NAME = "___PEEK_BUFFER";

    /**
     * This is the name of the field that is used to hold the items
     * that are popped / looked at.
     */
    private static final String POP_BUFFER_NAME = "___POP_BUFFER";

    /**
     * The name of the counter that is used to write into buffers.
     */
    private static final String PUSH_INDEX_NAME = "___PUSH_INDEX";

    /**
     * The name of the counter that is used to read from buffers.
     */
    private static final String POP_INDEX_NAME = "___POP_INDEX";

    /**
     * The name of the counter that is used to count executions of a phase.
     */
    private static final String COUNTER_NAME = "___COUNTER";

    /**
     * The name of the initial work function.
     */
    public static final String INIT_WORK_NAME = "___initWork";

    /**
     * Prefix for name of parameters in fused init function.
     */
    protected static final String INIT_PARAM_NAME = "___param";

    /**
     * Fuses all eligibles portions of <pipe>, returning the number of
     * filters eliminated.
     */
    public static int fuse(SIRPipeline pipe) {
	return doFuse(pipe, pipe.size(), pipe.size());
    }

    /**
     * Fuses all candidate portions of <pipe>, given that the caller
     * would prefer not to eliminate more than <targetElim> filters
     * from <pipe>.  The fusion does its best to respect this
     * constraint (in a greedy way for now), but it will error on the
     * side of eliminating MORE than <targetElim> instead of
     * eliminating fewer.
     */
    public static int fuse(SIRPipeline pipe, int targetElim) {
	// get num that are fusable
	int numElim = getNumElim(pipe);
	int maxLength;
	if (targetElim >= numElim) {
	    maxLength = pipe.size();
	} else {
	    maxLength = (int)Math.ceil(((float)numElim)/((float)(numElim-targetElim)));
	    //System.err.println("numElim = " + numElim + " targetElim=" + targetElim + " maxLength=" + maxLength);
	}
	return doFuse(pipe, maxLength, targetElim);
    }

    /**
     * Fuses all candidate portions of <pipe>, but only fusing in
     * segments of length <maxLength>, eliminating a maximum of
     * <maxElim>.  Candidates for fusion are sequences of filters that
     * do not have special, compiler-defined work functions.  Return
     * how many filters were ELIMINATED from this pipeline.
     */
    private static int doFuse(SIRPipeline pipe, int maxLength, int maxElim) {
	int numEliminated = 0;
	int start = 0;
	do {
	    // find start of candidate stretch for fusion
	    while (start < pipe.size()-1 && !isFusable(pipe.get(start),pipe.getChildren())) {
		start++;
	    }
	    // find end of candidate stretch for fusion
	    int end = start;
	    while ((end+1) < pipe.size() && isFusable(pipe.get(end+1),pipe.getChildren())
		   && (end-start+1<maxLength)) {
		end++;
	    }
	    // if we found anything to fuse
	    if (end > start) {
		fuse((SIRFilter)pipe.get(start),
		     (SIRFilter)pipe.get(end));
		numEliminated += end-start;
		start = start + 1;
		System.err.println("Fusing " + (end-start+1) + " Pipeline filters!");
	    } else {
		start = end + 1;
	    }
	} while (start < pipe.size()-1 && numEliminated<maxElim);
	// if pipe is down to a single filter and we're not at the
	// toplevel already, then eliminate the pipeline
	if (pipe.size()==1 && 
	    pipe.get(0) instanceof SIRFilter &&
	    pipe.getParent()!=null) {
	    Lifter.eliminatePipe(pipe);
	}
	return numEliminated;
    }

    /**
     * Returns how many filters in this can be eliminated in a fusion
     * operation.
     */
    private static int getNumElim(SIRPipeline pipe) {
	int numEliminated = 0;
	int start = 0;
	do {
	    // find start of candidate stretch for fusion
	    while (start < pipe.size()-1 && !isFusable(pipe.get(start),pipe.getChildren())) {
		start++;
	    }
	    // find end of candidate stretch for fusion
	    int end = start;
	    while ((end+1) < pipe.size() && isFusable(pipe.get(end+1),pipe.getChildren())) {
		end++;
	    }
	    // if we found anything to fuse
	    if (end > start) {
		numEliminated += end-start;
	    }
	    start = end + 1;
	} while (start < pipe.size()-1);
	return numEliminated;
    }

    /**
     * Returns whether or note <str> is a candidate component for
     * fusion.  For now, <str> must be a filter with a work function
     * in order for us to fuse it.
     */
    private static boolean isFusable(SIRStream str,List pipelineElems) {
	// don't allow two-stage filters that peek
	if (str instanceof SIRTwoStageFilter) {
	    //System.err.println("Couldn't fuse " + str + " because it is 2-stage filter");
	    //Can fuse in this specific case
	    if((pipelineElems.get(0)==str)&&(((SIRTwoStageFilter)str).getInitPush()==0))
		return true;
	    return false;
	}
	if ((str instanceof SIRFilter) && ((SIRFilter)str).needsWork()) {
	    return true;
	} else {
	    //System.err.println("Couldn't fuse " + str + " because it isn't filter or doesn't need work");
	    return false;
	}
    }
    
    /**
     * Fuses filters <first> ... <last>.  For now, assumes: 
     *
     * 1. all of <first> ... <last> are consecutive filters in their
     *     parent, which must be an SIRPipeline
     *
     */
    public static SIRFilter fuse(SIRFilter first,
				 SIRFilter last) {
	SIRPipeline parent = (SIRPipeline)first.getParent();
	// make a list of the filters to be fused
	List filterList = parent.getChildrenBetween(first, last);
	// fuse the filters
	SIRFilter fused = fuse(filterList);
	// return the fused filter
	return fused;
    }

    /**
     * In <parent>, replace <filterList> with <fused>, and add
     * arguments <initArgs> to call to <fused>'s init function.
     */
    private static void replace(SIRPipeline parent, 
				List filterList, 
				SIRFilter fused,
				List initArgs) {
	// have to get the first and last list items this way since we
	// only know it's a list. 
	final SIRStream first = (SIRStream)filterList.get(0);
	SIRStream last = (SIRStream)filterList.get(filterList.size()-1);
	// replace <filterList> with <fused>
	parent.replace(first, last, fused);
	// add args to <fused>'s init
	parent.setParams(parent.indexOf(fused), initArgs);
    }
				
    /*
     * Returns a fused filter that has same behavior as all of
     * <filters>.
     */
    private static SIRFilter fuse(List filters) {
	// check that all the filters are fusable
	for (ListIterator it = filters.listIterator(); it.hasNext(); ) {
	    SIRStream str = (SIRStream)it.next();
	    if (!isFusable(str,filters)) {
		Utils.fail("Trying to fuse a filter that is unfusable: " + 
			   str + " " + str.getName());
	    }
	}
	
	// rename the components of the filters
	RenameAll renamer = new RenameAll();
	for (ListIterator it=filters.listIterator(); it.hasNext(); ) {
	    renamer.renameFilterContents((SIRFilter)it.next());
	}
	
	// construct set of filter info
	List filterInfo = makeFilterInfo(filters);

	SIRFilter result;

	InitFuser initFuser;

	if(filters.get(0) instanceof SIRTwoStageFilter) {
	    SIRTwoStageFilter twostage=(SIRTwoStageFilter)filters.get(0);
	    // make a statement list for the init function
	    //JBlock statements = new JBlock(null, new JStatement[0], null);
	    // add the variable declarations
	    //makeWorkDecls(filterInfo,statements,true);
	    //makeWorkDecls(filterInfo,statements,false);
	    JMethodDeclaration initWork=twostage.getInitWork();
	    //Adding Decls
	    //for(int i=statements.size()-1;i>=0;i--)
	    //initWork.addStatementFirst(statements.getStatement(i));
	    if(makeWork(filterInfo, true)!=null)
		Utils.fail("WARNING: InitWork Already Needed when fusing SIRTwoStageFilter");

	    // make the steady-state work function
	    JMethodDeclaration steadyWork =  makeWork(filterInfo, false);
	    
	    // make the fused init functions
	    initFuser = makeInitFunction(filterInfo);
	    
	    JMethodDeclaration init=initFuser.getInitFunction();

	    FilterInfo first = (FilterInfo)filterInfo.get(0);
	    FilterInfo last = (FilterInfo)filterInfo.get(filterInfo.size()-1);

	    int steadyPop = first.steady.num * first.filter.getPopInt();
	    int steadyPeek = 
		(first.filter.getPeekInt() - first.filter.getPopInt()) + steadyPop;
	    int steadyPush = last.steady.num * last.filter.getPushInt();
	    
	    // fuse all other fields and methods
	    result = new SIRTwoStageFilter(first.filter.getParent(),
					   getFusedName(filterInfo),
					   getFields(filterInfo),
					   getMethods(filterInfo, 
						      init, 
						      initWork, 
						      steadyWork),
					   new JIntLiteral(steadyPeek), 
					   new JIntLiteral(steadyPop),
					   new JIntLiteral(steadyPush),
					   steadyWork,
					   twostage.getInitPeek(),
					   twostage.getInitPop(),
					   twostage.getInitPush(),
					   initWork,
					   Utils.voidToInt(first.filter.
						     getInputType()),
					   Utils.voidToInt(last.filter.
						     getOutputType()));
	    result.setInit(init);
	} else {
	    // make the initial work function
	    JMethodDeclaration initWork =  makeWork(filterInfo, true);
	    
	    // make the steady-state work function
	    JMethodDeclaration steadyWork =  makeWork(filterInfo, false);
	    
	    // make the fused init functions
	    initFuser = makeInitFunction(filterInfo);
	    
	    // fuse all other fields and methods
	    result = makeFused(filterInfo, initFuser.getInitFunction(), initWork, steadyWork);
	}
	
	// insert the fused filter in the parent
	replace((SIRPipeline)((SIRFilter)filters.get(0)).getParent(), 
		filters, result, initFuser.getInitArgs());
	
	// return result
	return result;
    }

    /**
     * Tabulates info on <filterList> that is needed for fusion.
     */
    private static List makeFilterInfo(List filterList) {
	// make the result
	List result = new LinkedList();
	// construct a schedule for <filterList>
	Schedule schedule = getSchedule(filterList);
	// get the schedules
	List initSched = schedule.getInitSchedule();
	List steadySched = schedule.getSteadySchedule();

	// DEBUGGING OUTPUT
	//SIRScheduler.printSchedule(initSched, "initialization");
	//SIRScheduler.printSchedule(steadySched, "steady state");

	// for each filter...
	ListIterator it = filterList.listIterator();
	for (int i=0; it.hasNext(); i++) {
	    // the filter
	    SIRFilter filter = (SIRFilter)it.next();

	    // the peek buffer
	    JVariableDefinition peekBufferVar = 
		new JVariableDefinition(null,
					at.dms.kjc.Constants.ACC_FINAL,
					new CArrayType(Utils.voidToInt(filter.
						       getInputType()), 
						       1 /* dimension */ ),
					PEEK_BUFFER_NAME + "_" + i,
					null);
	    JFieldDeclaration peekBuffer = new JFieldDeclaration(null,
								 peekBufferVar,
								 null,
								 null);
	    
	    // number of executions (num[0] is init, num[1] is steady)
	    int[] num = new int[2];

	    // for now, guard against empty/incomplete schedules
	    // by assuming a count of zero.  Talk to Michal about
	    // have entries of zero-weight in schedule?  FIXME.
	    if (initSched.size()>i) {
		num[0] = ((SchedRepSchedule)initSched.get(i)).
		    getTotalExecutions().intValue();
	    } else {
		num[0] = 0;
	    }
	    // do the same for the steady schedule
	    if (steadySched.size()>i) {
		num[1] = ((SchedRepSchedule)steadySched.get(i)).
		    getTotalExecutions().intValue();
	    } else {
		num[1] = 0;
	    }
	    
	    // calculate how much data we should buffer between the
	    // i'th and (i-1)'th filter.  This part of the code is
	    // ready for two stage filters even though they might not
	    // be passed here yet.
	    int peekBufferSize;
	    if (i==0) {
		// for the first filter, don't need any peek buffer
		peekBufferSize = 0;
	    } else {
		// otherwise, need however many were left over during
		// initialization between this filter and the last
		FilterInfo last = (FilterInfo)result.get(result.size()-1);
		int lastProduce = 0;
		// need to count first execution of a two-stage filter separately
		if (last.filter instanceof SIRTwoStageFilter &&
		    last.init.num > 0) {
		    lastProduce = ((SIRTwoStageFilter)last.filter).getInitPush() + 
			(last.init.num-1) * last.filter.getPushInt();
		} else {
		    lastProduce = last.init.num * last.filter.getPushInt();
		}
		int myConsume = 0;
		if (filter instanceof SIRTwoStageFilter &&
		    num[0] > 0) {
		    myConsume = ((SIRTwoStageFilter)filter).getInitPop() + 
			(num[0]-1) * filter.getPopInt();
		} else {
		    myConsume = num[0] * filter.getPopInt();
		}
		// the peek buffer is the difference between what the
		// previous one produces and this one consumes
		peekBufferSize = lastProduce - myConsume;
	    }

	    // get ready to make rest of phase-specific info
	    JVariableDefinition popBuffer[] = new JVariableDefinition[2];
	    JVariableDefinition popCounter[] = new JVariableDefinition[2];
	    JVariableDefinition pushCounter[] = new JVariableDefinition[2];
	    JVariableDefinition loopCounter[] = new JVariableDefinition[2];
	    
	    for (int j=0; j<2; j++) {
		// the pop buffer
		popBuffer[j] = makePopBuffer(filter, peekBufferSize, num[j], i);

		// the pop counter.
		popCounter[j] = 
		    new JVariableDefinition(null, 0, CStdType.Integer,
					    POP_INDEX_NAME + "_" + j + "_" + i,
					    new
					    JIntLiteral(- 1 /* this is since we're starting
							       at -1 and doing pre-inc 
							       instead of post-inc */ ));
	    
		// the push counter.  In the steady state, the initial
		// value of the push counter is the first slot after
		// the peek values are restored, which is peekBufferSize-1
		// (-1 since we're pre-incing, not post-incing).  In
		// the inital work function, the push counter starts
		// at -1 (since we're pre-incing, not post-incing).
		int pushInit = 
		    j==0 ? -1 : peekBufferSize - 1;
		pushCounter[j] = 
		    new JVariableDefinition(null, 0, CStdType.Integer,
					    PUSH_INDEX_NAME + "_" + j + "_" +i,
					    new JIntLiteral(pushInit));

		// the exec counter
		loopCounter[j] = 
		    new JVariableDefinition(null, 0, CStdType.Integer,
					    COUNTER_NAME + "_" + j + "_" +i,
					    null);
	    }

	    // add a filter info to <result>
	    result.add(new 
		       FilterInfo(filter, peekBuffer, peekBufferSize,
				  new PhaseInfo(num[0], 
						popBuffer[0], 
						popCounter[0], 
						pushCounter[0],
						loopCounter[0]),
				  new PhaseInfo(num[1], 
						popBuffer[1],
						popCounter[1], 
						pushCounter[1],
						loopCounter[1])
				  ));
	}
	// return result
	return result;
    }
	
    /**
     * Interfaces with the scheduler to return a schedule for
     * <filterList> in <parent>.
     */
    private static Schedule getSchedule(List filterList) {
	// make a scheduler
	Scheduler scheduler = new SimpleHierarchicalScheduler();
	// make a dummy parent object as a hook for the scheduler
	Object parent = new Object();
	// ask the scheduler to schedule the list
	SchedPipeline sp = scheduler.newSchedPipeline(parent);
	// add the filters to the parent
	for (ListIterator it = filterList.listIterator(); it.hasNext(); ) {
	    SIRFilter filter = (SIRFilter)it.next();
	    sp.addChild(scheduler.newSchedFilter(filter, 
						 filter.getPushInt(),
						 filter.getPopInt(),
						 filter.getPeekInt()));
	}
	// tell the scheduler we're interested in <parent>
	scheduler.useStream(sp);
	// return the schedule
	return scheduler.computeSchedule();
    }

    /**
     * Returns a JVariableDefinition for a pop buffer for <filter>
     * that executes <num> times in its schedule and appears in the
     * <pos>'th position of its pipeline.
     */
    private static JVariableDefinition makePopBuffer(SIRFilter filter, 
						     int peekBufferSize,
						     int num,
						     int pos) {
	// get the number of items looked at in an execution round
	int lookedAt = num * filter.getPopInt() + peekBufferSize;
	// make an initializer to make a buffer of extent <lookedAt>
	JExpression[] dims = { new JIntLiteral(null, lookedAt) };
	JExpression initializer = 
	    new JNewArrayExpression(null,
				    Utils.voidToInt(filter.getInputType()),
				    dims,
				    null);
	// make a buffer for all the items looked at in a round
	return new JVariableDefinition(null,
				       at.dms.kjc.Constants.ACC_FINAL,
				       new CArrayType(Utils.voidToInt(filter.
						      getInputType()), 
						      1 /* dimension */ ),
				       POP_BUFFER_NAME + "_" + pos,
				       initializer);
    }

    /**
     * Builds the initial work function for <filterList>, where <init>
     * indicates whether or not we're doing the initialization work
     * function.  If in init mode and there are no statements in the
     * work function, then it returns null instead (to indicate that
     * initWork is not needed.)
     */
    private static JMethodDeclaration makeWork(List filterInfo, boolean init) {
	// make a statement list for the init function
	JBlock statements = new JBlock(null, new JStatement[0], null);

	// add the variable declarations
	makeWorkDecls(filterInfo, statements, init);

	// add the work statements
	int before = statements.size();
	makeWorkBody(filterInfo, statements, init);
	int after = statements.size();

	if (after-before==0 && init) {
	    // return null to indicate empty initWork function
	    return null;
	} else {
	    // return result
	    return new JMethodDeclaration(null,
					  at.dms.kjc.Constants.ACC_PUBLIC,
					  CStdType.Void,
					  init ? INIT_WORK_NAME : "work",
					  JFormalParameter.EMPTY,
				      CClassType.EMPTY,
					  statements,
					  null,
					  null);
	}
    }

    /**
     * Adds local variable declarations to <statements> that are
     * needed by <filterInfo>.  If <init> is true, it does it for init
     * phase; otherwise for steady phase.
     */
    private static void makeWorkDecls(List filterInfo,
				      JBlock statements,
				      boolean init) {
	// add declarations for each filter
	for (ListIterator it = filterInfo.listIterator(); it.hasNext(); ) {
	    FilterInfo info = (FilterInfo)it.next();
	    // get list of local variable definitions from <filterInfo>
	    List locals = 
		init ? info.init.getVariables() : info.steady.getVariables();
	    // go through locals, adding variable declaration
	    for (ListIterator loc = locals.listIterator(); loc.hasNext(); ) {
		// get local
		JVariableDefinition local = 
		    (JVariableDefinition)loc.next();
		// add variable declaration for local
		statements.
		    addStatement(new JVariableDeclarationStatement(null, 
								   local, 
								   null));
	    }
	}
    }

    /**
     * Adds the body of the initial work function.  <init> indicates
     * whether or not this is the initial run of the work function
     * instead of the steady-state version.
     */
    private static void makeWorkBody(List filterInfo, 
				     JBlock statements,
				     boolean init) {
	// for all the filters...
	for (int i=0; i<filterInfo.size(); i++) {
	    FilterInfo cur = (FilterInfo)filterInfo.get(i);
	    PhaseInfo curPhase = init ? cur.init : cur.steady;
	    // we'll only need the "next" fields if we're not at the
	    // end of a pipe.
	    FilterInfo next = null;
	    PhaseInfo nextPhase = null;
	    // get the next fields
	    if (i<filterInfo.size()-1) {
		next = (FilterInfo)filterInfo.get(i+1);
		nextPhase = init ? next.init : next.steady;
	    }

	    // if the current filter doesn't execute at all, continue
	    // (FIXME this is part of some kind of special case for 
	    // filters that don't execute at all in a schedule, I think.)
	    if (curPhase.num!=0) {

		// if in the steady-state phase, restore the peek values
		if (!init) {
		    statements.addStatement(makePeekRestore(cur, curPhase));
		}
		// get the filter's work function
		JMethodDeclaration work = cur.filter.getWork();
		// take a deep breath and clone the body of the work function
		JBlock oldBody = new JBlock(null, work.getStatements(), null);
		JBlock body = (JBlock)ObjectDeepCloner.deepCopy(oldBody);
		// move variable declarations from front of <body> to
		// front of <statements>
		moveVarDecls(body, statements);
		// mutate <statements> to make them fit for fusion
		FusingVisitor fuser = 
		    new FusingVisitor(curPhase, nextPhase, i!=0,
				      i!=filterInfo.size()-1);
		for (ListIterator it = body.getStatementIterator(); 
		     it.hasNext() ; ) {
		    ((JStatement)it.next()).accept(fuser);
		}
		// get <body> into a loop in <statements>
		statements.addStatement(makeForLoop(body,
						    curPhase.loopCounter,
						    new 
						    JIntLiteral(curPhase.num))
					);
	    }
	    // if there's any peek buffer, store items to it
	    if (cur.peekBufferSize>0) {
		statements.addStatement(makePeekBackup(cur, curPhase));
	    }
	}
    }

    /**
     * Moves all variable declaration statements at front of <source>
     * to front of <dest>.
     */
    private static void moveVarDecls(JBlock source, JBlock dest) {
	JStatement decl;
	int index = 0;
	while (true) {
	    // get statement at <index> of source
	    decl = source.getStatement(index);
	    // if it's a var decl...
	    if (decl instanceof JVariableDeclarationStatement) {
		// remove the initializers from <decl> ...
		JVariableDefinition[] vars = ((JVariableDeclarationStatement)
					      decl).getVars();
		JExpressionListStatement assigns = stripInitializers(vars);
		// add to front of dest
		dest.addStatementFirst(decl);
		// remove from source
		source.removeStatement(index);
		// add assignment to source
		source.addStatement(index, assigns);
		index++;
	    } else {
		// quit looping when we run out of decl's
		break;
	    }
	}
    }

    /**
     * Strips all initializers out of <vars> and returns a statement that
     * assigns the initial value to each variable in <vars>.
     */
    private static JExpressionListStatement 
	stripInitializers(JVariableDefinition[] vars) {
	// make list to hold assignments
	LinkedList assign = new LinkedList();
	// go through vars 
	for (int i=0; i<vars.length; i++) {
	    // see if there's an initializer
	    if (vars[i].hasInitializer()) {
		// if so, clear it...
		JExpression init = vars[i].getValue();
		vars[i].setValue(null);
		// and make assignment
		JLocalVariableExpression lhs =
		    new JLocalVariableExpression(null, vars[i]);
		assign.add(new JAssignmentExpression(null,
						     lhs,
						     init));
	    }
	}
	return new JExpressionListStatement(null,
					    (JExpression[])
					    assign.toArray(new JExpression[0]),
					    null);
    }

    /**
     * Given that a phase is about to be executed, restores the peek
     * information to the front of the pop buffer.
     */
    private static JStatement makePeekRestore(FilterInfo filterInfo,
					      PhaseInfo phaseInfo) {
	// make a statement that will copy peeked items into the pop
	// buffer, assuming the counter will count from 0 to peekBufferSize

	// the lhs of the source of the assignment
	JExpression sourceLhs = 
	    new JFieldAccessExpression(null,
				       new JThisExpression(null),
				       filterInfo.peekBuffer.
				       getVariable().getIdent());

	// the rhs of the source of the assignment
	JExpression sourceRhs = 
	    new JLocalVariableExpression(null, 
					 phaseInfo.loopCounter);

	// the lhs of the dest of the assignment
	JExpression destLhs = 
	    new JLocalVariableExpression(null,
					 phaseInfo.popBuffer);
	    
	// the rhs of the dest of the assignment
	JExpression destRhs = 
	    new JLocalVariableExpression(null,
					 phaseInfo.loopCounter);

	// the expression that copies items from the pop buffer to the
	// peek buffer
	JExpression copyExp = 
	    new JAssignmentExpression(null,
				      new JArrayAccessExpression(null,
								 destLhs,
								 destRhs),
				      new JArrayAccessExpression(null,
								 sourceLhs,
								 sourceRhs));

	// finally we have the body of the loop
	JStatement body = new JExpressionStatement(null, copyExp, null);

	// return a for loop that executes (peek-pop) times.
	return makeForLoop(body,
			   phaseInfo.loopCounter, 
			   new JIntLiteral(filterInfo.peekBufferSize));
    }

    /**
     * Given that a phase has already executed, backs up the state of
     * unpopped items into the peek buffer.
     */
    private static JStatement makePeekBackup(FilterInfo filterInfo,
					     PhaseInfo phaseInfo) {
	// make a statement that will copy unpopped items into the
	// peek buffer, assuming the counter will count from 0 to peekBufferSize

	// the lhs of the destination of the assignment
	JExpression destLhs = 
	    new JFieldAccessExpression(null,
				       new JThisExpression(null),
				       filterInfo.peekBuffer.
				       getVariable().getIdent());

	// the rhs of the destination of the assignment
	JExpression destRhs = 
	    new JLocalVariableExpression(null, 
					 phaseInfo.loopCounter);

	// the lhs of the source of the assignment
	JExpression sourceLhs = 
	    new JLocalVariableExpression(null,
					 phaseInfo.popBuffer);
	    
	// the rhs of the source of the assignment... (add one to the
	// push index because of our pre-inc convention.)
	JExpression sourceRhs = 
	    new
	    JAddExpression(null, 
			   new JLocalVariableExpression(null, 
							phaseInfo.loopCounter),
			   new JAddExpression(null, new JIntLiteral(1),
					      new JLocalVariableExpression(null,
									   phaseInfo.popCounter)));
					      /*
	// need to subtract the difference in peek and pop counts to
	// see what we have to backup
	JExpression sourceRhs =
	    new JMinusExpression(null,
				 sourceRhs1,
				 new JIntLiteral(filterInfo.peekBufferSize));
					      */

	// the expression that copies items from the pop buffer to the
	// peek buffer
	JExpression copyExp = 
	    new JAssignmentExpression(null,
				      new JArrayAccessExpression(null,
								 destLhs,
								 destRhs),
				      new JArrayAccessExpression(null,
								 sourceLhs,
								 sourceRhs));

	// finally we have the body of the loop
	JStatement body = new JExpressionStatement(null, copyExp, null);

	// return a for loop that executes (peek-pop) times.
	return makeForLoop(body,
			   phaseInfo.loopCounter, 
			   new JIntLiteral(filterInfo.peekBufferSize));
    }

    /**
     * Returns a for loop that uses local variable <var> to count
     * <count> times with the body of the loop being <body>.  If count
     * is non-positive, just returns the initial assignment statement.
     */
    private static JStatement makeForLoop(JStatement body,
					  JLocalVariable var,
					  JExpression count) {
	// make init statement - assign zero to <var>.  We need to use
	// an expression list statement to follow the convention of
	// other for loops and to get the codegen right.
	JExpression initExpr[] = {
	    new JAssignmentExpression(null,
				      new JLocalVariableExpression(null, var),
				      new JIntLiteral(0)) };
	JStatement init = new JExpressionListStatement(null, initExpr, null);
	// if count==0, just return init statement
	if (count instanceof JIntLiteral) {
	    int intCount = ((JIntLiteral)count).intValue();
	    if (intCount<=0) {
		// return assignment statement
		return init;
	    }
	}
	// make conditional - test if <var> less than <count>
	JExpression cond = 
	    new JRelationalExpression(null,
				      Constants.OPE_LT,
				      new JLocalVariableExpression(null, var),
				      count);
	JExpression incrExpr = 
	    new JPostfixExpression(null, 
				   Constants.OPE_POSTINC, 
				   new JLocalVariableExpression(null, var));
	JStatement incr = 
	    new JExpressionStatement(null, incrExpr, null);

	return new JForStatement(null, init, cond, incr, body, null);
    }

    /**
     * Returns an init function that is the combinatio of those in
     * <filterInfo> and includes a call to <initWork>.  Also patches
     * the parent's init function to call the new one, given that
     * <result> will be the resulting fused filter.
     */
    private static 
	InitFuser makeInitFunction(List filterInfo) {
	// make an init function builder out of <filterList>
	InitFuser initFuser = new InitFuser(filterInfo);
	
	// do the work on the parent
	initFuser.doit((SIRPipeline)((FilterInfo)filterInfo.get(0)).filter.getParent());

	// make the finished initfuser
	return initFuser;
    }

    /**
     * Returns an array of the fields that should appear in filter
     * fusing all in <filterInfo>.
     */
    private static JFieldDeclaration[] getFields(List filterInfo) {
	// make result
	List result = new LinkedList();
	// add the peek buffer's and the list of fields from each filter
	for (ListIterator it = filterInfo.listIterator(); it.hasNext(); ) {
	    FilterInfo info = (FilterInfo)it.next();
	    result.add(info.peekBuffer);
	    result.addAll(Arrays.asList(info.filter.getFields()));
	}
	// return result
	return (JFieldDeclaration[])result.toArray(new JFieldDeclaration[0]);
    }

    /**
     * Returns an array of the methods fields that should appear in
     * filter fusing all in <filterInfo>, with extra <init>, <initWork>, 
     * and <steadyWork> appearing in the fused filter.
     */
    private static 
	JMethodDeclaration[] getMethods(List filterInfo,
					JMethodDeclaration init,
					JMethodDeclaration initWork,
					JMethodDeclaration steadyWork) {
	// make result
	List result = new LinkedList();
	// start with the methods that we were passed
	result.add(init);
	if (initWork!=null) {
	    result.add(initWork);
	}
	result.add(steadyWork);
	// add methods from each filter that aren't work methods
	for (ListIterator it = filterInfo.listIterator(); it.hasNext(); ) {
	    FilterInfo info = (FilterInfo)it.next();
	    SIRFilter filter=info.filter;
	    List methods = Arrays.asList(filter.getMethods());
	    for (ListIterator meth = methods.listIterator(); meth.hasNext(); ){
		JMethodDeclaration m = (JMethodDeclaration)meth.next();
		// add methods that aren't work (or initwork)
		if (m!=info.filter.getWork()) {
		    if(filter instanceof SIRTwoStageFilter) {
			if(m!=((SIRTwoStageFilter)filter).getInitWork())
			    result.add(m);
		    }
		    else
			result.add(m);
		}
	    }
	}
	// return result
	return (JMethodDeclaration[])result.toArray(new JMethodDeclaration[0]);
    }

    /**
     * Return a name for the fused filter that consists of those
     * filters in <filterInfo>.  <filterInfo> must be a list of either
     * SIRFilter's or FilterInfo's.
     */
    public static String getFusedName(List filterInfo) {
	StringBuffer name = new StringBuffer("Fused");
	for (ListIterator it = filterInfo.listIterator(); it.hasNext(); ) {
	    name.append("_");
	    Object o = it.next();
	    String childName = null;
	    if (o instanceof SIRFilter) {
		childName = ((SIRFilter)o).getIdent();
	    } else if (o instanceof FilterInfo) {
		childName = ((FilterInfo)o).filter.getIdent();
	    } else {
		throw new RuntimeException("Unexpected type: " + o.getClass());
	    }
	    if (childName.toLowerCase().startsWith("fused_")) {
		childName = childName.substring(6, childName.length());
	    }
	    name.append(childName.substring(0, Math.min(childName.length(), 3)));
	}
	return name.toString();
    }

    /**
     * Returns the final, fused filter.
     */
    private static SIRFilter makeFused(List filterInfo, 
				       JMethodDeclaration init, 
				       JMethodDeclaration initWork, 
				       JMethodDeclaration steadyWork) {
	// get the first and last filters' info
	FilterInfo first = (FilterInfo)filterInfo.get(0);
	FilterInfo last = (FilterInfo)filterInfo.get(filterInfo.size()-1);

	// calculate the peek, pop, and push count for the fused
	// filter in the STEADY state
	int steadyPop = first.steady.num * first.filter.getPopInt();
	int steadyPeek = 
	    (first.filter.getPeekInt() - first.filter.getPopInt()) + steadyPop;
	int steadyPush = last.steady.num * last.filter.getPushInt();

	SIRFilter result;
	// if initWork is null, then we can get away with a filter for
	// the fused result; otherwise we need a two-stage filter
	if (initWork==null) {
	    result = new SIRFilter(first.filter.getParent(),
				   getFusedName(filterInfo),
				   getFields(filterInfo),
				   getMethods(filterInfo, 
					      init, 
					      initWork, 
					      steadyWork),
				   new JIntLiteral(steadyPeek), 
				   new JIntLiteral(steadyPop),
				   new JIntLiteral(steadyPush),
				   steadyWork,
				   Utils.voidToInt(first.filter.
					     getInputType()),
				   Utils.voidToInt(last.filter.
					     getOutputType()));
	} else {
	    // calculate the peek, pop, and push count for the fused
	    // filter in the INITIAL state
	    int initPop = first.init.num * first.filter.getPopInt();
	    int initPeek =
		(first.filter.getPeekInt() - first.filter.getPopInt()) + initPop;
	    int initPush = last.init.num * last.filter.getPushInt();
	    
	    // make a new filter to represent the fused combo
	    result = new SIRTwoStageFilter(first.filter.getParent(),
					   getFusedName(filterInfo),
					   getFields(filterInfo),
					   getMethods(filterInfo, 
						      init, 
						      initWork, 
						      steadyWork),
					   new JIntLiteral(steadyPeek), 
					   new JIntLiteral(steadyPop),
					   new JIntLiteral(steadyPush),
					   steadyWork,
					   initPeek,
					   initPop,
					   initPush,
					   initWork,
					   Utils.voidToInt(first.filter.
						     getInputType()),
					   Utils.voidToInt(last.filter.
						     getOutputType()));
	}
	
	// set init function of fused filter
	result.setInit(init);
	return result;
    }
}
    
/**
 * Contains information that is relevant to a given filter's
 * inclusion in a fused pipeline.
 */
class FilterInfo {
    /**
     * The filter itself.
     */
    public final SIRFilter filter;

    /**
     * The persistent buffer for holding peeked items
     */
    public final JFieldDeclaration peekBuffer;

    /**
     * The size of the peek buffer
     */
    public final int peekBufferSize;

    /**
     * The info on the initial execution.
     */
    public final PhaseInfo init;
	
    /**
     * The info on the steady-state execution.
     */
    public final PhaseInfo steady;

    public FilterInfo(SIRFilter filter, JFieldDeclaration peekBuffer,
		      int peekBufferSize, PhaseInfo init, PhaseInfo steady) {
	this.filter = filter;
	this.peekBuffer = peekBuffer;
	this.peekBufferSize = peekBufferSize;
	this.init = init;
	this.steady = steady;
    }
}

class PhaseInfo {
    /**
     * The number of times this filter is executed in the parent.
     */ 
    public final int num;

    /**
     * The buffer for holding popped items.
     */
    public final JVariableDefinition popBuffer;

    /**
     * The counter for popped items.
     */
    public final JVariableDefinition popCounter;

    /*
     * The counter for pushed items (of the CURRENT phase)
     */
    public final JVariableDefinition pushCounter;

    /**
     * The counter for keeping track of executions of the whole block.
     */
    public final JVariableDefinition loopCounter;
    
    public PhaseInfo(int num, 
		     JVariableDefinition popBuffer,
		     JVariableDefinition popCounter,
		     JVariableDefinition pushCounter,
		     JVariableDefinition loopCounter) {
	this.num = num;
	this.popBuffer = popBuffer;
	this.popCounter = popCounter;
	this.pushCounter = pushCounter;
	this.loopCounter = loopCounter;
    }

    /**
     * Returns list of JVariableDefinitions of all var defs in here.
     */
    public List getVariables() {
	List result = new LinkedList();
	result.add(popBuffer);
	result.add(popCounter);
	result.add(pushCounter);
	result.add(loopCounter);
	return result;
    }
}

class FusingVisitor extends SLIRReplacingVisitor {
    /**
     * The info for the current filter.
     */
    private final PhaseInfo curInfo;

    /**
     * The info for the next filter in the pipeline.
     */
    private final PhaseInfo nextInfo;

    /**
     * Whether or not peek and pop expressions should be fused.
     */
    private final boolean fuseReads;

    /**
     * Whether or not push expressions should be fused.
     */
    private final boolean fuseWrites;

    public FusingVisitor(PhaseInfo curInfo, PhaseInfo nextInfo,
			 boolean fuseReads, boolean fuseWrites) {
	this.curInfo = curInfo;
	this.nextInfo = nextInfo;
	this.fuseReads = fuseReads;
	this.fuseWrites = fuseWrites;
    }

    public Object visitPopExpression(SIRPopExpression self,
				     CType tapeType) {
	// leave it alone not fusing reads
	if (!fuseReads) {
	    return super.visitPopExpression(self, tapeType);
	}

	// build ref to pop array
	JLocalVariableExpression lhs = 
	    new JLocalVariableExpression(null, curInfo.popBuffer);

	// build increment of index to array
	JExpression rhs =
	    new JPrefixExpression(null, 
				  Constants.OPE_PREINC, 
				  new JLocalVariableExpression(null,
							       curInfo.
							       popCounter));
	// return a new array access expression
	return new JArrayAccessExpression(null, lhs, rhs);
    }

    public Object visitPeekExpression(SIRPeekExpression oldSelf,
				      CType oldTapeType,
				      JExpression oldArg) {
	// leave it alone not fusing reads
	if (!fuseReads) {
	    return super.visitPeekExpression(oldSelf, oldTapeType, oldArg);
	}

	// do the super
	SIRPeekExpression self = 
	    (SIRPeekExpression)
	    super.visitPeekExpression(oldSelf, oldTapeType, oldArg);
	
	// build ref to pop array
	JLocalVariableExpression lhs = 
	    new JLocalVariableExpression(null, curInfo.popBuffer);

	// build subtraction of peek index from current pop index (add
	// one to the pop index because of our pre-inc convention)
	JExpression rhs =
	    new JAddExpression(null,
			      new JAddExpression(null,
						 new JIntLiteral(1),
						 new JLocalVariableExpression(null,
									      curInfo.
									      popCounter)),
			      self.getArg());

	// return a new array access expression
	return new JArrayAccessExpression(null, lhs, rhs);
    }

    public Object visitPushExpression(SIRPushExpression oldSelf,
				      CType oldTapeType,
				      JExpression oldArg) {
	// leave it alone not fusing writes
	if (!fuseWrites) {
	    return super.visitPushExpression(oldSelf, oldTapeType, oldArg);
	}

	// do the super
	SIRPushExpression self = 
	    (SIRPushExpression)
	    super.visitPushExpression(oldSelf, oldTapeType, oldArg);
	
	// build ref to push array
	JLocalVariableExpression lhs = 
	    new JLocalVariableExpression(null, nextInfo.popBuffer);

	// build increment of index to array
	JExpression rhs =
	    new JPrefixExpression(null,
				  Constants.OPE_PREINC, 
				  new JLocalVariableExpression(null,
							       nextInfo.
							       pushCounter));
	// return a new array assignment to the right spot
	return new JAssignmentExpression(
		  null,
		  new JArrayAccessExpression(null, lhs, rhs),
		  self.getArg());
    }
}

/**
 * This builds up the init function of the fused class by traversing
 * the init function of the parent.
 */
class InitFuser {
    /**
     * The info on the filters we're trying to fuse.
     */
    private final List filterInfo;

    /**
     * The block of the resulting fused init function.
     */
    private JBlock fusedBlock;
    
    /**
     * A list of the parameters of the fused block, all of type
     * JFormalParameter.
     */
    private List fusedParam;
    
    /**
     * A list of the arguments to the init function of the fused
     * block, all of type JExpression.
     */
    private List fusedArgs;

    /**
     * Cached copy of the method decl for the init function.
     */
    private JMethodDeclaration initFunction;

    /**
     * The number of filter's we've fused.
     */
    private int numFused;

    /**
     * <fusedFilter> represents what -will- be the result of the
     * fusion.  It has been allocated, but is not filled in with
     * correct values yet.
     */
    public InitFuser(List filterInfo) {
	this.filterInfo = filterInfo;
	this.fusedBlock = new JBlock(null, new JStatement[0], null);
	this.fusedParam = new LinkedList();
	this.fusedArgs = new LinkedList();
	this.numFused = 0;
    }

    public void doit(SIRPipeline parent) {
	for (ListIterator it = filterInfo.listIterator(); it.hasNext(); ) {
	    // process the arguments to a filter being fused
	    FilterInfo info = (FilterInfo)it.next();
	    int index = parent.indexOf(info.filter);
	    processArgs(info, parent.getParams(index));
	}
	makeInitFunction();
    }

    /**
     * Given that we found <args> in an init call to <info>,
     * incorporate this info into the init function of the fused
     * filter.
     */
    private void processArgs(FilterInfo info, List args) {
	// make parameters for <args>, and build <newArgs> to pass
	// to new init function call
	JExpression[] newArgs = new JExpression[args.size()];
	for (int i=0; i<args.size(); i++) {
	    JFormalParameter param = 
		new JFormalParameter(null,
				     0,
				     ((JExpression)args.get(i)).getType(),
				     FusePipe.INIT_PARAM_NAME + 
				     "_" + i + "_" + numFused,
				     false);
	    // add to list
	    fusedParam.add(param);
	    // make a new arg
	    newArgs[i] = new JLocalVariableExpression(null, param);
	    // increment fused count
	    numFused++;
	}

	// add the arguments to the list
	fusedArgs.addAll(args);

	// make a call to the init function of <info> with <params>
	fusedBlock.addStatement(new JExpressionStatement(
              null,
	      new JMethodCallExpression(null,
					new JThisExpression(null),
					info.filter.getInit().getName(),
					newArgs), null));
    }

    /**
     * Prepares the init function for the fused block once the
     * traversal of the parent's init function is complete.
     */
    private void makeInitFunction() {
	// add allocations for peek buffers
	for (ListIterator it = filterInfo.listIterator(); it.hasNext(); ) {
	    // get the next info
	    FilterInfo info = (FilterInfo)it.next();
	    // calculate dimensions of the buffer
	    JExpression[] dims = { new JIntLiteral(null, 
						   info.peekBufferSize) };
	    // add a statement initializeing the peek buffer
	    fusedBlock.addStatementFirst(new JExpressionStatement(null,
	      new JAssignmentExpression(
		  null,
		  new JFieldAccessExpression(null,
					     new JThisExpression(null),
					     info.peekBuffer.
					     getVariable().getIdent()),
		  new JNewArrayExpression(null,
					  Utils.voidToInt(info.filter.
					  getInputType()),
					  dims,
					  null)), null));
	}
	// now we can make the init function
	this.initFunction = new JMethodDeclaration(null,
				      at.dms.kjc.Constants.ACC_PUBLIC,
				      CStdType.Void,
				      "init",
				      (JFormalParameter[])
				      fusedParam.toArray(new 
							 JFormalParameter[0]),
				      CClassType.EMPTY,
				      fusedBlock,
				      null,
				      null);
    }
    
    /**
     * Returns fused init function of this.
     */
    public JMethodDeclaration getInitFunction() {
	Utils.assert(initFunction!=null);
	return initFunction;
    }

    /**
     * Returns the list of arguments that should be passed to init
     * function.
     */
    public List getInitArgs() {
	return fusedArgs;
    }
    
}
