package ca.carleton.gcrc.sensorDb.dbapi;

import java.util.List;

public interface DbAPI {

	String getDeviceIdFromSerialNumber(String serialNumber) throws Exception;

	List<Sensor> getSensorsFromDeviceId(String device_id) throws Exception;

	List<DeviceLocation> getDeviceLocationsFromDeviceId(String device_id) throws Exception;

	List<Location> getLocationsFromDeviceLocations(List<DeviceLocation> deviceLocations) throws Exception;

	Location getLocationFromLocationId(String locationId) throws Exception;

	List<ImportRecord> getImportRecords() throws Exception;
}
