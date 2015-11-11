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
		Map<String,String> sensorsMap = getSensorsFromDeviceId(device_id);

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
		{
			Observation observation = obsReader.read();
			while( null != observation ){
				if( isObservationInDatabase(observation) ) {
					report.skippedObservation(observation);
				} else {
					observations.add(observation);
				}

				observation = obsReader.read();
			}
		}
		
		
		// Start saving observations
		for( Observation observation : observations ){
			String sensor_label = observation.getColumn().getName();
			String sensor_id = sensorsMap.get( sensor_label );
			
			insertObservation(importUUID, device_id, sensor_id, observation, report);
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
	
	private Map<String,String> getSensorsFromDeviceId(String device_id) throws Exception {
		// Sensor UUID keyed by label
		Map<String,String> sensorsMap = new HashMap<String,String>();
		
		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,label FROM sensors WHERE device_id=?"
			);
			
			pstmt.setObject(1, UUID.fromString(device_id));
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String sensor_id = resultSet.getString(1);
				String label = resultSet.getString(2);
				
				if( null != sensorsMap.get(label) ){
					resultSet.close();
					throw new Exception("Sensor label ("+label+") reported more than once for device ("+device_id+")");
				}
				
				sensorsMap.put(label, sensor_id);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving sensors for device (id="+device_id+") from database", e);
		}

		return sensorsMap;
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
			String sensor_id,
			Observation observation, 
			ObservationFileImportReport report
			) throws Exception {
		
		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"INSERT INTO observations"
				+ " (device_id,sensor_id,corrected_utc_time,logged_time,numeric_value,import_id,import_key)"
				+ " VALUES (?,?,?,?,?,?,?)"
			);
			
			Date loggerTime = observation.getTime();
			Date correctedTime = loggerTime;
			
			pstmt.setObject(1, UUID.fromString(device_id));
			pstmt.setObject(2, UUID.fromString(sensor_id));
			pstmt.setTimestamp(3, new Timestamp(correctedTime.getTime()));
			pstmt.setTimestamp(4, new Timestamp(loggerTime.getTime()));
			pstmt.setDouble(5, observation.getValue());
			pstmt.setObject(6, UUID.fromString(importUUID));
			pstmt.setString(7, observation.computeImportKey());
			
			pstmt.executeUpdate();
			
			report.insertedObservation(observation);
			
		} catch (Exception e) {
			throw new Exception("Error inserting observation for sensor (id="+sensor_id+") to database", e);
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
