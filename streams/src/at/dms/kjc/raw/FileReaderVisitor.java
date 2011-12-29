package at.dms.kjc.raw;

import java.util.HashSet;
import java.util.Iterator;

import at.dms.kjc.flatgraph.FlatNode;
import at.dms.kjc.flatgraph.FlatVisitor;
import at.dms.kjc.sir.SIRFileReader;


public class FileReaderVisitor implements FlatVisitor {
    public static boolean foundReader;
    public static HashSet<FlatNode> fileReaders;
    
    public static void init(FlatNode top) {
        FileReaderVisitor frv = new FileReaderVisitor();
        top.accept(frv, new HashSet<FlatNode>(), false);
    }
    
    public FileReaderVisitor() 
    {
        foundReader = false;
        fileReaders = new HashSet<FlatNode>();
    }
    
    public void visitNode (FlatNode node) 
    {
        if (node.contents instanceof SIRFileReader) {
            fileReaders.add(node);
            foundReader = true;
        }
    }

    public static boolean connectedToFR(Coordinate tile) {
        Iterator<FlatNode> frs = fileReaders.iterator();
        while (frs.hasNext()) {
            if (Layout.areNeighbors(tile, Layout.getTile(frs.next()))) 
                return true;
        }
        return false;
    }
}
