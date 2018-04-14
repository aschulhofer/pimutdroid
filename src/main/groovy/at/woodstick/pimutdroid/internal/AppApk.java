package at.woodstick.pimutdroid.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class AppApk {
	private final static Logger LOGGER = Logging.getLogger(AppApk.class);
	
	private String rootDir;
	private String name;

	public AppApk(String rootDir, String name) {
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
		LOGGER.debug("Copy apk '{}' from {} to {} under name '{}'", name, rootDir, targetDir, newName);
		
		try {
			Files.copy(getPath(), targetDir.resolve(newName), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("{}", e);
			throw new GradleException(String.format("Failed to copy '%s' file to result dir location", name));
		}
	}
}
