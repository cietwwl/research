package research.visualization;

import java.util.ArrayList;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;

public class Node
{
	protected ArrayList<Node> children = new ArrayList<Node>();
	
	public Matrix4d transform;
	Matrix4d worldTransform = new Matrix4d();
	Renderable renderable;
	
	public Node ()
	{
		transform = new Matrix4d();
		transform.setIdentity();
		worldTransform.setIdentity();
	}
	
	public void requestRenderable (Renderer renderer)
	{
		this.renderable = renderer.requestRenderable(this);
	}
	
	public void render (Renderer renderer)
	{
		if (renderable == null)
		{
			requestRenderable(renderer);
		}
		
		renderable.render(renderer);
	}
	
	public void update (double time, double dt)
	{
		for (Node c : children)
		{
			c.worldTransform.mul(worldTransform, c.transform);
		}
	}
}
