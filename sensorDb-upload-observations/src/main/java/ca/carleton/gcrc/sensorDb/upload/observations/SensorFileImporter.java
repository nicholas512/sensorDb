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
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.dbapi.DbAPI;
import ca.carleton.gcrc.sensorDb.dbapi.Device;
import ca.carleton.gcrc.sensorDb.dbapi.DeviceLocation;
import ca.carleton.gcrc.sensorDb.dbapi.ImportReport;
import ca.carleton.gcrc.sensorDb.dbapi.ImportReportMemory;
import ca.carleton.gcrc.sensorDb.dbapi.Location;
import ca.carleton.gcrc.sensorDb.dbapi.LogRecord;
import ca.carleton.gcrc.sensorDb.dbapi.Observation;
import ca.carleton.gcrc.sensorDb.dbapi.Sensor;
import ca.carleton.gcrc.sensorDb.jdbc.DbConnection;

public class SensorFileImporter {

	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private DbConnection dbConn;
	private DbAPI dbAPI;
	
	public SensorFileImporter(DbConnection dbConn){
		this.dbConn = dbConn;
		this.dbAPI = dbConn.getAPI();
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

		ImportReport report = new ImportReportMemory();

		try {
			SensorFileReader obsReader = new SensorFileReader(reader);
			
			String deviceSerialNumber = obsReader.getDeviceSerialNumber();
			
			Device device = dbAPI.getDeviceFromSerialNumber(deviceSerialNumber);
			String device_id = device.getId();
			List<Sensor> sensors = dbAPI.getSensorsFromDeviceId(device_id);
			
			// Make a map of sensors based on label
			Map<String,Sensor> sensorsMap = new HashMap<String,Sensor>();
			for(Sensor sensor : sensors){
				
				if( sensorsMap.containsKey(sensor.getLabel()) ){
					throw new Exception("Multiple sensors with same label ("+sensor.getLabel()
						+") for device ("+deviceSerialNumber+")"
					);
				}
				
				sensorsMap.put(sensor.getLabel(), sensor);
			}

			// Check that sensors were found for all parsed columns
			for(SampleColumn column : obsReader.getColumns()){
				if( column.isValue() ){
					if( null == sensorsMap.get( column.getName() ) ){
						throw new Exception("Sensor with label ("+column.getName()+") not found for device ("+deviceSerialNumber+")");
					}
				}
			}

			report.setImportId(importUUID);
			
			// Get all observations that should be saved
			List<Sample> samples = new Vector<Sample>();
			Date firstTime = null;
			Date lastTime = null;
			{
				Sample sample = obsReader.read();
				while( null != sample ){
					samples.add(sample);
					
					Date currentTime = sample.getTime();
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

					sample = obsReader.read();
				}
			}
			
			// Compute a time corrector
			TimeCorrector timeCorrector = new TimeCorrector();
			timeCorrector.setStartTime(firstTime);
			timeCorrector.setEndTime(lastTime);
			timeCorrector.setInitialOffsetInSec(initialOffset);
			timeCorrector.setFinalOffsetInSec(finalOffset);
			
			// Get all the device locations for this device
			List<DeviceLocation> deviceLocations = dbAPI.getDeviceLocationsFromDeviceId(device_id);
			List<Location> locations = dbAPI.getLocationsFromDeviceLocations(deviceLocations);
			DeviceLocator deviceLocator = new DeviceLocator(deviceLocations, locations);
			
			// Start saving observations
			for( Sample sample : samples ){
				String sensor_label = sample.getColumn().getName();
				Sensor sensor = sensorsMap.get( sensor_label );
				
				insertSample(importUUID, device_id, sensor, sample, timeCorrector, deviceLocator, report);
			}

		} catch (Exception e) {
			
			report.setError(e);
			throw new Exception("Error during import process",e);

		} finally {
			try {
				saveImportReport(report);
			} catch(Exception e2) {
				// Ignore
				logger.error("Unable to save log",e2);
			}
		}
	}

	private boolean isObservationInDatabase(Observation obervation) throws Exception {
		String importKey = null;

		try {
			boolean inDatabase = false;
			
			importKey = obervation.getImportKey();
			if( null != importKey ){
				PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
					"SELECT id"
					+" FROM observations"
					+" WHERE import_key=?"
				);
				
				pstmt.setString(1, importKey);
				
				ResultSet res = pstmt.executeQuery();
				
				while( res.next() ){
					// If something matches, we should not insert
					inDatabase = true;
				}
			}
			
			return inDatabase;
			
		} catch (Exception e) {
			throw new Exception("Error while looking for matching observation: "+importKey, e);
		}
	}

	private void insertSample(
			String importUUID,
			String device_id,
			Sensor sensor,
			Sample sample, 
			TimeCorrector timeCorrector,
			DeviceLocator deviceLocator,
			ImportReport report
			) throws Exception {
		
		// insert into observations (device_id,sensor_id,location) values ('123','456',ST_GeomFromEWKT('srid=4326;POINT(0 0)'));
		try {
			Date loggerTime = sample.getTime();
			Date correctedTime = timeCorrector.correctTime(loggerTime);
			
			Location location = deviceLocator.getLocationFromTimestamp(correctedTime);
			if( null == location ){
				throw new Exception("Can not find location of device (id="+device_id+") for time "+correctedTime.toString());
			}

			String geometry = location.getGeometry();
			
			Observation observation = new Observation();
			observation.setDeviceId( device_id );
			observation.setSensorId( sensor.getId() );
			observation.setImportId( importUUID );
			observation.setImportKey( sample.computeImportKey() );
			observation.setObservationType( sensor.getTypeOfMeasurement() );
			observation.setUnitOfMeasure( sensor.getUnitOfMeasurement() );
			observation.setAccuracy( sensor.getAccuracy() );
			observation.setPrecision( sensor.getPrecision() );
			observation.setNumericValue( sample.getValue() );
			observation.setTextValue( sample.getText() );
			observation.setLoggedTime( loggerTime );
			observation.setCorrectedTime( correctedTime );
			observation.setLocation( geometry );
			observation.setElevation( location.getElevation() );
			observation.setMinHeight( sensor.getHeightInMetres() );
			observation.setMaxHeight( sensor.getHeightInMetres() );
			
			// Insert observation only if this location is meant to record observations.
			// "In Transit" locations should not be saved.
			if( location.isRecordingObservations() ){

				if( isObservationInDatabase(observation) ){
					report.skippedObservation(observation);
				} else {
					observation = dbAPI.createObservation(observation);
					
					report.insertedObservation(observation);
				}

			} else {
				report.skippedObservation(observation);
			};
			
		} catch (Exception e) {
			throw new Exception("Error inserting observation for sensor (id="+sensor.getId()+") to database", e);
		}
	}

	private void saveImportReport(ImportReport report) throws Exception {
		try {
			JSONObject jsonLog = report.produceReport();
			
			LogRecord logRecord = new LogRecord();
			logRecord.setTimestamp( new Date() ); // now
			logRecord.setLog(jsonLog);
			
			dbAPI.createLogRecord(logRecord);
			
		} catch (Exception e) {
			throw new Exception("Error inserting log to database", e);
		}
	}
}
