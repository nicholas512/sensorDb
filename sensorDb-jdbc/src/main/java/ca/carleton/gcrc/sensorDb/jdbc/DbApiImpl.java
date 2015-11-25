package ca.carleton.gcrc.sensorDb.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONObject;

import ca.carleton.gcrc.sensorDb.dbapi.DbAPI;
import ca.carleton.gcrc.sensorDb.dbapi.DeviceLocation;
import ca.carleton.gcrc.sensorDb.dbapi.ImportRecord;
import ca.carleton.gcrc.sensorDb.dbapi.Location;
import ca.carleton.gcrc.sensorDb.dbapi.LogRecord;
import ca.carleton.gcrc.sensorDb.dbapi.Sensor;

public class DbApiImpl implements DbAPI {

	private DbConnection dbConn;
	
	public DbApiImpl(DbConnection connection){
		this.dbConn = connection;
	}

	@Override
	public String getDeviceIdFromSerialNumber(String serialNumber) throws Exception {
		String device_id = null;
		
		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id FROM devices WHERE serial_number=?"
			);
			
			pstmt.setString(1, serialNumber);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			boolean found = false;
			while( resultSet.next() ){
				if( found ){
					resultSet.close();
					throw new Exception("More than one device with serial number: "+serialNumber);
				}
				
				found = true;
				device_id = resultSet.getString(1);
			}
			
			resultSet.close();
			
			if( !found ){
				throw new Exception("Can not find device with serial number: "+serialNumber);
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving device (sn="+serialNumber+") from database", e);
		}

		return device_id;
	}

	@Override
	public List<Sensor> getSensorsFromDeviceId(String device_id) throws Exception {
		List<Sensor> sensors = new Vector<Sensor>();
		
		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,device_id,label,type_of_measurement,unit_of_measurement,"
				+ "accuracy,precision,height_in_metres,serial_number"
				+ " FROM sensors"
				+ " WHERE device_id=?"
			);
			
			pstmt.setObject(1, UUID.fromString(device_id));
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String id = resultSet.getString(1);
				String deviceId = resultSet.getString(2);
				String label = resultSet.getString(3);
				String typeOfMeasurement = resultSet.getString(4);
				String unitOfMeasurement = resultSet.getString(5);
				double accuracy = resultSet.getDouble(6);
				double precision = resultSet.getDouble(7);
				double heightInMetres = resultSet.getDouble(8);
				String serialNumber = resultSet.getString(9);
				
				Sensor sensor = new Sensor();
				sensor.setId(id);
				sensor.setDeviceId(deviceId);
				sensor.setLabel(label);
				sensor.setTypeOfMeasurement(typeOfMeasurement);
				sensor.setUnitOfMeasurement(unitOfMeasurement);
				sensor.setAccuracy(accuracy);
				sensor.setPrecision(precision);
				sensor.setHeightInMetres(heightInMetres);
				sensor.setSerialNumber(serialNumber);
				
				sensors.add(sensor);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving sensors for device (id="+device_id+") from database", e);
		}

		return sensors;
	}
	
	@Override
	public List<DeviceLocation> getDeviceLocationsFromDeviceId(String device_id) throws Exception {
		List<DeviceLocation> deviceLocations = new Vector<DeviceLocation>();

		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,device_id,location_id,timestamp,notes FROM devices_locations WHERE device_id=?"
			);
			
			pstmt.setObject(1, UUID.fromString(device_id));
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String id = resultSet.getString(1);
				String deviceId = resultSet.getString(2);
				String locationId = resultSet.getString(3);
				Date timestamp = resultSet.getTimestamp(4);
				String notes = resultSet.getString(5);

				DeviceLocation deviceLocation = new DeviceLocation();
				deviceLocation.setId(id);
				deviceLocation.setDeviceId(deviceId);
				deviceLocation.setLocationId(locationId);
				deviceLocation.setTimestamp(timestamp);
				deviceLocation.setNotes(notes);
				
				deviceLocations.add(deviceLocation);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving device locations for device (id="+device_id+") from database", e);
		}

		return deviceLocations;
	}
	
	@Override
	public List<Location> getLocationsFromDeviceLocations(List<DeviceLocation> deviceLocations) throws Exception {
		List<Location> locations = new Vector<Location>();
		
		try {
			// Accumulate all location ids
			Set<String> locationIds = new HashSet<String>();
			for(DeviceLocation deviceLocation : deviceLocations){
				String locationId = deviceLocation.getLocationId();
				if( null != locationId ){
					locationIds.add(locationId);
				}
			}
			
			for(String locationId : locationIds){
				Location location = getLocationFromLocationId(locationId);
				locations.add(location);
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving locations for device locations from database", e);
		}

		return locations;
	}
	
	@Override
	public Location getLocationFromLocationId(String locationId) throws Exception {
		Location location = null;
		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,name,ST_AsEWKT(coordinates),elevation,comment,record_observations"
				+ " FROM locations"
				+ " WHERE id=?"
			);
			
			pstmt.setObject(1, UUID.fromString(locationId));
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String location_id = resultSet.getString(1);
				String name = resultSet.getString(2);
				String geometry = resultSet.getString(3);
				int elevation = resultSet.getInt(4);
				String comment = resultSet.getString(5);
				boolean recordingObservations = resultSet.getBoolean(6);
				
				location = new Location();
				location.setLocationId(location_id);
				location.setName(name);
				location.setGeometry(geometry);
				location.setElevation(elevation);
				location.setComment(comment);
				location.setRecordingObservations(recordingObservations);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving location (id="+locationId+") from database", e);
		}

		return location;
	}

	@Override
	public List<ImportRecord> getImportRecords() throws Exception {
		List<ImportRecord> importRecords = new Vector<ImportRecord>();

		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,import_time,filename,import_parameters FROM imports"
			);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String id = resultSet.getString(1);
				Date importTime = resultSet.getTimestamp(2);
				String fileName = resultSet.getString(3);
				String importParametersStr = resultSet.getString(4);
				
				JSONObject importParameters = new JSONObject(importParametersStr);

				ImportRecord importRecord = new ImportRecord();
				importRecord.setId(id);
				importRecord.setImportTime(importTime);
				importRecord.setFileName(fileName);
				importRecord.setImportParameters(importParameters);
				
				importRecords.add(importRecord);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving import records from database", e);
		}

		return importRecords;
	}

	@Override
	public ImportRecord getImportRecordFromImportId(String importId) throws Exception {
		ImportRecord importRecord = null;

		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,import_time,filename,import_parameters"
				+ " FROM imports"
				+ " WHERE id=?"
			);
			
			pstmt.setObject(1, UUID.fromString(importId));
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String id = resultSet.getString(1);
				Date importTime = resultSet.getTimestamp(2);
				String fileName = resultSet.getString(3);
				String importParametersStr = resultSet.getString(4);
				
				JSONObject importParameters = new JSONObject(importParametersStr);

				importRecord = new ImportRecord();
				importRecord.setId(id);
				importRecord.setImportTime(importTime);
				importRecord.setFileName(fileName);
				importRecord.setImportParameters(importParameters);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving import record (id="+importId+") from database", e);
		}

		return importRecord;
	}

	@Override
	public List<LogRecord> getLogRecords() throws Exception {
		List<LogRecord> logRecords = new Vector<LogRecord>();

		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,timestamp,log FROM logs"
			);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String id = resultSet.getString(1);
				Date timestamp = resultSet.getTimestamp(2);
				String logText = resultSet.getString(3);
				
				JSONObject logObj = new JSONObject(logText);

				LogRecord logRecord = new LogRecord();
				logRecord.setId(id);
				logRecord.setTimestamp(timestamp);
				logRecord.setLog(logObj);
				
				logRecords.add(logRecord);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving log records from database", e);
		}

		return logRecords;
	}

	@Override
	public LogRecord getLogRecordFromId(String logId) throws Exception {
		LogRecord logRecord = null;

		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,timestamp,log FROM logs WHERE id=?"
			);
			
			pstmt.setObject(1, UUID.fromString(logId));
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String id = resultSet.getString(1);
				Date timestamp = resultSet.getTimestamp(2);
				String logText = resultSet.getString(3);
				
				JSONObject logObj = new JSONObject(logText);

				logRecord = new LogRecord();
				logRecord.setId(id);
				logRecord.setTimestamp(timestamp);
				logRecord.setLog(logObj);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving log record (id="+logId+") from database", e);
		}

		return logRecord;
	}
}
