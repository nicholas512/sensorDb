package ca.carleton.gcrc.sensorDb.servlet.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
			int res_id = resultSet.getInt(1);
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
				int id = resultSet.getInt(1);
				String name = resultSet.getString(2);
				String responsible = resultSet.getString(3);
				String coordinates = resultSet.getString(4);
				int elevation = resultSet.getInt(5);
				
				JSONObject location = buildLocationJson(id,name,responsible,coordinates,elevation);
				
				locationArr.put(location);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving all locations from database", e);
		}
		
		result.put("ok", true);

		return result;
	}
	
	private JSONObject buildLocationJson(
			int id, 
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
			int res_id = resultSet.getInt(1);
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
				int res_id = resultSet.getInt(1);
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
	
	private JSONObject buildDeviceJson(
			int id, 
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
}
