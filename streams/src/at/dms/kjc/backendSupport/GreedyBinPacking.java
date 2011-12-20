/**
 * 
 */
package at.dms.kjc.backendSupport;

import at.dms.kjc.flatgraph.FlatNode;
import at.dms.kjc.flatgraph.GraphFlattener;
import at.dms.kjc.flatgraph.WorkSorted;
import at.dms.kjc.sir.*;
import java.util.*;
import at.dms.kjc.sir.lowering.partition.*;

/**
 * @author mgordon
 *
 */
public class GreedyBinPacking<T> {
    private int numBins;
    private LinkedList<T>[] bins;
    private HashMap<T, Integer> mapping;
    private long[] binWeight;
    private long maxBinWeight;
    
    public GreedyBinPacking(int numBins) {
        this.numBins = numBins;
        bins = new LinkedList[numBins];
        binWeight = new long[numBins];
        mapping = new HashMap<T, Integer>();
        for (int i = 0; i < numBins; i++) {
            bins[i] = new LinkedList<T>();
            binWeight[i] = 0;
        }
    }
    
    /**
     * Return the bin assigned to t
     * 
     * @param t The object 
     * @return return the bin
     */
    public int getBin(T t) {
    	return mapping.get(t).intValue();
    }

    public void pack(Map<T, Long> workEstimates) {
                
        //now sort the filters by work
    	WorkComparator<T> wc = new WorkComparator<T>(workEstimates);
    	TreeMap<T, Long> sortedMap = new TreeMap<T,Long>(wc);
    	sortedMap.putAll(workEstimates);
    	
        int filters = 0;
        //perform the packing
        for (T node : sortedMap.keySet()) {
        	filters++;
            /*if (node.contents instanceof SIRFileReader || 
                    node.contents instanceof SIRFileWriter)
                continue;
            */
   
            int bin = findMinBin();
            bins[bin].add(node);
            mapping.put(node, bin);
            binWeight[bin] += workEstimates.get(node);
        }
        //System.out.println("Packed " + filters + " filters.");
        maxBinWeight = -1;
        //find max bin
        for (int i = 0; i < numBins; i++)
            if (binWeight[i] > maxBinWeight) {
                maxBinWeight = binWeight[i];
            }
        
        /*
        for (int i = 0; i < numBins; i++) {
            System.out.println("Bin " + i + " (weight = " + binWeight[i] + "):");
            Iterator<SIRFilter> binIt = bins[i].iterator();
            while (binIt.hasNext()) {
                System.out.println("  " + binIt.next());
            }
        }
        */
    }
    
    public HashSet<Long> getCriticalPathTiles(double threshold) {
        HashSet<Long> cps = new HashSet<Long>();
        assert threshold > 0.0 && threshold < 1.0;
     
        double workThreshold = maxBinWeight * threshold;
        
        for (int i = 0; i < numBins; i++) {
            if (binWeight[i] >= workThreshold) {
                cps.add(new Long(i));
            }
        }

        return cps;
    }
    
    public HashSet<T> getCriticalpath(double threshold) {
        HashSet<T> cps = new HashSet<T>();
        assert threshold > 0.0 && threshold < 1.0;
     
        double workThreshold = maxBinWeight * threshold;
        
        for (int i = 0; i < numBins; i++) {
            if (binWeight[i] >= workThreshold) {
                Iterator<T> filters = bins[i].iterator();
                while (filters.hasNext()) {
                    cps.add(filters.next());
                }
            }
        }

        return cps;
    }
    
    private int findMinBin() {
        long minWeight = Long.MAX_VALUE;
        int minBin = -1;
        for (int i = 0; i < numBins; i++) 
            if (binWeight[i] < minWeight) {
                minBin = i;
                minWeight = binWeight[i];
            }
        return minBin;
    }
    
    public long maxBinWeight() {
        long maxBinWeight = 0;
        for (int i = 0; i < numBins; i++)
            if (binWeight[i] > maxBinWeight)
                maxBinWeight = binWeight[i];
        return maxBinWeight;
    }
}


class WorkComparator<T> implements Comparator<T>{
	private Map<T, Long> estimates;
	
	public WorkComparator(Map<T, Long> estimates) {
		this.estimates = estimates;
	}
	
	public int compare(T a, T b) {
		
	    if(estimates.get(a).longValue() < estimates.get(b).longValue()) {
	      return 1;
	    } else if(estimates.get(a).longValue() == estimates.get(b).longValue()) {
	      return 0;
	    } else {
	      return -1;
	    }
	  }

}