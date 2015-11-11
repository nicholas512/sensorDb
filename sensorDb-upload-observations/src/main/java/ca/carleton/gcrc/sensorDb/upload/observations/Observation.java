package ca.carleton.gcrc.sensorDb.upload.observations;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;

public class Observation {

	private Date time;
	private ObservationColumn column;
	private double value;
	private String line;
	private String deviceSerialNumber;
	private String importKey;

	public Observation(Date time, ObservationColumn column, double value){
		this.time = time;
		this.column = column;
		this.value = value;
	}
	
	public Date getTime() {
		return time;
	}

	public ObservationColumn getColumn() {
		return column;
	}

	public double getValue() {
		return value;
	}
	
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}

	public String getDeviceSerialNumber() {
		return deviceSerialNumber;
	}
	public void setDeviceSerialNumber(String deviceSerialNumber) {
		this.deviceSerialNumber = deviceSerialNumber;
	}
	
	public String computeImportKey() throws Exception {
		synchronized(this){
			if( null == importKey ){
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				pw.print(deviceSerialNumber);
				pw.print(",");
				pw.print(column.getName());
				pw.print(",");
				pw.print(line);
				pw.flush();
				
				String effectiveLine = sw.toString();
				
				// System.out.println("import key: "+effectiveLine);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(baos,"UTF-8");
				osw.write(effectiveLine);
				osw.flush();
				
				byte[] effectiveBytes = baos.toByteArray();
				
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] digest = md.digest(effectiveBytes);
				
				importKey = Base64.encodeBase64String(digest);
			}
		}
		
		return importKey;
	}
}
