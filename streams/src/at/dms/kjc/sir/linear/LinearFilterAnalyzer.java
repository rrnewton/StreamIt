package at.dms.kjc.sir.linear;

import java.util.*;
import at.dms.kjc.*;
import at.dms.kjc.sir.*;
import at.dms.kjc.sir.linear.*;
import at.dms.kjc.iterator.*;


/**
 * The LinearFilterAnalyzer visits all of the Filter definitions in
 * a StreamIT program. It determines which filters calculate linear
 * functions of their inputs, and for those that do, it keeps a mapping from
 * the filter name to the filter's matrix representation.
 *
 * $Id: LinearFilterAnalyzer.java,v 1.10 2002-09-09 21:52:06 aalamb Exp $
 **/
public class LinearFilterAnalyzer extends EmptyStreamVisitor {
    /** Mapping from filters to linear representations. never would have guessed that, would you? **/
    HashMap filtersToLinearRepresentation;

    // counters to keep track of how many of what type of stream constructs we have seen.
    int filtersSeen        = 0;
    int pipelinesSeen      = 0;
    int splitJoinsSeen     = 0;
    int feedbackLoopsSeen  = 0;

    
    
    /** use findLinearFilters to instantiate a LinearFilterAnalyzer **/
    private LinearFilterAnalyzer() {
	this.filtersToLinearRepresentation = new HashMap();
	checkRep();
    }

    /** Returns true if the specified filter has a linear representation that we have found. **/
    public boolean hasLinearRepresentation(SIRStream stream) {
	checkRep();
	// just check to see if the hash set has a mapping to something other than null.
	return (this.filtersToLinearRepresentation.get(stream) != null);
    }

    /**
     * returns the mapping from stream to linear representation that we have. Returns
     * null if we do not have a mapping.
     **/
    public LinearFilterRepresentation getLinearRepresentation(SIRStream stream) {
	checkRep();
	return (LinearFilterRepresentation)this.filtersToLinearRepresentation.get(stream);
    }
    
    /**
     * Main entry point -- searches the passed stream for
     * linear filters and calculates their associated matricies.
     *
     * If the debug flag is set, then we print a lot of debugging information
     **/
    public static LinearFilterAnalyzer findLinearFilters(SIRStream str, boolean debug) {
	// set up the printer to either print or not depending on the debug flag
	LinearPrinter.setOutput(debug);
	LinearPrinter.println("aal--In linear filter visitor");
	LinearFilterAnalyzer lfa = new LinearFilterAnalyzer();
	IterFactory.createIter(str).accept(lfa);
	// generate a report and print it.
	LinearPrinter.println(lfa.generateReport());
	return lfa;
    }
    

    /** More or less get a callback for each stram **/
    public void visitFilter(SIRFilter self, SIRFilterIter iter) {
	this.filtersSeen++;
	LinearPrinter.println("Visiting " + "(" + self + ")");

	// set up the visitor that will actually collect the data
	int peekRate = extractInteger(self.getPeek());
	int pushRate = extractInteger(self.getPush());
	LinearPrinter.println("  Peek rate: " + peekRate);
	LinearPrinter.println("  Push rate: " + pushRate);
	// if we have a peek or push rate of zero, this isn't a linear filter that we care about,
	// so only try and visit the filter if both are non-zero
	if ((peekRate != 0) && (pushRate != 0)) {
	    LinearFilterVisitor theVisitor = new LinearFilterVisitor(self.getIdent(),
								     peekRate, pushRate);

	    // pump the visitor through the work function
	    // (we might need to send it thought the init function as well so that
	    //  we can determine the initial values of fields. However, I think that
	    //  field prop is supposed to take care of this.)
	    self.getWork().accept(theVisitor);

	    // print out the results of pumping the visitor
	    if (theVisitor.computesLinearFunction()) {
		LinearPrinter.println("Linear filter found: " + self +
				   "\n-->Matrix:\n" + theVisitor.getMatrixRepresentation() +
				   "\n-->Constant Vector:\n" + theVisitor.getConstantVector());
		// add a mapping from the filter to its linear form.
		this.filtersToLinearRepresentation.put(self,
						       theVisitor.getLinearRepresentation());
	    }

	    
		
	} else {
	    LinearPrinter.println("  " + self.getIdent() + " is source/sink.");
	}

	// check the rep invariant
	checkRep();
    }

    /**
     * Eventually, this method will handle combining feedback loops (possibly).
     * For now it just keeps track of the number of times we have seen feedback loops.
     **/
    public void preVisitFeedbackLoop(SIRFeedbackLoop self, SIRFeedbackLoopIter iter) {
	this.feedbackLoopsSeen++;
    }
    /**
     * Eventually, this method will handle combining pipelines.
     * For now it just keeps track of the number of times we have seen pipelines.
     **/
    public void preVisitPipeline(SIRPipeline self, SIRPipelineIter iter) {
	this.pipelinesSeen++;
    }
    /**
     * Eventually, this method will handle combining splitjoins.
     * For now it just keeps track of the number of times we have seen splitjoins.
     **/
    public void preVisitSplitJoin(SIRSplitJoin self, SIRSplitJoinIter iter) {
	this.splitJoinsSeen++;
    }
	

    /**
     * Make a nice report on the number of stream constructs processed
     * and the number of things that we found linear forms for.
     **/
    private String generateReport() {
	String rpt;
	rpt = "Linearity Report\n";
	rpt += "---------------------\n";
	rpt += "Filters:       " + makePctString(filtersToLinearRepresentation.keySet().size(),
						 this.filtersSeen);
	rpt += "Pipelines:     " + this.pipelinesSeen + "\n";
	rpt += "SplitJoins:    " + this.splitJoinsSeen + "\n";
	rpt += "FeedbackLoops: " + this.feedbackLoopsSeen + "\n";
	rpt += "---------------------\n";
	return rpt;
    }
    /** simple utility for report generation. makes "top/bottom (pct) string **/
    private String makePctString(int top, int bottom) {
	float pct;
	// figure out percent to two decimal places
	pct = ((float)top)/((float)bottom);
	pct *= 10000;
	pct = Math.round(pct);
	pct /= 100;
	return top + "/" + bottom + " (" + pct + "%)\n";
    }
    
    /** extract the actual value from a JExpression that is actually a literal... **/
    private static int extractInteger(JExpression expr) {
	if (expr == null) {throw new RuntimeException("null peek rate");}
	if (!(expr instanceof JIntLiteral)) {throw new RuntimeException("non integer peek rate");}
	JIntLiteral literal = (JIntLiteral)expr;
	return literal.intValue();
    }

    private void checkRep() {
	// make sure that all keys in FiltersToMatricies are strings, and that all
	// values are LinearForms.
	Iterator keyIter = this.filtersToLinearRepresentation.keySet().iterator();
	while(keyIter.hasNext()) {
	    Object o = keyIter.next();
	    if (!(o instanceof SIRStream)) {throw new RuntimeException("Non stream key in LinearFilterAnalyzer");}
	    SIRStream key = (SIRStream)o;
	    Object val = this.filtersToLinearRepresentation.get(key);
	    if (val == null) {throw new RuntimeException("Null value found in LinearFilterAnalyzer");}
	    if (!(val instanceof LinearFilterRepresentation)) {
		throw new RuntimeException("Non LinearFilterRepresentation found in LinearFilterAnalyzer");
	    }
	}
    }
}








// ----------------------------------------
// Code for visitor class.
// ----------------------------------------




/**
 * A visitor class that goes through all of the expressions in the work function
 * of a filter to determine if the filter is linear and if it is what matrix
 * corresponds to the filter.<p>
 *
 * This main workings of this class are as follows: it is an AttributeVisitor,
 * which means in plain english, that its methods return objects. For the
 * LinearFilterVisitor, each method analyzes a IR node. It returns one of two
 * things. <p>
 *
 * Returning null indicates that that particular IR node does not compute a
 * linear function. Otherwise, a LinearForm is returned, which corresponds to
 * the linear function that is computed by that IR nodes. LinearForms can
 * be used to represent linear combinations of the input plus a constant.
 **/
class LinearFilterVisitor extends SLIREmptyAttributeVisitor {
    /**
     * Mappings from JLocalVariables and JFieldAccessExpressons
     * to LinearForms. Each LinearForm holds the
     * affine representation that maps the expression to a combination of
     * inputs (eg peek expressions indexes) and possibly a constant.
     **/
    private HashMap variablesToLinearForms;

    /**
     * number of items that are peeked at. therefore this is also the same
     * size of the vector that must be used to represent.
     **/
    private int peekSize;
    /**
     * Number of items that are pused. Therefore it also represents the
     * number of columns that are in the matrix representation.
     **/
    private int pushSize;

    /**
     * The current offset to add to a peeked value. Eg if we execute
     * <pre>peek(5); pop(); peek(5); </pre> the second peek
     * expression actually gets a different element, as you would expect.
     **/
    private int peekOffset;

    /**
     * The current push offset. This keeps track of which colimn in the Linear representation
     * of the current filter should be updated with the linear form.
     **/
    private int pushOffset;

    /**
     * Flag that is set when we detect that something blatently non-linear is
     * computed by the filter (eg <pre>push(peek(1)*peek(2);</pre>)
     * Starts off as false, and is set to true if we hit a statement that
     * makes the filter non-linear.
     **/
    private boolean nonLinearFlag;

    /** The matrix which represents this filter. **/
    private FilterMatrix representationMatrix;
    /**
     * A vector of offsets (eg constants that need to be added
     * to the combo of inputs to produce the output).
     **/
    private FilterVector representationVector;

    /**
     * String which represents the name of the filter being visited -- this is used
     * for reporting useful error messages.
     **/
    private String filterName;

    
    /**
     * Create a new LinearFilterVisitor which is looking to figure out 
     * how to compute, for all variables, linear forms from the input.
     * Also creates a LinearFilterRepresentation if the
     * filter computes a linear function.
     **/
    public LinearFilterVisitor(String name, int numPeeks, int numPushes) {
	this.peekSize = numPeeks;
	this.pushSize = numPushes;
	this.variablesToLinearForms = new HashMap();
	this.peekOffset = 0;
	this.pushOffset = 0;
	this.representationMatrix = new FilterMatrix(numPeeks, numPushes);
	this.representationVector = new FilterVector(numPushes);
	this.nonLinearFlag = false;
	this.filterName = name;
	checkRep();

    }

    /**
     * Returns a shallow clone of this filter visitor (eg all of the
     * data structures are copied, but the things that they point to are not.
     **/
    private LinearFilterVisitor copy() {
	// first, make the copy using the default constructors
	LinearFilterVisitor otherVisitor = new LinearFilterVisitor(this.filterName,
								  this.peekSize,
								  this.pushSize);

	// now, copy the other data structures.
	otherVisitor.variablesToLinearForms = new HashMap(this.variablesToLinearForms);
	otherVisitor.peekOffset = this.peekOffset;
	otherVisitor.pushOffset = this.pushOffset;
	otherVisitor.representationMatrix = this.representationMatrix.copy();
	otherVisitor.representationVector = (FilterVector)this.representationVector.copy();
	otherVisitor.nonLinearFlag = this.nonLinearFlag;

	

	// and I think that that is all the state that we need.
	return otherVisitor;
    }

    /**
     * Recconcile the differences between two LinearFilterVisitors.
     * Basically, this implements the confluence operation that we
     * want to to after analyzing both the then and the else parts
     * of an if statement. We assume that otherVisitor was passed through
     * the other branch, and we are at the point where the control flow
     * comes back together and we want to figure out what is going on.<p>
     *
     * The basic rules are pretty simple. If the representation matrices are
     * different, or either this or otherVisitor has the nonlinear flag
     * set, we set this.nonlinear flag to true, and continue (because the rest
     * is irrelevant). If the linear representations are different, we also
     * set the non linear flag and continue. Also, if the push counts are different
     * we complain loudly, and bomb with an exception. If the reps are not different,
     * then we do a set union on the variables to linear forms (like const prop).
     **/
    public void applyConfluence(LinearFilterVisitor other) {
	// first thing that we need to do is to check both non linear flags.
	if (this.nonLinearFlag || other.nonLinearFlag) {
	    this.nonLinearFlag = true;
	    return;
	}
	// now, check the linear representations
	if ((!this.representationMatrix.equals(other.representationMatrix)) ||
	    (!this.representationVector.equals(other.representationVector))) {
	    LinearPrinter.println("Different branches compute different functions. Nonlinear!");
	    this.nonLinearFlag = true;
	    return;
	}
	// now, check the peek offset
	if (!(this.peekOffset == other.peekOffset)) {
	    LinearPrinter.println("Different branches have diff num of pops. " +
				  "this = " + this.peekOffset +
				  " other= " + other.peekOffset +
				  ")  Nonlinear!");
	    this.nonLinearFlag = true;
	    return;
	}	    
	// now, check the push offset
	if (!(this.pushOffset == other.pushOffset)) {
	    LinearPrinter.println("Different branches have diff num of pushes. Nonlinear!");
	    this.nonLinearFlag = true;
	    return;
	}
	// now, do a set union on the two variable mappings and set
	// that union as the variable mapping for this
	this.variablesToLinearForms = setUnion(this.variablesToLinearForms,
					       other.variablesToLinearForms);

	// make sure we are still good from a representation invariant point of view
	checkRep();
    }

    /**
     * Implements set union for the confluence operation.
     * Returns the union of the two sets, that is it returns
     * a HashMap that contains only mappings from the same
     * key to the same value in both map1 and map2.
     **/
    private HashMap setUnion(HashMap map1,
			     HashMap map2) {
	HashMap unionMap = new HashMap();
	// iterate over the values in map1Iter.
	Iterator map1Iter = map1.keySet().iterator();
	while(map1Iter.hasNext()) {
	    Object currentKey = map1Iter.next();
	    // if map2 contains a mapping to the same thing as map1
	    // add the mapping to the union.
	    if (map2.get(currentKey).equals(map1.get(currentKey))) {
		unionMap.put(currentKey, map1.get(currentKey));
	    }
	}
	return unionMap;
    }      

    /** Returns true of the filter computes a linear function. **/
    public boolean computesLinearFunction() {
	// check the flag (which is set when we hit a non linear function in a push expression)
	// and check that we have seen the correct number of pushes.
	boolean enoughPushesSeen = (this.pushSize == this.pushOffset); // last push was to pushSize-1
	if (!(enoughPushesSeen)) {LinearPrinter.warn("Insufficient pushes detected in filter");}
	// if both the non linear flag is unset and there are enough pushes, return true
	return ((!this.nonLinearFlag) && enoughPushesSeen);
    }
    /** get the matrix representing this filter. **/
    public FilterMatrix getMatrixRepresentation() {
	return this.representationMatrix;
    }
    /** get the vector representing the constants that this filter adds/subtracts to produce output. **/
    public FilterVector getConstantVector() {
	return this.representationVector;
    }
    /** get the linear representation of this filter **/
    public LinearFilterRepresentation getLinearRepresentation() {
	// throw exception if this filter is not linear, becase therefore we
	// shouldn't be trying to get its linear form.
	if (!this.computesLinearFunction()) {
	    throw new RuntimeException("Can't get the linear form of a non linear filter!");
	}
	return new LinearFilterRepresentation(this.getMatrixRepresentation(),
					      this.getConstantVector());
    }


    /////// So the deal with this attribute visitor is that all of its methods that visit
    /////// expressions that are some sort of linear (or affine) calculation on the inputs
    /////// the method returns a LinearForm. They return null otherwise.



//     public Object visitArgs(JExpression[] args) {return null;}
    /**
     * Visit an a ArrayAccessExpression. Currently just warn the user if we see one of these.
     * Constant prop should have removed all resolvable array expressions, so if we see one
     * then it is not linear and therefore we should return null;
     **/
    public Object visitArrayAccessExpression(JArrayAccessExpression self,
					     JExpression prefix,
					     JExpression accessor) {
	return getMapping(self);
    }
    /**
     * When we visit an array initializer, we  simply need to return null, as we are currently
     * ignoring arrays on the principle that constprop gets rid of all the ones that are known at compile time.
     **/
    public Object visitArrayInitializer(JArrayInitializer self, JExpression[] elems){
	LinearPrinter.warn("Ignoring array initialization expression: " + self);
	throw new RuntimeException("Array initializations are not supported yet.");
    }

    //     public Object visitArrayLengthExpression(JArrayLengthExpression self, JExpression prefix) {return null;}

    /**
     * Visit's an assignment statement. If the LHS is a variable (local or field)
     * and the RHS becomes a linear form, then we add a mapping from
     * the variable (JLocalVariableExpression or JFieldAccessExpression)
     * to the linear form in the variablesToLinearForm map.
     **/
    public Object visitAssignmentExpression(JAssignmentExpression self, JExpression left, JExpression right) {
	LinearPrinter.println("  visiting assignment expression: " + self);
	LinearPrinter.println("   left side: " + left);
	LinearPrinter.println("   right side: " + right);


	//// NOTE !!!!
	//// This doesn't handle th case of aliasing yet. Oh dearie.
	// Also note that we basically ignore structures because the
	// necessary actions should be implemented in  
	// field prop and not reimplemented here
	
	// make sure that we start with legal state
	checkRep();

	// check the RHS to see if it is a linear form -- pump us through it
	LinearForm rightLinearForm = (LinearForm)right.accept(this);

	// if the RHS is not a linear form, we are done (because this expression computes
	// something nonlinear we need to remove any mappings to the left hand
	// side of the assignment expression (because it now contains something non linear).
	if (rightLinearForm == null) {
	    removeMapping(left);
	    return null;
	}

	
	// try and wrap the left hand side of this expression
	AccessWrapper leftWrapper = AccessWrapperFactory.wrapAccess(left);

	// if we successfully wrapped the expression, say so, and add it to our variable
	// map
	if (leftWrapper != null) {
	    LinearPrinter.println("   adding a mapping from " + left +
				  " to " + rightLinearForm);
	    // add the mapping from the local variable to the linear form
	    this.variablesToLinearForms.put(leftWrapper, rightLinearForm);
				  
	}

	// make sure that we didn't screw up our state
	checkRep();
	
	return null;
    }


    
    /**
     * visits a binary expression: eg add, sub, etc.
     * If the operator is a plus or minus,
     * we can deal with all LinearForms for left and right.
     * If the operator is multiply or divide, we can only deal if
     * both the left and right sides are LinearForms with
     * only offsets. It is not clear to me that we will be
     * ever run into this situation because constant prop should get
     * rid of them all.
     **/
    public Object visitBinaryExpression(JBinaryExpression self,
						  String oper,
						  JExpression left,
						  JExpression right) {

	// and I can't seem to figure out where the constants to recognize the
	// operators are. Therefore, I am going to hard code in the strings. Sorry about that.
	// if we are computing an additon or subtraction, we are all set, otherwise
	// we are done
	if (!(oper.equals("+") || oper.equals("-") || oper.equals("*") || oper.equals("/"))) {
	    LinearPrinter.println("  can't process " + oper + " linearly");
	    return null;
	}

	LinearPrinter.println("  visiting JBinaryExpression(" + oper + ")");
	LinearPrinter.println("   left: " + left);
	LinearPrinter.println("   right: " + right);
			      
	// first of all, try and figure out if left and right sub expression can
	// be represented in linear form.
	LinearForm leftLinearForm  = (LinearForm)left.accept(this);
	LinearForm rightLinearForm = (LinearForm)right.accept(this);

	// if both the left and right are non null, we are golden and can combine these two,
	// otherwise give up.
	if (leftLinearForm == null) {
	    LinearPrinter.println("  left arg (" + left + ") was not linear"); 
	    return null;
	}
	if (rightLinearForm == null) {
	LinearPrinter.println("  right arg (" + right + ") was not linear"); 
	    return null;
	}

	// if both expressions were linear, we can try to merge them together
	// dispatch on type -- sorry to all you language purists
	if ((self instanceof JAddExpression) || (self instanceof JMinusExpression)) {
	    return combineAddExpression(leftLinearForm, rightLinearForm, oper);
	} else if (self instanceof JMultExpression) {
	    return combineMultExpression(leftLinearForm, rightLinearForm, oper);
	} else if (self instanceof JDivideExpression) {
	    return combineDivideExpression(leftLinearForm, rightLinearForm, oper);
	} else {
 	    throw new RuntimeException("Non JAdd/JMinus/JMult/JDiv implementing +, -, *, /");
	}
    }

    /**
     * Combines an add expression whose arguments are both linear forms -- implements
     * a straightup element wise vector add.
     **/
    private Object combineAddExpression(LinearForm leftLinearForm, LinearForm rightLinearForm, String oper) {
	// if the operator is subtraction, negate the right expression
	if (oper.equals("-")) {
	    rightLinearForm = rightLinearForm.negate();
	}

	// now, add the two forms together and return the resulut
	LinearForm combinedLinearForm = leftLinearForm.plus(rightLinearForm);
	
	return combinedLinearForm;
    }

    /**
     * Combines a multiplication expression which has at most one non constant
     * sub expression. If both of the linear forms are non constant (eg 
     * weights that are non zero) then we return null
     **/
    private Object combineMultExpression(LinearForm leftLinearForm, LinearForm rightLinearForm, String oper) {
	LinearForm constantForm;
	LinearForm otherForm;

	// figure out which form represents a constant
	// is the left a constant?
	if (leftLinearForm.isOnlyOffset()) {
	    constantForm = leftLinearForm;
	    otherForm = rightLinearForm;
	    // how about the right?
	} else if (rightLinearForm.isOnlyOffset()) {
	    constantForm = rightLinearForm;
	    otherForm = leftLinearForm;
	// both are non constant, so give up
	} else {
	    return null;
	}

	// now, scale the other by the constant offset in the constant form
	// and return the scaled version
	LinearForm scaledForm = otherForm.multiplyByConstant(constantForm.getOffset());
	return scaledForm;
    }

    /**
     * Combines a division expresson. The right argument has to be a constant (eg only offset)
     **/
    private Object combineDivideExpression(LinearForm leftLinearForm, LinearForm rightLinearForm, String oper) {       
	// ensure that the right form is a constant. If not, we are done.
	if (!rightLinearForm.isOnlyOffset()) {
	    LinearPrinter.println("  right side of linear form is not constant"); 
	    return null;
	}
	LinearPrinter.println("  dividing left " + leftLinearForm + "\n   by right " + rightLinearForm);
	// just divide the left form by the constant
	return leftLinearForm.divideByConstant(rightLinearForm.getOffset());
    }
	    

    
    

//     public Object visitBitwiseComplementExpression(JUnaryExpression self, JExpression expr){return null;}
//     public Object visitBitwiseExpression(JBitwiseExpression self,
// 					 int oper,
// 					 JExpression left,
// 					 JExpression right){return null;}
//     public Object visitBlockStatement(JBlock self, JavaStyleComment[] comments){return null;}
//     public Object visitBreakStatement(JBreakStatement self, String label){return null;}
    /**
     * Visit a cast expression, which basically means that we need to do chopping off if we are casting to
     * an integer, byte, etc. If we cast something to an int, that is a non linear operation, so we
     * just return null.
     **/
    public Object visitCastExpression(JCastExpression self, JExpression expr, CType type){
	// if we have a non ordinal type for the expression, and an ordinal type for
	// the cast, then this is a non linear operation, and we should return null.
	if ((!expr.getType().isOrdinal()) && type.isOrdinal()) {
	    // this chops off digits (possibly), so non linear. We are all done.
	    return null;
	} else {
	    // return whatever the expression evaluates to
	    return expr.accept(this);
	}
    }
    

//     public Object visitCatchClause(JCatchClause self, JFormalParameter exception, JBlock body){return null;}
//     public Object visitClassBody(JTypeDeclaration[] decls,
// 				 JFieldDeclaration[] fields,
// 				 JMethodDeclaration[] methods,
// 				 JPhylum[] body){return null;}
//     public Object visitClassDeclaration(JClassDeclaration self, int modifiers,
// 					String ident, String superName,
// 					CClassType[] interfaces, JPhylum[] body,
// 					JFieldDeclaration[] fields, JMethodDeclaration[] methods,
// 					JTypeDeclaration[] decls){return null;}
//     public Object visitClassExpression(JClassExpression self, CType type){return null;}
//     public Object visitClassImport(String name){return null;}
//     public Object visitComment(JavaStyleComment self){return null;}
//     public Object visitComments(JavaStyleComment[] self){return null;}
//     public Object visitCompilationUnit(JCompilationUnit self, JPackageName packageName,
// 						 JPackageImport[] importedPackages,
// 				       JClassImport[] importedClasses,
// 				       JTypeDeclaration[] typeDeclarations){return null;}

    /**
     * visits a compound assignment expression -- eg +=, -=, etc.
     * If the left hand side is a JLocalVariableExpression or JFieldAccessExpression
     * we add the appropriate mapping in variablesToLinearForms.
     **/
    public Object visitCompoundAssignmentExpression(JCompoundAssignmentExpression self,
						    int oper, JExpression left,
 						    JExpression right){


	// act based on the operaton being performed. -- code mostly copied from Propagator.java
	// The basic premise is to expand out the compound operation into a normal JAssignmentExpression
	// and stuff ourselves (the visitor) through the new construct. This allows us to use our existing code.
	switch(oper) {

	case OPE_SR: // shift right operator:  >> -- non linear
	    return new JAssignmentExpression(null,left,new JShiftExpression(null,OPE_SR,left,right)).accept(this);

	case OPE_SL: // shift left operator:  << -- non linear
	    return new JAssignmentExpression(null,left,new JShiftExpression(null,OPE_SL,left,right)).accept(this);

	case OPE_PLUS: // plus operator:  +  -- linear!
	    return new JAssignmentExpression(null,left,new JAddExpression(null,left,right)).accept(this);

	case OPE_MINUS: // minus operator:  -  -- linear!
	    return new JAssignmentExpression(null,left,new JMinusExpression(null,left,right)).accept(this);

	case OPE_STAR: // multiplication operator:  +  -- linear!
	    return new JAssignmentExpression(null,left,new JMultExpression(null,left,right)).accept(this);

	case OPE_SLASH: // division operator:  / -- linear!
	    return new JAssignmentExpression(null,left,new JDivideExpression(null,left,right)).accept(this);

	case OPE_PERCENT: // modulus operator:  % -- non linear
	    return new JAssignmentExpression(null,left,new JModuloExpression(null,left,right)).accept(this);

	default:
	    throw new RuntimeException("Unknown operation while analyzing compound assignment expression:" + oper);
	}
    }


//     public Object visitCompoundStatement(JCompoundStatement self, JStatement[] body){return null;}
//     public Object visitConditionalExpression(JConditionalExpression self, JExpression cond,
// 					     JExpression left, JExpression right){return null;}
//     public Object visitConstructorCall(JConstructorCall self, boolean functorIsThis, JExpression[] params){return null;}
//     public Object visitConstructorDeclaration(JConstructorDeclaration self, int modifiers,
// 					      String ident, JFormalParameter[] parameters,
// 					      CClassType[] exceptions, JConstructorBlock body){return null;}
//     public Object visitContinueStatement(JContinueStatement self, String label){return null;}
//     public Object visitDoStatement(JDoStatement self, JExpression cond, JStatement body){return null;}
//     public Object visitEmptyStatement(JEmptyStatement self){return null;}
    /** equality is a non linear operation, so we return null. **/
    public Object visitEqualityExpression(JEqualityExpression self, boolean equal,
 					  JExpression left, JExpression right){
	return null;
    }
//     public Object visitExpressionListStatement(JExpressionListStatement self, JExpression[] expr){return null;}
//     public Object visitExpressionStatement(JExpressionStatement self, JExpression expr){return null;}
//     public Object visitFieldDeclaration(JFieldDeclaration self, int modifiers,
// 					CType type, String ident, JExpression expr){return null;}
    /**
     * visits a field access expression. If there is a linear form mapped to this
     * expression in variablesToLinearForms, we return that. If there is no linear
     * form mapped to this expression, we simply return null;
     **/
    public Object visitFieldExpression(JFieldAccessExpression self, JExpression left, String ident){
	checkRep();
	LinearPrinter.println("  visiting field access expression: " + self);
	LinearPrinter.println("   left: " + left);
	LinearPrinter.println("   ident: " + ident);

	checkRep();

	// wrap the field access and then use that wrapper as the
	// key into the variables->linearform mapping
	AccessWrapper wrapper = AccessWrapperFactory.wrapAccess(self);

	// if we have a mapping, return it. Otherwise return null.
	if (this.variablesToLinearForms.containsKey(wrapper)) {
	    LinearPrinter.println("   (found mapping for " + wrapper + ")");
	    return this.variablesToLinearForms.get(wrapper);
	} else {
	    LinearPrinter.println("   (no mapping found for " + wrapper + ")");
	    Iterator keyIter = this.variablesToLinearForms.keySet().iterator();
	    while(keyIter.hasNext()) {
		LinearPrinter.println("key: " + keyIter.next());
	    }
	    return null;
	}
    }
//     public Object visitFormalParameters(JFormalParameter self, boolean isFinal,
// 					CType type, String ident){return null;}
//     public Object visitForStatement(JForStatement self, JStatement init,
// 				    JExpression cond, JStatement incr, JStatement body){return null;}
    /**
     * Visit an if statement -- push this down the
     * the then clause, push a copy of this down the else clause,
     * and then merge the differences. Also of note is that
     * the conditional is a constant, constprop should have taken
     * care of it, so flag an error.
     **/
    public Object visitIfStatement(JIfStatement self, JExpression cond,
				   JStatement thenClause, JStatement elseClause){
	LinearForm condForm = (LinearForm)cond.accept(this);
	// if the cond form is a constant (eg only an offset), we should bomb an error as
	// const prop should have taken care of it.
	if (condForm != null) {
	    if (condForm.isOnlyOffset()) {
		throw new RuntimeException("Constant condition to if statement, " +
					   "const prop should have handled " +
					   self);
	    }
	}
	// now, make a copy of this
	LinearFilterVisitor otherVisitor = this.copy();
	// send this through the then clause, and the copy through the else clause
	thenClause.accept(this);
	// if we have an else clause, then send the visitor through it. Else,
	// the program could possibly skip the then clause, so we need to
	// merge the differences afterwards.
	if (elseClause != null) {
	    elseClause.accept(otherVisitor);
	}

	// recconcile the differences between the else clause (or skip if no
	// else clause).
	this.applyConfluence(otherVisitor);
	
	return null;
    }
//     public Object visitInnerClassDeclaration(JClassDeclaration self, int modifiers,
// 					     String ident, String superName,
// 					     CClassType[] interfaces, JTypeDeclaration[] decls,
// 					     JPhylum[] body, JFieldDeclaration[] fields,
// 					     JMethodDeclaration[] methods){return null;}
//     public Object visitInstanceofExpression(JInstanceofExpression self, JExpression expr, CType dest){return null;}
//     public Object visitInterfaceDeclaration(JInterfaceDeclaration self, int modifiers,
// 					    String ident, CClassType[] interfaces,
// 					    JPhylum[] body, JMethodDeclaration[] methods){return null;}
//     public Object visitJavadoc(JavadocComment self){return null;}
//     public Object visitLabeledStatement(JLabeledStatement self, String label,
// 					JStatement stmt){return null;}

    /**
     * Visit a local variable expression. If we have a mapping of this
     * variable in our mappings to linear forms, return the linear form
     * otherwise return null.
     **/
    public Object visitLocalVariableExpression(JLocalVariableExpression self, String ident){
 	LinearPrinter.println("  visiting local var expression: " + self);
	LinearPrinter.println("   variable: " + self.getVariable());

	return getMapping(self);
    }
//     public Object visitLogicalComplementExpression(JUnaryExpression self, JExpression expr){return null;}
    /**
     * Eventually, we should do interprodcedural analysis. Right now instead, we will
     * simply ignore them (eg return null signifying that they do not generate linear things).
     **/
    public Object visitMethodCallExpression(JMethodCallExpression self, JExpression prefix,
 					    String ident, JExpression[] args){
	LinearPrinter.warn("Assuming method call expression non linear(" +
			   ident + "). Also removing all field mappings.");
	this.removeAllFieldMappings();
	return null;
    }

    /**
     * Removes all of the mappings from fields to linear forms. We do this on a method call
     * to make conservative, safe assumptions.
     **/
    private void removeAllFieldMappings() {
	// basic idea is really simple -- iterate over all keys in our hashmap
	// and remove the ones that are AccessWrappers.
	Vector toRemove = new Vector(); // list of items to remove.
	Iterator keyIter = this.variablesToLinearForms.keySet().iterator();
	while(keyIter.hasNext()) {
	    Object key = keyIter.next();
	    if (key instanceof FieldAccessWrapper) {
		toRemove.add(key);
	    }
	}
	// now, remove all items in the toRemove list from the mapping
	Iterator removeIter = toRemove.iterator();
	while(removeIter.hasNext()) {
	    this.variablesToLinearForms.remove(removeIter.next());
	}
    }

    /**
     * Returns the mapping that we have from expr to linear form.
     * returns null if no such mapping exists. 
     **/
    public Object getMapping(JExpression expr) {
	checkRep();

	// wrap the variable that the expression represents
	AccessWrapper wrapper = AccessWrapperFactory.wrapAccess(expr);

	// if we have a mapping, return it. Otherwise return null
	if (this.variablesToLinearForms.containsKey(wrapper)) {
	    LinearPrinter.println("   (found mapping for " + expr + "!)");
	    return this.variablesToLinearForms.get(wrapper);
	} else {
	    LinearPrinter.println("   (mapping not found for " + expr + "!)");
	    return null;
	}
    }

    //     public Object visitMethodDeclaration(JMethodDeclaration self, int modifiers,
// 					 CType returnType, String ident,
// 					 JFormalParameter[] parameters, CClassType[] exceptions,
// 					 JBlock body){return null;}
//     public Object visitNameExpression(JNameExpression self, JExpression prefix, String ident){return null;}

    /**
     * Visit a NewArrayExpression, creating mappings for all entries of the array to
     * a zero linear form (corresponding to initializing all array entries to zero
     * in StreamIt semantics.
     **/
    public Object visitNewArrayExpression(JNewArrayExpression self, CType type,
 					  JExpression[] dims, JArrayInitializer init){
	LinearPrinter.warn("Ignoring new array expression " + self);
	return null;
    }
    

    //     public Object visitPackageImport(String name){return null;}
    //     public Object visitPackageName(String name){return null;}
    public Object visitParenthesedExpression(JParenthesedExpression self, JExpression expr){
	LinearPrinter.println("  visiting parenthesized expression");
	// pass ourselves through the parenthesized expression to generate the approprate constant forms
	return expr.accept(this);
    }
//     public Object visitPostfixExpression(JPostfixExpression self, int oper, JExpression expr){return null;}
//     public Object visitPrefixExpression(JPrefixExpression self, int oper, JExpression expr){return null;}
//     public Object visitQualifiedAnonymousCreation(JQualifiedAnonymousCreation self,
// 						  JExpression prefix, String ident,
// 						  JExpression[] params, JClassDeclaration decl){return null;}
//     public Object visitQualifiedInstanceCreation(JQualifiedInstanceCreation self,
// 						 JExpression prefix, String ident,
// 						 JExpression[] params){return null;}
    /**
     * Visit a relational expression (eg using a <, >, ==, !=, etc. type of parameter.
     * Since these are all non linear operatons, we need to return
     * null to flag the rest of the visitor that they are non linear operations.
     **/
    public Object visitRelationalExpression(JRelationalExpression self, int oper,
					    JExpression left, JExpression right){
	LinearPrinter.println("  visiting non linear" + self);
	return null;
    }
//     public Object visitReturnStatement(JReturnStatement self, JExpression expr){return null;}

    /**
     * Shift expressions are linear for integer operators.
     * They correspond to multiplications or divisions by a power of two.
     * if the RHS of the expression is a constant (eg a lienar form
     * with only an offset) then we can convert it to a power of two, and
     * then make the shift a multiplication or division. Otherwise, this is
     * not a linear operation.
     **/
    public Object visitShiftExpression(JShiftExpression self, int oper, JExpression left, JExpression right){
	// since the left and the right expressions are somre type of integer
	// or byte or something, we are all set. You can't shift floats, as it
	// is not in java syntax and KOPI disallows it in the semantic checking
	// phase.

	LinearPrinter.println("  visiting shift expression: " + self);
	LinearPrinter.println("   left: " + left);
	LinearPrinter.println("   right: " + right);
	LinearPrinter.println("   left from self: " + self.getLeft());
	LinearPrinter.println("   right from self: " + self.getRight());
	

	// try and figure out what the right hand side of the expression is.
	LinearForm rightForm = (LinearForm)right.accept(this);

	// if the right side is non linear, give up. Accept the left side
	// (to ensure any side effects are taken into account) and return null
	if (right == null) {
	    left.accept(this);
	    return null;
	}
	
	// if the right side is a constant, figure it out at compile time,
	// and accept the equivalant multiplication or division.
	if (rightForm.getOffset().isRealInteger()) {
	    int rightInt = (int)rightForm.getOffset().getReal(); // get the real part
	    // calculate the power of two.
	    // this probably won't handle _large_ powers of two.
	    int rightPowerOfTwo = 1 << rightInt;
	    // if this is a left shift, accept a multiplication
	    if (oper == OPE_SL) {
		return (new JMultExpression(null, left, new JIntLiteral(rightPowerOfTwo))).accept(this);
		// if this is a right shift, accept a division
	    } else if (oper == OPE_SR) {
		return (new JDivideExpression(null, left, new JIntLiteral(rightPowerOfTwo))).accept(this);
	    } else {
		throw new RuntimeException("Unknown operator in ShiftExpression:" + oper);
	    }
	} else {
	    // merely accept the left and return
	    left.accept(this);
	    return null;
	}
    }
//     public Object visitShortLiteral(JShortLiteral self, short value){return null;}
//     public Object visitSuperExpression(JSuperExpression self){return null;}
//     public Object visitSwitchGroup(JSwitchGroup self, JSwitchLabel[] labels, JStatement[] stmts){return null;}
//     public Object visitSwitchLabel(JSwitchLabel self, JExpression expr){return null;}
//     public Object visitSwitchStatement(JSwitchStatement self, JExpression expr, JSwitchGroup[] body){return null;}
//     public Object visitSynchronizedStatement(JSynchronizedStatement self, JExpression cond, JStatement body){return null;}
//     public Object visitThisExpression(JThisExpression self, JExpression prefix){return null;}
//     public Object visitThrowStatement(JThrowStatement self, JExpression expr){return null;}
//     public Object visitTryCatchStatement(JTryCatchStatement self, JBlock tryClause, JCatchClause[] catchClauses){return null;}
//     public Object visitTryFinallyStatement(JTryFinallyStatement self, JBlock tryClause, JBlock finallyClause){return null;}
//     public Object visitTypeDeclarationStatement(JTypeDeclarationStatement self, JTypeDeclaration decl){return null;}
//     public Object visitTypeNameExpression(JTypeNameExpression self, CType type){return null;}
//     public Object visitUnaryMinusExpression(JUnaryExpression self, JExpression expr){return null;}
//     public Object visitUnaryPlusExpression(JUnaryExpression self, JExpression expr){return null;}
//     public Object visitUnaryPromoteExpression(JUnaryPromote self, JExpression expr, CType type){return null;}
//     public Object visitUnqualifiedAnonymousCreation(JUnqualifiedAnonymousCreation self,
// 						    CClassType type, JExpression[] params,
// 						    JClassDeclaration decl){return null;}
//     public Object visitUnqualifiedInstanceCreation(JUnqualifiedInstanceCreation self, CClassType type,
// 						   JExpression[] params){return null;}
//     public Object visitVariableDeclarationStatement(JVariableDeclarationStatement self,
// 						    JVariableDefinition[] vars){return null;}
//     public Object visitVariableDefinition(JVariableDefinition self, int modifiers,
// 					  CType type, String ident, JExpression expr){return null;}
//     public Object visitWhileStatement(JWhileStatement self, JExpression cond, JStatement body){return null;}



    ///// SIR Constructs that are interesting for linear analysis (eg push, pop, and peek expressions)
    
    /**
     * when we visit a push expression, we are basically going to try and
     * resolve the argument expression into linear form. If the argument
     * resolves into linear form, then we are golden -- we make a note of the 
     **/
    public Object visitPushExpression(SIRPushExpression self, CType tapeType, JExpression arg) {
	LinearPrinter.println("  visiting push expression: " +
			   "argument: " + arg);
	// try and resolve the argument to a LinearForm by munging the argument
	LinearForm argLinearForm = (LinearForm)arg.accept(this);

	// if we get null, it means we don't know that this push expression will
	// yield a linear form, so therefore we can't characterize the filter as
	// a whole as having linear form.
	if (argLinearForm == null) {
	    // set the flag that says this filter isn't linear
	    this.nonLinearFlag = true;
	    LinearPrinter.println("  push argument wasn't linear: " + arg);
	} else {
	    // (note that the first push ends up in the rightmost column)
	    // so we calculate which column that this push statement corresponds to
	    int pushColumn = this.pushSize - this.pushOffset -1;
	    // we have a linear form, so we update the matrix representation
	    argLinearForm.copyToColumn(this.representationMatrix, pushColumn);
	    // update the constant vector with the offset from the linear form
	    this.representationVector.setElement(pushColumn, argLinearForm.getOffset());
	}

	// increment the push offset (for the next push statement)
	this.pushOffset++;

	LinearPrinter.println("   (current push offset: " + this.pushOffset);

	// make sure we didn't screw up anything.
	checkRep();
	// push expressions don't return values, so they also certainly don't return linear forms
	return null;
    }
    /**
     * A pop expression generates a linear form based on the current offset, and then
     * updates the current offset. The basic idea is that a pop expression represents using one
     * one of the input values from the tapes, and therefore should correspond to a linear form
     * with a "1" at the appropriate place in the weights vector.
     **/
    public Object visitPopExpression(SIRPopExpression self, CType tapeType) {
	LinearPrinter.println("  visiting pop expression: " + self);
	// A pop expression is one of the base cases for creating LinearForms
	// the pop expression will creates a linear form that corresponds to using
	// a peek at the current offset, which in turn corresponds to a
	// use of the element at size-peekoffset-1 in the input vector
	int inputIndex = this.peekSize - this.peekOffset - 1;

	if (inputIndex < 0) {
	    LinearPrinter.warn("Too many pops detected!");
	    this.nonLinearFlag = true;
	    return null;
	}
	
	
	LinearForm currentForm = this.getBlankLinearForm();
	currentForm.setWeight(inputIndex, ComplexNumber.ONE);
	
	// when we hit a pop expression, all further peek expressions have their
	// indicies incremented by one compared to the previous expressions
	this.peekOffset++;

	// return the linear form of the pop expression
	LinearPrinter.println("  returning " + currentForm + " from pop expression");
	return currentForm;
    }
    /**
     * Peek expressions are also base expressions that generate linear forms.
     * The peek index is transformed into a "1" in the appropriate place in
     * the weights vector of the returned linear form. We also have to keep track of
     * the case when there haev been previous pops which change the relative position of the
     * index we are processing.
     **/
    public Object visitPeekExpression(SIRPeekExpression self, CType tapeType, JExpression arg) {
	LinearPrinter.println("  visiting peek expression" +
			   " peek index: " + arg);

	// now, we have to visit the expression of the peek( ) to see if it is a constant
	// (in this context, that will be a linear form)
	LinearForm exprLinearForm = (LinearForm)arg.accept(this);

	// if we didn't find a linear form in the expression, we are cooked
	// (possibly we couldn't resolve the value of some variable
	if (exprLinearForm == null) {
	    return null;
	}

	// otherwise, make sure that the linear form is only a constant offset
	// (if it isn't, we are done beacuse we can't resolve what data item is being looked at)
	if (!exprLinearForm.isOnlyOffset()) {
	    return null;
	}

	// if the offset is not an integer, something is very wrong. Well, not wrong
	// but unesolable because the index is computed with input data.
	if (!exprLinearForm.isIntegerOffset()) {
	    return null;
	}

	// otherwise, create a new linear form that represents which input value that this
	// peek expression produces.
	// basically, it will be a linear form that is all zeros in its weights
	// except for a 1 in the index corresponding to the data item that this peek expression
	// accesses	
	LinearForm peekExprLinearForm = this.getBlankLinearForm();
	peekExprLinearForm.setWeight(this.peekSize - 1 - exprLinearForm.getIntegerOffset() - this.peekOffset,
				     ComplexNumber.ONE);
	LinearPrinter.println("  returning linear form from peek expression: " + peekExprLinearForm);
	return peekExprLinearForm;
    }


    ////// Generators for literal expressions
    
    /** boolean logic falls outside the realm of linear filter analysis -- return null**/
    public Object visitBooleanLiteral(JBooleanLiteral self,boolean value) {return null;}
    /** create the appropriate valued offset **/
    public Object visitByteLiteral(JByteLiteral self, byte value) {
	return this.getOffsetLinearForm((double)value);
    }
    /** create the appropriate valued offset **/
    public Object visitCharLiteral(JCharLiteral self,char value) {
	return this.getOffsetLinearForm((double)value);
    }
    /** create the appropriate valued offset **/
    public Object visitDoubleLiteral(JDoubleLiteral self,double value) {
	return this.getOffsetLinearForm((double)value);
    }
    /** create the appropriate valued offset **/
    public Object visitFloatLiteral(JFloatLiteral self,float value) {
	return this.getOffsetLinearForm((double)value);
    }
    /** create the appropriate valued offset **/
    public Object visitIntLiteral(JIntLiteral self, int value) {
	return this.getOffsetLinearForm((double)value);
    }
    /** create the appropriate valued offset **/
    public Object visitLongLiteral(JLongLiteral self,long value) {
	return this.getOffsetLinearForm((double)value);
    }
    /** create the appropriate valued offset **/
    public Object visitShortLiteral(JShortLiteral self,short value) {
	return this.getOffsetLinearForm((double)value);
    }
    /** We can't deal with strings, not linear, return null **/
    public Object visitStringLiteral(JStringLiteral self,String value) {
	return null;
    }
    /** if we have null nonsense, not going to be linear. Return null (how appropriate)**/
    public Object visitNullLiteral(JNullLiteral self) {
	return null;
    }



    /**
     * Removes the mapping of a local variable expression or a field access expression
     * from the variablesToLinearForm mapping. Throws an exception if we try to remove
     * a mapping to a type that we don't know about
     **/
    public void removeMapping(JExpression expr) {
	// wrap the field access expression
	AccessWrapper theWrapper = AccessWrapperFactory.wrapAccess(expr);
	if (theWrapper != null) {
	    LinearPrinter.println("   removing mapping for : " + expr);
	    // actually remove the data from the hash map
	    this.variablesToLinearForms.remove(theWrapper);
	} else {
	    LinearPrinter.println("   no previous mapping for: " + expr);
	}
    }
    

    

    /**
     * Creates a blank linear form that is appropriate for this filter (eg
     * it has size of peekSize.
     **/
    private LinearForm getBlankLinearForm() {
	checkRep();
	return new LinearForm(this.peekSize);
    }

    /** Creates a blank linear form that has the specified offset **/
    private LinearForm getOffsetLinearForm(double offset) {
	checkRep();
	LinearForm lf = this.getBlankLinearForm();
	lf.setOffset(offset);
	LinearPrinter.println("  created constant linear form for " + offset);
	return lf;
    }

    /**
     * Check the representation invariants of the LinearFilterVisitor.
     **/
    private void checkRep() {
	// make sure that the representation matrix is the correct size
	if (this.peekSize != this.representationMatrix.getRows()) {
	    throw new RuntimeException("Inconsistent matrix representation, rows");
	}
	if (this.pushSize != this.representationMatrix.getCols()) {
	    throw new RuntimeException("Inconsistent matrix representation, cols");
	}
	if (this.pushSize != this.representationVector.getCols()) {
	    throw new RuntimeException("Inconsistent vector representation, cols");
	}


	// check that the only values in the HashMap are LinearForm objects
	Iterator keyIter = this.variablesToLinearForms.keySet().iterator();
	while(keyIter.hasNext()) {
	    Object key = keyIter.next();
	    if (key == null) {throw new RuntimeException("Null key in linear form map");}
	    // make sure that they key is a JLocalVariable or a JFieldAccessExpression
	    if (!(key instanceof AccessWrapper)) {
		throw new RuntimeException("Non access wrapper in linear form map.");
	    }
	    Object val = this.variablesToLinearForms.get(key);
	    if (!(val instanceof LinearForm)) {throw new RuntimeException("Non LinearForm in value map");}
	}
	
	
	// make sure that the peekoffset is not less than one, and that it
	// is not greater than the peeksize
	if (this.peekOffset < 0) {throw new RuntimeException("Peekoffset < 0");}
	// if the filter doesn't peek at any data, the following is incorrect.
	if (peekSize != 0) {
	    if (this.peekOffset > this.peekSize) {
		throw new RuntimeException("Filter (" + this.filterName +
					   ") pops more than peeks:" +
					   "peekSize: " + this.peekSize + " " +
					   "peekOffset: " + this.peekOffset);
	    }
	}
	// make sure that the number of pushes that we have seen doesn't go past the end of
	// the matrix/vector that represents this filter.
	if (this.pushOffset > this.representationMatrix.getCols()) {
	    throw new RuntimeException("Filter (" + this.filterName +
				       ") pushes more items " + 
				       "than is declared (" + this.representationMatrix.getRows());
	}
	    
    }

}
























/** Control point for printing messages **/
class LinearPrinter {
    /** flag to control output generation. **/
    private static boolean outputEnabled = false;
    public static void setOutput(boolean outFlag) {
	outputEnabled = outFlag;
    }
    public static void println(String message) {
	if (outputEnabled) {
	    System.out.println(message);
	}
    }
    public static void print(String message) {
	if (outputEnabled) {
	    System.out.print(message);
	}
    }
    public static void warn(String message) {
	System.err.println("WARNING: " + message);
    }
}
    
