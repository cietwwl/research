package research.visualization;

import java.awt.Color;
import java.util.ArrayList;

public class Joint extends Node
{
	ArrayList<Joint> connections = new ArrayList<Joint>();
	public Color c = Color.black;
	public String name;
	
	public void connectTo (Joint j)
	{
		connections.add(j);
	}
}