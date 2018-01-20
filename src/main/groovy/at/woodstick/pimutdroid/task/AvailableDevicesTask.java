package at.woodstick.pimutdroid.task;

import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import at.woodstick.pimutdroid.internal.Device;
import at.woodstick.pimutdroid.internal.DeviceLister;

public class AvailableDevicesTask extends DefaultTask {
	static final Logger LOGGER = Logging.getLogger(AvailableDevicesTask.class);
	
	private DeviceLister deviceLister;
	
	@TaskAction
	void exec() throws IOException {
		deviceLister.retrieveDevices();
		
		LOGGER.quiet("Found {} device(s)", deviceLister.getNumberOfDevices());
		
		deviceLister.getStoredDeviceList().stream().forEach((Device device) -> { 
			LOGGER.quiet("{}", device.getId());
		});
	}

	public void setDeviceLister(DeviceLister deviceLister) {
		this.deviceLister = deviceLister;
	}
}
