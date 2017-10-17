package ca.carleton.gcrc.sensorDb.dbapi.memory;

import java.util.List;

import ca.carleton.gcrc.sensorDb.dbapi.Observation;
import ca.carleton.gcrc.sensorDb.dbapi.ObservationReader;

public class ObservationReaderMemory implements ObservationReader {

	private int index = 0;
	private List<Observation> observations = null;
	
	public ObservationReaderMemory(List<Observation> observations){
		this.observations = observations;
	}
	
	@Override
	public Observation read() throws Exception {
		if( index >= observations.size() ){
			return null;
		}
		
		Observation observation = observations.get(index);
		++index;
		return observation;
	}

	@Override
	public void close() throws Exception {
	}

}
