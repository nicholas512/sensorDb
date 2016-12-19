package ca.carleton.gcrc.sensorDb.dbapi;

public class Location {

	private String id;
	private String name;
	private String geometry;
	private double elevation;
	private Double accuracy;
	private String comment;
	private boolean recordingObservations;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGeometry() {
		return geometry;
	}
	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}
	public double getElevation() {
		return elevation;
	}
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public boolean isRecordingObservations() {
		return recordingObservations;
	}
	public void setRecordingObservations(boolean recordingObservations) {
		this.recordingObservations = recordingObservations;
	}
	public Double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}
}
