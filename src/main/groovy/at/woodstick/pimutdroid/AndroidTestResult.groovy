package at.woodstick.pimutdroid;

import java.nio.file.Path
import java.nio.file.Paths

import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import groovy.transform.CompileStatic

public class AndroidTestResult {
	private final static Logger LOGGER = Logging.getLogger(AndroidTestResult);
	
	private Project project;
	private String rootDir;

	public AndroidTestResult(Project project, String rootDir) {
		this.project = project;
		this.rootDir = rootDir;
	}
	
	public void copyTo(final String targetDir) {
		copyTo(Paths.get(targetDir));
	}
	
	public void copyTo(final Path targetDir) {
		FileTree testResult = project.fileTree(rootDir)
		
		LOGGER.lifecycle "Copy test results from ${rootDir} to ${targetDir}"
		
		project.copy {
			from testResult.files
			into targetDir
			include "**/*.xml"
		}
	}
}
