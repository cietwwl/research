package research.math;

import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

@SuppressWarnings("serial")
public class Vector3 extends Vector3d
{

	public Vector3()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public Vector3(double arg0, double arg1, double arg2)
	{
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public Vector3(double[] arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public Vector3(Tuple3d arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public Vector3(Tuple3f arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public Vector3(Vector3d arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public Vector3(Vector3f arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	
	public Vector3 subtract(Vector3d v)
	{
		Vector3 r = new Vector3(this);
		r.sub(v);
		
		return r;
	}

	public Vector3 divide(double d)
	{
		Vector3 r = new Vector3(this);
		r.scale(1.0/d);
		
		return r;
	}
	
	public Vector3 multiply (double d)
	{
		Vector3 r = new Vector3(this);
		r.scale(d);
		
		return r;
	}
}
