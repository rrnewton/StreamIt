package at.dms.kjc.slir.fission;

import java.util.*;
import at.dms.kjc.*;
import at.dms.kjc.slir.*;

public class FissionEdgeMemoizer {
    private static HashMap<EdgeDescriptor, InterSliceEdge> edges =
        new HashMap<EdgeDescriptor, InterSliceEdge>();

    public static void reset() {
        edges.clear();
    }

    public static void addEdge(InterSliceEdge edge) {
        EdgeDescriptor edgeDscr = new EdgeDescriptor(edge);

        edges.put(edgeDscr, edge);
    }

    public static InterSliceEdge getEdge(OutputSliceNode src, InputSliceNode dest) {
        EdgeDescriptor edgeDscr = new EdgeDescriptor(src, dest);      

        InterSliceEdge edge = edges.get(edgeDscr);

        if(edge == null) {
            edge = new InterSliceEdge(src, dest);
            edges.put(edgeDscr, edge);
        }

        return edge;
    }

    public static InterSliceEdge getEdge(Filter src, Filter dest) {
        return getEdge(src.getTail(), dest.getHead());
    }

    private static class EdgeDescriptor {
        public OutputSliceNode src;
        public InputSliceNode dest;

        public EdgeDescriptor(OutputSliceNode src, InputSliceNode dest) {
            this.src = src;
            this.dest = dest;
        }
        
        public EdgeDescriptor(Filter src, Filter dest) {
            this(src.getTail(), dest.getHead());
        }

        public EdgeDescriptor(InterSliceEdge edge) {
            this(edge.getSrc(), edge.getDest());
        }
        
        public boolean equals(Object obj) {
            if(obj instanceof EdgeDescriptor) {
                EdgeDescriptor edge = (EdgeDescriptor)obj;
                
                if(this.src.equals(edge.src) &&
                   this.dest.equals(edge.dest))
                    return true;
                
                return false;
            }
            
            return false;
        }
        
        public int hashCode() {
            return src.hashCode() + dest.hashCode();
        }
    }
}