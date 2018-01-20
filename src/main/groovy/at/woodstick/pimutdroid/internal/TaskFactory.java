package at.woodstick.pimutdroid.internal;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.internal.ClosureBackedAction;
import org.gradle.api.tasks.TaskContainer;

import groovy.lang.Closure;

public class TaskFactory {

	private TaskContainer taskContainer;

	public TaskFactory(TaskContainer taskContainer) {
		this.taskContainer = taskContainer;
	}
	
	public boolean containsTask(final String taskName) {
		return ( taskContainer.findByName(taskName) != null );
	}
	
	public Task create(String name, Closure<?> configClosure) {
		return taskContainer.create(name, new ClosureBackedAction<Task>(configClosure));
	}
	
	public Task create(String name, Action<Task> configAction) {
		return taskContainer.create(name, configAction);
	}
	
	public <T extends Task> T create(String name, Class<T> taskClass, Closure<?> configClosure) {
		return taskContainer.create(name, taskClass, new ClosureBackedAction<T>(configClosure));
	}
	
	public <T extends Task> T create(String name, Class<T> taskClass, Action<T> configAction) {
		return taskContainer.create(name, taskClass, configAction);
	}
	
	public <T extends Task> T create(String name, Class<T> taskClass) {
		return taskContainer.create(name, taskClass);
	}
	
	public Task named(final String name) {
		return taskContainer.getByName(name);
	}
}
