package at.woodstick.pimutdroid.internal;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
		
		if(!cmdRunner.installTestApkOnDevice(testApkPath)) {
			LOGGER.error("On device '{}': install of test apk failed", deviceId);
			return;
		}
		
		final String resultFileRemotePath = getResultFileRemotePath();
		final String removeResultRemoteBlob = resultFileRemotePath + "/*.xml";
		final String remoteResultFile = resultFileRemotePath + "/" + RESULT_FILE_REMOTE_NAME;
		
		cmdRunner.removeResultOnDevice(removeResultRemoteBlob);
		
		int numberApksToTest = appApkPaths.size();
		int numberApksTested = 0;
		
		for(String mutantApkPath : appApkPaths) {
			numberApksTested++;
			long startTime = System.currentTimeMillis();
			
			String localResultPath = Paths.get(mutantApkPath).getParent().resolve(localFilename).toString();
			
			LOGGER.debug("Install mutant apk {} on device {}", mutantApkPath, deviceId);
			
			boolean installResult = cmdRunner.installOnDevice(mutantApkPath);
			
			if(installResult) {
				LOGGER.debug("Run tests of package {} with runner {} and options {} on device {}", testPackage, runnerClass, testOptions, deviceId);
				
				long startTimeTests = System.currentTimeMillis();
				boolean runTestsResult = cmdRunner.runTests(testPackage, runnerClass, testOptions);
				long endTimeTests = System.currentTimeMillis();
				
				LOGGER.debug("Fetch result from {} to {} on device {}", remoteResultFile, localResultPath, deviceId);
				
				boolean fetchResult = cmdRunner.fetchResult(remoteResultFile, localResultPath);
				
				boolean removeResult = cmdRunner.removeResultOnDevice(removeResultRemoteBlob);
				
				boolean clearTestPkg = cmdRunner.clearPackage(testPackage);
				boolean clearAppPkg = cmdRunner.clearPackage(appPackage);
			
				LOGGER.lifecycle("On device '{}': ({}/{})|(took: {})|(tests took: {}) - finished testing '{}'\n(inst: {}, test: {}, fetch: {}, remove: {}, clear test: {}, clear app: {})", deviceId, numberApksTested, numberApksToTest, duration(startTime), duration(startTimeTests, endTimeTests), mutantApkPath, installResult, runTestsResult, fetchResult, removeResult, clearTestPkg, clearAppPkg);
			} else {
				LOGGER.error("On device '{}': ({}/{})|(took: {}) - install failed '{}'", deviceId, numberApksTested, numberApksToTest, duration(startTime), mutantApkPath);
			}
		}

		boolean uninstallResult = cmdRunner.uninstallFromDevice(appPackage);
		LOGGER.lifecycle("On device '{}': uninstalled app '{}' ({})", deviceId, appPackage, uninstallResult);
	}
	
	protected String duration(long startTime) {
		long endTime = System.currentTimeMillis();
		return duration(startTime, endTime);
	}
	
	protected String duration(long startTime, long endTime) {
		long duration = (endTime - startTime);
		
		long seconds = (TimeUnit.MILLISECONDS.toSeconds(duration) % 60);
		long minutes = (TimeUnit.MILLISECONDS.toMinutes(duration) % 60);
		
		return String.format("%02dm:%02ds", minutes, seconds);
	}
	
	protected String getResultFileRemotePath() {
		return "/storage/emulated/0/Android/data/" + appPackage + "/files";
	}
}
