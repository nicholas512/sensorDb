package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.Date;

public class Observation {

	private Date time;
	private ObservationColumn column;
	private double value;
	
	public Observation(Date time, ObservationColumn column, double value){
		this.time = time;
		this.column = column;
		this.value = value;
	}
	
	public Date getTime() {
		return time;
	}
	public ObservationColumn getColumn() {
		return column;
	}
	public double getValue() {
		return value;
	}
	
}
