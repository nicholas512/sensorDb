package ca.carleton.gcrc.sensorDb.dbapi;

import org.json.JSONObject;

public interface ImportReport {
	
	void setImportId(String importId);

	void insertedObservation(Observation observation);

	void skippedObservation(Observation observation);
	
	void setError(Throwable err);
	
	JSONObject produceReport() throws Exception;
}
