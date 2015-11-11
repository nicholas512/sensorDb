package ca.carleton.gcrc.sensorDb.upload.observations;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.json.JSONObject;

public class ObservationFileImportReportMemory implements ObservationFileImportReport {

	private String importId;
	private int insertedObservations = 0;
	private int skippedObservations = 0;
	private DateFormat dateFormatter;
	
	public ObservationFileImportReportMemory() {
		dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	    dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));	
	}
	
	public String getImportId() {
		return importId;
	}
	
	@Override
	public void setImportId(String importId){
		this.importId = importId;
	}

	@Override
	public void insertedObservation(Observation observation) {
		++insertedObservations;
	}

	@Override
	public void skippedObservation(Observation observation) {
		++skippedObservations;
	}

	@Override
	public String produceReport() throws Exception {
		JSONObject jsonReport = new JSONObject();
		
		jsonReport.put("type", "import");
		jsonReport.put("importId", importId);
		jsonReport.put("insertedCount", insertedObservations);
		jsonReport.put("skippedCount", skippedObservations);
		
		return jsonReport.toString();
	}

	public int getInsertedObservations() {
		return insertedObservations;
	}

	public int getSkippedObservations() {
		return skippedObservations;
	}
}
