package at.woodstick.pimutdroid.task;

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import at.woodstick.pimutdroid.PimutdroidPlugin
import at.woodstick.pimutdroid.PimutdroidPluginExtension

public class InfoTask extends DefaultTask {
	private final static Logger LOGGER = Logging.getLogger(InfoTask);

	@TaskAction
	void displayInfo() {
		PimutdroidPluginExtension extension = project.extensions[PimutdroidPlugin.PLUGIN_EXTENSION];
		
		LOGGER.quiet "Hello from pimutdroid!"
		LOGGER.quiet "Tasks in group: ${PimutdroidPlugin.PLUGIN_TASK_GROUP}"
		LOGGER.quiet "Mutants dir: ${extension.mutantsDir}"
		LOGGER.quiet "Package of mutants: ${extension.packageDir}"
		LOGGER.quiet "Output mutateAll to console: ${extension.outputMutateAll}"
		LOGGER.quiet "Output mutation task creation to console: ${extension.outputMutantCreation}"
		LOGGER.quiet "Run mutateAll for max first mutants: ${extension.maxFirstMutants}"
		LOGGER.quiet "Result ouput directory: ${extension.outputDir}"
		LOGGER.quiet "Target mutants: ${extension.instrumentationTestOptions.targetMutants}"
		LOGGER.quiet "Instrumentation Test options: ${extension.instrumentationTestOptions}"
	}
}
