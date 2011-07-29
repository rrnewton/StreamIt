package at.dms.kjc.slir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class OneFilterSlicer extends Slicer {


    public OneFilterSlicer(UnflatFilter[] topFilters, HashMap[] exeCounts) {
        super(topFilters, exeCounts);
    }

    public Filter[] partition() {
        LinkedList<UnflatFilter> queue = new LinkedList<UnflatFilter>();
        HashSet<UnflatFilter> visited = new HashSet<UnflatFilter>();
        LinkedList<Filter> slices = new LinkedList<Filter>();
        LinkedList<Filter> topSlicesList = new LinkedList<Filter>(); // slices with no
        // incoming dependencies
        HashSet<UnflatFilter> topUnflat = new HashSet<UnflatFilter>();

        // map unflatEdges -> Edge?
        HashMap<UnflatEdge, InterFilterEdge> edges = new HashMap<UnflatEdge, InterFilterEdge>();
        // add the top filters to the queue
        for (int i = 0; i < topFilters.length; i++) {
            topUnflat.add(topFilters[i]);
            queue.add(topFilters[i]);
        }

        while (!queue.isEmpty()) {
            UnflatFilter unflatFilter = queue.removeFirst();
            if (!visited.contains(unflatFilter)) {
                visited.add(unflatFilter);
                // the filter content for the new filter
                WorkNodeContent filterContent = getFilterContent(unflatFilter);
                // remember the work estimation based on the filter content

                SliceNode node;
                Filter slice;
                int filtersInSlice = 1;

                //System.out.println("** Creating slice with first filter = "
                //                   + filterContent);

                // create the input slice node
                if (unflatFilter.in != null && unflatFilter.in.length > 0) {
                    InterFilterEdge[] inEdges = new InterFilterEdge[unflatFilter.in.length];
                    node = new InputNode(unflatFilter.inWeights, inEdges);
                    for (int i = 0; i < unflatFilter.in.length; i++) {
                        UnflatEdge unflatEdge = unflatFilter.in[i];
                        // get the edge
                        InterFilterEdge edge = edges.get(unflatEdge);
                        // we haven't see the edge before
                        if (edge == null) { // set dest?, wouldn't this always
                            // be the dest
                            edge = new InterFilterEdge((InputNode) node);
                            edges.put(unflatEdge, edge);
                        } else {
                            // we've seen this edge before, set the dest to this
                            // node
                            if (edge.getDest() != node)
                                edge.setDest((InputNode) node);
                        }
                        inEdges[i] = edge;
                    }
                    slice = new Filter((InputNode) node);


                    WorkNode filterNode = new WorkNode(filterContent);
                    node.setNext(filterNode);
                    filterNode.setPrevious(node);
                    node = filterNode;

                } else { // null incoming arcs
                    node = new WorkNode(filterContent);
                    slice = new Filter(node);
                }

                if (topUnflat.contains(unflatFilter)) {
                    assert unflatFilter.in == null
                    || unflatFilter.in.length == 0;
                    topSlicesList.add(slice);
                } else
                    assert unflatFilter.in.length > 0;

                    // should be at least one filter in the slice by now, don't
                    // worry about
                    // linear stuff right now...

                    slices.add(slice);

                    // we are finished the current slice, create the outputslicenode
                    if (unflatFilter.out != null && unflatFilter.out.length > 0) {
                        InterFilterEdge[][] outEdges = new InterFilterEdge[unflatFilter.out.length][];
                        OutputNode outNode = new OutputNode(
                                unflatFilter.outWeights, outEdges);
                        node.setNext(outNode);
                        outNode.setPrevious(node);
                        for (int i = 0; i < unflatFilter.out.length; i++) {
                            UnflatEdge[] inner = unflatFilter.out[i];
                            InterFilterEdge[] innerEdges = new InterFilterEdge[inner.length];
                            outEdges[i] = innerEdges;
                            for (int j = 0; j < inner.length; j++) {
                                UnflatEdge unflatEdge = inner[j];
                                UnflatFilter dest = unflatEdge.dest;
                                // if we didn't visit one of the dests, add it
                                if (!visited.contains(dest))
                                    queue.add(dest);
                                InterFilterEdge edge = edges.get(unflatEdge);
                                if (edge == null) {
                                    edge = new InterFilterEdge(outNode);
                                    edges.put(unflatEdge, edge);
                                } else {
                                    if (edge.getSrc() != outNode)
                                        edge.setSrc(outNode);
                                }
                                innerEdges[j] = edge;
                            }
                        }
                    }
                    slice.finish();
            }
        }


        topSlices = topSlicesList;
        setupIO();
        
        return getSliceGraph();
    }

    private void setupIO() {
        Filter[] sliceGraph = getSliceGraph();
        int len = sliceGraph.length;
        int newLen = len;
        for (int i = 0; i < len; i++)
            if (((WorkNode) sliceGraph[i].getInputNode().getNext())
                    .isPredefined())
                newLen--;
        io = new Filter[len - newLen];
        int idx = 0;
        for (int i = 0; i < len; i++) {
            Filter slice = sliceGraph[i];
            if (((WorkNode) slice.getInputNode().getNext()).isPredefined()) {
                io[idx++] = slice;
                System.out.println(slice + " is i/o slice.");
            }
        }

    }

}
