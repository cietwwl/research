/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package research.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class LogOut
{
	String prefix;
	String cached = "";
	DateFormat dateFormat = new DateFormat("HH:mm:ss.SSS");
	
	public LogOut (Class<?> clazz)
	{
		prefix = clazz.getName()+":";
	}
	
	public LogOut(String prefix) 
	{
		this.prefix = prefix;
	}

	public final String build (Object...arguments)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(dateFormat.format(new Date()));
		builder.append(" ");
		builder.append(prefix);
		builder.append(":");

		for (int j=0;j<arguments.length; ++j)
		{
			builder.append(" ");
			if (arguments[j] instanceof Exception)
				builder.append(exceptionToString((Exception)arguments[j]));
			else
				builder.append(arguments[j]);
		}
	
		return builder.toString();
	}
	
	public final void println (Object...arguments)
	{
		LogPlatform.println(build(arguments));
	}
	
	public final String exceptionToString(Exception e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
	
}
