package ca.carleton.gcrc.sensorDb.upload.observations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

public class ObservationFileImportReportMemory implements ObservationFileImportReport {

	private int insertedObservations = 0;
	private int skippedObservations = 0;
	private int collidedObservations = 0;
	private List<String> collisionStrings = new Vector<String>();
	private DateFormat dateFormatter;
	
	public ObservationFileImportReportMemory() {
		dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	    dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));	
	}
	
	@Override
	public void insertedObservation(Date time, String sensor_id, double value) {
		++insertedObservations;
	}

	@Override
	public void skippedObservation(Date time, String sensor_id, double value) {
		++skippedObservations;
	}

	@Override
	public void collisionOnObservation(
			Date time, 
			String sensor_id,
			double value, 
			double db_value
			) {
		++collidedObservations;
		
		String dateStr = dateFormatter.format(time);
		
		collisionStrings.add(""
			+ dateStr
			+ " "
			+ sensor_id
			+ " value:"
			+ value
			+ " db:"
			+ db_value
			);
	}

	@Override
	public String produceReport() throws Exception {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		pw.println("Observation Import Report");
		for(String line : collisionStrings){
			pw.println(line);
		}
		
		int total = insertedObservations + collidedObservations + skippedObservations;
		pw.println("Total observations: "+total);
		pw.println("Inserted to database: "+insertedObservations);
		if( skippedObservations > 0 ){
			pw.println("Skipped: "+skippedObservations);
		}
		if( collidedObservations > 0 ){
			pw.println("Collisions: "+collidedObservations);
		}
		
		String report = sw.toString();
		
		pw.close();
		sw.close();
		
		return report;
	}

	public int getInsertedObservations() {
		return insertedObservations;
	}

	public int getSkippedObservations() {
		return skippedObservations;
	}

	public int getCollidedObservations() {
		return collidedObservations;
	}
}
