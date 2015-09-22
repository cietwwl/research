package research.csv.find;

import java.util.Comparator;

import research.util.Pair;

public class First<T> implements Finder<T> 
{
	CompareValue<T> include;
	Comparator<T> c;
	T f = null;
	T v = null;
	int i = -1;
	
	public First(Comparator<T> c, T f)
	{
		this(c,f,null);
	}

	public First(Comparator<T> c, T f, CompareValue<T> include)
	{
		this.c = c;
		this.f = f;
		this.include = include;
	}
	
	@Override
	public boolean process(T o, int index)
	{
		if (o != null)
		{
			if (include == null || include.process(o))
			{
				if (c.compare(o, f) > 0)
				{
					v = o;
					i = index;
				}
			}
		}
		
		return v == null;
	}
	
	public Pair<T, Integer> result()
	{
		if (v != null)
			return Pair.create(v,  i);
		
		return null;
	}
}