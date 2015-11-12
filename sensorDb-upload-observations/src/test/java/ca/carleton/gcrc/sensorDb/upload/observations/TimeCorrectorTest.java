package ca.carleton.gcrc.sensorDb.upload.observations;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

public class TimeCorrectorTest extends TestCase {

	// Are the two dates within 10 seconds of each other?
	private boolean areTimesEqual(Date date1, Date date2){
		long errorAllowedInMs = 10 * 1000;
		
		long errorInMs = date1.getTime() - date2.getTime();
		if( errorInMs < 0 ){
			errorInMs = 0 - errorInMs;
		}
		
		if( errorInMs <= errorAllowedInMs ){
			return true;
		}
		
		return false;
	}
	
	public void testNoDrift() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date startTime = formatter.parse("2015-01-01 00:00");
		Date endTime = formatter.parse("2015-01-02 00:00");
		int initialOffset = 0;
		int finalOffset = 0;

		TimeCorrector corrector = new TimeCorrector();
		corrector.setStartTime(startTime);
		corrector.setEndTime(endTime);
		corrector.setInitialOffsetInSec(initialOffset);
		corrector.setFinalOffsetInSec(finalOffset);
		
		Date testTime = formatter.parse("2015-01-01 12:00");
		Date correctedTime = corrector.correctTime(testTime);
		
		Date expectedTime = formatter.parse("2015-01-01 12:00");
		if( false == areTimesEqual(correctedTime, expectedTime) ){
			fail("Unexpected correction: "+correctedTime.toString());
		}
	}
	
	public void testPositiveFinalOffset() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date startTime = formatter.parse("2015-01-01 00:00");
		Date endTime = formatter.parse("2015-01-02 00:00");
		int initialOffset = 0;
		int finalOffset = 240; // 4 minutes ahead at the end of a day

		TimeCorrector corrector = new TimeCorrector();
		corrector.setStartTime(startTime);
		corrector.setEndTime(endTime);
		corrector.setInitialOffsetInSec(initialOffset);
		corrector.setFinalOffsetInSec(finalOffset);
		
		Date testTime = formatter.parse("2015-01-01 12:00");
		Date correctedTime = corrector.correctTime(testTime);
		
		Date expectedTime = formatter.parse("2015-01-01 11:58");
		if( false == areTimesEqual(correctedTime, expectedTime) ){
			fail("Unexpected correction: "+correctedTime.toString());
		}
	}
	
	public void testNegativeFinalOffset() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date startTime = formatter.parse("2015-01-01 00:00");
		Date endTime = formatter.parse("2015-01-02 00:00");
		int initialOffset = 0;
		int finalOffset = -240; // 4 minutes behind at the end of a day

		TimeCorrector corrector = new TimeCorrector();
		corrector.setStartTime(startTime);
		corrector.setEndTime(endTime);
		corrector.setInitialOffsetInSec(initialOffset);
		corrector.setFinalOffsetInSec(finalOffset);
		
		Date testTime = formatter.parse("2015-01-01 12:00");
		Date correctedTime = corrector.correctTime(testTime);
		
		Date expectedTime = formatter.parse("2015-01-01 12:02");
		if( false == areTimesEqual(correctedTime, expectedTime) ){
			fail("Unexpected correction: "+correctedTime.toString());
		}
	}
	
	public void testPositiveInitialOffset() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date startTime = formatter.parse("2015-01-01 00:00");
		Date endTime = formatter.parse("2015-01-02 00:00");
		int initialOffset = 3600; // Error programming the device. Start ahead by an hour
		int finalOffset = 3600; // No drift over day. Same error at end of day

		TimeCorrector corrector = new TimeCorrector();
		corrector.setStartTime(startTime);
		corrector.setEndTime(endTime);
		corrector.setInitialOffsetInSec(initialOffset);
		corrector.setFinalOffsetInSec(finalOffset);
		
		Date testTime = formatter.parse("2015-01-01 12:00");
		Date correctedTime = corrector.correctTime(testTime);
		
		Date expectedTime = formatter.parse("2015-01-01 11:00");
		if( false == areTimesEqual(correctedTime, expectedTime) ){
			fail("Unexpected correction: "+correctedTime.toString());
		}
	}
	
	public void testNegativeInitialOffset() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date startTime = formatter.parse("2015-01-01 00:00");
		Date endTime = formatter.parse("2015-01-02 00:00");
		int initialOffset = -3600; // Error programming the device. Start ahead by an hour
		int finalOffset = -3600; // No drift over day. Same error at end of day

		TimeCorrector corrector = new TimeCorrector();
		corrector.setStartTime(startTime);
		corrector.setEndTime(endTime);
		corrector.setInitialOffsetInSec(initialOffset);
		corrector.setFinalOffsetInSec(finalOffset);
		
		Date testTime = formatter.parse("2015-01-01 12:00");
		Date correctedTime = corrector.correctTime(testTime);
		
		Date expectedTime = formatter.parse("2015-01-01 13:00");
		if( false == areTimesEqual(correctedTime, expectedTime) ){
			fail("Unexpected correction: "+correctedTime.toString());
		}
	}

	public void testInitialFinalOffset() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date startTime = formatter.parse("2015-01-01 00:00");
		Date endTime = formatter.parse("2015-01-02 00:00");
		int initialOffset = 3600; // Error programming the device. Start ahead by an hour
		int finalOffset = 3840; // Over the day, gains 4 minutes

		TimeCorrector corrector = new TimeCorrector();
		corrector.setStartTime(startTime);
		corrector.setEndTime(endTime);
		corrector.setInitialOffsetInSec(initialOffset);
		corrector.setFinalOffsetInSec(finalOffset);
		
		Date testTime = formatter.parse("2015-01-01 12:00");
		Date correctedTime = corrector.correctTime(testTime);
		
		Date expectedTime = formatter.parse("2015-01-01 10:58");
		if( false == areTimesEqual(correctedTime, expectedTime) ){
			fail("Unexpected correction: "+correctedTime.toString());
		}
	}
}
