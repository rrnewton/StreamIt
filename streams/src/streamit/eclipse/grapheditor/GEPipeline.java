/*
 * Created on Jun 20, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package grapheditor;
import java.io.*;
import java.util.*;

import com.jgraph.graph.*;

/**
 * GEPipeline is the graph internal representation of a node. .
 * @author jcarlos
 *
 */
public class GEPipeline extends GEStreamNode implements Serializable{
			
	private GEStreamNode lastNode;		
	
	public GEPipeline(String name)
	{
	
		super(GEType.PIPELINE, name);
		System.out.println("Constructing the pipeline");
	}


/**
 * Constructs the pipeline and returns the last node in the pipeline that will be connecting
 * to the next graph structure.
 */
	public GEStreamNode construct(GraphStructure graphStruct)
	{
		System.out.println("Constructing the pipeline" +this.getName());
		boolean first = true;
	
		this.draw();
		ArrayList nodeList = (ArrayList) this.getSuccesors();
		Iterator listIter =  nodeList.listIterator();
		while(listIter.hasNext())
		{
			GEStreamNode strNode = (GEStreamNode) listIter.next();
			GEStreamNode lastTemp = strNode.construct(graphStruct);
			
			if(!first)
			{
				System.out.println("Connecting " + lastNode.getName()+  " to "+ strNode.getName());
				
				// TO BE ADDED
				// connectDraw(lastNode, strNode);
				
				
				
			}
			
			lastNode = lastTemp;
			first = false;
		}
		return this.lastNode;
	}	
	
	/**
	 * Draw this Pipeline
	 */	
	public void draw()
	{
		System.out.println("Drawing the pipeline " +this.getName());
		// TO BE ADDED
	}
	public void collapse()
	{
		// draw shrunk version
	}

	public static void test()
	{
		System.out.println("Testing static method in pipeline");
	}
	
	
	
}
