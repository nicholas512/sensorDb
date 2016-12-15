package ca.carleton.gcrc.sensorDb.dbapi;

import java.io.StringWriter;
import java.util.Date;

public class Device {

	private String id;
	private String serialNumber;
	private String accessCode;
	private String deviceType;
	private String manufacturer;
	private String manufacturerDeviceName;
	private Date acquiredOn;
	private String notes;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getAccessCode() {
		return accessCode;
	}
	public void setAccessCode(String accessCode) {
		this.accessCode = accessCode;
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

	public Date getAcquiredOn() {
		return acquiredOn;
	}
	public void setAcquiredOn(Date acquiredOn) {
		this.acquiredOn = acquiredOn;
	}

	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public String toString(){
		StringWriter sw = new StringWriter();
		sw.write("Device(");
		sw.write("id="+id);
		sw.write(",sn="+serialNumber);
		sw.write(")");
		
		return sw.toString();
	}
}
