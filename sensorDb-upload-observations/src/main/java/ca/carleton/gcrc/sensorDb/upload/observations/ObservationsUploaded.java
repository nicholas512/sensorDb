package ca.carleton.gcrc.sensorDb.upload.observations;

import java.security.Principal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.jdbc.DbConnection;
import ca.carleton.gcrc.upload.LoadedFile;
import ca.carleton.gcrc.upload.OnUploadedListener;

public class ObservationsUploaded implements OnUploadedListener {

	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private DbConnection dbConn;
	
	public ObservationsUploaded(DbConnection dbConn){
		this.dbConn = dbConn;
	}
	
	@Override
	public JSONObject onLoad(
			String progressId, 
			List<LoadedFile> uploadedFiles,
			Map<String, List<String>> parameters, 
			Principal userPrincipal,
			Cookie[] cookies
			) throws Exception {

		// Parse the files
		ObservationFileParser parser = new ObservationFileParser();
		for(LoadedFile loadedFile : uploadedFiles){
			logger.info("Uploaded file: "+loadedFile.getFile().getAbsolutePath());

			ObservationFile obsFile = parser.parse(loadedFile.getFile());
			importFile(obsFile);
		}

		JSONObject result = new JSONObject();
		result.put("ok", true);
		return result;
	}
	
	private void importFile(ObservationFile obsFile) throws Exception {
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