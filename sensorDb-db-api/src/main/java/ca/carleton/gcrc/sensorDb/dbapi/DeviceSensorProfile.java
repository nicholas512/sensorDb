package ca.carleton.gcrc.sensorDb.dbapi;

public class DeviceSensorProfile {

	private String id;
	private String deviceType;
	private String manufacturer;
	private String manufacturerDeviceName;
	private String sensorLabel;
	private String sensorTypeOfMeasurement;
	private String sensorUnitOfMeasurement;
	private Double sensorAccuracy;
	private Double sensorPrecision;
	private Double sensorHeightInMetres;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getManufacturerDeviceName() {
		return manufacturerDeviceName;
	}
	public void setManufacturerDeviceName(String manufacturerDeviceName) {
		this.manufacturerDeviceName = manufacturerDeviceName;
	}

	public String getSensorLabel() {
		return sensorLabel;
	}
	public void setSensorLabel(String sensorLabel) {
		this.sensorLabel = sensorLabel;
	}

	public String getSensorTypeOfMeasurement() {
		return sensorTypeOfMeasurement;
	}
	public void setSensorTypeOfMeasurement(String sensorTypeOfMeasurement) {
		this.sensorTypeOfMeasurement = sensorTypeOfMeasurement;
	}

	public String getSensorUnitOfMeasurement() {
		return sensorUnitOfMeasurement;
	}
	public void setSensorUnitOfMeasurement(String sensorUnitOfMeasurement) {
		this.sensorUnitOfMeasurement = sensorUnitOfMeasurement;
	}

	public Double getSensorAccuracy() {
		return sensorAccuracy;
	}
	public void setSensorAccuracy(Double sensorAccuracy) {
		this.sensorAccuracy = sensorAccuracy;
	}

	public Double getSensorPrecision() {
		return sensorPrecision;
	}
	public void setSensorPrecision(Double sensorPrecision) {
		this.sensorPrecision = sensorPrecision;
	}

	public Double getSensorHeightInMetres() {
		return sensorHeightInMetres;
	}
	public void setSensorHeightInMetres(Double sensorHeightInMetres) {
		this.sensorHeightInMetres = sensorHeightInMetres;
	}
}
