package ca.carleton.gcrc.sensorDb.dbapi;

public class Location {

	private String locationId;
	private String name;
	private String geometry;
	private int elevation;
	private String comment;
	private boolean recordingObservations;
	
	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
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
	public int getElevation() {
		return elevation;
	}
	public void setElevation(int elevation) {
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
}
