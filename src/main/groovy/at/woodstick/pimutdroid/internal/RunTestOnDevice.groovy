package at.woodstick.pimutdroid.internal;

import java.io.File

import javax.inject.Inject

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

public class RunTestOnDevice implements Runnable {

	private final static Logger LOGGER = Logging.getLogger(RunTestOnDevice);
	
	private Device device;
	
	private File adbExecuteable;
	
	private String appApkPath;
	private String testApkPath;
	private String testPackage;
	
	@Inject
	public RunTestOnDevice(Device device, File adbExecuteable, String appApkPath, String testApkPath, String testPackage) {
	this.device = device;
	this.adbExecuteable = adbExecuteable;
	this.appApkPath = appApkPath;
	this.testApkPath = testApkPath;
	this.testPackage = testPackage;
}

	@Override
	public void run() {
		installOnDevice(device);
	}

	void installOnDevice(Device device) {
		def installAppcommandList = [
			adbExecuteable,
			"-s",
			device.getId(),
			"install",
			"-r",
			appApkPath
		];
		
		AdbCommand installAppCommand = new AdbCommand(adbExecuteable, installAppcommandList);
		String output = installAppCommand.executeGetString();
		
		LOGGER.debug "$output"
		LOGGER.debug "${installAppCommand.getExitValue()}"
		
		
		
		
		def installTestApkcommandList = [
			adbExecuteable,
			"-s",
			device.getId(),
			"install",
			"-r",
			testApkPath
		];
		AdbCommand installTestApkCommand = new AdbCommand(adbExecuteable, installTestApkcommandList);
		output = installTestApkCommand.executeGetString();
		
		LOGGER.debug "$output"
		LOGGER.debug "${installTestApkCommand.getExitValue()}"
		
		
		
		def commandList = [
			adbExecuteable,
			"-s",
			device.getId(),
			"shell", "am", "instrument",
			"-w",
			"${testPackage}/android.support.test.runner.AndroidJUnitRunner"
		];
		
		AdbCommand adbCommand = new AdbCommand(adbExecuteable, commandList);
		output = adbCommand.executeGetString();
		
		LOGGER.debug "$output"
		LOGGER.debug "${adbCommand.getExitValue()}"
	}

}
