package ca.carleton.gcrc.sensorDb.jdbc;

import java.sql.ResultSet;

import ca.carleton.gcrc.sensorDb.dbapi.Observation;
import ca.carleton.gcrc.sensorDb.dbapi.ObservationReader;

public class ObservationReaderJdbc implements ObservationReader {

	static public String getFields() {
		return "id,device_id,sensor_id,import_id,import_key,observation_type,"
			+"unit_of_measure,accuracy,precision,numeric_value,text_value,"
			+"logged_time,corrected_utc_time,ST_AsEWKT(location),height_min_metres,"
			+"height_max_metres,elevation_in_metres";		
	}

	private ResultSet resultSet;
	
	public ObservationReaderJdbc(ResultSet resultSet){
		this.resultSet = resultSet;
	}
	
	@Override
	public Observation read() throws Exception {
		Observation observation = null;

		if( resultSet.next() ){
			observation = new Observation();
			observation.setId( resultSet.getString(1) ); // id
			observation.setDeviceId( resultSet.getString(2) ); // device_id
			observation.setSensorId( resultSet.getString(3) ); // sensor_id
			observation.setImportId( resultSet.getString(4) ); // import_id
			observation.setImportKey( resultSet.getString(5) ); // import_key
			observation.setObservationType( resultSet.getString(6) ); // observation_type
			observation.setUnitOfMeasure( resultSet.getString(7) ); // unit_of_measure
			observation.setAccuracy( resultSet.getDouble(8) ); // accuracy
			observation.setPrecision( resultSet.getDouble(9) ); // precision
			observation.setNumericValue( resultSet.getDouble(10) ); // numeric_value
			observation.setTextValue( resultSet.getString(11) ); // text_value
			observation.setLoggedTime( resultSet.getTimestamp(12) ); // logged_time
			observation.setCorrectedTime( resultSet.getTimestamp(13) ); // corrected_utc_time
			observation.setLocation( resultSet.getString(14) ); // location
			observation.setMinHeight( resultSet.getDouble(15) ); // height_min_metres
			observation.setMaxHeight( resultSet.getDouble(16) ); // height_max_metres
			observation.setElevation( resultSet.getDouble(17) ); // elevation_in_metres
		}
		
		return observation;
	}

	@Override
	public void close() throws Exception {
		if( null != resultSet ){
			resultSet.close();
			resultSet = null;
		}
	}

}
