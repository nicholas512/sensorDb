package ca.carleton.gcrc.sensorDb.upload.observations;

public interface ObservationFileImportReport {
	
	void setImportId(String importId);

	void insertedObservation(Observation observation);

	void skippedObservation(Observation observation);
	
	void setError(Throwable err);
	
	String produceReport() throws Exception;
}
