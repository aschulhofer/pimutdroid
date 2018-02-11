package at.woodstick.pimutdroid.internal;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.GradleBuild;

import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import at.woodstick.pimutdroid.configuration.BuildConfiguration;
import at.woodstick.pimutdroid.task.AfterMutationTask;
import at.woodstick.pimutdroid.task.AvailableDevicesTask;
import at.woodstick.pimutdroid.task.BuildMutantApkTask;
import at.woodstick.pimutdroid.task.BuildMutantsTask;
import at.woodstick.pimutdroid.task.InfoTask;
import at.woodstick.pimutdroid.task.MutationTestExecutionTask;
import at.woodstick.pimutdroid.task.PrepareMutantFilesTask;
import at.woodstick.pimutdroid.task.ReplaceClassWithMutantTask;

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
	public static final String TASK_BUILD_MUTANT_APK_NAME = "buildMutantApk";
	public static final String TASK_AFTER_MUTANT_NAME = "afterMutantTask";
	public static final String TASK_POST_MUTATION_NAME = "postMutation";
	public static final String TASK_CLEAN_NAME = "cleanMutation";
	public static final String TASK_AVAILABLE_DEVICES_NAME = "availableDevices";
	public static final String TASK_PLUGIN_INFO_NAME = "pimutInfo";
	public static final String TASK_TEST_ALL_MUTANTS_NAME = "mutateAllAdb"; // "testAllMutants";
	public static final String TASK_GENERATE_MUTATION_RESULT_NAME = "afterMutation"; // "generateMutationResult";
	public static final String TASK_TEST_ALL_MUTANTS_GENERATE_MUTATION_RESULT_NAME = "mutateAllGenerateResult"; // "testAllMutantsGenerateMutationResult";
	public static final String TASK_MUTATE_CLASSES_NAME = "mutateClasses";
	public static final String TASK_PRE_MUTATION_NAME = "preMutation";
	
	public static final String TASK_PITEST_NAME = "pitestDebug";
	public static final String TASK_ANDROID_ASSEMBLE_APP_NAME = "assembleDebug";
	public static final String TASK_ANDROID_ASSEMBLE_TEST_NAME = "assembleAndroidTest";
	public static final String TASK_ANDROID_COMPILE_SOURCES_NAME = "compileDebugSources";

	static final Logger LOGGER = Logging.getLogger(PluginTasksCreator.class);
	
	private final PimutdroidPluginExtension extension;
	private final PluginInternals pluginInternals;
	private final TaskFactory taskFactory;
	private final String defaultGroup;

	public PluginTasksCreator(PimutdroidPluginExtension extension, PluginInternals pluginInternals, TaskFactory taskFactory, String defaultGroup) {
		this.extension = extension;
		this.pluginInternals = pluginInternals;
		this.taskFactory = taskFactory;
		this.defaultGroup = defaultGroup;
	}

	public void createTasks() {
		createPimutInfoTask();
		createCleanTask();
		createAvailableDevicesTask();
		
		createMutateAllTask();
		createAfterMutationTask();
		createMutateAllAfterMutationTask();
		createPreMutationTask();
		createPrepareMutantFilesTask();
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
		
		createAfterMutationTask(TASK_GENERATE_MUTATION_RESULT_NAME + configUppercaseName, config);
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
	
	protected void createPrepareMutationGenerateTestResultTask() {
		createDefaultGroupTask(TASK_PREPARE_MUTATION_GENERATE_TEST_RESULT_NAME, (task) -> {
			task.doLast((ignore) -> {
				
				pluginInternals.getDeviceLister().retrieveDevices();
				
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
	
	protected void createBuildMutantsTask() {
		createDefaultGroupTask(TASK_BUILD_ALL_MUTANT_APKS_NAME, BuildMutantsTask.class, (task) -> {
			task.setMutationFilesProvider(pluginInternals.getMutationFilesProvider());
		});
	}
	
	protected void createReplaceClassWithMutantTask() {
		createDefaultGroupTask(TASK_MUTATE_AFTER_COMPILE_NAME, ReplaceClassWithMutantTask.class, (task) -> {
			task.setCompileClassDirPath(Paths.get(extension.getClassFilesDir()));
		});
	}
	
	protected void createBuildMutantApkTask() {
		createDefaultGroupTask(TASK_BUILD_MUTANT_APK_NAME, BuildMutantApkTask.class, (task) -> {
			task.setMutantResultRootDirPath(Paths.get(extension.getMutantResultRootDir()));
			task.setMutantClassFilesRootDirPath(Paths.get(extension.getMutantsDir()));
			
			task.dependsOn(TASK_ANDROID_ASSEMBLE_APP_NAME);
			task.finalizedBy(TASK_AFTER_MUTANT_NAME);
		});
	}
	
	protected void createAfterMutantTask() {
		createDefaultGroupTask(TASK_AFTER_MUTANT_NAME, (task) -> {
			task.doLast((ignore) -> {
				pluginInternals.getProjectLogger().lifecycle("Restore original class files");
				pluginInternals.getAppClassFiles().restore();
			});
		});
	}
	
	protected void createPrepareMutantFilesTask() {
		createDefaultGroupTask(TASK_POST_MUTATION_NAME, PrepareMutantFilesTask.class, (task) -> {
			task.setMutantFilesProvider(pluginInternals.getMutationFilesProvider());
			task.setMarkerFileFactory(pluginInternals.getMarkerFileFactory());
			
			task.dependsOn(TASK_MUTATE_CLASSES_NAME);
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
			task.setOutputDir(extension.getOutputDir());
			task.setAppResultDir(extension.getAppResultRootDir());
			task.setMutantsResultDir(extension.getMutantResultRootDir());
			task.setTargetedMutants(extension.getInstrumentationTestOptions().getTargetMutants());
		});
	}
	
	protected void createAfterMutationTask(final String taskName, final BuildConfiguration config) {
		createDefaultGroupTask(taskName, AfterMutationTask.class, (AfterMutationTask task) -> {
			task.setOutputDir(extension.getOutputDir());
			task.setAppResultDir(extension.getAppResultRootDir());
			task.setMutantsResultDir(extension.getMutantResultRootDir());
			task.setTargetedMutants(config.getTargetMutants());
		});
	}
	
	protected void createMutateAllAfterMutationTask() {
		createDefaultGroupTask(TASK_TEST_ALL_MUTANTS_GENERATE_MUTATION_RESULT_NAME, GradleBuild.class, (task) -> {
			task.setTasks(Arrays.asList(TASK_TEST_ALL_MUTANTS_NAME, TASK_GENERATE_MUTATION_RESULT_NAME));
		});
	}
	
	protected void createMutateClassesTask() {
		createDefaultGroupTask(TASK_MUTATE_CLASSES_NAME, (task) -> {
			task.dependsOn(TASK_PITEST_NAME);
			task.doLast((ignore) -> {
				pluginInternals.getProjectLogger().lifecycle("Class files mutated.");
			});
		});
	}
	
	protected void createCleanTask() {
		createDefaultGroupTask(TASK_CLEAN_NAME, Delete.class, (task) -> {
			task.delete(extension.getOutputDir(), extension.getMutantsDir());
		});
	}
	
	protected void createAvailableDevicesTask() {
		createDefaultGroupTask(TASK_AVAILABLE_DEVICES_NAME, AvailableDevicesTask.class, (AvailableDevicesTask task) -> {
			task.setDeviceLister(pluginInternals.getDeviceLister());
		});
	}
	
	protected void createPimutInfoTask() {
		createDefaultGroupTask(TASK_PLUGIN_INFO_NAME, InfoTask.class);
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
		return new DefaultGroupAction<>(configAction, defaultGroup);
	}
	
	protected <T extends Task> Action<T> defaultGroupAction() {
		return new DefaultGroupAction<>((task) -> {}, defaultGroup);
	}
}
