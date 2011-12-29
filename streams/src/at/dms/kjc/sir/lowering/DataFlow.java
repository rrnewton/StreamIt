package at.dms.kjc.sir.lowering;

import java.util.HashMap;

import at.dms.kjc.JExpression;
import at.dms.kjc.JForStatement;
import at.dms.kjc.JIfStatement;
import at.dms.kjc.JStatement;
import at.dms.kjc.JSwitchGroup;
import at.dms.kjc.JSwitchStatement;
import at.dms.kjc.JWhileStatement;
import at.dms.kjc.SLIRReplacingVisitor;

/**
 * Basic dataflow framework. Extend and fill in transfer functions and merge function
 * to get functionality. Currently only works in forward direction.
 */
public abstract class DataFlow extends SLIRReplacingVisitor {
    //Gets a little awkward at times but alows for general behavior that can be inherited
    //and still fits in with their visitor framework
    protected HashMap map;
    
    public DataFlow() {
        map=new HashMap();
    }
    
    // ----------------------------------------------------------------------
    // STATEMENT
    // ----------------------------------------------------------------------
    
    /**
     * Visits a while statement
     */
    @Override
	public Object visitWhileStatement(JWhileStatement self,
                                      JExpression cond,
                                      JStatement body) {
        cond.accept(this);
        HashMap mapStore=(HashMap)map.clone();
        body.accept(this);
        mergeFunction(mapStore);
        while(!map.equals(mapStore)) { //Iterate until fixed point
            body.accept(this);
            mergeFunction(mapStore);
        }
        return self;
    }

    /**
     * Visits a if statement
     */
    @Override
	public Object visitIfStatement(JIfStatement self,
                                   JExpression cond,
                                   JStatement thenClause,
                                   JStatement elseClause) {
        cond.accept(this);
        HashMap mapStore=(HashMap)map.clone();
        thenClause.accept(this);
        HashMap mapThen=map;
        map=mapStore;
        elseClause.accept(this);
        mergeFunction(mapThen);
        return self;
    }
    
    /**
     * Visits a for statement
     */
    @Override
	public Object visitForStatement(JForStatement self,
                                    JStatement init,
                                    JExpression cond,
                                    JStatement incr,
                                    JStatement body) {
        init.accept(this);
        cond.accept(this);
        HashMap mapStore=(HashMap)map.clone();
        body.accept(this);
        incr.accept(this);
        mergeFunction(mapStore);
        while(!map.equals(mapStore)) { //Iterate until fixed point
            body.accept(this);
            incr.accept(this);
            mergeFunction(mapStore);
        }
        return self;
    }

    /**
     * Visits a switch statement
     */
    @Override
	public Object visitSwitchStatement(JSwitchStatement self,
                                       JExpression expr,
                                       JSwitchGroup[] body) {
        expr.accept(this);
        HashMap mapStore=(HashMap)map.clone();
        HashMap mapAccum=(HashMap)map.clone();
        for (int i = 0; i < body.length; i++) {
            body[i].accept(this);
            mergeFunction(mapAccum);
            mapAccum=map;
            map=mapStore;     
        }
        map=mapAccum;
        return self;
    }
    
    //Merges map2 with field map and stores in map
    //Override with applicable merge function
    protected abstract void mergeFunction(HashMap map2);

    //Override visit methods with appropriate merge functions
    //More default behavior to be included as I start extending
    //and noticing commonality
}
