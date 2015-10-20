package research.util;

import java.util.Collection;
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

	public static boolean setContainsAll(Set<?> set, String... keys)
	{
		for (String key : keys)
		{
			if (!set.contains(key))
				return false;
		}
		
		return true;
	}
}
