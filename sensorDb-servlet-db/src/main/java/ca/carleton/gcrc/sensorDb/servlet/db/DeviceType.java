package ca.carleton.gcrc.sensorDb.servlet.db;

import java.util.List;
import java.util.Vector;

public class DeviceType {
	
	static private List<DeviceType> g_deviceTypes = null;
	
	static synchronized public List<DeviceType> getDeviceTypes(){
		if( null == g_deviceTypes ){
			g_deviceTypes = new Vector<DeviceType>();
			
			// LOG-PT1000
			{
				DeviceType type = new DeviceType("LOG-PT1000");
				type.setVoltageCount(1);
				type.setTempCount(1);
				g_deviceTypes.add(type);
			}
			
			// LOG-HC2
			{
				DeviceType type = new DeviceType("LOG-HC2");
				type.setVoltageCount(1);
				type.setTempCount(4);
				g_deviceTypes.add(type);
			}
		}
		
		return g_deviceTypes;
	};
	
	static DeviceType getDeviceTypeFromName(String name) throws Exception {
		for(DeviceType type : getDeviceTypes()){
			if( type.getLabel().equals(name) ){
				return type;
			}
		};
		
		throw new Exception("Can not find device type: "+name);
	}
	
	private String label;
	private boolean includeFirmware;
	private boolean includeNotes;
	private int voltageCount;
	private int tempCount;
	
	public DeviceType(String label){
		this.label = label;
		
		this.includeFirmware = true;
		this.includeNotes = true;
		this.voltageCount = 1;
		this.tempCount = 1;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean includesFirmware() {
		return includeFirmware;
	}

	public void setIncludeFirmware(boolean includeFirmware) {
		this.includeFirmware = includeFirmware;
	}

	public boolean includesNotes() {
		return includeNotes;
	}

	public void setIncludeNotes(boolean includeNotes) {
		this.includeNotes = includeNotes;
	}

	public int getTempCount() {
		return tempCount;
	}

	public void setTempCount(int tempCount) {
		this.tempCount = tempCount;
	}

	public int getVoltageCount() {
		return voltageCount;
	}

	public void setVoltageCount(int voltageCount) {
		this.voltageCount = voltageCount;
	}

}
