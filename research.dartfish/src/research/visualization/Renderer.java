package research.visualization;

import java.awt.Color;

public interface Renderer
{
	public Renderable requestRenderable (Node node);

	public void setScreen (Object graphics);
	public void resize (int width, int height);
	public void render (Node scene);
		public void flip();
	void clear(Color c);

	void setCamera(Camera camera);
}
