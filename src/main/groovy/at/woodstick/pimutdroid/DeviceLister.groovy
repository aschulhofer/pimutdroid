package at.woodstick.pimutdroid;

import java.util.regex.Matcher

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

public class DeviceLister {
	private final static Logger LOGGER = Logging.getLogger(DeviceLister);
	
	private File adbExecuteable;
	private Map<String, Device> deviceMap = new HashMap<>();
	private List<?> cmdList;
	
	public DeviceLister(File adbExecuteable) {
		this.adbExecuteable = adbExecuteable;
		this.cmdList = [adbExecuteable, "devices", "-l"];
	}
	
	public Collection<Device> getStoredDeviceList() {
		return deviceMap.values();
	}
	
	public int getNumberOfDevices() {
		return deviceMap.size();
	}
	
	public boolean hasDevices() {
		return !deviceMap.isEmpty();
	}
	
	public Device getFirstDevice() {
		return hasDevices() ? deviceMap.values().first() : null;
	} 
	
	public boolean hasDevice(final String id) {
		return deviceMap.containsKey(id);
	}
	
	public Device getDevice(final String id) {
		return deviceMap.get(id);
	}
	
	String trim(String stringToTrim) {
		return stringToTrim == null ? null : stringToTrim.trim();
	}
	
	public Map<String, Device> retrieveDevices(boolean storeDevices = true) {
		Map<String, Device> deviceMap = new HashMap<>();
		
		final OutputStream standardOutput = new ByteArrayOutputStream()
		final OutputStream errorOutput = new ByteArrayOutputStream()
	
		final Process proc = cmdList.execute()
		proc.waitForProcessOutput(standardOutput, errorOutput)

		LOGGER.debug "${proc.exitValue()}"
		
		final String devicesOutput = standardOutput.toString("UTF-8")

		LOGGER.debug "========== DEVICES ==========="
		LOGGER.debug "$adbExecuteable"
		LOGGER.debug "=============================="
		LOGGER.debug "$devicesOutput"
		LOGGER.debug "=============================="
		
		devicesOutput.eachLine { line, count ->
			
			LOGGER.debug "$line"
			
			final Matcher matcher = (line =~ /(.*)\s*device\s*product:(.*)\s*model:(.*)\s*device:(.*)/)
			if(matcher.matches()) {
				String id = trim(matcher.group(1));
				String productString = trim(matcher.group(2));
				String modelString = trim(matcher.group(3));
				String deviceString = trim(matcher.group(4));
				
				Device device = new Device(id, productString, modelString, deviceString);
				deviceMap.put(device.getId(), device);
			}
		}
		
		if(storeDevices) {
			this.deviceMap = new HashMap<>(deviceMap);
		}
		
		return deviceMap;
	}
}
