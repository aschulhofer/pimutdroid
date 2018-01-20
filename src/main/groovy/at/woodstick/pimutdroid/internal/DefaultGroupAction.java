package at.woodstick.pimutdroid.internal;

import org.gradle.api.Action;
import org.gradle.api.Task;

public class DefaultGroupAction<T extends Task> implements Action<T> {

	private final Action<T> configAction;
	private final String defaultGroup;
	
	public DefaultGroupAction(Action<T> configAction, String defaultGroup) {
		this.configAction = configAction;
		this.defaultGroup = defaultGroup;
	}

	@Override
	public void execute(T task) {
		task.setGroup(defaultGroup);
		
		configAction.execute(task);
	}
}
