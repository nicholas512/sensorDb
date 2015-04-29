package ca.carleton.gcrc.sensorDb.servlet.db;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

public class DateTest extends TestCase {

	public void testDateFormat(){
		TimeZone tz = TimeZone.getTimeZone("UTC");
		Calendar calendar = Calendar.getInstance(tz);
		
		calendar.set(2015, 04-1, 28, 0, 0, 0);
		
		Date date = calendar.getTime();
		
		System.out.println("Date: "+date.toString());
	}

	public void testParseUtcString() throws Exception {
		Date time = DateUtils.parseUtcString("2015-04-29 00:00:00");

		long expected = 1430265600000L;
		
		if( expected != time.getTime() ){
			fail("Error parsing UTC string: "+time.getTime());
		}
	}
}
