package at.woodstick.pimutdroid

import java.nio.file.Path
import java.nio.file.Paths

import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem

class MutantTestHandler {
	
	private Project project;
	
	public MutantTestHandler(Project project) {
		this.project = project;
	}
	
	public void execute(int numMutants, boolean outputConsole = false) {
		def mutantIds = 0..(numMutants-1)
		def mutants = mutantIds.collect { "mutant" + it }

		def wrapperOSFile = OperatingSystem.current().isWindows() ? "gradlew.bat" : "gradlw";
		
		File gradleWrapper = project.rootDir.toPath().resolve(wrapperOSFile).toFile();
		
		def cmd = gradleWrapper.absolutePath;
		
		println cmd
		println mutants

		mutants.each { mutant ->
			def mutantCmd = cmd + " " + mutant

			println mutantCmd

			def process = mutantCmd.execute()
			def sb = new StringBuffer()
			process.consumeProcessErrorStream(sb)

			if(outputConsole) {
				process.inputStream.eachLine {println it}
			}
			else {
				println process.text
			}

			println sb.toString()
		}
	}
}
