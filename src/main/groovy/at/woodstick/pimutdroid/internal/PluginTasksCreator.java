package at.woodstick.pimutdroid.internal;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.Delete;

import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import at.woodstick.pimutdroid.task.AvailableDevicesTask;
import at.woodstick.pimutdroid.task.InfoTask;

public class PluginTasksCreator {

	public static final String TASK_CLEAN_NAME = "cleanMutation";
	public static final String TASK_AVAILABLE_DEVICES_NAME = "availableDevices";
	public static final String TASK_PLUGIN_INFO_NAME = "pimutInfo";
	
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
	}
	
	// ########################################################################
	
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
	
	protected <T extends Task> T createDefaultGroupTask(final String name, Class<T> taskClass, Action<T> configAction) {
		return taskFactory.create(name, taskClass, defaultGroupAction(configAction));
	}
	
	protected <T extends Task> T createDefaultGroupTask(final String name, Class<T> taskClass) {
		return taskFactory.create(name, taskClass, defaultGroupAction());
	}
	
	protected <T extends Task> Action<T> defaultGroupAction(final Action<T> configAction) {
		return new DefaultGroupAction<>(configAction, defaultGroup);
	}
	
	protected <T extends Task> Action<T> defaultGroupAction() {
		return new DefaultGroupAction<>((task) -> {}, defaultGroup);
	}
}
