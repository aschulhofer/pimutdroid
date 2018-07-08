package at.woodstick.pimutdroid.internal;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class AdbCommandFactory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private File adbExecuteable;
	
	public AdbCommandFactory(File adbExecuteable) {
		this.adbExecuteable = adbExecuteable;
	}

	public ConsoleCommand runTests(final String deviceId, final List<String> testOptionList, String testPackage, String runner) {
		List<?> commandList = Arrays.asList(
			adbExecuteable,
			"-s",
			deviceId,
			"shell", "am", "instrument",
			testOptionList,
			"-w",
			testPackage + "/" + runner
		);
		
		return new ConsoleCommand(commandList);
	}
	
	public ConsoleCommand uninstallPackage(final String deviceId, final String pkg) {
		List<?> commandList = Arrays.asList(
			adbExecuteable,
			"-s",
			deviceId,
			"shell", "pm", "uninstall",
			pkg
		);
		
		return new ConsoleCommand(commandList);
	}
	
	public ConsoleCommand clearPackage(final String deviceId, final String pkg) {
		List<?> commandList = Arrays.asList(
			adbExecuteable,
			"-s",
			deviceId,
			"shell", "pm", "clear",
			pkg
		);
		
		return new ConsoleCommand(commandList);
	}
	
	public ConsoleCommand installReplaceApk(final String deviceId, final String apkPath) {
		List<?> commandList = Arrays.asList(
			adbExecuteable,
			"-s",
			deviceId,
			"install", "-r",
			apkPath
		);
		
		return new ConsoleCommand(commandList);
	}
	
	public ConsoleCommand installReplaceTestApk(final String deviceId, final String apkPath) {
		List<?> commandList = Arrays.asList(
			adbExecuteable,
			"-s",
			deviceId,
			"install", "-r", "-t",
			apkPath
		);
		
		return new ConsoleCommand(commandList);
	}
	
	public ConsoleCommand pull(final String deviceId, final String remotePath, final String localPath) {
		List<?> commandList = Arrays.asList(
			adbExecuteable,
			"-s",
			deviceId,
			"pull",
			remotePath,
			localPath
		);
		
		return new ConsoleCommand(commandList);
	}
	
	public ConsoleCommand remove(final String deviceId, final String remotePath) {
		List<?> commandList = Arrays.asList(
			adbExecuteable,
			"-s",
			deviceId,
			"shell", "rm",
			remotePath
		);
		
		return new ConsoleCommand(commandList);
	}
	
	public static AdbCommandFactory newFactory(File adbExecuteable) {
		return new AdbCommandFactory(adbExecuteable);
	}
}
