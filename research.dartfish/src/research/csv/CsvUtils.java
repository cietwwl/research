package research.csv;

import java.util.Map;

import research.csv.find.Finder;
import research.util.Pair;

public class CsvUtils
{
	public static <T> Pair<T, Integer> findValueInColumn (Csv csv, String columnName, int index, Finder<T> c)
	{
		if (csv.rows.size() < index + 1)
			return null;
		
		for (int i=index+1; i<csv.rows.size(); ++i)
		{
			Map<String, Object> row = csv.rows.get(i);

			@SuppressWarnings("unchecked")
			T v = (T) row.get(columnName);
			if (!c.process(v, i))
				break;
		}
		
		return c.result();
	}
}
