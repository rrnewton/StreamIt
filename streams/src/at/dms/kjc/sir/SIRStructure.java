package at.dms.kjc.sir;

import at.dms.kjc.lir.LIRStreamType;
import at.dms.kjc.*;

/**
 * This class represents a data structure that may be passed between
 * streams on tapes.  It is implemented as an SIRStream for simplicity;
 * a better design would have a parent class of SIRStream which was
 * "SIR object with fields", and derive from that.
 */
public class SIRStructure extends SIRStream
{
    public SIRStructure(SIRContainer parent,
                        String ident,
                        JFieldDeclaration[] fields)
    {
        super(parent, ident, fields, null);
    }
    public SIRStructure()
    {
        super();
    }
    
    /* Things that can't be called: */
    public void addMethod(JMethodDeclaration method)
    {
        at.dms.util.Utils.fail(ident + ": attempt to add a method to a Structure");
    }
    public void addMethods(JMethodDeclaration[] m)
    {
        at.dms.util.Utils.fail(ident + ": attempt to add a method to a Structure");
    }
    public void setMethods(JMethodDeclaration[] m)
    {
        at.dms.util.Utils.fail(ident + ": attempt to add a method to a Structure");
    }
    public void setWork(JMethodDeclaration newWork)
    {
        at.dms.util.Utils.fail(ident + ": attempt to add a work function to a Structure");
    }
    public void setInit(JMethodDeclaration newInit)
    {
        at.dms.util.Utils.fail(ident + ": attempt to add an init function to a Structure");
    }
    public void setInitWithoutReplacement(JMethodDeclaration newInit)
    {
        at.dms.util.Utils.fail(ident + ": attempt to add an init function to a Structure");
    }
    
    /* Things that we need to implement: */
    public CType getOutputType() { return null; }
    public LIRStreamType getStreamType() { return null; } // (implement?)
    public CType getInputType() { return null; }
    public boolean needsInit() { return false; }
    public boolean needsWork() { return false; }

    public Object clone() 
    {
        SIRStructure s = new SIRStructure(this.parent,
                                          this.ident,
                                          this.fields);
        return s;
    }

    public void accept(StreamVisitor v)
    {
        v.visitStructure(this,
                         parent,
                         fields);
    }
    
    public Object accept(AttributeStreamVisitor v)
    {
        return v.visitStructure(this,
                                parent,
                                fields);
    }
}
