package ca.carleton.gcrc.sensorDb.servlet.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

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
			Integer elevation
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			String geom = String.format("POINT(%f %f)", lng, lat);
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"INSERT INTO locations (name,coordinates,elevation)"
				+" VALUES (?,ST_GeomFromText(?,4326),?)"
				+" RETURNING id,name,ST_AsText(coordinates),elevation"
			);
			
			pstmt.setString(1, name);
			pstmt.setString(2, geom);
			if( null == elevation ){
				pstmt.setNull(3,java.sql.Types.INTEGER);
			} else {
				pstmt.setInt(3, elevation);
			}
			
			ResultSet resultSet = pstmt.executeQuery();
			
			resultSet.next();
			String res_id = resultSet.getString(1);
			String res_name = resultSet.getString(2);
			String res_coordinates = resultSet.getString(3);
			int res_elevation = resultSet.getInt(4);
				
			JSONObject location = buildLocationJson(res_id,res_name,res_coordinates,res_elevation);
			result.put("location", location);
			
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
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,name,ST_AsText(coordinates),elevation FROM locations"
			);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				String res_name = resultSet.getString(2);
				String res_coordinates = resultSet.getString(3);
				int res_elevation = resultSet.getInt(4);
					
				JSONObject location = buildLocationJson(res_id,res_name,res_coordinates,res_elevation);
				
				locationArr.put(location);
			}
			
			resultSet.close();
			
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
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,name,ST_AsText(coordinates),elevation FROM locations WHERE id=?"
			);
			
			UUID uuid = UUID.fromString(location_id);
			
			pstmt.setObject(1, uuid);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				String res_name = resultSet.getString(2);
				String res_coordinates = resultSet.getString(3);
				int res_elevation = resultSet.getInt(4);
					
				JSONObject location = buildLocationJson(res_id,res_name,res_coordinates,res_elevation);
				
				locationArr.put(location);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving location ("+location_id+") from database", e);
		}
		
		result.put("ok", true);

		return result;
	}
	
	/**
	 * Create JSON representation of a location record
	 * @param id
	 * @param name
	 * @param responsible
	 * @param coordinates
	 * @param elevation
	 * @return
	 */
	private JSONObject buildLocationJson(
			String id, 
			String name, 
			String coordinates, 
			int elevation ){
		
		JSONObject location = new JSONObject();
		location.put("type", "location");
		location.put("id", id);
		location.put("name", name);
		location.put("coordinates", coordinates);
		location.put("elevation", elevation);
		return location;
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
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,device_type,manufacturer,manufacturer_device_name,sensor_label,"
				+ "sensor_type_of_measurement,sensor_unit_of_measurement,sensor_accuracy,"
				+ "sensor_precision,sensor_height_in_metres FROM device_sensor_profiles"
			);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			Map<String,List<DeviceTypeFields>> listOfFieldsByType = 
					new HashMap<String,List<DeviceTypeFields>>();
			
			while( resultSet.next() ){
				DeviceTypeFields fields = new DeviceTypeFields();
				
				fields.id = resultSet.getString(1);
				fields.device_type = resultSet.getString(2);
				fields.manufacturer = resultSet.getString(3);
				fields.manufacturer_device_name = resultSet.getString(4);
				fields.sensor_label = resultSet.getString(5);
				fields.sensor_type_of_measurement = resultSet.getString(6);
				fields.sensor_unit_of_measurement = resultSet.getString(7);
				fields.sensor_accuracy = resultSet.getDouble(8);
				fields.sensor_precision = resultSet.getDouble(9);
				fields.sensor_height_in_metres = resultSet.getDouble(10);
					
				List<DeviceTypeFields> listOfFields = listOfFieldsByType.get(fields.manufacturer_device_name);
				if( null == listOfFields ){
					listOfFields = new Vector<DeviceTypeFields>();
					listOfFieldsByType.put(fields.manufacturer_device_name, listOfFields);
				}
				
				listOfFields.add(fields);
			}

			for(List<DeviceTypeFields> listOfFields : listOfFieldsByType.values()){
				JSONObject deviceType = buildDeviceTypeJson(listOfFields);
				
				deviceTypesArr.put(deviceType);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving all device types from database", e);
		}
		
		result.put("ok", true);

		return result;
	}
	
	/**
	 * Return a device type from name
	 * @return
	 * @throws Exception
	 */
	public JSONObject getDeviceTypeFromName(
			String name
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray deviceTypesArr = new JSONArray();
			result.put("deviceTypes", deviceTypesArr);
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,device_type,manufacturer,manufacturer_device_name,sensor_label,"
				+"sensor_type_of_measurement,sensor_unit_of_measurement,sensor_accuracy,"
				+"sensor_precision,sensor_height_in_metres"
				+" FROM device_sensor_profiles"
				+" WHERE manufacturer_device_name=?"
			);
			
			pstmt.setString(1, name);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			List<DeviceTypeFields> listOfFields = new Vector<DeviceTypeFields>();
			
			while( resultSet.next() ){
				DeviceTypeFields fields = new DeviceTypeFields();
				
				fields.id = resultSet.getString(1);
				fields.device_type = resultSet.getString(2);
				fields.manufacturer = resultSet.getString(3);
				fields.manufacturer_device_name = resultSet.getString(4);
				fields.sensor_label = resultSet.getString(5);
				fields.sensor_type_of_measurement = resultSet.getString(6);
				fields.sensor_unit_of_measurement = resultSet.getString(7);
				fields.sensor_accuracy = resultSet.getDouble(8);
				fields.sensor_precision = resultSet.getDouble(9);
				fields.sensor_height_in_metres = resultSet.getDouble(10);
					
				listOfFields.add(fields);
			}

			JSONObject deviceType = buildDeviceTypeJson(listOfFields);
			
			deviceTypesArr.put(deviceType);
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving device type "+name+" from database", e);
		}
		
		result.put("ok", true);

		return result;
	}

	/**
	 * @param listOfFields
	 * @return
	 */
	private JSONObject buildDeviceTypeJson(List<DeviceTypeFields> listOfFields){
		
		JSONObject deviceType = new JSONObject();
		deviceType.put("type", "deviceType");
		
		{
			DeviceTypeFields fields = listOfFields.get(0);
			deviceType.put("device_type", fields.device_type);
			deviceType.put("manufacturer", fields.manufacturer);
			deviceType.put("manufacturer_device_name", fields.manufacturer_device_name);
		}
		
		JSONArray sensors = new JSONArray();
		deviceType.put("sensors", sensors);
		
		for(DeviceTypeFields fields : listOfFields){
			JSONObject sensor = new JSONObject();
			sensors.put(sensor);
			
			sensor.put("label", fields.sensor_label);
			sensor.put("type_of_measurement", fields.sensor_type_of_measurement);
			sensor.put("unit_of_measurement", fields.sensor_unit_of_measurement);
			sensor.put("accuracy", fields.sensor_accuracy);
			sensor.put("precision", fields.sensor_precision);
			sensor.put("height_in_metres", fields.sensor_height_in_metres);
		}

		return deviceType;
	}

	/**
	 * CReate a new device record
	 * @param serialNumber
	 * @param type
	 * @param notes
	 * @return
	 * @throws Exception
	 */
	public JSONObject createDevice(
			String serialNumber, 
			String type,
			Date acquiredOn,
			String notes
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONObject jsonDeviceType = null;
			JSONArray jsonSensorDefs = null;
			{
				JSONObject deviceTypesResult = getDeviceTypeFromName(type);
				JSONArray arr = deviceTypesResult.getJSONArray("deviceTypes");
				if( arr.length() < 1 ){
					throw new Exception("Can not find device type: "+type);
				}
				jsonDeviceType = arr.getJSONObject(0);
				jsonSensorDefs = jsonDeviceType.getJSONArray("sensors");
			}
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"INSERT INTO devices (serial_number,device_type,manufacturer,manufacturer_device_name,acquired_on,notes)"
				+" VALUES (?,?,?,?,?,?)"
				+" RETURNING id,serial_number,device_type,manufacturer,manufacturer_device_name,acquired_on,notes"
			);
			
			pstmt.setString(1, serialNumber);
			pstmt.setString(2, jsonDeviceType.getString("device_type"));
			pstmt.setString(3, jsonDeviceType.getString("manufacturer"));
			pstmt.setString(4, jsonDeviceType.getString("manufacturer_device_name"));
			pstmt.setTimestamp(5, new Timestamp(acquiredOn.getTime()));
			pstmt.setString(6, notes);

			ResultSet resultSet = pstmt.executeQuery();
			
			resultSet.next();
			String res_id = resultSet.getString(1);
			String res_serialNumber = resultSet.getString(2);
			String res_deviceType = resultSet.getString(3);
			String res_manufacturer = resultSet.getString(4);
			String res_manufacturer_device_name = resultSet.getString(5);
			Date res_acquired_on = new Date( resultSet.getTimestamp(6).getTime() );
			String res_notes = resultSet.getString(7);
				
			JSONObject device = buildDeviceJson(
				res_id,
				res_serialNumber,
				res_deviceType,
				res_manufacturer,
				res_manufacturer_device_name,
				res_acquired_on,
				res_notes
				);
			result.put("device", device);
			
			JSONArray sensors = new JSONArray();
			device.put("sensors", sensors);
			
			// Create sensors...
			for(int i=0; i<jsonSensorDefs.length(); ++i){
				JSONObject jsonSensorDef = jsonSensorDefs.getJSONObject(i);
				
				String label = jsonSensorDef.getString("label");
				String type_of_measurement = jsonSensorDef.getString("type_of_measurement");
				String unit_of_measurement = jsonSensorDef.optString("unit_of_measurement");
				double accuracy = jsonSensorDef.getDouble("accuracy");
				double precision = jsonSensorDef.getDouble("precision");
				double height_in_metres = jsonSensorDef.getDouble("height_in_metres");

				JSONObject sensorJson = createDeviceSensor(
						res_id,
						label,
						type_of_measurement,
						unit_of_measurement,
						accuracy,
						precision,
						height_in_metres
						);
				sensors.put(sensorJson);
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
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,serial_number,device_type,manufacturer,manufacturer_device_name,acquired_on,notes"
				+ " FROM devices"
			);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				String res_serialNumber = resultSet.getString(2);
				String res_device_type = resultSet.getString(3);
				String res_manufacturer = resultSet.getString(4);
				String res_manufacturer_device_name = resultSet.getString(5);
				Date res_acquired_on = new Date( resultSet.getTimestamp(6).getTime() );
				String res_notes = resultSet.getString(7);
					
				JSONObject device = buildDeviceJson(
						res_id,
						res_serialNumber,
						res_device_type,
						res_manufacturer,
						res_manufacturer_device_name,
						res_acquired_on,
						res_notes
						);
				
				deviceArr.put(device);
			}
			
			resultSet.close();
			
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
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,serial_number,device_type,manufacturer,manufacturer_device_name,acquired_on,notes"
				+ " FROM devices WHERE id=?"
			);
			
			UUID uuid = UUID.fromString(device_id);
			
			pstmt.setObject(1, uuid);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				String res_serialNumber = resultSet.getString(2);
				String res_device_type = resultSet.getString(3);
				String res_manufacturer = resultSet.getString(4);
				String res_manufacturer_device_name = resultSet.getString(5);
				Date res_acquired_on = new Date( resultSet.getTimestamp(6).getTime() );
				String res_notes = resultSet.getString(7);
					
				JSONObject device = buildDeviceJson(
						res_id,
						res_serialNumber,
						res_device_type,
						res_manufacturer,
						res_manufacturer_device_name,
						res_acquired_on,
						res_notes
						);
				
				JSONArray sensors = getSensorsFromDeviceId(device_id);
				device.put("sensors", sensors);
				
				deviceArr.put(device);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving device ("+device_id+") from database", e);
		}
		
		result.put("ok", true);

		return result;
	}
	
	/**
	 * @param id
	 * @param serialNumber
	 * @param type
	 * @param notes
	 * @return
	 */
	private JSONObject buildDeviceJson(
			String id,
			String serialNumber,
			String deviceType,
			String manufacturer,
			String manufacturer_device_name,
			Date acquired_on,
			String notes 
			){
		
		JSONObject device = new JSONObject();
		device.put("type", "device");
		device.put("id", id);
		device.put("serial_number", serialNumber);
		device.put("device_type", deviceType);
		device.put("manufacturer", manufacturer);
		device.put("manufacturer_device_name", manufacturer_device_name);
		device.put("acquired_on", acquired_on.getTime());
		device.put("acquired_on_text", acquired_on.toString());
		device.put("notes", notes);
		return device;
	}
	
	/**
	 * @param device_id
	 * @param label
	 * @param typeOfMeasurment
	 * @param units
	 * @return
	 * @throws Exception
	 */
	private JSONObject createDeviceSensor(
			String device_id,
			String label,
			String type_of_measurement,
			String unit_of_measurement,
			double accuracy,
			double precision,
			double height_in_metres
			) throws Exception {
		
		JSONObject sensor = null;
		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
					"INSERT INTO sensors (device_id,label,type_of_measurement,unit_of_measurement,accuracy,precision,height_in_metres)"
					+" VALUES (?,?,?,?,?,?,?)"
					+" RETURNING id,device_id,label,type_of_measurement,unit_of_measurement,accuracy,precision,height_in_metres"
				);
				
			pstmt.setObject(1, UUID.fromString(device_id));
			pstmt.setString(2, label);
			pstmt.setString(3, type_of_measurement);
			pstmt.setString(4, unit_of_measurement);
			pstmt.setDouble(5, accuracy);
			pstmt.setDouble(6, precision);
			pstmt.setDouble(7, height_in_metres);

			ResultSet resultSet = pstmt.executeQuery();
			
			resultSet.next();
			String res_id = resultSet.getString(1);
			String res_device_id = resultSet.getString(2);
			String res_label = resultSet.getString(3);
			String res_type_of_measurement = resultSet.getString(4);
			String res_unit_of_measurement = resultSet.getString(5);
			double res_accuracy = resultSet.getDouble(6);
			double res_precision = resultSet.getDouble(7);
			double res_height_in_metres = resultSet.getDouble(8);
				
			sensor = buildSensorJson(
					res_id,
					res_device_id,
					res_label,
					res_type_of_measurement,
					res_unit_of_measurement,
					res_accuracy,
					res_precision,
					res_height_in_metres
					);

		} catch(Exception e) {
			throw new Exception("Error while creating sensor ("+label+") for device ("+device_id+")",e);
		}
		
		return sensor;
	}
	
	/**
	 * @param device_id
	 * @return
	 * @throws Exception  
	 */
	private JSONArray getSensorsFromDeviceId(String device_id) throws Exception {

		JSONArray result = new JSONArray();
		
		try {
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,device_id,label,type_of_measurement,unit_of_measurement,accuracy,precision,height_in_metres"
				+" FROM sensors"
				+" WHERE device_id=?"
			);
			
			pstmt.setObject(1, UUID.fromString(device_id));
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				String res_device_id = resultSet.getString(2);
				String res_label = resultSet.getString(3);
				String res_type_of_measurement = resultSet.getString(4);
				String res_unit_of_measurement = resultSet.getString(5);
				double res_accuracy = resultSet.getDouble(6);
				double res_precision = resultSet.getDouble(7);
				double res_height_in_metres = resultSet.getDouble(8);
					
				JSONObject sensor = buildSensorJson(
						res_id,
						res_device_id,
						res_label,
						res_type_of_measurement,
						res_unit_of_measurement,
						res_accuracy,
						res_precision,
						res_height_in_metres
						);
				
				result.put(sensor);
			}
			
			resultSet.close();
			
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
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,device_id,label,type_of_measurement,unit_of_measurement,accuracy,precision,height_in_metres"
				+" FROM sensors"
			);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				String res_device_id = resultSet.getString(2);
				String res_label = resultSet.getString(3);
				String res_type_of_measurement = resultSet.getString(4);
				String res_unit_of_measurement = resultSet.getString(5);
				double res_accuracy = resultSet.getDouble(6);
				double res_precision = resultSet.getDouble(7);
				double res_height_in_metres = resultSet.getDouble(8);
					
				JSONObject sensor = buildSensorJson(
						res_id,
						res_device_id,
						res_label,
						res_type_of_measurement,
						res_unit_of_measurement,
						res_accuracy,
						res_precision,
						res_height_in_metres
						);
				
				JSONArray arr = map.get(res_device_id);
				if( null == arr ){
					arr = new JSONArray();
					map.put(res_device_id, arr);
				}
				
				arr.put(sensor);
			}
			
			resultSet.close();
			
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
	private JSONObject buildSensorJson(
			String id, 
			String device_id, 
			String label, 
			String type_of_measurement, 
			String unit_of_measurement,
			double accuracy,
			double precision,
			double height_in_meters
			){

		JSONObject location = new JSONObject();
		location.put("type", "sensor");
		location.put("id", id);
		location.put("label", label);
		location.put("type_of_measurement", type_of_measurement);
		location.put("unit_of_measurement", unit_of_measurement);
		location.put("accuracy", accuracy);
		location.put("precision", precision);
		location.put("height_in_meters", height_in_meters);
		return location;
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
			
			// Get Sql Time
			Timestamp dbTime = new Timestamp( time.getTime() );
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"INSERT INTO devices_locations (timestamp,device_id,location_id,notes) VALUES (?,?,?,?)"
				+" RETURNING id,timestamp,device_id,location_id,notes"
			);
			
			pstmt.setTimestamp(1, dbTime);
			pstmt.setObject(2, UUID.fromString(device_id) );
			pstmt.setObject(3, UUID.fromString(location_id) );
			pstmt.setString(4, notes);

			ResultSet resultSet = pstmt.executeQuery();
			
			resultSet.next();
			String res_id = resultSet.getString(1);
			Date res_time = new Date( resultSet.getTimestamp(2).getTime() );
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
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,timestamp,device_id,location_id,notes FROM devices_locations"
			);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				Date res_time = new Date( resultSet.getTimestamp(2).getTime() );
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
	
	/**
	 * @param id
	 * @param time
	 * @param device_id
	 * @param location_id
	 * @param notes
	 * @return
	 */
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
	
	public JSONObject getListOfLogEntries(
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray logEntriesArr = new JSONArray();
			result.put("logEntries", logEntriesArr);
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,timestamp FROM logs"
			);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				Date res_timestamp = new Date( resultSet.getTimestamp(2).getTime() );
					
				JSONObject logEntry = buildLogEntryJson(res_id,res_timestamp);
				
				logEntriesArr.put(logEntry);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving all log entries from database", e);
		}
		
		result.put("ok", true);

		return result;
	}

	private JSONObject buildLogEntryJson(
			String id, 
			Date time
			){
		
		JSONObject logEntry = new JSONObject();
		logEntry.put("type", "logEntry");
		logEntry.put("id", id);
		logEntry.put("timestamp", time.getTime());
		logEntry.put("timestamp_text", time.toString());
		return logEntry;
	}

	public JSONObject getLogFromId(
			String id
			) throws Exception {

		JSONObject result = new JSONObject();
		
		try {
			JSONArray logsArr = new JSONArray();
			result.put("logs", logsArr);
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"SELECT id,timestamp,log FROM logs WHERE id=?"
			);
			
			pstmt.setObject(1, UUID.fromString(id));
			
			ResultSet resultSet = pstmt.executeQuery();
			
			while( resultSet.next() ){
				String res_id = resultSet.getString(1);
				Date res_timestamp = new Date( resultSet.getTimestamp(2).getTime() );
				String res_log = resultSet.getString(3);
					
				JSONObject log = buildLogJson(res_id,res_timestamp,res_log);
				
				logsArr.put(log);
			}
			
			resultSet.close();
			
		} catch (Exception e) {
			throw new Exception("Error retrieving log "+id+" from database", e);
		}
		
		result.put("ok", true);

		return result;
	}

	private JSONObject buildLogJson(
			String id, 
			Date time,
			String log
			){
		
		JSONObject logEntry = new JSONObject();
		logEntry.put("type", "log");
		logEntry.put("id", id);
		logEntry.put("timestamp", time.getTime());
		logEntry.put("timestamp_text", time.toString());
		logEntry.put("log", log);
		return logEntry;
	}
}
