package at.woodstick.pimutdroid.internal;

import static at.woodstick.pimutdroid.internal.Utils.capitalize;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import at.woodstick.pimutdroid.configuration.BuildConfiguration;
import at.woodstick.pimutdroid.task.MutateClassesTask;
import info.solidsoft.gradle.pitest.PitestPlugin;
import info.solidsoft.gradle.pitest.PitestPluginExtension;
import info.solidsoft.gradle.pitest.PitestTask;

public class MutateClassesTaskCreator {

	public static final String TASK_MUTATE_CLASSES_NAME = "mutateClasses";
	
	static final Logger LOGGER = Logging.getLogger(MutateClassesTaskCreator.class);
	
	private final PimutdroidPluginExtension extension;
	private final PitestPluginExtension pitestExtension;
	private final TaskFactory taskFactory;

	public MutateClassesTaskCreator(PimutdroidPluginExtension extension, PitestPluginExtension pitestExtension, TaskFactory taskFactory) {
		this.extension = extension;
		this.pitestExtension = pitestExtension;
		this.taskFactory = taskFactory;
	}
	
	// ########################################################################
	
	public void createTasksForBuildConfiguration(BuildConfiguration config) {
		String configName = config.getName();
		String configUppercaseName = capitalize(configName);
		
		config.setMaxMutationsPerClass(getMaxMutationsPerClassForConfig(config));
		config.setTargetMutants(getTargetedMutantsForConfig(config));
		config.setMutators(getMutatorsForConfig(config));
		
		createMutateClassesTask(TASK_MUTATE_CLASSES_NAME + configUppercaseName, config);
	}
	
	// ########################################################################
	
	public void createMutateClassesTask() {
		BuildConfiguration standardConfig = new BuildConfiguration("");
		standardConfig.setTargetMutants(extension.getInstrumentationTestOptions().getTargetMutants());
		createTasksForBuildConfiguration(standardConfig);
	}
	
	// ########################################################################
	
	protected Task createMutateClassesTask(final String taskName, BuildConfiguration config) {
		return createMutateClassesTask(taskName, config.getMaxMutationsPerClass(), config.getTargetMutants(), config.getMutators());
	}
	
	protected Task createMutateClassesTask(final String taskName, final Integer MaxMutationsPerClass, final Set<String> targetedMutants, final Set<String> mutators) {
		return taskFactory.create(taskName, MutateClassesTask.class, (task) -> {
			task.setTargetedMutants(targetedMutants);
			task.setMaxMutationsPerClass(MaxMutationsPerClass);
			task.setMutators(mutators);
			
			task.dependsOn(getPitestTaskName());
		});
	}
	
	// ########################################################################
	
	public void configureTasks(TaskGraphAdaptor graph) {
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
				pitestTaskInstance.setMutators(mutateClassesTaskInstance.getMutators());
			} else {
				LOGGER.error("Unable to configure pitest task from mutate classes task.");
			}
		}
	}
	
	// ########################################################################
	
	private Integer getMaxMutationsPerClass() {
		return pitestExtension.getMaxMutationsPerClass();
	}
	
	private Set<String> getMutators() {
		return pitestExtension.getMutators();
	}
	
	private Integer getMaxMutationsPerClassForConfig(BuildConfiguration config) {
		return config.getMaxMutationsPerClass() != null ? config.getMaxMutationsPerClass() : getMaxMutationsPerClass();
	}
	
	private Set<String> getTargetedMutantsForConfig(BuildConfiguration config) {
		Set<String> targetedMutants = config.getTargetMutants();
		
		if(targetedMutants == null || targetedMutants.isEmpty()) {
			targetedMutants = new HashSet<>();
			targetedMutants.addAll(extension.getInstrumentationTestOptions().getTargetMutants());
		}
		
		return targetedMutants;
	}
	
	private Set<String> getMutatorsForConfig(BuildConfiguration config) {
		Set<String> mutators = config.getMutators();
		
		if(mutators == null) {
			mutators = getMutators();
		}
		
		return mutators;
	}
	
	// ########################################################################

	private String getFlavorBuildTypeTaskPart() {
		return capitalize(extension.getProductFlavor()) + capitalize(extension.getTestBuildType());
	}
	
	protected String getPitestTaskName() {
		return PitestPlugin.PITEST_TASK_NAME + getFlavorBuildTypeTaskPart();
	}
}
