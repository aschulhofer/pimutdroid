package at.woodstick.pimutdroid.task;

import org.gradle.api.internal.AbstractTask;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskAction;

import at.woodstick.pimutdroid.PimutdroidPluginExtension;

public abstract class PimutBaseTask extends AbstractTask {

	protected PimutdroidPluginExtension extension;
	
	protected PimutBaseTask() {
		super();
		contructed();
	}

	protected void contructed() {
		extension = getPluginExtension();
	}
	
	protected void beforeTaskAction() {
		
	}
	
	protected abstract void exec();
	
	@TaskAction
	protected void taskAction() {
		beforeTaskAction();
		exec();
	}
	
	protected PimutdroidPluginExtension getPluginExtension() {
		return getProject().getExtensions().findByType(TypeOf.typeOf(PimutdroidPluginExtension.class));
	}
}
