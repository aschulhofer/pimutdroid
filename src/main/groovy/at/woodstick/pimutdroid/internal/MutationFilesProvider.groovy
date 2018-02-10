package at.woodstick.pimutdroid.internal;

import org.gradle.api.Project
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import at.woodstick.pimutdroid.PimutdroidPluginExtension;
import groovy.transform.CompileStatic

@CompileStatic
public class MutationFilesProvider {
	
	private static final Logger LOGGER = Logging.getLogger(MutationFilesProvider);

	private Project project;
	private PimutdroidPluginExtension extension;
	private Set<String> targetedMutants;
	
	public MutationFilesProvider(Project project, PimutdroidPluginExtension extension) {
		this(project, extension, extension.instrumentationTestOptions.targetMutants);
	}
	
	public MutationFilesProvider(Project project, PimutdroidPluginExtension extension, Set<String> targetedMutants) {
		this.project = project;
		this.extension = extension;
		this.targetedMutants = targetedMutants;
	}

	public FileTree getMutantFiles(Collection<String> targetMutants, String mutantsDir, String mutantTargetGlob) {
		Set<String> includes = targetMutants.collect { mutantGlob ->
			mutantGlob = mutantGlob.replaceAll("\\.", "/") + "/" + mutantTargetGlob;
			mutantGlob
		}.toSet();
		
		LOGGER.debug("Include ${includes} mutant files");
		
		FileTree mutantsTask = project.fileTree(
			dir: mutantsDir,
			includes: includes
		);
		
		LOGGER.debug("Found ${mutantsTask} mutant files");
		
		return mutantsTask;
	}
	
	public FileTree getAllMutantClassFiles() {
		return getMutantFiles(["**"], extension.mutantsDir, "**/mutants/**/*.class");
	}
	
	public FileTree getMutantFileByName(final String filenameWithExtension) {
		return getMutantFiles(["**"], extension.mutantsDir, "**/mutants/**/" + filenameWithExtension);
	}
	
	public FileTree getMutantMarkerFiles() {
		return getMutantFiles(targetedMutants, extension.mutantsDir, "**/mutants/**/*." + MarkerFileFactory.FILE_EXTENSION);
	}
	
	public FileTree getMutantClassFiles() {
		return getMutantFiles(targetedMutants, extension.mutantsDir, "**/mutants/**/*.class");
    }
	
	public FileTree getMutantResultTestFiles() {
		return getMutantFiles(targetedMutants, extension.mutantResultRootDir, "**/*.xml");
	}
}
