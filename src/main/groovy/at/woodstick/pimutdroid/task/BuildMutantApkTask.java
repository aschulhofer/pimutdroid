package at.woodstick.pimutdroid.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import at.woodstick.pimutdroid.internal.AppApk;
import at.woodstick.pimutdroid.internal.MuidProvider;
import at.woodstick.pimutdroid.internal.MutantOutputLocation;
import at.woodstick.pimutdroid.internal.MutationFilesProvider;

public class BuildMutantApkTask extends PimutBaseTask {
	
	static final Logger LOGGER = Logging.getLogger(BuildMutantApkTask.class);
	
	private String muidPropertyName;
	
	private Path mutantClassFilesRootDirPath;
	private Path mutantResultRootDirPath;

	private AppApk mutantApk;
	
	private MutationFilesProvider mutationFilesProvider;
	private String muid;
	
	@Override
	protected void beforeTaskAction() {
		mutantApk = getAppApk();
		mutationFilesProvider = new MutationFilesProvider(getProject(), extension);
		
		if(muidPropertyName == null) {
			muidPropertyName = extension.getMuidProperty();
		}
		
		if(mutantClassFilesRootDirPath == null) {
			mutantClassFilesRootDirPath = Paths.get(extension.getMutantClassesDir());
		}
		
		if(mutantResultRootDirPath == null) {
			mutantResultRootDirPath = Paths.get(extension.getMutantResultRootDir());
		}
		
		MuidProvider muidProvider = new MuidProvider(getProject(), muidPropertyName);
		muid = muidProvider.getMuid();
	}
	
	@Override
	protected void exec() {
		LOGGER.debug("Get mutant with id {}", muid);
		
		FileTree mutantMarkerFiletree = mutationFilesProvider.getMutantFileByName(muid);
		
		if(mutantMarkerFiletree.isEmpty()) {
			throw new GradleException(String.format("No marker file for mutant id '%s'", muid));
		}
		
		File markerFile = mutantMarkerFiletree.getSingleFile();
		
		MutantOutputLocation outputLocation = new MutantOutputLocation(mutantClassFilesRootDirPath, mutantResultRootDirPath, markerFile);
		
		try {
			outputLocation.createDirectory();
		} catch (IOException e) {
			LOGGER.error("{}", e);
		}
		
		Path targetDirPath = outputLocation.getTargetDirPath();
		
		LOGGER.debug("Copy mutant app from '{}'", mutantApk.getPath());
		LOGGER.debug("To path '{}'", targetDirPath);
		
		mutantApk.copyTo(targetDirPath);
	
		LOGGER.debug("Copy marker file '{}' To path '{}'", markerFile.toPath(), targetDirPath);
		
		try {
			outputLocation.copyMarkerFile();
		} catch (IOException e) {
			LOGGER.error("{}", e);
			throw new GradleException(String.format("Failed to copy '%s' marker file to result dir location", muid));
		}
		
		LOGGER.lifecycle("Built mutant apk '{}'.", muid);
	}

	public void setMuidPropertyName(String muidPropertyName) {
		this.muidPropertyName = muidPropertyName;
	}

	public void setMutantClassFilesRootDirPath(Path mutantClassFilesRootDirPath) {
		this.mutantClassFilesRootDirPath = mutantClassFilesRootDirPath;
	}

	public void setMutantResultRootDirPath(Path mutantResultRootDirPath) {
		this.mutantResultRootDirPath = mutantResultRootDirPath;
	}
}
