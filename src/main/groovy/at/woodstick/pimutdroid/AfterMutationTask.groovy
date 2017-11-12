package at.woodstick.pimutdroid

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

public class AfterMutationTask extends DefaultTask {
	private final static Logger LOGGER = Logging.getLogger(AfterMutationTask);
	
	String test;
	
	@TaskAction
	public void exec() {
		LOGGER.lifecycle "After mutation test ($test)"
		
		
	}	
}
