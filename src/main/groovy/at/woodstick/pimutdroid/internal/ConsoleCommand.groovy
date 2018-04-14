package at.woodstick.pimutdroid.internal;

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import groovy.transform.CompileStatic

@CompileStatic
public class ConsoleCommand {
	private final static Logger LOGGER = Logging.getLogger(ConsoleCommand)

	private static final int EXIT_VALUE_ERROR = 1;

	private List<?> commandList;
	
	private int exitValue;
	
	public ConsoleCommand(List<?> commandList) {
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
