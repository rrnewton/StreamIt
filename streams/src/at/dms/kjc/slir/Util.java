package at.dms.kjc.slir;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;



/**
 *  A class with useful functions that span classes. 
 * 
 * 
**/
public class Util {

    /**
     * Get a traversal (linked list iterator) that includes all the slice nodes of the
     * given slice traversal.  Inserting or removing in the returned iterator will not
     * affect the list passed to sliceNodeTraversal.  Altering the individual SliceNode's
     * will alter the SliceNode's in the original list.
     * 
     * @param slices a list of Slice's.
     * @return An Iterator over SliceNode's.
     */
    public static Iterator<InternalFilterNode> sliceNodeTraversal(List<Filter> slices) {
        LinkedList<InternalFilterNode> trav = new LinkedList<InternalFilterNode>();
        ListIterator it = slices.listIterator();
    
        while (it.hasNext()) {
            Filter slice = (Filter) it.next();
          	trav.add(slice.getInputNode());
        	trav.add(slice.getWorkNode());
        	trav.add(slice.getOutputNode());
        }
    
        return trav.listIterator();
    }

    /**
     * Get a traversal (linked list) that includes the head nodes of the
     * given array of slices.
     * 
     * @param slices an array of Slice's.
     * @return An Iterator over SliceNodes.
     */
    public static Iterator<InternalFilterNode> sliceNodeTraversal(Filter[] slices) {
        LinkedList<InternalFilterNode> trav = new LinkedList<InternalFilterNode>();
    
        for (int i = 0; i < slices.length; i++) {
        	trav.add(slices[i].getInputNode());
        	trav.add(slices[i].getWorkNode());
        	trav.add(slices[i].getOutputNode());
        }
    
        return trav.listIterator();
    }

    /**
     * Given two slice nodes, return an edge.
     * Returns an existing edge from src to dst if one is found, else creates a new edge.
     * Only looks at src's edges to find one to return, not at dst's edges.
     * If the edge should be an InterSliceEdge, then the returned edge will be one.
     * Does not update the SliceNode's in any way.
     * @param src  Source SliceNode for edge
     * @param dst  Destination SliceNode for Edge
     * @return an InterSliceEdge or Edge from src to dst
     */
    public static InterFilterChannel srcDstToEdge(Filter src, Filter dst, SchedulingPhase phase) {

    	InterFilterChannel[][] edgesedges = src.getOutputNode().getDests(phase);
    	for (InterFilterChannel[] edges : edgesedges) {
    		for (InterFilterChannel edge : edges) {
    			assert edge.src.getParent() == src;
    			if (edge.dest.getParent() == dst) {
    				return edge;
    			}
    		}
    	}
    	return new InterFilterChannel(src.getOutputNode(), dst.getInputNode());

    }
    
    /**                                                                                                                                    
     * Return true if the sets contructed from list and array are equal.                                                                   
     * @param <T> Type of list elements and array elements.                                                                                
     *                                                                                                                                     
     * @param list The list                                                                                                                
     * @param array The array                                                                                                              
     * @return true if the sets contructed from list and array are equal.                                                                  
     */
    public static <T> boolean setCompare(LinkedList<T> list, T[] array) {
        if (array.length == list.size()) {
            HashSet<T> listSet = new HashSet<T>();
            HashSet<T> arraySet = new HashSet<T>();
            for (int i = 0; i < array.length; i++) {
                listSet.add(list.get(i));
                arraySet.add(array[i]);
            }
            return listSet.equals(arraySet);
        }
        return false;
    }

    /**                                                                                                                                    
     * Return true if the sets contructed from two arrays are equal.                                                                       
     * @param <T> Type of list elements and array elements.                                                                                
     *                                                                                                                                     
     * @param array1 First array                                                                                                           
     * @param array2 Second array                                                                                                          
     * @return true if the sets contructed from two arrays are equal.                                                                      
     */
    public static <T> boolean setCompare(T[] array1, T[] array2) {
        if (array1.length == array2.length) {
            HashSet<T> array1Set = new HashSet<T>();
            HashSet<T> array2Set = new HashSet<T>();
            for(int i = 0 ; i < array1.length ; i++) {
                array1Set.add(array1[i]);
                array2Set.add(array2[i]);
            }
            return array1Set.equals(array2Set);
        }
        return false;
    }

}