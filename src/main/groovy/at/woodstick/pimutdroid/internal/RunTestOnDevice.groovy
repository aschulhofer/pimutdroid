package at.woodstick.pimutdroid.internal;

import java.io.File

import javax.inject.Inject

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

public class RunTestOnDevice implements Runnable {

	private final static Logger LOGGER = Logging.getLogger(RunTestOnDevice);
	
	private Device device;
	
	private File adbExecuteable;
	
	private List<String> appApkPaths;
	private String testApkPath;
	private String testPackage;
	
	@Inject
	public RunTestOnDevice(Device device, File adbExecuteable, List<String> appApkPaths, String testApkPath, String testPackage) {
		this.device = device;
		this.adbExecuteable = adbExecuteable;
		this.appApkPaths = appApkPaths;
		this.testApkPath = testApkPath;
		this.testPackage = testPackage;
	}

	@Override
	public void run() {
		installOnDevice(device, testApkPath);
		
		appApkPaths.each {
			installOnDevice(device, appApkPaths.first());
			runTests(device);
		}
	}
	
	void installOnDevice(Device device, String apkPath) {
		def installAppcommandList = [
			adbExecuteable,
			"-s",
			device.getId(),
			"install",
			"-r",
			apkPath
		];
		
		AdbCommand installAppCommand = new AdbCommand(adbExecuteable, installAppcommandList);
		String output = installAppCommand.executeGetString();
		
		LOGGER.debug "$output"
		LOGGER.debug "${installAppCommand.getExitValue()}"
	}
		
		
	void runTests(Device device) {	
		def commandList = [
			adbExecuteable,
			"-s",
			device.getId(),
			"shell", "am", "instrument",
			"-w",
			"${testPackage}/android.support.test.runner.AndroidJUnitRunner"
		];
		
		AdbCommand adbCommand = new AdbCommand(adbExecuteable, commandList);
		String output = adbCommand.executeGetString();
		
		LOGGER.debug "$output"
		LOGGER.debug "${adbCommand.getExitValue()}"
	}
	
	
//	void installTestApk(Device device) {
//		def installTestApkcommandList = [
//			adbExecuteable,
//			"-s",
//			device.getId(),
//			"install",
//			"-r",
//			testApkPath
//		];
//		AdbCommand installTestApkCommand = new AdbCommand(adbExecuteable, installTestApkcommandList);
//		String output = installTestApkCommand.executeGetString();
//		
//		LOGGER.debug "$output"
//		LOGGER.debug "${installTestApkCommand.getExitValue()}"
//	}

}
