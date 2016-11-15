package ca.carleton.gcrc.sensorDb.dbapi;

import java.util.Date;

public class Observation {

	private String id;
	private String deviceId;
	private String sensorId;
	private String importId;
	private String importKey;
	private String observationType;
	private String unitOfMeasure;
	private Double accuracy;
	private Double precision;
	private Double numericValue;
	private String textValue;
	private Date loggedTime;
	private Date correctedTime;
	private String location;
	private Double minHeight;
	private Double maxHeight;
	private Double elevation;

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

	public String getImportId() {
		return importId;
	}
	public void setImportId(String importId) {
		this.importId = importId;
	}

	public String getImportKey() {
		return importKey;
	}
	public void setImportKey(String importKey) {
		this.importKey = importKey;
	}

	public String getObservationType() {
		return observationType;
	}
	public void setObservationType(String observationType) {
		this.observationType = observationType;
	}

	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}
	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}

	public Double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}

	public Double getPrecision() {
		return precision;
	}
	public void setPrecision(Double precision) {
		this.precision = precision;
	}

	public Double getNumericValue() {
		return numericValue;
	}
	public void setNumericValue(Double numericValue) {
		this.numericValue = numericValue;
	}

	public String getTextValue() {
		return textValue;
	}
	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	public Date getLoggedTime() {
		return loggedTime;
	}
	public void setLoggedTime(Date loggedTime) {
		this.loggedTime = loggedTime;
	}

	public Date getCorrectedTime() {
		return correctedTime;
	}
	public void setCorrectedTime(Date correctedTime) {
		this.correctedTime = correctedTime;
	}

	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}

	public Double getMinHeight() {
		return minHeight;
	}
	public void setMinHeight(Double minHeight) {
		this.minHeight = minHeight;
	}

	public Double getMaxHeight() {
		return maxHeight;
	}
	public void setMaxHeight(Double maxHeight) {
		this.maxHeight = maxHeight;
	}

	public Double getElevation() {
		return elevation;
	}
	public void setElevation(Double elevation) {
		this.elevation = elevation;
	}
	
	public String toString(){
		return "Observation("+id
				+" device:"+deviceId
				+" sensor:"+sensorId
				+" type:"+observationType
				+" time:"+loggedTime
				+" numeric:"+numericValue
				+" text:"+textValue
				+")";
	}
}
