package at.dms.kjc.common;

import java.io.Serializable;
import java.util.HashSet;

import at.dms.kjc.JExpression;
import at.dms.kjc.JFieldAccessExpression;
import at.dms.kjc.JLocalVariableExpression;
import at.dms.kjc.JPhylum;
import at.dms.kjc.SLIREmptyVisitor;
import at.dms.kjc.flatgraph.FlatNode;
import at.dms.kjc.sir.SIRFilter;

/**
 * This class will return a HashSet containing all the
 * variables (locals and fields) used or defined from the entry
 * point of the visitor. 
 *
 * @author Michael Gordon
 * 
 */

public class VariablesDefUse extends SLIREmptyVisitor
{
    private HashSet<Serializable> vars;

    /**
     * Given <entry>, the starting point of the visit, return 
     * a HashSet of all variables used or defined during the IR visit.
     *
     *
     * @param entry The contruct that starts the visiting
     *
     *
     * @return A Hashset containing JLocalVariables for accessed locals 
     * or Strings for accessed fields
     *
     */
    public static HashSet<Serializable> getVars(JPhylum entry) 
    {
        VariablesDefUse used = new VariablesDefUse();
    
        entry.accept(used);
    
        return used.vars;
    }
    
    public static HashSet<Serializable> getVars(FlatNode node) 
    {
        VariablesDefUse used = new VariablesDefUse();
    
        if (node.isFilter()) {
            SIRFilter filter = (SIRFilter)node.contents;
        
            for (int i = 0; i < filter.getMethods().length; i++) {
                filter.getMethods()[i].accept(used);
            }
            for (int i = 0; i < filter.getFields().length; i++) {
                filter.getFields()[i].accept(used);
            }
        }
        return used.vars;
    }
    

    private VariablesDefUse() 
    {
        vars = new HashSet<Serializable>();
    }
    

    @Override
	public void visitFieldExpression(JFieldAccessExpression self,
                                     JExpression left,
                                     String ident) 
    {
        vars.add(ident);
    }

    @Override
	public void visitLocalVariableExpression(JLocalVariableExpression self,
                                             String ident) 
    {
        vars.add(self.getVariable());
    }
}
