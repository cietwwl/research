package research.math;

import java.util.Collection;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class Maths
{
	public static double[] toDoubleArray (Collection<Double> c)
	{
		double[] a = new double[c.size()];
		int i=0;
		for (Double d : c)
		{
			a[i++] = d; 
		}
		
		return a;
	}

	public static double[] only (Collection<Vector3> c, int i)
	{
		double[] a = new double[c.size()];
		
		int j=0;
		for (Vector3 d : c)
		{
			a[j++] = d.get(i); 
		}
		
		return a;
	}
	
	public static double average (double[] v)
	{
		double total = 0;
		for (double s : v)
			total += s;
		
		double average = total / (double)v.length;
		
		return average;
	}
	
	public static double stddev (double[] v)
	{
		StandardDeviation stddev = new StandardDeviation();
		double sd = stddev.evaluate(v);
		return sd;
	}
}
