package ca.carleton.gcrc.sensorDb.dbapi;

public class Sensor {

	private String id;
	private String deviceId;
	private String label;
	private String serialNumber;
	private String typeOfMeasurement;
	private String unitOfMeasurement;
	private double accuracy;
	private double precision;
	private double heightInMetres;

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

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getTypeOfMeasurement() {
		return typeOfMeasurement;
	}
	public void setTypeOfMeasurement(String typeOfMeasurement) {
		this.typeOfMeasurement = typeOfMeasurement;
	}

	public String getUnitOfMeasurement() {
		return unitOfMeasurement;
	}
	public void setUnitOfMeasurement(String unitOfMeasurement) {
		this.unitOfMeasurement = unitOfMeasurement;
	}

	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getHeightInMetres() {
		return heightInMetres;
	}
	public void setHeightInMetres(double heightInMetres) {
		this.heightInMetres = heightInMetres;
	}
}
