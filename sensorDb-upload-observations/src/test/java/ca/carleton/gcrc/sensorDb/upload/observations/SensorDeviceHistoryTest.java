package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.Date;

import java.util.List;
import java.util.ArrayList;

import ca.carleton.gcrc.sensorDb.dbapi.Device;
import ca.carleton.gcrc.sensorDb.dbapi.DeviceSensor;
import ca.carleton.gcrc.sensorDb.dbapi.Sensor;
import ca.carleton.gcrc.sensorDb.dbapi.memory.DbApiMemory;
import ca.carleton.gcrc.sensorDb.upload.observations.DeviceSensorHistory;

import junit.framework.TestCase;


public class SensorDeviceHistoryTest extends TestCase {

    DbApiMemory dbApi;

	List<Device> devices;
	List<DeviceSensor> deviceSensors;
	List<Sensor> sensors;

	Date initialDeviceDeployment;
	Date swapSensors2017;
	
	Date targetDate;
	Date aLaterDate;
	
	protected void setUp() throws Exception {
		this.dbApi = new DbApiMemory();
		
		// Populate with appropriate devices and sensors
		this.devices = new ArrayList<Device>(1);
		this.deviceSensors = new ArrayList<DeviceSensor>();
        this.sensors= new ArrayList<Sensor>(4);

        this.initialDeviceDeployment = DateUtils.parseUtcString("01.02.2014 16:45:00");
        this.swapSensors2017 = DateUtils.parseUtcString("28.01.2017 16:45:00");
        
        this.targetDate = DateUtils.parseUtcString("20.04.2016 16:45:00");
        this.aLaterDate = DateUtils.parseUtcString("11.09.2019 16:45:00");
        
		{
			Device device = new Device();
			device.setSerialNumber("E509EC");
			device = dbApi.createDevice(device);
            devices.add(device);
		}
		{
			Sensor sensor = new Sensor();
			sensor.setLabel("#1:oC");
			sensor = dbApi.createSensor(sensor);
            sensors.add(sensor);
		}
		{
			Sensor sensor = new Sensor();
			sensor.setLabel("#2:oC");
			sensor = dbApi.createSensor(sensor);
            sensors.add(sensor);
		}
        {
			Sensor sensor = new Sensor();
			sensor.setLabel("#1:oC");
			sensor = dbApi.createSensor(sensor);
            sensors.add(sensor);
		}
        {
			Sensor sensor = new Sensor();
			sensor.setLabel("#2:oC");
			sensor = dbApi.createSensor(sensor);
            sensors.add(sensor);
		}
        {
			Sensor sensor = new Sensor();
			sensor.setLabel("#3:oC");
			sensor = dbApi.createSensor(sensor);
            sensors.add(sensor);
		}
		{
			DeviceSensor deviceSensor = new DeviceSensor();
			deviceSensor.setDeviceId(devices.get(0).getId());
            deviceSensor.setSensorId(sensors.get(0).getId());
            deviceSensor.setTimestamp(initialDeviceDeployment);
            deviceSensors.add(deviceSensor);
		}
        {
			DeviceSensor deviceSensor = new DeviceSensor();
			deviceSensor.setDeviceId(devices.get(0).getId());
            deviceSensor.setSensorId(sensors.get(1).getId());
            deviceSensor.setTimestamp(initialDeviceDeployment);
            deviceSensors.add(deviceSensor);
		}
        {
			DeviceSensor deviceSensor = new DeviceSensor();
			deviceSensor.setDeviceId(devices.get(0).getId());
            deviceSensor.setSensorId(sensors.get(2).getId());
            deviceSensor.setTimestamp(swapSensors2017);
            deviceSensors.add(deviceSensor);
		}
        {
			DeviceSensor deviceSensor = new DeviceSensor();
			deviceSensor.setDeviceId(devices.get(0).getId());
            deviceSensor.setSensorId(sensors.get(3).getId());
            deviceSensor.setTimestamp(swapSensors2017);
            deviceSensors.add(deviceSensor);
		}
        {
			DeviceSensor deviceSensor = new DeviceSensor();
			deviceSensor.setDeviceId(devices.get(0).getId());
            deviceSensor.setSensorId(sensors.get(4).getId());
            deviceSensor.setTimestamp(swapSensors2017);
            deviceSensors.add(deviceSensor);
		}
	}

    public void testGetSensorAtTimestamp() throws Exception {
		
		DeviceSensorHistory deviceSensorHistory = new DeviceSensorHistory(deviceSensors, sensors);
		//
		Sensor sensor1AtTarget = deviceSensorHistory.getSensorAtTimestamp("#1:oC", targetDate);
		Sensor sensor2AtTarget = deviceSensorHistory.getSensorAtTimestamp("#2:oC", targetDate);
		Sensor sensor1Later = deviceSensorHistory.getSensorAtTimestamp("#1:oC", aLaterDate);
		Sensor sensor2Later = deviceSensorHistory.getSensorAtTimestamp("#2:oC", aLaterDate);
		
		// Now check that the returned sensors are appropriate
		assertNotSame(sensor1AtTarget, sensor1Later);
		assertNotSame(sensor2AtTarget, sensor2Later);

		// Asking for a sensor that doesn't exist
		try {
			Sensor sensorWithWrongLabel = deviceSensorHistory.getSensorAtTimestamp("#65:oC", targetDate);
			fail("Expect exception when asking for sensor with wrong label");
		} catch(Exception e){
			assertEquals(1, 1);
			//assertEquals("Timestamp", e.getMessage().substring(0, 9) );
		}

	}
}
