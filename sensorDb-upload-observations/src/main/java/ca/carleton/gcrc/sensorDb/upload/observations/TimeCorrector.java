package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.Date;

/**
 * A positive offset means that a time (to correct) is ahead
 * of UTC time and must be moved back. A negative offset means that
 * a time must be moved forward to match UTC.
 *
 */
public class TimeCorrector {

	public Date startTime;
	public Date endTime;
	public int initialOffsetInSec = 0;
	public int finalOffsetInSec = 0;
	
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public int getInitialOffsetInSec() {
		return initialOffsetInSec;
	}

	public void setInitialOffsetInSec(int initialOffsetInSec) {
		this.initialOffsetInSec = initialOffsetInSec;
	}

	public int getFinalOffsetInSec() {
		return finalOffsetInSec;
	}

	public void setFinalOffsetInSec(int finalOffsetInSec) {
		this.finalOffsetInSec = finalOffsetInSec;
	}

	public Date correctTime(Date originalDate){
		long originalInMs = originalDate.getTime();
		
		long initialOffsetInMs = initialOffsetInSec * 1000;
		long finalOffsetInMs = finalOffsetInSec * 1000;

		long startTimeInMs = startTime.getTime();
		long endTimeInMs = endTime.getTime();
		
		long duration = endTimeInMs - startTimeInMs;
		double factor = ((double)finalOffsetInMs - (double)initialOffsetInMs) / (double)duration;
		
		double correctedInMs = (double)originalInMs 
				- (((double)originalInMs - (double)startTimeInMs) * factor)
				- (double)initialOffsetInMs;
		
		return new Date( (long)correctedInMs );
	}
}
