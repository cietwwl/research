package research.visualization;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class DefaultJava2D
{
	static Dimension ScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
	public static void create (Node scene)
	{
		JFrame F = new JFrame();
		ScreenJava2D screen = new ScreenJava2D();
		F.add(screen);
		F.setUndecorated(true);
	    F.setSize(new Dimension(ScreenSize.width*3/4, ScreenSize.height*3/4));
		
		RendererJava2D renderer = new RendererJava2D();
		Camera camera = new Camera();
		renderer.setCamera(camera);
		screen.setRenderer(renderer);
		screen.setScene(scene);

		F.setVisible(true);
		F.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
