package at.woodstick.pimutdroid.task;

import java.nio.file.Paths;

import org.gradle.api.internal.AbstractTask;
//import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskAction;

import com.android.build.gradle.BaseExtension;

import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import at.woodstick.pimutdroid.internal.AppApk;
import at.woodstick.pimutdroid.internal.DeviceLister;
import at.woodstick.pimutdroid.internal.MarkerFileFactory;
import at.woodstick.pimutdroid.internal.MutantClassFileFactory;

public abstract class PimutBaseTask extends AbstractTask {

	protected PimutdroidPluginExtension extension;
	protected BaseExtension androidExtension;

	protected PimutBaseTask() {
		super();
		contructed();
	}

	protected void contructed() {
		extension = getPluginExtension();
		androidExtension = getAndroidExtension();
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
	
	protected AppApk getAppApk() {
		return new AppApk(getProject(), extension.getApkAppOutputRootDir(), extension.getApkName());
	}
	
	protected DeviceLister getDeviceLister() {
		return new DeviceLister(androidExtension.getAdbExecutable());
	}
	
	protected MarkerFileFactory getMarkerFileFactory() {
		return new MarkerFileFactory();
	}
	
	protected MutantClassFileFactory getMutantClassFileFactory() {
		return new MutantClassFileFactory(Paths.get(extension.getMutantClassesDir()));
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
}
