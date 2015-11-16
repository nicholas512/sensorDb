package ca.carleton.gcrc.sensorDb.upload.observations;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;

public class ObservationFileImportReportMemory implements ObservationFileImportReport {

	private String importId;
	private int insertedObservations = 0;
	private int skippedObservations = 0;
	private DateFormat dateFormatter;
	private Map<String,Integer> observedTextFields = new HashMap<String,Integer>();
	
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
		
		if( null != observation.getText() ){
			// Accumulate the text fields and count them
			String text = observation.getText();
			if( false == observedTextFields.containsKey(text) ){
				observedTextFields.put(text,0);
			}
			
			int count = observedTextFields.get(text);
			++count;
			observedTextFields.put(text,count);
		}
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
		
		JSONObject jsonProblems = new JSONObject();
		int problemCount = 0;
		for(String errorText : observedTextFields.keySet()){
			int count = observedTextFields.get(errorText);
			problemCount += count;
			jsonProblems.put(errorText, count);
		}
		if( problemCount > 0 ){
			jsonReport.put("problemCount", problemCount);
			jsonReport.put("problems", jsonProblems);
		}
		
		return jsonReport.toString();
	}

	public int getInsertedObservations() {
		return insertedObservations;
	}

	public int getSkippedObservations() {
		return skippedObservations;
	}
}
