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

import at.woodstick.pimutdroid.internal.MarkerFileFactory;
import at.woodstick.pimutdroid.internal.MuidProvider;
import at.woodstick.pimutdroid.internal.MutantClassFile;
import at.woodstick.pimutdroid.internal.MutantClassFileFactory;
import at.woodstick.pimutdroid.internal.MutantMarkerFile;
import at.woodstick.pimutdroid.internal.MutationFilesProvider;

public class ReplaceClassWithMutantTask extends PimutBaseTask {

	static final Logger LOGGER = Logging.getLogger(ReplaceClassWithMutantTask.class);

	private String muidPropertyName;
	private Path compileClassDirPath;
	
	private MutationFilesProvider mutationFilesProvider;
	private MarkerFileFactory markerFileFactory;
	private MutantClassFileFactory mutantClassFileFactory;
	private String muid;
	
	@Override
	protected void beforeTaskAction() {
		markerFileFactory = getMarkerFileFactory();
		mutantClassFileFactory = getMutantClassFileFactory();
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
		MutantClassFile mutantClassFile = mutantClassFileFactory.fromMarkerFile(mutantMarkerFile);
		
		final String mutantClassName = mutantClassFile.getClassName();
		
		Path compiledClassReplacePath = compileClassDirPath.resolve(mutantClassFile.getRelativePackageClassDirPath()).resolve(mutantClassName + ".class");
	
		LOGGER.debug("Found file '{}'", mutantClassFile);
		LOGGER.debug("Used for mutant class '{}'", mutantClassName);
		
		LOGGER.debug("Copy mutant class file '{}'", mutantClassFile.getFile());
		LOGGER.debug("Target class file '{}'", compiledClassReplacePath);
		
		replaceClassFile(mutantClassFile.getFile().toPath(), compiledClassReplacePath);
	}

	protected void replaceClassFile(Path mutantClassFile, Path originalClassFile) {
		try {
			Files.copy(mutantClassFile, originalClassFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new GradleException(String.format("Failed to replace compiled class with mutant class for mutant id '%s'", muid), e);
		}
	}
	
	public void setMuidPropertyName(String muidPropertyName) {
		this.muidPropertyName = muidPropertyName;
	}

	public void setCompileClassDirPath(Path compileClassDirPath) {
		this.compileClassDirPath = compileClassDirPath;
	}
}
