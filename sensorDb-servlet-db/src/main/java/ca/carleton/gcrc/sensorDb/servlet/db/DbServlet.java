package ca.carleton.gcrc.sensorDb.servlet.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.jdbc.DbConnection;

@SuppressWarnings("serial")
public class DbServlet extends HttpServlet {

	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

//	private DbConnection dbConn = null;
	private DbServletActions actions = null;
	private File mediaDir = null;
	
	public DbServlet(DbConnection dbConn, File mediaDir){
//		this.dbConn = dbConn;
		
		this.actions = new DbServletActions(dbConn);
		this.mediaDir = mediaDir;
	}
	
	public void init(ServletConfig config) throws ServletException {
		//ServletContext context = config.getServletContext();
		
		logger.info(this.getClass().getSimpleName()+" servlet initialization - start");
		

		logger.info(this.getClass().getSimpleName()+" servlet initialization - completed");
	}

	public void destroy() {
		try {

		} catch (Exception e) {
			logger.error("Unable to shutdown agreement worker", e);
		}
		
	}

	@Override
	protected void doGet(
		HttpServletRequest req
		,HttpServletResponse resp
		) throws ServletException, IOException {
		
		try {
			List<String> path = computeRequestPath(req);
			
			if( path.size() < 1 ) {
				JSONObject result = actions.getWelcome();
				sendJsonResponse(resp, result);

			} else if( path.size() == 1 && path.get(0).equals("getLocations") ) {
				JSONObject result = actions.getLocations();
				sendJsonResponse(resp, result);

			} else if( path.size() == 1 && path.get(0).equals("getDeviceTypes") ) {
				JSONObject result = actions.getDeviceTypes();
				sendJsonResponse(resp, result);

			} else if( path.size() == 1 && path.get(0).equals("getDevices") ) {
				JSONObject result = actions.getDevices();
				sendJsonResponse(resp, result);

			} else if( path.size() == 1 && path.get(0).equals("getDeviceLocations") ) {
				JSONObject result = actions.getDeviceLocations();
				sendJsonResponse(resp, result);

			} else if( path.size() == 1 && path.get(0).equals("getListOfLogEntries") ) {
				JSONObject result = actions.getListOfLogEntries();
				sendJsonResponse(resp, result);

			} else if( path.size() == 1 && path.get(0).equals("getImportRecords") ) {
				JSONObject result = actions.getImportRecords();
				sendJsonResponse(resp, result);

			} else if( path.size() >= 2 && path.get(0).equals("getImportFile") ) {
				String importId = path.get(1);
				
				String fileName = actions.getImportFileNameFromImportId(importId);
				File file = null;
				if( null != fileName ){
					file = new File(mediaDir, fileName);
					
					if( false == file.exists() ){
						throw new Exception("File "+fileName+" with import "+importId+" is not found");
					}
				}

				if( null == file ) {
					resp.setStatus(404); // not found
				} else {
					resp.setStatus(200);
				}
				
				resp.setContentType("text/file");
				resp.setCharacterEncoding("utf-8");
				resp.addHeader("Cache-Control", "no-cache");
				resp.addHeader("Pragma", "no-cache");
				resp.addHeader("Expires", "-1");
				
				ServletOutputStream os = resp.getOutputStream();
				FileInputStream fis = new FileInputStream(file);
				int b = fis.read();
				while( b >= 0 ){
					os.write(b);
					b = fis.read();
				}
				os.flush();
				fis.close();

			} else if( path.size() >= 2 && path.get(0).equals("getObservationsByImportId") ) {
				String importId = path.get(1);
				
				resp.setContentType("text/csv");
				resp.setCharacterEncoding("utf-8");
				resp.addHeader("Cache-Control", "no-cache");
				resp.addHeader("Pragma", "no-cache");
				resp.addHeader("Expires", "-1");
				
				ServletOutputStream os = resp.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os,"UTF-8");
				
				actions.getObservationsFromImportId(importId, osw);
				
				osw.flush();

			} else if( path.size() == 1 && path.get(0).equals("getLog") ) {
				String id = getStringParameter(req, "id");
				JSONObject result = actions.getLogFromId(id);
				sendJsonResponse(resp, result);
				
			} else {
				throw new Exception("Invalid action requested");
			}
			
		} catch(Exception e) {
			reportError(e, resp);
		}
	}
	
	@Override
	protected void doPost(
		HttpServletRequest req
		,HttpServletResponse resp
		) throws ServletException, IOException {

		try {
			List<String> path = computeRequestPath(req);
			
			if( path.size() == 1 && path.get(0).equals("createLocation") ) {

				String name = getStringParameter(req, "name");
				String geomType = getStringParameter(req, "geomType");
				Integer elevation = optIntegerParameter(req, "elevation");
				Double accuracy = optDoubleParameter(req, "accuracy");
				String comment = optStringParameter(req, "comment");
				boolean recordingObservations = getCheckboxParameter(req, "record_observations");

				String wkt = null;
				if( "longlat".equals(geomType) ){
					double lat = getDoubleParameter(req, "lat");
					double lng = getDoubleParameter(req, "lng");
					wkt = String.format("SRID=4326;POINT(%f %f)", lng, lat);
					
				} else if( "wkt".equals(geomType) ){
					wkt = "SRID=4326;"+getStringParameter(req, "wkt");
					
				} else {
					throw new Exception("Unkonwn geometry type: "+geomType);
				}
				
				JSONObject result = actions.createLocation(
						name
						,wkt
						,elevation
						,accuracy
						,comment
						,recordingObservations
						);
				sendJsonResponse(resp, result);

			} else if( path.size() == 1 && path.get(0).equals("createDevice") ) {

					String serialNumber = getStringParameter(req, "serial_number");
                    String accessCode = getStringParameter(req, "access_code");
					String deviceType = getStringParameter(req, "device_type");
					Date acquiredOn = getDateParameter(req, "acquired_on");
					String notes = getStringParameter(req, "notes");

					JSONObject result = actions.createDevice(
							serialNumber
                            ,accessCode
							,deviceType
							,acquiredOn
							,notes
							);
					sendJsonResponse(resp, result);

			} else if( path.size() == 1 && path.get(0).equals("addDeviceLocation") ) {

					Date time = getDateParameter(req, "time");
					String device_id = getStringParameter(req, "device_id");
					String location_id = getStringParameter(req, "location_id");
					String notes = getStringParameter(req, "notes");

					JSONObject result = actions.addDeviceLocation(
							time,
							device_id,
							location_id,
							notes
							);
					sendJsonResponse(resp, result);

			} else {
				throw new Exception("Invalid action requested");
			}
			
		} catch(Exception e) {
			logger.error("POST error",e);
			reportError(e, resp);
		}
	}
	
	private void sendJsonResponse(HttpServletResponse resp, JSONObject result) throws Exception {
		if( null == result ) {
			resp.setStatus(304); // not modified
		} else {
			resp.setStatus(200);
		}
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf-8");
		resp.addHeader("Cache-Control", "no-cache");
		resp.addHeader("Pragma", "no-cache");
		resp.addHeader("Expires", "-1");
		
		if( null != result ) {
			OutputStreamWriter osw = new OutputStreamWriter(resp.getOutputStream(), "UTF-8");
			result.write(osw);
			osw.flush();
		}
		
	}

	private void reportError(Throwable t, HttpServletResponse resp) throws ServletException {
		try {
			resp.setStatus(400);
			resp.setContentType("application/json");
			resp.setCharacterEncoding("utf-8");
			resp.addHeader("Cache-Control", "must-revalidate");
			
			JSONObject errorObj = new JSONObject();
			errorObj.put("error", t.getMessage());
			
			int limit = 15;
			Throwable cause = t;
			JSONObject causeObj = errorObj;
			while( null != cause && limit > 0 ){
				--limit;
				cause = cause.getCause();
				
				if( null != cause ){
					JSONObject causeErr = new JSONObject();
					causeErr.put("error", cause.getMessage());
					causeObj.put("cause", causeErr);
					
					causeObj = causeErr;
				}
			}
			
			OutputStreamWriter osw = new OutputStreamWriter(resp.getOutputStream(), "UTF-8");
			errorObj.write(osw);
			osw.flush();
			
		} catch (Exception e) {
			logger.error("Unable to report error", e);
			throw new ServletException("Unable to report error", e);
		}
	}
	
	private List<String> computeRequestPath(HttpServletRequest req) throws Exception {
		List<String> paths = new Vector<String>();
		
		String path = req.getPathInfo();
		if( null != path ) {
			boolean first = true;
			String[] pathFragments = path.split("/");
			for(String f : pathFragments) {
				if( first ){
					// Skip first which is empty (/getUser/user1 -> "","getUser","user1")
					first = false;
				} else {
					paths.add(f);
				}
			}
		}
		
		return paths;
	}
	
	private String optStringParameter(HttpServletRequest req, String paramName) throws Exception {
		String[] values = req.getParameterValues(paramName);
		if( null == values || values.length < 1 ){
			return null;
		}
		if( values.length > 1 ){
			throw new Exception("'"+paramName+"' parameter must be specified at most once");
		}
		
		String value = values[0];
		
		return value;
	}
	
	private String getStringParameter(HttpServletRequest req, String paramName) throws Exception {
		String value = optStringParameter(req, paramName);
		if( null == value ){
			throw new Exception("'"+paramName+"' parameter must be specified");
		}
		
		return value;
	}
	
	private Integer optIntegerParameter(HttpServletRequest req, String paramName) throws Exception {
		String strValue = optStringParameter(req, paramName);
		if( null == strValue ){
			return null;
		}
		
		try {
			int value = Integer.parseInt(strValue);
			
			return value;
			
		} catch (Exception e) {
			throw new Exception("'"+paramName+"' parameter must be an integer number",e);
		}
	}
	
	private boolean getCheckboxParameter(HttpServletRequest req, String paramName) throws Exception {
		String strValue = optStringParameter(req, paramName);
		if( null == strValue ){
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("unused")
	private int getIntegerParameter(HttpServletRequest req, String paramName) throws Exception {
		Integer value = optIntegerParameter(req, paramName);
		if( null == value ){
			throw new Exception("'"+paramName+"' parameter must be specified");
		}

		return value.intValue();
	}
	
	private Double optDoubleParameter(HttpServletRequest req, String paramName) throws Exception {
		String strValue = optStringParameter(req, paramName);
		if( null == strValue ){
			return null;
		}
		
		if( "".equals(strValue.trim()) ){
			return null;
		}
		
		try {
			double value = Double.parseDouble(strValue);
			
			return value;
			
		} catch (Exception e) {
			throw new Exception("'"+paramName+"' parameter must be a floating point number",e);
		}
	}
	
	private double getDoubleParameter(HttpServletRequest req, String paramName) throws Exception {
		Double value = optDoubleParameter(req, paramName);
		if( null == value ){
			throw new Exception("'"+paramName+"' parameter must be specified");
		}

		return value.doubleValue();
	}
	
	private Date optDateParameter(HttpServletRequest req, String paramName) throws Exception {
		String strValue = optStringParameter(req, paramName);
		if( null == strValue ){
			return null;
		}
		
		try {
			Date value = DateUtils.parseUtcString(strValue);
			return value;
			
		} catch (Exception e) {
			throw new Exception("'"+paramName+"' parameter must be a date",e);
		}
	}
	
	private Date getDateParameter(HttpServletRequest req, String paramName) throws Exception {
		Date value = optDateParameter(req, paramName);
		if( null == value ){
			throw new Exception("'"+paramName+"' parameter must be specified");
		}

		return value;
	}
}
