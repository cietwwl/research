package research.math;

import javax.vecmath.Vector3d;

import research.util.Pair;

public class Vectors
{
	public static Vector3 findAxisBetween(Vector3 u, Vector3 v)
	{
		return u.cross(v).unitize();
	}
	
	public static double unitOnAxis (Vector3 u, Vector3 v)
	{
		double d = u.dot(v);
		if (d >= 0)
			return 1.0;

		return -1.0;
	}
	
	public static double findAngleBetweenACOS(Vector3 u, Vector3 v)
	{
	    double cosTheta = u.dot(v)/u.length()/v.length();
	    return Math.acos(cosTheta);
	}
	
	public static double findAngleBetweenATAN2(Vector3 u, Vector3 v)
	{
		return Math.atan2(u.cross(v).length(),u.dot(v));		
	}
	
	public static double findAngleBetween(Vector3 u, Vector3 v)
	{
		return findAngleBetweenATAN2(u,v);
	}
	
	public static double findAngleBetweenLimbsOfThreeJoints (Vector3 a, Vector3 b, Vector3 c)
	{
	    Vector3 limb1 = c.subtract(b);
	    Vector3 limb2 = a.subtract(b);
	    
	    double angle = findAngleBetween(limb1,limb2);
	    double angleDegrees = Math.toDegrees(angle);
	    return angleDegrees;
	}
	
	public static Pair<Double, Vector3> findAngleAndAxisBetweenLimbsOfThreeJoints (Vector3 a, Vector3 b, Vector3 c)
	{
	    Vector3 limb1 = c.subtract(b);
	    Vector3 limb2 = a.subtract(b);

	    return Pair.create(Math.toDegrees(findAngleBetween(limb1,limb2)), findAxisBetween(limb1, limb2));
	}
	
	static public void multiply(Vector3 m, Vector3...vs)
	{
		for (Vector3 v : vs)
		{
			v.x *= m.x;
			v.y *= m.y;
			v.z *= m.z;
		}
	}
}
