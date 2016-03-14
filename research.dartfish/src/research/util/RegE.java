package research.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegE 
{
	static public String[] match (String e, String p)
	{
		if (e == null)
			return null;
		
		Pattern pattern = Pattern.compile(p);
		Matcher m = pattern.matcher(e);
		
		if (!m.matches())
			return null;
		
		ArrayList<String> a = new ArrayList<String>();
		for (int i=0; i<m.groupCount(); ++i)
		{
			a.add(m.group(i+1));
		}
		
		return a.toArray(new String[0]);
	}
}
