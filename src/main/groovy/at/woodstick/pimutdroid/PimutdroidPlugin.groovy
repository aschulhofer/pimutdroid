package at.woodstick.pimutdroid;

import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.reporting.ReportingExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidBasePlugin

import at.woodstick.pimutdroid.configuration.BuildConfiguration
import at.woodstick.pimutdroid.internal.PluginInternals
import at.woodstick.pimutdroid.internal.PluginTasksCreator
import groovy.transform.CompileStatic
import info.solidsoft.gradle.pitest.PitestPlugin
import info.solidsoft.gradle.pitest.PitestPluginExtension
import java.nio.file.Path

@CompileStatic
class PimutdroidPlugin implements Plugin<Project> {

	private final static Logger LOGGER = Logging.getLogger(PimutdroidPlugin);
	
	static final String PLUGIN_EXTENSION  = "pimut";
	static final String PLUGIN_TASK_GROUP = "Mutation";
	static final String RUNNER = "android.support.test.runner.AndroidJUnitRunner";
	
	private Project project;
	private PimutdroidPluginExtension extension;
	
	@Override
	public void apply(Project project) {
		this.project = project;
		
		if(!project.plugins.hasPlugin(AndroidBasePlugin.class)) {
			throw new GradleException(String.format("Android plugin must be applied to project"));
		}
		
		addDependencies(project);
		
		NamedDomainObjectContainer<BuildConfiguration> buildConfigurations = project.container(BuildConfiguration);
		
		extension = project.extensions.create(PLUGIN_EXTENSION, PimutdroidPluginExtension, project, buildConfigurations);
		
		project.getPluginManager().apply(PitestPlugin);
		
		addMutationDependencies(project);
		
		BaseExtension androidExtension = project.getExtensions().findByType(BaseExtension.class);
		PitestPluginExtension pitestExtension = project.getExtensions().findByType(PitestPluginExtension.class);
				
		setDefaultValuesOnUsedPlugins(androidExtension);
		
		project.afterEvaluate {
			LOGGER.lifecycle("Project is evaluated.");
			
			setDefaultExtensionValues(androidExtension, pitestExtension);

			PluginInternals pluginInternals = new PluginInternals(project, extension, androidExtension);
			pluginInternals.initialize();
			
			final PluginTasksCreator pluginTasksCreator = new PluginTasksCreator(extension, pluginInternals, pluginInternals.getTaskFactory(), PLUGIN_TASK_GROUP);
			pluginTasksCreator.createTasks();
			
			buildConfigurations.all({ BuildConfiguration config ->
				LOGGER.lifecycle(config.getName());
				pluginTasksCreator.createTasksForBuildConfiguration(config);
			});
		}
	}
	
	protected void setDefaultValuesOnUsedPlugins(BaseExtension androidExtension) {
		
		Path reportingBasePath = project.getExtensions().getByType(ReportingExtension.class).getBaseDir().toPath();
		
		if(androidExtension.testOptions.resultsDir == null) {
			androidExtension.testOptions.resultsDir = "${reportingBasePath}/mutation/test-results";
		}
		
		if(androidExtension.testOptions.reportDir == null) {
			androidExtension.testOptions.reportDir = "${reportingBasePath}/mutation/test-reports";
		}
	}
	
	protected void setDefaultValuesOnUsedPlugins(PitestPluginExtension pitestExtension) {
		
	}
	
	protected void setDefaultExtensionValues(BaseExtension androidExtension, PitestPluginExtension pitest) {
		
		if(extension.applicationId == null) {
			extension.applicationId = androidExtension.defaultConfig.applicationId;
		}

		if(extension.testApplicationId == null) {
			extension.testApplicationId = (androidExtension.defaultConfig.testApplicationId ?: "${extension.applicationId}.test");
		}
				
		if(extension.packageDir == null) {
			extension.packageDir = extension.applicationId.replaceAll("\\.", "/")
		}
		
		if(extension.mutantsDir == null) {
			extension.mutantsDir = "${pitest.reportDir}/debug"
		}
		
		if(extension.instrumentationTestOptions.runner == null && androidExtension.defaultConfig.testInstrumentationRunner != null) {
			extension.instrumentationTestOptions.runner = androidExtension.defaultConfig.testInstrumentationRunner
		}
		else {
			extension.instrumentationTestOptions.runner = RUNNER;
		}
		
		if(extension.instrumentationTestOptions.targetMutants == null || extension.instrumentationTestOptions.targetMutants.empty) {
			extension.instrumentationTestOptions.targetMutants = [extension.packageDir].toSet();
		}
		
		if(extension.outputDir == null) {
			extension.outputDir = "${project.buildDir}/mutation/result";
		}
		
		if(extension.testResultDir == null) {
			extension.testResultDir = androidExtension.testOptions.resultsDir
		}
		
		if(extension.testReportDir == null) {
			extension.testReportDir = androidExtension.testOptions.reportDir
		}
		
		if(extension.mutantResultRootDir == null) {
			extension.mutantResultRootDir = "${extension.outputDir}/mutants"
		}
		
		if(extension.appResultRootDir == null) {
			extension.appResultRootDir = "${extension.outputDir}/app/debug"
		}
		
		if(extension.classFilesDir == null) {
			extension.classFilesDir = "${project.buildDir}/intermediates/classes/debug"
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
	
	protected void addMutationDependencies(Project project) {
		project.rootProject.buildscript.configurations.maybeCreate(PitestPlugin.PITEST_CONFIGURATION_NAME);
		project.rootProject.buildscript.dependencies.add(PitestPlugin.PITEST_CONFIGURATION_NAME, project.files("${project.projectDir}/mutantLibs/pitest-export-plugin-0.1-SNAPSHOT.jar"));
	}
}
