package ca.carleton.gcrc.sensorDb.upload.observations;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.util.Objects;
// import javafx.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.dbapi.DeviceSensor;
import ca.carleton.gcrc.sensorDb.dbapi.Sensor;


public class DeviceSensorHistory {
	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private SensorTemporalSelector sensorTemporalSelector = null;
	private List<Pair<Date,SensorLabelSelector>> sensorConfigurations = null;


	public DeviceSensorHistory(
			List<DeviceSensor> deviceSensors,
			List<Sensor> sensors
			) throws Exception{
		
		deviceSensors = new ArrayList<DeviceSensor>(deviceSensors);
		
		// Make temporal selector for all elements
		sensorTemporalSelector = new SensorTemporalSelector(deviceSensors, sensors);
		
		// Make list of configurations
		sensorConfigurations = new ArrayList<Pair<Date,SensorLabelSelector>>();
		for (Date date : sensorTemporalSelector.getDeviceReconfigurationDates()){

			List<Sensor> sensorsAtDate = sensorTemporalSelector.getSensorsAtTimestamp(date);
			
			List<String> labels = new ArrayList<String>(sensorsAtDate.size());

			for ( Sensor sensor : sensorsAtDate ){
				labels.add(sensor.getLabel());
			}

			SensorLabelSelector sensorSelector = new SensorLabelSelector(sensorsAtDate, labels);

			Pair<Date,SensorLabelSelector> sensorConfiguration = new Pair<Date,SensorLabelSelector>(date, sensorSelector);
			System.out.println(sensorConfiguration.toString());
			sensorConfigurations.add(sensorConfiguration);

		}

	}
	
	public Sensor getSensorAtTimestamp(String label, Date timestamp) throws Exception{
		SensorLabelSelector sensorLabelSelector = null;

		// Iterate through to find date
		for (Pair<Date,SensorLabelSelector> configuration : sensorConfigurations ){
			if (configuration.first.getTime() <= timestamp.getTime()){
				sensorLabelSelector = configuration.second;
			}
		}
		
		Sensor sensor = sensorLabelSelector.getSensorFromLabel(label);

		return sensor;
	}
}

/**
 * Taken from https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/util/Pair.java
 */
/**
 * Container to ease passing around a tuple of two objects. This object provides a sensible
 * implementation of equals(), returning true if equals() is true on each of the contained
 * objects.
 */
final class Pair<F, S> {
    public final F first;
    public final S second;
    /**
     * Constructor for a Pair.
     *
     * @param first the first object in the Pair
     * @param second the second object in the pair
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }
    /**
     * Checks the two objects for equality by delegating to their respective
     * {@link Object#equals(Object)} methods.
     *
     * @param o the {@link Pair} to which this one is to be checked for equality
     * @return true if the underlying objects of the Pair are both considered
     *         equal
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> p = (Pair<?, ?>) o;
        return Objects.equals(p.first, first) && Objects.equals(p.second, second);
    }
    /**
     * Compute a hash code using the hash codes of the underlying objects
     *
     * @return a hashcode of the Pair
     */
    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }
    @Override
    public String toString() {
        return "Pair{" + String.valueOf(first) + " " + String.valueOf(second) + "}";
    }
    /**
     * Convenience method for creating an appropriately typed pair.
     * @param a the first object in the Pair
     * @param b the second object in the pair
     * @return a Pair that is templatized with the types of a and b
     */
    public static <A, B> Pair <A, B> create(A a, B b) {
        return new Pair<A, B>(a, b);
    }
}