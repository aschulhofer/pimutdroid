package at.woodstick.pimutdroid.internal;

import java.io.File
import java.util.List

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import groovy.transform.CompileStatic

// TODO: rename into command, remove adb
@CompileStatic
public class AdbCommand {
	private final static Logger LOGGER = Logging.getLogger(AdbCommand)

	private static final int EXIT_VALUE_ERROR = 1;

	private File adbExecuteable;
	private List<?> commandList;
	
	private int exitValue;
	
	public AdbCommand(File adbExecuteable, List<?> commandList) {
		this.adbExecuteable = adbExecuteable;
		this.commandList = commandList;
	}	
	
	public int getExitValue() {
		return exitValue;
	}
	
	public boolean hasErrorExitValue() {
		return getExitValue() == EXIT_VALUE_ERROR;
	}

	public int execute(final OutputStream stdout, final OutputStream stderr) {
		final Process proc = commandList.flatten().execute();
		proc.waitForProcessOutput(stdout, stderr);

		exitValue = proc.exitValue();
		return exitValue;
	}
	
	public int execute() {
		final OutputStream stdout = new ByteArrayOutputStream();
		final OutputStream stderr = new ByteArrayOutputStream();
		
		exitValue = execute(stdout, stderr);
		
		return exitValue;
	}
	
	public String executeGetString() {
		final OutputStream stdout = new ByteArrayOutputStream();
		final OutputStream stderr = new ByteArrayOutputStream();
		
		LOGGER.debug "=============================="
		LOGGER.debug "$adbExecuteable"
		LOGGER.debug "=============================="
		LOGGER.debug "$commandList"
		LOGGER.debug "=============================="
		
		exitValue = execute(stdout, stderr);
		
		LOGGER.debug "$exitValue"
		
		final String output = stdout.toString("UTF-8");

		LOGGER.debug "=============================="
		LOGGER.debug "$output"
		LOGGER.debug "=============================="
		
		return output;
	}
}
