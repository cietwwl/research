package research.visualization;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class Test
{
	static Dimension ScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
	public static void main(String[] args)
	{
		Joint j0 = new Joint();
		j0.transform.set(new Vector3d(0,0,0));
		Joint j1 = new Joint();
		j0.transform.set(new Vector3d(1,1,1));
		j0.connectTo(j1);
		
		Node scene = new Node();
		scene.children.add(j0);
		scene.children.add(j1);
		DefaultJava2D.create(scene);
	}		
}
