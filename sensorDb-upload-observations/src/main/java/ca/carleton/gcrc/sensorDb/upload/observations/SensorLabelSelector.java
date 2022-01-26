package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.carleton.gcrc.sensorDb.dbapi.Sensor;

public class SensorLabelSelector {
    private Map<String,Sensor> sensorsByLabel;

    public SensorLabelSelector(
        List<Sensor> sensors, 
        List<String> labels) throws Exception{
    
        this.sensorsByLabel = new HashMap<String,Sensor>();
			for(Sensor sensor : sensors){
				
				if( sensorsByLabel.containsKey(sensor.getLabel()) ){
					throw new Exception("Multiple sensors with same label ("+sensor.getLabel()
						+")"
					);
				}
				
				sensorsByLabel.put(sensor.getLabel(), sensor);
			}

    }

    public Sensor getSensorFromLabel(String label) throws Exception{
        Sensor sensor = null;
		
        for (String lab : sensorsByLabel.keySet()){
            if (label == lab){
                sensor = sensorsByLabel.get(lab);
            }
        }
		
        if (null == sensor){
            throw new Exception("Sensor with label '" + label + "' not found!");
        }
		return sensor;
    }
}

