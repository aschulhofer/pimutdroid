package at.woodstick.pimutdroid.internal;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class RunTestOnDevice implements Runnable {

	private static final Logger LOGGER = Logging.getLogger(RunTestOnDevice.class);
	
	private static final String RESULT_FILE_REMOTE_NAME = "report-0.xml";
	
	private Map<String, List<String>> testOptions;
	private List<String> appApkPaths;
	private String testApkPath;
	private String testPackage;
	private String appPackage;
	private String runnerClass;
	private String localFilename;
	
	private AdbDeviceCommandBridge cmdRunner;
	
	@Inject
	public RunTestOnDevice(AdbDeviceCommandBridge cmdRunner, Map<String, List<String>> testOptions,
			List<String> appApkPaths, String testApkPath, String testPackage, String appPackage, String runnerClass,
			String localFilename) {
		this.cmdRunner = cmdRunner;
		this.testOptions = testOptions;
		this.appApkPaths = appApkPaths;
		this.testApkPath = testApkPath;
		this.testPackage = testPackage;
		this.appPackage = appPackage;
		this.runnerClass = runnerClass;
		this.localFilename = localFilename;
	}

	@Override
	public void run() {
		
		String deviceId = cmdRunner.getDeviceId();
		
		LOGGER.debug("Install test apk {} on device {}", testApkPath, deviceId);
		
		cmdRunner.installOnDevice(testApkPath);
		
		final String resultFileRemotePath = getResultFileRemotePath();
		final String removeResultRemoteBlob = resultFileRemotePath + "/*.xml";
		final String remoteResultFile = resultFileRemotePath + "/" + RESULT_FILE_REMOTE_NAME;
		
		cmdRunner.removeResultOnDevice(removeResultRemoteBlob);
		
		for(String mutantApkPath : appApkPaths) {
			// TODO: pass per mutant result dir to store xml file
			String localResultPath = Paths.get(mutantApkPath).getParent().resolve(localFilename).toString();
			
			LOGGER.debug("Install mutant apk {} on device {}", mutantApkPath, deviceId);
			
			cmdRunner.installOnDevice(mutantApkPath);
			
			LOGGER.debug("Run tests of package {} with runner {} and options {} on device {}", testPackage, runnerClass, testOptions, deviceId);
			
			cmdRunner.runTests(testPackage, runnerClass, testOptions);
			
			LOGGER.debug("Fetch result from {} to {} on device {}", remoteResultFile, localResultPath, deviceId);
			
			cmdRunner.fetchResult(remoteResultFile, localResultPath);
			
			cmdRunner.removeResultOnDevice(removeResultRemoteBlob);
		}
	}
	
	protected String getResultFileRemotePath() {
		return "/storage/emulated/0/Android/data/" + appPackage + "/files";
	}
}
