/*
 * Created on Jun 18, 2003
 */

package grapheditor;

import java.util.*;
import java.io.*;

//import com.sun.rsasign.t;


/**
 * Graph data structure that has GEStreamNode objects as its nodes. 
 * @author jcarlos
 */
public class GraphStructure implements Serializable{
	
	private HashMap graph;
	
	// The toplevel GEStreamNode. Typically it should be a GEPipeline object.  
	private GEStreamNode topLevel;

	
	public GraphStructure(ArrayList nodes)
	{
		for (int i = 0;  i < nodes.size(); i++)
		{
			GEStreamNode n = (GEStreamNode) nodes.get(i);
			this.graph.put(n, n.getChildren());
		}
	}
	
	public GraphStructure()
	{
		graph = new HashMap();
	}
	
	
	public void addHierarchy(GEStreamNode parentNode, ArrayList children)
	{
		this.graph.put(parentNode, children);
	}
			
	// Add a node that will be a child node of the node parent.
	public void addNode(GEStreamNode node, GEStreamNode parent, int index)
	{
		ArrayList nodeList = this.getChildren(parent); 
		nodeList.add(index, node);
	}
	
	// Delete node and all of the children belonging to that node	
	public void deleteNode(GEStreamNode node)
	{
		ArrayList nodeList = this.getChildren(node);
		int listSize = nodeList.size();
		
		for (int i = 0; i < listSize; i++)
		{
			GEStreamNode n = (GEStreamNode) nodeList.get(i);
			this.graph.remove(n);
		}
		this.graph.remove(node);
	}
	
	// Provide the children of node n 
	public ArrayList getChildren(GEStreamNode n)
	{
		return (ArrayList) this.graph.get(n);
	}
	
	public void constructGraph()
	{
		this.topLevel.construct();
		
		ArrayList nodeList =  (ArrayList) this.topLevel.getChildren();
		
	    ListIterator listIter = nodeList.listIterator();
	    while(listIter.hasNext())
	    {
	    	GEStreamNode strNode =  (GEStreamNode) listIter.next();
	    	strNode.draw(); 
	    }
	}
	
	
}

