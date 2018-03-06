package at.woodstick.pimutdroid.internal;

import static at.woodstick.pimutdroid.internal.Utils.capitalize;

import java.util.Optional;

import org.gradle.api.Action;
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
	private final String defaultTaskGroup;

	public MutateClassesTaskCreator(PimutdroidPluginExtension extension, PitestPluginExtension pitestExtension, TaskFactory taskFactory, String defaultTaskGroup) {
		this.extension = extension;
		this.pitestExtension = pitestExtension;
		this.taskFactory = taskFactory;
		this.defaultTaskGroup = defaultTaskGroup;
	}

	
	// ########################################################################
	
	public void createMutateClassesTask() {
		createMutateClassesTask(TASK_MUTATE_CLASSES_NAME);
	}
	
	protected void createMutateClassesTask(final String taskName) {
		createDefaultGroupTask(taskName, MutateClassesTask.class, (task) -> {
			task.dependsOn(getPitestTaskName());
			task.setTargetedMutants(extension.getInstrumentationTestOptions().getTargetMutants());
			task.setMaxMutationsPerClass(getMaxMutationsPerClass());
		});
	}
	
	protected void createMutateClassesTask(final String taskName, BuildConfiguration config) {
		createDefaultGroupTask(taskName, MutateClassesTask.class, (task) -> {
			task.dependsOn(getPitestTaskName());
			task.setTargetedMutants(config.getTargetMutants());
			task.setMaxMutationsPerClass(config.getMaxMutationsPerClass());
		});
	}
	
	public void createTasksForBuildConfiguration(BuildConfiguration config) {
		String configName = config.getName();
		String configUppercaseName = capitalize(configName);
		
		config.setMaxMutationsPerClass(getMaxMutationsPerClassForConfig(config));
		
		createMutateClassesTask(TASK_MUTATE_CLASSES_NAME + configUppercaseName, config);
	}
	
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
			} else {
				LOGGER.error("Unable to configure pitest task from mutate classes task.");
			}
		}
	}
	
	// ########################################################################
	
	private Integer getMaxMutationsPerClass() {
		return pitestExtension.getMaxMutationsPerClass();
	}
	
	private Integer getMaxMutationsPerClassForConfig(BuildConfiguration config) {
		return config.getMaxMutationsPerClass() != null ? config.getMaxMutationsPerClass() : getMaxMutationsPerClass();
	}
	
	// ########################################################################
	
	private <T extends Task> T createDefaultGroupTask(final String name, final Class<T> taskClass) {
		return taskFactory.create(name, taskClass, defaultGroupAction());
	}

	private <T extends Task> T createDefaultGroupTask(final String name, final Class<T> taskClass, final Action<T> configAction) {
		return taskFactory.create(name, taskClass, defaultGroupAction(configAction));
	}
	
	private Task createDefaultGroupTask(final String name) {
		return taskFactory.create(name, defaultGroupAction());
	}
	
	private Task createDefaultGroupTask(final String name, final Action<Task> configAction) {
		return taskFactory.create(name, defaultGroupAction(configAction));
	}
	
	private <T extends Task> Action<T> defaultGroupAction(final Action<T> configAction) {
		return new DefaultGroupAction<>(configAction, defaultTaskGroup);
	}
	
	private <T extends Task> Action<T> defaultGroupAction() {
		return new DefaultGroupAction<>((task) -> {}, defaultTaskGroup);
	}
	
	// ########################################################################

	private String getFlavorBuildTypeTaskPart() {
		return capitalize(extension.getProductFlavor()) + capitalize(extension.getTestBuildType());
	}
	
	protected String getPitestTaskName() {
		return PitestPlugin.PITEST_TASK_NAME + getFlavorBuildTypeTaskPart();
	}
}
