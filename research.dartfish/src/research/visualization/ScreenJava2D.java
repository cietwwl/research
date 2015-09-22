package research.visualization;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class ScreenJava2D extends JPanel
{
	Renderer renderer;
	Node scene;
	
	public ScreenJava2D ()
	{
		
	}
	
	public void setRenderer (Renderer renderer)
	{
		this.renderer = renderer;
	}
	
	public void setScene (Node scene)
	{
		this.scene = scene;
	}
	
	long firstTime = 0;
	long lastTime = 0;
	
	public void paintComponent (Graphics g)
	{
		if (firstTime == 0)
			firstTime = System.currentTimeMillis();
			
		if (lastTime == 0)
			lastTime = firstTime;
		
		long time = System.currentTimeMillis();
		long dt = time - lastTime;
		
		if (renderer == null || scene == null)
			return;
		
		scene.update((time - firstTime) / 1000.0, dt / 1000.0);
		
		renderer.resize(this.getWidth(), this.getHeight());
		renderer.setScreen(g);
		renderer.clear(Color.black);
		renderer.render(scene);
		
		SleepAndRefresh(dt);
	}
	
	static float MaxFPS = 50.0f;
	
	void SleepAndRefresh(long dt)
	{
		if(dt < 1000.0/MaxFPS)
		{
			try {
				Thread.sleep((long) (1000.0/MaxFPS - dt));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
				
		repaint();
	}
	
}
