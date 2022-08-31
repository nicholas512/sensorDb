package ca.carleton.gcrc.sensorDb.dbapi;

import java.util.Date;

public class DeviceSensor {

	private String id;
	private String deviceId;
	private String sensorId;
	private Date timestamp;
	private String notes;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getSensorId() {
		return sensorId;
	}
	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
}
