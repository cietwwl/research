package research.csv.find;

import java.util.Comparator;

import research.math.Vector3;

public class LessThanVector3NotZero implements Comparator<Vector3> {
	
	Vector3 d;
	
	public LessThanVector3NotZero(Vector3 d)
	{
		this.d = d;
	}
	
	@Override
	public int compare(Vector3 o1, Vector3 o2)
	{
		if (o1.equals(Vector3.Zero))
			throw new SkipException();

		Double d1 = o1.dot(d);
		Double d2 = o2.dot(d);
		return Double.compare(d2, d1);
	}
}