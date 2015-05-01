package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObservationColumn {

	static private Pattern patternValue = Pattern.compile("^(#.*)$");
	
	static public ObservationColumn parseColumnString(String str) throws Exception {
		ObservationColumn column = new ObservationColumn();
		
		Matcher matcherValue = patternValue.matcher( str );
		
		if( "no".equalsIgnoreCase(str.trim()) ){
			column.setSerial(true);
			
		} else if( "time".equalsIgnoreCase(str.trim()) ){
			column.setTime(true);
			
		} else if( matcherValue.matches() ){
			column.setValue(true);
			column.setName( matcherValue.group(1).trim() );
		
		} else {
			throw new Exception("Unable to analyze column: "+str);
		}
		
		return column;
	}
	
	private boolean isValue = false;
	private boolean isTime = false;
	private boolean isSerial = false;
	private String name = null;

	public ObservationColumn(){
		
	}
	
	public boolean isValue() {
		return isValue;
	}

	public void setValue(boolean isValue) {
		this.isValue = isValue;
	}

	public boolean isTime() {
		return isTime;
	}

	public void setTime(boolean isTime) {
		this.isTime = isTime;
	}

	public boolean isSerial() {
		return isSerial;
	}

	public void setSerial(boolean isSerial) {
		this.isSerial = isSerial;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
