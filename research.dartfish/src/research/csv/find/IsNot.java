package research.csv.find;

import java.util.Comparator;

import research.math.Vector3;

public class IsNot<T extends Comparable<?>> implements CompareValue<T> 
{
	Comparator<T> c;
	T v = null;
	
	public IsNot(Comparator<T> c, T v)
	{
		this.c = c;
		this.v = v;
	}
	
	@Override
	public boolean process(T o)
	{
		return c.compare(o, v) != 0;
	}
}