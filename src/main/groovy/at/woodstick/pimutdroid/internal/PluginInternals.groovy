package at.woodstick.pimutdroid.internal;

import java.nio.file.Paths;

import org.apache.tools.ant.TaskAdapter
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.logging.Logger;

import com.android.build.gradle.BaseExtension;

import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import groovy.transform.CompileStatic;
import info.solidsoft.gradle.pitest.PitestPluginExtension

@CompileStatic
public class PluginInternals {

	private Project project;
	private PimutdroidPluginExtension extension;
	private BaseExtension androidExtension;
	private PitestPluginExtension pitestExtension;
	
	private TaskFactory taskFactory;
	
	private File adbExecuteable;
	private MutationFilesProvider mutationFilesProvider;
	private MarkerFileFactory markerFileFactory;
	private MutantClassFileFactory mutantClassFileFactory;
	private DeviceTestOptionsProvider deviceTestOptionsProvider;
	private DeviceLister deviceLister;
	
	private AppClassFiles appClassFiles;
	private AndroidTestResult androidTestResult;
	private AppApk appApk;
	private AppApk appTestApk;
	private AppApk originalResultAppApk;
	
	
	public PluginInternals(Project project, PimutdroidPluginExtension extension, BaseExtension androidExtension, PitestPluginExtension pitestExtension) {
		this.project = project;
		this.extension = extension;
		this.androidExtension = androidExtension;
		this.pitestExtension = pitestExtension;
	}
	
	public Logger getProjectLogger() {
		return project.getLogger();
	}
	
	private TaskExecutionGraph getTaskGraph() {
		return project.getGradle().getTaskGraph();
	}
	
	public void whenTaskGraphReady(final Action<TaskGraphAdaptor> readyAction) {
		getTaskGraph().whenReady({ TaskExecutionGraph graph -> 
			readyAction.execute(TaskGraphAdaptor.forGraph(graph));
		});
	}
	
	/**
	 * Call after extension values are set.
	 * 
	 * @see Project#afterEvaluate
	 */
	public void initialize() {
		taskFactory = new TaskFactory(project.getTasks());
		
		adbExecuteable = androidExtension.getAdbExecutable();
		
		mutationFilesProvider = new MutationFilesProvider(project, extension, extension.getInstrumentationTestOptions().getTargetMutants());
		
		markerFileFactory = new MarkerFileFactory();
		
		mutantClassFileFactory = new MutantClassFileFactory(Paths.get(extension.getMutantsDir()));
		
		deviceLister = new DeviceLister(adbExecuteable);
		
		appClassFiles = new AppClassFiles(
			project, 
			extension.getClassFilesDir(), 
			"${extension.appResultRootDir}/backup/classes"
		);
		
		androidTestResult = new AndroidTestResult(project, extension.getTestResultDir());
		
		appApk = new AppApk(project, extension.getApkAppOutputRootDir(), "${project.name}-debug.apk");
		
		appTestApk = new AppApk(project, extension.getApkTestOutputRootDir(), "${project.name}-debug-androidTest.apk");
		
		deviceTestOptionsProvider = new DeviceTestOptionsProvider(
			extension.getInstrumentationTestOptions(),
			"de.schroepf.androidxmlrunlistener.XmlRunListener"
		);
		
		originalResultAppApk = new AppApk(project, extension.getAppResultRootDir(), "${project.name}-debug.apk");
	}

	public Integer getMaxMutationsPerClass() {
		return pitestExtension.getMaxMutationsPerClass();
	}
	
	public File getAdbExecuteable() {
		return adbExecuteable;
	}

	public MutationFilesProvider getMutationFilesProvider() {
		return mutationFilesProvider;
	}

	public MarkerFileFactory getMarkerFileFactory() {
		return markerFileFactory;
	}

	public MutantClassFileFactory getMutantClassFileFactory() {
		return mutantClassFileFactory;
	}

	public DeviceTestOptionsProvider getDeviceTestOptionsProvider() {
		return deviceTestOptionsProvider;
	}

	public DeviceLister getDeviceLister() {
		return deviceLister;
	}

	public AppClassFiles getAppClassFiles() {
		return appClassFiles;
	}

	public AndroidTestResult getAndroidTestResult() {
		return androidTestResult;
	}

	public AppApk getAppApk() {
		return appApk;
	}

	public AppApk getAppTestApk() {
		return appTestApk;
	}

	public AppApk getOriginalResultAppApk() {
		return originalResultAppApk;
	}

	public TaskFactory getTaskFactory() {
		return taskFactory;
	}
}
