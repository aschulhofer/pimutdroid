package at.woodstick.pimutdroid.task;

import java.nio.file.Path
import java.nio.file.Paths

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import at.woodstick.pimutdroid.AndroidTestResult
import at.woodstick.pimutdroid.AppApk
import at.woodstick.pimutdroid.AppClassFiles
import at.woodstick.pimutdroid.MutantFile
import at.woodstick.pimutdroid.PimutdroidPlugin
import at.woodstick.pimutdroid.PimutdroidPluginExtension
import groovy.transform.CompileStatic

//@CompileStatic
public class MutantTask extends DefaultTask {
	private final static Logger LOGGER = Logging.getLogger(MutantTask);
	
	private MutantFile mutantFile;
	private boolean copyApk;
	private boolean storeTestResults = false;
	
	private AppClassFiles appClassFiles;
	private AndroidTestResult androidTestResult;
	private AppApk appApk;
	
	Path getMutantDir(String rootDir) {
		return Paths.get("${rootDir}/${mutantFile.getName()}/${mutantFile.getId()}");
	} 
	
	@TaskAction
	void exec() {
		PimutdroidPluginExtension extension = project.extensions[PimutdroidPlugin.PLUGIN_EXTENSION];
		
		LOGGER.lifecycle "Create mutant apk ${mutantFile.getId()} for mutant class ${mutantFile.getName()}"
		LOGGER.lifecycle "Connected test against mutant finished."
		
		final Path mutantDir = getMutantDir("${extension.outputDir}/mutants");
		
		if(storeTestResults) {
			LOGGER.lifecycle "Copy test results to ${mutantDir}"
			
			androidTestResult.copyTo(mutantDir);
		}
		
		if(copyApk) {
			LOGGER.lifecycle "Copy apk '${project.name}-debug.apk' to ${mutantDir}"
			
			appApk.copyTo(mutantDir);
		}
		
		// TODO: move to own task?
		appClassFiles.restore();
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

	public AppApk getAppApk() {
		return appApk;
	}

	public void setAppApk(AppApk appApk) {
		this.appApk = appApk;
	}
}
