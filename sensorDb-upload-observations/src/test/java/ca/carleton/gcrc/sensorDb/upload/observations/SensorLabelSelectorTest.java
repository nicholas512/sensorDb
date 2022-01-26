package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.List;
import java.util.ArrayList;


import ca.carleton.gcrc.sensorDb.dbapi.Sensor;
import ca.carleton.gcrc.sensorDb.dbapi.memory.DbApiMemory;
import ca.carleton.gcrc.sensorDb.upload.observations.SensorLabelSelector;

import junit.framework.TestCase;


public class SensorLabelSelectorTest extends TestCase {
	DbApiMemory dbApi;

	List<Sensor> uniqueSensors;
	List<Sensor> sensorsWithDuplicates;
	List<String> labels = new ArrayList<String>(4);

	protected void setUp() throws Exception {
		this.dbApi = new DbApiMemory();
		
		// Populate with appropriate devices and sensors
		labels.add("#1:oC");
		labels.add("#2:oC");
		labels.add("#3:oC");
		labels.add("#4:oC");

        this.uniqueSensors= new ArrayList<Sensor>(4);
		this.sensorsWithDuplicates= new ArrayList<Sensor>(5);
		
		{
			Sensor sensor = new Sensor();
			sensor.setLabel("#1:oC");
			sensor = dbApi.createSensor(sensor);
            uniqueSensors.add(sensor);
			sensorsWithDuplicates.add(sensor);
		}
		{
			Sensor sensor = new Sensor();
			sensor.setLabel("#2:oC");
			sensor = dbApi.createSensor(sensor);
            uniqueSensors.add(sensor);
			sensorsWithDuplicates.add(sensor);
		}
        {
			Sensor sensor = new Sensor();
			sensor.setLabel("#3:oC");
			sensor = dbApi.createSensor(sensor);
            uniqueSensors.add(sensor);
			sensorsWithDuplicates.add(sensor);
		}
        {
			Sensor sensor = new Sensor();
			sensor.setLabel("#4:oC");
			sensor = dbApi.createSensor(sensor);
            uniqueSensors.add(sensor);
			sensorsWithDuplicates.add(sensor);
		}
        {
			Sensor sensor = new Sensor();
			sensor.setLabel("#1:oC");
			sensor = dbApi.createSensor(sensor);
			sensorsWithDuplicates.add(sensor);
		}
		
	}

	public void testGetSensors() throws Exception{
		SensorLabelSelector SensorLabelSelector = new SensorLabelSelector(uniqueSensors, labels);
		assertEquals("#1:oC", SensorLabelSelector.getSensorFromLabel("#1:oC").getLabel());

		}

	public void testNoDuplicates() throws Exception {
		try {
			SensorLabelSelector SensorLabelSelector = new SensorLabelSelector(sensorsWithDuplicates, labels);
			fail( "Missing exception : should detect multiple sensors" );
	   } catch( Exception e ) {
			assertEquals( "Multiple sensors with same label (#1:oC)", e.getMessage() ); // Optionally make sure you get the correct message, too
	   }


	}
	
}