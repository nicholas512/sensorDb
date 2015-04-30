package ca.carleton.gcrc.sensorDb.upload.observations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.List;
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

public class ObservationFileParser {

	static private Pattern patternFirstLine = Pattern.compile("^Logger:\\s*#([^']*)'.*$");
	
	public ObservationFile parse(File inFile) throws Exception {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		try {
			fis = new FileInputStream(inFile);
			isr = new InputStreamReader(fis,"UTF-8");
			
			return parse(isr);
			
		} catch(Exception e) {
			String fileName = null;
			if( null != inFile ){
				fileName = inFile.getAbsolutePath();
			}
			throw new Exception("Error while parsing file: "+fileName,e);
		} finally {
			if( null != isr ){
				try {
					isr.close();
				} catch(Exception e) {
					// ignore
				}
			}
			if( null != fis ){
				try {
					fis.close();
				} catch(Exception e) {
					// ignore
				}
			}
		}
	}

	public ObservationFile parse(Reader reader) throws Exception {
		BufferedReader bufReader = null;
		try {
			bufReader = new BufferedReader(reader);
			
			return parseBuffered(bufReader);
			
		} catch (Exception e) {
			throw new Exception("Error while parsing observations file",e);
		
		} finally {
			if( null != bufReader ){
				try {
					bufReader.close();
				} catch(Exception e) {
					// ignore
				}
			}
		}
	}

	public ObservationFile parseBuffered(BufferedReader bufReader) throws Exception {
		ObservationFile observationFile = new ObservationFile();
		
		// Read top line
		{
			String firstLine = bufReader.readLine();
			Matcher matcherFirstLine = patternFirstLine.matcher(firstLine);
			if( matcherFirstLine.matches() ){
				String serialNumber = matcherFirstLine.group(1).trim();
				observationFile.setSerialNumber(serialNumber);
				
			} else {
				throw new Exception("Error while analyzing first line");
			}
		}
		
		// Read columns
		{
			String line = bufReader.readLine();
			String[] columnStrings = line.split(",");
			for(String columnString : columnStrings){
				ObservationColumn column = ObservationColumn.parseColumnString(columnString);
				observationFile.addColumn(column);
			}
		}
		
		// Check that a date is provided in columns
		int timeColumnIndex = 0;
		{
			boolean dateProvided = false;
			int index = 0;
			for(ObservationColumn column : observationFile.getColumns()){
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
		
		// Read lines until end of file
		{
			List<ObservationColumn> columns = observationFile.getColumns();
			int columnCount = columns.size();
			int lineNumber = 1;
			String line = bufReader.readLine();
			while( null != line ){
				String[] fields = line.split(",");
				if( fields.length > columnCount ){
					throw new Exception("More fields than columns on record "+lineNumber);
				}
				
				// Time
				if( timeColumnIndex >= fields.length ){
					throw new Exception("Time not included on record "+lineNumber);
				}
				Date time = null;
				try {
					time = DateUtils.parseUtcString(fields[timeColumnIndex]);
				} catch(Exception e) {
					throw new Exception("Problem parsing date on record "+lineNumber, e);
				}
				
				// Create an observation for each value
				for(int index=0; index<fields.length; ++index){
					String fieldStr = fields[index];
					ObservationColumn column = columns.get(index);
					
					if( column.isValue() ){
						double value = Double.parseDouble(fieldStr.trim());
						
						Observation obs = new Observation(time, column, value);
						observationFile.addObservation(obs);
					}
				}
				
				++lineNumber;
				line = bufReader.readLine();
			}
		}
		
		return observationFile;
	}
}
