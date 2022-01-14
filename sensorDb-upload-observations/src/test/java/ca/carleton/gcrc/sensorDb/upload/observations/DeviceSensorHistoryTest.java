package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.Date;

import java.util.List;
import java.util.ArrayList;

import ca.carleton.gcrc.sensorDb.dbapi.Device;
import ca.carleton.gcrc.sensorDb.dbapi.DeviceSensor;
import ca.carleton.gcrc.sensorDb.dbapi.Sensor;
import ca.carleton.gcrc.sensorDb.dbapi.memory.DbApiMemory;

import junit.framework.TestCase;


public class DeviceSensorHistoryTest extends TestCase {
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

        this.initialDeviceDeployment = DateUtils.parseUtcString("28.01.2016 16:45:00");
        this.swapSensors2017 = DateUtils.parseUtcString("28.01.2017 16:45:00");
        
        this.targetDate = DateUtils.parseUtcString("28.05.2016 16:45:00");
        this.aLaterDate = DateUtils.parseUtcString("28.05.2019 16:45:00");
        
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

	public void testGetDeviceReconfigurationDates() throws Exception{
		DeviceSensorHistory deviceSensorHistory = new DeviceSensorHistory(deviceSensors, sensors);
		List<Date> reconfigurationDates = deviceSensorHistory.getDeviceReconfigurationDates();

		assertEquals("Wrong length, expected" + 2 + " actual: "+ reconfigurationDates.size(), 2, reconfigurationDates.size());

		for (int i = 0; i < reconfigurationDates.size() - 1; i++){
			if (reconfigurationDates.get(i).getTime() > reconfigurationDates.get(i+1).getTime()){
				fail("Dates in wrong order (expect sorted earliest to latest)");
			}
		}
	}

	public void testGetSensorsAtTimestamp() throws Exception {
		
		DeviceSensorHistory deviceSensorHistory = new DeviceSensorHistory(deviceSensors, sensors);
		List<Sensor> sensorsAtTarget = deviceSensorHistory.getSensorsAtTimestamp(targetDate);
		List<Sensor> sensorsLater = deviceSensorHistory.getSensorsAtTimestamp(aLaterDate);
		// Now check that the returned sensors are appropriate

		if( sensorsAtTarget.size() != 2 ){
			fail("Wrong number of sensors returned");
		}
        if( sensorsLater.size() != 3 ){
			fail("Wrong number of sensors returned");
		}
        if( sensorsAtTarget.get(0).getId() != sensors.get(0).getId() &&
            sensorsAtTarget.get(0).getId() != sensors.get(1).getId()){
            fail("Missing sensor from result set");
        }
        if( sensorsAtTarget.get(0).getId() == sensors.get(2).getId() ||
            sensorsAtTarget.get(0).getId() == sensors.get(3).getId() ||
            sensorsAtTarget.get(0).getId() == sensors.get(4).getId()){
            fail("Wrong sensor in result set");
        }


	}
	
}