package ca.carleton.gcrc.sensorDb.servlet.db;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.dbapi.DbAPI;
import ca.carleton.gcrc.sensorDb.dbapi.Device;
import ca.carleton.gcrc.sensorDb.dbapi.DeviceLocation;
import ca.carleton.gcrc.sensorDb.dbapi.DeviceSensorProfile;
import ca.carleton.gcrc.sensorDb.dbapi.ImportRecord;
import ca.carleton.gcrc.sensorDb.dbapi.Location;
import ca.carleton.gcrc.sensorDb.dbapi.LogRecord;
import ca.carleton.gcrc.sensorDb.dbapi.Sensor;
import ca.carleton.gcrc.sensorDb.jdbc.DbConnection;

public class DbServletActions {

	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private JSONObject cached_welcome = null;
	// private DbConnection dbConn = null;
	private DbAPI dbAPI = null;

	public DbServletActions(DbConnection dbConn){
		//this.dbConn = dbConn;
		this.dbAPI = dbConn.getAPI();
	}
	
	synchronized public JSONObject getWelcome() throws Exception{
		if( null == cached_welcome ){
			cached_welcome = new JSONObject();
			cached_welcome.put("DbServlet", true);
		}
		
		return cached_welcome;
	}

	/**
	 * Create a new location record
	 * @param name
	 * @param responsible
	 * @param lat
	 * @param lng
	 * @param elevation
	 * @return
	 * @throws Exception
	 */
	public JSONObject createLocation(
			String name, 
			double lat, 
			double lng, 
			Integer elevation,
			String comment,
			boolean recordingObservations
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			String geom = String.format("POINT(%f %f)", lng, lat);

			Location location = new Location();
			location.setName(name);
			location.setGeometry(geom);
			location.setElevation(elevation);
			location.setComment(comment);
			location.setRecordingObservations(recordingObservations);

			location = dbAPI.createLocation(location);
				
			JSONObject jsonLocation = buildLocationJson(location);
			result.put("location", jsonLocation);
			
		} catch (Exception e) {
			throw new Exception("Error inserting location into database", e);
		}
		
		result.put("ok", true);
		result.put("action", "insert location");
		return result;
	}

	/**
	 * Returns all location records
	 * @return
	 * @throws Exception
	 */
	public JSONObject getLocations(
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray locationArr = new JSONArray();
			result.put("locations", locationArr);
			
			Collection<Location> locations = dbAPI.getLocations();

			for(Location location : locations){
				JSONObject jsonLocation = buildLocationJson(location);
				
				locationArr.put(jsonLocation);
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving all locations from database", e);
		}
		
		result.put("ok", true);

		return result;
	}

	/**
	 * Get a location record from its UUID
	 * @param location_id
	 * @return
	 * @throws Exception
	 */
	public JSONObject getLocationFromId(
			String location_id
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray locationArr = new JSONArray();
			result.put("locations", locationArr);
			
			
			Location location = dbAPI.getLocationFromLocationId(location_id);
			if( null != location ){
				JSONObject jsonLocation = buildLocationJson(location);
				
				locationArr.put(jsonLocation);
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving location ("+location_id+") from database", e);
		}
		
		result.put("ok", true);

		return result;
	}
	
	/**
	 * Create JSON representation of a location record
	 * @param location
	 * @return
	 */
	private JSONObject buildLocationJson(Location location){
		
		JSONObject result = new JSONObject();
		result.put("type", "location");
		result.put("id", location.getId());
		result.put("name", location.getName());
		result.put("coordinates", location.getGeometry());
		result.put("elevation", location.getElevation());
		result.put("comment", location.getComment());
		result.put("record_observations", location.isRecordingObservations());
		return result;
	}
	
	/**
	 * Return all device types
	 * @return
	 * @throws Exception
	 */
	public JSONObject getDeviceTypes(
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray deviceTypesArr = new JSONArray();
			result.put("deviceTypes", deviceTypesArr);
			
			Collection<DeviceSensorProfile> profiles = dbAPI.getDeviceSensorProfiles();
			
			Map<String,List<DeviceSensorProfile>> profilesByManufacturerName = 
					new HashMap<String,List<DeviceSensorProfile>>();
			
			for(DeviceSensorProfile profile : profiles){
				List<DeviceSensorProfile> profilesForName = 
						profilesByManufacturerName.get(profile.getManufacturerDeviceName());
				if( null == profilesForName ){
					profilesForName = new Vector<DeviceSensorProfile>();
					profilesByManufacturerName.put(profile.getManufacturerDeviceName(), profilesForName);
				}
				
				profilesForName.add(profile);
			}

			for(List<DeviceSensorProfile> profilesForName : profilesByManufacturerName.values()){
				JSONObject deviceType = buildDeviceTypeJson(profilesForName);
				
				deviceTypesArr.put(deviceType);
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving all device types from database", e);
		}
		
		result.put("ok", true);

		return result;
	}
	
	/**
	 * @param deviceSensorProfiles
	 * @return
	 */
	private JSONObject buildDeviceTypeJson(List<DeviceSensorProfile> deviceSensorProfiles){
		
		JSONObject deviceType = new JSONObject();
		deviceType.put("type", "deviceType");
		
		{
			DeviceSensorProfile profile = deviceSensorProfiles.get(0);
			deviceType.put("device_type", profile.getDeviceType());
			deviceType.put("manufacturer", profile.getManufacturer());
			deviceType.put("manufacturer_device_name", profile.getManufacturerDeviceName());
		}
		
		JSONArray sensors = new JSONArray();
		deviceType.put("sensors", sensors);
		
		for(DeviceSensorProfile profile : deviceSensorProfiles){
			JSONObject sensor = new JSONObject();
			sensors.put(sensor);
			
			sensor.put("label", profile.getSensorLabel());
			sensor.put("type_of_measurement", profile.getSensorTypeOfMeasurement());
			sensor.put("unit_of_measurement", profile.getSensorUnitOfMeasurement());
			sensor.put("accuracy", profile.getSensorAccuracy());
			sensor.put("precision", profile.getSensorPrecision());
			sensor.put("height_in_metres", profile.getSensorHeightInMetres());
		}

		return deviceType;
	}

	/**
	 * CReate a new device record
	 * @param serialNumber
     * @Param accessCode
	 * @param type
	 * @param notes
	 * @return
	 * @throws Exception
	 */
	public JSONObject createDevice(
			String serialNumber,
            String accessCode,
			String type,
			Date acquiredOn,
			String notes
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			Device device = new Device();
			device.setSerialNumber(serialNumber);
			device.setAccessCode(accessCode);
			device.setManufacturerDeviceName(type);
			device.setAcquiredOn(acquiredOn);
			device.setNotes(notes);
			
			device = dbAPI.createDevice(device);
			
				
			JSONObject jsonDevice = buildDeviceJson(device);
			result.put("device", jsonDevice);
			
			JSONArray sensorsArr = new JSONArray();
			jsonDevice.put("sensors", sensorsArr);
			
			// Report associated sensors...
			Collection<Sensor> sensors = dbAPI.getSensorsFromDeviceId(device.getId());
			for(Sensor sensor : sensors){
				JSONObject jsonSensor = buildSensorJson(sensor);
				sensorsArr.put(jsonSensor);
			}
			
		} catch (Exception e) {
			throw new Exception("Error inserting device into database", e);
		}
		
		result.put("ok", true);
		result.put("action", "insert device");
		return result;
	}
	
	/**
	 * Get all device records from database
	 * @return
	 * @throws Exception
	 */
	public JSONObject getDevices(
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray deviceArr = new JSONArray();
			result.put("devices", deviceArr);
			
			Collection<Device> devices = dbAPI.getDevices();

			for(Device device : devices){
				JSONObject jsonDevice = buildDeviceJson(device);
				
				deviceArr.put(jsonDevice);
			}
			
			// Get sensors
			Map<String,JSONArray> sensorMap = getAllSensors();
			for(int i=0; i<deviceArr.length(); ++i){
				JSONObject device = deviceArr.getJSONObject(i);
				
				JSONArray sensors = sensorMap.get(device.getString("id"));
				if( null != sensors ){
					device.put("sensors", sensors);
				}
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving all devices from database", e);
		}
		
		result.put("ok", true);

		return result;
	}

	/**
	 * @param device_id
	 * @return
	 * @throws Exception
	 */
	public JSONObject getDeviceFromId(
			String device_id
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray deviceArr = new JSONArray();
			result.put("devices", deviceArr);
			
			Device device = dbAPI.getDeviceFromId(device_id);
				
			JSONObject jsonDevice = buildDeviceJson(device);
			
			JSONArray sensors = getSensorsFromDeviceId(device_id);
			jsonDevice.put("sensors", sensors);
			
			deviceArr.put(jsonDevice);
			
		} catch (Exception e) {
			throw new Exception("Error retrieving device ("+device_id+") from database", e);
		}
		
		result.put("ok", true);

		return result;
	}
	
	/**
	 * @param id
	 * @param serialNumber
     * @Param accessCode
	 * @param type
	 * @param notes
	 * @return
	 */
	private JSONObject buildDeviceJson(Device device){
		
		JSONObject jsonDevice = new JSONObject();
		jsonDevice.put("type", "device");
		jsonDevice.put("id", device.getId());
		jsonDevice.put("serial_number", device.getSerialNumber());
        jsonDevice.put("access_code", device.getAccessCode());
		jsonDevice.put("device_type", device.getDeviceType());
		jsonDevice.put("manufacturer", device.getManufacturer());
		jsonDevice.put("manufacturer_device_name", device.getManufacturerDeviceName());
		jsonDevice.put("acquired_on", device.getAcquiredOn().getTime());
		jsonDevice.put("acquired_on_text", DateUtils.getUtcDateString(device.getAcquiredOn()));
		jsonDevice.put("notes", device.getNotes());
		return jsonDevice;
	}
	
	/**
	 * @param device_id
	 * @return
	 * @throws Exception  
	 */
	private JSONArray getSensorsFromDeviceId(String device_id) throws Exception {

		JSONArray result = new JSONArray();
		
		try {
			
			Collection<Sensor> sensors = dbAPI.getSensorsFromDeviceId(device_id);
			
			for(Sensor sensor : sensors){
				JSONObject jsonSensor = buildSensorJson(sensor);
				result.put(jsonSensor);
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving sensors for device ("+device_id+") from database", e);
		}
		
		return result;
	}
	
	/**
	 * @param device_id
	 * @return
	 * @throws Exception  
	 */
	private Map<String,JSONArray> getAllSensors() throws Exception {

		Map<String,JSONArray> map = new HashMap<String,JSONArray>();
		
		try {
			Collection<Sensor> sensors = dbAPI.getSensors();

			for(Sensor sensor : sensors){
				JSONObject jsonSensor = buildSensorJson(sensor);
				
				JSONArray arr = map.get(sensor.getDeviceId());
				if( null == arr ){
					arr = new JSONArray();
					map.put(sensor.getDeviceId(), arr);
				}
				
				arr.put(jsonSensor);
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving all sensors from database", e);
		}
		
		return map;
	}
	
	/**
	 * @param id
	 * @param device_id
	 * @param label
	 * @param typeOfMeasurement
	 * @param unitOfMeasurement
	 * @return
	 */
	private JSONObject buildSensorJson(Sensor sensor){

		JSONObject jsonSensor = new JSONObject();
		jsonSensor.put("type", "sensor");
		jsonSensor.put("id", sensor.getId());
		jsonSensor.put("label", sensor.getLabel());
		jsonSensor.put("type_of_measurement", sensor.getTypeOfMeasurement());
		jsonSensor.put("unit_of_measurement", sensor.getUnitOfMeasurement());
		jsonSensor.put("accuracy", sensor.getAccuracy());
		jsonSensor.put("precision", sensor.getPrecision());
		jsonSensor.put("height_in_meters", sensor.getHeightInMetres());
		return jsonSensor;
	}

	/**
	 * @param time
	 * @param device_id
	 * @param location_id
	 * @param notes
	 * @return
	 * @throws Exception
	 */
	public JSONObject addDeviceLocation(
			Date time, 
			String device_id,
			String location_id,
			String notes
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			DeviceLocation deviceLocation = new DeviceLocation();
			deviceLocation.setDeviceId(device_id);
			deviceLocation.setLocationId(location_id);
			deviceLocation.setTimestamp(time);
			deviceLocation.setNotes(notes);
			
			deviceLocation = dbAPI.createDeviceLocation(deviceLocation);
				
			JSONObject jsonDeviceLocation = buildDeviceLocationJson(deviceLocation);
			result.put("deviceLocation", jsonDeviceLocation);
			
		} catch (Exception e) {
			throw new Exception("Error inserting deviceLocation into database", e);
		}
		
		result.put("ok", true);
		result.put("action", "insert device");
		return result;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public JSONObject getDeviceLocations(
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray deviceLocationArr = new JSONArray();
			result.put("deviceLocations", deviceLocationArr);

			Collection<DeviceLocation> deviceLocations = dbAPI.getDeviceLocations();

			for(DeviceLocation deviceLocation : deviceLocations){
				JSONObject jsonDeviceLocation = buildDeviceLocationJson(deviceLocation);
				
				deviceLocationArr.put(jsonDeviceLocation);
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving all deviceLocations from database", e);
		}
		
		result.put("ok", true);

		return result;
	}
	
	/**
	 * @param deviceLocation
	 * @return
	 */
	private JSONObject buildDeviceLocationJson(DeviceLocation deviceLocation){
		
		JSONObject jsonDeviceLocation = new JSONObject();
		jsonDeviceLocation.put("type", "deviceLocation");
		jsonDeviceLocation.put("id", deviceLocation.getId());
		jsonDeviceLocation.put("timestamp", deviceLocation.getTimestamp().getTime());
		jsonDeviceLocation.put("timestamp_text", DateUtils.getUtcDateString(deviceLocation.getTimestamp()));
		jsonDeviceLocation.put("device_id", deviceLocation.getDeviceId());
		jsonDeviceLocation.put("location_id", deviceLocation.getLocationId());
		jsonDeviceLocation.put("notes", deviceLocation.getNotes());
		return jsonDeviceLocation;
	}
	
	public JSONObject getImportRecords() throws Exception {
		
		JSONObject result = new JSONObject();
		
		try {
			List<ImportRecord> importRecords = dbAPI.getImportRecords();
			
			JSONArray importRecordsArr = new JSONArray();
			result.put("importRecords", importRecordsArr);

			for(ImportRecord importRecord : importRecords){
				JSONObject jsonImport = importRecord.toJSON();
				importRecordsArr.put(jsonImport);
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving all import records from database", e);
		}
		
		result.put("ok", true);

		return result;
	}
	
	public String getImportFileNameFromImportId(String importId) throws Exception {
		
		String fileName = null;
		
		try {
			ImportRecord importRecord = dbAPI.getImportRecordFromImportId(importId);
			if( null != importRecord ){
				fileName = importRecord.getFileName();
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving file name from import record", e);
		}
		
		return fileName;
	}
	
	public JSONObject getListOfLogEntries(
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray logEntriesArr = new JSONArray();
			result.put("logEntries", logEntriesArr);
			
			List<LogRecord> logRecords = dbAPI.getLogRecords();
			for(LogRecord logRecord : logRecords){
				JSONObject logEntry = buildLogEntryJson(logRecord);
				
				logEntriesArr.put(logEntry);
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving all log entries from database", e);
		}
		
		result.put("ok", true);

		return result;
	}

	public JSONObject getLogFromId(
			String id
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray logsArr = new JSONArray();
			result.put("logs", logsArr);
			
			LogRecord logRecord = dbAPI.getLogRecordFromId(id);
			if( null != logRecord ){
				JSONObject logEntry = buildLogEntryJson(logRecord);
				logsArr.put(logEntry);
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving log "+id+" from database", e);
		}
		
		result.put("ok", true);

		return result;
	}

	private JSONObject buildLogEntryJson(LogRecord logRecord){
		
		JSONObject logEntry = new JSONObject();
		logEntry.put("type", "logEntry");
		logEntry.put("id", logRecord.getId());
		
		if( null != logRecord.getTimestamp() ){
			Date time = logRecord.getTimestamp();
			logEntry.put("timestamp", time.getTime());
			logEntry.put("timestamp_text", DateUtils.getUtcDateString(time));
		}
		
		if( null != logRecord.getLog() ){
			logEntry.put("log", logRecord.getLog());
		}

		return logEntry;
	}
}
