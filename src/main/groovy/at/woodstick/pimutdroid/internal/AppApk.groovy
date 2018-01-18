package at.woodstick.pimutdroid.internal;

import java.nio.file.Path
import java.nio.file.Paths

import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import groovy.transform.CompileStatic

// TODO: Hold file / path to apk
// TODO: as java class
public class AppApk {
	private final static Logger LOGGER = Logging.getLogger(AppApk);
	
	private Project project;
	private String rootDir;
	private String name;

	public AppApk(Project project, String rootDir, String name) {
		this.project = project;
		this.rootDir = rootDir;
		this.name = name;
	}

	public Path getPath() {
		return Paths.get(rootDir, name);
	}
	
	public String getName() {
		return name;
	}

	public void copyTo(final String targetDir) {
		copyTo(Paths.get(targetDir), name);
	}
	
	public void copyTo(final String targetDir, final String newName) {
		copyTo(Paths.get(targetDir), newName);
	}
	
	public void copyTo(final Path targetDir) {
		copyTo(targetDir, name);
	}
	
	public void copyTo(final Path targetDir, final String newName) {
		FileTree testResult = project.fileTree(rootDir);
		
		LOGGER.lifecycle "Copy apk from ${rootDir} to ${targetDir}"
		
		project.copy {
			from rootDir
			into targetDir
			include name
			rename(name, newName)
		}
	}
}
