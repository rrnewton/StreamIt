package at.dms.kjc.spacetime;

import java.util.ListIterator;
import at.dms.kjc.sir.*;
import at.dms.kjc.*;
import at.dms.kjc.spacetime.switchIR.*;
import at.dms.util.Utils;
import java.util.LinkedList;
import at.dms.kjc.flatgraph2.FilterContent;

/** This class will rawify the SIR code and it creates the 
 * switch code.  It does not rawify the compute code in place. 
**/
public class Rawify
{
    public static void run(ListIterator traces, RawChip rawChip,
			   boolean init) 
    {
	//iterate over the traces in the given order and generate the 
	//switch code, the tile code, and the off chip stuff for 
	//each TraceNode
	Trace trace;
	
	while (traces.hasNext()) {
	    trace = (Trace)traces.next();
	    //iterate over the TraceNodes
	    TraceNode traceNode = trace.getHead();
	    while (traceNode != null) {
		//do the appropiate code generation
		if (traceNode.isFilterTrace()) {
		    RawTile tile = rawChip.getTile(((FilterTraceNode)traceNode).getX(), 
						   ((FilterTraceNode)traceNode).getY());
		    //create the filter info class
		    FilterInfo filterInfo = FilterInfo.getFilterInfo((FilterTraceNode)traceNode);
		    //switch code for the trace
		    //generate switchcode based on the presence of buffering		    
		    int mult = (init) ? filterInfo.initMult : filterInfo.steadyMult;
		    
		    if(filterInfo.isLinear())
			createSwitchCodeLinear((FilterTraceNode)traceNode,
					       trace,filterInfo,init,false,tile,rawChip,mult);
		    else if (filterInfo.isDirect())
			createSwitchCode((FilterTraceNode)traceNode, 
					 trace, filterInfo, init, false, tile, rawChip, mult);
		    else
			createSwitchCodeBuffered((FilterTraceNode)traceNode, 
				     trace, filterInfo, init, tile, rawChip, mult);
		    //generate the compute code for the trace and place it in
		    //the tile
		    if (init) {
			//create the prime pump stage switch code 
			//after the initialization switch code
			createPrimePumpSwitchCode((FilterTraceNode)traceNode, 
				     trace, filterInfo, init, tile, rawChip);
			tile.getComputeCode().addTraceInit(filterInfo);
		    }
		    else
			tile.getComputeCode().addTraceSteady(filterInfo);
		}
		else if (traceNode instanceof EnterTraceNode) {
		    if (init) 
			openInputFile((EnterTraceNode)traceNode, rawChip);
		}
		else if (traceNode instanceof ExitTraceNode) {
		    if (init)
			openOutputFile((ExitTraceNode)traceNode, rawChip);
		}
		
		
		//get the next tracenode
		traceNode = traceNode.getNext();
	    }
	    
	}
	
    }

    private static void openInputFile(EnterTraceNode enterNode, RawChip rawChip) 
    {
	FilterTraceNode next = (FilterTraceNode)enterNode.getDests()[0][0].getNext();
	if (!rawChip.getTile(next.getX(), next.getY()).hasIODevice()) 
	    Utils.fail("Tile not connected to io device");
	MagicDram dram = (MagicDram)rawChip.getTile(next.getX(), next.getY()).getIODevice();
	dram.inputFiles.add(enterNode);
    }
    
    private static void openOutputFile(ExitTraceNode exitNode, RawChip rawChip) 
    {
	FilterTraceNode prev = (FilterTraceNode)exitNode.getSources()[0].getPrevious();
	if (!rawChip.getTile(prev.getX(), prev.getY()).hasIODevice()) 
	    Utils.fail("Tile not connected to io device");
	//get the dram
	MagicDram dram = (MagicDram)rawChip.getTile(prev.getX(), prev.getY()).getIODevice();
	dram.outputFiles.add(exitNode);
    }
    
    
    private static void createPrimePumpSwitchCode(FilterTraceNode node, Trace parent,
						  FilterInfo filterInfo,
						  boolean init, RawTile tile, RawChip rawChip) 
    {
	//call create switch code with init false (not in init) and primepump true
	createSwitchCode(node, parent, filterInfo, false, true, tile, rawChip, filterInfo.primePump);
    }
    
    private static void createMagicDramLoad(InputTraceNode node, FilterTraceNode next,
					    boolean init, RawChip rawChip) 
    {
	if (!rawChip.getTile(next.getX(), next.getY()).hasIODevice()) 
	    Utils.fail("Tile not connected to io device");
	
	MagicDram dram = (MagicDram)rawChip.getTile(next.getX(), next.getY()).getIODevice();
	
	LinkedList insList = init ? dram.initInsList : dram.steadyInsList;
	OutputTraceNode output = TraceBufferSchedule.getOutputBuffer(node);
	insList.add(new MagicDramLoad(node, output));
	dram.addBuffer(output, node);
    }

    /**
     * Generate a single magic dram store instruction for this output trace node
     **/
    private static void createMagicDramStore(OutputTraceNode node, FilterTraceNode prev, 
					     boolean init, RawChip rawChip)
					      
    {
	if (!rawChip.getTile(prev.getX(), prev.getY()).hasIODevice()) 
	    Utils.fail("Tile not connected to io device");
	//get the dram
	MagicDram dram = (MagicDram)rawChip.getTile(prev.getX(), prev.getY()).getIODevice();
	//get the list we should add to
	LinkedList insList = init ? dram.initInsList : dram.steadyInsList;
	//add the instruction
	insList.add(new MagicDramStore(node, 
				       TraceBufferSchedule.getInputBuffers(node)));
    }
    
    private static void createSwitchCodeLinear(FilterTraceNode node, Trace parent, 
					       FilterInfo filterInfo, boolean init, boolean primePump, 
					       RawTile tile, RawChip rawChip, int mult) {
	//createReceiveCode(0,  node,  parent,  filterInfo,  init,  primePump,  tile,  rawChip);
	ComputeNode sourceNode = null;
	if (node.getPrevious().isFilterTrace())
	    sourceNode = rawChip.getTile(((FilterTraceNode)node.getPrevious()).getX(),  
					 ((FilterTraceNode)node.getPrevious()).getY());
	else {
	    if (KjcOptions.magicdram && node.getPrevious() !=  null &&
		node.getPrevious().isInputTrace() &&
		tile.hasIODevice()) 
		sourceNode = tile.getIODevice();
	    else 
		return;
	}
	SwitchIPort src = rawChip.getIPort(sourceNode, tile);
	sourceNode = null;
	ComputeNode destNode = null;
	if (node.getNext().isFilterTrace())
	    destNode = rawChip.getTile(((FilterTraceNode)node.getNext()).getX(),  
				       ((FilterTraceNode)node.getNext()).getY());
	else {
	    if (KjcOptions.magicdram && node.getNext() !=  null &&
		node.getNext().isOutputTrace() && tile.hasIODevice())
		destNode = tile.getIODevice();
	    else
		return;
	}
	SwitchOPort dest = rawChip.getOPort(tile, destNode);
	destNode = null;
	FilterContent content = node.getFilter();
	final int peek = content.getArray().length;
	final int pop = content.getPopCount();
	final int numPop = peek/pop;
	System.out.println("SRC: "+src);
	System.out.println("DEST: "+dest);
	SwitchCodeStore code = tile.getSwitchCode();
	for(int i = 0; i<numPop-1; i++)
	    for(int j = 0; j<pop; j++) {
		FullIns ins = new FullIns(tile, new MoveIns(SwitchReg.R1, src));
		ins.addRoute(src, SwitchOPort.CSTI);
		code.appendIns(ins, init||primePump);
		for(int k = i-1; k>= 0; k--) {
		    FullIns newIns = new FullIns(tile);
		    newIns.addRoute(SwitchReg.R1, SwitchOPort.CSTI);
		    code.appendIns(newIns, init||primePump);
		}
	    }
	Label label = code.getFreshLabel();
	code.appendIns(label, init||primePump);
	final int numTimes = Linear.getMult(peek);
	for(int i = 0;i<numTimes*pop;i++) {
	    FullIns ins = new FullIns(tile, new MoveIns(SwitchReg.R1, src));
	    ins.addRoute(src, SwitchOPort.CSTI);
	    code.appendIns(ins, init||primePump);
	    for(int j = 1;j<numPop;j++) {
		FullIns newIns = new FullIns(tile);
		newIns.addRoute(SwitchReg.R1, SwitchOPort.CSTI);
		code.appendIns(newIns, init||primePump);
	    }
	}
	for(int i=0;i<numTimes-1;i++) {
	    FullIns newIns=new FullIns(tile);
	    newIns.addRoute(SwitchIPort.CSTO,dest);
	    code.appendIns(newIns,init||primePump);
	}
	FullIns newIns=new FullIns(tile,new JumpIns(label.getLabel()));
	newIns.addRoute(SwitchIPort.CSTO,dest);
	code.appendIns(newIns,init||primePump);
	//code.appendIns(new JumpIns(label.getLabel()),init||primePump);
    }

    private static void createSwitchCode(FilterTraceNode node, Trace parent, 
					 FilterInfo filterInfo,
					 boolean init, boolean primePump, RawTile tile,
					 RawChip rawChip, int mult) 
    {
	for (int i = 0; i < mult; i++) {
	    //append the receive code
	    if (generateSwitchCodeReceive(node) && node.getPrevious() != null)
		createReceiveCode(i, node, parent, filterInfo, init, primePump, tile, rawChip);
	    //append the send code 
	    if (generateSwitchCodeSend(node) && node.getNext() != null)
		createSendCode(i, node, parent, filterInfo, init, primePump, tile, rawChip);
	}
    }
    
    //determine whether we would generate switch code for this node, 
    //it may be doing internal inter-trace communication
    private static boolean generateSwitchCodeReceive(FilterTraceNode node) 
    {
	//always generate switch code for magic drams
	if (KjcOptions.magicdram)
	    return true;
	
	//otherwise only generate intra-trace switch code
	if (node.getPrevious() != null && node.getPrevious().isFilterTrace()) 
	    return true;
	
	return false;
    }
    
    //determine whether we would generate switch code for this node, 
    //it may be doing internal inter-trace communication
    private static boolean generateSwitchCodeSend(FilterTraceNode node) 
    {
	//always generate switch code for magic drams
	if (KjcOptions.magicdram)
	    return true;
	
	//otherwise only generate intra-trace switch code
	if (node.getNext() != null && node.getNext().isFilterTrace()) 
	    return true;
	
	return false;
    }
    
    private static void createSwitchCodeBuffered(FilterTraceNode node, Trace parent, 
						 FilterInfo filterInfo,
						 boolean init, RawTile tile,
						 RawChip rawChip, int mult) 
    {
	//create the switch code for each firing 
	createSwitchCode(node, parent, filterInfo, init, false, tile, rawChip, mult);
	
	//now we must take care of the remaining items on the input tape 
	//after the initialization phase if the upstream filter produces more than
	//we consume in init
	if (init && filterInfo.remaining > 0) {
	    for (int i = 0; 
		 i < filterInfo.remaining * Util.getTypeSize(node.getFilter().getInputType()); 
		 i++) {
		if (node.getPrevious().isFilterTrace()) {
		    RouteIns ins = new RouteIns(tile);
		    //add the route from the source tile to this
		    //tile's compute processor
		    ins.addRoute(rawChip.getTile(((FilterTraceNode)node.getPrevious()).getX(), 
						 ((FilterTraceNode)node.getPrevious()).getY()),
				 tile);
		    tile.getSwitchCode().appendIns(ins, init);
		}   
		else if (KjcOptions.magicdram) {
		    InputTraceNode input = (InputTraceNode)node.getPrevious();
		    createMagicDramLoad(input, node, init, rawChip);
		}
	    }
	}
    }
    
    
    
    private static void createReceiveCode(int iteration, FilterTraceNode node, Trace parent, 
				   FilterInfo filterInfo, boolean init, boolean primePump, RawTile tile,
				   RawChip rawChip) 
    {
	//if this is the init and it is the first time executing
	//and a twostage filter, use initpop and multiply this
	//by the size of the type it is receiving
	int itemsReceiving = filterInfo.itemsNeededToFire(iteration, init) *
	    Util.getTypeSize(node.getFilter().getInputType());

	//the source of the data, either a device or another raw tile
	ComputeNode sourceNode = null;
	
	if (node.getPrevious().isFilterTrace())
	    sourceNode = rawChip.getTile(((FilterTraceNode)node.getPrevious()).getX(), 
					 ((FilterTraceNode)node.getPrevious()).getY());
	else {
	    if (KjcOptions.magicdram && node.getPrevious() != null &&
		node.getPrevious().isInputTrace() &&
		tile.hasIODevice()) 
		sourceNode = tile.getIODevice();
	    else 
		return;
	}
	
	for (int j = 0; j < itemsReceiving; j++) {
	    RouteIns ins = new RouteIns(tile);
	    //add the route from the source tile to this
	    //tile's compute processor
	    ins.addRoute(sourceNode,
			 tile);
	    //append the instruction to the appropriate schedule
	    //for the primepump append to the end of the init stage
	    //so set final arg to true if init or primepump
	    tile.getSwitchCode().appendIns(ins, (init || primePump));
	    //if we are receiving from an inputtracenode and 
	    //magic dram is enabled, generate the magic dram load ins
	    if (KjcOptions.magicdram && node.getPrevious() != null &&
		node.getPrevious().isInputTrace())
		createMagicDramLoad((InputTraceNode)node.getPrevious(), 
				    node, (init || primePump), rawChip);
	    
	}
    }

    private static void createSendCode(int iteration, FilterTraceNode node, Trace parent, 
				       FilterInfo filterInfo, boolean init, boolean primePump, 
				       RawTile tile, RawChip rawChip) 
    {
	//get the items needed to fire and multiply it by the type 
	//size
	int items = filterInfo.itemsFiring(iteration, init) * 
	    Util.getTypeSize(node.getFilter().getOutputType());
	
	ComputeNode destNode = null;
	
	if (node.getNext().isFilterTrace())
	    destNode = rawChip.getTile(((FilterTraceNode)node.getNext()).getX(), 
				       ((FilterTraceNode)node.getNext()).getY());
	else {
	    if (KjcOptions.magicdram && node.getNext() != null &&
		node.getNext().isOutputTrace() && tile.hasIODevice())
		destNode = tile.getIODevice();
	    else
		return;
	}
	
	for (int j = 0; j < items; j++) {
	    RouteIns ins = new RouteIns(tile);
	    //add the route from this tile to the next trace node
	    ins.addRoute(tile, destNode);
	    //append the instruction
	    //for the primepump append to the end of the init stage
	    //so set final arg to true if init or primepump
	    tile.getSwitchCode().appendIns(ins, (init||primePump));
	    //if we are connected to an output trace node and 
	    //magicdram is enabled, create the magic dram store instuction
	    if (KjcOptions.magicdram && node.getNext() != null &&
		node.getNext().isOutputTrace())
		createMagicDramStore((OutputTraceNode)node.getNext(), 
				     node, (init || primePump), rawChip);
	}	
    }



}

