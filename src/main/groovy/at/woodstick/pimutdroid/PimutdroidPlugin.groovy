package at.woodstick.pimutdroid;

import java.nio.file.Paths

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.GradleBuild

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidBasePlugin

import at.woodstick.pimutdroid.internal.AndroidTestResult
import at.woodstick.pimutdroid.internal.AppApk
import at.woodstick.pimutdroid.internal.AppClassFiles
import at.woodstick.pimutdroid.internal.Device
import at.woodstick.pimutdroid.internal.DeviceLister
import at.woodstick.pimutdroid.internal.DeviceTestOptionsProvider
import at.woodstick.pimutdroid.internal.MarkerFileFactory
import at.woodstick.pimutdroid.internal.MutantClassFileFactory
import at.woodstick.pimutdroid.internal.MutationFilesProvider
import at.woodstick.pimutdroid.internal.PluginInternals
import at.woodstick.pimutdroid.internal.PluginTasksCreator
import at.woodstick.pimutdroid.internal.RunTestOnDevice
import at.woodstick.pimutdroid.internal.TaskFactory
import at.woodstick.pimutdroid.task.AfterMutationTask
import at.woodstick.pimutdroid.task.BuildMutantApkTask
import at.woodstick.pimutdroid.task.BuildMutantsTask
import at.woodstick.pimutdroid.task.InfoTask
import at.woodstick.pimutdroid.task.MutationTestExecutionTask
import at.woodstick.pimutdroid.task.PrepareMutantFilesTask
import at.woodstick.pimutdroid.task.ReplaceClassWithMutantTask
import info.solidsoft.gradle.pitest.PitestPlugin

//@CompileStatic
class PimutdroidPlugin implements Plugin<Project> {

	private final static Logger LOGGER = Logging.getLogger(PimutdroidPlugin);
	
	static final String PLUGIN_EXTENSION  = "pimut";
	static final String PLUGIN_TASK_GROUP = "Mutation";
	
	private Project project;
	private PimutdroidPluginExtension extension;
	
	private String runner = "android.support.test.runner.AndroidJUnitRunner";

	private PluginInternals pluginInternals;
	
	private TaskFactory taskFactory;
	private PluginTasksCreator pluginTasksCreator;
		
	private File adbExecuteable;
	private MutationFilesProvider mutationFilesProvider;
	private MarkerFileFactory markerFileFactory;
	private MutantClassFileFactory mutantClassFileFactory;
	private DeviceTestOptionsProvider deviceTestOptionsProvider;
	private DeviceLister deviceLister;
	
	private AppClassFiles appClassFiles;
	private AndroidTestResult androidTestResult;
	private AppApk appApk;
	private AppApk appTestApk;
	
	private Task createTask(String name, Closure closure) {
		return project.task([group: PLUGIN_TASK_GROUP], name, closure);
	}
	
	private Task createTask(String name, Map<String, ?> args, boolean useDefaultGroup = true, Closure closure) {
		if(useDefaultGroup) {
			args["group"] = PLUGIN_TASK_GROUP;
		}
		return project.task(args, name, closure);
	}
	
	private boolean projectHasConfiguration(final String configurationName) {
		return ( project.configurations.find({ conf -> return conf.getName().equalsIgnoreCase(configurationName) }) != null );
	}
	
	private String getAndroidTestConfigurationName() {
		return projectHasConfiguration("androidTestImplementation") ? "androidTestImplementation" : "androidTestCompile";
	}
	
	protected void addDependencies(Project project) {
		project.rootProject.buildscript.configurations.maybeCreate(PitestPlugin.PITEST_CONFIGURATION_NAME);
		project.rootProject.buildscript.dependencies.add(PitestPlugin.PITEST_CONFIGURATION_NAME, project.files("${project.projectDir}/mutantLibs/pitest-export-plugin-0.1-SNAPSHOT.jar"));
		
		project.dependencies.add(getAndroidTestConfigurationName(), "de.schroepf:android-xml-run-listener:0.2.0");
	}
	
	@Override
	public void apply(Project project) {
		this.project = project;
		
		if(!project.plugins.hasPlugin(AndroidBasePlugin.class)) {
			throw new GradleException(String.format("Android plugin must be applied to project"));
		}
		
		addDependencies(project);
		
		project.getPluginManager().apply(PitestPlugin);
		
		extension = project.extensions.create(PLUGIN_EXTENSION, PimutdroidPluginExtension);
		extension.pitest = project.extensions[PitestPlugin.PITEST_CONFIGURATION_NAME];
		
		if(project.android.testOptions.resultsDir == null) {
			project.android.testOptions.resultsDir = "${project.reporting.baseDir.path}/mutation/test-results"
		}
		
		if(project.android.testOptions.reportDir == null) {
			project.android.testOptions.reportDir = "${project.reporting.baseDir.path}/mutation/test-reports"
		}
		
		project.afterEvaluate {
			
			pluginInternals = new PluginInternals(project, extension, project.getExtensions().findByType(BaseExtension.class));
			
			taskFactory = new TaskFactory(project.getTasks());
			pluginTasksCreator = new PluginTasksCreator(extension, pluginInternals, taskFactory, PLUGIN_TASK_GROUP);
			
			if(project.android.defaultConfig.testInstrumentationRunner != null) {
				runner = project.android.defaultConfig.testInstrumentationRunner
			}
			
			if(extension.packageDir == null) {
				extension.packageDir = project.android.defaultConfig.applicationId.replaceAll("\\.", "/")
			}
			
			if(extension.mutantsDir == null) {
				extension.mutantsDir = "${extension.pitest.reportDir}/debug"
			}
			
			if(extension.instrumentationTestOptions.targetMutants == null || extension.instrumentationTestOptions.targetMutants.empty) {
				extension.instrumentationTestOptions.targetMutants = [extension.packageDir]
			}
			
			if(extension.outputMutantCreation == null) {
				extension.outputMutantCreation = false;
			}
			
			if(extension.outputDir == null) {
				extension.outputDir = "${project.buildDir}/mutation/result";
			}
			
			if(extension.testResultDir == null) {
				extension.testResultDir = project.android.testOptions.resultsDir
			}
			
			if(extension.testReportDir == null) {
				extension.testReportDir = project.android.testOptions.reportDir
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

			adbExecuteable = project.android.getAdbExecutable();
			mutationFilesProvider = new MutationFilesProvider(project, extension);
			markerFileFactory = new MarkerFileFactory();
			mutantClassFileFactory = new MutantClassFileFactory(Paths.get(extension.mutantsDir));
			deviceLister = new DeviceLister(adbExecuteable);
			
			appClassFiles = new AppClassFiles(
				project, 
				extension.classFilesDir, 
				"${extension.appResultRootDir}/backup/classes"
			);
			androidTestResult = new AndroidTestResult(project, extension.testResultDir);
			appApk = new AppApk(project, "${project.buildDir}/outputs/apk/debug/", "${project.name}-debug.apk");
			appTestApk = new AppApk(project, "${project.buildDir}/outputs/apk/androidTest/debug/", "${project.name}-debug-androidTest.apk");
			
			deviceTestOptionsProvider = new DeviceTestOptionsProvider(
				extension.instrumentationTestOptions,
				"de.schroepf.androidxmlrunlistener.XmlRunListener"
			);
			
			pluginInternals.create();
			
			pluginTasksCreator.createTasks();
			
			createTasks();
			
			project.tasks.compileDebugSources.finalizedBy "mutateAfterCompileByMarkerFile"
		}

		project.gradle.taskGraph.whenReady { TaskExecutionGraph graph -> 
			LOGGER.info "Taskgraph ready"
			
			def buildMutantTasks = graph.getAllTasks().findAll { Task task ->
				task instanceof BuildMutantApkTask
			}

			if(buildMutantTasks.isEmpty()) {
				LOGGER.lifecycle "Disable replace class with mutant class task (no mutant build task found)";
				project.tasks.mutateAfterCompileByMarkerFile.enabled = false;
			}
		}
	}
	
	protected void createTasks() {
		def mutateAllAdbTask = createTask("mutateAllAdb", [type: MutationTestExecutionTask]) {}
		
		mutateAllAdbTask.adbExecuteable = adbExecuteable
		mutateAllAdbTask.deviceLister = deviceLister
		mutateAllAdbTask.mutationFilesProvider = mutationFilesProvider
		mutateAllAdbTask.deviceTestOptionsProvider = deviceTestOptionsProvider
		mutateAllAdbTask.testApk = appTestApk
		mutateAllAdbTask.appApk = appApk
		mutateAllAdbTask.targetMutants = extension.instrumentationTestOptions.targetMutants
		mutateAllAdbTask.appResultRootDir = extension.appResultRootDir
		mutateAllAdbTask.mutantResultRootDir = extension.mutantResultRootDir
		mutateAllAdbTask.appPackage = project.android.defaultConfig.applicationId
		mutateAllAdbTask.testPackage = project.android.defaultConfig.testApplicationId
		mutateAllAdbTask.runner = runner
		
		Task afterMutationTask = createTask("afterMutation", [type: AfterMutationTask]) {
			outputDir = extension.outputDir
			appResultDir = extension.appResultRootDir
			mutantsResultDir = extension.mutantResultRootDir
			
			doLast {
				println "Finished after mutation."
			}
		}
		afterMutationTask.mutationFilesProvider = mutationFilesProvider;
		
		Task mutateAllGenerateResultTask = createTask("mutateAllGenerateResult", [type: AfterMutationTask]) {
			dependsOn "mutateAllAdb"
			
			outputDir = extension.outputDir
			appResultDir = extension.appResultRootDir
			mutantsResultDir = extension.mutantResultRootDir
			
			doLast {
				println "Finished after mutation."
			}
		}
		mutateAllGenerateResultTask.mutationFilesProvider = mutationFilesProvider;
		
		createTask("mutantClassesList") {
			doLast {
				int numberMutants = 0;

				mutationFilesProvider.getMutantClassFiles().each { File file ->
					numberMutants++;

					LOGGER.quiet "Mutant $numberMutants" + "\t" + file.parentFile.getName() + "\t" +  file.getName()

				}
			}
		}
		
		createTask("mutantMarkerList") {
			doLast {
				int numberMutants = 0;

				mutationFilesProvider.getMutantMarkerFiles().each { File file ->
					numberMutants++;

					LOGGER.quiet "Mutant $numberMutants" + "\t" + file.parentFile.getName() + "\t" +  file.getName()
				}
			}
		}
		
		createTask("mutantXmlResultList") {
			doLast {
				int numberMutants = 0;

				mutationFilesProvider.getMutantResultTestFiles().each { File file ->
					numberMutants++;

					LOGGER.quiet "Mutant $numberMutants" + "\t" + file.parentFile.getName() + "\t" +  file.getName()
				}
			}
		}
		
		createTask("createMutants") {
			finalizedBy "pitestDebug"
			
			doLast {
				LOGGER.info "mutants ready."
			}
		}
		
		createTask("generateMutants") {
			dependsOn "preMutation"
			finalizedBy "pitestDebug"

			doLast {
				LOGGER.info "mutants ready."
			}
		}
		
		createTask("unitTestMutants") {
			finalizedBy "pitestDebug"

			doLast {
				LOGGER.info "mutants ready."
			}
		}
		
		createTask("mutateClasses") {
			dependsOn "pitestDebug"
			
			doLast {
				LOGGER.lifecycle "Class files mutated."	
			}
		}
		
		createTask("preMutation") {
			dependsOn = ["assembleDebug", "assembleAndroidTest"]
			
			doFirst {
				// Backup compiled debug class files
				appClassFiles.backup();
				
				// Copy unmutated apk
				appApk.copyTo(extension.appResultRootDir);
				
				// Copy test apk
				appTestApk.copyTo(extension.appResultRootDir);
			}
		}
		
		createTask("prepareMutation") {
			dependsOn "preMutation"
			dependsOn "mutateClasses"
			dependsOn "postMutation"
			dependsOn "prepareMutationGenerateTestResult"
			
			doLast {
				LOGGER.lifecycle "Preparations for mutation finished."
			}
		}
		
		PrepareMutantFilesTask prepareMutantFilesTask = project.getTasks().create("postMutation", PrepareMutantFilesTask.class, new Action<PrepareMutantFilesTask>() {
			public void execute(PrepareMutantFilesTask task) {
				task.setMutantFilesProvider(mutationFilesProvider);
				task.setMarkerFileFactory(markerFileFactory);
			}
		});
		prepareMutantFilesTask.setGroup(PLUGIN_TASK_GROUP);
		prepareMutantFilesTask.dependsOn("mutateClasses");
	
		createTask("prepareMutationGenerateTestResult") {
			dependsOn = ["assembleDebug", "assembleAndroidTest"]
			
			doLast {
				deviceLister.retrieveDevices();
				
				AppApk appApk = new AppApk(project, extension.appResultRootDir, "${project.name}-debug.apk");
				
				RunTestOnDevice rtod = new RunTestOnDevice(
					deviceLister.getFirstDevice(),
					adbExecuteable,
					deviceTestOptionsProvider.getOptions(),
					[appApk.getPath().toString()],
					appTestApk.getPath().toString(),
					project.android.defaultConfig.testApplicationId,
					project.android.defaultConfig.applicationId,
					runner
				);
				
				rtod.run();
				
				LOGGER.lifecycle "Connected tests finished. Storing expected results."
			}
		}
		
		createTask("afterMutantTask") {
			doLast {
				LOGGER.lifecycle("Restore original class files");
				appClassFiles.restore();
			}
		}
		
		
		ReplaceClassWithMutantTask mutateAfterCompileTask = project.getTasks().create("mutateAfterCompileByMarkerFile", ReplaceClassWithMutantTask.class, new Action<ReplaceClassWithMutantTask>() {
			public void execute(ReplaceClassWithMutantTask task) {
				task.setMutationFilesProvider(mutationFilesProvider);
				task.setMarkerFileFactory(markerFileFactory);
				task.setMutantClassFileFactory(mutantClassFileFactory);
				task.setCompileClassDirPath(Paths.get(extension.classFilesDir));
			}
		});
		mutateAfterCompileTask.setGroup(PLUGIN_TASK_GROUP);
		
		BuildMutantApkTask mutantApkTask = project.getTasks().create("buildMutantApk", BuildMutantApkTask.class, new Action<BuildMutantApkTask>() {
			public void execute(BuildMutantApkTask task) {
				task.setMutationFilesProvider(mutationFilesProvider);
				task.setMarkerFileFactory(markerFileFactory);
				task.setMutantResultRootDirPath(Paths.get(extension.mutantResultRootDir));
				task.setMutantApk(appApk);
				task.setMutantClassFilesRootDirPath(Paths.get(extension.mutantsDir));
			}
		});
		mutantApkTask.setGroup(PLUGIN_TASK_GROUP);
		mutantApkTask.dependsOn("assembleDebug");
		mutantApkTask.finalizedBy("afterMutantTask");
		
		BuildMutantsTask buildMutantsTask = project.getTasks().create("buildAllMutantApks", BuildMutantsTask.class, new Action<BuildMutantsTask>() {
			public void execute(BuildMutantsTask task) {
				task.setMutationFilesProvider(mutationFilesProvider);
			}
		});
		buildMutantsTask.setGroup(PLUGIN_TASK_GROUP);
		
		createTask("mutationWithClean", [type: GradleBuild]) {
			tasks = ["cleanMutation", "prepareMutation", "buildAllMutantApks", "mutateAllAdb", "afterMutation"]
		}
		
		createTask("mutation", [type: GradleBuild]) {
			tasks = ["prepareMutation", "buildAllMutantApks", "mutateAllAdb", "afterMutation"]
		}
	}

}
