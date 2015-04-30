package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.Date;

import junit.framework.TestCase;

public class DateUtilsTest extends TestCase {

	public void testParseUtcString() throws Exception {
		Date time = DateUtils.parseUtcString("23.12.2014 01:23:45");
		
		long expected = 1419297825000L;
		
		if( time.getTime() != expected ){
			fail("Unexpected time: "+time.getTime());
		}
	}
}
