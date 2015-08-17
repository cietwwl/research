package research.math;

import javax.vecmath.Vector3d;

public class Vectors
{
	public static double findAngleBetween(Vector3d u, Vector3d v)
	{
	    double cosTheta = u.dot(v)/u.length()/v.length();
	    return Math.acos(cosTheta);
	}
	
	public static double findAngleBetweenLimbsOfThreeJoints (Vector3 a, Vector3 b, Vector3 c)
	{
	    Vector3 limb1 = c.subtract(b);
	    Vector3 limb2 = a.subtract(b);
	    
	    double angle = findAngleBetween(limb1,limb2);
	    double angleDegrees = Math.toDegrees(angle);
	    return angleDegrees;
	}
}
