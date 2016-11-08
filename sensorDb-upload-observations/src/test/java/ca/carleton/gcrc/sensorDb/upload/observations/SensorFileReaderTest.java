package ca.carleton.gcrc.sensorDb.upload.observations;

import java.io.StringReader;

import junit.framework.TestCase;

public class SensorFileReaderTest extends TestCase {

	public void testCrCrLf() throws Exception {
		String input = "Logger: #E50AA2 'T2NODE_STR' - USP_EXP2 - (CGI) Expander for GP5W - (V2.7, Jan 12 2016)\r\r\n"
				+"No,Time,#1:oC,#2:oC,#3:oC,#4:oC,#HK-Bat:V\r\r\n"
				+"1,06.05.2015 10:18:45,21.6816,21.1393,21.6108,21.1236,3.662\r\r\n";

		StringReader sr = new StringReader(input);
		SensorFileReader sensorReader = new SensorFileReader(sr);

		String sn = sensorReader.getDeviceSerialNumber();
		if( false == "E50AA2".equals(sn) ){
			fail("Unexpected serial number");
		}

		Sample sample1 = sensorReader.read();
		if( null == sample1 ){
			fail("Expected sample1");
		}
		Sample sample2 = sensorReader.read();
		if( null == sample2 ){
			fail("Expected sample2");
		}
		Sample sample3 = sensorReader.read();
		if( null == sample3 ){
			fail("Expected sample3");
		}
		Sample sample4 = sensorReader.read();
		if( null == sample4 ){
			fail("Expected sample4");
		}
		Sample sample5 = sensorReader.read();
		if( null == sample5 ){
			fail("Expected sample5");
		}

		Sample sample6 = sensorReader.read();
		if( null != sample6 ){
			fail("sample6 should be null");
		}
	}
	
}
