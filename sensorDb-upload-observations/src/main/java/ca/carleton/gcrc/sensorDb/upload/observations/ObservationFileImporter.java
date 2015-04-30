package ca.carleton.gcrc.sensorDb.upload.observations;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.jdbc.DbConnection;

public class ObservationFileImporter {

	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private DbConnection dbConn;
	
	public ObservationFileImporter(DbConnection dbConn){
		this.dbConn = dbConn;
	}
	
	public void importFile(File file) throws Exception {
		try {
			ObservationFileParser parser = new ObservationFileParser();
			ObservationFile obsFile = parser.parse(file);
			importFile(obsFile);
			
		} catch (Exception e) {
			String fileName = null;
			if( null != file ){
				fileName = file.getAbsolutePath();
			}
			throw new Exception("Error while importing observation file "+fileName,e);
		}
	}

	public void importFile(ObservationFile obsFile) throws Exception {
		String device_id = getDeviceIdFromSerialNumber(obsFile.getSerialNumber());
		Map<String,String> sensorsMap = getSensorsFromDeviceId(device_id);

		// Check that sensors were found for all parsed columns
		for(ObservationColumn column : obsFile.getColumns()){
			if( column.isValue() ){
				if( null == sensorsMap.get( column.getName() ) ){
					throw new Exception("Sensor with label ("+column.getName()+") not found for device ("+obsFile.getSerialNumber()+")");
				}
			}
		}
		
		// All seems fine. Start saving observations
		for(Observation observation : obsFile.getObservations()){
			String sensor_id = sensorsMap.get( observation.getColumn().getName() );
			
			insertObservation(observation.getTime(), sensor_id, observation.getValue());
		}
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

	private void insertObservation(Date time, String sensor_id, double value) throws Exception {
		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"INSERT INTO observations (sensor_id,timestamp,numeric_value) VALUES (?,?,?)"
			);
			
			pstmt.setObject(1, UUID.fromString(sensor_id));
			pstmt.setTimestamp(2, new java.sql.Timestamp(time.getTime()));
			pstmt.setDouble(3, value);
			
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw new Exception("Error inserting observation for sensor (id="+sensor_id+") to database", e);
		}
	}
}
