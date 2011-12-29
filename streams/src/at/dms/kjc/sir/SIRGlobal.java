package at.dms.kjc.sir;

import java.util.Map;

import at.dms.kjc.CType;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.lir.LIRStreamType;

/**
 * This class represents a data that is available to all filters.
 */

public class SIRGlobal extends SIRStream
{
    public SIRGlobal()
    {
        super();
    }

    public void setWork(JMethodDeclaration newWork)
    {
        at.dms.util.Utils.fail(ident + ": attempt to add a work function to a Global");
    }
    public int getPushForSchedule(Map<SIROperator, int[]>[] counts)
    {
        at.dms.util.Utils.fail(ident + ": attempt to call getPushForSchedule for Global");
        return -1;
    }
    public int getPopForSchedule(Map<SIROperator, int[]>[] counts)
    {
        at.dms.util.Utils.fail(ident + ": attempt to call getPopForSchedule for Global");
        return -1;
    }

    /* Things that we need to implement: */
    public CType getOutputType() { return null; }
    public LIRStreamType getStreamType() { return null; } // (implement?)
    public CType getInputType() { return null; }
    public boolean needsInit() { return true; }
    public boolean needsWork() { return false; }

    public Object accept(AttributeStreamVisitor v)
    {
        at.dms.util.Utils.fail(ident + ": SIRGlobal does not accept AttributeStreamVisitor");
        return null;
        //return v.visitGlobal(this, methods);
    }
}


