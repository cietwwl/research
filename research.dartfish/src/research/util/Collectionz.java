package research.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Collectionz
{
	public static boolean mapContainsAll(Map<?, ?> map, String... keys)
	{
		if (map == null)
			return false;
		
		for (String key : keys)
		{
			if (!map.containsKey(key))
				return false;
		}
		
		return true;
	}

	public static boolean mapContainsAllAndNotNull(Map<?, ?> map, String... keys)
	{
		if (map == null)
			return false;
		
		for (String key : keys)
		{
			if (!map.containsKey(key) || map.get(key) == null)
				return false;
		}
		
		return true;
	}

	public static boolean setContainsAll(Set<?> set, String... keys)
	{
		for (String key : keys)
		{
			if (!set.contains(key))
				return false;
		}
		
		return true;
	}
	
	public static boolean allNotNull(Object...a)
	{
		for (Object o : a)
		{
			if (o == null)
				return false;
		}
		
		return true;
	}
	
	public static Map toMap(Object... a)
	{
		Map m = new HashMap();
		for (int i=0; i<a.length; i+=2)
		{
			m.put(a[i], a[i+1]);
		}
		return m;
	}
	
	public static <T> boolean isFirst(Collection<T> c, T o)
	{
		for (T e : c)
		{
			if (e.equals(o))
				return true;
			
			return false;
		}
		
		return false;
	}

	public static <T> boolean isFirst(T[] c, T o)
	{
		for (T e : c)
		{
			if (e.equals(o))
				return true;
			
			return false;
		}
		
		return false;
	}
}
