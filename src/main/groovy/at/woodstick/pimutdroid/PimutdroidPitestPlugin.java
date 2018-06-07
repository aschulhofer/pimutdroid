package at.woodstick.pimutdroid;

import static at.woodstick.pimutdroid.PimutdroidBasePlugin.FORCED_PITEST_DEPENDENCY;
import static at.woodstick.pimutdroid.PimutdroidBasePlugin.PITEST_CONFIGURATION_NAME;
import static at.woodstick.pimutdroid.PimutdroidBasePlugin.PITEST_DEPENDENCY_NAME;
import static at.woodstick.pimutdroid.PimutdroidBasePlugin.PITEST_VERSION;
import static at.woodstick.pimutdroid.PimutdroidBasePlugin.getBuildDir;
import static at.woodstick.pimutdroid.PimutdroidBasePlugin.getReportsDir;

import java.io.File;
import java.util.Collection;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import com.android.build.gradle.BaseExtension;

import at.woodstick.pimutdroid.configuration.BuildConfiguration;
import at.woodstick.pimutdroid.internal.AndroidVariant;
import at.woodstick.pimutdroid.internal.DefaultExtensionValuesCheck;
import at.woodstick.pimutdroid.internal.DefaultGroupTaskFactory;
import at.woodstick.pimutdroid.internal.ExtensionValuesCheck;
import at.woodstick.pimutdroid.internal.MutateClassesTaskCreator;
import at.woodstick.pimutdroid.internal.TaskFactory;
import at.woodstick.pimutdroid.internal.TaskGraphAdaptor;
import at.woodstick.pimutdroid.internal.VariantProvider;
import info.solidsoft.gradle.pitest.PitestPluginExtension;

class PimutdroidPitestPlugin implements Plugin<Project> {

	private final static Logger LOGGER = Logging.getLogger(PimutdroidPitestPlugin.class);
	
	private Project project;
	
	@Override
	public void apply(Project project) {
		this.project = project;

		project.getPlugins().apply(PimutdroidBasePlugin.class);
		
		final PimutdroidPluginExtension extension = getPluginExtension();
		
		BaseExtension androidExtension = project.getExtensions().findByType(BaseExtension.class);
		PitestPluginExtension pitestExtension = project.getExtensions().findByType(PitestPluginExtension.class);

		setDefaultValuesOnUsedPlugins(pitestExtension);

		project.afterEvaluate((prj) -> {
			LOGGER.debug("Project is evaluated.");
			
			Collection<AndroidVariant> variants = VariantProvider.withExtensionContainer(project.getExtensions()).getVariantsFrom(androidExtension);
			
			final File buildDir = getBuildDir(project);
			final File reportsDir = getReportsDir(project);
			
			ExtensionValuesCheck defaultExtensionValues = new DefaultExtensionValuesCheck(project.getName(), buildDir, reportsDir, extension, androidExtension, pitestExtension, variants);
			defaultExtensionValues.checkAndSetValues();
			
			if(extension.getForcePitestVersion()) {
				forcePitestDependency();
				LOGGER.debug("Pitest version forced to '{}'", PITEST_VERSION);
			}
			
			TaskFactory taskFactory = new DefaultGroupTaskFactory(project.getTasks(), PimutdroidBasePlugin.PLUGIN_TASK_GROUP);

			MutateClassesTaskCreator taskCreator = new MutateClassesTaskCreator(extension, pitestExtension, taskFactory);
			taskCreator.createMutateClassesTask();
			
			TaskGraphAdaptor taskGraph = new TaskGraphAdaptor(project.getGradle().getTaskGraph());
			taskGraph.whenReady(taskCreator::configureTasks);
			
			NamedDomainObjectContainer<BuildConfiguration> buildConfigurations = extension.getBuildConfiguration();
			buildConfigurations.all((BuildConfiguration config) -> { 
				LOGGER.debug("Create tasks for configuration {}", config.getName());
				taskCreator.createTasksForBuildConfiguration(config);
			});
		});
	}

	protected PimutdroidPluginExtension getPluginExtension() {
		NamedDomainObjectContainer<BuildConfiguration> buildConfigurations = project.container(BuildConfiguration.class);
		PimutdroidPluginExtension extension = project.getExtensions().create(PimutdroidBasePlugin.PLUGIN_EXTENSION, PimutdroidPluginExtension.class, buildConfigurations);
		
		return extension;
	}
	
	protected void setDefaultValuesOnUsedPlugins(PitestPluginExtension pitestExtension) {
		if(pitestExtension.getMaxMutationsPerClass() == null) {
			pitestExtension.setMaxMutationsPerClass(0);
		}
	}
	
	protected void forcePitestDependency() {
		ScriptHandler buildScript = project.getRootProject().getBuildscript();
		DependencySet pitestConifurationDependencies = buildScript.getConfigurations().getByName(PITEST_CONFIGURATION_NAME).getDependencies();
		
		pitestConifurationDependencies.removeIf((dependency) -> {
			if(PITEST_DEPENDENCY_NAME.equals(dependency.getName())) {
				LOGGER.debug("Remove pitest-command-line dependency: {}", dependency);
				return true;
			}
			
			return false;
		});
		
		buildScript.getDependencies().add(PITEST_CONFIGURATION_NAME, FORCED_PITEST_DEPENDENCY);
		
		pitestConifurationDependencies.all((dependency) -> {
			LOGGER.debug("Pitest dependency: {}", dependency);
		});
	}
}
