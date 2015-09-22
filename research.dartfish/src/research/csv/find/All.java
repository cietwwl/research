package research.csv.find;

import java.util.Comparator;

import research.util.Pair;

public class All<T extends Comparable<?>> implements Finder<T> 
{
	CompareValue<T> include;
	Comparator<T> c;
	T max = null;
	int i = -1;
	
	public All(Comparator<T> c)
	{
		this(c, null);
	}

	public All(Comparator<T> c, CompareValue<T> include)
	{
		this.c = c;
		this.include = include;
	}
	
	@Override
	public boolean process(T o, int index)
	{
		if (o != null)
		{
			if (include == null || include.process(o))
			{
				if (max == null || (c.compare(o, max) > 0))
				{
					max = o;
					i = index;
				}
			}
		}
		
		return true;
	}
	
	public Pair<T, Integer> result()
	{
		if (max != null)
			return Pair.create(max,  i);
		
		return null;
	}
}