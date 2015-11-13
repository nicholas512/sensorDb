package ca.carleton.gcrc.sensorDb.dbapi;

import java.util.Date;

import org.json.JSONObject;

public class ImportRecord {

	private String id;
	private Date importTime;
	private String fileName;
	private JSONObject importParameters;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public Date getImportTime() {
		return importTime;
	}
	public void setImportTime(Date importTime) {
		this.importTime = importTime;
	}

	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public JSONObject getImportParameters() {
		return importParameters;
	}
	public void setImportParameters(JSONObject importParameters) {
		this.importParameters = importParameters;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		json.put("type", "importRecord");
		
		if( null != id ){
			json.put("id", id);
		}
		
		if( null != importTime ){
			json.put("importTime", importTime.getTime());
		}

		if( null != fileName ){
			json.put("fileName", fileName);
		}

		if( null != importParameters ){
			json.put("importParameters", importParameters);
		}
		
		return json;
	}
}
