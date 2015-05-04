package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.Date;

public interface ObservationFileImportReport {

	void insertedObservation(Date time, String sensor_id, double value);

	void collisionOnObservation(Date time, String sensor_id, double value, double db_value);

	void skippedObservation(Date time, String sensor_id, double value);
	
	String produceReport() throws Exception;
}
