/*
 * Created on Jun 23, 2003
 *
 */
package grapheditor;

import java.io.*;
import com.jgraph.graph.*;
import java.awt.Color;
import javax.swing.BorderFactory; 
import com.jgraph.JGraph;

/**
 * GEJoiner is the graph editor's internal representation of a joiner.
 * @author jcarlos
 */
public class GEJoiner extends GEStreamNode implements Serializable{
	
	/**
	 * The weights corresponding to the splitter.
	 */	
	private int[] weights;

	/**
	 * GEJoiner constructor.
	 * @param name The name of the GEJoiner.
	 * @param weights The weights of the GEJoiner.
	 */
	public GEJoiner (String name, int[] weights)
	{
		super(GEType.JOINER, name);
		this.name = name;
		this.weights = weights;
	}
	
	/**
	 * Get the weights of this 
	 * @return The weights corresponding to the GEJoiner
	 */
	public int[] getWeights()
	{
		return this.weights;
	}
	
	/**
	 * Get the weight as a string of the form: "(weight1, weight2, weight3,... , weightN)".
	 * @return String representation of the weights of the GEJoiner.
	 */
	public String getWeightsAsString()
	{
		String strWeight = "(";
		for(int i = 0; i < this.weights.length; i++)
		{
			if (i != 0)
			{
				strWeight += ", ";
			}
			strWeight += this.weights[i];
		
		}
		
		strWeight += ")";
		return strWeight;
	}
	
	/**
	 * Contructs the joiner and returns <this>.
	 * @return <this>
	 */
	public GEStreamNode construct(GraphStructure graphStruct)
	{
		System.out.println("Constructing the Joiner " +this.getName());
		
		
		this.setInfo(this.getWeightsAsString());
		this.setUserObject(this.getInfoLabel());
				
		(graphStruct.getAttributes()).put(this, this.attributes);
		GraphConstants.setAutoSize(this.attributes, true);
		GraphConstants.setBounds(this.attributes, graphStruct.setRectCoords(this));
		GraphConstants.setBorder(this.attributes , BorderFactory.createRaisedBevelBorder());
		GraphConstants.setBackground(this.attributes, Color.orange);
		
		this.port = new DefaultPort();
		this.add(this.port);
		graphStruct.getCells().add(this);
		
		this.draw();
		return this;
	}

	/**
	 * Draw this Joiner
	 */
	public void draw()
	{
		System.out.println("Drawing the Joiner " +this.getName());
		// TO BE ADDED
	}
	
	public void collapseExpand(JGraph jgraph){};
}
