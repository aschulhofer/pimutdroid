package at.woodstick.pimutdroid.task;

import java.nio.file.Path
import java.nio.file.Paths

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import at.woodstick.pimutdroid.AndroidTestResult
import at.woodstick.pimutdroid.AppApk
import at.woodstick.pimutdroid.AppClassFiles
import at.woodstick.pimutdroid.MutantFile

//@CompileStatic
public class MutantTask extends DefaultTask {
	private final static Logger LOGGER = Logging.getLogger(MutantTask);
	
	private MutantFile mutantFile;
	private boolean copyApk;
	private boolean storeTestResults = false;
	private String mutantRootDir; 
	
	private AppClassFiles appClassFiles;
	private AndroidTestResult androidTestResult;
	private AppApk mutantApk;
	
	Path getMutantDir(String mutantRootDir) {
		return Paths.get("${mutantRootDir}/${mutantFile.getName()}/${mutantFile.getId()}");
	} 
	
	@TaskAction
	void exec() {
		LOGGER.lifecycle "Created mutant apk ${mutantFile.getId()} for mutant class ${mutantFile.getName()}"

		final Path mutantDir = getMutantDir(mutantRootDir);
		
		if(storeTestResults) {
			LOGGER.lifecycle "Connected test against mutant finished."
			LOGGER.lifecycle "Copy test results to ${mutantDir}"

			androidTestResult.copyTo(mutantDir);
		}
		
		if(copyApk) {
			LOGGER.lifecycle "Copy apk '${mutantApk.getName()}' to ${mutantDir}"
			
			mutantApk.copyTo(mutantDir);
		}
	}
	
	public MutantFile getMutantFile() {
		return mutantFile;
	}
	
	public void setMutantFile(MutantFile mutantFile) {
		this.mutantFile = mutantFile;
	}
	
	public boolean isCopyApk() {
		return copyApk;
	}
	
	public void setCopyApk(boolean copyApk) {
		this.copyApk = copyApk;
	}
	
	public boolean isStoreTestResults() {
		return storeTestResults;
	}

	public void setStoreTestResults(boolean storeTestResults) {
		this.storeTestResults = storeTestResults;
	}

	public AndroidTestResult getAndroidTestResult() {
		return androidTestResult;
	}

	public void setAndroidTestResult(AndroidTestResult androidTestResult) {
		this.androidTestResult = androidTestResult;
	}

	public AppClassFiles getAppClassFiles() {
		return appClassFiles;
	}

	public void setAppClassFiles(AppClassFiles appClassFiles) {
		this.appClassFiles = appClassFiles;
	}

	public AppApk getMutantApk() {
		return mutantApk;
	}

	public void setMutantApk(AppApk mutantApk) {
		this.mutantApk = mutantApk;
	}

	public String getMutantRootDir() {
		return mutantRootDir;
	}

	public void setMutantRootDir(String mutantRootDir) {
		this.mutantRootDir = mutantRootDir;
	}
}