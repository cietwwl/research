package research.math;

import java.util.List;

import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

@SuppressWarnings("serial")
public class Vector3 extends Vector3d implements Comparable<Vector3>
{

	public Vector3()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public double get (int i)
	{
		if (i == 0)
			return x;
		if (i == 1)
			return y;
		if (i == 2)
			return z;
		
		throw new RuntimeException("Unknown index");
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

	public Vector3 add(Vector3d v)
	{
		Vector3d r = new Vector3(this);
		r.add(v);
		
		return new Vector3(r);
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
	
	public Vector3 cross (Vector3 rhs)
	{
		Vector3 result = new Vector3();
		result.cross(this, rhs);
		return result;
	}
	
	public Vector3 unitize ()
	{
		Vector3 result = new Vector3(this);
		result.normalize();
		
		return result;
	}
	
	static public Vector3
		UnitX = new Vector3(1,0,0), 
		UnitY = new Vector3(0,1,0), 
		UnitZ = new Vector3(0,0,1),

		UnitNegX = new Vector3(-1,0,0), 
		UnitNegY = new Vector3(0,-1,0), 
		UnitNegZ = new Vector3(0,0,-1),

		UnitX_Z = new Vector3(1,0,1), 
		Unit_YZ = new Vector3(0,1,1), 
		UnitXY_ = new Vector3(1,1,0),
		Zero = new Vector3(0,0,0);

	@Override
	public int compareTo(Vector3 o)
	{
		double s = o.lengthSquared() - this.lengthSquared();
		if (s > 0)
			return 1;
		if (s < 0)
			return -1;
		
		return 0;
	}

	public static Vector3 Zero()
	{
		return new Vector3(0,0,0);
	}

	public Vector3 multiply(Vector3 v)
	{
		return new Vector3(x * v.x, y * v.y, z * v.z);
	}
	
	public static Vector3 boundingBox (List<? extends Vector3> vs)
	{
		Vector3 min = new Vector3(vs.get(0));
		Vector3 max = new Vector3(vs.get(0));
		for (Vector3 v : vs)
		{
			if (v.x < min.x)
				min.x = v.x;
			if (v.y < min.y)
				min.y = v.y;
			if (v.z < min.z)
				min.z = v.z;
			
			if (v.x > max.x)
				max.x = v.x;
			if (v.y > max.y)
				max.y = v.y;
			if (v.z > max.z)
				max.z = v.z;
		}
		
		return max.subtract(min);
	}

	public boolean isNearZero()
	{
		double EPS = 0.0001;
		return (
			(x < EPS && x > -EPS) &&
			(y < EPS && y > -EPS) &&
			(z < EPS && z > -EPS)
		);
			
	}
}
