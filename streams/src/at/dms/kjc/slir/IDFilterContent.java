package at.dms.kjc.slir;

import at.dms.kjc.CType;
import at.dms.kjc.sir.SIRIdentity;

public class IDFilterContent extends WorkNodeContent {

    public IDFilterContent() {
        my_unique_ID = unique_ID++;
        name = "SliceID_" + my_unique_ID;
        peek = 1;
    }
    
    public IDFilterContent(SIRIdentity f) {
    	super(f);
    	my_unique_ID = unique_ID++;
        name = "SliceID_" + my_unique_ID;
        peek = 1;
    }
    
    @Override
	public void setInputType(CType type) {
        inputType = type;
    }
    
    @Override
	public void setOutputType(CType type) {
        outputType = type;
    }
    
    /**
     * Returns push amount.
     */
    @Override
	public int getPushInt() {
        return 1;
    }

    /**
     * Returns pop amount.
     */
    @Override
	public int getPopInt() {
        return 1;
    }

    /**
     * Returns peek amount.
     */
    @Override
	public int getPeekInt() {
        return 1;
    }

    /**
     * Returns push amount of init stage.
     * result may be garbage or error if !isTwoStage()
     */
    @Override
	public int getPreworkPush() {
        return 0;
    }

    /**
     * Returns pop amount of init stage.
     * result may be garbage or error if !isTwoStage()
     */
    @Override
	public int getPreworkPop() {
        return 0;
    }

    /**
     * Returns peek amount of init stage.
     * result may be garbage or error if !isTwoStage()
     */
    @Override
	public int getPreworkPeek() {
        return 0;
    }
  
    /**
     * Create and return a slice with a single ID filter.
     */
    public static Filter createIDSlice(StaticSubGraph ssg) {
        InputNode input = new InputNode();
        OutputNode output = new OutputNode();
        IDFilterContent id = new IDFilterContent();
        WorkNode filter = new WorkNode(id);
        
        input.setNext(filter);
        filter.setPrevious(input);
        filter.setNext(output);
        output.setPrevious(filter);
        
        Filter slice = new Filter(ssg);
        slice.setInputNode(input);
        slice.setOutputNode(output);
        slice.setWorkNode(filter);
        
        slice.setOutputNode(output);
        
        slice.finish();
        
        return slice;
    }
    
}
