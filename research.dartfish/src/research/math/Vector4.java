package research.math;

import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple4d;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector4d;
import javax.vecmath.Vector4f;

@SuppressWarnings("serial")
public class Vector4 extends Vector4d
{

	public Vector4()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public Vector4(double arg0, double arg1, double arg2, double arg3)
	{
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

	public Vector4(double[] arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public Vector4(Tuple3d arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public Vector4(Tuple4d arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public Vector4(Tuple4f arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public Vector4(Vector4d arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public Vector4(Vector4f arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public Vector4(Vector3 p, double length)
	{
		x = p.x;
		y = p.y;
		z = p.z;
		w = length;
	}
}
