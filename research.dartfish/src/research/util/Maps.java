package research.util;

import java.util.Map;

public class Maps
{
	public static <T> T ensureKey(Map m, Object key, T value)
	{
		if (m.containsKey(key))
			return (T)m.get(key);
		
		m.put(key,  value);
		return value;
	}

}
