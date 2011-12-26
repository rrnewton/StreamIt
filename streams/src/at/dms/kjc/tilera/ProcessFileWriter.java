package at.dms.kjc.tilera;

import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import at.dms.kjc.JBlock;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.slir.*;

public class ProcessFileWriter {
    private static int totalOutputs = 0;
    protected WorkNode filterNode;
    protected SchedulingPhase phase;
    protected TileraBackEndFactory factory;
    protected TileCodeStore codeStore;
    protected FileOutputContent fileOutput;
    protected static HashMap<WorkNode, Tile> allocatingTiles;
    protected Tile allocatingTile; 

    static {
        allocatingTiles = new HashMap<WorkNode, Tile>();
    }
    
    public ProcessFileWriter (WorkNode filter, SchedulingPhase phase, TileraBackEndFactory factory) {
        this.filterNode = filter;
        this.fileOutput = (FileOutputContent)filter.getWorkNodeContent();
        this.phase = phase;
        this.factory = factory;
    }
    
    public static int getTotalOutputs() {
        return totalOutputs;
    }
    
    public static Set<WorkNode> getFileWriterFilters() {
        return allocatingTiles.keySet();
    }
    
    /**
     * Return the tile that this file writer's buffer should be allocated on.
     * @param fo  The file writer
     */
    public static Tile getAllocatingTile(WorkNode fo) {
        assert fo.isFileOutput();
        
        if (!allocatingTiles.containsKey(fo)) {
            Tile allocatingTile = nextAllocatingTile(fo);
            System.out.println(fo + " assigned to Tile " + allocatingTile.getTileNumber());
            allocatingTiles.put(fo, allocatingTile);
        }
        
        return allocatingTiles.get(fo);
    }
    
    /** 
     * Decide on the allocating tile for the file writer and create the shared, uncacheable heap
     * on that tile the output will be written to.
     */
    public void processFileWriter() {
        //do nothing if faking io
        if (phase == SchedulingPhase.INIT) {
            int outputs = filterNode.getWorkNodeContent().getSteadyMult();
            System.out.println("Outputs for " + filterNode + ": " + outputs);
            totalOutputs += outputs;
            assert allocatingTiles.containsKey(filterNode);
            allocatingTile = allocatingTiles.get(filterNode);
            codeStore = allocatingTile.getComputeCode();
                        
            JBlock block = new JBlock();
            //create the heap with shared and uncacheable attributes
            codeStore.appendTxtToGlobal("ilibHeap fileWriteHeap;\n");
            block.addStatement(Util.toStmt("int flags = ILIB_MEM_SHARED | ILIB_MEM_UNCACHEABLE"));
            block.addStatement(Util.toStmt("ilib_mem_create_heap(flags, &fileWriteHeap)"));
            codeStore.addStatementFirstToBufferInit(block);
        }
    }
    
    /**
     * @return The tile we should allocate this file reader on.  Remember that 
     * the file reader is allocated to off-chip memory.  We just cycle through the tiles
     * if there is more than one file reader, one reader per tile.
     */
    private static Tile nextAllocatingTile(WorkNode fo) {
        List<Tile> reverseOrder = TileraBackend.chip.getAbstractTiles(); 
        Collections.reverse(reverseOrder);
        for (Tile tile : reverseOrder) {
            if (!allocatingTiles.containsValue(tile)) {
                allocatingTiles.put(fo, tile);
                return tile;
            }
        }
        assert false : "Too many file readers for this chip (one per tile)!";
        return null;
    }
}
