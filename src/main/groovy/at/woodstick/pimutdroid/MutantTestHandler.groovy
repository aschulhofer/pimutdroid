package at.woodstick.pimutdroid

import java.nio.file.Path
import java.nio.file.Paths

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.internal.os.OperatingSystem

class MutantTestHandler {
	
	private final static Logger LOGGER = Logging.getLogger(MutantTestHandler);
	
	private Project project;
	
	public MutantTestHandler(Project project) {
		this.project = project;
	}
	
	public void execute(int numMutants, boolean outputConsole = false) {
		def mutantIds = 0..(numMutants-1);
		def mutants = mutantIds.collect { "mutant" + it }

		final String wrapperOSFile = OperatingSystem.current().isWindows() ? "gradlew.bat" : "gradlw";
		
		File gradleWrapper = project.rootDir.toPath().resolve(wrapperOSFile).toFile();
		
		def cmd = gradleWrapper.absolutePath;
		
		LOGGER.info cmd;
		LOGGER.info "$mutants";

		mutants.each { mutant ->
			def mutantCmd = "$cmd $mutant";

			LOGGER.lifecycle mutantCmd;

			def process = mutantCmd.execute();
			def sb = new StringBuffer();
			process.consumeProcessErrorStream(sb);

			if(outputConsole) {
				process.inputStream.eachLine {LOGGER.lifecycle it}
			}
			else {
				LOGGER.lifecycle process.text;
			}

			LOGGER.warn sb.toString();
		}
	}
}
