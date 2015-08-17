/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package research.util;

public class LogNull
{
	LogOut out;
	
	public LogNull (Class<?> clazz)
	{
		out = new LogOut(clazz);
	}
	
	public LogNull(String string) {
		out = new LogOut(string);
	}

	public void println(Object... string)
	{
		// TODO Auto-generated method stub
		
	}
}
