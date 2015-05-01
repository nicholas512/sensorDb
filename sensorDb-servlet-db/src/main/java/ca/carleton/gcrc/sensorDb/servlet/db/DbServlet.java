package ca.carleton.gcrc.sensorDb.servlet.db;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
	
	public DbServlet(DbConnection dbConn){
//		this.dbConn = dbConn;
		
		this.actions = new DbServletActions(dbConn);
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
				double lat = getDoubleParameter(req, "lat");
				double lng = getDoubleParameter(req, "lng");
				Integer elevation = optIntegerParameter(req, "elevation");

				JSONObject result = actions.createLocation(
						name
						,lat
						,lng
						,elevation
						);
				sendJsonResponse(resp, result);

			} else if( path.size() == 1 && path.get(0).equals("createDevice") ) {

					String serialNumber = getStringParameter(req, "serial");
					String deviceType = getStringParameter(req, "device_type");
					String notes = getStringParameter(req, "notes");

					JSONObject result = actions.createDevice(
							serialNumber
							,deviceType
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
