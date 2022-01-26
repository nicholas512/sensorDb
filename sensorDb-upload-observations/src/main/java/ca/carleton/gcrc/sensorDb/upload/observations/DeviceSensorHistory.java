package ca.carleton.gcrc.sensorDb.upload.observations;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.dbapi.DeviceSensor;
import ca.carleton.gcrc.sensorDb.upload.observations.SensorLabelSelector;
import ca.carleton.gcrc.sensorDb.upload.observations.SensorTemporalSelector;
import ca.carleton.gcrc.sensorDb.dbapi.Sensor;

public class DeviceSensorHistory {
	//final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private List<DeviceSensor> deviceSensors = null;
	private Map<String,Sensor> sensorsById = null;
	private Map<Date, SensorLabelSelector> SensorLabelSelectorsByDate = null;


	public DeviceSensorHistory(
			List<DeviceSensor> deviceSensors,
			List<Sensor> sensors
			) throws Exception{
		this.deviceSensors = new ArrayList<DeviceSensor>(deviceSensors);
		this.sensorsById = new HashMap<String,Sensor>();
		
		// Sort device sensors by time
		Collections.sort(this.deviceSensors, new Comparator<DeviceSensor>(){

			@Override
			public int compare(DeviceSensor dl1, DeviceSensor dl2) {
				Date date1 = dl1.getTimestamp();
				Date date2 = dl2.getTimestamp();

				long ts1 = 0;
				long ts2 = 0;
				
				if( null != date1 ){
					ts1 = date1.getTime();
				}
				if( null != date2 ){
					ts2 = date2.getTime();
				}
				
				if( ts1 < ts2 ) return -1;
				if( ts1 > ts2 ) return 1;
				return 0;
			}
			
		});
		
		for(Sensor sensor : sensors){
			this.sensorsById.put(sensor.getId(), sensor);
		}
		
		// Make history of sensors at times
		for (Date date : getDeviceReconfigurationDates()){
			List<String> labels = new ArrayList<String>();
			Set<String> uniqueLabels = new HashSet<String>();

			for (Sensor sensor : sensors){
				uniqueLabels.add(sensor.getLabel());
			}
			
			labels.addAll(uniqueLabels);

			SensorLabelSelector selector = new SensorLabelSelector(sensors, labels);
			SensorLabelSelectorsByDate.put(date, selector);
		}
	}
	
	public Sensor getSensorAtTimestamp(String label, Date timestamp) throws Exception{
		Date lastChange = getLastReconfigurationDate(timestamp);
		SensorLabelSelector SensorLabelSelector = SensorLabelSelectorsByDate.get(lastChange);
		Sensor sensor = SensorLabelSelector.getSensorFromLabel(label);

		return sensor;
	}
}


