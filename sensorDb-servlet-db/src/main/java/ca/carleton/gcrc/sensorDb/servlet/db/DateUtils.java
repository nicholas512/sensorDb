package ca.carleton.gcrc.sensorDb.servlet.db;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils {

	static private Pattern patternDate = Pattern.compile("(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)( (\\d\\d):(\\d\\d)(:(\\d\\d))?)?");
	
	static public Date parseUtcString(String utcString) throws Exception {
		
		Matcher matcherDate = patternDate.matcher(utcString.trim());
		if( matcherDate.matches() ){
			TimeZone tz = TimeZone.getTimeZone("UTC");
			Calendar calendar = Calendar.getInstance(tz);
			calendar.clear();

			int year = Integer.parseInt( matcherDate.group(1) );
			int month = Integer.parseInt( matcherDate.group(2) );
			int day = Integer.parseInt( matcherDate.group(3) );
			
			if( null == matcherDate.group(4) ){
				// No time
				calendar.set(year, month-1, day);
				return calendar.getTime();
			}
			
			int hours = Integer.parseInt( matcherDate.group(5) );
			int minutes = Integer.parseInt( matcherDate.group(6) );
			
			if( null == matcherDate.group(7) ){
				// No seconds
				calendar.set(year, month-1, day, hours, minutes);
				return calendar.getTime();
			}
			
			int seconds = Integer.parseInt( matcherDate.group(8) );
			
			calendar.set(year, month-1, day, hours, minutes, seconds);
			return calendar.getTime();

		} else {
			throw new Exception("Can not parse UTC date: "+utcString);
		}
	};
}
