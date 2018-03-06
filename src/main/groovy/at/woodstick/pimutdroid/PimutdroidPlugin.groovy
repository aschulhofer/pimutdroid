package at.woodstick.pimutdroid;

import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.android.build.gradle.BaseExtension

import at.woodstick.pimutdroid.configuration.BuildConfiguration
import at.woodstick.pimutdroid.internal.DefaultExtensionValuesCheck
import at.woodstick.pimutdroid.internal.ExtensionValuesCheck
import at.woodstick.pimutdroid.internal.PluginInternals
import at.woodstick.pimutdroid.internal.PluginTasksCreator
import groovy.transform.CompileStatic
import info.solidsoft.gradle.pitest.PitestPlugin
import info.solidsoft.gradle.pitest.PitestPluginExtension

@CompileStatic
class PimutdroidPlugin implements Plugin<Project> {

	private final static Logger LOGGER = Logging.getLogger(PimutdroidPlugin);
	
	private Project project;
	
	@Override
	public void apply(Project project) {
		this.project = project;
		
		if(project.plugins.hasPlugin(PimutdroidPitestPlugin.class)) {
			throw new GradleException(String.format("Pimutdroid pitest plugin already applied!"));
		} else {
			project.plugins.apply(PimutdroidPitestPlugin.class);
		}
		
		addDependencies(project);
		
		NamedDomainObjectContainer<BuildConfiguration> buildConfigurations = project.container(BuildConfiguration);
		
		PimutdroidPluginExtension extension = project.extensions.getByType(PimutdroidPluginExtension);
		if(extension == null) {
			extension = project.extensions.create(PimutdroidBasePlugin.PLUGIN_EXTENSION, PimutdroidPluginExtension, buildConfigurations);
		}
		
		project.getPluginManager().apply(PitestPlugin);
		
		BaseExtension androidExtension = project.getExtensions().findByType(BaseExtension.class);
		PitestPluginExtension pitestExtension = project.getExtensions().findByType(PitestPluginExtension.class);
		
		setDefaultValuesOnUsedPlugins(androidExtension);
		setDefaultValuesOnUsedPlugins(pitestExtension);
		
		project.afterEvaluate {
			LOGGER.debug("Project is evaluated.");
			
			ExtensionValuesCheck defaultExtensionValues = new DefaultExtensionValuesCheck(project.getName(), project.getBuildDir(), extension, androidExtension, pitestExtension);
			defaultExtensionValues.checkAndSetValues();
			
			PluginInternals pluginInternals = new PluginInternals(project, extension, androidExtension, pitestExtension);
			pluginInternals.initialize();
			
			final PluginTasksCreator pluginTasksCreator = new PluginTasksCreator(extension, pluginInternals, pluginInternals.getTaskFactory(), PimutdroidBasePlugin.PLUGIN_TASK_GROUP);
			pluginTasksCreator.createTasks();
			
			buildConfigurations.all({ BuildConfiguration config ->
				LOGGER.debug("Create tasks for configuration {}", config.getName());
				pluginTasksCreator.createTasksForBuildConfiguration(config);
			});
		}
	}
	
	protected void setDefaultValuesOnUsedPlugins(BaseExtension androidExtension) {
		
	}
	
	protected void setDefaultValuesOnUsedPlugins(PitestPluginExtension pitestExtension) {
		if(pitestExtension.maxMutationsPerClass == null) {
			pitestExtension.maxMutationsPerClass = 0;
		}
	}
	
	private boolean projectHasConfiguration(final String configurationName) {
		return ( project.configurations.find({ Configuration conf -> return conf.getName().equalsIgnoreCase(configurationName) }) != null );
	}
	
	private String getAndroidTestConfigurationName() {
		return projectHasConfiguration("androidTestImplementation") ? "androidTestImplementation" : "androidTestCompile";
	}
	
	protected void addDependencies(Project project) {
		project.dependencies.add(getAndroidTestConfigurationName(), "de.schroepf:android-xml-run-listener:0.2.0");
	}
}
