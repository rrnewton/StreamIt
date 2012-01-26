package at.dms.kjc.sir.lowering.fission;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import at.dms.kjc.CStdType;
import at.dms.kjc.Constants;
import at.dms.kjc.JAddExpression;
import at.dms.kjc.JAssignmentExpression;
import at.dms.kjc.JBlock;
import at.dms.kjc.JEmptyStatement;
import at.dms.kjc.JEqualityExpression;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JFieldAccessExpression;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JIfStatement;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JMinusExpression;
import at.dms.kjc.JModuloExpression;
import at.dms.kjc.JStatement;
import at.dms.kjc.JVariableDefinition;
import at.dms.kjc.ObjectDeepCloner;
import at.dms.kjc.common.LowerIterationExpression;
import at.dms.kjc.iterator.IterFactory;
import at.dms.kjc.sir.SIRContainer;
import at.dms.kjc.sir.SIRFeedbackLoop;
import at.dms.kjc.sir.SIRFilter;
import at.dms.kjc.sir.SIRIdentity;
import at.dms.kjc.sir.SIRJoinType;
import at.dms.kjc.sir.SIRJoiner;
import at.dms.kjc.sir.SIRPopExpression;
import at.dms.kjc.sir.SIRSplitJoin;
import at.dms.kjc.sir.SIRSplitType;
import at.dms.kjc.sir.SIRSplitter;
import at.dms.kjc.sir.SIRTwoStageFilter;
import at.dms.kjc.slir.WorkNodeContent;
import at.dms.util.SIRPrinter;
import at.dms.util.Utils;

/**
 * This class splits a filter that uses the iteration count feature 
 * into a duplicate/round-robin split-join of a user-specified width.
 */
public class IterationStatelessDuplicate {
    private static final String ITERATION_COUNT_FIELD_PATTERN = 
        LowerIterationExpression.ITER_VAR_NAME + "(__(\\d)+)?";
    /** Iteration count variable name */
    private static final String ITERATION_COUNT_VARNAME =
        LowerIterationExpression.ITER_VAR_NAME;
    /** Iteration count start for the individual fissed filter */
    private static final String ITERATION_COUNT_START_VARNAME =
        "__iteration_countStart";
    /** Iteration count total increment (i.e. sum of work of all filters) */
    private static final String ITERATION_COUNT_TOTAL_VARNAME =
        "__iteration_countStepIncr";
    /** Iteration count reps for the individual fissed filter */
    private static final String ITERATION_COUNT_REPS_VARNAME =
        "__iteration_countReps";
    
    /**
     * Toggle that indicates whether a round-robing splitter should be
     * used in the resulting splitjoin if pop==peek for the filter
     * we're fissing.  This seems like it would be a good idea, but it
     * turns out to usually be faster to duplicate all data and
     * decimate at the nodes.
     */
    private static final boolean USE_ROUNDROBIN_SPLITTER = true;
    
    /**
     * The filter we're duplicating.
     */
    private SIRFilter origFilter;
    
    /**
     * The number of repetitions to duplicate
     */
    private int reps;

    /**
     * The desired ratio of work between the fissed children.
     * Invariant: workRatio.length = reps.
     */
    private int[] workRatio;

    /**
     * The list of resulting filters
     */
    private LinkedList<SIRFilter> newFilters;
    
    private IterationStatelessDuplicate(SIRFilter origFilter, int reps, int[] workRatio) {
        this.origFilter = origFilter;
        this.reps = reps;
        this.workRatio = workRatio;
        this.newFilters = new LinkedList<SIRFilter>();
    }
    
    /**
     * Duplicates <filter> into a <reps>-way SplitJoin and replaces
     * the filter with the new construct in the parent.  The new
     * filters will be load-balanced according to 'workRatio'.  For
     * example, if workRatio = {1, 2}, then the second child will do
     * twice as much work as the first child.
     *
     * Requires workRatio.length == reps.
     */
    public static SIRSplitJoin doit(SIRFilter origFilter, int reps, int[] workRatio) {
        if (isFissable(origFilter)) {
            return new IterationStatelessDuplicate(origFilter, reps, workRatio).doIt();
        } else {
            Utils.fail("Trying to split an un-fissable filter: " + origFilter);
            return null;
        }
    }
    /**
     * Duplicates <filter> into a <reps>-way SplitJoin and replaces
     * the filter with the new construct in the parent.  The resulting
     * children will all do the same amount of work.
     */
    public static SIRSplitJoin doit(SIRFilter origFilter, int reps) {
        // init workRatio to {1, 1, ..., 1}
        int[] workRatio = new int[reps];
        for (int i=0; i<reps; i++) {
            workRatio[i] = 1;
        }
        return doit(origFilter, reps, workRatio);
    }
    
    /**
     * Checks whether or not <filter> can be fissed.  
     */
    public static boolean isFissable(SIRFilter filter) {
        //do not fiss sinks
        if (filter.getPushInt() == 0)
            return false;

        // check that it does not have mutable state
        if (hasMutableState(filter)) {
            return false;
        }
    
      //Hack to prevent fissing file writers
        if(filter.getIdent().startsWith("FileReader"))
            return false;
        
      //Hack to prevent fissing file writers
        if(filter.getIdent().startsWith("FileWriter"))
            return false;

        //Don't fiss identities
        if(filter instanceof SIRIdentity)
            return false;

        // don't split a filter with a feedbackloop as a parent, just
        // as a precaution, since feedbackloops are hard to schedule
        // when stuff is blowing up in the body
        SIRContainer[] parents = filter.getParents();
        for (int i=0; i<parents.length; i++) {
            if (parents[i] instanceof SIRFeedbackLoop) {
                return false;
            }
        }
        // We don't yet support fission of two-stage filters that peek.
        if (filter instanceof SIRTwoStageFilter) {
            SIRTwoStageFilter twoStage = (SIRTwoStageFilter)filter;
            if (twoStage.getInitPopInt()>0) {
                return false;
            }
        }
        /*
        //This seems to break fission too
        if(filter.getPopInt()==0)
            return false;
*/
        return true;
    }

    /**
     * Returns whether or not <filter> has mutable state.  This is
     * equivalent to checking if there are any assignments to fields
     * outside of the init function.
     * 
     * Ignores any variables that are products of using iteration counts.
     */
    public static boolean hasMutableState(final SIRFilter filter) {
        return sizeOfMutableState(filter) > 0;
    }
    
    public static boolean hasMutableState(final WorkNodeContent filter) {
        return sizeOfMutableState(filter) > 0;
    }
    
    /**
     * Returns the number of bytes of mutable state (using C types)
     * for filter <filter>.  Conservatively assumes that if a single
     * location in an array is mutable state, then the entire array is
     * mutable state.
     * 
     * Ignores any variables that are products of using iteration counts.
     */
    private static int sizeOfMutableState(final SIRFilter filter) {
        HashSet<String> mutatedFields = StatelessDuplicate.getMutableState(filter);
        // tally up the size of all the fields found
        int mutableSizeInC = 0;
        JFieldDeclaration fields[] = filter.getFields();
        for (int i=0; i<fields.length; i++) {            
            JVariableDefinition var = fields[i].getVariable();
            
            if (var.getIdent().matches(ITERATION_COUNT_FIELD_PATTERN)) {
                continue;
            }
            
            if (mutatedFields.contains(var.getIdent())) {
                if (var.getType() == null) {
                    // this should never happen
                    System.err.println("Warning: found null type of variable in JFieldDeclaration.");
                    mutableSizeInC++;  // increment size just in case
                } else {
                    int size = var.getType().getSizeInC();
                    // fields should always have non-zero size
                    assert size > 0;
                    mutableSizeInC += size;
                }
            }
        }

        return mutableSizeInC;
    }
    
    private static int sizeOfMutableState(final WorkNodeContent filter) {
        HashSet<String> mutatedFields = StatelessDuplicate.getMutableState(filter);
        // tally up the size of all the fields found
        int mutableSizeInC = 0;
        JFieldDeclaration fields[] = filter.getFields();
        for (int i=0; i<fields.length; i++) {
            JVariableDefinition var = fields[i].getVariable();
            
            if (var.getIdent().matches(ITERATION_COUNT_FIELD_PATTERN)) {
                continue;                
            }
            
            if (mutatedFields.contains(var.getIdent())) {
                if (var.getType() == null) {
                    // this should never happen
                    System.err.println("Warning: found null type of variable in JFieldDeclaration.");
                    mutableSizeInC++;  // increment size just in case
                } else {
                    int size = var.getType().getSizeInC();
                    // fields should always have non-zero size
                    assert size > 0;
                    mutableSizeInC += size;
                }
            }
        }

        return mutableSizeInC;
    }


    
    /**
     * @return new SIRSplitJoin pointing to the fissed SIRFilters
     */
    private SIRSplitJoin doIt() {
        System.out.println("calling iterationStatelessDuplicate.");
        
        /* TODO(ewong): remove Debugging printer */ 
        SIRPrinter printer = new SIRPrinter("IterationStatelessDuplicate_origFilter_" + origFilter.getIdent() + ".sir");
        IterFactory.createFactory().createIter(origFilter).accept(printer);
        printer.close();
        
        
        // make new filters
        for (int i=0; i<reps; i++) {
            newFilters.add(makeDuplicate(i));
        }

        // make result
        SIRSplitJoin result = new SIRSplitJoin(origFilter.getParent(), origFilter.getIdent() + "_Fiss");

        // replace in parent
        origFilter.getParent().replace(origFilter, result);

        // make an init function
        JMethodDeclaration init = makeSJInit(result);
        
        // create the splitter
        if (USE_ROUNDROBIN_SPLITTER && origFilter.getPeekInt()==origFilter.getPopInt()) {
            // without peeking, it's a round-robin..
            // assign split weights according to workRatio and pop rate of filter
            JExpression[] splitWeights = new JExpression[reps];
            for (int i=0; i<reps; i++) {
                splitWeights[i] = new JIntLiteral(workRatio[i] * origFilter.getPopInt());
            }
            result.setSplitter(SIRSplitter.createWeightedRR(result, splitWeights));
        } else {
            // with peeking, it's just a duplicate splitter
            result.setSplitter(SIRSplitter.
                               create(result, SIRSplitType.DUPLICATE, reps));
        }
        
        // create the joiner
        if (origFilter.getPushInt() > 0) {
            // assign join weights according to workRatio and push rate of filter
            JExpression[] joinWeights = new JExpression[reps];
            for (int i=0; i<reps; i++) {
                joinWeights[i] = new JIntLiteral(workRatio[i] * origFilter.getPushInt());
            }
            result.setJoiner(SIRJoiner.createWeightedRR(result, joinWeights));
        } else {
            result.setJoiner(SIRJoiner.create(result,
                                              SIRJoinType.NULL, 
                                              reps));
            // rescale the joiner to the appropriate width
            result.rescale();
        }

        // set the init function
        result.setInit(init);
        
        
        /* TODO(ewong): remove Debugging printer */ 
        printer = new SIRPrinter("IterationStatelessDuplicate_newSplitJoin_" + origFilter.getIdent() + ".sir");
        IterFactory.createFactory().createIter(result).accept(printer);
        printer.close();
        
        return result;       
    }
    
    /**
     * Returns an init function for the containing splitjoin.
     */
    @SuppressWarnings("unchecked")
    private JMethodDeclaration makeSJInit(SIRSplitJoin sj) {
        // start by cloning the original init function, so we can get
        // the signature right
        JMethodDeclaration result = (JMethodDeclaration)
            ObjectDeepCloner.deepCopy(origFilter.getInit());
        // get the parameters
        List params = sj.getParams();
        // now build up the body as a series of calls to the sub-streams
        LinkedList bodyList = new LinkedList();
        for (ListIterator<SIRFilter> it = newFilters.listIterator(); it.hasNext(); ) {
            // build up the argument list
            LinkedList args = new LinkedList();
            for (ListIterator pit = params.listIterator(); pit.hasNext(); ) {
                args.add((JExpression)pit.next());
            }
            // add the child and the argument to the parent
            sj.add(it.next(), args);
        }
        // replace the body of the init function with statement list
        // we just made
        result.setBody(new JBlock(null, bodyList, null));
        // return result
        return result;
    }
    

    /**
     * Makes the <i>'th duplicate filter in this.
     */
    private SIRFilter makeDuplicate(int i) {
        // start by cloning the original filter, and copying the state
        // into a new filter.
        SIRFilter cloned = (SIRFilter)ObjectDeepCloner.deepCopy(origFilter);
        // set cloned isFiss to true
        cloned.setFissed(true);
        
        // wrap the filter with iteration count updates
        wrapFilterWithIterationCountUpdate(cloned, i);
        
        // if there is no peeking, then we can returned <cloned>.
        if (USE_ROUNDROBIN_SPLITTER && origFilter.getPeekInt()==origFilter.getPopInt()) {
            return cloned;
        } else {
            // wrap work function with pop statements for extra items
            wrapWorkFunction(cloned, i);
            return cloned;
        }
    }
    
    private void wrapFilterWithIterationCountUpdate(SIRFilter filter, int i) {
        // the iteration count field may have been renamed.  this variable stores the name of the iteration
        // variable.  When we come across its actual name, it will be updated.
        String iterationCountVarname = ITERATION_COUNT_VARNAME;
        
        // add the appropriate fields to the Filter
        // add the work value field.  This will differ from filter to filter because of the number of reps:
        filter.addField(
                new JFieldDeclaration(
                        new JVariableDefinition(
                                Constants.ACC_PRIVATE,
                                CStdType.Integer,
                                ITERATION_COUNT_REPS_VARNAME,
                                new JIntLiteral(workRatio[i]))));
        // add the start value field.  This will differ from filter to filter because of where it needs to start:
        int startValue = 0;
        for (int j=0; j != i; j++) {
            startValue += workRatio[j];
        }
        filter.addField(
                new JFieldDeclaration(
                        new JVariableDefinition(
                                Constants.ACC_PRIVATE,
                                CStdType.Integer,
                                ITERATION_COUNT_START_VARNAME,
                                new JIntLiteral(startValue))));
        for (JFieldDeclaration field : filter.getFields()) {
            if (field.getVariable().getIdent().startsWith(iterationCountVarname)) {
                field.setValue(new JIntLiteral(startValue));
                
                // We have found the actual field for the iteration count variable.
                iterationCountVarname = field.getVariable().getIdent();
                break;
            }
        }
        // add the incr value field.  This will be the same for all filters (sum of all work ratio).
        int totalWork = 0;
        for (int work : workRatio) {
            totalWork += work;
        }
        filter.addField(
                new JFieldDeclaration(
                        new JVariableDefinition(
                                Constants.ACC_PRIVATE,
                                CStdType.Integer,
                                ITERATION_COUNT_TOTAL_VARNAME,
                                new JIntLiteral(totalWork))));
        
        // set the appropriate update at the end of the work function (after iterationCount is incremented)
        JMethodDeclaration work = filter.getWork();

        // ((i - START - REPS) % TOT) == 0
        JFieldAccessExpression iterCount = new JFieldAccessExpression(iterationCountVarname);
        JFieldAccessExpression iterStart = new JFieldAccessExpression(ITERATION_COUNT_START_VARNAME);
        JFieldAccessExpression iterReps = new JFieldAccessExpression(ITERATION_COUNT_REPS_VARNAME);
        JFieldAccessExpression iterTot = new JFieldAccessExpression(ITERATION_COUNT_TOTAL_VARNAME);
        iterCount.setType(CStdType.Integer);
        iterStart.setType(CStdType.Integer);
        iterReps.setType(CStdType.Integer);
        iterTot.setType(CStdType.Integer);
        JEqualityExpression condExpr = 
            new JEqualityExpression(null, 
                true,
                new JModuloExpression(null, 
                    new JMinusExpression(null, 
                        new JMinusExpression(null, 
                            iterCount,
                            iterStart),
                        iterReps),
                    iterTot),
                new JIntLiteral(0));

        // (i = i + TOT - REPS)
        JStatement thenExpr = new JExpressionStatement(
            new JAssignmentExpression(
                iterCount,
                new JAddExpression(
                    iterCount,
                    new JMinusExpression(null,
                        iterTot,
                        iterReps))));
        
        // empty
        JStatement elseStmt = new JEmptyStatement(null, null);
        
        
        work.addStatement(new JIfStatement(null, 
                condExpr,
                thenExpr,
                elseStmt, 
                null));
        
    }
    
    // For the i'th child in the fissed splitjoin, does two things:
    // 
    // 1. wraps the work function in a loop corresponding to the
    // workRatio of the filter
    //
    // 2. adds pop statements before and after the loop to eliminate
    // items destined for other filters
    // 
    // Also adjusts the I/O rates to match this behavior.  Note that
    // this function only applies to duplication utilizing a duplicate
    // splitter.
    private void wrapWorkFunction(SIRFilter filter, int i) {
        JMethodDeclaration work = filter.getWork();
        // if we have a workRatio of 0, we'll get a class cast
        // exception from JEmptyStatement to JBlock on the next line
        assert workRatio[i] > 0 : "Require workRatio > 0 in horizontal fission.";
        // wrap existing work function according to its work ratio
        work.setBody((JBlock)Utils.makeForLoop(work.getBody(), workRatio[i]));

        // calculate the pops that should come before and after
        int popsBefore = 0;
        int popsAfter = 0;
        for (int j=0; j<i; j++) {
            popsBefore += workRatio[j] * filter.getPopInt();
        }
        for (int j=i+1; j<reps; j++) {
            popsAfter += workRatio[j] * filter.getPopInt();
        }

        // add pop statements to beginning and end of filter
        work.getBody().addStatementFirst(makePopLoop(popsBefore));
        work.getBody().addStatement(makePopLoop(popsAfter));

        // SET NEW I/O RATES FOR FILTER:
        // pop items destined for any splitjoins
        filter.setPop(getTotalPops());
        // push items according to work ratio
        filter.setPush(workRatio[i] * origFilter.getPushInt());
        // peek the same as the pop rate, unless the last execution
        // peeked even further
        filter.setPeek(Math.max(getTotalPops(),
                                popsBefore+(workRatio[i]-1)*origFilter.getPopInt()+origFilter.getPeekInt()));
    }

    /**
     * Returns a loop that pops <i> items.
     */
    private JStatement makePopLoop(int i) {
        JStatement[] popStatement = 
            { new JExpressionStatement(null, new SIRPopExpression(), null) } ;
        // wrap it in a block and a for loop
        JBlock popBlock = new JBlock(null, popStatement, null);
        return Utils.makeForLoop(popBlock, i);
    }

    /**
     * Returns the total number of items popped by the fissed filters.
     */
    private int totalPops = 0;
    private int getTotalPops() {
        // memoize the sum
        if (totalPops > 0) return totalPops;
        // otherwise compute it
        for (int i=0; i<reps; i++) {
            totalPops += workRatio[i] * origFilter.getPopInt();
        }
        return totalPops;
    }
}
