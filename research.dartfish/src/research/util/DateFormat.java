/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package research.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateFormat
{
	SimpleDateFormat sdf;
	
	public static String format(String format, Date date)
	{
		return new DateFormat(format).format(date);
	}
	
	public static Date parse(String format, String date)
	{
		try {
			return new DateFormat(format).parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public DateFormat(String format)
	{
		sdf = new SimpleDateFormat(format);
	}

	public String format(Date date, int i)
	{
		sdf.setTimeZone(TimeZone.getTimeZone("GMT-"+i+":00"));
		return sdf.format(date);
	}

	public String format(Date date)
	{
		return format(date, 0);
	}

	public Date parse(String time) throws ParseException
	{
		return sdf.parse(time);
	}

}
