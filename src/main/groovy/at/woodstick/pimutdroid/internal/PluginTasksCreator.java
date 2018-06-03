package at.woodstick.pimutdroid.internal;

import static at.woodstick.pimutdroid.internal.Utils.capitalize;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Delete;
import org.gradle.tooling.GradleConnectionException;

import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import at.woodstick.pimutdroid.configuration.BuildConfiguration;
import at.woodstick.pimutdroid.task.BuildMutantApkTask;
import at.woodstick.pimutdroid.task.BuildMutantsTask;
import at.woodstick.pimutdroid.task.CompiledClassesTask;
import at.woodstick.pimutdroid.task.MutationResultTask;
import at.woodstick.pimutdroid.task.MutationTestExecutionTask;
import at.woodstick.pimutdroid.task.PrepareMutantFilesTask;
import at.woodstick.pimutdroid.task.ReplaceClassWithMutantTask;

public class PluginTasksCreator {
	
	static final Logger LOGGER = Logging.getLogger(PluginTasksCreator.class);
	
	public static final String TASK_CLEAN_NAME 						= "cleanMutation";
	public static final String TASK_CLEAN_OUTPUT_NAME 				= "cleanMutationOutput";
	public static final String TASK_CLEAN_MUTANT_CLASSES_NAME		= "cleanMutantClasses";
	public static final String TASK_CLEAN_APPLICATION_FILES_NAME 	= "cleanMutantAppFiles";
	public static final String TASK_CLEAN_RESULT_FILES_NAME 		= "cleanMutantResultFiles";
	public static final String TASK_CLEAN_BUILD_LOG_FILES_NAME 		= "cleanMutantBuildLogFiles";
    
	public static final String TASK_BACKUP_APKS_NAME 				= "backupApks";
	public static final String TASK_BACKUP_COMPILED_CLASSES_NAME 	= "backupCompiledClasses";
	public static final String TASK_PREPARE_MUTATION_FILES_NAME 	= "prepareMutationFiles";
	
	public static final String TASK_MUTATE_AFTER_COMPILE_NAME 		= "injectMutantAfterCompileByMarkerFile";
	public static final String TASK_BUILD_ONLY_MUTANT_APK_NAME 		= "buildOnlyMutantApk";
	public static final String TASK_RESTORE_COMPILED_CLASSES_NAME 	= "restoreCompiledClasses";
	
	public static final String TASK_BUILD_MUTANT_APKS_NAME 			= "buildMutantApks";
	public static final String TASK_BUILD_MUTANT_APKS_ONLY_NAME 	= "buildMutantApksOnly";
	public static final String TASK_TEST_MUTANTS_NAME 				= "testMutants";
	public static final String TASK_TEST_MUTANTS_ONLY_NAME 			= "testMutantsOnly";
	public static final String TASK_GENERATE_MUTATION_RESULT_NAME 	= "generateMutationResult";
	
	public static final String TASK_GENERATE_MUTATION_RESULT_ONLY_NAME 		= "generateMutationResultOnly";
	public static final String TASK_TEST_MUTANTS_GENERATE_RESULT_ONLY_NAME 	= "testMutantsGenerateResultOnly";
	public static final String TASK_GENERATE_EPEXCTED_TEST_RESULT_NAME 		= "generateExpectedResult";
	public static final String TASK_PREPARE_APPLICATION_MUTATION_DATA_NAME 	= "prepareApplicationMutationData";
	
	public static final String TASK_MUTATE_CLASSES_NAME = MutateClassesTaskCreator.TASK_MUTATE_CLASSES_NAME;

	
	private final PimutdroidPluginExtension extension;
	private final PluginInternals pluginInternals;
	private final TaskFactory taskFactory;

	public PluginTasksCreator(PimutdroidPluginExtension extension, PluginInternals pluginInternals, TaskFactory taskFactory) {
		this.extension = extension;
		this.pluginInternals = pluginInternals;
		this.taskFactory = taskFactory;
	}

	public void createTasks() {
		createCleanTask();
		createCleanOutputTask();
		createCleanMutantClassesTask();
		createCleanApplicationFilesTask();
		createCleanResultFilesTask();
		createCleanBuildLogFilesTask();

		createBuildMutantApkTask();
		createRestoreCompiledClassesTask();
		
		createBackupApksTask();
		createBackupCompiledClassesTask();
		createGenerateExpectedTestResultTask();
		createPrepareApplicationMutationDataTask();
		createReplaceClassWithMutantTask();
		
		BuildConfiguration standardConfig = new BuildConfiguration("");
		standardConfig.setTargetMutants(extension.getInstrumentationTestOptions().getTargetMutants());
		createTasksForBuildConfiguration(standardConfig);

		postCreateTasks();
		
		pluginInternals.whenTaskGraphReady(this::configureTasks);
	}
	
	// ########################################################################
	
	public void createTasksForBuildConfiguration(BuildConfiguration config) {
		String configName = config.getName();
		String configUppercaseName = capitalize(configName);
		
		createPrepareMutationFilesTask(TASK_PREPARE_MUTATION_FILES_NAME + configUppercaseName, config)
			.dependsOn(TASK_MUTATE_CLASSES_NAME + configUppercaseName)
			.dependsOn(TASK_BACKUP_COMPILED_CLASSES_NAME);
		
		createBuildMutantApksTask(TASK_BUILD_MUTANT_APKS_NAME + configUppercaseName, config)
			.dependsOn(TASK_PREPARE_MUTATION_FILES_NAME + configUppercaseName);
		
		createTestMutantsTask(TASK_TEST_MUTANTS_NAME + configUppercaseName, config)
			.dependsOn(TASK_BUILD_MUTANT_APKS_NAME + configUppercaseName)
			.dependsOn(TASK_PREPARE_APPLICATION_MUTATION_DATA_NAME);
		
		createMutationResultTask(TASK_GENERATE_MUTATION_RESULT_NAME + configUppercaseName, config)
			.dependsOn(TASK_TEST_MUTANTS_NAME + configUppercaseName);
		
		createBuildMutantApksTask(TASK_BUILD_MUTANT_APKS_ONLY_NAME + configUppercaseName, config);
		
		createTestMutantsTask(TASK_TEST_MUTANTS_ONLY_NAME + configUppercaseName, config);
		
		createMutationResultTask(TASK_GENERATE_MUTATION_RESULT_ONLY_NAME + configUppercaseName, config);
		
		createMutationResultTask(TASK_TEST_MUTANTS_GENERATE_RESULT_ONLY_NAME + configUppercaseName, config)
			.dependsOn(TASK_TEST_MUTANTS_ONLY_NAME + configUppercaseName);
	}
	
	// ########################################################################
	
	protected void postCreateTasks() {
		taskFactory.named(getAndroidCompileSourcesTaskName()).finalizedBy(TASK_MUTATE_AFTER_COMPILE_NAME);
	}
	
	protected void configureTasks(TaskGraphAdaptor graph) {
		if(graph.hasNotTask(BuildMutantApkTask.class)) {
			LOGGER.debug("Disable replace class with mutant class task (no mutant build task found)");
			taskFactory.named(TASK_MUTATE_AFTER_COMPILE_NAME).setEnabled(false);
		}
	}

	// ########################################################################
	
	protected void createGenerateExpectedTestResultTask() {
		taskFactory.create(TASK_GENERATE_EPEXCTED_TEST_RESULT_NAME, (task) -> {
			task.doLast((ignore) -> {
				
				pluginInternals.getDeviceLister().retrieveDevices();
				
				if(pluginInternals.getDeviceLister().noDevicesConnected()) {
					throw new GradleConnectionException("No devices connected. Please connect a device.");
				}
				
				AppApk appApk = pluginInternals.getOriginalResultAppApk();
				
				AdbDeviceCommandBridge deviceBridge = new AdbDeviceCommandBridge(
					pluginInternals.getDeviceLister().getFirstDevice(),
					AdbCommandFactory.newFactory(pluginInternals.getAdbExecuteable())
				);
				
				RunTestOnDevice rtod = new RunTestOnDevice(
					deviceBridge,
					pluginInternals.getDeviceTestOptionsProvider().getOptions(),
					Arrays.asList(appApk.getPath().toString()),
					pluginInternals.getAppTestApk().getPath().toString(),
					extension.getTestApplicationId(),
					extension.getApplicationId(),
					extension.getInstrumentationTestOptions().getRunner(),
					extension.getExpectedTestResultFilename()
				);
				
				rtod.run();
				
				task.getLogger().info("Connected tests finished. Storing expected results.");
			});
			
			task.dependsOn(TASK_BACKUP_APKS_NAME);
			task.dependsOn(getAndroidAssembleAppTaskName(), getAndroidAssembleTestTaskName());
		});
	}
	
	protected void createPrepareApplicationMutationDataTask() {
		taskFactory.create(TASK_PREPARE_APPLICATION_MUTATION_DATA_NAME, (task) -> {
			task.dependsOn(TASK_BACKUP_APKS_NAME);
			task.dependsOn(TASK_GENERATE_EPEXCTED_TEST_RESULT_NAME);
		});
	}
	
	protected void createBackupApksTask() {
		taskFactory.create(TASK_BACKUP_APKS_NAME, (task) -> {
			task.doLast((ignore) -> {
				// Copy unmutated apk
				pluginInternals.getAppApk().copyTo(extension.getAppResultRootDir());
				
				// Copy test apk
				pluginInternals.getAppTestApk().copyTo(extension.getAppResultRootDir());
			});
			
			task.dependsOn(TASK_BACKUP_COMPILED_CLASSES_NAME);
			task.dependsOn(getAndroidAssembleAppTaskName(), getAndroidAssembleTestTaskName());
		});
	}
	
	protected void createReplaceClassWithMutantTask() {
		taskFactory.create(TASK_MUTATE_AFTER_COMPILE_NAME, ReplaceClassWithMutantTask.class, (task) -> {
			task.setCompileClassDirPath(Paths.get(extension.getClassFilesDir()));
		});
	}
	
	protected void createBuildMutantApkTask() {
		taskFactory.create(TASK_BUILD_ONLY_MUTANT_APK_NAME, BuildMutantApkTask.class, (task) -> {
			task.dependsOn(getAndroidAssembleAppTaskName());
			task.finalizedBy(TASK_RESTORE_COMPILED_CLASSES_NAME);
		});
	}

	protected void createBackupCompiledClassesTask() {
		taskFactory.create(TASK_BACKUP_COMPILED_CLASSES_NAME, CompiledClassesTask.class, (task) -> {
			task.backup();
			task.dependsOn(getAndroidAssembleAppTaskName());
		});
	}
	
	protected void createRestoreCompiledClassesTask() {
		taskFactory.create(TASK_RESTORE_COMPILED_CLASSES_NAME, CompiledClassesTask.class, (task) -> {
			task.restore();
		});
	}

	// ########################################################################
	
	protected Task createBuildMutantApksTask(final String taskName, BuildConfiguration config) {
		return createBuildMutantsTask(taskName, config.getTargetMutants());
	}
	
	protected Task createBuildMutantsTask(final String taskName, Set<String> targetedMutatnts) {
		return taskFactory.create(taskName, BuildMutantsTask.class, (task) -> {
			task.setTargetedMutants(targetedMutatnts);
		});
	}
	
	// ########################################################################
	
	protected Task createPrepareMutationFilesTask(final String taskName, BuildConfiguration config) {
		return createPrepareMutantFilesTask(taskName, config.getTargetMutants());
	}
	
	protected Task createPrepareMutantFilesTask(final String taskName, Set<String> targetedMutants) {
		return taskFactory.create(taskName, PrepareMutantFilesTask.class, (task) -> {
			task.setTargetedMutants(targetedMutants);
		});
	}

	// ########################################################################
	
	protected Task createTestMutantsTask(final String taskName, final BuildConfiguration config) {
		return createMutateAllTask(taskName, config.getTargetMutants());
	}
	
	protected Task createMutateAllTask(final String taskName, final Set<String> targetedMutants) { 
		return taskFactory.create(taskName, MutationTestExecutionTask.class, (MutationTestExecutionTask task) -> {
			task.setDeviceLister(pluginInternals.getDeviceLister());
			task.setAdbExecuteable(pluginInternals.getAdbExecuteable());
			task.setDeviceLister(pluginInternals.getDeviceLister());
			task.setMutationFilesProvider(pluginInternals.getMutationFilesProvider());
			task.setDeviceTestOptionsProvider(pluginInternals.getDeviceTestOptionsProvider());
			task.setTestApk(pluginInternals.getAppTestApk());
			task.setAppApk(pluginInternals.getAppApk());
			
			task.setTargetMutants(targetedMutants);
			task.setAppResultRootDir(extension.getAppResultRootDir());
			task.setMutantResultRootDir(extension.getMutantResultRootDir());
			task.setAppPackage(extension.getApplicationId());
			task.setTestPackage(extension.getTestApplicationId());
			task.setRunner(extension.getInstrumentationTestOptions().getRunner());
		});
	}
	
	// ########################################################################
	
	protected Task createMutationResultTask(final String taskName, final BuildConfiguration config) {
		return createMutationResultTask(taskName, config.getTargetMutants());
	}
	
	protected Task createMutationResultTask(final String taskName, Set<String> targetedMutants) {
		return taskFactory.create(taskName, MutationResultTask.class, (MutationResultTask task) -> {
			task.setOutputDir(extension.getMutantReportRootDir());
			task.setAppResultDir(extension.getAppResultRootDir());
			task.setMutantsResultDir(extension.getMutantResultRootDir());
			task.setTargetedMutants(targetedMutants);
		});
	}
	
	// ########################################################################
	
	protected void createCleanTask() {
		taskFactory.create(TASK_CLEAN_NAME, Delete.class, (task) -> {
			task.dependsOn(TASK_CLEAN_MUTANT_CLASSES_NAME);
			task.dependsOn(TASK_CLEAN_OUTPUT_NAME);
			task.dependsOn(TASK_CLEAN_APPLICATION_FILES_NAME);
		});
	}
	
	protected void createCleanOutputTask() {
		taskFactory.create(TASK_CLEAN_OUTPUT_NAME, Delete.class, (task) -> {
			task.delete(extension.getOutputDir());
		});
	}
	
	protected void createCleanMutantClassesTask() {
		taskFactory.create(TASK_CLEAN_MUTANT_CLASSES_NAME, Delete.class, (task) -> {
			task.delete(extension.getMutantClassesDir());
		});
	}
	
	protected void createCleanApplicationFilesTask() {
		taskFactory.create(TASK_CLEAN_APPLICATION_FILES_NAME, Delete.class, (task) -> {
			task.delete(extension.getAppResultRootDir());
		});
	}
	
	protected void createCleanResultFilesTask() {
		taskFactory.create(TASK_CLEAN_RESULT_FILES_NAME, Delete.class, (task) -> {
			task.delete(extension.getMutantReportRootDir());
		});
	}
	
	protected void createCleanBuildLogFilesTask() {
		taskFactory.create(TASK_CLEAN_BUILD_LOG_FILES_NAME, Delete.class, (task) -> {
			task.delete(extension.getMutantBuildLogsDir());
		});
	}
	
	// ########################################################################

	private String getFlavorBuildTypeTaskPart() {
		return capitalize(extension.getProductFlavor()) + capitalize(extension.getTestBuildType());
	}
	
	protected String getAndroidAssembleAppTaskName() {
		return "assemble" + getFlavorBuildTypeTaskPart();
	}
	
	protected String getAndroidAssembleTestTaskName() {
		return "assemble" + getFlavorBuildTypeTaskPart() + "AndroidTest";
	}

	protected String getAndroidCompileSourcesTaskName() {
		return "compile" + getFlavorBuildTypeTaskPart() + "Sources";
	}
}
