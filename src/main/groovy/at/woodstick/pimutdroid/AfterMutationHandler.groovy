package at.woodstick.pimutdroid

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class AfterMutationHandler {
	
	private final static Logger LOGGER = Logging.getLogger(AfterMutationHandler);
	
	def execute() {
		println "After mutation"
	}
	
}
