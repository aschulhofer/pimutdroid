package at.woodstick.pimutdroid.task;

import java.util.List;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import at.woodstick.pimutdroid.internal.Device;
import at.woodstick.pimutdroid.internal.DeviceProvider;

public class ConfiguredDevicesTask extends PimutBaseTask {
	static final Logger LOGGER = Logging.getLogger(ConfiguredDevicesTask.class);
	
	private DeviceProvider deviceProvider;
	
	@Override
	protected void beforeTaskAction() {
		deviceProvider = getDeviceProvider();
	}
	
	@Override
	protected void exec() {
		
		List<Device> devices = deviceProvider.getDevices();
		
		LOGGER.quiet("'{}' device(s)", devices.size());
		
		devices.stream().forEach((Device device) -> { 
			LOGGER.quiet("{}", device.getId());
		});
	}
}
