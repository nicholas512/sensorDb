package ca.carleton.gcrc.sensorDb.upload.observations;

import java.io.File;
import java.util.Date;

import ca.carleton.gcrc.sensorDb.dbapi.Device;
import ca.carleton.gcrc.sensorDb.dbapi.DeviceLocation;
import ca.carleton.gcrc.sensorDb.dbapi.ImportRecord;
import ca.carleton.gcrc.sensorDb.dbapi.Location;
import ca.carleton.gcrc.sensorDb.dbapi.Observation;
import ca.carleton.gcrc.sensorDb.dbapi.ObservationReader;
import ca.carleton.gcrc.sensorDb.dbapi.Sensor;
import ca.carleton.gcrc.sensorDb.dbapi.memory.DbApiMemory;
import junit.framework.TestCase;

public class SensorFileImporterTest extends TestCase {

	public void testDeltaTimeLine() throws Exception {
		File testFile = TestSupport.findResourceFile("sensor.delta.txt");
		
		ConversionRequest conversionRequest = new ConversionRequest();
		conversionRequest.setFileToConvert(testFile);
		conversionRequest.setOriginalFileName(testFile.getName());
		conversionRequest.setImporterName("Me");
		conversionRequest.setInitialOffset(0);
		conversionRequest.setFinalOffset(100);
		
		DbApiMemory dbApi = new DbApiMemory();
		
		// Populate with appropriate devices and sensors
		Device device = null;
		Location location = null;
		{
			device = new Device();
			device.setSerialNumber("E509EC");
			device = dbApi.createDevice(device);
		}
		{
			Sensor sensor = new Sensor();
			sensor.setLabel("#1:oC");
			dbApi.createSensor(sensor);
		}
		{
			Sensor sensor = new Sensor();
			sensor.setLabel("#HK-Bat:V");
			dbApi.createSensor(sensor);
		}
		{
			location = new Location();
			location.setGeometry("POINT(0 0)");
			location.setRecordingObservations(true);
			location = dbApi.createLocation(location);
		}
		{
			DeviceLocation deviceLocation = new DeviceLocation();
			deviceLocation.setDeviceId( device.getId() );
			deviceLocation.setLocationId( location.getId() );
			deviceLocation.setTimestamp( DateUtils.parseUtcString("01.01.2016 01:00:00") );
			dbApi.createDeviceLocation(deviceLocation);
		}
		
		SensorFileImporter importer = new SensorFileImporter(dbApi);
		ImportRecord importRecord = importer.importFile(conversionRequest);
		
		// Now check that the delta time in the file took precedence over
		// the set offset. To do that, loop through all the observations
		// and capture the maximum time. It should be close to the last obervation
		// time plus the delta time
		ObservationReader obsReader = dbApi.getObservationsFromImportId( importRecord.getId() );
		Date maxTime = null;
		Observation observation = obsReader.read();
		while( null != observation ){
			if( null == maxTime ){
				maxTime = observation.getCorrectedTime();
			} else if( observation.getCorrectedTime().getTime() > maxTime.getTime() ){
				maxTime = observation.getCorrectedTime();
			}
			
			observation = obsReader.read();
		}
		obsReader.close();
		Date compareTime = DateUtils.parseUtcString("28.01.2016 16:45:00");
		if( compareTime.getTime() > maxTime.getTime() ){
			fail("Delta time specified in file was not respected");
		}
	}
	
}
