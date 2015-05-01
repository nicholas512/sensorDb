package ca.carleton.gcrc.sensorDb.upload.observations;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*

Logger: #E5096D 'PT1000TEMP' - USP_EXP2 - (CGI) Expander for GP5W - (V2.60, Mai 27 2014)
No,Time,#1:oC,#HK-Bat:V
1,22.04.2015 18:00:01,22.4735,3.510
2,22.04.2015 19:00:01,21.962

The serial number for the device is: E5096D

Two sensors:
- Label: '1'  Units: 'oC'  (temperature)
- Label: 'HK-Bat' Units: 'V' (voltage)

Each line has a date and a number of variables readings. 
 */

public class ObservationFileReader {

	static private Pattern patternFirstLine = Pattern.compile("^Logger:\\s*#([^']*)'.*$");

	private BufferedReader bufReader;
	private String deviceSerialNumber = null;
	private List<ObservationColumn> columns = new Vector<ObservationColumn>();
	private int timeColumnIndex;
	private boolean reachedEnd = false;
	private List<Observation> cachedObservations = new Vector<Observation>();
	int lineNumber = 0;
	
	public ObservationFileReader(Reader reader) throws Exception {
		bufReader = new BufferedReader(reader);
		
		readPreamble();
	}

	public String getDeviceSerialNumber() {
		return deviceSerialNumber;
	}

	public List<ObservationColumn> getColumns() {
		return columns;
	}
	
	public Observation read() throws Exception {
		// If end is reached, keep returning null
		if( reachedEnd ){
			return null;
		}
		
		// Picked what we have already parsed
		if( cachedObservations.size() > 0 ){
			Observation obs = cachedObservations.remove(0);
			return obs;
		}
		
		// Get new line
		int columnCount = columns.size();
		String line = bufReader.readLine();
		++lineNumber;
		if( null == line ){
			reachedEnd = true;
			return null;
		}

		// Parse line
		String[] fields = line.split(",");
		if( fields.length > columnCount ){
			throw new Exception("More fields than columns on line "+lineNumber);
		}
		
		// Time
		if( timeColumnIndex >= fields.length ){
			throw new Exception("Time not included on line "+lineNumber);
		}
		Date time = null;
		try {
			time = DateUtils.parseUtcString(fields[timeColumnIndex]);
		} catch(Exception e) {
			throw new Exception("Problem parsing date on line "+lineNumber, e);
		}
		
		// Create an observation for each value
		for(int index=0; index<fields.length; ++index){
			String fieldStr = fields[index];
			ObservationColumn column = columns.get(index);
			
			if( column.isValue() ){
				double value = Double.parseDouble(fieldStr.trim());
				
				Observation obs = new Observation(time, column, value);
				cachedObservations.add(obs);
			}
		}
		
		// Re-enter
		return read();
	}

	private void readPreamble() throws Exception {
		// Read top line
		{
			String firstLine = bufReader.readLine();
			++lineNumber;
			Matcher matcherFirstLine = patternFirstLine.matcher(firstLine);
			if( matcherFirstLine.matches() ){
				String serialNumber = matcherFirstLine.group(1).trim();
				this.deviceSerialNumber = serialNumber;
				
			} else {
				throw new Exception("Error while analyzing first line");
			}
		}
		
		// Read columns
		{
			String line = bufReader.readLine();
			++lineNumber;
			String[] columnStrings = line.split(",");
			for(String columnString : columnStrings){
				ObservationColumn column = ObservationColumn.parseColumnString(columnString);
				this.columns.add(column);
			}
		}
		
		// Check that a date is provided in columns
		{
			boolean dateProvided = false;
			int index = 0;
			for(ObservationColumn column : columns){
				if( column.isTime() ){
					if( dateProvided ){
						throw new Exception("Multiple time columns reported");
					}
					
					timeColumnIndex = index;
					dateProvided = true;
				}
				++index;
			}
			
			if( !dateProvided ){
				throw new Exception("No column reporting time");
			}
		}
	}
}
