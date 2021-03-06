package at.woodstick.pimutdroid.task;

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction
import at.woodstick.pimutdroid.PimutdroidBasePlugin
import at.woodstick.pimutdroid.PimutdroidPlugin
import at.woodstick.pimutdroid.PimutdroidPluginExtension
import groovy.transform.CompileStatic

@CompileStatic
public class InfoTask extends DefaultTask {
	private final static Logger LOGGER = Logging.getLogger(InfoTask);

	@TaskAction
	void displayInfo() {
		PimutdroidPluginExtension extension = project.extensions.getByType(PimutdroidPluginExtension);
		
		LOGGER.quiet("Hello from pimutdroid!");
		LOGGER.quiet("Tasks in group: ${PimutdroidBasePlugin.PLUGIN_TASK_GROUP}");
		LOGGER.quiet("Project group: ${getProject().getGroup()}");
		LOGGER.quiet("Application id: ${extension.applicationId}");
		LOGGER.quiet("Application package: ${extension.applicationPackage}");
		LOGGER.quiet("Test application id: ${extension.testApplicationId}");
		LOGGER.quiet("BuildType: ${extension.testBuildType}");
		LOGGER.quiet("ProductFlavor: ${extension.productFlavor}");
		LOGGER.quiet("Ouput directory: ${extension.outputDir}");
		LOGGER.quiet("Mutants class files dir: ${extension.mutantClassesDir}");
		LOGGER.quiet("Mutant build result directory: ${extension.mutantResultRootDir}");
		LOGGER.quiet("Result report directory: ${extension.mutantReportRootDir}");
		LOGGER.quiet("Target mutants: ${extension.instrumentationTestOptions.targetMutants}");
		LOGGER.quiet("Instrumentation Test options: ${extension.instrumentationTestOptions}");
	}
}
