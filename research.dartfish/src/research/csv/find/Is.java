package research.csv.find;

import java.util.Comparator;

public class Is<T extends Comparable<?>> implements CompareValue<T> 
{
	Comparator<T> c;
	T v = null;
	
	public Is(Comparator<T> c, T v)
	{
		this.c = c;
		this.v = v;
	}
	
	@Override
	public boolean process(T o)
	{
		return c.compare(o, v) == 0;
	}
}