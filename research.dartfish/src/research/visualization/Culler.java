package research.visualization;

import java.util.ArrayList;
import java.util.Vector;

public class Culler
{
	Vector<Node> drawables = new Vector<Node>();
	
	public void cull(Node scene)
	{
		drawables.clear();
		doCull(scene);
	}
	
	public void doCull (Node scene)
	{
		drawables.add(scene);
		for (Node c : scene.children)
		{
			doCull(c);
		}
	}
}
