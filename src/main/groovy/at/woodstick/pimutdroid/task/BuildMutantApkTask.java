package at.woodstick.pimutdroid.task;

import java.io.File;
import java.nio.file.Path;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import at.woodstick.pimutdroid.internal.AppApk;
import at.woodstick.pimutdroid.internal.MarkerFileFactory;
import at.woodstick.pimutdroid.internal.MutantMarkerFile;
import at.woodstick.pimutdroid.internal.MutationFilesProvider;

public class BuildMutantApkTask extends DefaultTask {
	
	static final Logger LOGGER = Logging.getLogger(BuildMutantApkTask.class);
	
	private static final String PROPERTY_NAME_MUID = "pimut.muid";
	
	private String muidPropertyName = PROPERTY_NAME_MUID;
	
	private MutationFilesProvider mutationFilesProvider;
	private MarkerFileFactory markerFileFactory;
	
	private Path mutantClassFilesRootDirPath;
	
	private Path mutantResultRootDirPath;
	private AppApk mutantApk;
	
	private String muid;
	
	protected String getMuid() {
		final Project project = getProject();
		
		if(!project.hasProperty(muidPropertyName)) {
			throw new GradleException(String.format("Property '%s' for mutant id not set", muidPropertyName));
		}
		
		final String muid = (String) getProject().property(muidPropertyName);
		
		return muid;
	}
	
	@TaskAction
	void exec() {
		
		muid = getMuid();
		
		LOGGER.lifecycle("Get mutant with id {}", muid);
		
		FileTree mutantMarkerFiletree = mutationFilesProvider.getMutantFileByName(muid);
		
		if(mutantMarkerFiletree.isEmpty()) {
			throw new GradleException(String.format("No marker file for mutant id '%s'", muid));
		}
		
		File markerFile = mutantMarkerFiletree.getSingleFile();
		
		MutantMarkerFile mutantMarkerFile = markerFileFactory.fromMarkerFile(markerFile);
		
		Path mutantDirPath = mutantMarkerFile.getFile().getParentFile().toPath();
		
		Path relativeMutantDirPath = mutantClassFilesRootDirPath.relativize(mutantDirPath);
		
		Path targetDirPath = mutantResultRootDirPath.resolve(relativeMutantDirPath);
		
		LOGGER.lifecycle("Copy mutant app from '{}'", mutantApk.getPath());
		LOGGER.lifecycle("To path '{}'", targetDirPath);
		
		mutantApk.copyTo(targetDirPath);
	}

	public void setMutationFilesProvider(MutationFilesProvider mutationFilesProvider) {
		this.mutationFilesProvider = mutationFilesProvider;
	}

	public void setMarkerFileFactory(MarkerFileFactory markerFileFactory) {
		this.markerFileFactory = markerFileFactory;
	}

	public void setMutantClassFilesRootDirPath(Path mutantClassFilesRootDirPath) {
		this.mutantClassFilesRootDirPath = mutantClassFilesRootDirPath;
	}

	public void setMutantResultRootDirPath(Path mutantResultRootDirPath) {
		this.mutantResultRootDirPath = mutantResultRootDirPath;
	}

	public void setMutantApk(AppApk mutantApk) {
		this.mutantApk = mutantApk;
	}
}
