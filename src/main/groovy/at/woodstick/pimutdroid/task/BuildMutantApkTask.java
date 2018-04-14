package at.woodstick.pimutdroid.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import at.woodstick.pimutdroid.internal.AppApk;
import at.woodstick.pimutdroid.internal.MarkerFileFactory;
import at.woodstick.pimutdroid.internal.MuidProvider;
import at.woodstick.pimutdroid.internal.MutantMarkerFile;
import at.woodstick.pimutdroid.internal.MutationFilesProvider;

public class BuildMutantApkTask extends PimutBaseTask {
	
	static final Logger LOGGER = Logging.getLogger(BuildMutantApkTask.class);
	
	private String muidPropertyName;
	
	private Path mutantClassFilesRootDirPath;
	private Path mutantResultRootDirPath;

	private AppApk mutantApk;
	
	private MarkerFileFactory markerFileFactory;
	private MutationFilesProvider mutationFilesProvider;
	private String muid;
	
	@Override
	protected void beforeTaskAction() {
		mutantApk = getAppApk();
		markerFileFactory = getMarkerFileFactory();
		mutationFilesProvider = new MutationFilesProvider(getProject(), extension);
		
		if(muidPropertyName == null) {
			muidPropertyName = extension.getMuidProperty();
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
		
		MutantMarkerFile mutantMarkerFile = markerFileFactory.fromMarkerFile(markerFile);
		
		Path mutantDirPath = mutantMarkerFile.getFile().getParentFile().toPath();
		
		Path relativeMutantDirPath = mutantClassFilesRootDirPath.relativize(mutantDirPath);
		
		Path targetDirPath = mutantResultRootDirPath.resolve(relativeMutantDirPath);
		
		LOGGER.debug("Copy mutant app from '{}'", mutantApk.getPath());
		LOGGER.debug("To path '{}'", targetDirPath);
		
		try {
			Files.createDirectories(targetDirPath);
		} catch (IOException e) {
			LOGGER.error("{}", e);
		}
		
		mutantApk.copyTo(targetDirPath);
	
		LOGGER.debug("Copy marker file '{}' To path '{}'", markerFile.toPath(), targetDirPath);
		
		try {
			Files.copy(markerFile.toPath(), targetDirPath.resolve(mutantMarkerFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
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
