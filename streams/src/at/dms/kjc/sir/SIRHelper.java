package at.dms.kjc.sir;

import java.util.Map;

import at.dms.kjc.CType;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.lir.LIRStreamType;

/**
 * This class represents a set of helper methods available to all filters.
 */

public class SIRHelper extends SIRStream
{
    private boolean _native;
    public SIRHelper(boolean _native)
    {
        super();
        this._native = _native;
    }

    public boolean isNative() { return _native; }

    @Override
	public void setWork(JMethodDeclaration newWork)
    {
        at.dms.util.Utils.fail(ident + ": attempt to add a work function to a Helper");
    }
    @Override
	public void setInit(JMethodDeclaration newInit)
    {
        at.dms.util.Utils.fail(ident + ": attempt to add an init function to a Helper");
    }
    @Override
	public void setInitWithoutReplacement(JMethodDeclaration newInit)
    {
        at.dms.util.Utils.fail(ident + ": attempt to add an init function to a Helper");
    }
    @Override
	public int getPushForSchedule(Map<SIROperator, int[]>[] counts)
    {
        at.dms.util.Utils.fail(ident + ": attempt to call getPushForSchedule for Helper");
        return -1;
    }
    @Override
	public int getPopForSchedule(Map<SIROperator, int[]>[] counts)
    {
        at.dms.util.Utils.fail(ident + ": attempt to call getPopForSchedule for Helper");
        return -1;
    }

    /* Things that we need to implement: */
    @Override
	public CType getOutputType() { return null; }
    @Override
	public LIRStreamType getStreamType() { return null; } // (implement?)
    @Override
	public CType getInputType() { return null; }
    @Override
	public boolean needsInit() { return false; }
    @Override
	public boolean needsWork() { return false; }

    //public void setIdent(java.lang.String name) {
    //  System.out.println("Warning: Refuse to rename Helper!");
    //}

    @Override
	public Object accept(AttributeStreamVisitor v)
    {
        at.dms.util.Utils.fail(ident + ": SIRHelper does not accept AttributeStreamVisitor");
        return null;
        //return v.visitHelper(this, methods);
    }

}
