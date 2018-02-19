package at.woodstick.pimutdroid.internal;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.GradleBuild;
import org.gradle.tooling.GradleConnectionException;

import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import at.woodstick.pimutdroid.configuration.BuildConfiguration;
import at.woodstick.pimutdroid.task.AfterMutationTask;
import at.woodstick.pimutdroid.task.AvailableDevicesTask;
import at.woodstick.pimutdroid.task.BuildMutantApkTask;
import at.woodstick.pimutdroid.task.BuildMutantsTask;
import at.woodstick.pimutdroid.task.CompiledClassesTask;
import at.woodstick.pimutdroid.task.InfoTask;
import at.woodstick.pimutdroid.task.MutateClassesTask;
import at.woodstick.pimutdroid.task.MutationTestExecutionTask;
import at.woodstick.pimutdroid.task.PrepareMutantFilesTask;
import at.woodstick.pimutdroid.task.ReplaceClassWithMutantTask;
import info.solidsoft.gradle.pitest.PitestTask;

public class PluginTasksCreator {

	public static final String TASK_LIST_MUTANT_XML_RESULT_NAME = "mutantXmlResultList";
	public static final String TASK_LIST_MUTANT_MARKER_NAME = "mutantMarkerList";
	public static final String TASK_LIST_MUTANT_CLASSES_NAME = "mutantClassesList";
	public static final String TASK_PREPARE_MUTATION_GENERATE_TEST_RESULT_NAME = "prepareMutationGenerateTestResult";
	public static final String TASK_PREPARE_MUTATION_NAME = "prepareMutation";
	public static final String TASK_MUTATION_WITH_CLEAN_NAME = "mutationWithClean";
	public static final String TASK_MUTATION_NAME = "mutation";
	public static final String TASK_BUILD_ALL_MUTANT_APKS_NAME = "buildAllMutantApks";
	public static final String TASK_MUTATE_AFTER_COMPILE_NAME = "mutateAfterCompileByMarkerFile";
	public static final String TASK_BUILD_ONLY_MUTANT_APK_NAME = "buildOnlyMutantApk";
	public static final String TASK_BEFORE_MUTATION_NAME = "beforeMutationTask";
	public static final String TASK_AFTER_MUTANT_NAME = "afterMutantTask";
	public static final String TASK_POST_MUTATION_NAME = "postMutation";
	public static final String TASK_CLEAN_NAME = "cleanMutation";
	public static final String TASK_CLEAN_OUTPUT_NAME = "cleanMutationOuput";
	public static final String TASK_CLEAN_MUTANT_CLASSES_NAME = "cleanMutantClasses";
	public static final String TASK_AVAILABLE_DEVICES_NAME = "availableDevices";
	public static final String TASK_PLUGIN_INFO_NAME = "pimutInfo";
	public static final String TASK_TEST_ALL_MUTANTS_NAME = "mutateAllAdb"; // "testAllMutants";
	public static final String TASK_GENERATE_MUTATION_RESULT_NAME = "afterMutation"; // "generateMutationResult";
	public static final String TASK_TEST_ALL_MUTANTS_GENERATE_MUTATION_RESULT_NAME = "mutateAllGenerateResult"; // "testAllMutantsGenerateMutationResult";
	public static final String TASK_MUTATE_CLASSES_NAME = "mutateClasses";
	public static final String TASK_PRE_MUTATION_NAME = "preMutation";
	
	public static final String TASK_ALL_MUTANT_APKS_ONLY_NAME = "buildAllMutants";
	
	// Pitest and android plugin tasks
	public static final String TASK_PITEST_NAME = "pitestDebug";
	public static final String TASK_ANDROID_ASSEMBLE_APP_NAME = "assembleDebug";
	public static final String TASK_ANDROID_ASSEMBLE_TEST_NAME = "assembleAndroidTest";
	public static final String TASK_ANDROID_COMPILE_SOURCES_NAME = "compileDebugSources";

	static final Logger LOGGER = Logging.getLogger(PluginTasksCreator.class);
	
	private final PimutdroidPluginExtension extension;
	private final PluginInternals pluginInternals;
	private final TaskFactory taskFactory;
	private final String defaultTaskGroup;

	public PluginTasksCreator(PimutdroidPluginExtension extension, PluginInternals pluginInternals, TaskFactory taskFactory, String defaultTaskGroup) {
		this.extension = extension;
		this.pluginInternals = pluginInternals;
		this.taskFactory = taskFactory;
		this.defaultTaskGroup = defaultTaskGroup;
	}

	public void createTasks() {
		createPimutInfoTask();
		
		createCleanTask();
		createCleanOutputTask();
		createCleanMutantClassesTask();

		createAvailableDevicesTask();
		
		createMutateAllTask();
		createAfterMutationTask();
		createMutateAllAfterMutationTask();
		createPreMutationTask();
		createPrepareMutantFilesTask();
		createBeforeMutationTask();
		createAfterMutantTask();
		createBuildMutantApkTask();
		createBuildMutantsTask();
		createReplaceClassWithMutantTask();
		createMutateClassesTask();
		createPrepareMutationTask();
		createMutationTask();
		createMutationWithCleanTask();
		
		createPrepareMutationGenerateTestResultTask();
		
		createListMutantClasses();
		createListMutantMarker();
		createListMutantXmlResults();
		
		postCreateTasks();
		
		pluginInternals.whenTaskGraphReady(this::configureTasks);
	}
	
	// ########################################################################
	
	public void createTasksForBuildConfiguration(BuildConfiguration config) {
		String configName = config.getName();
		String configUppercaseName = configName.substring(0, 1).toUpperCase() + configName.substring(1);
		
		config.setMaxMutationsPerClass(getMaxMutationsPerClassForConfig(config));
		
		createAfterMutationTask(TASK_GENERATE_MUTATION_RESULT_NAME + configUppercaseName, config);
		createBuildMutantsTask(TASK_BUILD_ALL_MUTANT_APKS_NAME + configUppercaseName, config);
		createMutateClassesTask(TASK_MUTATE_CLASSES_NAME + configUppercaseName, config);
		
		createBuildMutantApksTask(TASK_ALL_MUTANT_APKS_ONLY_NAME + configUppercaseName, configUppercaseName);
	}
	
	// ########################################################################
	
	protected void postCreateTasks() {
		taskFactory.named(TASK_ANDROID_COMPILE_SOURCES_NAME).finalizedBy(TASK_MUTATE_AFTER_COMPILE_NAME);
	}
	
	protected void configureTasks(TaskGraphAdaptor graph) {
		if(graph.hasNotTask(BuildMutantApkTask.class)) {
			LOGGER.lifecycle("Disable replace class with mutant class task (no mutant build task found)");
			taskFactory.named(TASK_MUTATE_AFTER_COMPILE_NAME).setEnabled(false);
		}
		
		if(graph.hasTask(PitestTask.class) && graph.hasTask(MutateClassesTask.class)) {
			
			Optional<PitestTask> pitestTask = Optional.empty();
			Optional<MutateClassesTask> mutateClassesTask = Optional.empty();
			
			for(Task task : graph.getAllTasks()) {
				if(task instanceof PitestTask) {
					pitestTask = Optional.of((PitestTask)task);
				}
				
				if(task instanceof MutateClassesTask) {
					mutateClassesTask = Optional.of((MutateClassesTask)task);
				}
			}
			
			if(pitestTask.isPresent() && mutateClassesTask.isPresent()) {
				
				PitestTask pitestTaskInstance = pitestTask.get();
				MutateClassesTask mutateClassesTaskInstance = mutateClassesTask.get();
				
				pitestTaskInstance.setTargetClasses(mutateClassesTaskInstance.getTargetedMutants());
				pitestTaskInstance.setMaxMutationsPerClass(mutateClassesTaskInstance.getMaxMutationsPerClass());
			} else {
				LOGGER.error("Unable to configure pitest task from mutate classes task.");
			}
		}
	}
	
	// ########################################################################
	
	protected void createListMutantClasses() {
		createDefaultGroupTask(TASK_LIST_MUTANT_CLASSES_NAME, (task) -> {
			task.doLast((ignore) -> {
				int numberMutants = 0;
				for(File file : pluginInternals.getMutationFilesProvider().getMutantClassFiles()) {
					numberMutants++;
					pluginInternals.getProjectLogger().quiet("Mutant {}\t{}\t{}", numberMutants, file.getParentFile().getName(), file.getName());
				}
			});
		});
	}
	
	protected void createListMutantMarker() {
		createDefaultGroupTask(TASK_LIST_MUTANT_MARKER_NAME, (task) -> {
			task.doLast((ignore) -> {
				int numberMutants = 0;
				for(File file : pluginInternals.getMutationFilesProvider().getMutantMarkerFiles()) {
					numberMutants++;
					pluginInternals.getProjectLogger().quiet("Mutant {}\t{}\t{}", numberMutants, file.getParentFile().getName(), file.getName());
				}
			});
		});
	}
	
	protected void createListMutantXmlResults() {
		createDefaultGroupTask(TASK_LIST_MUTANT_XML_RESULT_NAME, (task) -> {
			task.doLast((ignore) -> {
				int numberMutants = 0;
				for(File file : pluginInternals.getMutationFilesProvider().getMutantResultTestFiles()) {
					numberMutants++;
					pluginInternals.getProjectLogger().quiet("Mutant {}\t{}\t{}", numberMutants, file.getParentFile().getName(), file.getName());
				}
			});
		});
	}
	
	// ########################################################################
	
	protected void createBuildMutantApksTask(final String taskName, final String configName) {
		createDefaultGroupTask(taskName, GradleBuild.class, (task) -> {
			task.setTasks(
				Arrays.asList(
					TASK_BEFORE_MUTATION_NAME,
					TASK_MUTATE_CLASSES_NAME + configName, 
					TASK_POST_MUTATION_NAME, 
					TASK_BUILD_ALL_MUTANT_APKS_NAME + configName
				)
			);
		});
	}
	
	// ########################################################################
	
	protected void createPrepareMutationGenerateTestResultTask() {
		createDefaultGroupTask(TASK_PREPARE_MUTATION_GENERATE_TEST_RESULT_NAME, (task) -> {
			task.doLast((ignore) -> {
				
				pluginInternals.getDeviceLister().retrieveDevices();
				
				if(pluginInternals.getDeviceLister().noDevicesConnected()) {
					throw new GradleConnectionException("No devices connected. Please connect a device.");
				}
				
				AppApk appApk = pluginInternals.getOriginalResultAppApk();
				
				RunTestOnDevice rtod = new RunTestOnDevice(
					pluginInternals.getDeviceLister().getFirstDevice(),
					pluginInternals.getAdbExecuteable(),
					pluginInternals.getDeviceTestOptionsProvider().getOptions(),
					Arrays.asList(appApk.getPath().toString()),
					pluginInternals.getAppTestApk().getPath().toString(),
					extension.getTestApplicationId(),
					extension.getApplicationId(),
					extension.getInstrumentationTestOptions().getRunner()
				);
				
				rtod.run();
				
				pluginInternals.getProjectLogger().lifecycle("Connected tests finished. Storing expected results.");
				
			});
		});
	}
	
	protected void createPrepareMutationTask() {
		createDefaultGroupTask(TASK_PREPARE_MUTATION_NAME, GradleBuild.class, (task) -> {
			task.setTasks(Arrays.asList(TASK_PRE_MUTATION_NAME, TASK_MUTATE_CLASSES_NAME, TASK_POST_MUTATION_NAME, TASK_PREPARE_MUTATION_GENERATE_TEST_RESULT_NAME));
		});
	}
	
	protected void createMutationTask() {
		createDefaultGroupTask(TASK_MUTATION_NAME, GradleBuild.class, (task) -> {
			task.setTasks(Arrays.asList(TASK_PREPARE_MUTATION_NAME, TASK_BUILD_ALL_MUTANT_APKS_NAME, TASK_TEST_ALL_MUTANTS_NAME, TASK_GENERATE_MUTATION_RESULT_NAME));
		});
	}
	
	protected void createMutationWithCleanTask() {
		createDefaultGroupTask(TASK_MUTATION_WITH_CLEAN_NAME, GradleBuild.class, (task) -> {
			task.setTasks(Arrays.asList(TASK_CLEAN_NAME, TASK_PREPARE_MUTATION_NAME, TASK_BUILD_ALL_MUTANT_APKS_NAME, TASK_TEST_ALL_MUTANTS_NAME, TASK_GENERATE_MUTATION_RESULT_NAME));
		});
	}
	
	protected void createMutateAllAfterMutationTask() {
		createDefaultGroupTask(TASK_TEST_ALL_MUTANTS_GENERATE_MUTATION_RESULT_NAME, GradleBuild.class, (task) -> {
			task.setTasks(Arrays.asList(TASK_TEST_ALL_MUTANTS_NAME, TASK_GENERATE_MUTATION_RESULT_NAME));
		});
	}
	
	protected void createBuildMutantsTask() {
		createBuildMutantsTask(TASK_BUILD_ALL_MUTANT_APKS_NAME);
	}
	
	protected void createBuildMutantsTask(final String taskName) {
		createDefaultGroupTask(taskName, BuildMutantsTask.class, (task) -> {
			task.setTargetedMutants(extension.getInstrumentationTestOptions().getTargetMutants());
		});
	}
	
	protected void createBuildMutantsTask(final String taskName, BuildConfiguration config) {
		createDefaultGroupTask(taskName, BuildMutantsTask.class, (task) -> {
			task.setTargetedMutants(config.getTargetMutants());
		});
	}
	
	protected void createReplaceClassWithMutantTask() {
		createDefaultGroupTask(TASK_MUTATE_AFTER_COMPILE_NAME, ReplaceClassWithMutantTask.class, (task) -> {
			task.setCompileClassDirPath(Paths.get(extension.getClassFilesDir()));
		});
	}
	
	protected void createBuildMutantApkTask() {
		createDefaultGroupTask(TASK_BUILD_ONLY_MUTANT_APK_NAME, BuildMutantApkTask.class, (task) -> {
			task.setMutantResultRootDirPath(Paths.get(extension.getMutantResultRootDir()));
			task.setMutantClassFilesRootDirPath(Paths.get(extension.getMutantsDir()));
			
			task.dependsOn(TASK_ANDROID_ASSEMBLE_APP_NAME);
			task.finalizedBy(TASK_AFTER_MUTANT_NAME);
		});
	}
	
	protected void createAfterMutantTask() {
		createDefaultGroupTask(TASK_AFTER_MUTANT_NAME, CompiledClassesTask.class, (task) -> {
			task.setBackup(false);
		});
	}
	
	protected void createBeforeMutationTask() {
		createDefaultGroupTask(TASK_BEFORE_MUTATION_NAME, CompiledClassesTask.class, (task) -> {
			task.setBackup(true);
			
			task.dependsOn(TASK_ANDROID_ASSEMBLE_APP_NAME);
		});
	}
	
	protected void createPrepareMutantFilesTask() {
		createDefaultGroupTask(TASK_POST_MUTATION_NAME, PrepareMutantFilesTask.class, (task) -> {
			task.setMutantFilesProvider(pluginInternals.getMutationFilesProvider());
			task.setMarkerFileFactory(pluginInternals.getMarkerFileFactory());
			
//			task.dependsOn(TASK_MUTATE_CLASSES_NAME);
		});
	}
	
	protected void createPreMutationTask() {
		createDefaultGroupTask(TASK_PRE_MUTATION_NAME, (task) -> {
			task.doLast((ignore) -> {
				// Backup compiled debug class files
				pluginInternals.getAppClassFiles().backup();
				
				// Copy unmutated apk
				pluginInternals.getAppApk().copyTo(extension.getAppResultRootDir());
				
				// Copy test apk
				pluginInternals.getAppTestApk().copyTo(extension.getAppResultRootDir());
			});
			
			task.dependsOn(TASK_ANDROID_ASSEMBLE_APP_NAME, TASK_ANDROID_ASSEMBLE_TEST_NAME);
		});
	}
	
	protected void createMutateAllTask() {
		createDefaultGroupTask(TASK_TEST_ALL_MUTANTS_NAME, MutationTestExecutionTask.class, (MutationTestExecutionTask task) -> {
			task.setDeviceLister(pluginInternals.getDeviceLister());
			task.setAdbExecuteable(pluginInternals.getAdbExecuteable());
			task.setDeviceLister(pluginInternals.getDeviceLister());
			task.setMutationFilesProvider(pluginInternals.getMutationFilesProvider());
			task.setDeviceTestOptionsProvider(pluginInternals.getDeviceTestOptionsProvider());
			task.setTestApk(pluginInternals.getAppTestApk());
			task.setAppApk(pluginInternals.getAppApk());
			
			task.setTargetMutants(extension.getInstrumentationTestOptions().getTargetMutants());
			task.setAppResultRootDir(extension.getAppResultRootDir());
			task.setMutantResultRootDir(extension.getMutantResultRootDir());
			task.setAppPackage(extension.getApplicationId());
			task.setTestPackage(extension.getTestApplicationId());
			task.setRunner(extension.getInstrumentationTestOptions().getRunner());
		});
	}
	
	protected void createAfterMutationTask() {
		createAfterMutationTask(TASK_GENERATE_MUTATION_RESULT_NAME);
	}
	
	protected void createAfterMutationTask(final String taskName) {
		createDefaultGroupTask(taskName, AfterMutationTask.class, (AfterMutationTask task) -> {
			task.setOutputDir(extension.getMutantReportRootDir());
			task.setAppResultDir(extension.getAppResultRootDir());
			task.setMutantsResultDir(extension.getMutantResultRootDir());
			task.setTargetedMutants(extension.getInstrumentationTestOptions().getTargetMutants());
		});
	}
	
	protected void createAfterMutationTask(final String taskName, final BuildConfiguration config) {
		createDefaultGroupTask(taskName, AfterMutationTask.class, (AfterMutationTask task) -> {
			task.setOutputDir(extension.getMutantReportRootDir());
			task.setAppResultDir(extension.getAppResultRootDir());
			task.setMutantsResultDir(extension.getMutantResultRootDir());
			task.setTargetedMutants(config.getTargetMutants());
		});
	}
	
	protected void createMutateClassesTask() {
		createMutateClassesTask(TASK_MUTATE_CLASSES_NAME);
	}
	
	protected void createMutateClassesTask(final String taskName) {
		createDefaultGroupTask(taskName, MutateClassesTask.class, (task) -> {
			task.dependsOn(TASK_PITEST_NAME);
			task.setTargetedMutants(extension.getInstrumentationTestOptions().getTargetMutants());
			task.setMaxMutationsPerClass(pluginInternals.getMaxMutationsPerClass());
		});
	}
	
	protected void createMutateClassesTask(final String taskName, BuildConfiguration config) {
		createDefaultGroupTask(taskName, MutateClassesTask.class, (task) -> {
			task.dependsOn(TASK_PITEST_NAME);
			task.setTargetedMutants(config.getTargetMutants());
			task.setMaxMutationsPerClass(config.getMaxMutationsPerClass());
		});
	}
	
	protected void createCleanTask() {
		createDefaultGroupTask(TASK_CLEAN_NAME, Delete.class, (task) -> {
			task.delete(extension.getOutputDir(), extension.getMutantsDir());
		});
	}
	
	protected void createCleanOutputTask() {
		createDefaultGroupTask(TASK_CLEAN_OUTPUT_NAME, Delete.class, (task) -> {
			task.delete(extension.getOutputDir());
		});
	}
	
	protected void createCleanMutantClassesTask() {
		createDefaultGroupTask(TASK_CLEAN_MUTANT_CLASSES_NAME, Delete.class, (task) -> {
			task.delete(extension.getMutantsDir());
		});
	}
	
	protected void createAvailableDevicesTask() {
		createDefaultGroupTask(TASK_AVAILABLE_DEVICES_NAME, AvailableDevicesTask.class);
	}
	
	protected void createPimutInfoTask() {
		createDefaultGroupTask(TASK_PLUGIN_INFO_NAME, InfoTask.class);
	}
	
	// ########################################################################
	
	protected Integer getMaxMutationsPerClassForConfig(BuildConfiguration config) {
		return config.getMaxMutationsPerClass() != null ? config.getMaxMutationsPerClass() : pluginInternals.getMaxMutationsPerClass();
	}
	
	// ########################################################################
	
	protected <T extends Task> T createDefaultGroupTask(final String name, final Class<T> taskClass) {
		return taskFactory.create(name, taskClass, defaultGroupAction());
	}

	protected <T extends Task> T createDefaultGroupTask(final String name, final Class<T> taskClass, final Action<T> configAction) {
		return taskFactory.create(name, taskClass, defaultGroupAction(configAction));
	}
	
	protected Task createDefaultGroupTask(final String name) {
		return taskFactory.create(name, defaultGroupAction());
	}
	
	protected Task createDefaultGroupTask(final String name, final Action<Task> configAction) {
		return taskFactory.create(name, defaultGroupAction(configAction));
	}
	
	protected <T extends Task> Action<T> defaultGroupAction(final Action<T> configAction) {
		return new DefaultGroupAction<>(configAction, defaultTaskGroup);
	}
	
	protected <T extends Task> Action<T> defaultGroupAction() {
		return new DefaultGroupAction<>((task) -> {}, defaultTaskGroup);
	}
}
