package ca.carleton.gcrc.sensorDb.dbapi;

import java.util.Date;

import org.json.JSONObject;

public class LogRecord {

	private String id;
	private Date timestamp;
	private JSONObject log;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public JSONObject getLog() {
		return log;
	}
	public void setLog(JSONObject log) {
		this.log = log;
	}
}
