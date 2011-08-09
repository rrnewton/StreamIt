package at.dms.kjc.smp;

import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.WorkNodeInfo;
import at.dms.kjc.slir.fission.FissionGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FissionGroupStore {

    private static HashSet<FissionGroup> fissionGroups;

    private static HashMap<Filter, FissionGroup> unfizzedToFissionGroup;
    private static HashMap<Filter, FissionGroup> fizzedToFissionGroup;

    static {
        fissionGroups = new HashSet<FissionGroup>();

        unfizzedToFissionGroup = new HashMap<Filter, FissionGroup>();
        fizzedToFissionGroup = new HashMap<Filter, FissionGroup>();
    }

    public static void addFissionGroup(FissionGroup group) {
        fissionGroups.add(group);

        unfizzedToFissionGroup.put(group.unfizzedSlice, group);

        for(Filter slice : group.fizzedSlices)
            fizzedToFissionGroup.put(slice, group);
    }

    public static Set<FissionGroup> getFissionGroups() {
        return fissionGroups;
    }

    public static FissionGroup getFissionGroup(Filter slice) {
        if(unfizzedToFissionGroup.containsKey(slice))
            return unfizzedToFissionGroup.get(slice);

        if(fizzedToFissionGroup.containsKey(slice))
            return fizzedToFissionGroup.get(slice);

        return null;
    }

    public static boolean isFizzed(Filter slice) {
        return unfizzedToFissionGroup.containsKey(slice) ||
            fizzedToFissionGroup.containsKey(slice);
    }

    public static boolean isUnfizzedSlice(Filter slice) {
        return unfizzedToFissionGroup.containsKey(slice);
    }

    public static boolean isFizzedSlice(Filter slice) {
        return fizzedToFissionGroup.containsKey(slice);
    }

    public static Filter getUnfizzedSlice(Filter slice) {
        if(unfizzedToFissionGroup.containsKey(slice))
            return slice;

        if(fizzedToFissionGroup.containsKey(slice))
            return fizzedToFissionGroup.get(slice).unfizzedSlice;

        return null;
    }

    public static WorkNodeInfo getUnfizzedFilterInfo(Filter slice) {
        return getFissionGroup(slice).unfizzedFilterInfo;
    }

    public static Filter[] getFizzedSlices(Filter slice) {
        if(unfizzedToFissionGroup.containsKey(slice))
            return unfizzedToFissionGroup.get(slice).fizzedSlices;

        if(fizzedToFissionGroup.containsKey(slice))
            return fizzedToFissionGroup.get(slice).fizzedSlices;

        return null;
    }

    public static int getFizzedSliceIndex(Filter slice) {
        return getFissionGroup(slice).getFizzedSliceIndex(slice);
    }

    public static void reset() {
        fissionGroups.clear();
        unfizzedToFissionGroup.clear();
        fizzedToFissionGroup.clear();
    }
}
