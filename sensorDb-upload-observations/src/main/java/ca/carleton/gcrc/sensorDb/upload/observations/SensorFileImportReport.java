package ca.carleton.gcrc.sensorDb.upload.observations;

public interface SensorFileImportReport {
	
	void setImportId(String importId);

	void insertedObservation(Sample observation);

	void skippedObservation(Sample observation);
	
	void setError(Throwable err);
	
	String produceReport() throws Exception;
}
