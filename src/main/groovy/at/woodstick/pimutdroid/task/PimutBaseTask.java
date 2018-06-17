package at.woodstick.pimutdroid.task;

import java.nio.file.Paths;

import org.gradle.api.internal.AbstractTask;
//import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskAction;

import com.android.build.gradle.BaseExtension;

import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import at.woodstick.pimutdroid.internal.AdbCommandFactory;
import at.woodstick.pimutdroid.internal.AdbDeviceCommandBridge;
import at.woodstick.pimutdroid.internal.AndroidSerialProvider;
import at.woodstick.pimutdroid.internal.AppApk;
import at.woodstick.pimutdroid.internal.Device;
import at.woodstick.pimutdroid.internal.DeviceLister;
import at.woodstick.pimutdroid.internal.DeviceProvider;
import at.woodstick.pimutdroid.internal.DeviceTestOptionsProvider;
import at.woodstick.pimutdroid.internal.ListDevicesCommand;
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
		return new AppApk(extension.getApkAppOutputRootDir(), extension.getApkName());
	}
	
	protected AppApk getTestApk() {
		return new AppApk(extension.getApkTestOutputRootDir(), extension.getTestApkName());
	}
	
	protected AppApk getOriginalResultAppApk() {
		return new AppApk(extension.getAppResultRootDir(), extension.getApkName());
	}
	
	protected DeviceLister getDeviceLister() {
		return new DeviceLister(ListDevicesCommand.newInstance(androidExtension.getAdbExecutable()));
	}
	
	protected DeviceProvider getDeviceProvider() {
		return new DeviceProvider(extension.getDevices(), getDeviceLister(), new AndroidSerialProvider());
	}
	
	protected MarkerFileFactory getMarkerFileFactory() {
		return new MarkerFileFactory();
	}
	
	protected MutantClassFileFactory getMutantClassFileFactory() {
		return new MutantClassFileFactory(Paths.get(extension.getMutantClassesDir()));
	}
	
	protected AdbCommandFactory getAdbCommandFactory() {
		return AdbCommandFactory.newFactory(androidExtension.getAdbExecutable());
	}
	
	protected AdbDeviceCommandBridge getDeviceAdbCommandBridge(Device device) {
		return new AdbDeviceCommandBridge(device, getAdbCommandFactory());
	}
	
	protected DeviceTestOptionsProvider getDeviceTestOptionsProvider() {
		return new DeviceTestOptionsProvider(
			extension.getInstrumentationTestOptions(),
			"de.schroepf.androidxmlrunlistener.XmlRunListener"
		);
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
