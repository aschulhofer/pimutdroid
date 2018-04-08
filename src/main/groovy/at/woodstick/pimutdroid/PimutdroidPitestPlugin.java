package at.woodstick.pimutdroid;

import static at.woodstick.pimutdroid.PimutdroidBasePlugin.getBuildDir;
import static at.woodstick.pimutdroid.PimutdroidBasePlugin.getReportsDir;

import java.io.File;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import com.android.build.gradle.BaseExtension;

import at.woodstick.pimutdroid.configuration.BuildConfiguration;
import at.woodstick.pimutdroid.internal.DefaultExtensionValuesCheck;
import at.woodstick.pimutdroid.internal.DefaultGroupTaskFactory;
import at.woodstick.pimutdroid.internal.ExtensionValuesCheck;
import at.woodstick.pimutdroid.internal.MutateClassesTaskCreator;
import at.woodstick.pimutdroid.internal.TaskFactory;
import at.woodstick.pimutdroid.internal.TaskGraphAdaptor;
import groovy.transform.CompileStatic;
import info.solidsoft.gradle.pitest.PitestPlugin;
import info.solidsoft.gradle.pitest.PitestPluginExtension;

@CompileStatic
class PimutdroidPitestPlugin implements Plugin<Project> {

	private final static Logger LOGGER = Logging.getLogger(PimutdroidPitestPlugin.class);
	
	private Project project;
	
	@Override
	public void apply(Project project) {
		this.project = project;

		project.getPlugins().apply(PimutdroidBasePlugin.class);
		
		NamedDomainObjectContainer<BuildConfiguration> buildConfigurations = project.container(BuildConfiguration.class);
		
		PimutdroidPluginExtension extension = project.getExtensions().create(PimutdroidBasePlugin.PLUGIN_EXTENSION, PimutdroidPluginExtension.class, buildConfigurations);
		
		maybeApplyPitest();
		
		BaseExtension androidExtension = project.getExtensions().findByType(BaseExtension.class);
		PitestPluginExtension pitestExtension = project.getExtensions().findByType(PitestPluginExtension.class);
		
		setDefaultValuesOnUsedPlugins(androidExtension);
		setDefaultValuesOnUsedPlugins(pitestExtension);
		
		project.afterEvaluate((prj) -> {
			LOGGER.debug("Project is evaluated.");
			
			final File buildDir = getBuildDir(project);
			final File reportsDir = getReportsDir(project);
			
			ExtensionValuesCheck defaultExtensionValues = new DefaultExtensionValuesCheck(project.getName(), buildDir, reportsDir, extension, androidExtension, pitestExtension);
			defaultExtensionValues.checkAndSetValues();
			
			TaskFactory taskFactory = new DefaultGroupTaskFactory(project.getTasks(), PimutdroidBasePlugin.PLUGIN_TASK_GROUP);

			MutateClassesTaskCreator taskCreator = new MutateClassesTaskCreator(extension, pitestExtension, taskFactory);
			taskCreator.createMutateClassesTask();
			
			TaskGraphAdaptor taskGraph = new TaskGraphAdaptor(project.getGradle().getTaskGraph());
			taskGraph.whenReady(taskCreator::configureTasks);
			
			buildConfigurations.all((BuildConfiguration config) -> { 
				LOGGER.debug("Create tasks for configuration {}", config.getName());
				taskCreator.createTasksForBuildConfiguration(config);
			});
		});
	}

	protected void maybeApplyPitest() {
		if(!project.getPlugins().hasPlugin(PitestPlugin.class)) {
			project.getPluginManager().apply(PitestPlugin.class);
		}
	}
	
	protected void setDefaultValuesOnUsedPlugins(BaseExtension androidExtension) {
	
	}

	protected void setDefaultValuesOnUsedPlugins(PitestPluginExtension pitestExtension) {
		pitestExtension.setPitestVersion(PimutdroidBasePlugin.PITEST_VERSION);
		
		if(pitestExtension.getMaxMutationsPerClass() == null) {
			pitestExtension.setMaxMutationsPerClass(0);
		}
	}
}
