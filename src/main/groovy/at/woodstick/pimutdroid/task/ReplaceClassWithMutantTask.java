package at.woodstick.pimutdroid.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import at.woodstick.pimutdroid.internal.MarkerFileFactory;
import at.woodstick.pimutdroid.internal.MutantClassFile;
import at.woodstick.pimutdroid.internal.MutantClassFileFactory;
import at.woodstick.pimutdroid.internal.MutantMarkerFile;
import at.woodstick.pimutdroid.internal.MutationFilesProvider;

public class ReplaceClassWithMutantTask extends DefaultTask {

	static final Logger LOGGER = Logging.getLogger(ReplaceClassWithMutantTask.class);

	private static final String PROPERTY_NAME_MUID = "pimut.muid";
	
	private String muidPropertyName = PROPERTY_NAME_MUID;
	
	private MutationFilesProvider mutationFilesProvider;
	private MarkerFileFactory markerFileFactory;
	private MutantClassFileFactory mutantClassFileFactory;
	private Path compileClassDirPath;
	
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
		MutantClassFile mutantClassFile = mutantClassFileFactory.fromMarkerFile(mutantMarkerFile);
		
		final String mutantClassName = mutantClassFile.getClassName();
		
		Path compiledClassReplacePath = compileClassDirPath.resolve(mutantClassFile.getRelativePackageClassDirPath()).resolve(mutantClassName + ".class");
	
		LOGGER.lifecycle("Found file '{}'", mutantClassFile);
		LOGGER.lifecycle("Used for mutant class '{}'", mutantClassName);
		
		LOGGER.lifecycle("Copy mutant class file '{}'", mutantClassFile.getFile());
		LOGGER.lifecycle("Target class file '{}'", compiledClassReplacePath);
		
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

	public void setMutationFilesProvider(MutationFilesProvider mutationFilesProvider) {
		this.mutationFilesProvider = mutationFilesProvider;
	}

	public void setMarkerFileFactory(MarkerFileFactory markerFileFactory) {
		this.markerFileFactory = markerFileFactory;
	}

	public void setMutantClassFileFactory(MutantClassFileFactory mutantClassFileFactory) {
		this.mutantClassFileFactory = mutantClassFileFactory;
	}

	public void setCompileClassDirPath(Path compileClassDirPath) {
		this.compileClassDirPath = compileClassDirPath;
	}
}
