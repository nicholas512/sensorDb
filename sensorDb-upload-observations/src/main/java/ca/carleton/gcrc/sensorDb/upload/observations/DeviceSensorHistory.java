package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.dbapi.DeviceSensor;
import ca.carleton.gcrc.sensorDb.dbapi.Sensor;


public class DeviceSensorHistory {
	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private SensorTemporalSelector sensorConfigurations = null;
	private Map<String,Sensor> sensorsById = null;
	private Map<Date, SensorLabelSelector> SensorLabelSelectorsByDate = null;


	public DeviceSensorHistory(
			List<DeviceSensor> deviceSensors,
			List<Sensor> sensors
			) throws Exception{
		
		deviceSensors = new ArrayList<DeviceSensor>(deviceSensors);
		sensorsById = new HashMap<String,Sensor>();
		
		// Make temporal selector for all elements
		sensorConfigurations = new SensorTemporalSelector(deviceSensors, sensors);
		
		// Make map
		for (Date date : sensorConfigurations.getDeviceReconfigurationDates()){

			List<Sensor> sensorsAtDate = sensorConfigurations.getSensorsAtTimestamp(date);
			
			List<String> labels = new ArrayList<String>(sensorsAtDate.size());

			for ( Sensor sensor : sensorsAtDate ){
				labels.add(sensor.getLabel());
			}

			SensorLabelSelector sensorSelector = new SensorLabelSelector(sensorsAtDate, labels);

			SensorLabelSelectorsByDate.put(date, sensorSelector);
		}

	}
	
	public Sensor getSensorAtTimestamp(String label, Date timestamp) throws Exception{
		Date lastChange = sensorConfigurations.getLastReconfigurationDate(timestamp);
		SensorLabelSelector SensorLabelSelector = SensorLabelSelectorsByDate.get(lastChange);
		Sensor sensor = SensorLabelSelector.getSensorFromLabel(label);

		return sensor;
	}
}


