package at.woodstick.pimutdroid.internal;

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import org.gradle.api.GradleException
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
		LOGGER.debug "Copy apk '${name}' from ${rootDir} to ${targetDir} under name '${newName}'"
		
		try {
			Files.copy(getPath(), targetDir.resolve(newName), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("{}", e);
			throw new GradleException(String.format("Failed to copy '%s' file to result dir location", name));
		}
	}
}
