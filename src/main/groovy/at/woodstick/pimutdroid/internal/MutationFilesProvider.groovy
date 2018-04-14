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
	private String mutantTestResultFilename;
	
	public MutationFilesProvider(Project project, PimutdroidPluginExtension extension) {
		this(project, extension, new HashSet<>());
	}
	
	public MutationFilesProvider(Project project, PimutdroidPluginExtension extension, Set<String> targetedMutants) {
		this(project, extension, targetedMutants, extension.getMutantTestResultFilename());
	}
	
	public MutationFilesProvider(Project project, PimutdroidPluginExtension extension, Set<String> targetedMutants, String mutantTestResultFilename) {
		this.project = project;
		this.extension = extension;
		this.targetedMutants = targetedMutants;
		this.mutantTestResultFilename = mutantTestResultFilename;
	}

	public FileTree getMutantFiles(Collection<String> targetMutants, String mutantsDir, String mutantTargetGlob) {
		Set<String> includes = targetMutants.collect { mutantGlob ->
			mutantGlob = mutantGlob.replaceAll("\\.", "/") + "/" + mutantTargetGlob;
			mutantGlob
		}.toSet();
		
		if(targetMutants.isEmpty()) {
			includes.add(mutantTargetGlob);
		}
		
		LOGGER.debug("Include ${includes} mutant files");
		
		FileTree mutantsTask = project.fileTree(
			dir: mutantsDir,
			includes: includes
		);
		
		LOGGER.debug("Found ${mutantsTask} mutant files");
		
		return mutantsTask;
	}
	
	public FileTree getAllMutantClassFiles() {
		return getMutantFiles(["**"], extension.mutantClassesDir, "**/mutants/**/*.class");
	}
	
	public FileTree getMutantFileByName(final String filenameWithExtension) {
		return getMutantFiles(["**"], extension.mutantClassesDir, "**/mutants/**/" + filenameWithExtension);
	}
	
	public FileTree getMutantMarkerFiles() {
		return getMutantMarkerFiles(extension.mutantClassesDir);
	}
	
	public FileTree getMutantMarkerFiles(String mutantsDir) {
		return getMutantFiles(targetedMutants, mutantsDir, "**/mutants/**/*." + MarkerFileFactory.FILE_EXTENSION);
	}
	
	public FileTree getMutantClassFiles() {
		return getMutantFiles(targetedMutants, extension.mutantClassesDir, "**/mutants/**/*.class");
    }
	
	public FileTree getMutantResultTestFiles() {
		return getMutantFiles(targetedMutants, extension.mutantResultRootDir, "**/${mutantTestResultFilename}");
	}
}
