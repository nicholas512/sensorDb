package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.dbapi.DeviceLocation;
import ca.carleton.gcrc.sensorDb.dbapi.Location;

public class DeviceLocator {
	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private List<DeviceLocation> deviceLocations = null;
	private Map<String,Location> locationsById = null;
	
	public DeviceLocator(
			List<DeviceLocation> deviceLocations,
			List<Location> locations
			){
		this.deviceLocations = new ArrayList<DeviceLocation>(deviceLocations);
		this.locationsById = new HashMap<String,Location>();
		
		// Sort device locations by time
		Collections.sort(this.deviceLocations, new Comparator<DeviceLocation>(){

			@Override
			public int compare(DeviceLocation dl1, DeviceLocation dl2) {
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
		
		for(Location location : locations){
			this.locationsById.put(location.getId(), location);
		}
	}
	
	public Location getLocationFromTimestamp(Date timestamp){
		DeviceLocation deviceLocation = null;
		for(DeviceLocation dl : deviceLocations){
			if( dl.getTimestamp().getTime() < timestamp.getTime() ){
				deviceLocation = dl;
			}
		}
		
		Location location = null;
		if( null != deviceLocation ){
			String locationId = deviceLocation.getLocationId();
			location = locationsById.get(locationId);
		}
		
		return location;
	}
}
