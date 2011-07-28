package at.dms.kjc.cell;

import at.dms.kjc.backendSupport.Channel;
import at.dms.kjc.backendSupport.GetOrMakeChannel;
import at.dms.kjc.slir.Edge;
import at.dms.kjc.slir.InterFilterEdge;

public class GetOrMakeCellChannel extends GetOrMakeChannel {

    public GetOrMakeCellChannel(CellBackendFactory backEndBits) {
        super(backEndBits);
    }
    
    @Override
    public Channel makeInterSliceChannel(InterFilterEdge e) {
        return new InterSPUChannel(e);
    }
    
    @Override
    public Channel makeIntraSliceChannel(Edge e) {
        return new InterSPUChannel(e);
    }
    
}
