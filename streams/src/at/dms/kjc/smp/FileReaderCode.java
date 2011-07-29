package at.dms.kjc.smp;

import java.util.LinkedList;
import java.util.List;

import at.dms.kjc.JStatement;
import at.dms.kjc.slir.InputNode;
import at.dms.kjc.slir.InterFilterEdge;
import at.dms.kjc.slir.OutputNode;
import at.dms.kjc.slir.SchedulingPhase;

public abstract class FileReaderCode {
    /** the output buffer is the source */
    protected InputRotatingBuffer parent;
    /** a file reader implementation may need synchronization if non-blocking*/
    protected List<JStatement> waitCallsSteady;
    /** the code for transferring */
    protected List<JStatement> commandsSteady;
    /** the code for transferring in the init */
    protected List<JStatement> commandsInit;
    /** the output slice node */
    protected InputNode input;
    /** any declarations that are needed */
    protected List<JStatement> decls;
    /** the output slice node of the file */
    protected OutputNode fileOutput;
    /** the edge between the file reader and this input buffer */
    protected InterFilterEdge edge;

    /** unique id generator */
    protected int id;
    protected static int uid = 0;
    
    public FileReaderCode(InputRotatingBuffer buf) {
        parent = buf;
        waitCallsSteady = new LinkedList<JStatement>();
        commandsSteady = new LinkedList<JStatement>();
        commandsInit = new LinkedList<JStatement>();
        decls = new LinkedList<JStatement>();
        input = parent.filterNode.getParent().getInputNode();
        fileOutput = input.getSingleEdge(SchedulingPhase.STEADY).getSrc();   
        if (input.oneInput(SchedulingPhase.INIT)) {
            assert input.getSources(SchedulingPhase.INIT)[0] == input.getSources(SchedulingPhase.STEADY)[0];
        }
        edge = input.getSingleEdge(SchedulingPhase.STEADY);
        id = uid++;
    }
    
    /**
     * Return the code that will transfer the items from the
     * output buffer to to appropriate input buffer(s)
     * 
     * @return the commands
     */
    public List<JStatement> getCode(SchedulingPhase which) {
        if (which == SchedulingPhase.INIT)
            return commandsInit;
        
        return commandsSteady;
    }
    
    /**
     * Return declarations of variables 
     * @return declarations of variables
     */
    public List<JStatement> decls() {
        return decls;
    }
    
    /**
     * Return statements that wait 
     * 
     * @return the wait statements
     */
    public List<JStatement> waitCallsSteady() {
        return waitCallsSteady;    
    }

    /**
     * Return unique id for FileReaderCode
     *
     * @return unique id
     */
    public int getID() {
        return id;
    }
}
