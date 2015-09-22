package research.visualization;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class Camera
{
	Matrix4d transform = new Matrix4d();
	Matrix4d inverted = null;
	
	public Camera()
	{
		transform.setTranslation(new Vector3d(0, 0.25, 0));
		transform.setScale(1/2000.0);
	}

	Vector3d t0 = new Vector3d();
	Vector3d t1 = new Vector3d();
	Matrix4d m1 = new Matrix4d();
	Matrix4d m2 = new Matrix4d();
	
	public void calculatePoint (Vector3d p0, Vector2d s0)
	{
  		if (inverted == null)
		{
			inverted = new Matrix4d();
			inverted.set(transform);
		}
		
  		m1.setIdentity();
  		m1.setTranslation(p0);
  		
  		m2.mul(inverted, m1);
  		t1 = new Vector3d(m2.m03,m2.m13,m2.m23);
  		
		t0.set(t1);
		s0.x = t0.y;
		s0.y = 1.0 - t0.z;
	}
}
