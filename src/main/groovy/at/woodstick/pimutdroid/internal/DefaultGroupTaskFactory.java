package at.woodstick.pimutdroid.internal;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.internal.ClosureBackedAction;
import org.gradle.api.tasks.TaskContainer;

import groovy.lang.Closure;

public class DefaultGroupTaskFactory extends TaskFactory {

	private String defaultTaskGroup;
	
	public DefaultGroupTaskFactory(TaskContainer taskContainer, String defaultTaskGroup) {
		super(taskContainer);
		this.defaultTaskGroup = defaultTaskGroup;
	}
	
	@Override
	public Task create(final String name) {
		return super.create(name, defaultGroupAction());
	}
	
	@Override
	public Task create(String name, Closure<?> configClosure) {
		return super.create(name, defaultGroupAction(configClosure));
	}
	
	@Override
	public Task create(String name, Action<Task> configAction) {
		return super.create(name, defaultGroupAction(configAction));
	}
	
	@Override
	public <T extends Task> T create(String name, Class<T> taskClass, Closure<?> configClosure) {
		return super.create(name, taskClass, defaultGroupAction(configClosure));
	}
	
	@Override
	public <T extends Task> T create(String name, Class<T> taskClass, Action<T> configAction) {
		return super.create(name, taskClass, defaultGroupAction(configAction));
	}
	
	@Override
	public <T extends Task> T create(String name, Class<T> taskClass) {
		return super.create(name, taskClass, defaultGroupAction());
	}
	
	protected <T extends Task> Action<T> defaultGroupAction(final Closure<?> configClosure) {
		return defaultGroupAction(new ClosureBackedAction<T>(configClosure));
	}
	
	protected <T extends Task> Action<T> defaultGroupAction(final Action<T> configAction) {
		return new DefaultGroupAction<>(configAction, defaultTaskGroup);
	}
	
	protected <T extends Task> Action<T> defaultGroupAction() {
		return new DefaultGroupAction<>((task) -> {}, defaultTaskGroup);
	}
}
