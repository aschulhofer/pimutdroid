package at.woodstick.pimutdroid.task;

import java.io.File;
import java.util.Set

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.os.OperatingSystem;

import at.woodstick.pimutdroid.internal.MuidProvider
import at.woodstick.pimutdroid.internal.MutationFilesProvider;
import groovy.transform.CompileStatic

@CompileStatic
public class BuildMutantsTask extends PimutBaseTask {
	
	private static final Logger LOGGER = Logging.getLogger(BuildMutantsTask.class);
	
	private Set<String> targetedMutants;
	private File gradleWrapper;
	
	private MutationFilesProvider mutationFilesProvider;
	
	@Override
	protected void beforeTaskAction() {
		if(targetedMutants == null) {
			targetedMutants = new HashSet<>();
		}
		
		if(gradleWrapper == null) {
			final String wrapperOSFile = OperatingSystem.current().isWindows() ? "gradlew.bat" : "gradlew";
			gradleWrapper = getProject().getRootDir().toPath().resolve(wrapperOSFile).toFile();
		}
		
		mutationFilesProvider = new MutationFilesProvider(getProject(), extension, targetedMutants);
	}
	
	@Override
	protected void exec() {
		FileTree mutantMarkerFiles = mutationFilesProvider.getMutantMarkerFiles();
		
		final String gradleWrapperCmd = gradleWrapper.getAbsolutePath();
		
		for(File markerFile : mutantMarkerFiles) {
			
			def mutantCmd = "$gradleWrapperCmd buildOnlyMutantApk -Ppimut.muid=${markerFile.getName()}";
			
			LOGGER.lifecycle(mutantCmd);

			def process = mutantCmd.execute();
			def sb = new StringBuffer();
			process.consumeProcessErrorStream(sb);
			
			LOGGER.lifecycle(process.text);
			LOGGER.lifecycle(sb.toString());
			
		}
	}

	@Input
	public Set<String> getTargetedMutants() {
		return targetedMutants;
	}

	public void setTargetedMutants(Set<String> targetedMutants) {
		this.targetedMutants = targetedMutants;
	}

	public File getGradleWrapper() {
		return gradleWrapper;
	}

	public void setGradleWrapper(File gradleWrapper) {
		this.gradleWrapper = gradleWrapper;
	}
}
