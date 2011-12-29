package at.dms.kjc.tilera;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import at.dms.classfile.Constants;
import at.dms.kjc.CClassType;
import at.dms.kjc.CStdType;
import at.dms.kjc.JArrayAccessExpression;
import at.dms.kjc.JAssignmentExpression;
import at.dms.kjc.JBlock;
import at.dms.kjc.JExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.JFieldAccessExpression;
import at.dms.kjc.JFormalParameter;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JLocalVariableExpression;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JPostfixExpression;
import at.dms.kjc.JStatement;
import at.dms.kjc.JThisExpression;
import at.dms.kjc.JVariableDeclarationStatement;
import at.dms.kjc.JVariableDefinition;
import at.dms.kjc.slir.InputNode;
import at.dms.kjc.slir.InterFilterEdge;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.slir.WorkNodeInfo;
import at.dms.kjc.tilera.arrayassignment.ArrayAssignmentStatements;

public class BufferRemoteWritesTransfers extends BufferTransfers {
        
    /** reference to head if this input buffer is shared as an output buffer */
    protected JExpression head;
    /** name of variable containing head of array offset */
    protected String writeHeadName;
    /** definition for head */
    protected JVariableDefinition writeHeadDefn;
    /** optimization: if this is true, then we can write directly to the remote input buffer
     * when pushing and not into the local buffer.  This only works when the src and dest are on
     * different tiles.
     */
    protected boolean directWrite = false;
    /** the address array of addresses into the local shared buffer (src buffer) in 
     * which to write for init
     */
    protected LinkedList<Integer> addressArrayInit;
    /** the address array of addresses into the local shared buffer (src buffer) in 
     * which to write for steady
     */
    protected LinkedList<Integer> addressArraySteady;
    protected static int uniqueID = 0;
    protected int myID;
    /** name of the address array pointer that is used in the push method,
     * points to either steady or init
     */
    protected String addrArrayPointer;
    /** name of the var that points to the address array for the steady */
    protected String addrArraySteadyVar;
    /** name of the var that points to the address array for the init */
    protected String addrArrayInitVar;
    /** true if this buffer's dest is a file writer */
    protected boolean fileWrite = false;
    protected static final String FAKE_IO_VAR = "__fake_output_var__";
    
    public BufferRemoteWritesTransfers(RotatingBuffer buf) {
        super(buf);
        myID = uniqueID++;
        
        //set up the head pointer for writing
        writeHeadName = buf.getIdent() + "head";
        writeHeadDefn = new JVariableDefinition(null,
                Constants.ACC_STATIC,
                CStdType.Integer, writeHeadName, null);
        
        head = new JFieldAccessExpression(writeHeadName);
        head.setType(CStdType.Integer);
      
        if (output.oneOutput(SchedulingPhase.STEADY) && 
                (output.oneOutput(SchedulingPhase.INIT) || output.noOutputs(SchedulingPhase.INIT)) &&
                TileraBackend.scheduler.getComputeNode(parent.filterNode) !=
                    TileraBackend.scheduler.getComputeNode(output.getSingleEdge(SchedulingPhase.STEADY).getDest().getNextFilter()) &&
                    //now make sure that it is single appearance or downstream has only one input for both steady and init
                    (output.getSingleEdge(SchedulingPhase.STEADY).getDest().singleAppearance() &&
                            //check steady for only one input downstream or only one rotation
                            (output.getSingleEdge(SchedulingPhase.STEADY).getDest().totalWeights(SchedulingPhase.STEADY) == 
                                WorkNodeInfo.getFilterInfo(output.getSingleEdge(SchedulingPhase.STEADY).getDest().getNextFilter()).totalItemsPopped(SchedulingPhase.STEADY) ||
                                output.getSingleEdge(SchedulingPhase.STEADY).getDest().oneInput(SchedulingPhase.INIT)) &&
                                //check the init stage
                                (output.noOutputs(SchedulingPhase.INIT) || (output.oneOutput(SchedulingPhase.INIT) &&
                                        output.getSingleEdge(SchedulingPhase.INIT).getDest().totalWeights(SchedulingPhase.INIT) == 
                                            WorkNodeInfo.getFilterInfo(output.getSingleEdge(SchedulingPhase.INIT).getDest().getNextFilter()).totalItemsPopped(SchedulingPhase.INIT) ||
                                            output.getSingleEdge(SchedulingPhase.INIT).getDest().oneInput(SchedulingPhase.INIT)))))
        {
            directWrite = true;
            if (output.getSingleEdge(SchedulingPhase.STEADY).getDest().getNextFilter().isFileOutput()) {
                fileWrite = true;
                decls.add(Util.toStmt("volatile " + buf.getType().toString() + " " + FAKE_IO_VAR));
            }
            
            assert !usesSharedBuffer();
        }
        
        decls.add(new JVariableDeclarationStatement(writeHeadDefn));
        
        generateStatements(SchedulingPhase.INIT);
        generateStatements(SchedulingPhase.STEADY);
    }
    
    public boolean usesSharedBuffer() {
        return parent instanceof InputRotatingBuffer;
    }
    
    /**
     * Add the static initializer of the address array for pushing into the input buffer of the
     * downstream filter.
     */
    private void addAddressArrayDecls(SchedulingPhase phase) {
        boolean init = (phase == SchedulingPhase.INIT); 
        LinkedList<Integer> addressArray = (init ? addressArrayInit : addressArraySteady);
        String varName = "__addArray_" + phase + myID + "__"; 
        
        if (init) {
            varName = addrArrayInitVar;
            decls.add(Util.toStmt("int *" + addrArrayPointer));
        } 
        else
            varName = addrArraySteadyVar;
        
        
        StringBuffer decl = new StringBuffer("int " + varName + "[] = {");
        if (addressArray != null) {
            for (int i = 0 ; i < addressArray.size(); i++) {
                decl.append(addressArray.get(i));
                if (i != addressArray.size() - 1)
                    decl.append(", ");
            }
        }
        decl.append("}");
        decls.add(Util.toStmt(decl.toString()));
    }
    
    private void generateStatements(SchedulingPhase phase) {
     
        WorkNode filter;
        //if we are directly writing, then the push method does the remote writes,
        //so no other remote writes are necessary
        if (directWrite)
            return;

        //if this is an input buffer shared as an output buffer, then the output
        //filter is the local src filter of this input buffer
        if (usesSharedBuffer()) {
            filter = ((InputRotatingBuffer)parent).getLocalSrcFilter();
        }
        else  //otherwise it is an output buffer, so use the parent's filter
            filter = parent.filterNode;


        WorkNodeInfo fi = WorkNodeInfo.getFilterInfo(filter);

        List<JStatement> statements = null;
        
        ArrayAssignmentStatements reorderStatements = new ArrayAssignmentStatements();
        
        switch (phase) {
            case INIT: statements = commandsInit; break;
            case PRIMEPUMP: assert false; break;
            case STEADY: statements = commandsSteady; break;
        }

        //no further code necessary if nothing is being produced
        if (fi.totalItemsSent(phase) == 0)
            return;
        
        assert fi.totalItemsSent(phase) % output.totalWeights(phase) == 0;
        
        //we might have to skip over some elements when we push into the buffer if this
        //is a shared buffer
        int writeOffset = getWriteOffset(phase);
       
        Tile sourceTile = TileraBackend.backEndBits.getLayout().getComputeNode(filter);
        
        int rotations = fi.totalItemsSent(phase) / output.totalWeights(phase);
        
        //first create an map from destinations to ints to index into the state arrays
        HashMap<InterFilterEdge, Integer> destIndex = new HashMap<InterFilterEdge, Integer>();
        int index = 0;
        int numDests = output.getDestSet(phase).size();
        int[][] destIndices = new int[numDests][];
        int[] nextWriteIndex = new int[numDests];
        
        for (InterFilterEdge edge : output.getDestSet(phase)) {
            destIndex.put(edge, index);
            destIndices[index] = getDestIndices(edge, rotations, phase);
            nextWriteIndex[index] = 0;
            index++;
        }
        
        int items = 0;
        for (int rot = 0; rot < rotations; rot++) {
            for (int weightIndex = 0; weightIndex < output.getWeights(phase).length; weightIndex++) {
                InterFilterEdge[] dests = output.getDests(phase)[weightIndex];
                for (int curWeight = 0; curWeight < output.getWeights(phase)[weightIndex]; curWeight++) {
                    int sourceElement= rot * output.totalWeights(phase) + 
                        output.weightBefore(weightIndex, phase) + curWeight + writeOffset;
                        items++;
                        for (InterFilterEdge dest : dests) {
                            int destElement = 
                                destIndices[destIndex.get(dest)][nextWriteIndex[destIndex.get(dest)]];
                            nextWriteIndex[destIndex.get(dest)]++;
                            Tile destTile = 
                                TileraBackend.backEndBits.getLayout().getComputeNode(dest.getDest().getNextFilter());
                            //don't do anything if this dest is on the same tiles, we are sharing the buffer with the
                            //dest, and the indices are the same.
                            
                            if (destTile == sourceTile && destElement == sourceElement && usesSharedBuffer()) 
                                continue;
                            
                            if (destTile == sourceTile) {
                                assert !usesSharedBuffer() : "Trying to reorder a single buffer! Could lead to race. " + filter;
                            }
                            
                            SourceAddressRotation addrBuf = parent.getAddressBuffer(dest.getDest());
                            reorderStatements.addAssignment(addrBuf.currentWriteBufName, "", destElement, 
                                    parent.currentWriteBufName, "", sourceElement);
                            //System.out.println("remoteWrites: " + addrBuf.currentWriteBufName + ", " + destElement + "; " + 
                            //		   parent.currentWriteBufName + ", " + sourceElement);
                           
                        }
                }
            }
        }

        assert items == fi.totalItemsSent(phase);
        
        //add the compressed assignment statements to the appropriate stage
        //these do the remote writes and any local copying needed
        statements.addAll(reorderStatements.toCompressedJStmts());   
    }
    
    private int[] getDestIndices(InterFilterEdge edge, int outputRots, SchedulingPhase phase) {
        int[] indices = new int[outputRots * output.getWeight(edge, phase)];
        InputNode input = edge.getDest();
        WorkNodeInfo dsFilter = WorkNodeInfo.getFilterInfo(input.getNextFilter());
        //System.out.println("Dest copyDown: "+dsFilter.copyDown);
        assert indices.length %  input.getWeight(edge, phase) == 0;
        
        int inputRots = indices.length / input.getWeight(edge, phase);
        int nextWriteIndex = 0;

        for (int rot = 0; rot < inputRots; rot++) {
            for (int index = 0; index < input.getWeights(phase).length; index++) {
                if (input.getSources(phase)[index] == edge) {
                    for (int item = 0; item < input.getWeights(phase)[index]; item++) {
                        indices[nextWriteIndex++] = rot * input.totalWeights(phase) +
                            input.weightBefore(index, phase) + item + 
                            (phase == SchedulingPhase.INIT ? 0 : dsFilter.copyDown);
                        //System.out.println("Dest index: " + indices[nextWriteIndex -1]);
                    }
                }
            }
        }
        
        assert nextWriteIndex == indices.length;
        
        return indices;
    }
    
    /** 
     * if we are using a shared buffer, then we have to start pushing over the
     * copy down and any non-local edges into the buffer
     */
    private int getWriteOffset(SchedulingPhase phase) {
        if (usesSharedBuffer()) {
            WorkNodeInfo localDest = WorkNodeInfo.getFilterInfo(parent.filterNode);
            
            //no address array needed but we have to set the head to the copydown plus
            //the weights of any inputs that are not mapped to this tile that appear before
            //the local source
            
            InputNode input = parent.filterNode.getParent().getInputNode();
            WorkNode localSrc = ((InputRotatingBuffer)parent).getLocalSrcFilter();
            //the local source and dest might not communicate in the init stage, if not
            //the offset should just be zero
            if (!input.hasEdgeFrom(phase, localSrc))
                return 0;
            
            InterFilterEdge theEdge = input.getEdgeFrom(phase, localSrc);
            int offset = input.weightBefore(theEdge, phase);
   
            //if we are not in the init, we must skip over the dest's copy down
            if (SchedulingPhase.INIT != phase) 
                offset += localDest.copyDown;
            return offset;
        } else
            return 0;
    }

    @Override
	public JStatement zeroOutHead(SchedulingPhase phase) {
        //if we have shared buffer, then we are using it for the output and input of filters
        //on the same tile, so we need to do special things to the head
        int literal = 0; 
        JBlock block = new JBlock();
        if (usesSharedBuffer()) {
                literal = getWriteOffset(phase);
        } else {
            if (directWrite) {
                InterFilterEdge edge = output.getSingleEdge(phase);
                //if we are directly writing then we have to get the index into the remote
                //buffer of start of this source
                
                //first make sure we actually write in this stage
                if (edge == null || !edge.getDest().getSourceSet(phase).contains(edge)) {
                    literal = 0;
                }
                else {
                    WorkNodeInfo destInfo = WorkNodeInfo.getFilterInfo(edge.getDest().getNextFilter());
                    literal = 
                        edge.getDest().weightBefore(edge, phase);
                    //if we are in the init, skip copy down as well
                    if (SchedulingPhase.INIT == phase)
                            literal += destInfo.copyDown;
                }
            } else {
                //no optimizations, just zero head so that we write to beginning of output buffer
                literal = 0;
            }
        }
        
        block.addStatement(new JExpressionStatement(
                new JAssignmentExpression(head, new JIntLiteral(literal))));
        
        return block;
    }

    @Override
	public JMethodDeclaration pushMethod() {
        JExpression bufRef = null;
        //set the buffer reference to the input buffer of the remote buffer that we are writing to
        if (directWrite) {
            bufRef = new JFieldAccessExpression(new JThisExpression(),  
                        parent.getAddressBuffer(output.getSingleEdge(SchedulingPhase.STEADY).getDest()).currentWriteBufName);
        }
        else   //not a direct write, so the buffer ref to the write buffer of the buffer
            bufRef = parent.writeBufRef();
        
        String valName = "__val";
        JFormalParameter val = new JFormalParameter(
                parent.getType(),
                valName);
        JLocalVariableExpression valRef = new JLocalVariableExpression(val);
        JBlock body = new JBlock();

        if (fileWrite && TileraBackend.FAKE_IO) {
            //if we are faking the io and this writes to a file writer assign val to volatile value
            body.addStatement(Util.toStmt(FAKE_IO_VAR + " = " + valName));
        } else {
            //otherwise generate buffer assignment
            body.addStatement(
                    new JExpressionStatement(new JAssignmentExpression(
                            new JArrayAccessExpression(bufRef, new JPostfixExpression(at.dms.kjc.Constants.OPE_POSTINC,
                                    head)),
                                    valRef)));
        }

        
        JMethodDeclaration retval = new JMethodDeclaration(
                null,
                /*at.dms.kjc.Constants.ACC_PUBLIC | at.dms.kjc.Constants.ACC_STATIC |*/ Constants.ACC_INLINE,
                CStdType.Void,
                parent.pushMethodName(),
                new JFormalParameter[]{val},
                CClassType.EMPTY,
                body, null, null);
        
        
        return retval;
    }
    
    
}
