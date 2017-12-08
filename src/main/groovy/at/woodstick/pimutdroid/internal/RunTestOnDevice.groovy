package at.woodstick.pimutdroid.internal;

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.List

import javax.inject.Inject

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import groovy.transform.CompileStatic

@CompileStatic
public class RunTestOnDevice implements Runnable {

	private static final Logger LOGGER = Logging.getLogger(RunTestOnDevice);
	
	private static final String RESULT_FILE_REMOTE_NAME = "report-0.xml";
	private static final String RESULT_FILE_LOCAL_NAME = "adb-test-report.xml";
	
	private Device device;
	
	private File adbExecuteable;
	
	private List<String> appApkPaths;
	private String testApkPath;
	private String testPackage;
	private String appPackage;
	
	@Inject
	public RunTestOnDevice(Device device, File adbExecuteable, List<String> appApkPaths, String testApkPath, String testPackage, String appPackage) {
		this.device = device;
		this.adbExecuteable = adbExecuteable;
		this.appApkPaths = appApkPaths;
		this.testApkPath = testApkPath;
		this.testPackage = testPackage;
		this.appPackage = appPackage;
	}

	@Override
	public void run() {
		installOnDevice(device, testApkPath);
		
		String resultFileRemotePath = "/storage/emulated/0/Android/data/${appPackage}/files";
		
		removeResultOnDevice(device, "${resultFileRemotePath}/*.xml");
		
		appApkPaths.each { String mutantApkPath ->
			// TODO: pass per mutant result dir to store xml file
			String resultPath = Paths.get(mutantApkPath).getParent().resolve(RESULT_FILE_LOCAL_NAME).toString();
			
			installOnDevice(device, mutantApkPath);
			runTests(device, testPackage);
			getResult(device, "${resultFileRemotePath}/${RunTestOnDevice.RESULT_FILE_REMOTE_NAME}", resultPath);
			
			removeResultOnDevice(device, "${resultFileRemotePath}/*.xml");
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
		
	void runTests(Device device, String testPackage) {	
		def commandList = [
			adbExecuteable,
			"-s",
			device.getId(),
			"shell", "am", "instrument",
			"-e",
			"listener",
			"de.schroepf.androidxmlrunlistener.XmlRunListener",
			"-w",
			"${testPackage}/android.support.test.runner.AndroidJUnitRunner"
		];
		
		AdbCommand adbCommand = new AdbCommand(adbExecuteable, commandList);
		String output = adbCommand.executeGetString();
		
		LOGGER.debug "$output"
		LOGGER.debug "${adbCommand.getExitValue()}"
	}
	
	void getResult(Device device, String remotePath, String localPath) {
		def commandList = [
			adbExecuteable,
			"-s",
			device.getId(),
			"pull",
			remotePath,
			localPath
		];
		
		AdbCommand adbCommand = new AdbCommand(adbExecuteable, commandList);
		String output = adbCommand.executeGetString();
		
		LOGGER.debug "$output"
		LOGGER.debug "${adbCommand.getExitValue()}"
	}

	void removeResultOnDevice(Device device, String remotePath) {
		def commandList = [
			adbExecuteable,
			"-s",
			device.getId(),
			"shell",
			"rm",
			remotePath
		];
		
		AdbCommand adbCommand = new AdbCommand(adbExecuteable, commandList);
		String output = adbCommand.executeGetString();
		
		LOGGER.debug "$output"
		LOGGER.debug "${adbCommand.getExitValue()}"
	}
}
