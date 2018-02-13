package at.woodstick.pimutdroid.task;

import org.gradle.api.internal.AbstractTask;
//import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskAction;

import com.android.build.gradle.BaseExtension;

import at.woodstick.pimutdroid.PimutdroidInternalPluginExtension;
import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import at.woodstick.pimutdroid.internal.PluginInternals;

public abstract class PimutBaseTask extends AbstractTask {

	protected PimutdroidPluginExtension extension;
	protected BaseExtension androidExtension;
	
	protected PluginInternals internals;
	
	private PimutdroidInternalPluginExtension internalExtension;

	protected PimutBaseTask() {
		super();
		contructed();
	}

	protected void contructed() {
		extension = getPluginExtension();
		androidExtension = getAndroidExtension();
		
		internalExtension = getInternalExtension();
		internals = internalExtension.getPluginInternals();
	}
	
	// ########################################################################
	
	protected void beforeTaskAction() { }
	
	protected abstract void exec();
	
	@TaskAction
	protected void taskAction() {
		beforeTaskAction();
		exec();
	}

	// ########################################################################
	
	protected PimutdroidPluginExtension getPluginExtension() {
//		return getProject().getExtensions().findByType(TypeOf.typeOf(PimutdroidPluginExtension.class));
		return getProject().getExtensions().findByType(PimutdroidPluginExtension.class);
	}
	
	protected BaseExtension getAndroidExtension() {
//		return getProject().getExtensions().findByType(TypeOf.typeOf(BaseExtension.class));
		return getProject().getExtensions().findByType(BaseExtension.class);
	}
	
	// ########################################################################
	
	private PimutdroidInternalPluginExtension getInternalExtension() {
//		return getProject().getExtensions().findByType(TypeOf.typeOf(PimutdroidInternalPluginExtension.class));
		return getProject().getExtensions().findByType(PimutdroidInternalPluginExtension.class);
	}
}
