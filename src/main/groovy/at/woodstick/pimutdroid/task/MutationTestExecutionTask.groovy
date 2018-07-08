package at.woodstick.pimutdroid.task;

import org.gradle.api.Action;
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerConfiguration
import org.gradle.workers.WorkerExecutor

import at.woodstick.pimutdroid.internal.AdbDeviceCommandBridge
import at.woodstick.pimutdroid.internal.AppApk
import at.woodstick.pimutdroid.internal.Device
import at.woodstick.pimutdroid.internal.DeviceProvider
import at.woodstick.pimutdroid.internal.DeviceTestOptionsProvider
import at.woodstick.pimutdroid.internal.MutationFilesProvider
import at.woodstick.pimutdroid.internal.RunTestOnDevice
import groovy.transform.CompileStatic

@CompileStatic
public class MutationTestExecutionTask extends PimutBaseTask {
	private static final Logger LOGGER = Logging.getLogger(MutationTestExecutionTask);
	
	private MutationFilesProvider mutationFilesProvider;
	private DeviceTestOptionsProvider deviceTestOptionsProvider;
	private DeviceProvider deviceProvider;

	private AppApk testApk;
	
	private String runner;
	private String appPackage;
	private String testPackage;
	
	private Collection<String> targetMutants;
	private String appResultRootDir;
	private String mutantResultRootDir;
	
	private String mutantTestResultFilename;
	
	@Override
	protected void beforeTaskAction() {
		if(mutantTestResultFilename == null) {
			mutantTestResultFilename = extension.getMutantTestResultFilename();
		}
		
		if(testApk == null) {
			testApk = getTestApk();
		}
		
		deviceProvider = getDeviceProvider();
		mutationFilesProvider = new MutationFilesProvider(getProject(), extension);
		deviceTestOptionsProvider = getDeviceTestOptionsProvider();
	}
	
	@Override
	protected void exec() {
		
		List<Device> deviceList = deviceProvider.getDevices();
		
		FileTree mutantApks = mutationFilesProvider.getMutantFiles(targetMutants, mutantResultRootDir, "**/*.apk");
		
		WorkerExecutor workerExecutor = getServices().get(WorkerExecutor.class);
		
		int numMutants = mutantApks.size();
		int numDevices = deviceList.size();
		List<String> fullMutantApkFilepathList = mutantApks.collect({ File file -> file.getPath().toString() }).toList();
		
		def mutantPartition = getMutantPathsPerDevice(fullMutantApkFilepathList, numDevices);

		LOGGER.quiet "Partition mutants ${numMutants} on ${numDevices} devices."
		LOGGER.debug "Partition: ${mutantPartition}"

		Integer testTimeout = extension.getTestTimeout();
				
		deviceList.eachWithIndex { Device device, int index ->
			def mutantApkFilepathList = mutantPartition.get(index);
			LOGGER.quiet "Submit worker for device '${device.getId()}'... work on '${mutantApkFilepathList.size()}' mutants"
			LOGGER.debug "Mutants to run ${mutantApkFilepathList} (index: ${index})"

			AdbDeviceCommandBridge deviceBridge = getDeviceAdbCommandBridge(device);
			
			workerExecutor.submit(RunTestOnDevice.class, new Action<WorkerConfiguration>() {
				@Override
				void execute(WorkerConfiguration config) {
					config.setIsolationMode(IsolationMode.NONE);
					config.setParams(
						deviceBridge, 
						deviceTestOptionsProvider.getOptions(), 
						mutantApkFilepathList, 
						testApk.getPath().toString(), 
						testPackage, 
						appPackage, 
						runner,
						mutantTestResultFilename
					);
				}
			});
		}
		
		workerExecutor.await();
		
		LOGGER.quiet "Workers finished."
	}

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

	public AppApk getTestApk() {
		return testApk;
	}

	public void setTestApk(AppApk testApk) {
		this.testApk = testApk;
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

	public String getRunner() {
		return runner;
	}

	public void setRunner(String runner) {
		this.runner = runner;
	}

	public String getMutantTestResultFilename() {
		return mutantTestResultFilename;
	}

	public void setMutantTestResultFilename(String mutantTestResultFilename) {
		this.mutantTestResultFilename = mutantTestResultFilename;
	}
}
