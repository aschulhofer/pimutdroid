package at.woodstick.pimutdroid.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class AdbDeviceCommandBridge implements Serializable {

	/**
	 * Serializeable to be passed as argument to {@link RunTestOnDevice}
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = Logging.getLogger(AdbDeviceCommandBridge.class);
	
	
	private Device device;
	private AdbCommandFactory commandFactory;
	
	public AdbDeviceCommandBridge(Device device, AdbCommandFactory commandFactory) {
		this.device = device;
		this.commandFactory = commandFactory;
	}

	public String getDeviceId() {
		return device.getId();
	}

	public boolean installOnDevice(String apkPath) {
		ConsoleCommand installAppCommand = commandFactory.installReplaceApk(getDeviceId(), apkPath);
		
		return runAdbCommand(installAppCommand);
	}
		
	public boolean runTests(String testPackage, String runner, Map<String, List<String>> testOptions) {
//		Collection<String> testOptionList = testOptions.collectMany { String key, List<String> value -> ["-e", key, value.join(",")]};
		List<String> testOptionList = new ArrayList<String>();
		for(Map.Entry<String, List<String>> entry : testOptions.entrySet()) {
			testOptionList.add("-e");
			testOptionList.add(entry.getKey());
			testOptionList.add(String.join(",", entry.getValue()));
		}
		
		ConsoleCommand runTestsCommand = commandFactory.runTests(getDeviceId(), testOptionList, testPackage, runner);
		
		return runAdbCommand(runTestsCommand);
	}
	
	public boolean fetchResult(String remotePath, String localPath) {
		ConsoleCommand pullCommand = commandFactory.pull(getDeviceId(), remotePath, localPath);

		return runAdbCommand(pullCommand);
	}

	public boolean removeResultOnDevice(String remotePath) {
		ConsoleCommand removeCommand = commandFactory.remove(getDeviceId(), remotePath);
		
		return runAdbCommand(removeCommand);
	}
	
	protected boolean runAdbCommand(ConsoleCommand adbCommand) {
		String output = adbCommand.executeGetString();
		
		LOGGER.debug("{}", adbCommand.getExitValue());
		
		if(adbCommand.hasErrorExitValue()) {
			LOGGER.error("{}", output);
			return false;
		} else {
			LOGGER.debug("{}", output);
			return true;
		}
	}
}
