package ca.carleton.gcrc.sensorDb.servlet.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.jdbc.DbConnection;

public class DbServletActions {

	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private JSONObject cached_welcome = null;
	private DbConnection dbConn = null;

	public DbServletActions(DbConnection dbConn){
		this.dbConn = dbConn;
	}
	
	synchronized public JSONObject getWelcome() throws Exception{
		if( null == cached_welcome ){
			cached_welcome = new JSONObject();
			cached_welcome.put("DbServlet", true);
		}
		
		return cached_welcome;
	}

	public JSONObject createLocation(
			String name, 
			String responsible,
			double lat, 
			double lng, 
			Integer elevation
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			String geom = String.format("POINT(%f %f)", lng, lat);
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"INSERT INTO locations (name,responsible_party,coordinates,elevation)"
				+" VALUES (?,?,ST_GeomFromText(?,4326),?)"
				+" RETURNING id,name,responsible_party,ST_AsText(coordinates),elevation"
			);
			
			pstmt.setString(1, name);
			if( null == responsible ){
				pstmt.setNull(2, java.sql.Types.VARCHAR);
			} else {
				pstmt.setString(2, responsible);
			}
			pstmt.setString(3, geom);
			if( null == elevation ){
				pstmt.setNull(4,java.sql.Types.INTEGER);
			} else {
				pstmt.setInt(4, elevation);
			}
			
			ResultSet resultSet = pstmt.executeQuery();
			
			resultSet.next();
			String res_id = resultSet.getString(1);
			String res_name = resultSet.getString(2);
			String res_responsible = resultSet.getString(3);
			String res_coordinates = resultSet.getString(4);
			int res_elevation = resultSet.getInt(5);
				
			JSONObject location = buildLocationJson(res_id,res_name,res_responsible,res_coordinates,res_elevation);
			result.put("location", location);
			
		} catch (Exception e) {
			throw new Exception("Error inserting location into database", e);
		}
		
		result.put("ok", true);
		result.put("action", "insert location");
		return result;
	}

	public JSONObject getLocations(
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray locationArr = new JSONArray();
			result.put("locations", locationArr);
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,name,responsible_party,ST_AsText(coordinates),elevation FROM locations"
			);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				String res_name = resultSet.getString(2);
				String res_responsible = resultSet.getString(3);
				String res_coordinates = resultSet.getString(4);
				int res_elevation = resultSet.getInt(5);
					
				JSONObject location = buildLocationJson(res_id,res_name,res_responsible,res_coordinates,res_elevation);
				
				locationArr.put(location);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving all locations from database", e);
		}
		
		result.put("ok", true);

		return result;
	}

	public JSONObject getLocationFromId(
			String location_id
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray locationArr = new JSONArray();
			result.put("locations", locationArr);
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,name,responsible_party,ST_AsText(coordinates),elevation FROM locations WHERE id=?"
			);
			
			UUID uuid = UUID.fromString(location_id);
			
			pstmt.setObject(1, uuid);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				String res_name = resultSet.getString(2);
				String res_responsible = resultSet.getString(3);
				String res_coordinates = resultSet.getString(4);
				int res_elevation = resultSet.getInt(5);
					
				JSONObject location = buildLocationJson(res_id,res_name,res_responsible,res_coordinates,res_elevation);
				
				locationArr.put(location);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving location ("+location_id+") from database", e);
		}
		
		result.put("ok", true);

		return result;
	}
	
	private JSONObject buildLocationJson(
			String id, 
			String name, 
			String responsible, 
			String coordinates, 
			int elevation ){
		
		JSONObject location = new JSONObject();
		location.put("type", "location");
		location.put("id", id);
		location.put("name", name);
		location.put("responsible", responsible);
		location.put("coordinates", coordinates);
		location.put("elevation", elevation);
		return location;
	}

	public JSONObject createDevice(
			String serialNumber, 
			String type,
			String notes
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"INSERT INTO devices (serial_number,device_type,notes) VALUES (?,?,?)"
				+" RETURNING id,serial_number,device_type,notes"
			);
			
			pstmt.setString(1, serialNumber);
			pstmt.setString(2, type);
			pstmt.setString(3, notes);

			ResultSet resultSet = pstmt.executeQuery();
			
			resultSet.next();
			String res_id = resultSet.getString(1);
			String res_serialNumber = resultSet.getString(2);
			String res_deviceType = resultSet.getString(3);
			String res_Notes = resultSet.getString(4);
				
			JSONObject device = buildDeviceJson(res_id,res_serialNumber,res_deviceType,res_Notes);
			result.put("device", device);
			
		} catch (Exception e) {
			throw new Exception("Error inserting device into database", e);
		}
		
		result.put("ok", true);
		result.put("action", "insert device");
		return result;
	}

	public JSONObject getDevices(
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray deviceArr = new JSONArray();
			result.put("devices", deviceArr);
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,serial_number,device_type,notes FROM devices"
			);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				String res_serialNumber = resultSet.getString(2);
				String res_deviceType = resultSet.getString(3);
				String res_Notes = resultSet.getString(4);
					
				JSONObject device = buildDeviceJson(res_id,res_serialNumber,res_deviceType,res_Notes);
				
				deviceArr.put(device);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving all devices from database", e);
		}
		
		result.put("ok", true);

		return result;
	}

	public JSONObject getDeviceFromId(
			String device_id
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray deviceArr = new JSONArray();
			result.put("devices", deviceArr);
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,serial_number,device_type,notes FROM devices WHERE id=?"
			);
			
			UUID uuid = UUID.fromString(device_id);
			
			pstmt.setObject(1, uuid);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				String res_serialNumber = resultSet.getString(2);
				String res_deviceType = resultSet.getString(3);
				String res_notes = resultSet.getString(4);
					
				JSONObject device = buildDeviceJson(res_id,res_serialNumber,res_deviceType,res_notes);
				
				deviceArr.put(device);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving device ("+device_id+") from database", e);
		}
		
		result.put("ok", true);

		return result;
	}
	
	private JSONObject buildDeviceJson(
			String id, 
			String serialNumber, 
			String type, 
			String notes ){
		
		JSONObject device = new JSONObject();
		device.put("type", "device");
		device.put("id", id);
		device.put("serialNumber", serialNumber);
		device.put("device_type", type);
		device.put("notes", notes);
		return device;
	}

	public JSONObject addDeviceLocation(
			Date time, 
			String device_id,
			String location_id,
			String notes
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			// Check if device_id is valid
			try {
				JSONObject query = getDeviceFromId(device_id);
				JSONArray devices = query.getJSONArray("devices");
				if( devices.length() < 1 ){
					throw new Exception("Device not found");
				}
				if( devices.length() > 1 ){
					throw new Exception("Multiple devices with same identifier were found");
				}
				
			} catch (Exception e) {
				throw new Exception("Error finding device ("+device_id+")",e);
			}
			
			// Check if location_id is valid
			try {
				JSONObject query = getLocationFromId(location_id);
				JSONArray devices = query.getJSONArray("locations");
				if( devices.length() < 1 ){
					throw new Exception("Location not found");
				}
				if( devices.length() > 1 ){
					throw new Exception("Multiple locations with same identifier were found");
				}
				
			} catch (Exception e) {
				throw new Exception("Error finding device ("+device_id+")",e);
			}
			
			// Get Sql Date
			java.sql.Date dbDate = new java.sql.Date( time.getTime() );
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"INSERT INTO devices_locations (timestamp,device_id,location_id,notes) VALUES (?,?,?,?)"
				+" RETURNING id,timestamp,device_id,location_id,notes"
			);
			
			pstmt.setDate(1, dbDate);
			pstmt.setObject(2, UUID.fromString(device_id) );
			pstmt.setObject(3, UUID.fromString(location_id) );
			pstmt.setString(4, notes);

			ResultSet resultSet = pstmt.executeQuery();
			
			resultSet.next();
			String res_id = resultSet.getString(1);
			java.sql.Date res_time_sql = resultSet.getDate(2);
			Date res_time = new Date( res_time_sql.getTime() );
			String res_device_id = resultSet.getString(3);
			String res_location_id = resultSet.getString(4);
			String res_notes = resultSet.getString(5);
				
			JSONObject deviceLocation = buildDeviceLocationJson(res_id,res_time,res_device_id,res_location_id,res_notes);
			result.put("deviceLocation", deviceLocation);
			
		} catch (Exception e) {
			throw new Exception("Error inserting deviceLocation into database", e);
		}
		
		result.put("ok", true);
		result.put("action", "insert device");
		return result;
	}

	public JSONObject getDeviceLocations(
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray deviceLocationArr = new JSONArray();
			result.put("deviceLocations", deviceLocationArr);
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,timestamp,device_id,location_id,notes FROM devices_locations"
			);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				java.sql.Date res_time_sql = resultSet.getDate(2);
				Date res_time = new Date( res_time_sql.getTime() );
				String res_device_id = resultSet.getString(3);
				String res_location_id = resultSet.getString(4);
				String res_notes = resultSet.getString(5);
					
				JSONObject deviceLocation = buildDeviceLocationJson(res_id,res_time,res_device_id,res_location_id,res_notes);
				
				deviceLocationArr.put(deviceLocation);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving all deviceLocations from database", e);
		}
		
		result.put("ok", true);

		return result;
	}
	
	private JSONObject buildDeviceLocationJson(
			String id, 
			Date time,
			String device_id, 
			String location_id, 
			String notes ){
		
		JSONObject device = new JSONObject();
		device.put("type", "deviceLocation");
		device.put("id", id);
		device.put("timestamp", time.getTime());
		device.put("timestamp_text", time.toString());
		device.put("device_id", device_id);
		device.put("location_id", location_id);
		device.put("notes", notes);
		return device;
	}
	
}
