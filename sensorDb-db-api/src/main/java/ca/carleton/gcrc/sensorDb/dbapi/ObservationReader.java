package ca.carleton.gcrc.sensorDb.dbapi;

public interface ObservationReader {

	Observation read() throws Exception;

	void close() throws Exception;
}
