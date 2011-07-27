package at.dms.kjc.slir.fission;

import at.dms.kjc.slir.Filter;
import at.dms.kjc.backendSupport.FilterInfo;

public class FissionGroup {
    public Filter unfizzedSlice;
    public FilterInfo unfizzedFilterInfo;

    public Filter[] fizzedSlices;

    public FissionGroup(Filter unfizzedSlice, FilterInfo unfizzedFilterInfo, Filter[] fizzedSlices) {
        this.unfizzedSlice = unfizzedSlice;
        this.unfizzedFilterInfo = unfizzedFilterInfo;
        this.fizzedSlices = fizzedSlices;
    }

    public Filter getUnfizzedSlice() {
        return unfizzedSlice;
    }

    public FilterInfo getUnfizzedFilterInfo() {
        return unfizzedFilterInfo;
    }

    public Filter[] getFizzedSlices() {
        return fizzedSlices;
    }

    public int getFizzedSliceIndex(Filter slice) {
        int index = -1;
        for(int x = 0 ; x < fizzedSlices.length ; x++) {
            if(fizzedSlices[x].equals(slice)) {
                index = x;
                break;
            }
        }

        return index;
    }
}
