package at.dms.kjc.cell;

import at.dms.kjc.backendSupport.GetOrMakeChannel;
import at.dms.kjc.backendSupport.IntraSSGChannel;
import at.dms.kjc.slir.InterFilterEdge;
import at.dms.kjc.slir.IntraSSGEdge;

public class GetOrMakeCellChannel extends GetOrMakeChannel {

    public GetOrMakeCellChannel(CellBackendFactory backEndBits) {
        super(backEndBits);
    }
    
    @Override
    public IntraSSGChannel makeInterSliceChannel(InterFilterEdge e) {
        return new InterSPUChannel(e);
    }
    
    @Override
    public IntraSSGChannel makeIntraSliceChannel(IntraSSGEdge e) {
        return new InterSPUChannel(e);
    }
    
}
