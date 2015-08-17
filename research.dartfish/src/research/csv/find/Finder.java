package research.csv.find;

import research.util.Pair;

public interface Finder<T> 
{
	public boolean process(T o, int index);
	public Pair<T, Integer> result();
}