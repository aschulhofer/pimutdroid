package at.woodstick.pimutdroid.task;

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import at.woodstick.pimutdroid.MutantFile
import groovy.transform.CompileStatic

@CompileStatic
public class MutantTask extends DefaultTask {
	private final static Logger LOGGER = Logging.getLogger(MutantTask);
	
	private MutantFile mutantFile;
	private boolean copyApk;
	private boolean storeTestResults;
	
	@TaskAction
	void exec() {
		LOGGER.lifecycle "Create mutant apk ${mutantFile.getId()} for mutant class ${mutantFile.getName()}"
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
}
