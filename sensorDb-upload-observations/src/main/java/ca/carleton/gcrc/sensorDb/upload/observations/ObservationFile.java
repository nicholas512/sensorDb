package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.List;
import java.util.Vector;

public class ObservationFile {

	private String serialNumber = null;
	private List<ObservationColumn> columns = new Vector<ObservationColumn>();
	private List<Observation> observations = new Vector<Observation>();

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public List<ObservationColumn> getColumns() {
		return columns;
	}
	
	public void addColumn(ObservationColumn column){
		columns.add(column);
	}

	public List<Observation> getObservations() {
		return observations;
	}
	
	public void addObservation(Observation observation){
		observations.add(observation);
	}
}
