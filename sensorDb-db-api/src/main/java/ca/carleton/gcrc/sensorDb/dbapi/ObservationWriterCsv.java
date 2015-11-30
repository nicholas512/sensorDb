package ca.carleton.gcrc.sensorDb.dbapi;

import java.io.Writer;
import java.util.Date;

public class ObservationWriterCsv {

	private Writer writer;
	
	public ObservationWriterCsv(Writer writer){
		this.writer = writer;
	}

	public void writeHeader() throws Exception {
		writer.write("\"id\",\"device_id\",\"sensor_id\","
		+ "\"import_id\",\"import_key\",\"observation_type\",\"unit_of_measure\","
		+ "\"accuracy\",\"precision\",\"numeric_value\",\"text_value\","
		+ "\"logged_time\",\"corrected_utc_time\",\"location\","
		+ "\"height_min_metres\",\"height_max_metres\",\"elevation_in_metres\"\n");
	}
	
	public void write(Observation observation) throws Exception {
		write( observation.getId() );
		writer.write( "," );
		write( observation.getDeviceId() );
		writer.write( "," );
		write( observation.getSensorId() );
		writer.write( "," );
		write( observation.getImportId() );
		writer.write( "," );
		write( observation.getImportKey() );
		writer.write( "," );
		write( observation.getObservationType() );
		writer.write( "," );
		write( observation.getUnitOfMeasure() );
		writer.write( "," );
		write( observation.getAccuracy() );
		writer.write( "," );
		write( observation.getPrecision() );
		writer.write( "," );
		write( observation.getNumericValue() );
		writer.write( "," );
		write( observation.getTextValue() );
		writer.write( "," );
		write( observation.getLoggedTime() );
		writer.write( "," );
		write( observation.getCorrectedTime() );
		writer.write( "," );
		write( observation.getLocation() );
		writer.write( "," );
		write( observation.getMinHeight() );
		writer.write( "," );
		write( observation.getMaxHeight() );
		writer.write( "," );
		write( observation.getElevation() );
		writer.write( "\n" );
	}
	
	public void flush() throws Exception {
		writer.flush();
	}

	private void write(String str) throws Exception {
		if( null != str ){
			writer.write("\"");
			
			for(int loop=0; loop<str.length(); ++loop){
				char c = str.charAt(loop);
				switch(c){
				case '"':
					writer.write("\"\"");
					break;
				default:
					writer.write(c);
				}
			}

			writer.write("\"");
		}
	}

	private void write(Double number) throws Exception {
		if( null != number ){
			writer.write( number.toString() );
		}
	}

	private void write(Date date) throws Exception {
		if( null != date ){
			writer.write(""+date.getTime());
		}
	}
}
