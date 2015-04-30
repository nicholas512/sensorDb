package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils {

	// dd.mm.yyyy hh:mm:ss
	static private Pattern patternDate1 = Pattern.compile("(\\d\\d)\\.(\\d\\d)\\.(\\d\\d\\d\\d) (\\d\\d):(\\d\\d):(\\d\\d)");
	
	static public Date parseUtcString(String utcString) throws Exception {
		
		Matcher matcherDate1 = patternDate1.matcher(utcString.trim());
		
		if( matcherDate1.matches() ){
			TimeZone tz = TimeZone.getTimeZone("UTC");
			Calendar calendar = Calendar.getInstance(tz);
			calendar.clear();

			int year = Integer.parseInt( matcherDate1.group(3) );
			int month = Integer.parseInt( matcherDate1.group(2) );
			int day = Integer.parseInt( matcherDate1.group(1) );
			
			int hours = Integer.parseInt( matcherDate1.group(4) );
			int minutes = Integer.parseInt( matcherDate1.group(5) );
			int seconds = Integer.parseInt( matcherDate1.group(6) );
			
			calendar.set(year, month-1, day, hours, minutes, seconds);
			return calendar.getTime();

		} else {
			throw new Exception("Can not parse UTC date: "+utcString);
		}
	};
}
