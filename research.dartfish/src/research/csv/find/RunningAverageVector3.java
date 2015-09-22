package research.csv.find;

import java.util.Comparator;

import research.math.Vector3;
import research.util.Pair;

public class RunningAverageVector3 implements Finder<Vector3>
{
	int samples = 0;
	Vector3 average = new Vector3(0,0,0);
	CompareValue<Vector3> include;
	
	public RunningAverageVector3(CompareValue<Vector3> include)
	{
		this.include = include;
	}
	
	@Override
	public boolean process(Vector3 o, int index)
	{
		if (o != null)
		{
			if (include.process(o))
			{
				average.add(o);
				samples ++;
			}
		}
		
		return true;
	}
	
	public Pair<Vector3, Integer> result()
	{
		return Pair.create(average.divide(samples), samples);
	}
}