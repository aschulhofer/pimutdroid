package at.woodstick.pimutdroid.task;

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerConfiguration
import org.gradle.workers.WorkerExecutor
import org.gradle.api.Action;

import at.woodstick.pimutdroid.internal.AdbCommand
import at.woodstick.pimutdroid.internal.AppApk
import at.woodstick.pimutdroid.internal.Device
import at.woodstick.pimutdroid.internal.DeviceLister
import at.woodstick.pimutdroid.internal.MutationFilesProvider
import at.woodstick.pimutdroid.internal.RunTestOnDevice
import groovy.transform.CompileStatic

@CompileStatic
public class MutationTestExecutionTask extends DefaultTask {
	private static final Logger LOGGER = Logging.getLogger(MutationTestExecutionTask);
	
	private File adbExecuteable;

	private DeviceLister deviceLister;
	private MutationFilesProvider mutationFilesProvider;
	private AppApk testApk;
	private AppApk appApk;
	
	private String appPackage;
	private String testPackage;
	
	private Collection<String> targetMutants;
	private String appResultRootDir;
	private String mutantResultRootDir;
	
	private List<List<String>> getMutantPathsPerDevice(List<String> fullMutantApkFilepathList, int numDevices) {
		int numMutants = fullMutantApkFilepathList.size();
		int partitionSize = (int)(numMutants / numDevices);
		int remainderSize = (numMutants % numDevices);
		
		List<List<String>> mutantPartition = fullMutantApkFilepathList.collate(partitionSize);
		List<String> remainderList = (remainderSize > 0) ? mutantPartition.pop() : new ArrayList<String>();
		
		remainderList.eachWithIndex { String path, int index ->
			mutantPartition.get(index).add(path);
		}
		
		return mutantPartition;
	}
	
	@TaskAction
	void exec() {
		FileTree mutantApks = mutationFilesProvider.getMutantFiles(targetMutants, mutantResultRootDir, "**/*.apk");
		
		deviceLister.retrieveDevices();
		
		if(deviceLister.noDevicesConnected()) {
			throw new GradleException("No devices found");
		}
		
		WorkerExecutor workerExecutor = getServices().get(WorkerExecutor.class);
		
		int numMutants = mutantApks.size();
		int numDevices = deviceLister.getNumberOfDevices();
		List<String> fullMutantApkFilepathList = mutantApks.collect({ File file -> file.getPath().toString() }).toList();
		
		def mutantPartition = getMutantPathsPerDevice(fullMutantApkFilepathList, numDevices);

		LOGGER.quiet "Partition mutants ${numMutants} on ${numDevices} devices."
		LOGGER.quiet "Partition: ${mutantPartition}"
		
		deviceLister.getStoredDeviceList().eachWithIndex { Device device, int index ->
			def mutantApkFilepathList = mutantPartition.get(index);
			LOGGER.quiet "Submit worker for device '${device.getId()}..."
			LOGGER.quiet "Mutants to run ${mutantApkFilepathList} (index: ${index})"

			workerExecutor.submit(RunTestOnDevice.class, new Action<WorkerConfiguration>() {
				@Override
				void execute(WorkerConfiguration config) {
					config.setIsolationMode(IsolationMode.NONE);
					config.setParams(
						device, adbExecuteable, mutantApkFilepathList, testApk.getPath().toString(), testPackage, appPackage
					);
				}
			});
		}
		
		workerExecutor.await();
		
		LOGGER.quiet "Workers finished."
	}

	public File getAdbExecuteable() {
		return adbExecuteable;
	}

	public void setAdbExecuteable(File adbExecuteable) {
		this.adbExecuteable = adbExecuteable;
	}

	public DeviceLister getDeviceLister() {
		return deviceLister;
	}

	public void setDeviceLister(DeviceLister deviceLister) {
		this.deviceLister = deviceLister;
	}

	public MutationFilesProvider getMutationFilesProvider() {
		return mutationFilesProvider;
	}

	public void setMutationFilesProvider(MutationFilesProvider mutationFilesProvider) {
		this.mutationFilesProvider = mutationFilesProvider;
	}

	public AppApk getTestApk() {
		return testApk;
	}

	public void setTestApk(AppApk testApk) {
		this.testApk = testApk;
	}

	public AppApk getAppApk() {
		return appApk;
	}

	public void setAppApk(AppApk appApk) {
		this.appApk = appApk;
	}

	public Collection<String> getTargetMutants() {
		return targetMutants;
	}

	public void setTargetMutants(Collection<String> targetMutants) {
		this.targetMutants = targetMutants;
	}

	public String getAppResultRootDir() {
		return appResultRootDir;
	}

	public void setAppResultRootDir(String appResultRootDir) {
		this.appResultRootDir = appResultRootDir;
	}

	public String getMutantResultRootDir() {
		return mutantResultRootDir;
	}

	public void setMutantResultRootDir(String mutantResultRootDir) {
		this.mutantResultRootDir = mutantResultRootDir;
	}

	public String getAppPackage() {
		return appPackage;
	}

	public void setAppPackage(String appPackage) {
		this.appPackage = appPackage;
	}

	public String getTestPackage() {
		return testPackage;
	}

	public void setTestPackage(String testPackage) {
		this.testPackage = testPackage;
	}
}
