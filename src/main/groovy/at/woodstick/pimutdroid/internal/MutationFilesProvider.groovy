package at.woodstick.pimutdroid.internal;

import java.util.Collection;
import org.gradle.api.Project
import org.gradle.api.file.FileTree;

import at.woodstick.pimutdroid.PimutdroidPluginExtension;

public class MutationFilesProvider {

	private Project project;
	private PimutdroidPluginExtension extension;
	
	public MutationFilesProvider(Project project, PimutdroidPluginExtension extension) {
		this.project = project;
		this.extension = extension;
	}
	
	public FileTree getMutantClassFiles() {
		FileTree mutantsTask = getMutantFiles(extension.targetMutants, extension.mutantsDir, "**/mutants/**/*.class");
		
		// Skip inner classes
		if(extension.skipInnerClasses) {
			mutantsTask = mutantsTask.matching { exclude "**/*\$*.class" } ;
		}
		
        return mutantsTask;
    }
	
	public FileTree getMutantFiles(Collection<String> targetMutants, String mutantsDir, String mutantTargetGlob) {
		Set<String> includes = targetMutants.collect { mutantGlob ->
			mutantGlob = mutantGlob.replaceAll("\\.", "/") + "/" + mutantTargetGlob;
			mutantGlob
		}.toSet()
		
		FileTree mutantsTask = project.fileTree(
			dir: mutantsDir,
			includes: includes
		)
		
		return mutantsTask;
	}
	
}
