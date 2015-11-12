package ca.carleton.gcrc.sensorDb.upload.observations;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.jdbc.DbConnection;

public class ObservationFileImporter {

	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private DbConnection dbConn;
	
	public ObservationFileImporter(DbConnection dbConn){
		this.dbConn = dbConn;
	}
	
	public void importFile(ConversionRequest conversionRequest) throws Exception {
		if( null == conversionRequest ){
			throw new Exception("A conversion request must be provided");
		}

		File file = conversionRequest.getFileToConvert();
		String fileName = null;
		if( null != file ){
			fileName = file.getAbsolutePath();
		}
		
		JSONObject jsonParams = new JSONObject();
		jsonParams.put("initial_offset", conversionRequest.getInitialOffset());
		jsonParams.put("final_offset", conversionRequest.getFinalOffset());
		jsonParams.put("originalFileName", conversionRequest.getOriginalFileName());
		jsonParams.put("importerName", conversionRequest.getImporterName());
		jsonParams.put("notes", conversionRequest.getNotes());
		String paramStr = jsonParams.toString();

		// Record this file into the database
		String importUuid = null;
		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"INSERT INTO imports (import_time,filename,import_parameters) VALUES (?,?,?) RETURNING id"
			);

			pstmt.setTimestamp(1, new Timestamp((new Date()).getTime())); // now
			pstmt.setString(2, file.getName());
			pstmt.setString(3, paramStr);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				importUuid = resultSet.getString(1);
			}
			
			resultSet.close();
		} catch (Exception e) {
			throw new Exception("Error while recording observation file "+fileName,e);
		}
		
		logger.error("Import UUID: "+importUuid);
		
		// Import the file
		FileInputStream fis = null;
		InputStreamReader isr = null;
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis,"UTF-8");
			
			importFile(
					isr
					,importUuid
					,conversionRequest.getInitialOffset()
					,conversionRequest.getFinalOffset()
					);

		} catch (Exception e) {
			throw new Exception("Error while importing observation file "+fileName,e);
		
		} finally {
			if( null != isr ){
				try {
					isr.close();
				} catch(Exception e) {
					// Ignore
				}
			}
			if( null != fis ){
				try {
					fis.close();
				} catch(Exception e) {
					// Ignore
				}
			}
		}
	}

	public void importFile(
			Reader reader
			,String importUUID
			,int initialOffset
			,int finalOffset
			) throws Exception {

		ObservationFileReader obsReader = new ObservationFileReader(reader);
		
		String deviceSerialNumber = obsReader.getDeviceSerialNumber();
		
		String device_id = getDeviceIdFromSerialNumber(deviceSerialNumber);
		Map<String,Sensor> sensorsMap = getSensorsFromDeviceId(device_id);

		// Check that sensors were found for all parsed columns
		for(ObservationColumn column : obsReader.getColumns()){
			if( column.isValue() ){
				if( null == sensorsMap.get( column.getName() ) ){
					throw new Exception("Sensor with label ("+column.getName()+") not found for device ("+deviceSerialNumber+")");
				}
			}
		}

		ObservationFileImportReport report = new ObservationFileImportReportMemory();
		report.setImportId(importUUID);
		
		// Get all observations that should be saved
		List<Observation> observations = new Vector<Observation>();
		Date firstTime = null;
		Date lastTime = null;
		{
			Observation observation = obsReader.read();
			while( null != observation ){
				if( isObservationInDatabase(observation) ) {
					report.skippedObservation(observation);
				} else {
					observations.add(observation);
					
					Date currentTime = observation.getTime();
					if( null == firstTime ){
						firstTime = currentTime;
					} else if( currentTime.getTime() < firstTime.getTime() ){
						firstTime = currentTime;
					}
					if( null == lastTime ){
						lastTime = currentTime;
					} else if( currentTime.getTime() > lastTime.getTime() ){
						lastTime = currentTime;
					}
				}

				observation = obsReader.read();
			}
		}
		
		// Compute a time corrector
		TimeCorrector timeCorrector = new TimeCorrector();
		timeCorrector.setStartTime(firstTime);
		timeCorrector.setEndTime(lastTime);
		timeCorrector.setInitialOffsetInSec(initialOffset);
		timeCorrector.setFinalOffsetInSec(finalOffset);
		
		// Get all the device locations for this device
		List<DeviceLocation> deviceLocations = getDeviceLocationsFromDeviceId(device_id);
		Map<String,Location> locationsById = getLocationsFromDeviceLocations(deviceLocations);
		DeviceLocator deviceLocator = new DeviceLocator(deviceLocations, locationsById);
		
		// Start saving observations
		for( Observation observation : observations ){
			String sensor_label = observation.getColumn().getName();
			Sensor sensor = sensorsMap.get( sensor_label );
			
			insertObservation(importUUID, device_id, sensor, observation, timeCorrector, deviceLocator, report);
		}
		
		saveImportReport(report);
	}

	private String getDeviceIdFromSerialNumber(String serialNumber) throws Exception {
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
	
	private Map<String,Sensor> getSensorsFromDeviceId(String device_id) throws Exception {
		// Sensor UUID keyed by label
		Map<String,Sensor> sensorsMap = new HashMap<String,Sensor>();
		
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
				
				if( null != sensorsMap.get(label) ){
					resultSet.close();
					throw new Exception("Sensor label ("+label+") reported more than once for device ("+device_id+")");
				}
				
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
				
				sensorsMap.put(label, sensor);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving sensors for device (id="+device_id+") from database", e);
		}

		return sensorsMap;
	}
	
	private List<DeviceLocation> getDeviceLocationsFromDeviceId(String device_id) throws Exception {
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
	
	private Map<String,Location> getLocationsFromDeviceLocations(List<DeviceLocation> deviceLocations) throws Exception {
		Map<String,Location> locationsById = new HashMap<String,Location>();
		
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
				locationsById.put(locationId, location);
			}
			
		} catch (Exception e) {
			throw new Exception("Error retrieving locations for device locations from database", e);
		}

		return locationsById;
	}
	
	private Location getLocationFromLocationId(String locationId) throws Exception {
		Location location = null;
		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,name,ST_AsEWKT(coordinates),elevation FROM locations WHERE id=?"
			);
			
			pstmt.setObject(1, UUID.fromString(locationId));
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String location_id = resultSet.getString(1);
				String name = resultSet.getString(2);
				String geometry = resultSet.getString(3);
				int elevation = resultSet.getInt(4);
				
				location = new Location();
				location.setLocationId(location_id);
				location.setName(name);
				location.setGeometry(geometry);
				location.setElevation(elevation);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving location (id="+locationId+") from database", e);
		}

		return location;
	}

	private boolean isObservationInDatabase(Observation observation) throws Exception {
		try {
			boolean inDatabase = false;
			
			String importKey = observation.computeImportKey();
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT sensor_id"
				+" FROM observations"
				+" WHERE import_key=?"
			);
			
			pstmt.setString(1, importKey);
			
			ResultSet res = pstmt.executeQuery();
			
			while( res.next() ){
				// If something matches, we should not insert
				inDatabase = true;
			}
			
			return inDatabase;
			
		} catch (Exception e) {
			throw new Exception("Error while looking for matching observation: "+observation.getLine(), e);
		}
	}

	private void insertObservation(
			String importUUID,
			String device_id,
			Sensor sensor,
			Observation observation, 
			TimeCorrector timeCorrector,
			DeviceLocator deviceLocator,
			ObservationFileImportReport report
			) throws Exception {
		
		// insert into observations (device_id,sensor_id,location) values ('123','456',ST_GeomFromEWKT('srid=4326;POINT(0 0)'));
		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"INSERT INTO observations"
				+ " (device_id,sensor_id,import_id,import_key,observation_type,unit_of_measure,accuracy,"
				+ "precision,numeric_value,text_value,logged_time,corrected_utc_time,location,"
				+ "height_min_metres,height_max_metres)"
				+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,ST_GeomFromEWKT(?),?,?)"
			);
			
			Date loggerTime = observation.getTime();
			Date correctedTime = timeCorrector.correctTime(loggerTime);
			
			Location location = deviceLocator.getLocationFromTimestamp(correctedTime);
			if( null == location ){
				throw new Exception("Can not find location of device (id="+device_id+") for time "+correctedTime.toString());
			}
			String geometry = location.getGeometry();
			
			pstmt.setObject(1, UUID.fromString(device_id)); // device_id
			pstmt.setObject(2, UUID.fromString(sensor.getId())); // sensor_id
			pstmt.setObject(3, UUID.fromString(importUUID)); // import_id
			pstmt.setString(4, observation.computeImportKey()); // import_key
			pstmt.setString(5, sensor.getTypeOfMeasurement()); // observation_type
			pstmt.setString(6, sensor.getUnitOfMeasurement()); // unit_of_measure
			pstmt.setDouble(7, sensor.getAccuracy()); // accuracy
			pstmt.setDouble(8, sensor.getPrecision()); // precision

			if( null == observation.getValue() ){
				pstmt.setNull(9, java.sql.Types.DOUBLE); // numeric value
			} else {
				pstmt.setDouble(9, observation.getValue()); // numeric value
			}

			if( null == observation.getText() ){
				pstmt.setNull(10, java.sql.Types.VARCHAR); // text value
			} else {
				pstmt.setString(10, observation.getText()); // text value
			}

			pstmt.setTimestamp(11, new Timestamp(loggerTime.getTime())); // logged_time
			pstmt.setTimestamp(12, new Timestamp(correctedTime.getTime())); // corrected_utc_time
			pstmt.setString(13, geometry); // location
			pstmt.setDouble(14, sensor.getHeightInMetres()); // height_min_metres
			pstmt.setDouble(15, sensor.getHeightInMetres()); // height_max_metres
			
			pstmt.executeUpdate();
			
			report.insertedObservation(observation);
			
		} catch (Exception e) {
			throw new Exception("Error inserting observation for sensor (id="+sensor.getId()+") to database", e);
		}
	}

	private void saveImportReport(ObservationFileImportReport report) throws Exception {
		Date time = new Date(); // now
		
		try {
			String log = report.produceReport();
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"INSERT INTO logs (timestamp,log) VALUES (?,?)"
			);
			
			pstmt.setTimestamp(1, new Timestamp(time.getTime()));
			pstmt.setString(2, log);
			
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw new Exception("Error inserting log to database", e);
		}
	}
}
