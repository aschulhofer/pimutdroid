package at.woodstick.pimutdroid.task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.os.OperatingSystem;

import at.woodstick.pimutdroid.internal.ConsoleCommand;
import at.woodstick.pimutdroid.internal.MutationFilesProvider;
import at.woodstick.pimutdroid.internal.PluginTasksCreator;

public class BuildMutantsTask extends PimutBaseTask {
	
	private static final Logger LOGGER = Logging.getLogger(BuildMutantsTask.class);

	private Path mutantBuildLogRootDir;
	private Set<String> targetedMutants;
	private File gradleWrapper;
	private boolean failBuildOnError = false;
	
	private MutationFilesProvider mutationFilesProvider;
	private Path mutantBuildLogFailedDir;
	private Path mutantBuildLogDir;
	
	@Override
	protected void beforeTaskAction() {
		if(mutantBuildLogRootDir == null) {
			mutantBuildLogRootDir = Paths.get(extension.getMutantBuildLogsDir());
		}
		
		mutantBuildLogFailedDir = mutantBuildLogRootDir.resolve("failed");
		mutantBuildLogDir = mutantBuildLogRootDir.resolve("info");
		
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
		final FileTree mutantMarkerFiles = mutationFilesProvider.getMutantMarkerFiles();
		
		LOGGER.lifecycle("Run with: {}", gradleWrapper);
		LOGGER.lifecycle("Log dir: {}", mutantBuildLogRootDir);
		
		try {
			Files.createDirectories(mutantBuildLogFailedDir);
			Files.createDirectories(mutantBuildLogDir);
		} catch (IOException e) {
			LOGGER.error("Unable to create log build directories {}, {}", mutantBuildLogFailedDir, mutantBuildLogDir);
			LOGGER.error("", e);
			throw new GradleException("Unable to create log build directories", e);
		}
		
		for(File markerFile : mutantMarkerFiles) {
			
			List<Object> mutantCmd = getBuildCommandList(markerFile.getName());
			
			ConsoleCommand cmd = new ConsoleCommand(mutantCmd);
			
			final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
			final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
			
			cmd.execute(stdout, stderr);
			
			if(cmd.hasErrorExitValue()) {
				writeLogFile(stderr, mutantBuildLogFailedDir, "error", markerFile);
				LOGGER.error("Mutant apk build failed (muid: {}).", markerFile.getName());
				
				if(failBuildOnError) {
					throw new GradleException(String.format("Mutant apk build failed (muid: %s)", markerFile.getName()));
				}
			} else {
				writeLogFile(stdout, mutantBuildLogDir, "info", markerFile);
				LOGGER.lifecycle("Mutant apk built (muid: {}).", markerFile.getName());
			}
			
		}
	}

	protected List<Object> getBuildCommandList(final String muid) {
		return Arrays.asList(
			gradleWrapper,
			PluginTasksCreator.TASK_BUILD_ONLY_MUTANT_APK_NAME,
			String.format("-Ppimut.muid=%s", muid)
		);
	}
	
	protected void writeLogFile(final ByteArrayOutputStream bufferToWrite, final Path rootDir, final String logFilenameSuffix, final File markerFile) {
		File logFile = rootDir.resolve(markerFile.getName().toLowerCase() + "-" + logFilenameSuffix + ".log").toFile();
		
		try(OutputStream fs = new FileOutputStream(logFile)) {
			bufferToWrite.writeTo(fs);
		} catch (IOException e) {
			LOGGER.warn("unable to create build logfile ({}) for mutant {}", logFilenameSuffix, markerFile.getName());
			LOGGER.debug("", e);
		} 
	}
	
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

	public boolean isFailBuildOnError() {
		return failBuildOnError;
	}

	public void setFailBuildOnError(boolean failBuildOnError) {
		this.failBuildOnError = failBuildOnError;
	}
}
