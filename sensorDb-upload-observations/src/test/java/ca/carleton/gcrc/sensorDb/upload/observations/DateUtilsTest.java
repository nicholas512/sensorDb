package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

public class DateUtilsTest extends TestCase {

	public void testParseUtcString() throws Exception {
		Date time = DateUtils.parseUtcString("23.12.2014 01:23:45");
		
		long expected = 1419297825000L;
		
		if( time.getTime() != expected ){
			fail("Unexpected time: "+time.getTime());
		}
	}

	public void testParseUtcStrings() throws Exception {
		Map<String,Long> tests = new HashMap<String,Long>();
		
		tests.put("02.11.2014 05:00:00", 1414904400000L);
		tests.put("02.11.2014 06:00:00", 1414908000000L);
		
		List<String> keys = new Vector<String>();
		keys.addAll( tests.keySet() );
		Collections.sort(keys);
		
		for(String dateStr : keys){
			Long expectedValue = tests.get(dateStr);
			long expected = expectedValue.longValue();
			
			Date time = DateUtils.parseUtcString(dateStr);
			
			if( time.getTime() != expected ){
				fail("Unexpected time: "+time.getTime()+" "+dateStr);
			}
		}
	}
}
