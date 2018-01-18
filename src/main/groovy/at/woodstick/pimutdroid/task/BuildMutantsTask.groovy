package at.woodstick.pimutdroid.task;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.os.OperatingSystem;

import at.woodstick.pimutdroid.internal.MutationFilesProvider;
import groovy.transform.CompileStatic

@CompileStatic
public class BuildMutantsTask extends DefaultTask {
	
	static final Logger LOGGER = Logging.getLogger(BuildMutantsTask.class);
	
	private MutationFilesProvider mutationFilesProvider;
	
	@TaskAction
	void exec() {
		
		FileTree mutantMarkerFiles = mutationFilesProvider.getMutantMarkerFiles();
		
		final String wrapperOSFile = OperatingSystem.current().isWindows() ? "gradlew.bat" : "gradlew";
		
		File gradleWrapper = getProject().getRootDir().toPath().resolve(wrapperOSFile).toFile();
		
		final String gradleWrapperCmd = gradleWrapper.getAbsolutePath();
		
		for(File markerFile : mutantMarkerFiles) {
			
			def mutantCmd = "$gradleWrapperCmd buildMutantApk -Ppimut.muid=${markerFile.getName()}";
			
			LOGGER.lifecycle mutantCmd;

			def process = mutantCmd.execute();
			def sb = new StringBuffer();
			process.consumeProcessErrorStream(sb);

//			if(outputConsole) {
//				process.inputStream.eachLine {LOGGER.lifecycle it}
//			}
//			else {
				LOGGER.lifecycle process.text;
//			}

			LOGGER.warn sb.toString();
		}
		
	}

	public void setMutationFilesProvider(MutationFilesProvider mutationFilesProvider) {
		this.mutationFilesProvider = mutationFilesProvider;
	}
}
