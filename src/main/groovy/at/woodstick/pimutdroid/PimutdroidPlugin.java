package at.woodstick.pimutdroid;

import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import com.android.build.gradle.BaseExtension;

import at.woodstick.pimutdroid.configuration.BuildConfiguration;
import at.woodstick.pimutdroid.internal.InfoTasksCreator;
import at.woodstick.pimutdroid.internal.PluginInternals;
import at.woodstick.pimutdroid.internal.PluginTasksCreator;
import info.solidsoft.gradle.pitest.PitestPluginExtension;

class PimutdroidPlugin implements Plugin<Project> {

	private final static Logger LOGGER = Logging.getLogger(PimutdroidPlugin.class);
	
	private Project project;
	
	@Override
	public void apply(Project project) {
		this.project = project;
		
		if(project.getPlugins().hasPlugin(PimutdroidPitestPlugin.class)) {
			throw new GradleException(String.format("Pimutdroid pitest plugin already applied!"));
		} else {
			project.getPlugins().apply(PimutdroidPitestPlugin.class);
		}
		
		addDependencies(project);
		
		final PimutdroidPluginExtension extension = getPluginExtension();
		
		BaseExtension androidExtension = project.getExtensions().findByType(BaseExtension.class);
		PitestPluginExtension pitestExtension = project.getExtensions().findByType(PitestPluginExtension.class);
		
		project.afterEvaluate(prj -> {
			LOGGER.debug("Project is evaluated.");
			
			PluginInternals pluginInternals = new PluginInternals(project, extension, androidExtension, pitestExtension, PimutdroidBasePlugin.PLUGIN_TASK_GROUP);
			pluginInternals.initialize();
			
			PluginTasksCreator pluginTasksCreator = new PluginTasksCreator(extension, pluginInternals, pluginInternals.getTaskFactory());
			pluginTasksCreator.createTasks();
			
			NamedDomainObjectContainer<BuildConfiguration> buildConfigurations = extension.getBuildConfiguration();
			buildConfigurations.all((BuildConfiguration config) -> { 
				LOGGER.debug("Create tasks for configuration {}", config.getName());
				pluginTasksCreator.createTasksForBuildConfiguration(config);
			});
		
			InfoTasksCreator infoTasksCreator = new InfoTasksCreator(pluginInternals, pluginInternals.getTaskFactory());
			infoTasksCreator.createTasks();
		});
	}
	
	protected PimutdroidPluginExtension getPluginExtension() {
		PimutdroidPluginExtension extension = project.getExtensions().getByType(PimutdroidPluginExtension.class);
		if(extension == null) {
			NamedDomainObjectContainer<BuildConfiguration> buildConfigurations = project.container(BuildConfiguration.class);
			extension = project.getExtensions().create(PimutdroidBasePlugin.PLUGIN_EXTENSION, PimutdroidPluginExtension.class, buildConfigurations);
		}
		
		return extension;
	}
	
	private boolean projectHasConfiguration(final String configurationName) {
		return ( project.getConfigurations().findByName(configurationName) != null );
	}
	
	private String getAndroidTestConfigurationName() {
		return projectHasConfiguration("androidTestImplementation") ? "androidTestImplementation" : "androidTestCompile";
	}
	
	protected void addDependencies(Project project) {
		project.getDependencies().add(getAndroidTestConfigurationName(), "de.schroepf:android-xml-run-listener:0.2.0");
	}
}
