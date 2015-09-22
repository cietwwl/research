package research.balance;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import research.csv.CsvIn;
import research.visualization.DefaultJava2D;

public class SkeletonCsvViewer implements Skeleton.Delegate
{

	@Override
	public void onUpdate(Skeleton skeleton)
	{
	}

	
	public static void main (String[] args) throws IOException
	{
		try
		{
			Set<String> a = new HashSet<String>();
			for (String arg : args)
				a.add(arg);
			
			Skeleton skeleton = new Skeleton(Skeleton.defaultJoints, new Float(args[1]));
			skeleton.setData(new CsvIn(args[0]));
			skeleton.setRepeat(a.contains("-r"));
			DefaultJava2D.create(skeleton);
		}
		catch (Exception e)
		{
			System.out.println(e);
			throw e;
		}
	}
}
