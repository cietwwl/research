package research.visualization;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

public class RendererJava2D implements Renderer
{
	Camera camera;
	Graphics g;
	
	public static class JointI implements Renderable
	{
		Joint j;
		RendererJava2D r;
		Font font;

		public JointI (RendererJava2D r, Joint j)
		{
			this.r = r;
			this.j = j;
			font = new Font(Font.SANS_SERIF, 0, 10);
		}
		
		@Override
		public void render(Renderer renderer)
		{
			for (Joint c : j.connections)
			{
				r.drawLine(Color.yellow, j.worldTransform, c.worldTransform);
			}
			
			Vector3d v = new Vector3d();
			j.transform.get(v);
			int x = (int)v.x;
			int y = (int)v.y;
			int z = (int)v.z;
			
			
			if (!j.name.startsWith("p_") && !j.name.startsWith("_"))
				r.drawText(Color.white, j.worldTransform, j.name 
					+ "(" + x + "," + y + "," + z + ")" 
					,font);
			r.drawCircle(j.c, j.worldTransform, 5);
		}
	}
	
	public static class NoDrawI implements Renderable
	{

		@Override
		public void render(Renderer renderer)
		{
			// TODO Auto-generated method stub
			
		}
	}
	
	@Override
	public Renderable requestRenderable(Node node)
	{
		if (node instanceof Joint)
			return new JointI(this, (Joint)node);
		
		return new NoDrawI();
	}

	static Vector3d p0 = new Vector3d(), p1 = new Vector3d();
	static Vector2d s0 = new Vector2d(), s1 = new Vector2d();
	int width, height;
	
	public void resize (int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	
	public void drawCircle(Color c, Matrix4d t0, int radius)
	{
		g.setColor(c);
		
		t0.get(p0);
		
		camera.calculatePoint(p0,s0);
		
		int s0x = (int)(s0.x * width), 
			s0y = (int)(s0.y * height), 
			s1x = (int)(s0.x * width), 
			s1y = (int)(s0.y * height);
		
		g.drawOval(s0x, s0y, radius, radius);
	}

	public void drawText(Color c, Matrix4d t0, String text, Font font)
	{
		g.setColor(c);
		
		t0.get(p0);
		
		camera.calculatePoint(p0,s0);
		
		int s0x = (int)(s0.x * width), 
			s0y = (int)(s0.y * height);
		
		g.setFont(font);
		g.drawChars(text.toCharArray(), 0, text.length(), s0x, s0y);
	}
	
	public void drawLine(Color c, Matrix4d t0, Matrix4d t1)
	{
		g.setColor(c);
		
		t0.get(p0);
		t1.get(p1);
		
		camera.calculatePoint(p0,s0);
		camera.calculatePoint(p1,s1);
		
		int s0x = (int)(s0.x * width), 
			s0y = (int)(s0.y * height), 
			s1x = (int)(s1.x * width), 
			s1y = (int)(s1.y * height);
		
		g.drawLine(s0x, s0y, s1x, s1y);
	}

	@Override
	public void clear (Color c)
	{
		g.setColor(c);
		g.fillRect(0, 0, width, height);
	}
	
	public void flip()
	{
	}
	
	public void setScreen (Object graphics)
	{
		this.g = (Graphics)graphics;
	}
	
	Culler culler = new Culler();
	
	public void render (Node scene)
	{
		culler.cull(scene);
		for (Node n : culler.drawables)
		{
			n.render(this);
		}
	}

	@Override
	public void setCamera(Camera camera)
	{
		this.camera = camera;
		
	}
}
