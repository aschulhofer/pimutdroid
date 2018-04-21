package at.woodstick.pimutdroid.internal;

import static at.woodstick.pimutdroid.internal.StringEachLine.eachLineOf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class DeviceLister {
	private final static Logger LOGGER = Logging.getLogger(DeviceLister.class);
	
	private static final Pattern DEVICES_DATA_PATTERN = Pattern.compile("(.*?)\\s*device\\s*product:(.*?)\\s*model:(.*?)\\s*device:(.*?)\\s*transport_id:.*");
	
	private Map<String, Device> deviceMap = new HashMap<>();
	private ListDevicesCommand devicesCommand;
	
	public DeviceLister(ListDevicesCommand command) {
		this.devicesCommand = command;
	}
	
	public Collection<Device> getStoredDeviceList() {
		return deviceMap.values();
	}
	
	public int getNumberOfDevices() {
		return deviceMap.size();
	}
	
	public boolean noDevicesConnected() {
		return deviceMap.isEmpty();
	}
	
	public boolean hasDevices() {
		return !noDevicesConnected();
	}
	
	public Device getFirstDevice() {
		return hasDevices() ? deviceMap.values().iterator().next() : null;
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
	
	public Map<String, Device> retrieveDevices() {
		return retrieveDevices(true);
	}
	
	public Map<String, Device> retrieveDevices(boolean storeDevices) {
		Map<String, Device> deviceMap = new HashMap<>();
		
		final String devicesOutput = devicesCommand.executeGetString();

		LOGGER.debug("========== DEVICES ===========");
		LOGGER.debug("{}", devicesOutput);
		LOGGER.debug("==============================");
		LOGGER.debug("{}", devicesCommand.getExitValue());
		LOGGER.debug("==============================");
		
		eachLineOf(devicesOutput).call(line -> {
			LOGGER.debug("{}", line);
			
			final Matcher matcher = DEVICES_DATA_PATTERN.matcher(line);
					
			if(matcher.matches()) {
				String id = trim(matcher.group(1));
				String productString = trim(matcher.group(2));
				String modelString = trim(matcher.group(3));
				String deviceString = trim(matcher.group(4));
				
				Device device = new Device(id, productString, modelString, deviceString);
				deviceMap.put(device.getId(), device);
			}
			
		});
		
		if(storeDevices) {
			this.deviceMap = new HashMap<>(deviceMap);
		}
		
		return deviceMap;
	}
}
