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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.dbapi.DeviceSensor;
import ca.carleton.gcrc.sensorDb.dbapi.Sensor;

public class SensorTemporalSelector {
	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private List<DeviceSensor> deviceSensors = null;
	private Map<String,Sensor> sensorsById = null;

	public SensorTemporalSelector(
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
		
	}
	
    public List<Date> getDeviceReconfigurationDates(){
        List <Date> reconfigurationDates = new ArrayList<Date>();
        
        if ( deviceSensors.size() > 0 ){
            
            int i = 0;
            reconfigurationDates.add(deviceSensors.get(0).getTimestamp());  

            for (DeviceSensor ds : deviceSensors){
                if (ds.getTimestamp().getTime() != reconfigurationDates.get(i).getTime()){
                    reconfigurationDates.add(ds.getTimestamp());
                    i++;
                }
            }
        }

        return reconfigurationDates;
    }

	public Date getLastReconfigurationDate(Date timestamp) throws Exception{
		Date lastChange = null;

        // Find last update to device-sensor configuration
        // TODO: Try to find a better way that does not rely on 
        // assuming all sensors change at once
        for(DeviceSensor ds : deviceSensors){
			if( ds.getTimestamp().getTime() < timestamp.getTime() ){
				lastChange = ds.getTimestamp();
			}
		}
		return lastChange;
	}

	public List<Sensor> getSensorsAtTimestamp(Date timestamp) throws Exception{
		Date lastChange = getLastReconfigurationDate(timestamp);
		
		List<Sensor> sensors = new ArrayList<Sensor>();
		if( null != lastChange ){
            
            for (DeviceSensor ds : deviceSensors){
                
                if( ds.getTimestamp().getTime() == lastChange.getTime()){
                    String sensorId = ds.getSensorId();
                    sensors.add(sensorsById.get(sensorId));
                }
            }
		}

		if (sensors.size() == 0){
            sensors = null;
        }

		return sensors;
	}

}


