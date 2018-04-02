package at.woodstick.pimutdroid.internal;

import java.nio.file.Paths

import javax.inject.Inject

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import groovy.transform.CompileStatic

@CompileStatic
public class RunTestOnDevice implements Runnable {

	private static final Logger LOGGER = Logging.getLogger(RunTestOnDevice);
	
	private static final String RESULT_FILE_REMOTE_NAME = "report-0.xml";
	
	private Device device;
	
	private File adbExecuteable;
	private Map<String, List<String>> testOptions;
	private List<String> appApkPaths;
	private String testApkPath;
	private String testPackage;
	private String appPackage;
	private String runner;
	private String localFilename;
	
	@Inject
	public RunTestOnDevice(Device device, File adbExecuteable, Map<String, List<String>> testOptions,
			List<String> appApkPaths, String testApkPath, String testPackage, String appPackage, String runner,
			String localFilename) {
		this.device = device;
		this.adbExecuteable = adbExecuteable;
		this.testOptions = testOptions;
		this.appApkPaths = appApkPaths;
		this.testApkPath = testApkPath;
		this.testPackage = testPackage;
		this.appPackage = appPackage;
		this.runner = runner;
		this.localFilename = localFilename;
	}

	@Override
	public void run() {
		installOnDevice(device, testApkPath);
		
		String resultFileRemotePath = "/storage/emulated/0/Android/data/${appPackage}/files";
		
		removeResultOnDevice(device, "${resultFileRemotePath}/*.xml");
		
		appApkPaths.each { String mutantApkPath ->
			// TODO: pass per mutant result dir to store xml file
			String resultPath = Paths.get(mutantApkPath).getParent().resolve(localFilename).toString();
			
			installOnDevice(device, mutantApkPath);
			runTests(device, testPackage, testOptions);
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
		
	void runTests(Device device, String testPackage, Map<String, List<String>> testOptions) {
		
		Collection<String> testOptionList = testOptions.collectMany { String key, List<String> value -> ["-e", key, value.join(",")]};
		
		def commandList = [
			adbExecuteable,
			"-s",
			device.getId(),
			"shell", "am", "instrument",
			testOptionList,
			"-w",
			"${testPackage}/${runner}"
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
