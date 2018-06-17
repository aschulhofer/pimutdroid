package at.woodstick.pimutdroid.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import at.woodstick.pimutdroid.configuration.DevicesOptions;

public class DeviceProvider {

	private static final Logger LOGGER = Logging.getLogger(DeviceProvider.class);
	
	private DevicesOptions options;
	private DeviceLister deviceLister;
	private AndroidSerialProvider androidSerialProvider;
	
	public DeviceProvider(DevicesOptions options, DeviceLister deviceLister, AndroidSerialProvider androidSerialProvider) {
		this.options = options;
		this.deviceLister = deviceLister;
		this.androidSerialProvider = androidSerialProvider;
	}

	public Device getDevice() {
		retrieveDevices();
		
		if(!options.getIgnoreAndroidSerial()) {
			if(androidSerialProvider.hasAndroidSerial()) {
				String serialNumber = androidSerialProvider.getAndroidSerial();
				LOGGER.debug("Got serial '{}' from ANDROID_SERIAL", serialNumber);
				
				return getDeviceFromLister(serialNumber);
			}
		}
		
		Set<String> serialNumbers = options.getSerialNumbers();
		
		if(serialNumbers.isEmpty()) {
			LOGGER.debug("Get first device");
			return deviceLister.getFirstDevice();
		} else {
			for(String serial : serialNumbers) {
				Optional<Device> device = getDeviceFromListerIfAvailable(serial);

				if(device.isPresent()) {
					LOGGER.debug("Found configured device '{}'", serial);
					return device.get();
				} else {
					LOGGER.warn("Configured device '{}' not found, try next", serial);
				}
			}
			
			throw new PimutdroidException("No configured device found to connect to");
		}
	}

	public List<Device> getDevices() {
		List<Device> devices = new ArrayList<>();
		Set<String> serialNumbers = options.getSerialNumbers();
		
		if(!options.getIgnoreAndroidSerial() || !options.getParallelExecution()) {
			Device device = getDevice();
			devices.add(device);
			return devices;
		}

		retrieveDevices();
		
		if(serialNumbers.isEmpty()) {
			for(Device device : deviceLister.getStoredDeviceList()) {
				devices.add(device);
			}
		} else {
			for(String serialNumber : serialNumbers) {
				if(deviceLister.hasDevice(serialNumber)) {
					LOGGER.debug("Configured device '{}' available", serialNumber);
					devices.add(deviceLister.getDevice(serialNumber));
				} else {
					LOGGER.warn("Configured device with '{}' not available, will be ignored", serialNumber);
				}
			}
		}
		
		return devices;
	}
	
	protected Device getDeviceFromLister(String serialNumber) {
		if(!deviceLister.hasDevice(serialNumber)) {
			throw new PimutdroidException("No device found for id '" + serialNumber + "'");
		} else {
			return deviceLister.getDevice(serialNumber);
		}
	}
	
	protected Optional<Device> getDeviceFromListerIfAvailable(String serialNumber) {
		if(deviceLister.hasDevice(serialNumber)) {
			return Optional.of(deviceLister.getDevice(serialNumber));
		}
		
		return Optional.empty();
	}
	
	protected void retrieveDevices() {
		deviceLister.retrieveDevices();
		if(deviceLister.noDevicesConnected()) {
			LOGGER.error("No devices connected");
			throw new GradleException("No devices connected. Please connect a device.");
		}
	}
}
