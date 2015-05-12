package ca.carleton.gcrc.sensorDb.servlet.db;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

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

	public void testParseUtcStringErrors() throws Exception {

		List<String> errors = new Vector<String>();
		errors.add("2015-00-28 00:00:00"); // invalid month
		errors.add("2015-13-28 00:00:00"); // invalid month
		errors.add("2015-02-00 00:00:00"); // invalid day
		errors.add("2015-02-29 00:00:00"); // invalid day
		errors.add("2015-02-01 24:00:00"); // invalid hour
		errors.add("2015-02-01 20:60:00"); // invalid minutes
		errors.add("2015-02-01 20:00:60"); // invalid seconds

		for(String error : errors){
			try {
				DateUtils.parseUtcString(error);
				
				fail("Date parsing should throw an error: "+error);
			} catch(Exception e) {
				// OK
			}
		};
		

	}
}
