package at.woodstick.pimutdroid.internal;

import static at.woodstick.pimutdroid.internal.Utils.capitalize;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.GradleBuild;
import org.gradle.tooling.GradleConnectionException;

import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import at.woodstick.pimutdroid.configuration.BuildConfiguration;
import at.woodstick.pimutdroid.task.AvailableDevicesTask;
import at.woodstick.pimutdroid.task.BuildMutantApkTask;
import at.woodstick.pimutdroid.task.BuildMutantsTask;
import at.woodstick.pimutdroid.task.CompiledClassesTask;
import at.woodstick.pimutdroid.task.InfoTask;
import at.woodstick.pimutdroid.task.MutationResultTask;
import at.woodstick.pimutdroid.task.MutationTestExecutionTask;
import at.woodstick.pimutdroid.task.PrepareMutantFilesTask;
import at.woodstick.pimutdroid.task.ReplaceClassWithMutantTask;
import info.solidsoft.gradle.pitest.PitestPlugin;

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
	public static final String TASK_CLEAN_OUTPUT_NAME = "cleanMutationOutput";
	public static final String TASK_CLEAN_MUTANT_CLASSES_NAME = "cleanMutantClasses";
	public static final String TASK_CLEAN_APPLICATION_FILES_NAME = "cleanMutantAppFiles";
	public static final String TASK_AVAILABLE_DEVICES_NAME = "availableDevices";
	public static final String TASK_PLUGIN_INFO_NAME = "pimutInfo";
	public static final String TASK_TEST_ALL_MUTANTS_NAME = "testAllMutants";
	public static final String TASK_GENERATE_MUTATION_RESULT_NAME = "generateMutationResult";
	public static final String TASK_TEST_ALL_MUTANTS_GENERATE_MUTATION_RESULT_NAME = "testAllMutantsGenerateMutationResult";
	public static final String TASK_RUN_MUTANT_TEST_GENERATE_MUTATION_RESULT_NAME = "runMutationTestsGenerateResult";
	public static final String TASK_MUTATE_CLASSES_NAME = MutateClassesTaskCreator.TASK_MUTATE_CLASSES_NAME;
	public static final String TASK_PRE_MUTATION_NAME = "preMutation";
	public static final String TASK_ALL_MUTANT_APKS_ONLY_NAME = "buildAllMutants";
	
	static final Logger LOGGER = Logging.getLogger(PluginTasksCreator.class);
	
	private final PimutdroidPluginExtension extension;
	private final PluginInternals pluginInternals;
	private final TaskFactory taskFactory;

	public PluginTasksCreator(PimutdroidPluginExtension extension, PluginInternals pluginInternals, TaskFactory taskFactory) {
		this.extension = extension;
		this.pluginInternals = pluginInternals;
		this.taskFactory = taskFactory;
	}

	public void createTasks() {
		createPimutInfoTask();
		
		createCleanTask();
		createCleanOutputTask();
		createCleanMutantClassesTask();
		createCleanApplicationFilesTask();

		createAvailableDevicesTask();
		
		createMutateAllTask();
		createMutationResultTask();
		createMutateAllAfterMutationTask();
		createPreMutationTask();
		createPrepareMutantFilesTask();
		createBeforeMutationTask();
		createAfterMutantTask();
		createBuildMutantApkTask();
		createBuildAllMutantsTask();
		createBuildMutantApksTask();
		createReplaceClassWithMutantTask();
		createPrepareMutationTask();
		createMutationTask();
		createMutationWithCleanTask();
		
		createRunMutatinoTestGenerateResultTask();
		
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
		String configUppercaseName = capitalize(configName);
		
		createMutationResultTask(TASK_GENERATE_MUTATION_RESULT_NAME + configUppercaseName, config);
		createBuildMutantsTask(TASK_BUILD_ALL_MUTANT_APKS_NAME + configUppercaseName, config);
		createBuildMutantApksTask(TASK_ALL_MUTANT_APKS_ONLY_NAME + configUppercaseName, configUppercaseName);
	}
	
	// ########################################################################
	
	protected void postCreateTasks() {
		taskFactory.named(getAndroidCompileSourcesTaskName()).finalizedBy(TASK_MUTATE_AFTER_COMPILE_NAME);
	}
	
	protected void configureTasks(TaskGraphAdaptor graph) {
		if(graph.hasNotTask(BuildMutantApkTask.class)) {
			LOGGER.lifecycle("Disable replace class with mutant class task (no mutant build task found)");
			taskFactory.named(TASK_MUTATE_AFTER_COMPILE_NAME).setEnabled(false);
		}
	}
	
	// ########################################################################
	
	protected void createListMutantClasses() {
		taskFactory.create(TASK_LIST_MUTANT_CLASSES_NAME, (task) -> {
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
		taskFactory.create(TASK_LIST_MUTANT_MARKER_NAME, (task) -> {
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
		taskFactory.create(TASK_LIST_MUTANT_XML_RESULT_NAME, (task) -> {
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
	
	protected void createBuildMutantApksTask() {
		createBuildMutantApksTask(TASK_ALL_MUTANT_APKS_ONLY_NAME, "");
	}
	
	protected void createBuildMutantApksTask(final String taskName, final String configName) {
		taskFactory.create(taskName, GradleBuild.class, (task) -> {
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
		taskFactory.create(TASK_PREPARE_MUTATION_GENERATE_TEST_RESULT_NAME, (task) -> {
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
					extension.getInstrumentationTestOptions().getRunner(),
					extension.getExpectedTestResultFilename()
				);
				
				rtod.run();
				
				pluginInternals.getProjectLogger().lifecycle("Connected tests finished. Storing expected results.");
				
			});
		});
	}
	
	protected void createPrepareMutationTask() {
		taskFactory.create(TASK_PREPARE_MUTATION_NAME, GradleBuild.class, (task) -> {
			task.setTasks(Arrays.asList(TASK_PRE_MUTATION_NAME, TASK_MUTATE_CLASSES_NAME, TASK_POST_MUTATION_NAME, TASK_PREPARE_MUTATION_GENERATE_TEST_RESULT_NAME));
		});
	}
	
	protected void createMutationTask() {
		taskFactory.create(TASK_MUTATION_NAME, GradleBuild.class, (task) -> {
			task.setTasks(Arrays.asList(TASK_PREPARE_MUTATION_NAME, TASK_BUILD_ALL_MUTANT_APKS_NAME, TASK_TEST_ALL_MUTANTS_NAME, TASK_GENERATE_MUTATION_RESULT_NAME));
		});
	}
	
	protected void createMutationWithCleanTask() {
		taskFactory.create(TASK_MUTATION_WITH_CLEAN_NAME, GradleBuild.class, (task) -> {
			task.setTasks(Arrays.asList(TASK_CLEAN_NAME, TASK_PREPARE_MUTATION_NAME, TASK_BUILD_ALL_MUTANT_APKS_NAME, TASK_TEST_ALL_MUTANTS_NAME, TASK_GENERATE_MUTATION_RESULT_NAME));
		});
	}
	
	protected void createMutateAllAfterMutationTask() {
		taskFactory.create(TASK_TEST_ALL_MUTANTS_GENERATE_MUTATION_RESULT_NAME, GradleBuild.class, (task) -> {
			task.setTasks(Arrays.asList(TASK_TEST_ALL_MUTANTS_NAME, TASK_GENERATE_MUTATION_RESULT_NAME));
		});
	}
	
	protected void createRunMutatinoTestGenerateResultTask() {
		taskFactory.create(TASK_RUN_MUTANT_TEST_GENERATE_MUTATION_RESULT_NAME, GradleBuild.class, (task) -> {
			task.setTasks(Arrays.asList(TASK_PRE_MUTATION_NAME, TASK_PREPARE_MUTATION_GENERATE_TEST_RESULT_NAME, TASK_TEST_ALL_MUTANTS_NAME, TASK_GENERATE_MUTATION_RESULT_NAME));
		});
	}
	
	protected void createBuildAllMutantsTask() {
		createBuildMutantsTask(TASK_BUILD_ALL_MUTANT_APKS_NAME);
	}
	
	protected void createBuildMutantsTask(final String taskName) {
		taskFactory.create(taskName, BuildMutantsTask.class, (task) -> {
			task.setTargetedMutants(extension.getInstrumentationTestOptions().getTargetMutants());
		});
	}
	
	protected void createBuildMutantsTask(final String taskName, BuildConfiguration config) {
		taskFactory.create(taskName, BuildMutantsTask.class, (task) -> {
			task.setTargetedMutants(config.getTargetMutants());
		});
	}
	
	protected void createReplaceClassWithMutantTask() {
		taskFactory.create(TASK_MUTATE_AFTER_COMPILE_NAME, ReplaceClassWithMutantTask.class, (task) -> {
			task.setCompileClassDirPath(Paths.get(extension.getClassFilesDir()));
		});
	}
	
	protected void createBuildMutantApkTask() {
		taskFactory.create(TASK_BUILD_ONLY_MUTANT_APK_NAME, BuildMutantApkTask.class, (task) -> {
			task.setMutantResultRootDirPath(Paths.get(extension.getMutantResultRootDir()));
			task.setMutantClassFilesRootDirPath(Paths.get(extension.getMutantClassesDir()));
			
			task.dependsOn(getAndroidAssembleAppTaskName());
			task.finalizedBy(TASK_AFTER_MUTANT_NAME);
		});
	}

	protected void createAfterMutantTask() {
		taskFactory.create(TASK_AFTER_MUTANT_NAME, CompiledClassesTask.class, (task) -> {
			task.setBackup(false);
		});
	}
	
	protected void createBeforeMutationTask() {
		taskFactory.create(TASK_BEFORE_MUTATION_NAME, CompiledClassesTask.class, (task) -> {
			task.setBackup(true);
			
			task.dependsOn(getAndroidAssembleAppTaskName());
		});
	}
	
	protected void createPrepareMutantFilesTask() {
		taskFactory.create(TASK_POST_MUTATION_NAME, PrepareMutantFilesTask.class, (task) -> {
			task.setMutantFilesProvider(pluginInternals.getMutationFilesProvider());
			task.setMarkerFileFactory(pluginInternals.getMarkerFileFactory());
		});
	}
	
	protected void createPreMutationTask() {
		taskFactory.create(TASK_PRE_MUTATION_NAME, (task) -> {
			task.doLast((ignore) -> {
				// Backup compiled debug class files
				pluginInternals.getAppClassFiles().backup();
				
				// Copy unmutated apk
				pluginInternals.getAppApk().copyTo(extension.getAppResultRootDir());
				
				// Copy test apk
				pluginInternals.getAppTestApk().copyTo(extension.getAppResultRootDir());
			});
			
			task.dependsOn(getAndroidAssembleAppTaskName(), getAndroidAssembleTestTaskName());
		});
	}

	protected void createMutateAllTask() {
		taskFactory.create(TASK_TEST_ALL_MUTANTS_NAME, MutationTestExecutionTask.class, (MutationTestExecutionTask task) -> {
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
	
	protected void createMutationResultTask() {
		createMutationResultTask(TASK_GENERATE_MUTATION_RESULT_NAME);
	}
	
	protected void createMutationResultTask(final String taskName) {
		taskFactory.create(taskName, MutationResultTask.class, (MutationResultTask task) -> {
			task.setOutputDir(extension.getMutantReportRootDir());
			task.setAppResultDir(extension.getAppResultRootDir());
			task.setMutantsResultDir(extension.getMutantResultRootDir());
			task.setTargetedMutants(extension.getInstrumentationTestOptions().getTargetMutants());
		});
	}
	
	protected void createMutationResultTask(final String taskName, final BuildConfiguration config) {
		taskFactory.create(taskName, MutationResultTask.class, (MutationResultTask task) -> {
			task.setOutputDir(extension.getMutantReportRootDir());
			task.setAppResultDir(extension.getAppResultRootDir());
			task.setMutantsResultDir(extension.getMutantResultRootDir());
			task.setTargetedMutants(config.getTargetMutants());
		});
	}
	
	protected void createCleanTask() {
		taskFactory.create(TASK_CLEAN_NAME, Delete.class, (task) -> {
			task.delete(extension.getOutputDir(), extension.getMutantClassesDir());
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
	
	protected void createAvailableDevicesTask() {
		taskFactory.create(TASK_AVAILABLE_DEVICES_NAME, AvailableDevicesTask.class);
	}
	
	protected void createPimutInfoTask() {
		taskFactory.create(TASK_PLUGIN_INFO_NAME, InfoTask.class);
	}
	
	// ########################################################################

	private String getFlavorBuildTypeTaskPart() {
		return capitalize(extension.getProductFlavor()) + capitalize(extension.getTestBuildType());
	}
	
	protected String getPitestTaskName() {
		return PitestPlugin.PITEST_TASK_NAME + getFlavorBuildTypeTaskPart();
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
