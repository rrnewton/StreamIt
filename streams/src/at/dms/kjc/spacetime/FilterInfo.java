package at.dms.kjc.spacetime;

//import at.dms.kjc.sir.*;
import at.dms.kjc.slicegraph.Edge;
import at.dms.kjc.slicegraph.FilterSliceNode;
import at.dms.kjc.slicegraph.InputSliceNode;
import at.dms.kjc.slicegraph.FilterContent;
//import at.dms.util.Utils;
//import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;

/**
 * A class to hold all the various information for a filter.
 * 
 */
public class FilterInfo {
    public int prePeek;

    public int prePop;

    public int prePush;

    public int remaining;

    public int bottomPeek;

    public int initMult;

    public int steadyMult;

    public int push;

    public int pop;

    public int peek;

    public int initItemsNeeded;
    
    private boolean linear;

    public FilterSliceNode sliceNode;

    public FilterContent filter;

    /** HashMap of all the filter infos FilterSliceNode -> FilterInfo */
    private static HashMap<FilterSliceNode, FilterInfo> filterInfos;

    // true if everything is set and we can use this class
    // because once a filter info is created you cannot
    // change the underlying filter...
    private static boolean canuse;

    static {
        filterInfos = new HashMap<FilterSliceNode, FilterInfo>();
        canuse = false;
    }

    /** 
     * Call this when it is safe to use filter infos, meaning all the 
     * information that they collect has been calculated so the information
     * can be presented.
     *
     */
    public static void canUse() {
        canuse = true;
    }

    /**
     * Force the filter info to be recalculated.
     *
     */
    public static void reset() {
        filterInfos = new HashMap<FilterSliceNode, FilterInfo>();
    }
    
    public static FilterInfo getFilterInfo(FilterSliceNode sliceNode) {
        assert canuse;
        if (!filterInfos.containsKey(sliceNode)) {
            FilterInfo info = new FilterInfo(sliceNode);
            filterInfos.put(sliceNode, info);
            return info;
        } else
            return filterInfos.get(sliceNode);
    }

    private FilterInfo(FilterSliceNode sliceNode) {
        filter = sliceNode.getFilter();
        this.sliceNode = sliceNode;
        this.steadyMult = filter.getSteadyMult();
        this.initMult = filter.getInitMult();
        // multiply the primepump number by the
        // steady state multiplicity to get the true
        // primepump multiplicity
        prePeek = 0;
        prePush = 0;
        prePop = 0;
        linear = filter.isLinear();
        if (linear) {
            peek = filter.getArray().length;
            push = 1;
            pop = filter.getPopCount();
            calculateRemaining();
        } else if (sliceNode.isFileInput()) {
            push = 1;
            pop = 0;
            peek = 0;
            calculateRemaining();
        } else if (sliceNode.isFileOutput()) {
            push = 0;
            pop = 1;
            peek = 0;
            calculateRemaining();
        } else {
            push = filter.getPushInt();
            pop = filter.getPopInt();
            peek = filter.getPeekInt();
            if (isTwoStage()) {
                prePeek = filter.getInitPeek();
                prePush = filter.getInitPush();
                prePop = filter.getInitPop();
            }
            calculateRemaining();
        }
    }
    
    public boolean isTwoStage() {
        return filter.isTwoStage();
    }

    private int calculateRemaining() {
        // the number of times this filter fires in the initialization
        // schedule
        int initFire = initMult;

        // if this is not a twostage, fake it by adding to initFire,
        // so we always think the preWork is called
        // if (!(filter instanceof SIRTwoStageFilter))
        if (!filter.isTwoStage())
            initFire++;

        // see my thesis for an explanation of this calculation
        if (initFire - 1 > 0) {
            bottomPeek = Math.max(0, peek - (prePeek - prePop));
        } else
            bottomPeek = 0;

        // don't call initItemsReceived() here it
        // may cause an infinite loop because it creates filter infos
        int initItemsRec = 0;
        if (sliceNode.getPrevious().isFilterSlice()) {
            FilterContent filterC = ((FilterSliceNode) sliceNode.getPrevious())
                .getFilter();
            initItemsRec = filterC.getPushInt() * filterC.getInitMult();
            if (filterC.isTwoStage()) {
                initItemsRec -= filterC.getPushInt();
                initItemsRec += filterC.getInitPush();
            }
        } else { // previous is an input slice
            InputSliceNode in = (InputSliceNode) sliceNode.getPrevious();

            // add all the upstream filters items that reach this filter
            for (int i = 0; i < in.getWeights().length; i++) {
                Edge incoming = in.getSources()[i];
                FilterContent filterC = ((FilterSliceNode) incoming.getSrc()
                                         .getPrevious()).getFilter();
                // calculate the init items sent by the upstream filter
                int upstreamInitItems = 0;
                upstreamInitItems = filterC.getPushInt()
                    * filterC.getInitMult();
                if (filterC.isTwoStage()) {
                    upstreamInitItems -= filterC.getPushInt();
                    upstreamInitItems += filterC.getInitPush();
                }
                /*
                 * System.out.println("Upstream: " + filterC);
                 System.out.println("push: " + filterC.getPushInt() + 
                 " init Mult: " + filterC.getInitMult());
                 System.out.println("Upstream: " + upstreamInitItems + " Ratio: " + 
                 incoming.getSrc().ratio(incoming));
                 */
                initItemsRec += (int) (((double) upstreamInitItems) * incoming
                                       .getSrc().ratio(incoming));
            }
        }
        
        initItemsNeeded = (prePeek + bottomPeek + Math.max((initFire - 2), 0) * pop); 
        
        remaining = initItemsRec
            - initItemsNeeded;

        assert remaining >= 0 : filter.getName()
            + ": Error calculating remaining " + initItemsRec + " < "
            + initItemsNeeded;
        return remaining;
    }

    public boolean isLinear() {
        return linear;
    }

    // does this filter require a receive buffer during code
    // generation
    public boolean noBuffer() {
        if (peek == 0 && prePeek == 0)
            return true;
        return false;
    }

    // can we use a simple (non-circular) receive buffer for this filter
    public boolean isSimple() {
        if (noBuffer())
            return false;

        if (peek == pop && remaining == 0 && (prePop == prePeek))
            return true;
        return false;
    }

    // return the number of items produced in the init stage
    public int initItemsSent() {
        int items = push * initMult;
        if (isTwoStage()) {
            /*
             * upStreamItems -=
             * ((SIRTwoStageFilter)previous.getFilter()).getPushInt();
             * upStreamItems +=
             * ((SIRTwoStageFilter)previous.getFilter()).getInitPush();
             */
            items -= push;
            items += prePush;
        }
        return items;
    }

    /**
     * Return the number of items received in the init stage including
     * the remaining items on the tape that are not consumed in the
     * schedule.
     * 
     * @return The number of items received in the init stage.
     */
    public int initItemsReceived() {
        return initItemsReceived(false);
    }
    
    /**
     * Return the number of items received in the init stage including
     * the remaining items on the tape that are not consumed in the
     * schedule.
     * 
     * @param debug if true, print debug info.
     * 
     * @return The number of items received in the init stage.
     */
    public int initItemsReceived(boolean debug) {
        // the number of items produced by the upstream filter in
        // initialization
        int upStreamItems = 0;

        if (debug)
            System.out.println("*****  Init items received " + this + " *****");
        
        if (sliceNode.getPrevious().isFilterSlice()) {
            upStreamItems = 
                FilterInfo.getFilterInfo(
                        (FilterSliceNode) sliceNode.getPrevious()).initItemsSent();
            if (debug)
                System.out.println(" Upstream filter sends: " + upStreamItems);
        } else { // previous is an input slice
            InputSliceNode in = (InputSliceNode) sliceNode.getPrevious();
            if (debug)
                System.out.println(" Upstream input node:");
            // add all the upstream filters items that reach this filter
            Iterator<Edge> edges = in.getSourceSet().iterator();
            while (edges.hasNext()) {
                Edge incoming = edges.next();
                upStreamItems += 
                    (int) 
                    ((double)FilterInfo.getFilterInfo((FilterSliceNode)incoming.getSrc().getPrevious())
                            .initItemsSent() * incoming.getSrc().ratio(incoming));
                if (debug) {
                    System.out.println("   " + incoming + ": sends " + 
                            FilterInfo.getFilterInfo((FilterSliceNode)incoming.getSrc().getPrevious())
                            .initItemsSent() + ", at ratio " + incoming.getSrc().ratio(incoming) + " = " +
                            (int) 
                            ((double)FilterInfo.getFilterInfo((FilterSliceNode)incoming.getSrc().getPrevious())
                                    .initItemsSent() * incoming.getSrc().ratio(incoming)));
                }
                            //((double) incoming.getSrc()
                            //        .getWeight(incoming) / incoming.getSrc().totalWeights()));
                // upStreamItems +=
                // (int)(FilterInfo.getFilterInfo(previous[i]).initItemsSent() *
                // ((double)out.getWeight(in) / out.totalWeights()));
            }
        }
        if (debug)
            System.out.println("*****");
        return upStreamItems;
    }


    /**
     * get the total number of items received during the execution of the stage
     * we are in (based on <pre>init</pre> and <pre>primepump</pre>.  For primepump, this is just
     * for one firing of the parent slice in the primepump stage, the slice may fire
     * many times in the prime pump schedule to fill the rotating buffers.
     * 
     * @param init
     * @param primepump
     * @return total number of items received
     */
    public int totalItemsReceived(boolean init, boolean primepump) {
        assert !((init) && (init && primepump)) : "incorrect usage";
        int items = 0;
        // get the number of items received
        if (init)
            items = initItemsReceived();
        else if (primepump)
            items = steadyMult * pop;
        else
            items = steadyMult * pop;

        return items;
    }

    /**
     * get the total number of itmes sent during the execution of the stage
     * we are in (based on <pre>init</pre> and <pre>primepump</pre>.  For primepump, this is just
     * for one firing of the parent slice in the primepump stage, the slice may fire
     * many times in the prime pump schedule to fill the rotating buffers.
     * 
     * @param init
     * @param primepump
     * @return total number of itmes sent
     */
    public int totalItemsSent(boolean init, boolean primepump) {
        assert !((init) && (init && primepump)) : "incorrect usage";
        int items = 0;
        if (init)
            items = initItemsSent();
        else if (primepump)
            items = steadyMult * push;
        else
            items = steadyMult * push;
        return items;
    }

    /**
     * @param init
     * @param primepump
     * @return The multiplicity of the filter in the given stage, return
     * the steady mult for the primepump stage.
     */
    public int getMult(boolean init, boolean primepump) {
        assert !((init) && (init && primepump)) : "incorrect usage";
        if (init)
            return initMult;
        else 
            return steadyMult; 
    }
    
    
    
    /**
     * @param exeCount The iteration we are querying. 
     * @param init Init stage?
     * @return The number of items this filter will produce.
     */
    public int itemsFiring(int exeCount, boolean init) {
        int items = push;

        if (init && exeCount == 0 && isTwoStage())
            items = prePush;

        return items;
    }

    /**
     * @param exeCount The current execution we are querying of the filter.
     * @param init Is this the init stage?
     * @return The number of items needed for fire this filter in the given stage at 
     * at the given iteration.
     */
    public int itemsNeededToFire(int exeCount, boolean init) {
        int items = pop;

        // if we and this is the first execution we need either peek or initPeek
        if (init && exeCount == 0) {
            if (isTwoStage())
                items = prePeek;
            else
                items = peek;
        }

        return items;
    }

    public String toString() {
        return sliceNode.toString();
    }

    /*
     * Not needed now, but needed for magic crap public FilterSliceNode[]
     * getNextFilters() { FilterSliceNode[] ret;
     * 
     * if (sliceNode.getNext() == null) return new FilterSliceNode[0]; else if
     * (sliceNode.getNext().isFilterSlice()) { ret = new FilterSliceNode[1];
     * ret[0] = (FilterSliceNode)sliceNode.getNext(); } else { //output slice
     * node HashSet set = new HashSet(); OutputSliceNode output =
     * (OutputSliceNode)sliceNode.getNext(); for (int i = 0; i <
     * output.getDests().length; i++) for (int j = 0; j <
     * output.getDests()[i].length; j++)
     * set.add(output.getDests()[i][j].getNext()); ret =
     * (FilterSliceNode[])set.toArray(new FilterSliceNode[0]); } return ret; }
     * 
     * 
     * //for the filter slice node, get all upstream filter slice nodes, //going
     * thru input and output slice nodes public FilterSliceNode[]
     * getPreviousFilters() { FilterSliceNode[] ret;
     * 
     * if (sliceNode.getPrevious() == null) return new FilterSliceNode[0];
     * 
     * if (sliceNode.getPrevious().isFilterSlice()) { ret = new
     * FilterSliceNode[1]; ret[0] = (FilterSliceNode)sliceNode.getPrevious(); }
     * else { //input slice node InputSliceNode input =
     * (InputSliceNode)sliceNode.getPrevious();
     * 
     * //here we assume each slice has at least one filter slice node ret = new
     * FilterSliceNode[input.getSources().length]; for (int i = 0; i <
     * ret.length; i++) { ret[i] =
     * (FilterSliceNode)input.getSources()[i].getSrc().getPrevious(); } } return
     * ret; }
     */
}
