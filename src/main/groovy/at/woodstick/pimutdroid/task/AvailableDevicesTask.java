package at.woodstick.pimutdroid.task;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import at.woodstick.pimutdroid.internal.Device;
import at.woodstick.pimutdroid.internal.DeviceLister;

public class AvailableDevicesTask extends PimutBaseTask {
	static final Logger LOGGER = Logging.getLogger(AvailableDevicesTask.class);
	
	private DeviceLister deviceLister;
	
	@Override
	protected void beforeTaskAction() {
		deviceLister = getDeviceLister();
	}
	
	@Override
	protected void exec() {
		deviceLister.retrieveDevices();
		
		LOGGER.quiet("Found {} device(s)", deviceLister.getNumberOfDevices());
		
		deviceLister.getStoredDeviceList().stream().forEach((Device device) -> { 
			LOGGER.quiet("{}", device.getId());
		});
	}
}
