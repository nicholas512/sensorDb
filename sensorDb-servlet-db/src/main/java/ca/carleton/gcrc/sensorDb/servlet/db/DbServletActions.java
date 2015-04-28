package ca.carleton.gcrc.sensorDb.servlet.db;

import java.sql.PreparedStatement;

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

		int updateResult = 0;
		
		try {
			String geom = String.format("POINT(%f %f)", lng, lat);
			
			PreparedStatement pstmt = dbConn.getConnection().prepareStatement(
				"INSERT INTO locations (name,responsible_party,coordinates,elevation) VALUES (?,?,ST_GeomFromText(?,4326),?)"
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
			
			updateResult = pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw new Exception("Error inserting location into database", e);
		}
		
		JSONObject result = new JSONObject();
		result.put("ok", true);
		result.put("action", "insert location");
		result.put("result", updateResult);
		return result;
	}

}
