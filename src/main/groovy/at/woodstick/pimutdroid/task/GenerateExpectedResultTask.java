package at.woodstick.pimutdroid.task;

import java.util.Arrays;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import at.woodstick.pimutdroid.internal.AdbDeviceCommandBridge;
import at.woodstick.pimutdroid.internal.AppApk;
import at.woodstick.pimutdroid.internal.Device;
import at.woodstick.pimutdroid.internal.DeviceProvider;
import at.woodstick.pimutdroid.internal.DeviceTestOptionsProvider;
import at.woodstick.pimutdroid.internal.RunTestOnDevice;

public class GenerateExpectedResultTask extends PimutBaseTask {
	private static final Logger LOGGER = Logging.getLogger(GenerateExpectedResultTask.class);

	private DeviceProvider deviceProvider;
	
	private AppApk originalResultAppApk;
	private AppApk testAppApk;
	
	@Override
	protected void beforeTaskAction() {
		deviceProvider = getDeviceProvider();
		
		if(originalResultAppApk == null) {
			originalResultAppApk = getOriginalResultAppApk();
		}
		
		if(testAppApk == null) {
			testAppApk = getTestApk();
		}
	}
	
	@Override
	protected void exec() {
		Device device = deviceProvider.getDevice();
		
		AdbDeviceCommandBridge deviceBridge = getDeviceAdbCommandBridge(device);
		
		DeviceTestOptionsProvider testOptionsProvider = getDeviceTestOptionsProvider();
		
		RunTestOnDevice rtod = new RunTestOnDevice(
			deviceBridge,
			testOptionsProvider.getOptions(),
			Arrays.asList(originalResultAppApk.getPath().toString()),
			testAppApk.getPath().toString(),
			extension.getTestApplicationId(),
			extension.getApplicationId(),
			extension.getInstrumentationTestOptions().getRunner(),
			extension.getExpectedTestResultFilename()
		);
		
		rtod.run();
		
		LOGGER.info("Connected tests finished. Storing expected results.");
	}

}
