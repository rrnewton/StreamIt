package at.dms.kjc.sir.lowering;

import java.util.Random;

import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.SLIREmptyVisitor;
import at.dms.kjc.sir.SIRStream;

/**
 * Returns a hash of all the literals in a set of functions and
 * fields.  This hash captures both the order and number of literals
 * appearing in the functions.
 *
 * This hash can be used to detect whether constant prop performed any
 * actions on a filter.
 */
class HashLiterals extends SLIREmptyVisitor {
    /**
     * The hash value computed by this.
     */
    private long hash = 0;
    /**
     * A random number used for the hash function.  It has a fixed
     * seed so the sequence of numbers for each instance of
     * HashLiterals will be the same.
     */
    private final Random random = new Random(0);
    
    /**
     * Returns a hash code for the literals appearing in functions and
     * field declarations of 'str'.  Note that this does NOT recurse
     * into the children of 'str'.
     */
    public static long doit(SIRStream str) {
        HashLiterals hasher = new HashLiterals();
        // visit methods and I/O rates
        JMethodDeclaration[] methods = str.getMethods();
        for (int i=0; i<methods.length; i++) {
            methods[i].accept(hasher);
            methods[i].getPeek().accept(hasher);
            methods[i].getPop().accept(hasher);
            methods[i].getPush().accept(hasher);
        }
        // visit fields
        JFieldDeclaration[] fields = str.getFields();
        for (int i=0; i<fields.length; i++) {
            fields[i].accept(hasher);
        }
        // return hash
        return hasher.hash;
    }

    /**
     * Registers a value that appears in the filter.  This maintains
     * the value of the hash.
     */
    private void registerVal(long value) {
        // rotate left by random amount and XOR with value. (note that
        // the random sequence is the same for every hash computation
        // done)
        hash = Long.rotateLeft(hash, random.nextInt()) ^ value;
    }

    /**
     * prints a boolean literal
     */
    @Override
	public void visitBooleanLiteral(boolean value) {
        registerVal(value ? 0 : 1);
    }

    /**
     * prints a byte literal
     */
    @Override
	public void visitByteLiteral(byte value) {
        registerVal(value);
    }

    /**
     * prints a character literal
     */
    @Override
	public void visitCharLiteral(char value) {
        registerVal(Character.getNumericValue(value));
    }

    /**
     * prints a double literal
     */
    @Override
	public void visitDoubleLiteral(double value) {
        registerVal(Double.doubleToLongBits(value));
    }

    /**
     * prints a float literal
     */
    @Override
	public void visitFloatLiteral(float value) {
        registerVal(Double.doubleToLongBits(value));
    }

    /**
     * prints a int literal
     */
    @Override
	public void visitIntLiteral(int value) {
        registerVal(value);
    }

    /**
     * prints a long literal
     */
    @Override
	public void visitLongLiteral(long value) {
        registerVal(value);
    }

    /**
     * prints a short literal
     */
    @Override
	public void visitShortLiteral(short value) {
        registerVal(value);
    }

    /**
     * prints a string literal
     */
    @Override
	public void visitStringLiteral(String value) {
        registerVal(value.hashCode());
    }

    /**
     * prints a null literal
     */
    @Override
	public void visitNullLiteral() {
        // arbitrary constant
        registerVal(1);
    }
}
